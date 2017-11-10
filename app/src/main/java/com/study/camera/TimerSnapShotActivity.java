package com.study.camera;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

public class TimerSnapShotActivity extends AppCompatActivity implements SurfaceHolder.Callback,Camera.PictureCallback, View.OnClickListener {
    private static final String TAG = "TimerSnapShotActivity";
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private boolean isPermission;
    private Camera mCamera;
    private Camera.Parameters mParameters;
    private Handler mHandlerTimer;
    private boolean isRunningTimer=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_snap_shot);
        Log.i(TAG, "onCreate: ");
        initView();
        needPermission();
    }

    private void needPermission() {
        Log.i(TAG, "needPermission: 0");
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},0);
        }else {
            Log.i(TAG, "needPermission: 1");
            isPermission=true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 0:
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "授权了", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "onRequestPermissionsResult: 0");
                    isPermission=true;
                }else {
                    Toast.makeText(this, "未授权", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "onRequestPermissionsResult: 1");
                    isPermission=false;
                }
                break;
            default:
                break;
        }
    }

    private void initView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_timer_snap_shot);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mSurfaceView.setClickable(true);
        mSurfaceView.setFocusable(true);
        mSurfaceView.setFocusableInTouchMode(true);

        mSurfaceView.setOnClickListener(this);
        mHandlerTimer = new Handler(){
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case 1:
                        Log.i(TAG, "handleMessage: ");
                        mCamera.takePicture(null,null,TimerSnapShotActivity.this);
                        break;
                    default:
                        break;
                }
            }
        };
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(isPermission){
            mCamera = Camera.open();
            try {
                mCamera.setPreviewDisplay(mHolder);
                mParameters = mCamera.getParameters();
                if(this.getResources().getConfiguration().orientation!= Configuration.ORIENTATION_LANDSCAPE){
                    mParameters.set("orientation","portrait");
                    //Android 2.2及以上版本
                    mCamera.setDisplayOrientation(90);
                    mParameters.setRotation(90);
                }else {
                    mParameters.set("orientation","landscape");
                    mCamera.setDisplayOrientation(180);
                    mParameters.setRotation(180);
                }
                //获取支持的颜色相关参数
                List<String> supportedColorEffects = mParameters.getSupportedColorEffects();
                Iterator<String> iterator = supportedColorEffects.iterator();
                while (iterator.hasNext()){
                    String next = iterator.next();
                    Log.i(TAG, "surfaceCreated: next="+next);
                    if(next.equals(Camera.Parameters.EFFECT_SOLARIZE)){//过度曝光效果
                        mParameters.setColorEffect(Camera.Parameters.EFFECT_SOLARIZE);
                        Log.i(TAG, "surfaceCreated: in the while");
                        break;
                    }
                }
                mCamera.setParameters(mParameters);
            } catch (IOException e) {
                mCamera.release();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged: ");
        if(mCamera!=null){
            mCamera.startPreview();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed: ");
        if(mCamera!=null){
            mCamera.stopPreview();
            mCamera.release();
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.i(TAG, "onPictureTaken: ");
        Uri imageUri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,new ContentValues());
        try {
            OutputStream outputStream=getContentResolver().openOutputStream(imageUri);
            outputStream.write(data);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }

    @Override
    public void onClick(View v) {
        Log.i(TAG, "onClick: ");
        switch (v.getId()){
            case R.id.surface_timer_snap_shot:
                if(!isRunningTimer){
                    Message message=new Message();
                    message.what=1;
                    mHandlerTimer.sendMessageDelayed(message,10000);
                    isRunningTimer=true;
                }
                break;
            default:
                break;
        }
    }
}
