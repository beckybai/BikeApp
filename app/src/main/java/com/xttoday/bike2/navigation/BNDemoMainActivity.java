package com.xttoday.bike2.navigation;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRouteGuideManager;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.baidu.navisdk.adapter.BaiduNaviManager.NaviInitListener;
import com.baidu.navisdk.adapter.BaiduNaviManager.RoutePlanListener;
import com.baidu.navisdk.adapter.PackageUtil;
import com.baidu.navisdk.adapter.base.BaiduNaviSDKProxy;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.xttoday.bike2.R;
import com.xttoday.bike2.TraceLine;
import com.xttoday.bike2.app.AppConfig;
import com.xttoday.bike2.app.ErrorCode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import cz.msebera.android.httpclient.Header;

public class BNDemoMainActivity extends Activity {


	public static List<Activity> activityList = new LinkedList<Activity>();

	private static final String APP_FOLDER_NAME = "BNSDKSimpleDemo";

	private Button mWgsNaviBtn = null;
	private Button mGcjNaviBtn = null;
	private Button mBdmcNaviBtn = null;
	private Button mDb06ll = null;
	private String mSDCardPath = null;
	public static final String ROUTE_PLAN_NODE = "routePlanNode";
	public static final String SHOW_CUSTOM_ITEM = "showCustomItem";
	public static final String RESET_END_NODE = "resetEndNode";
	public static final String VOID_MODE = "voidMode";

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		activityList.add(this);
        Bundle bundle=getIntent().getExtras();
		String traceID=bundle.getString("traceID");

		setContentView(R.layout.activity_navigation_layout);
		mWgsNaviBtn = (Button) findViewById(R.id.wgsNaviBtn);
		mGcjNaviBtn = (Button) findViewById(R.id.gcjNaviBtn);
		mBdmcNaviBtn = (Button) findViewById(R.id.bdmcNaviBtn);
		mDb06ll = (Button) findViewById(R.id.mDb06llNaviBtn);
	
		//initListener();
		if (initDirs()) {
			initNavi();		
		}
        naviTrace(AppConfig.URL_DRAW_TRACE+"?traceID="+traceID);

	}
    public void naviTrace(String url){
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(int i,Header[]headers,byte[]bytes){
                //setTrace(new String(bytes));
                String traceJSON=new String(bytes);
                List<LatLng>points=new ArrayList<LatLng>();
                TraceLine tc=new TraceLine();//实例化路线类
                points=tc.jsonToPolyline(traceJSON);//将路线转化为可以使用的点列表
                if(points!=null) {
                    routeplanToNavi(CoordinateType.BD09LL,points);
                }
                else{
                    Log.d("error", ErrorCode.pointErr);
                }

            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                System.out.println("fail");
            }
        });
    }

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void initListener() {
	
//		if (mWgsNaviBtn != null) {
//			mWgsNaviBtn.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View arg0) {
//					if (BaiduNaviManager.isNaviInited()) {
//						routeplanToNavi(CoordinateType.WGS84);
//					}
//				}
//
//			});
//		}
//		if (mGcjNaviBtn != null) {
//			mGcjNaviBtn.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View arg0) {
//					if (BaiduNaviManager.isNaviInited()) {
//						routeplanToNavi(CoordinateType.GCJ02);
//					}
//				}
//
//			});
//		}
//		if (mBdmcNaviBtn != null) {
//			mBdmcNaviBtn.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View arg0) {
//
//					if (BaiduNaviManager.isNaviInited()) {
//						routeplanToNavi(CoordinateType.BD09_MC);
//					}
//				}
//			});
//		}
//
//		if (mDb06ll != null) {
//			mDb06ll.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View arg0) {
//					if (BaiduNaviManager.isNaviInited()) {
//						routeplanToNavi(CoordinateType.BD09LL);
//					}
//				}
//			});
//		}


	}


	private boolean initDirs() {
		mSDCardPath = getSdcardDir();
		if (mSDCardPath == null) {
			return false;
		}
		File f = new File(mSDCardPath, APP_FOLDER_NAME);
		if (!f.exists()) {
			try {
				f.mkdir();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	String authinfo = null;

	private void initNavi() {
		// BaiduNaviManager.getInstance().setNativeLibraryPath(mSDCardPath +
		// "/BaiduNaviSDK_SO");
	
	
		BNOuterTTSPlayerCallback ttsCallback = null;

		BaiduNaviManager.getInstance().init(this, mSDCardPath, APP_FOLDER_NAME, new NaviInitListener() {
			@Override
			public void onAuthResult(int status, String msg) {
				if (0 == status) {
					authinfo = "key校验成功!";
				} else {
					authinfo = "key校验失败, " + msg;
				}
				BNDemoMainActivity.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(BNDemoMainActivity.this, authinfo, Toast.LENGTH_LONG).show();
					}
				});
			}

			public void initSuccess() {
				Toast.makeText(BNDemoMainActivity.this, "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
			}

			public void initStart() {
				Toast.makeText(BNDemoMainActivity.this, "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
			}

			public void initFailed() {
				Toast.makeText(BNDemoMainActivity.this, "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
			}

		},  null/* null mTTSCallback */);
	}

	private String getSdcardDir() {
		if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().toString();
		}
		return null;
	}

	private void routeplanToNavi(CoordinateType coType,List<LatLng> points) {
		BNRoutePlanNode sNode = null;
		BNRoutePlanNode eNode = null;
        int length=points.size();
        List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
        sNode=new BNRoutePlanNode(points.get(0).longitude,points.get(0).latitude,"oo",null,coType);
        eNode=new BNRoutePlanNode(points.get(length-1).longitude,points.get(length-1).latitude,"oo",null,coType);
        list.add(sNode);
        list.add(eNode);
        BaiduNaviManager.getInstance().launchNavigator(this, list, 1, true, new DemoRoutePlanListener(sNode));
//		switch (coType) {
//			case GCJ02: {
//				sNode = new BNRoutePlanNode(116.30142, 40.05087, "百度大厦", null, coType);
//				eNode = new BNRoutePlanNode(116.39750, 39.90882, "北京天安门", null, coType);
//				break;
//			}
//			case WGS84: {
//				sNode = new BNRoutePlanNode(116.300821, 40.050969, "百度大厦", null, coType);
//				eNode = new BNRoutePlanNode(116.397491, 39.908749, "北京天安门", null, coType);
//				break;
//			}
//			case BD09_MC: {
//				sNode = new BNRoutePlanNode(12947471, 4846474, "百度大厦", null, coType);
//				eNode = new BNRoutePlanNode(12958160, 4825947, "北京天安门", null, coType);
//				break;
//			}
//			case BD09LL: {
//				sNode = new BNRoutePlanNode(116.30784537597782, 40.057009624099436, "百度大厦", null, coType);
//				eNode = new BNRoutePlanNode(116.40386525193937, 39.915160800132085, "北京天安门", null, coType);
//				break;
//			}
//			default:
//				;
//			}
	}

	public class DemoRoutePlanListener implements RoutePlanListener {

		private BNRoutePlanNode mBNRoutePlanNode = null;

		public DemoRoutePlanListener(BNRoutePlanNode node) {
			mBNRoutePlanNode = node;
		}

		@Override
		public void onJumpToNavigator() {
			/*
			 * 设置途径点以及resetEndNode会回调该接口
			 */
		 
			for (Activity ac : activityList) {
			   
				if (ac.getClass().getName().endsWith("BNDemoGuideActivity")) {
				 
					return;
				}
			}
			Intent intent = new Intent(BNDemoMainActivity.this, BNDemoGuideActivity.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable(ROUTE_PLAN_NODE, (BNRoutePlanNode) mBNRoutePlanNode);
			intent.putExtras(bundle);
			startActivity(intent);
			
		}

		@Override
		public void onRoutePlanFailed() {
			// TODO Auto-generated method stub
			Toast.makeText(BNDemoMainActivity.this, "算路失败", Toast.LENGTH_SHORT).show();
		}
	}

	private BNOuterTTSPlayerCallback mTTSCallback = new BNOuterTTSPlayerCallback() {

		@Override
		public void stopTTS() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "stopTTS");
		}

		@Override
		public void resumeTTS() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "resumeTTS");
		}

		@Override
		public void releaseTTSPlayer() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "releaseTTSPlayer");
		}

		@Override
		public int playTTSText(String speech, int bPreempt) {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "playTTSText" + "_" + speech + "_" + bPreempt);

			return 1;
		}

		@Override
		public void phoneHangUp() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "phoneHangUp");
		}

		@Override
		public void phoneCalling() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "phoneCalling");
		}

		@Override
		public void pauseTTS() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "pauseTTS");
		}

		@Override
		public void initTTSPlayer() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "initTTSPlayer");
		}

		@Override
		public int getTTSState() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "getTTSState");
			return 1;
		}
	};

}
