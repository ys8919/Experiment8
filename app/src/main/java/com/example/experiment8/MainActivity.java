package com.example.experiment8;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener ,SeekBar.OnSeekBarChangeListener{
    private static final String TAG = "MainActivity";
    private ListView listview;
    private int indexArea = -1;
    private List<String> MusicList=new ArrayList<String>();
    int status = 0x11;  //定义音乐播放状态，0x11代表没有播放，0x12代表正在播放，0x13代表暂停

   // private MusicService.MusicController mMusicController;
    private ActivityReceiver mActivityReceiver;
    public static final String CTL_ACTION = "com.Experiment.action.CTL_ACTION";
    public static final String UPDATE_ACTION = "com.Experiment.action.UPDATE_ACTION";
    String[] musicNames = new String[]{"打击乐器", "康加舞", "蓝调小号","手拍鼓"};

    private  TextView textView4;
    private  TextView textView3;
    private  TextView textView;
    private Button button;
    private Button button2;
    private Button button3;
    private android.widget.SeekBar seekBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listview=findViewById(R.id.ListView);
        textView=findViewById(R.id.textView);
        textView3=findViewById(R.id.textView3);
        textView4=findViewById(R.id.textView4);
        button=findViewById(R.id.button);
        button2=findViewById(R.id.button2);
        button3=findViewById(R.id.button3);
        seekBar=findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);

        button.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);

        for(String name:musicNames){
            MusicList.add(name);
        }

        MusicAdapter adapter = new MusicAdapter(MainActivity.this, R.layout.music_list,MusicList);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Toast.makeText(MailListActivity.this,phoneDtos.get(position).getName(), Toast.LENGTH_SHORT).show();
                if (indexArea != -1) {
                    View view1 = (View) parent.getChildAt(indexArea).findViewById(R.id.view);
                    view1.setBackgroundResource(R.color.colorMusic1);
                    TextView textView = (TextView) parent.getChildAt(indexArea).findViewById(R.id.textView2);
                    textView.setTextColor(getResources().getColor(R.color.colorText));
                }
                indexArea = position;
                View view1 = (View) parent.getChildAt(indexArea).findViewById(R.id.view);
                TextView textView = (TextView) parent.getChildAt(indexArea).findViewById(R.id.textView2);
                textView.setTextColor(getResources().getColor(R.color.colorMusic));
                view1.setBackgroundResource(R.color.colorMusic);
                Log.d(TAG, "onItemClick: ctlCurrent"+indexArea);
                Intent intent = new Intent(CTL_ACTION);
                intent.putExtra("control", 3);
                intent.putExtra("ctlCurrent", indexArea);
                Log.d(TAG, "onItemClick: ctlCurrent："+indexArea);
                sendBroadcast(intent);
            }

            MediaPlayer mp = new MediaPlayer();

        });
        mActivityReceiver = new ActivityReceiver();
        //创建IntentFilter
        IntentFilter filter = new IntentFilter();
        //指定BroadcastReceiver监听的Action
        filter.addAction(UPDATE_ACTION);
        //注册BroadcastReceiver
        registerReceiver(mActivityReceiver, filter);

        Intent intent = new Intent(this, MusicService.class);
        //增加StartService，来增加后台播放功能
        startService(intent);
        Log.d(TAG, "onCreate: ");
        /*List<String> list = new ArrayList<>();
        //获取指定路径下的所有文件
        list = Utils.getFilesAllName("R.raw");
        if(list != null){
            for(String listname : list ){
                Log.e("TAG","listname :"+listname );
                //判断文件是不是MP4后缀
                if(listname .endsWith(".mp3")){
                    //获取路径下最后一个‘/’后的坐标
                    int lastindex = listname .lastIndexOf("/");//获取具体文件名称
                    String name= listname .substring(lastindex+1,listname .length());
                    //获取到想要的名称后，去干你想干的事
                    String str = getExternalStorageDirectory() + "music/hetangyuese.mp3";
                    // dosomething
                }
            }
        }

         */

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(CTL_ACTION);
        switch (v.getId()){
            case R.id.button:
                intent.putExtra("control", 1);

                break;
            case R.id.button2:
                intent.putExtra("control", 4);
                break;
            case R.id.button3:
                intent.putExtra("control", 5);
                break;
          /*  case R.id.stop:
                intent.putExtra("control", 2);
                break;

           */
        }
        //发送广播，将被Service中的BroadcastReceiver接收到
        sendBroadcast(intent);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        textView3.setText(new SimpleDateFormat("mm:ss", Locale.getDefault()).format(new Date(progress)));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Intent intent = new Intent(CTL_ACTION);
        intent.putExtra("progressStatus", true);
        //intent.putExtra("control", 6);
        sendBroadcast(intent);

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Intent intent = new Intent(CTL_ACTION);
        intent.putExtra("progress", seekBar.getProgress());
        intent.putExtra("progressStatus", false);
        intent.putExtra("control", 6);
        sendBroadcast(intent);
    }

    public class ActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //获取Intent中的update消息，update代表播放状态
            int update = intent.getIntExtra("update", -1);
            //获取Intent中的current消息，current代表当前正在播放的歌曲
            int current = intent.getIntExtra("current", -1);
            int MusicDirector = intent.getIntExtra("MusicDirector", -1);
            int CurrentPosition = intent.getIntExtra("CurrentPosition", -1);
            //Log.d(TAG, "onReceive: update："+update);
            if (current >= 0){
                textView4.setText(musicNames[current]);
                textView.setText(new SimpleDateFormat("mm:ss", Locale.getDefault()).format(new Date(MusicDirector)));
                seekBar.setMax(MusicDirector);
                for(int i=0;i<listview.getChildCount();i++){
                    if(((LinearLayout)listview.getChildAt(i)).getTag().equals(current)){
                        LinearLayout linearLayout=(LinearLayout)listview.getChildAt(i);
                        View view2=( View)linearLayout.findViewById(R.id.view);
                        TextView textView1=(TextView)linearLayout.findViewById(R.id.textView2);
                        textView1.setTextColor(getResources().getColor(R.color.colorMusic));
                        view2.setBackgroundResource(R.color.colorMusic);
                    }else{
                        LinearLayout linearLayout=(LinearLayout)listview.getChildAt(i);
                        View view2=( View)linearLayout.findViewById(R.id.view);
                        TextView textView1=(TextView)linearLayout.findViewById(R.id.textView2);
                        textView1.setTextColor(getResources().getColor(R.color.colorText));
                        view2.setBackgroundResource(R.color.colorMusic1);
                    }
                }


            }
            if(CurrentPosition>=0){
                textView3.setText(new SimpleDateFormat("mm:ss", Locale.getDefault()).format(new Date(CurrentPosition)));
                seekBar.setProgress(CurrentPosition);
            }


            switch (update){
                case 0x11:
                    button.setBackgroundResource(R.drawable.button1);
                    status = 0x11;
                    break;
                case 0x12:
                    //在播放状态下设置使用暂停图标
                    button.setBackgroundResource(R.drawable.button2);
                    status = 0x12;
                    break;
                case 0x13:
                    //在暂停状态下设置使用播放图标
                    button.setBackgroundResource(R.drawable.button1);
                    status = 0x13;
                    break;
            }

        }
    }
}
