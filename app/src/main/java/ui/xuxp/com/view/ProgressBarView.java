package ui.xuxp.com.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
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

    //视图实际的长度（进度条方向）,进度条长度，绘制进度条的开始边界/结束边界
    protected float viewWidth,viewHeight, realSize ,lineSize ,lineStart,lineEnd ;

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

    /**
     * 构造方法
     * @param context
     * @param attrs 属性集，通过将布局文件中的属性解析出来，以key-value的形式存储起来
     * @param defStyleAttr
     */
    public ProgressBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //获取属性
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
        //若需要显示文字信息，则进行画笔的初始化
        if(showHint){
            textPaint = new Paint();
            textPaint.setColor(textColor);
            textPaint.setTextSize(textSize);
            circleRadius = Math.max(circleRadius,textSize);
        }
    }


    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(orientation == 0){
            viewWidth = MeasureSpec.getSize(widthMeasureSpec);
            viewHeight = measureSize(heightMeasureSpec);
            realSize = viewWidth - getPaddingLeft() - getPaddingRight();
        }else{
            viewWidth = measureSize(widthMeasureSpec);
            viewHeight = MeasureSpec.getSize(heightMeasureSpec);
            realSize = viewHeight - getPaddingBottom() - getPaddingTop();
        }

        setMeasuredDimension((int)viewWidth,(int)viewHeight);

        //计算实际的绘制边界
        lineStart = circleRadius ;
        lineEnd = realSize - circleRadius;
        lineSize = realSize - circleRadius * 2 ;
    }


    @Override
    protected synchronized void onDraw(Canvas canvas) {
        //保存画布
        canvas.save();
        //移动画布,令画布的进度条位于x/y轴上
        if(orientation == 0) {
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
            start = lineBoundaryValid(progressSize);
            end = lineEnd ;
            if ( end > start) {
                canvas.drawLine(start, 0, end, 0, mPaint);
            }
        }else{//垂直绘制
            start = lineStart ;
            end = lineBoundaryValid(realSize - progressSize);
            if(end > start)
                canvas.drawLine(0,start,0,end,mPaint);
        }

        //绘制已加载进度
        mPaint.setColor(isEnabled()?reachedColor:disableColor);
        if(orientation == 0){
            start = lineStart ;
            end = lineBoundaryValid(progressSize);
            if(end > start){//水平绘制
                canvas.drawLine(start, 0, end, 0, mPaint);
            }
        }else{
            start = lineBoundaryValid(realSize - progressSize) ;
            end = lineEnd ;
            if(end > start)//垂直绘制
                canvas.drawLine(0,start,0,end,mPaint);
        }


        //绘制圆
        mPaint.setColor(isEnabled()?circleColor:disableColor);

        float centerOffset ;

        //获取进度，随后绘制
        String progress = String.valueOf(getProgress());
        if(orientation == 0) {
            centerOffset = lineBoundaryValid(progressSize);
            canvas.drawCircle(centerOffset, 0, circleRadius, mPaint);
            if(showHint){
                //测量待绘制的文本的宽度
                int textWidth = (int) textPaint.measureText(progress);
                int textHeight = (int) (textPaint.descent() - textPaint.ascent());
                //绘制进度文本
                canvas.drawText(progress,centerOffset - textWidth/2,circleRadius - textHeight/2,textPaint);
            }
        }else {
            centerOffset = lineBoundaryValid(realSize - progressSize);
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
     *
     * @param measureSpec 由View的onMeasure方法回调得到的measureSpec
     * @return
     */
    private int measureSize(int measureSpec) {
        //预设结果输出
        int result = 0;
        //获取度量模式
        int mode = MeasureSpec.getMode(measureSpec);
        //获取尺寸大小
        int size = MeasureSpec.getSize(measureSpec);

        if (mode == MeasureSpec.EXACTLY) {//若度量模式为精确测量，宽度尺寸大小即为最终结果大小
            //用户设置为 比如80dp  match_parent fill_parent
            result = size;
        } else if(orientation == 0){//水平，测
            // 上下边距 + Math.max(进度条粗细，圆的直径)
            result = getPaddingTop() + getPaddingBottom() + Math.max(barSize, Math.abs(circleRadius * 2));

            //若模式设定为MeasureSpec.AT_MOST，则自定义控件的宽度应该受到父布局的限定
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(result, size);
            }
        }else{
            //左右边距 + Math.max(进度条粗细，圆的直径)
            result = getPaddingLeft() + getPaddingRight() + Math.max(barSize, Math.abs(circleRadius * 2));

            //若模式设定为MeasureSpec.AT_MOST，则自定义控件的宽度应该受到父布局的限定
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
//                    Log.i(TAG,String.format("x = event.getX() = %s , realSize = %s , viewWidth = %s",event.getX(),realSize , viewWidth));
                    offset = event.getX();
                    /*if(offset <= getPaddingLeft()){
                        offset = 0 ;
                    }else if(offset >= viewWidth - getPaddingRight()){
                        offset = realSize ;
                    }*/
                    p = Math.round((offset/realSize)*getMax());

                }else{
//                    Log.i(TAG,String.format("x = event.getY() = %s , realSize = %s , viewHeight = %s",event.getY(),realSize , viewHeight));
                    offset = getHeight() - event.getY();

                    /*if(offset <= getPaddingBottom()){
                        offset = 0 ;
                    }else if(offset >= viewHeight - getPaddingTop()){
                        offset = realSize ;
                    }*/
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

    private static final String TAG = ProgressBarView.class.getSimpleName();
    /**
     * 边界检测，若值超出了进度条绘制的边界，则进行校正
     * @param value value
     * @return  valid value
     */
    protected float lineBoundaryValid(float value){
        return value < lineStart ? lineStart : value > lineEnd ? lineEnd : value ;
    }

    protected int dp2px(int dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, getResources().getDisplayMetrics());
    }

    protected int sp2px(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }
}
