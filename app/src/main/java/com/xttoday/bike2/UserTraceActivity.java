package com.xttoday.bike2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.JsonReader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.xttoday.bike2.app.AppConfig;
import com.xttoday.bike2.helper.SQLiteHandler;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

/**
 * @function 用于显示用户自己分享的的路线
 * @via 传入的参数为用户的ID
 */

public class UserTraceActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_trace);
        SQLiteHandler db;
        db = new SQLiteHandler(getApplicationContext());
        HashMap<String, String> user = db.getUserDetails();
        searchUserTrace(user.get("uid"));
        System.out.println(user.get("uid"));
    }

    public void searchUserTrace(String uid){//根据用户ID查找用户自己的路线
        String url= AppConfig.URL_SEARCH_USER_TRACE+"?userID="+uid;
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
        lv.setAdapter(new ArrayAdapter<String>(UserTraceActivity.this,
                android.R.layout.simple_list_item_1, endStrs));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                //点击后在标题上显示点击了第几行                    setTitle("你点击了第"+arg2+"行");
                Intent showTrace = new Intent(UserTraceActivity.this, ShowTrace_Activity.class);
                Bundle bundle = new Bundle();
                bundle.putString("ID", Integer.toString(strs.get(arg2).traceID));
                showTrace.putExtras(bundle);
                startActivity(showTrace);
            }
        });
    }

}
