# CropBiggerView
Free cropping, custom view that can zoom in on details.


waiting for update...
效果图

![Screen_Recording_20200828-180948.gif](https://upload-images.jianshu.io/upload_images/4322600-dd08f9f1b6ed0b14.gif?imageMogr2/auto-orient/strip)


核心代码
▶Part  1 :裁剪部分

* 绘制裁剪边框线，效果看起来是一个长方形，为了实现随意裁剪， 用了四条线组成裁剪框，核心代码如下。
```
 /**
     * 画裁剪框边界线
     *
     * @param canvas
     */
    private void drawFrame(Canvas canvas) {
        mFramePaint.setStyle(Paint.Style.STROKE);
        mFramePaint.setFilterBitmap(true);
//        mFramePaint.setColor(mFrameColor);
//        mFramePaint.setStrokeWidth(mFrameStrokeWeight);
//        canvas.drawRect(mFrameRectF, mFramePaint);

        mFramePaint.setColor(getResources().getColor(R.color.crop_line_color));
        mFramePaint.setStrokeWidth(20);

        switch (mTouchArea){
            case LEFT_TOP:
                //横向top
                lastTop_left = moveX;
                lastTop_top = moveY;
                canvas.drawLine(moveX,moveY,lastTop_right,lastTop_bottom,mFramePaint);
                //竖向 left
                lastLeft_left = moveX;
                lastLeft_top = moveY;
                canvas.drawLine(moveX,moveY,lastLeft_right,lastLeft_bottom,mFramePaint);

//                //横向bottom
                canvas.drawLine(lastBottom_left,lastBottom_top,lastBottom_right,lastBottom_bottom,mFramePaint);
//
//                //竖向right
                canvas.drawLine(lastRight_left,lastRight_top,lastRight_right,lastRight_bottom,mFramePaint);
                break;
            case RIGHT_TOP:
                //横向top
                lastTop_right = moveX;
                lastTop_bottom = moveY;
                canvas.drawLine(lastTop_left,lastTop_top,moveX,moveY,mFramePaint);
                //竖向 left
                canvas.drawLine(lastLeft_left,lastLeft_top,lastLeft_right,lastLeft_bottom,mFramePaint);

                //横向bottom
                canvas.drawLine(lastBottom_left,lastBottom_top,lastBottom_right,lastBottom_bottom,mFramePaint);

                //竖向right
                lastRight_left = moveX;
                lastRight_top = moveY;
                canvas.drawLine(moveX,moveY,lastRight_right,lastRight_bottom,mFramePaint);
                break;

            case LEFT_BOTTOM:
                //横向top
                canvas.drawLine(lastTop_left,lastTop_top,lastTop_right,lastTop_bottom,mFramePaint);
                //竖向 left
                lastLeft_right = moveX;
                lastLeft_bottom = moveY;
                canvas.drawLine(lastLeft_left,lastLeft_top,moveX,moveY,mFramePaint);

                //横向bottom
                lastBottom_left = moveX;
                lastBottom_top = moveY;
                canvas.drawLine(moveX,moveY,lastBottom_right,lastBottom_bottom,mFramePaint);

                //竖向right
                canvas.drawLine(lastRight_left,lastRight_top,lastRight_right,lastRight_bottom,mFramePaint);
                break;
            case RIGHT_BOTTOM:
                //横向top
                canvas.drawLine(lastTop_left,lastTop_top,lastTop_right,lastTop_bottom,mFramePaint);
//                竖向 left
                canvas.drawLine(lastLeft_left,lastLeft_top,lastLeft_right,lastLeft_bottom,mFramePaint);

                //横向bottom
                lastBottom_right = moveX;
                lastBottom_bottom = moveY;
                canvas.drawLine(lastBottom_left,lastBottom_top,moveX,moveY,mFramePaint);

                //竖向right
                lastRight_right = moveX;
                lastRight_bottom = moveY;
                canvas.drawLine(lastRight_left,lastRight_top,moveX,moveY,mFramePaint);
                break;
            case OUT_OF_BOUNDS:
                //横向top
                canvas.drawLine(lineLeft,lineTop,lineRight,lineTop,mFramePaint);
                //竖向 left
                canvas.drawLine(lineLeft,lineTop,lineLeft,lineBottom,mFramePaint);

                //横向bottom
                canvas.drawLine(lineLeft,lineBottom,lineRight,lineBottom,mFramePaint);

                //竖向right
                canvas.drawLine(lineRight,lineTop,lineRight,lineBottom,mFramePaint);
        }
    }
```

划线的逻辑没什么复杂的，判断图片边界位置，确定坐标系，画四条线组成一个裁剪框就可以了。


* 画覆盖的半透明蒙层以及四个交汇点的图片。
注：此处的蒙层需跟随裁剪框形状改变，是不规则形状，使用path绘制。代码如下
```
    /**
     * 画裁剪框的半透明覆盖层和四角图片
     *
     * @param canvas
     */
    private void drawOverlay(Canvas canvas) {
        //设置画笔为填充模式
        mTranslucentPaint.setStyle(Paint.Style.FILL);
        mTranslucentPaint.setFilterBitmap(true);
        mTranslucentPaint.setColor(mOverlayColor);

        /*用path画出四条线所组成的图形*/
        Path path2 =new Path();
        path2.moveTo(lastTop_left,lastTop_top);
        path2.lineTo(lastRight_left,lastRight_top);
        path2.lineTo(lastBottom_right,lastBottom_bottom);
        path2.lineTo(lastBottom_left,lastBottom_top);
        path2.close();
        canvas.drawPath(path2, mTranslucentPaint);

        //画左上角图片
        canvas.drawBitmap(leftTopBitmap,lastTop_left - 45,lastLeft_top - 45,mFramePaint);
        //右上角
        canvas.drawBitmap(rightTopBitmap,lastTop_right - 45,lastRight_top - 45,mFramePaint);

        //左下角
        canvas.drawBitmap(leftBottomBitmap,lastLeft_right - 45,lastBottom_top - 45,mFramePaint);

        //右下角
        canvas.drawBitmap(rightBottomBitmap,lastRight_right - 45,lastBottom_bottom - 45,mFramePaint);

    }
```

* 接下来需要实现手指滑动对应边框顶点时，裁剪框线条跟随手指移动。
注：需判断手指落入裁剪框的什么位置，从而确定滑动了哪个顶点。核心代码如下：
```
 private void onActionMove(MotionEvent event) {
        float diffX = event.getX() - mLastX;
        float diffY = event.getY() - mLastY;
        moveX = event.getX();
        moveY = event.getY();
        // 区分点击的区域进行移动
        switch (mTouchArea) {
            case CENTER: {
                moveFrame(diffX, diffY);
                break;
            }
            case LEFT_TOP: {
                moveHandleLeftTop(diffX, diffY);
                break;
            }
            case RIGHT_TOP: {
                moveHandleRightTop(diffX, diffY);
                break;
            }
            case LEFT_BOTTOM: {
                moveHandleLeftBottom(diffX, diffY);
                break;
            }
            case RIGHT_BOTTOM: {
                moveHandleRightBottom(diffX, diffY);
                break;
            }

            case CENTER_LEFT: {
                moveHandleCenterLeft(diffX);
                break;
            }
            case CENTER_TOP: {
                moveHandleCenterTop(diffY);
                break;
            }
            case CENTER_RIGHT: {
                moveHandleCenterRight(diffX);
                break;
            }
            case CENTER_BOTTOM: {
                moveHandleCenterBottom(diffY);
                break;
            }
            case OUT_OF_BOUNDS: {
                break;
            }
        }
        invalidate();
        mLastX = event.getX();
        mLastY = event.getY();
    }

/**判断距离是否在右上角，此方法参考github博主————**/
 private void moveHandleRightTop(float diffX, float diffY) {
        if (mCropMode == CropModeEnum.FREE) {
            mFrameRectF.right += diffX;
            mFrameRectF.top += diffY;
            if (isWidthTooSmall()) {
                float offsetX = mFrameMinSize - mFrameRectF.width();
                mFrameRectF.right += offsetX;
            }
            if (isHeightTooSmall()) {
                float offsetY = mFrameMinSize - mFrameRectF.height();
                mFrameRectF.top -= offsetY;
            }
            checkScaleBounds();
        } else {
            float dx = diffX;
            float dy = diffX * getRatioY() / getRatioX();
            mFrameRectF.right += dx;
            mFrameRectF.top -= dy;
            if (isWidthTooSmall()) {
                float offsetX = mFrameMinSize - mFrameRectF.width();
                mFrameRectF.right += offsetX;
                float offsetY = offsetX * getRatioY() / getRatioX();
                mFrameRectF.top -= offsetY;
            }
            if (isHeightTooSmall()) {
                float offsetY = mFrameMinSize - mFrameRectF.height();
                mFrameRectF.top -= offsetY;
                float offsetX = offsetY * getRatioX() / getRatioY();
                mFrameRectF.right += offsetX;
            }
            float ox, oy;
            if (!isInsideX(mFrameRectF.right)) {
                ox = mFrameRectF.right - mImageRectF.right;
                mFrameRectF.right -= ox;
                oy = ox * getRatioY() / getRatioX();
                mFrameRectF.top += oy;
            }
            if (!isInsideY(mFrameRectF.top)) {
                oy = mImageRectF.top - mFrameRectF.top;
                mFrameRectF.top += oy;
                ox = oy * getRatioX() / getRatioY();
                mFrameRectF.right -= ox;
            }
        }
    }

```


▶Part 2：放大镜部分
* 放大镜部分使用 canvas.clipPath实现圆形效果。根据onTouch时手指移动位置同步移动canvas画布，核心代码如下：
```
//被放大的图形
    private void drawMagnifierPart(Canvas canvas){
        if(mPointX == -1.0f || mPointY == -1.0f) return;

        Path path = new Path();
        path.addCircle(RADIUS, RADIUS, RADIUS, Path.Direction.CW);

        canvas.clipPath(path);
      //画放大后的图

        float dx = RADIUS - (mPointX - moveLeft) * (FACTOR / cropScale);
        float dy =  RADIUS - (mPointY - moveTop) * (FACTOR / cropScale);
        canvas.translate(dx,dy);

        matrix.setScale(FACTOR, FACTOR);

        canvas.drawBitmap(mBitmap, matrix, null);
    }
```

▶Part 3:裁剪图片
* 裁剪图片使用Paint 的PorterDuff.Mode.SRC_IN属性，具体代码如下：
```
 /**
     * 获取裁剪图片
     * @return
     */
    public Bitmap getCroppedBitmap() {
        Bitmap source = getBitmap();
        if (source == null)
            return null;

        Bitmap temp = Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(temp);

        Paint pathPaint = new Paint();
        pathPaint.setAntiAlias(true);

        Path path = new Path();
        pathPaint.setStyle(Paint.Style.FILL);
        pathPaint.setColor(Color.YELLOW);
        path.moveTo(lastTop_left,lastTop_top);
        path.lineTo(lastRight_left,lastRight_top);
        path.lineTo(lastBottom_right,lastBottom_bottom);
        path.lineTo(lastBottom_left,lastBottom_top);
        path.lineTo(lastTop_left,lastTop_top);
        path.close();
        canvas.save();
        canvas.drawPath(path,pathPaint);
        canvas.restore();

        Paint bitmapPaint = new Paint();
        bitmapPaint.setAntiAlias(true);
        bitmapPaint.setFilterBitmap(true);
        bitmapPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));


        canvas.save();
        canvas.drawBitmap(source,mMatrix, bitmapPaint);
        canvas.restore();
        return temp;
    }
```
▶另附Github地址，欢迎交流。

[https://github.com/CelineDeer/CropBiggerView](https://github.com/CelineDeer/CropBiggerView)
