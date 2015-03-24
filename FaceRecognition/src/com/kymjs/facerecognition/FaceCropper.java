/*
 * Copyright (C) 2014, kymjs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kymjs.facerecognition;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.util.Log;

/**
 * 从Bitmap获取脸部位置的工具类.<br>
 * 它支持多个人脸的识别并剪裁全部识别出来的脸（默认最多8个，效率问题）支持任何格式的图片
 * 
 * @author kymjs (http://www.kymjs.com)
 */
public final class FaceCropper {

    public enum SizeMode {
        FACE_MARGIN, EYE_MARGIN
    };

    // default constant
    private static final int MAX_FACES = 8; // 最多识别几张脸
    private static final int FACE_MARGIN_PX = 100; // 脸与脸间隔
    private static final float EYE_MARGIN_PX = 2F; // 双眼间距
    private static final int MIN_FACE_SIZE = 200; // 人脸最小直径

    // default value
    private int mFaceMinSize = MIN_FACE_SIZE;
    private int mMaxFaces = MAX_FACES;
    private int mFaceMarginPx = FACE_MARGIN_PX;
    private float mEyeMarginPx = EYE_MARGIN_PX;
    private SizeMode mSizeMode = SizeMode.EYE_MARGIN;

    private boolean mDebug = true;
    private Paint mDebugPainter; // 用作调试阶段显示人脸识别的圈
    private Paint mDebugAreaPainter; // 用作调试阶段显示人脸识别的圈

    public FaceCropper() {
        initPaints();
    }

    public FaceCropper(int faceMarginPx) {
        setFaceMarginPx(faceMarginPx);
        initPaints();
    }

    public FaceCropper(float eyesDistanceFactorMargin) {
        setEyeDistanceFactorMargin(eyesDistanceFactorMargin);
        initPaints();
    }

    private void initPaints() {
        mDebugPainter = new Paint();
        mDebugPainter.setColor(Color.RED);
        mDebugPainter.setAlpha(80);

        mDebugAreaPainter = new Paint();
        mDebugAreaPainter.setColor(Color.GREEN);
        mDebugAreaPainter.setAlpha(80);
    }

    /********************* core method **********************************/

    /**
     * 获取debug模式下的图片
     * 
     * @param ctx
     * @param res
     * @return
     */
    public Bitmap getDebugImage(Context ctx, int res) {
        // 以565格式通过resId创建bitmap
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        return getDebugImage(BitmapFactory.decodeResource(ctx.getResources(),
                res, bitmapOptions));
    }

    /**
     * 获取debug模式下的图片
     * 
     * @param bitmap
     * @return
     */
    public Bitmap getDebugImage(Bitmap bitmap) {
        FaceResult result = cropFace(bitmap, true);
        Canvas canvas = new Canvas(result.getBitmap());

        canvas.drawBitmap(result.getBitmap(), new Matrix(), null);
        canvas.drawRect(result.getInit().x, result.getInit().y,
                result.getEnd().x, result.getEnd().y, mDebugAreaPainter);

        return result.getBitmap();
    }

    /**
     * 剪裁指定图片中的脸
     * 
     * @param ctx
     * @param res
     * @return
     */
    public Bitmap cropFace(Context ctx, int res) {
        return getCroppedImage(ctx, res);
    }

    /**
     * 剪裁指定图片中的脸
     * 
     * @param bitmap
     * @return
     */
    public Bitmap cropFace(Bitmap bitmap) {
        return getCroppedImage(bitmap);
    }

    /**
     * 获取裁剪以后的人脸
     * 
     * @param ctx
     * @param res
     * @return
     */
    private Bitmap getCroppedImage(Context ctx, int res) {
        // 以565格式通过resId创建bitmap
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        return getCroppedImage(BitmapFactory.decodeResource(ctx.getResources(),
                res, bitmapOptions));
    }

    /**
     * 获取裁剪以后的人脸
     * 
     * @param bitmap
     * @return
     */
    private Bitmap getCroppedImage(Bitmap bitmap) {
        FaceResult result = cropFace(bitmap, mDebug);
        Bitmap croppedBitmap = Bitmap.createBitmap(result.getBitmap(),
                result.getInit().x, result.getInit().y, result.getEnd().x
                        - result.getInit().x,
                result.getEnd().y - result.getInit().y);
        if (result.getBitmap() != croppedBitmap) {
            result.getBitmap().recycle();
        }
        return croppedBitmap;
    }

    /**
     * 剪裁脸部的核心方法
     * 
     * @param bitmap
     *            待识别的图片
     * @param debug
     *            是否开启调试模式
     * @return
     */
    private FaceResult cropFace(Bitmap bitmap, boolean debug) {
        Bitmap formatBitmap = BitmapOperate.formatBitmap(bitmap);
        formatBitmap = BitmapOperate.formatBitmapTo565(formatBitmap);
        Bitmap aimBitmap = formatBitmap.copy(Bitmap.Config.RGB_565, true);
        if (formatBitmap != aimBitmap) {
            formatBitmap.recycle();
        }
        // 创建一个人脸识别器
        FaceDetector faceDetector = new FaceDetector(aimBitmap.getWidth(),
                aimBitmap.getHeight(), mMaxFaces);
        // 人脸数组
        FaceDetector.Face[] faces = new FaceDetector.Face[mMaxFaces];
        // Bitmap必须是565格式
        int faceCount = faceDetector.findFaces(aimBitmap, faces);

        if (debug) {
            Log.d("kymjs", faceCount + "张脸被找到");
        }

        if (faceCount == 0) {
            return new FaceResult(aimBitmap);
        }

        int initX = aimBitmap.getWidth();
        int initY = aimBitmap.getHeight();
        int endX = 0;
        int endY = 0;

        PointF centerFace = new PointF();

        Canvas canvas = new Canvas(aimBitmap);
        canvas.drawBitmap(aimBitmap, new Matrix(), null);

        // 计算每张脸的最小外接圆
        for (int i = 0; i < faceCount; i++) {
            FaceDetector.Face face = faces[i];
            // 通常采用眼睛间距乘以三作为脸的外接圆直径
            int faceSize = (int) (face.eyesDistance() * 3);
            if (SizeMode.FACE_MARGIN.equals(mSizeMode)) {
                faceSize += mFaceMarginPx * 2;
            } else if (SizeMode.EYE_MARGIN.equals(mSizeMode)) {
                faceSize += face.eyesDistance() * mEyeMarginPx;
            }
            faceSize = Math.max(faceSize, mFaceMinSize);
            face.getMidPoint(centerFace);

            if (debug) { // 显示调试人脸识别圈
                canvas.drawPoint(centerFace.x, centerFace.y, mDebugPainter);
                canvas.drawCircle(centerFace.x, centerFace.y,
                        face.eyesDistance() * 1.5f, mDebugPainter);
            }

            int tInitX = (int) (centerFace.x - faceSize / 2);
            int tInitY = (int) (centerFace.y - faceSize / 2);
            tInitX = Math.max(0, tInitX);
            tInitY = Math.max(0, tInitY);

            int tEndX = tInitX + faceSize;
            int tEndY = tInitY + faceSize;
            tEndX = Math.min(tEndX, aimBitmap.getWidth());
            tEndY = Math.min(tEndY, aimBitmap.getHeight());

            initX = Math.min(initX, tInitX);
            initY = Math.min(initY, tInitY);
            endX = Math.max(endX, tEndX);
            endY = Math.max(endY, tEndY);
        }

        int sizeX = endX - initX;
        int sizeY = endY - initY;

        if (sizeX + initX > aimBitmap.getWidth()) {
            sizeX = aimBitmap.getWidth() - initX;
        }
        if (sizeY + initY > aimBitmap.getHeight()) {
            sizeY = aimBitmap.getHeight() - initY;
        }
        Point init = new Point(initX, initY);
        Point end = new Point(initX + sizeX, initY + sizeY);
        return new FaceResult(aimBitmap, init, end);
    }

    /********************** config method *******************************/

    public int getMaxFaces() {
        return mMaxFaces;
    }

    public void setMaxFaces(int maxFaces) {
        this.mMaxFaces = maxFaces;
    }

    public int getFaceMinSize() {
        return mFaceMinSize;
    }

    public void setFaceMinSize(int faceMinSize) {
        mFaceMinSize = faceMinSize;
    }

    public int getFaceMarginPx() {
        return mFaceMarginPx;
    }

    public void setFaceMarginPx(int faceMarginPx) {
        mFaceMarginPx = faceMarginPx;
        mSizeMode = SizeMode.FACE_MARGIN;
    }

    public SizeMode getSizeMode() {
        return mSizeMode;
    }

    public float getEyeDistanceFactorMargin() {
        return mEyeMarginPx;
    }

    public void setEyeDistanceFactorMargin(float eyeDistanceFactorMargin) {
        mEyeMarginPx = eyeDistanceFactorMargin;
        mSizeMode = SizeMode.EYE_MARGIN;
    }

    public boolean isDebug() {
        return mDebug;
    }

    public void setDebug(boolean debug) {
        mDebug = debug;
    }

    private class FaceResult {
        Bitmap mBitmap;
        Point mInit;
        Point mEnd;

        public FaceResult(Bitmap bitmap, Point init, Point end) {
            mBitmap = bitmap;
            mInit = init;
            mEnd = end;
        }

        public FaceResult(Bitmap bitmap) {
            mBitmap = bitmap;
            mInit = new Point(0, 0);
            mEnd = new Point(bitmap.getWidth(), bitmap.getHeight());
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }

        public Point getInit() {
            return mInit;
        }

        public Point getEnd() {
            return mEnd;
        }
    }

}
