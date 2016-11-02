package com.xttoday.bike2.location;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.xttoday.bike2.R;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/***
 * 定位滤波demo，实际定位场景中，可能会存在很多的位置抖动，此示例展示了一种对定位结果进行的平滑优化处理
 * 实际测试下，该平滑策略在市区步行场景下，有明显平滑效果，有效减少了部分抖动，开放算法逻辑，希望能够对开发者提供帮助
 * 注意：该示例场景仅用于对定位结果优化处理的演示，里边相关的策略或算法并不一定适用于您的使用场景，请注意！！！
 *
 * @author baidu
 *
 */
public class LocationFilter extends Activity {
	private MapView mMapView = null;
	private BaiduMap mBaiduMap;
	private Button reset;
	private LocationService locService;
	private LinkedList<LocationEntity> locationList = new LinkedList<LocationEntity>(); // 存放历史定位结果的链表，最大存放当前结果的前5次定位结果
	private LinkedList<LocationEntity> uploadLists = new LinkedList<LocationEntity>();
	private BDLocation lastLocation = null;
	private int defaultDistance = 5;
	private Button bttime;
	private TextView txtminu;
	private TextView txtsec;
	private long timeusedinsec;
	private boolean isstop = false;
	private TextView txtv1;
	private TextView txtv2;
	int count;//目的是让下面point1=poin仅有效一次，防止报错
	LatLng poin,point,point1;
	Polyline line;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.acitivity_filter);
		mMapView = (MapView) findViewById(R.id.bmapView);
		reset = (Button) findViewById(R.id.clear);
		bttime = (Button)findViewById(R.id.btn_time);
		txtminu = (TextView)findViewById(R.id.txt_min);
		txtsec = (TextView)findViewById(R.id.txt_sec);
		txtv1 = (TextView)findViewById(R.id.txt_v1);
		txtv2 = (TextView)findViewById(R.id.txt_v2);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
		mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
				MyLocationConfiguration.LocationMode.NORMAL, true, null));
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(15));
		locService = ((LocationApplication) getApplication()).locationService;
		LocationClientOption mOption = locService.getDefaultLocationClientOption();
		mOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
		mOption.setCoorType("bd09ll");
		locService.setLocationOption(mOption);
		locService.registerListener(listener);
		locService.start();
	}

	/***
	 * 定位结果回调，在此方法中处理定位结果
	 */
	BDLocationListener listener = new BDLocationListener() {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// TODO Auto-generated method stub
			if(null != location && location.getLocType() != BDLocation.TypeServerError){
				//	WriteLog.getInstance().writeLog(location.getLongitude()+","+location.getLatitude());
				if(lastLocation == null){
					lastLocation = location;
//					logMsg("call back:"+location.getLongitude()+","+location.getLatitude()); //GPS初始阶段会存在定位失败情况
				}else{
					float[] distance = new float[1];
					Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(), location.getLatitude(), location.getLongitude(), distance);
					location.getTime();
					if(distance[0] >= defaultDistance){
						lastLocation = location;
//						logMsg("call back:"+location.getLongitude()+","+location.getLatitude());
					}
				}

				if (location != null && (location.getLocType() == 161 || location.getLocType() == 66)) {
					Message locMsg = locHander.obtainMessage();
					Bundle locData;
					locData = Algorithm(location);
					if (locData != null) {
						locData.putParcelable("loc", location);
						locMsg.setData(locData);
						locHander.sendMessage(locMsg);
					}
				}
			}
		}
	};

	/***
	 * 平滑策略代码实现方法，主要通过对新定位和历史定位结果进行速度评分，
	 * 来判断新定位结果的抖动幅度，如果超过经验值，则判定为过大抖动，进行平滑处理,若速度过快，
	 * 则推测有可能是由于运动速度本身造成的，则不进行低速平滑处理 ╭(●｀∀´●)╯
	 *
	 * @param
	 * @return Bundle
	 */
	private Bundle Algorithm(BDLocation location) {
		Bundle locData = new Bundle();
		double curSpeed = 0;
		boolean judge = true;
		boolean save = false;
		double score = 0;
		double newd = 0;
		LocationEntity newLocation = new LocationEntity();
		if (locationList.isEmpty() || locationList.size() < 2) {
			LocationEntity temp = new LocationEntity();
			temp.location = location;
			temp.speed = location.getSpeed();
			temp.time = System.currentTimeMillis();
			locData.putInt("iscalculate", 1);
			locationList.add(temp);
			uploadLists.add(temp);
			locData.putInt("haslastpoint", 0);
			save = true;
		} else {
			if (locationList.size() > 5)
				locationList.removeFirst();
			for (int i = 0; i < locationList.size(); ++i) {
				LatLng lastPoint = new LatLng(locationList.get(i).location.getLatitude(),
						locationList.get(i).location.getLongitude());
				LatLng curPoint = new LatLng(location.getLatitude(), location.getLongitude());
				double distance = DistanceUtil.getDistance(lastPoint, curPoint);
				Log.d("distance",String.valueOf(distance));
				curSpeed = distance / (System.currentTimeMillis() - locationList.get(i).time) * 1000;
				score += curSpeed * Utils.EARTH_WEIGHT[i];
				/*if (distance < defaultDistance)
					judge = false;
					*/
				newd = distance;
			}
			if ((score > 9.99 && score < 300 && judge)) { // 经验值,开发者可根据业务自行调整，也可以不使用这种算法
				location.setLongitude(
						(locationList.get(locationList.size() - 1).location.getLongitude() + location.getLongitude())
								/ 2);
				location.setLatitude(2);
				location.setLatitude(
						(locationList.get(locationList.size() - 1).location.getLatitude() + location.getLatitude())
								/ 2);
				location.setSpeed(((float) curSpeed + location.getSpeed()) / 2);
				locData.putInt("iscalculate", 1);
				locData.putInt("haslastpoint",1);
				poin = new LatLng((lastLocation.getLatitude()), (lastLocation.getLongitude()));
				locData.putParcelable("lastpoint",poin);

				//位置合理时，加入队列
				newLocation.location = location;
				newLocation.time = System.currentTimeMillis();
				locationList.add(newLocation);
				if(newd> 15 )
					uploadLists.add(newLocation);
			}
			else {
				locData.putInt("iscalculate", 0);
			}

			//*********************
				/*
				/* put the point into the list, declaration is at the end.you can prepare the test set like this :
				/*
			    	location.setLatitude(666);
			    	location.setLatitude(233);
			    	location.setSpeed(55);
			    	location.time =  System.currentTimeMillis();
					LocationEntity newLocation = new LocationEntity();
					newLocation.location = location;
					locationList.add(newLocation);

				/尽量减少存的点的数目
				*/


//point1获取的是上一次生成线条覆盖后末端的经纬度，用以下次生成线条作为起点

		}
		return locData;
	}

	/***
	 * 接收定位结果消息，并显示在地图上
	 */
	private Handler locHander = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			try {
				BDLocation location = msg.getData().getParcelable("loc");
				int iscal = msg.getData().getInt("iscalculate");
				int vel = msg.getData().getInt("haslastpoint");
				BDLocation lastlocation = msg.getData().getParcelable("lastpoint");
				if (location != null) {
					LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
					// 构建Marker图标
					BitmapDescriptor bitmap = null;
					if (iscal == 0) {
						//bitmap = BitmapDescriptorFactory.fromResource(R.drawable.bluedot); // 非推算结果
					} else {
						bitmap = BitmapDescriptorFactory.fromResource(R.drawable.bluedot); // 推算结果

						// 构建MarkerOption，用于在地图上添加Marker
						OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
						// 在地图上添加Marker，并显示
						mBaiduMap.addOverlay(option);
						mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(point));

						if(vel!=0) {
							poin = new LatLng((location.getLatitude()), (location.getLongitude()));//先得到一个定位点
							if (count == 0) {
								point1 = poin;//我这边测试如果point1第一次为空会闪退，所以赋一个坐标先
								count = 1;
							}
							point = new LatLng((location.getLatitude()), (location.getLongitude()));
							List<LatLng> points = new ArrayList<LatLng>();
							points.add(point1);
							points.add(point);
							PolylineOptions ooPolyline = new PolylineOptions().width(10)
									.color(0xAAFF0000).points(points);
							line = (Polyline) mBaiduMap.addOverlay(ooPolyline);
							point1 = new LatLng(ooPolyline.getPoints().get(1).latitude, ooPolyline.getPoints().get(1).longitude);
						}
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		//WriteLog.getInstance().close();
		locService.unregisterListener(listener);
		locService.stop();
		mMapView.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mMapView.onResume();
		bttime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mHandler.removeMessages(1);
				String aaa=bttime.getText().toString();
				if(aaa.equals("start")){
					mHandler.sendEmptyMessage(1);
					isstop = false;
					bttime.setText("pause");
				}else {
					mHandler.sendEmptyMessage(0);
					isstop = true;
					bttime.setText("start");
				}
			}
		});


		reset.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mBaiduMap != null)
					mBaiduMap.clear();
				txtminu.setText("00");
				txtsec.setText("00");
				bttime.setText("start");
				timeusedinsec = 0;
				isstop = true;
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mMapView.onPause();

	}

	/**
	 * 封装定位结果和时间的实体类
	 *
	 * @author baidu
	 *
	 */
	class LocationEntity {
		BDLocation location;
		long time;
		double speed;
	}


	private Handler mHandler = new Handler() {
		/*
         * edit by yuanjingchao 2014-08-04 19:10
         */
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
				case 1:
					// 添加更新ui的代码
					if (!isstop) {
						updateView();
						mHandler.sendEmptyMessageDelayed(1, 1000);
					}
					break;
				case 0:
					break;
			}
		}

	};

	private void updateView() {
		timeusedinsec += 1;
		int minute = (int) (timeusedinsec / 60)%60;
		int second = (int) (timeusedinsec % 60);
		if (minute < 10)
			txtminu.setText("0" + minute);
		else
			txtminu.setText("" + minute);
		if (second < 10)
			txtsec.setText("0" + second);
		else
			txtsec.setText("" + second);
	}
}
