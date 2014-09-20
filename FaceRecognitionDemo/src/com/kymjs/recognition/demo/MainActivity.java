package com.kymjs.recognition.demo;

import org.kymjs.aframe.bitmap.utils.BitmapCreate;
import org.kymjs.aframe.ui.BindView;
import org.kymjs.aframe.ui.activity.BaseActivity;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.kymjs.facerecognition.FaceCropper;

public class MainActivity extends BaseActivity {
    @BindView(id = R.id.imageView1)
    private ImageView imgOriginal;
    @BindView(id = R.id.imageView2)
    private ImageView imgNew;

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        Bitmap jobsImg = BitmapCreate.bitmapFromResource(
                getResources(), R.drawable.jobs, 0, 0);
        imgOriginal.setImageBitmap(jobsImg);
        FaceCropper faceCropper = new FaceCropper();
        faceCropper.setDebug(false);
        Bitmap bitmap = faceCropper.cropFace(jobsImg);
        imgNew.setImageBitmap(bitmap);
    }
}
