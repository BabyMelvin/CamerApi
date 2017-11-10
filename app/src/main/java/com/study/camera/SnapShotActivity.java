package com.study.camera;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

public class SnapShotActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        Camera.PictureCallback, View.OnClickListener {
    private static final String TAG = "SnapShotActivity";
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private boolean mIsPermission;
    private Camera mCamera;
    private Camera.Parameters mParameters;


    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snap_shot);
        initView();
        checkCameraPermission();
    }

    private void checkCameraPermission() {
        Log.i(TAG, "checkCameraPermission: ");
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},0);
        }else{
            mIsPermission=true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "checkCameraPermission: ");
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},0);
        }else{
            mIsPermission=true;
        }
    }

    private void initView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_snap_shot);
        mHolder = mSurfaceView.getHolder();
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHolder.addCallback(this);

        mSurfaceView.setFocusable(true);
        mSurfaceView.setClickable(true);
        //触摸模式也可以获得焦点
        mSurfaceView.setFocusableInTouchMode(true);
        mSurfaceView.setOnClickListener(this);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(mIsPermission){
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
        if(mCamera!=null){
            mCamera.startPreview();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(mCamera!=null){
            mCamera.stopPreview();
            mCamera.release();
        }
    }
    //再压缩图片的时候被回调,data:实际的JPEG字符数组，camera：捕获图像的对象
    @Override
    public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
        Uri imageUri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,new ContentValues());
        Log.i(TAG, "onPictureTaken: imageUri="+imageUri);
        try {
            OutputStream outputStream = getContentResolver().openOutputStream(imageUri);
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
    private class TimerRunnable implements Runnable{

        @Override
        public void run() {

        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.surface_snap_shot:
                                mCamera.takePicture(null,null,this);
                break;
            default:
                break;
        }
    }
}
