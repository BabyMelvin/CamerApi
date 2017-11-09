package com.study.camera;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MediaStoreCameraIntent extends AppCompatActivity {

    private ImageView mReturnedImageView;
    private Button mTakePictureButton;
    private Button mSaveButton;
    private TextView mTitleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_store_camera_intent);
        initView();
    }

    private void initView() {
        mReturnedImageView = (ImageView) findViewById(R.id.ReturnedImageView);
        mTakePictureButton = (Button) findViewById(R.id.take_picture);
        mSaveButton = (Button) findViewById(R.id.save_data_button);
        mTitleText = (TextView) findViewById(R.id.title_text);
    }
}
