package com.xttoday.bike2.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.xttoday.bike2.R;
import com.xttoday.bike2.ShowUserActivity;
import com.xttoday.bike2.forum.ShowForum;

public class BottomActivity extends Fragment
{


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.activity_bottom, container, false);
        Button bike=(Button)view.findViewById(R.id.button);
        Button forum=(Button)view.findViewById(R.id.button2);
        Button user=(Button)view.findViewById(R.id.button3);
        bike.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent beginBiking=new Intent(getActivity(),com.xttoday.bike2.trackshow.MainActivity.class);
                startActivity(beginBiking);
            }
        });
        forum.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toForum=new Intent(getActivity(), ShowForum.class);
                startActivity(toForum);
            }
        });
        user.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent showUser = new Intent(getActivity(),ShowUserActivity.class);

                startActivity(showUser);
            }
        });

        return view;
    }
}
