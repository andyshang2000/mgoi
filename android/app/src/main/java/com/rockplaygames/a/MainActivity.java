package com.rockplaygames.a;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.adobe.air.AndroidActivityWrapper;
import com.adobe.air.ApplicationFileManager;
import com.applovin.sdk.AppLovinSdk;
import com.rockplaygames.PrincessWeddingSalon.R;

import java.util.HashMap;
import java.util.List;

import pub.ane.PublisherExtContext;

public class MainActivity extends BaseActivity {

    AndroidActivityWrapper wrapper;
    private ImageView view;
    private PublisherExtContext ane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addSplashView();
        requestPermission();
        wrapper = AndroidActivityWrapper.CreateAndroidActivityWrapper(this, true);
        wrapper.onCreate(this, getOnCreateParam());
    }

    private void addSplashView() {
        view = new ImageView(this);
        view.setImageResource(R.drawable.splash);
        view.setAdjustViewBounds(true);
        view.setScaleType(ImageView.ScaleType.FIT_XY);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;  // 屏幕宽度（像素）
        int height = metric.heightPixels;
        float aspect = (float) width / (float) height;
        if (aspect < 480.0 / 800.0) {
            layoutParams.height = height;
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        } else {
            layoutParams.width = width;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        view.setLayoutParams(layoutParams);
        ((ViewGroup) findViewById(android.R.id.content)).addView(view);
        //on flash boot, remove the splash view
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("cn.abel.action.broadcast");
        this.registerReceiver(new MyBroadcastReciver(), intentFilter);
    }

    private void requestPermission() {
        requestRuntimePermission(
                new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                new PermissionListener() {

                    @Override
                    public void onGranted() {
                    }

                    @Override
                    public void onDenied(List<String> deniedPermission) {
                    }
                });
    }

    public void setExtension(PublisherExtContext publisherExtContext) {
        this.ane = publisherExtContext;
    }

    private class MyBroadcastReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("cn.abel.action.broadcast")) {
                view.setVisibility(View.GONE);
            }
        }
    }

    private String[] getOnCreateParam() {
        String xmlPath = "";
        String rootDirectory = "";
        String extraArgs = "-nodebug";
        Boolean isADL = Boolean.valueOf(false);
        Boolean isDebuggerMode = Boolean.valueOf(false);
        String[] args = {xmlPath, rootDirectory, extraArgs,
                isADL.toString(), isDebuggerMode.toString()};
        return args;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            showAlert();
            return true;
        }
        return wrapper.onKeyDown(keyCode, event, super.onKeyDown(keyCode, event));
    }

    private void showAlert() {
        new AlertDialog.Builder(this).setTitle(getString(R.string.back_title)) // ""
                .setMessage(getString(R.string.back_msg)) // 确认要退出么？
                .setPositiveButton(getString(R.string.back_yes) /* 确定 */, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        finish();
                        System.exit(0);
                    }
                }).setNegativeButton(getString(R.string.back_no) /* "取消" */, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
            }
        }).show();
    }

    @Override
    public void onResume() {
        if (ane != null)
            ane.onResume();
        super.onResume();
        wrapper.onResume();
    }

    @Override
    protected void onPause() {
        if (ane != null)
            ane.onPause();
        super.onPause();
        wrapper.onPause();
    }

    @Override
    protected void onDestroy() {
        if (ane != null)
            ane.onDestroy();
        super.onDestroy();
        wrapper.onDestroy();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        wrapper.onRestart();
    }

    @Override
    protected void onStop() {
        if (ane != null)
            ane.onStop();
        super.onStop();
        wrapper.onStop();
    }

    @Override
    public void onStart() {
        if (ane != null)
            ane.onStart();
        super.onStart();
    }

    public void onBackPressed() {
        if (ane != null)
            ane.onBackPressed();
    }

    @Override
    public void onLowMemory() {
        wrapper.onLowMemory();
    }
}
