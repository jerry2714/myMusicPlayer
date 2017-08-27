package ledcubeproject.ui;

/**
 * Created by user123 on 2017/3/26.
 */

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class ColorPickerDialog extends Dialog {
    private final boolean debug = true;
    private final String TAG = "ColorPicker";

    Context context;
    private String title;//標題
    private int mInitialColor;//初始顏色
    private OnColorChangedListener mListener;

    /**
     * 初始顏色黑色
     *
     * @param context
     * @param title    對話框標題
     * @param listener 回調
     */
    public ColorPickerDialog(Context context, String title,
                             OnColorChangedListener listener) {
        this(context, Color.BLACK, title, listener);
    }

    /**
     * @param context
     * @param initialColor 初始顏色
     * @param title        標題
     * @param listener     回調
     */
    public ColorPickerDialog(Context context, int initialColor,
                             String title, OnColorChangedListener listener) {
        super(context);
        this.context = context;
        mListener = listener;
        mInitialColor = initialColor;
        this.title = title;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager manager = getWindow().getWindowManager();
        int height = (int) (manager.getDefaultDisplay().getHeight() * 0.5f);
        int width = (int) (manager.getDefaultDisplay().getWidth() * 0.7f);
        ColorPickerView myView = new ColorPickerView(context, height, width);
        setContentView(myView);
        setTitle(title);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getmInitialColor() {
        return mInitialColor;
    }

    public void setmInitialColor(int mInitialColor) {
        this.mInitialColor = mInitialColor;
    }

    public OnColorChangedListener getmListener() {
        return mListener;
    }

    public void setmListener(OnColorChangedListener mListener) {
        this.mListener = mListener;
    }

    /**
     * 回調接口
     *
     * @author <a href="clarkamx@gmail.com">LynK</a>
     *         <p>
     *         Create on 2012-1-6 上午8:21:05
     */
    public interface OnColorChangedListener {
        /**
         * 回調函數
         *
         * @param color 選中的顏色
         */
        void colorChanged(int color);
    }

    private class ColorPickerView extends View {
        private final int[] mCircleColors;//漸變色環顏色
        private final int[] mRectColors;//漸變方塊顏色
        private Paint mPaint;//漸變色環畫筆
        private Paint mCenterPaint;//中間圓畫筆
        private Paint mLinePaint;//分隔線畫筆
        private Paint mRectPaint;//漸變方塊畫筆
        private Shader rectShader;//漸變方塊漸變圖像
        private float rectLeft;//漸變方塊左x坐標
        private float rectTop;//漸變方塊右x坐標
        private float rectRight;//漸變方塊上y坐標
        private float rectBottom;//漸變方塊下y坐標
        private int mHeight;//View高
        private int mWidth;//View寬
        private float r;//色環半徑(paint中部)
        private float centerRadius;//中心圓半徑

        private boolean downInCircle = true;//按在漸變環上
        private boolean downInRect;//按在漸變方塊上
        private boolean highlightCenter;//高亮
        private boolean highlightCenterLittle;//微亮

        public ColorPickerView(Context context, int height, int width) {
            super(context);
            this.mHeight = height - 36;
            this.mWidth = width;
            setMinimumHeight(height - 36);
            setMinimumWidth(width);

//漸變色環参數
            mCircleColors = new int[]{0xFFFF0000, 0xFFFF00FF, 0xFF0000FF,
                    0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000};
            Shader s = new SweepGradient(0, 0, mCircleColors, null);
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setShader(s);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(50);
            r = width / 2 * 0.7f - mPaint.getStrokeWidth() * 0.5f;

//中心圓参數
            mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mCenterPaint.setColor(mInitialColor);
            mCenterPaint.setStrokeWidth(5);
            centerRadius = (r - mPaint.getStrokeWidth() / 2) * 0.7f;

//邊框参數
            mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mLinePaint.setColor(Color.parseColor("#72A1D1"));
            mLinePaint.setStrokeWidth(4);

//黑白漸變参數
            mRectColors = new int[]{0xFF000000, mCenterPaint.getColor(), 0xFFFFFFFF};
            mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mRectPaint.setStrokeWidth(5);
            rectLeft = -r - mPaint.getStrokeWidth() * 0.5f;
            rectTop = r + mPaint.getStrokeWidth() * 0.5f +
                    mLinePaint.getStrokeMiter() * 0.5f + 15;
            rectRight = r + mPaint.getStrokeWidth() * 0.5f;
            rectBottom = rectTop + 50;
        }

        @Override
        protected void onDraw(Canvas canvas) {
//移動中心
            canvas.translate(mWidth / 2, mHeight / 2 - 50);
//畫中心圓
            canvas.drawCircle(0, 0, centerRadius, mCenterPaint);
//是否顯示中心圓外的小圓環
            if (highlightCenter || highlightCenterLittle) {
                int c = mCenterPaint.getColor();
                mCenterPaint.setStyle(Paint.Style.STROKE);
                if (highlightCenter) {
                    mCenterPaint.setAlpha(0xFF);
                } else if (highlightCenterLittle) {
                    mCenterPaint.setAlpha(0x90);
                }
                canvas.drawCircle(0, 0,
                        centerRadius + mCenterPaint.getStrokeWidth(), mCenterPaint);

                mCenterPaint.setStyle(Paint.Style.FILL);
                mCenterPaint.setColor(c);
            }
//畫色環
            canvas.drawOval(new RectF(-r, -r, r, r), mPaint);
//畫黑白漸變塊
            if (downInCircle) {
                mRectColors[1] = mCenterPaint.getColor();
            }
            rectShader = new LinearGradient(rectLeft, 0, rectRight, 0, mRectColors, null, Shader.TileMode.MIRROR);
            mRectPaint.setShader(rectShader);
            canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, mRectPaint);
            float offset = mLinePaint.getStrokeWidth() / 2;
            canvas.drawLine(rectLeft - offset, rectTop - offset * 2,
                    rectLeft - offset, rectBottom + offset * 2, mLinePaint);//左
            canvas.drawLine(rectLeft - offset * 2, rectTop - offset,
                    rectRight + offset * 2, rectTop - offset, mLinePaint);//上
            canvas.drawLine(rectRight + offset, rectTop - offset * 2,
                    rectRight + offset, rectBottom + offset * 2, mLinePaint);//右
            canvas.drawLine(rectLeft - offset * 2, rectBottom + offset,
                    rectRight + offset * 2, rectBottom + offset, mLinePaint);//下
            super.onDraw(canvas);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX() - mWidth / 2;
            float y = event.getY() - mHeight / 2 + 50;
            boolean inCircle = inColorCircle(x, y,
                    r + mPaint.getStrokeWidth() / 2, r - mPaint.getStrokeWidth() / 2);
            boolean inCenter = inCenter(x, y, centerRadius);
            boolean inRect = inRect(x, y);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downInCircle = inCircle;
                    downInRect = inRect;
                    highlightCenter = inCenter;
                case MotionEvent.ACTION_MOVE:
                    if (downInCircle && inCircle) {//down按在漸變色環內, 且move也在漸變色環內
                        float angle = (float) Math.atan2(y, x);
                        float unit = (float) (angle / (2 * Math.PI));
                        if (unit < 0) {
                            unit += 1;
                        }
                        mCenterPaint.setColor(interpCircleColor(mCircleColors, unit));
                        if (debug) Log.v(TAG, "色環內, 坐標: " + x + "," + y);
                    } else if (downInRect && inRect) {//down在漸變方塊內, 且move也在漸變方塊內
                        mCenterPaint.setColor(interpRectColor(mRectColors, x));
                    }
                    if (debug)
                        Log.v(TAG, "[MOVE] 高亮: " + highlightCenter + "微亮: " + highlightCenterLittle + " 中心: " + inCenter);
                    if ((highlightCenter && inCenter) || (highlightCenterLittle && inCenter)) {//點擊中心圓, 當前移動在中心圓
                        highlightCenter = true;
                        highlightCenterLittle = false;
                    } else if (highlightCenter || highlightCenterLittle) {//點擊在中心圓, 當前移出中心圓
                        highlightCenter = false;
                        highlightCenterLittle = true;
                    } else {
                        highlightCenter = false;
                        highlightCenterLittle = false;
                    }
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    if (highlightCenter && inCenter) {//點擊在中心圓, 且當前啟動在中心圓
                        if (mListener != null) {
                            mListener.colorChanged(mCenterPaint.getColor());
                            ColorPickerDialog.this.dismiss();
                        }
                    }
                    if (downInCircle) {
                        downInCircle = false;
                    }
                    if (downInRect) {
                        downInRect = false;
                    }
                    if (highlightCenter) {
                        highlightCenter = false;
                    }
                    if (highlightCenterLittle) {
                        highlightCenterLittle = false;
                    }
                    invalidate();
                    break;
            }
            return true;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(mWidth, mHeight);
        }

        /**
         * 坐標是否在色環上
         *
         * @param x         坐標
         * @param y         坐標
         * @param outRadius 色環外半徑
         * @param inRadius  色環內半徑
         * @return
         */
        private boolean inColorCircle(float x, float y, float outRadius, float inRadius) {
            double outCircle = Math.PI * outRadius * outRadius;
            double inCircle = Math.PI * inRadius * inRadius;
            double fingerCircle = Math.PI * (x * x + y * y);
            if (fingerCircle < outCircle && fingerCircle > inCircle) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * 坐標是否在中心圓上
         *
         * @param x            坐標
         * @param y            坐標
         * @param centerRadius 圓半徑
         * @return
         */
        private boolean inCenter(float x, float y, float centerRadius) {
            double centerCircle = Math.PI * centerRadius * centerRadius;
            double fingerCircle = Math.PI * (x * x + y * y);
            if (fingerCircle < centerCircle) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * 坐標是否在漸變色中
         *
         * @param x
         * @param y
         * @return
         */
        private boolean inRect(float x, float y) {
            if (x <= rectRight && x >= rectLeft && y <= rectBottom && y >= rectTop) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * 獲取圓環上顏色
         *
         * @param colors
         * @param unit
         * @return
         */
        private int interpCircleColor(int colors[], float unit) {
            if (unit <= 0) {
                return colors[0];
            }
            if (unit >= 1) {
                return colors[colors.length - 1];
            }

            float p = unit * (colors.length - 1);
            int i = (int) p;
            p -= i;

// now p is just the fractional part [0...1) and i is the index
            int c0 = colors[i];
            int c1 = colors[i + 1];
            int a = ave(Color.alpha(c0), Color.alpha(c1), p);
            int r = ave(Color.red(c0), Color.red(c1), p);
            int g = ave(Color.green(c0), Color.green(c1), p);
            int b = ave(Color.blue(c0), Color.blue(c1), p);

            return Color.argb(a, r, g, b);
        }

        /**
         * 獲取漸變塊上顏色
         *
         * @param colors
         * @param x
         * @return
         */
        private int interpRectColor(int colors[], float x) {
            int a, r, g, b, c0, c1;
            float p;
            if (x < 0) {
                c0 = colors[0];
                c1 = colors[1];
                p = (x + rectRight) / rectRight;
            } else {
                c0 = colors[1];
                c1 = colors[2];
                p = x / rectRight;
            }
            a = ave(Color.alpha(c0), Color.alpha(c1), p);
            r = ave(Color.red(c0), Color.red(c1), p);
            g = ave(Color.green(c0), Color.green(c1), p);
            b = ave(Color.blue(c0), Color.blue(c1), p);
            return Color.argb(a, r, g, b);
        }

        private int ave(int s, int d, float p) {
            return s + Math.round(p * (d - s));
        }
    }
}

