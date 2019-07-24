package com.thunder.ktv.record2view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.view.SurfaceHolder;

import com.thunder.ktv.record2view.databinding.ActivityMainBinding;
import com.thunder.ktv.record2view.record.AudioRecorder;
import com.thunder.ktv.record2view.record.RecordOutputStream;

public class MainActivity extends Activity {
    private ActivityMainBinding binding;
    private static Context context;
    MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setViewModel(MainViewModel.getInstance());
        binding.setHandler(new OnClickListenerHandler(context));
        initPermission();
        AudioRecorder.getIstance().setOutputStream(new RecordOutputStream(binding.recordView.sineWavesView));

        mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.sinewave);
        mediaPlayer.setLooping(true);
        binding.videoView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mediaPlayer.setSurface(binding.videoView.getHolder().getSurface());
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        mediaPlayer = null;
    }
    private void initPermission() {
        int permission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            //需不需要解释的dialog
            //if (shouldRequest()) return;
            //请求权限
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
    }
    private boolean granted;

    /**
     * 请求权限的回调
     * <p>
     * 参数1：requestCode-->是requestPermissions()方法传递过来的请求码。
     * 参数2：permissions-->是requestPermissions()方法传递过来的需要申请权限
     * 参数3：grantResults-->是申请权限后，系统返回的结果，PackageManager.PERMISSION_GRANTED表示授权成功，PackageManager.PERMISSION_DENIED表示授权失败。
     * grantResults和permissions是一一对应的
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0) {
            granted = grantResults[0] == PackageManager.PERMISSION_GRANTED;//是否授权，可以根据permission作为标记
        }
    }
}
