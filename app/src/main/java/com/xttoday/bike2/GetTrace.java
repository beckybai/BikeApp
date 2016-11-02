package com.xttoday.bike2;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class GetTrace extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_trace);
        Button showTraceByID=(Button)findViewById(R.id.buttonShowTrace);
        final EditText start=(EditText)findViewById(R.id.startPlace);
        final EditText end=(EditText)findViewById(R.id.endPlace);

        //final String traceIDText=traceID.getText().toString();
        showTraceByID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchTrace =new Intent(GetTrace.this,searchTrace.class);
                Bundle bundle=new Bundle();
                bundle.putString("startPlace",start.getText().toString());
                bundle.putString("endPlace",end.getText().toString());
                searchTrace.putExtras(bundle);
                startActivity(searchTrace);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_get_trace, menu);
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
