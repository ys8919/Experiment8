package com.example.experiment8;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;

import java.util.List;

public class MusicAdapter extends ArrayAdapter {
    public MusicAdapter(@NonNull Context context, int resource ,List<String> musicList) {
        super(context, resource,musicList);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        String  musicName = (String)getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.music_list, null);
        LinearLayout linearLayout=view.findViewById(R.id.linearLayout);
        linearLayout.setTag(position);
        View view1=view.findViewById(R.id.view);
        view1.setTag(position);
        TextView name = (TextView)view.findViewById(R.id.textView2);
        name.setTag(position);
        name.setText(musicName);

        return view;
    }

}
