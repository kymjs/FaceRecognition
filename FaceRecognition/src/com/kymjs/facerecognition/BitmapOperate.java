package com.kymjs.facerecognition;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

final class BitmapOperate {
    /**
     * 格式化图片宽高，使之可以被识别
     * 
     * @param bitmap
     *            待格式化的图片
     * @return
     */
    public static Bitmap formatBitmap(Bitmap bitmap) {
        Bitmap aimBitmap = null;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        boolean needChange = false;
        if (width % 2 == 1) {
            width++;
            needChange = true;
        }
        if (height % 2 == 1) {
            height++;
            needChange = true;
        }
        // 如果发生了改变，则做形变
        if (needChange) {
            aimBitmap = Bitmap.createScaledBitmap(bitmap, width,
                    height, false);
            bitmap = null;
        } else {
            aimBitmap = bitmap;
        }
        return aimBitmap;
    }

    /**
     * 格式化图片为565格式，使之可以裁剪
     * 
     * @param bitmap
     *            待处理的图片
     * @return
     */
    public static Bitmap formatBitmapTo565(Bitmap bitmap) {
        Bitmap aimBitmap = null;
        if (bitmap.getConfig() != Bitmap.Config.RGB_565) {
            aimBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                    bitmap.getHeight(), Bitmap.Config.RGB_565);
            // 将创建的565格式作为画布，把bitmap重新画到565画布上
            Paint paint = new Paint();
            Canvas canvas = new Canvas(aimBitmap);
            paint.setColor(Color.BLACK);
            canvas.drawBitmap(bitmap, 0, 0, paint);
            bitmap = null;
        }
        return aimBitmap;
    }
}
