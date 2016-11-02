package com.xttoday.bike2;

import android.util.Log;

import com.baidu.mapapi.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuyp on 15/12/24.
 * 本类用于处理路线，如将json格式的路线转化为可以显示在地图上的polyline,或者给定一个含有节点的列表，然后转化为符合要求的json格式。
 */
public class TraceLine {
    public List<LatLng> jsonToPolyline(String traceJSON){
        List<LatLng>points=new ArrayList<LatLng>();
        try{
            JSONTokener jsonParser=new JSONTokener(traceJSON);//JSON解析类
            JSONObject trace=(JSONObject)jsonParser.nextValue();
            JSONArray traceX=trace.getJSONArray("tracex");//获取
            JSONArray traceY=trace.getJSONArray("tracey");
            int lengthX=traceX.length();
            int lengthY=traceY.length();
            //Log.d("debugTest",Double.toString(traceX.getDouble(2)));
            if(lengthX==lengthY&&lengthX>0){
                double x,y;
                for(int j=0;j<lengthX;j++){
                    x=traceX.getDouble(j);
                    y=traceY.getDouble(j);
                    Log.d("debugTest", Double.toString(traceX.getDouble(j)));
                    points.add(new LatLng(x,y));
                }
            }
            return points;
        }catch(JSONException ex){
            return null;
        }
    }
}
