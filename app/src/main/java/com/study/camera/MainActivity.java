package com.study.camera;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
    private ImageView mImageView;
    private final static int CAMERA_RESULT=0;
    private static final String TAG = "MainActivity";
    private Uri mImageFileUri;
    private String mImageFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView= (ImageView) findViewById(R.id.from_camera);
        Log.i(TAG, "onCreate: ");
        //1.设置绝对的路径
        mImageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Pictures/mypicture.jpg";
        File imageFile=new File(mImageFilePath);
        mImageFileUri = Uri.fromFile(imageFile);
        Log.i(TAG, "onCreate: imageFilePath="+ mImageFilePath);
        needCheckPermission();
        //测试需要打开该文件
     //   startCamera();
    }

    private void needCheckPermission() {
        if(ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{READ_EXTERNAL_STORAGE,WRITE_EXTERNAL_STORAGE},0);
        }
        if(ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{READ_EXTERNAL_STORAGE,WRITE_EXTERNAL_STORAGE},1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==0){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "可以读取", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "未授权", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void startCamera() {
        Log.i(TAG, "startCamera: 0");
        //启动camera
        //Intent intent=new Intent("android.media.action.IMAGE_CAPTURE");
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Log.i(TAG, "startCamera: 1");
       // startActivity(intent);
        //绕过大小限制，key-value形式，将URI指定要保存的路径挂钩
        intent.putExtra(MediaStore.EXTRA_OUTPUT,mImageFileUri);
        startActivityForResult(intent,CAMERA_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult: 0");
        switch (requestCode){
            case CAMERA_RESULT:
                if( resultCode==RESULT_OK){
                    Log.i(TAG, "onActivityResult: 1");
                    //1.获得121X162像素大小图片
//                    Bundle extras=data.getExtras();
//                    Bitmap bmp= (Bitmap) extras.get("data");
//                    mImageView.setImageBitmap(bmp);
                    //2.获得大图片
                    //定义如何形式读入到内存中
//                    BitmapFactory.Options bmpOptions=new BitmapFactory.Options();
//                    bmpOptions.inSampleSize=8;//为原图片大小的1/8比例
//                    Bitmap bmp=BitmapFactory.decodeFile(mImageFilePath,bmpOptions);
//                    mImageView.setImageBitmap(bmp);
                    //3.利用显示维度来确定inSampleSize
                    Display currentDisplay=getWindowManager().getDefaultDisplay();
                    int dw=currentDisplay.getWidth();
                    int dh= currentDisplay.getHeight();
                    Log.i(TAG, "onActivityResult: dw="+dw);
                    Log.i(TAG, "onActivityResult: dh="+dh);
                    //加载图片的尺寸而不是图片的本身
                    BitmapFactory.Options bmpFactoryOptions=new BitmapFactory.Options();
                    bmpFactoryOptions.inJustDecodeBounds=true;//这会导致outHeight和outWidth被设置
                    Bitmap bitmap=BitmapFactory.decodeFile(mImageFilePath,bmpFactoryOptions);
                    Log.i(TAG, "onActivityResult: bmpFactoryOptions.outHeight="+bmpFactoryOptions.outHeight);
                    Log.i(TAG, "onActivityResult: bmpFactoryOptions.outWidth="+bmpFactoryOptions.outWidth);
                    int heightRatio= (int) Math.ceil(bmpFactoryOptions.outHeight/(float)dh);
                    int widthRatio= (int) Math.ceil(bmpFactoryOptions.outWidth/(float)dw);
                    Log.i(TAG, "onActivityResult: heightRatio="+heightRatio);
                    Log.i(TAG, "onActivityResult: widthRatio="+widthRatio);
                    if(heightRatio>1&&widthRatio>1) {
                            bmpFactoryOptions.inSampleSize=Math.max(heightRatio,widthRatio);
                    }
                    bmpFactoryOptions.inJustDecodeBounds=false;//真正的解码
                    bitmap=BitmapFactory.decodeFile(mImageFilePath,bmpFactoryOptions);
                    mImageView.setImageBitmap(bitmap);
                }
                break;
            default:
                break;
        }

    }

    public void startContent(View view) {
        startActivity(new Intent(this,MediaStoreCameraIntent.class));
    }
}
