package ui.xuxp.com.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.ProgressBar;

import ui.xuxp.com.R;

public class ProgressBarView extends ProgressBar {

    //进度条的高度（线条的粗细）
    protected int barSize = dp2px(2);

    //进度条部分未达到的颜色
    protected int unReachedColor = Color.parseColor("#eeeeee");

    //进度条部分已达到的颜色
    protected int reachedColor = Color.parseColor("#ff3333");

    //圆的颜色
    protected int circleColor = Color.parseColor("#ff3333");

    //视图实际的宽度，绘制进度条的开始边界/结束边界
    protected float realWeight ,drawBorderStart,drawBorderEnd;

    //圆的半径
    protected int circleRadius = dp2px(3);

    //画笔
    private Paint mPaint ;

    public ProgressBarView(Context context) {
        this(context,null);
    }

    public ProgressBarView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ProgressBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.ProgressBarView);
        barSize = (int) ta.getDimension(R.styleable.ProgressBarView_bar_size,barSize);
        unReachedColor = ta.getColor(R.styleable.ProgressBarView_bar_unreach_color, unReachedColor);
        reachedColor = ta.getColor(R.styleable.ProgressBarView_bar_reach_color,reachedColor);
        circleColor = ta.getColor(R.styleable.ProgressBarView_bar_circle_color,circleColor);
        circleRadius = (int) ta.getDimension(R.styleable.ProgressBarView_bar_circle_radius,circleRadius);
        ta.recycle();
        mPaint = new Paint();
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }


    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        setMeasuredDimension(width,height);

        //计算实际的绘制宽度
        realWeight = width - getPaddingLeft() - getPaddingRight();
        drawBorderStart = circleRadius ;
        drawBorderEnd = realWeight - circleRadius;
    }


    @Override
    protected synchronized void onDraw(Canvas canvas) {
        //保存画布
        canvas.save();
        //移动画布
        canvas.translate(getPaddingLeft(), getHeight() / 2);
        //计算左边进度在整个控件宽度的占比
        float radio = getProgress() * 1.0f / getMax();
        //获取左边进度的宽度
        float progressX = radio * realWeight;

        if (progressX > realWeight) {
            //左边进度+文字的宽度超过progressbar的宽度 重新计算左边进度的宽度 这个时候也就意味着不需要绘制右边进度
            progressX = realWeight;
        }

        //绘制未加载进度
        float start = progressX < drawBorderStart ? drawBorderStart:progressX;
        float end = drawBorderEnd ;
        if ( end > start) {
            mPaint.setColor(unReachedColor);
            mPaint.setStrokeWidth(barSize);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setAntiAlias(true);
            canvas.drawLine(start, 0, end, 0, mPaint);
        }

        //计算左边进度结束的位置 如果结束的位置小于0就不需要绘制左边的进度
        start = drawBorderStart ;
        end = progressX > drawBorderEnd ? drawBorderEnd : progressX;
        //绘制已加载进度
        if (end > start) {
            mPaint.setColor(reachedColor);
            mPaint.setStrokeWidth(barSize);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setAntiAlias(true);
            canvas.drawLine(start, 0, end, 0, mPaint);
        }

        //绘制圆
        mPaint.setColor(circleColor);

        float circleX = progressX;
        if(circleX <circleRadius){
            circleX = circleRadius ;
        }else if(circleX > realWeight - circleRadius){
            circleX = realWeight - circleRadius ;
        }
        canvas.drawCircle(circleX,0,circleRadius,mPaint);

        //重置画布
        canvas.restore();

    }


    /**
     * 测量视图的高度
     * @param heightMeasureSpec
     * @return
     */
    private int measureHeight(int heightMeasureSpec) {
        int result = 0;
        //获取高度模式
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        //获取宽度模式
        int size = MeasureSpec.getSize(heightMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            //精准模式 用户设置为 比如80dp  match_parent fill_parent
            result = size;
        } else {
            // paddingTop+paddingBottom+ progressbar高度和圆直径的最大值
            result = getPaddingTop() + getPaddingBottom() + Math.max(barSize, Math.abs(circleRadius * 2));
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(result, size);
            }
        }
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                float xUp = event.getX();
                int p = Math.round((xUp/realWeight)*getMax());
                //若进度没有发生变化，不更新视图
                if(getProgress() == p){
                    break;
                }
                setProgress(p);
                break;
        }
        invalidate();
        return true;
    }

    protected int dp2px(int dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, getResources().getDisplayMetrics());
    }

    protected int sp2px(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }
}
