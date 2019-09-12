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

    //方向,0标识水平，1标识垂直
    protected int orientation = 0 ;

    //进度条的粗细（线条的粗细）
    protected int barSize = dp2px(4);

    //进度条部分未达到的颜色
    protected int unReachedColor = Color.parseColor("#eeeeee");

    //进度条部分已达到的颜色
    protected int reachedColor = Color.parseColor("#ff3333");

    //圆的颜色
    protected int circleColor = Color.parseColor("#ff3333");

    //禁用时的颜色
    private int disableColor = Color.parseColor("#dddddd");

    //字体颜色
    protected int textColor = Color.WHITE ;

    //视图实际的长度（进度条方向），绘制进度条的开始边界/结束边界
    protected float realSize ,drawBorderStart,drawBorderEnd;

    //圆的半径
    protected int circleRadius = dp2px(3);

    //文字大小
    protected int textSize = sp2px(12);

    //进度条相关画笔
    private Paint mPaint ;

    //文字画笔
    private Paint textPaint ;

    //是否显示进度
    private boolean showHint ;

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
        orientation = ta.getInt(R.styleable.ProgressBarView_android_orientation,orientation);
        showHint = ta.getBoolean(R.styleable.ProgressBarView_bar_show_hint,false);
        textColor = ta.getColor(R.styleable.ProgressBarView_android_textColor,textColor);
        textSize = (int) ta.getDimension(R.styleable.ProgressBarView_android_textSize,textSize);
        disableColor = ta.getColor(R.styleable.ProgressBarView_bar_disable_color,disableColor);
        ta.recycle();
        mPaint = new Paint();
        mPaint.setStrokeWidth(barSize);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);
        if(showHint){
            textPaint = new Paint();
            textPaint.setColor(textColor);
            textPaint.setTextSize(textSize);
            circleRadius = Math.max(circleRadius,textSize);
        }
    }


    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width , height ;
        if(orientation == 0){
            width = MeasureSpec.getSize(widthMeasureSpec);
            height = measureSize(heightMeasureSpec);
            realSize = width - getPaddingLeft() - getPaddingRight();
        }else{
            width = measureSize(widthMeasureSpec);
            height = MeasureSpec.getSize(heightMeasureSpec);
            realSize = height - getPaddingBottom() - getPaddingTop();
        }

        setMeasuredDimension(width,height);

        //计算实际的绘制边界
        drawBorderStart = circleRadius ;
        drawBorderEnd = realSize - circleRadius;
    }


    @Override
    protected synchronized void onDraw(Canvas canvas) {
        //保存画布
        canvas.save();
        if(orientation == 0) {
            //移动画布
            canvas.translate(getPaddingLeft(), getHeight() / 2);
        }else{
            canvas.translate(getWidth()/2,getPaddingTop());
        }
        //计算已加载进度在整个控件宽度的占比
        float radio = getProgress() * 1.0f / getMax();
        //获取已加载进度的宽度
        float progressSize = radio * realSize;

        //绘制未加载进度
        mPaint.setColor(isEnabled() ? unReachedColor:disableColor);
        float start,end ;
        if(orientation == 0){//水平绘制
            //绘制未加载进度
            start = progressSize < drawBorderStart ? drawBorderStart:progressSize;
            end = drawBorderEnd ;
            if ( end > start) {
                canvas.drawLine(start, 0, end, 0, mPaint);
            }
        }else{//垂直绘制
            start = drawBorderStart ;
            end = realSize - progressSize > drawBorderEnd ? drawBorderEnd:realSize - progressSize;
            if(start < end)
                canvas.drawLine(0,start,0,end,mPaint);
        }

        //绘制已加载进度
        mPaint.setColor(isEnabled()?reachedColor:disableColor);
        if(orientation == 0){
            start = drawBorderStart ;
            end = progressSize > drawBorderEnd ? drawBorderEnd : progressSize;
            if(end > start){//水平绘制
                canvas.drawLine(start, 0, end, 0, mPaint);
            }
        }else{
            end = realSize - progressSize < drawBorderStart ? drawBorderStart : realSize - progressSize;
            start = drawBorderEnd ;
            if(start> end)//垂直绘制
                canvas.drawLine(0,start,0,end,mPaint);
        }


        //绘制圆
        mPaint.setColor(isEnabled()?circleColor:disableColor);

        float centerOffset = progressSize;
        if(centerOffset <drawBorderStart){
            centerOffset = circleRadius ;
        }else if(centerOffset > drawBorderEnd){
            centerOffset = drawBorderEnd ;
        }
        //获取进度，随后绘制
        String progress = String.valueOf(getProgress());
        if(orientation == 0) {
            canvas.drawCircle(centerOffset, 0, circleRadius, mPaint);
            if(showHint){
                //测量待绘制的文本的宽度
                int textWidth = (int) textPaint.measureText(progress);
                int textHeight = (int) (textPaint.descent() - textPaint.ascent());
                //绘制进度文本
                canvas.drawText(progress,centerOffset - textWidth/2,circleRadius - textHeight/2,textPaint);
            }
        }else {
            centerOffset = realSize -centerOffset;
            canvas.drawCircle(0,  centerOffset, circleRadius, mPaint);
            if(showHint){
                //测量待绘制的文本的宽度
                int textWidth = (int) textPaint.measureText(progress);
                int textHeight = (int) (textPaint.descent() - textPaint.ascent());
                //绘制进度文本
                canvas.drawText(progress,- textWidth/2,centerOffset + textHeight/4,textPaint);

            }
        }

        //重置画布
        canvas.restore();

    }


    /**
     * 测量视图的值
     * @param measureSpec
     * @return
     */
    private int measureSize(int measureSpec) {
        int result = 0;
        //获取高度模式
        int mode = MeasureSpec.getMode(measureSpec);
        //获取宽度模式
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            //精准模式 用户设置为 比如80dp  match_parent fill_parent
            result = size;
        } else if(orientation == 0){//水平，测
            // paddingTop+paddingBottom+ progressbar高度和圆直径的最大值
            result = getPaddingTop() + getPaddingBottom() + Math.max(barSize, Math.abs(circleRadius * 2));
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(result, size);
            }
        }else{
            result = getPaddingLeft() + getPaddingRight() + Math.max(barSize, Math.abs(circleRadius * 2));
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(result, size);
            }
        }
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //若视图不可用，不处理触摸事件
        if(!isEnabled()){
            return false;
        }

        switch(event.getAction()){
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:

                int p ;
                float offset;
                if(orientation == 0){
                    offset = event.getX();
                    p = Math.round((offset/realSize)*getMax());

                }else{
                    offset = realSize - event.getY();
                    p = Math.round((offset/realSize)*getMax());
                }
                //若进度没有发生变化，不更新视图
                if(getProgress() == p){
                    break;
                }
                setProgress(p);
                break;
        }
        return true;
    }

    protected int dp2px(int dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, getResources().getDisplayMetrics());
    }

    protected int sp2px(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }
}
