[![OSL](http://www.kymjs.com/image/logo_s.png)](http://www.kymjs.com/)
脸部剪裁
===============
#简介
在做社交类APP时，常常会遇到让用户自己上传图片的情况。只是用户上传的图片大小是未知的，而呈现在手机屏幕上时常常需要裁剪，这时候如果使用固定位置裁剪可能不能达到最佳效果。<br>
例如用户上传一张生活照，需要裁剪成一个正方形图片，这个时候如果单纯的取图片的中心区域的方形，可能会把脑袋剪裁成一半。<br>
本项目就是用于解决此种问题而创建的。

#使用方法
更多详细使用方法请查看[开源实验室](http://www.kymjs.com)中智能相片裁剪的讲解

```java
//最简单的使用，你只需要记住一个方法就够了
@Override
public void initWidget() {
    super.initWidget();

    Bitmap bitmap = BitmapCreate.bitmapFromResource(getResources(),
        R.drawable.image, 0, 0);
    imgOriginal.setImageBitmap(bitmap);
    
    //核心方法
    FaceCropper faceCropper = new FaceCropper();
    faceCropper.setDebug(false);
    Bitmap cropBitmap = faceCropper.cropFace(bitmap);

    imgNew.setImageBitmap(cropBitmap);
}
```

[!image](https://github.com/kymjs/FaceRecognition/blob/master/image.png)
[![image](https://github.com/kymjs/FaceRecognition/blob/master/image.png)](http://www.kymjs.com)
