package com.xttoday.bike2;
import com.xttoday.bike2.TraceObject;
import android.content.Intent;
import android.os.Trace;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.xttoday.bike2.app.AppConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;


public class searchTrace extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_trace);
        Intent it=getIntent();
        Bundle bd=it.getExtras();
        //System.out.println(bd.getString("startPlace")+bd.getString("endPlace"));
        searchTrace(bd.getString("startPlace"),bd.getString("endPlace"));
//        final String[] strs=new String[] {"1","2","3"};
//        final ArrayList<String> strs = new ArrayList<String>();
//        strs.add("3");
//
//        ListView lv = (ListView) findViewById(R.id.listView);//得到ListView对象的引用 /*为ListView设置Adapter来绑定数据*/
//        lv.setAdapter(new ArrayAdapter<String>(this,
//                android.R.layout.simple_list_item_1, strs));
//        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
//                //点击后在标题上显示点击了第几行                    setTitle("你点击了第"+arg2+"行");
//                Intent showTrace =new Intent(searchTrace.this,ShowTrace_Activity.class);
//                Bundle bundle=new Bundle();
//                bundle.putString("startPlace",Integer.toString(arg2+1));
//                showTrace.putExtras(bundle);
//                startActivity(showTrace);
//            }
//        });

    }

    public void searchTrace(String start,String end){//根据起始地点和终点来搜索符合条件的路线
        String url= AppConfig.URL_SEARCH_TRACE+"?startPlace="+start+"&endPlace="+end;
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(int i,Header[]headers,byte[]bytes){
                //setTrace(new String(bytes));
                String traceJSON=new String(bytes);//获得访问接口返回的数据
                System.out.println(traceJSON);
                //final String[] strs=new String[] {};
                final ArrayList<TraceObject> strs = new ArrayList<TraceObject>();
                JsonReader reader=new JsonReader(new StringReader(traceJSON));//json解析
                try {
                    reader.beginArray();
                    while(reader.hasNext()){
                        reader.beginObject();
//                        String startPlace="";
//                        String endPlace="";
//                        String ID="";
                        String tagName;
                        TraceObject thisTraceObject=new TraceObject();//新建一个路线类
                        while(reader.hasNext()){
                            tagName=reader.nextName();
                            if("startPlace".equals(tagName)){
                                thisTraceObject.startPlace=reader.nextString();
                            }
                            else if("endPlace".equals(tagName)){
                                thisTraceObject.endPlace=reader.nextString();
                            }
                            else if("id".equals(tagName)){
                                thisTraceObject.traceID=reader.nextInt();
                            }
                            else{
                                tagName=reader.nextString();
                            }
                        }
                        strs.add(thisTraceObject);
                        reader.endObject();
                    }
                    reader.endArray();

                }catch(IOException ex){
                    ex.printStackTrace();
                }
                   System.out.println("to add strs");
                   showList(strs);



            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                System.out.println("fail");
            }
        });
    }
    private void showList(final ArrayList<TraceObject> strs){
        ListView lv = (ListView) findViewById(R.id.listView);//得到ListView对象的引用 /*为ListView设置Adapter来绑定数据*/
        ArrayList<String> endStrs=new ArrayList<String>();
        for(int i=0;i<strs.size();i++){
            endStrs.add(strs.get(i).startPlace+"-"+strs.get(i).endPlace);
        }
        lv.setAdapter(new ArrayAdapter<String>(searchTrace.this,
                android.R.layout.simple_list_item_1, endStrs));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                //点击后在标题上显示点击了第几行                    setTitle("你点击了第"+arg2+"行");
                Intent showTrace = new Intent(searchTrace.this, ShowTrace_Activity.class);
                Bundle bundle = new Bundle();
                bundle.putString("ID", Integer.toString(strs.get(arg2).traceID));
                showTrace.putExtras(bundle);
                startActivity(showTrace);
            }
        });


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_trace, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
