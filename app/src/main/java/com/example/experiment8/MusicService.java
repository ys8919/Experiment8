package com.example.experiment8;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
import java.util.Timer;
import java.util.TimerTask;

public class MusicService extends Service {

    private static final String TAG = "MusicService";
    MyReceiver serviceReceiver;
    AssetManager mAssetManager;
    int status = 0x11;  //定义音乐播放状态，0x11代表没有播放，0x12代表正在播放，0x13代表暂停
    int current = 0; // 记录当前正在播放的音乐
    int control=0;
    int progressList=0;
    private MediaPlayer mediaPlayer;
    private Boolean progressStatus = false;
    //public final IBinder binder = (IBinder) new MusicController();
    //int[] musics=new int[]{R.raw.d3,R.raw.k1,R.raw.l4,R.raw.s2};
    String[] musics = new String[]{"打击乐器 .mp3", "康加舞.mp3", "蓝调小号.mp3","手拍鼓.mp3"};
   /* public class MusicController extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
        public void play() throws IOException {
            mediaPlayer.prepare();
            mediaPlayer.start();//开启音乐
            //定时发送播放信息
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    //实例化一个Message对象

                }
            }, 0, 50);
        }
        public void pause() {
            mediaPlayer.pause();//暂停音乐
        }
        public long getMusicDuration() {
            return mediaPlayer.getDuration();//获取文件的总长度
        }
        public long getPosition() {
            return mediaPlayer.getCurrentPosition();//获取当前播放进度
        }
        public void setPosition (int position) {
            mediaPlayer.seekTo(position);//重新设定播放进度
        }

    }

    */

    @Override
    public IBinder onBind(Intent intent) {
        //return binder;
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mAssetManager = getAssets();
        serviceReceiver = new MyReceiver();
        //创建IntentFilter
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.CTL_ACTION);
        registerReceiver(serviceReceiver, filter);
        Intent sendIntent = new Intent(MainActivity.UPDATE_ACTION);
        //sendIntent.putExtra("ceshi", "测试");
        //发送广播，将被Activity中的BroadcastReceiver接收到
        sendBroadcast(sendIntent);
        //创建MediaPlayer
        mediaPlayer = new MediaPlayer();
        //为MediaPlayer播放完成事件绑定监听器

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //实例化一个Message对象
                if(progressStatus) {//进度条状态，拖动进度条时暂停发送
                    return;
                }
               // if(status == 0x12||status == 0x13){
                if(mediaPlayer.isPlaying()){
                    Intent sendIntent = new Intent(MainActivity.UPDATE_ACTION);
                    int CurrentPosition=mediaPlayer.getCurrentPosition();
                    progressList=CurrentPosition;
                    int MusicDirector=mediaPlayer.getDuration();
                    sendIntent.putExtra("current", current);
                    sendIntent.putExtra("update", status);
                    sendIntent.putExtra("CurrentPosition", CurrentPosition);
                    sendIntent.putExtra("MusicDirector", MusicDirector);
                    //发送广播，将被Activity中的BroadcastReceiver接收到
                    sendBroadcast(sendIntent);

                }
            }
        }, 0, 50);

    }
    /**
     * 任意一次unbindService()方法，都会触发这个方法
     * 用于释放一些绑定时使用的资源

     */
    /*@Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

     */
    @Override
    public void onDestroy() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.release();
        mediaPlayer = null;
        super.onDestroy();
    }


    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            control = intent.getIntExtra("control", -1);
            int ctlCurrent = intent.getIntExtra("ctlCurrent", -1);
            int progress = intent.getIntExtra("progress", -1);
            progressStatus=intent.getBooleanExtra("progressStatus",false);

            //Log.d(TAG, "onReceive: status"+status);
            if(ctlCurrent>=0){
                current=ctlCurrent;
            }
            //Log.d(TAG, "onReceive: control:"+control);
            switch (control){
                case 1: // 播放或暂停
                    //原来处于没有播放状态
                    if (status ==0x11){
                        //准备播放音乐
                        prepareAndPlay(musics[current]);
                        status = 0x12;
                    }
                    //原来处于播放状态
                    else if (status == 0x12){
                        //暂停
                        //progressList=mediaPlayer.getCurrentPosition();
                        Log.d(TAG, "onReceive: getCurrentPosition:"+mediaPlayer.getCurrentPosition());
                        Log.d(TAG, "onReceive: progress12:"+progressList);
                        mediaPlayer.pause();
                        status = 0x13; // 改变为暂停状态
                    }
                    //原来处于暂停状态
                    else if (status == 0x13){
                        //播放
                        Log.d(TAG, "onReceive: progress13:"+progressList);
                        if(progress==-1){
                            mediaPlayer.seekTo(progressList);
                        }
                        mediaPlayer.start();
                        status = 0x12; // 改变状态
                    }
                    break;
                //停止声音
                case 2:
                    //如果原来正在播放或暂停
                    if (status == 0x12 || status == 0x13){
                        //停止播放
                        mediaPlayer.stop();
                        status = 0x11;

                    }
                    break;
                case 3: // 点击音乐名播放
                    //原来处于没有播放状态
                    prepareAndPlay(musics[current]);
                    status = 0x12;
                    control=1;
                    break;
                case 4: // 上一曲
                    int CurrentPosition=mediaPlayer.getCurrentPosition();
                    //Log.d(TAG, "onReceive: CurrentPosition："+CurrentPosition);
                    if(CurrentPosition>2) {
                        prepareAndPlay(musics[current]);
                        status = 0x12;
                        control = 1;
                    }else{
                        if(current>0){
                            current--;
                        }else {
                            current = 0;
                        }
                        prepareAndPlay(musics[current]);
                        status = 0x12;
                        control = 1;
                    }
                    break;
                case 5: // 下一曲
                    current++;
                    if (current >= musics.length) {
                        current = 0;
                    }
                    prepareAndPlay(musics[current]);
                    status = 0x12;
                    control=1;
                    break;
                case 6: // 拖动进度条
                    if( mediaPlayer.isPlaying()){
                        mediaPlayer.seekTo(progress);
                        Log.d(TAG, "onReceive: 播放拖动："+progress);
                    }else if(status>0x11){

                        mediaPlayer.seekTo(progress);
                        Log.d(TAG, "onReceive: 暂停拖动："+progress);
                        mediaPlayer.start();
                        status = 0x12; // 改变状态
                    }
                    control = 1;
                    break;
            }
            //广播通知Activity更改图标、文本框
            int MusicDirector=mediaPlayer.getDuration();
            Intent sendIntent = new Intent(MainActivity.UPDATE_ACTION);
            sendIntent.putExtra("update", status);
            sendIntent.putExtra("current", current);
            sendIntent.putExtra("MusicDirector", MusicDirector);
            //发送广播，将被Activity中的BroadcastReceiver接收到
            sendBroadcast(sendIntent);
        }
    }

    private void prepareAndPlay(String music) {
        //打开指定的音乐文件

        try{
            final AssetFileDescriptor assetFileDescriptor = mAssetManager.openFd(music);
            if (mediaPlayer==null){
                mediaPlayer = new MediaPlayer();
                mediaPlayer.reset();
                mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
                mediaPlayer.prepare();
                mediaPlayer.start();
            }else if (mediaPlayer.isPlaying()){
                mediaPlayer.stop();
               // mediaPlayer.release();
              //  mediaPlayer = new MediaPlayer();
                mediaPlayer.reset();
                mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
                mediaPlayer.prepare();
                mediaPlayer.start();

                 /*new Handler().postDelayed(new Runnable(){
                    @Override
                   public void run() {
                        try{
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                        }catch (Exception e){

                            e.printStackTrace();
                        }
                    }
                }, 1000);
                   */
            }else{
                mediaPlayer = new MediaPlayer();
                mediaPlayer.reset();
                mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
                mediaPlayer.prepare();
                mediaPlayer.start();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        if(mediaPlayer!=null) {
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (control == 1) {
                        current++;
                    }
                    if (current >= musics.length) {
                        current = 0;
                    }
                    //准备并播放音乐
                    prepareAndPlay(musics[current]);
                    status = 0x12;

                    //发送广播通知Activity更改文本框
                    int MusicDirector = mediaPlayer.getDuration();
                    Intent sendIntent = new Intent(MainActivity.UPDATE_ACTION);
                    sendIntent.putExtra("current", current);
                    sendIntent.putExtra("MusicDirector", MusicDirector);
                    //发送广播，将被Activity中的BroadcastReceiver接收到
                    sendBroadcast(sendIntent);

                }
            });
        }
    }

}