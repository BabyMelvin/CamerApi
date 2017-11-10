package com.study.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class MyCamera extends AppCompatActivity implements SurfaceHolder.Callback {

    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private boolean mIsPermission;
    private final int LARGE_HEIGHT=500;
    private final int LARGE_WIDTH=500;
    private static final String TAG = "MyCamera";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_my_camera);
        intView();
       // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        checkCameraPermission();
        Log.i(TAG, "onCreate: ");
    }

    private void intView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated: ");
      //  checkCameraPermission();
        int bestWidht=0;
        int bestHeight=0;
        if(mIsPermission){
           // mCamera=Camera.open();
            mCamera= Camera.open();
            Camera.Parameters parameters=mCamera.getParameters();
            Log.i(TAG, "surfaceCreated: flushMode="+parameters.getFlashMode());
            try {
                if(this.getResources().getConfiguration().orientation!= Configuration.ORIENTATION_LANDSCAPE){
                    parameters.set("orientation","portrait");
                    //Android 2.2及以上版本
                    mCamera.setDisplayOrientation(90);
                    parameters.setRotation(90);
                }else {
                    parameters.set("orientation","landscape");
                    mCamera.setDisplayOrientation(180);
                    parameters.setRotation(180);
                }
                //获取支持的颜色相关参数
                List<String> supportedColorEffects = parameters.getSupportedColorEffects();
                Iterator<String> iterator = supportedColorEffects.iterator();
                while (iterator.hasNext()){
                    String next = iterator.next();
                    Log.i(TAG, "surfaceCreated: next="+next);
                    if(next.equals(Camera.Parameters.EFFECT_SOLARIZE)){//过度曝光效果
                        parameters.setColorEffect(Camera.Parameters.EFFECT_SOLARIZE);
                        Log.i(TAG, "surfaceCreated: in the while");
                        break;
                    }
                }
                //获取预览尺寸先关参数
                List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
                if(supportedPreviewSizes.size()>1) {
                    Iterator<Camera.Size> iteratorSize = supportedPreviewSizes.iterator();
                    while (iteratorSize.hasNext()) {
                        Camera.Size size = iteratorSize.next();
                        Log.i(TAG, "surfaceCreated: sizeW=" + size.width + " sizeH" + size.height);
                        if(size.width>0&&size.width<=LARGE_WIDTH&&size.height>0&&size.height<LARGE_HEIGHT){
                            bestHeight=size.height;
                            bestWidht=size.width;
                           // break;
                        }
                    }
                }
                Log.i(TAG, "surfaceCreated: bestW"+bestWidht+"bestH"+bestHeight);
                if(bestHeight!=0&&bestWidht!=0){
                    parameters.setPreviewSize(bestWidht,bestHeight);
                    //需要设置SurfaceView的大小，否则会显示扭曲或者模糊
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mSurfaceView.getLayoutParams();
                    layoutParams.width=bestWidht;
                    layoutParams.height=bestHeight;
                    mSurfaceView.setLayoutParams(layoutParams);
                }
                mCamera.setPreviewDisplay(mHolder);
            } catch (IOException e) {
                mCamera.release();
                e.printStackTrace();
            }
            mCamera.startPreview();
        }

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
        switch (requestCode){
            case 0:
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    mIsPermission = true;
                }else {
                    Toast.makeText(this, "相机未授权", Toast.LENGTH_SHORT).show();
                }
                break;
            case 1:
                break;
            default:
                break;
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged: ");

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed: ");
        if(mCamera!=null) {
            mCamera.stopPreview();
            mCamera.release();
        }
    }
}
