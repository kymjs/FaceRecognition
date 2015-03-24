package com.kymjs.recognition.demo;

import org.kymjs.kjframe.KJActivity;
import org.kymjs.kjframe.bitmap.helper.BitmapCreate;
import org.kymjs.kjframe.ui.BindView;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.kymjs.facerecognition.FaceCropper;

/**
 * 最简单的用法
 * 
 * @author kymjs (http://www.kymjs.com)
 * 
 */
public class MainActivity extends KJActivity {
    @BindView(id = R.id.imageView1)
    private ImageView imgOriginal;
    @BindView(id = R.id.imageView2)
    private ImageView imgNew;

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_main);
    }

    @Override
    public void initWidget() {
        super.initWidget();
        Bitmap bitmap = BitmapCreate.bitmapFromResource(getResources(),
                R.drawable.image, 0, 0);
        imgOriginal.setImageBitmap(bitmap);

        FaceCropper faceCropper = new FaceCropper();
        faceCropper.setDebug(false);
        Bitmap cropBitmap = faceCropper.cropFace(bitmap);

        imgNew.setImageBitmap(cropBitmap);
    }
}
