package com.xttoday.bike2.forum;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.xttoday.bike2.R;
import com.xttoday.bike2.app.AppConfig;
import com.xttoday.bike2.helper.SQLiteHandler;
import com.xttoday.bike2.helper.SessionManager;

import java.util.HashMap;

public class ShowForum extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_forum);
        SessionManager session = new SessionManager(getApplicationContext());
        if (!session.isLoggedIn()) {
            Toast.makeText(getApplicationContext(),"请先登录",Toast.LENGTH_SHORT).show();
        }
        else {
            SQLiteHandler db = new SQLiteHandler(getApplicationContext());
            HashMap<String, String> user = db.getUserDetails();
            String uid = user.get("uid");
            WebView wv=(WebView)findViewById(R.id.webViewForum);
            wv.getSettings().setJavaScriptEnabled(true);;

            wv.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            wv.loadUrl(AppConfig.LOGIN_FORUM_URL+"?uid="+uid);
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);// 使用当前WebView处理跳转
                    return true;// true表示此事件在此处被处理，不需要再广播
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    // 有页面跳转时被回调
                    //view.loadUrl(url);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    // 页面跳转结束后被回调
                }

                @Override
                public void onReceivedError(WebView view, int errorCode,
                                            String description, String failingUrl) {
                    // 出错
                }
            });
        }


    }



}
