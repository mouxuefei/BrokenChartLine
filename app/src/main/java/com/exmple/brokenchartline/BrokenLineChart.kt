package com.exmple.brokenchartline

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import java.util.*

class BrokenLineChart @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    /**
     * View宽度
     */
    private var mViewWidth: Int = 0
    /**
     * View高度
     */
    private var mViewHeight: Int = 0
    /**
     * 边框线画笔
     */
    private lateinit var mBorderLinePaint: Paint

    /**
     * 文本画笔
     */
    private lateinit var mTextPaint: Paint
    /**
     * 要绘制的折线线画笔
     */
    private lateinit var mBrokenLinePaint: Paint
    /**
     * 圆画笔
     */
    private lateinit var mCirclePaint: Paint
    /**
     * 圆的半径
     */
    private val radius = dp2px(3f)
    /**
     * 边框的左边距
     */
    private val mBrokenLineLeft = dp2px(10f)
    /**
     * 边框的上边距
     */
    private val mBrokenLineTop = dp2px(10f)
    /**
     * 边框的下边距
     */
    private val mBrokenLineBottom = dp2px(10f)
    /**
     * 边框的右边距
     */
    private val mBrokenLinerRight = dp2px(10f)
    /**
     * 需要绘制的宽度
     */
    private var mNeedDrawWidth: Float = 0.toFloat()
    /**
     * 需要绘制的高度
     */
    private var mNeedDrawHeight: Float = 0.toFloat()
    /**
     * 边框文本
     */
    private var valueTextY: ArrayList<Int> = ArrayList(3)

    /**
     * 数据值
     */
    private var valueOld: ArrayList<Int> = ArrayList(6)
    private var valueNew: ArrayList<Int> = ArrayList(6)

    //默认开始月份
    private var defaultStartMonth = 1

    /**
     * 图表的最大值
     */
    private val maxValue = 40

    init {
        initPaint()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mViewHeight = measuredHeight
        mViewWidth = measuredWidth
        initNeedDrawWidthAndHeight()
    }

    /**
     * 初始化画笔
     */
    private fun initPaint() {
        //关闭硬件加速
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        /**初始化文本画笔 */
        mTextPaint = Paint()
        mTextPaint.isAntiAlias = true
        mTextPaint.style = Paint.Style.FILL
        mTextPaint.textSize = sp2px(14f)
        mTextPaint.color = Color.parseColor("#ffffff")

        /**初始化边框线画笔 */
        mBorderLinePaint = Paint()
        mBorderLinePaint.isAntiAlias = true
        mBorderLinePaint.style = Paint.Style.STROKE
        mBorderLinePaint.color = Color.parseColor("#ffffff")

        /**初始化折线画笔 */
        mBrokenLinePaint = Paint()
        mBrokenLinePaint.isAntiAlias = true
        mBrokenLinePaint.style = Paint.Style.STROKE
        mBrokenLinePaint.color = Color.parseColor("#ff5400")
        mBrokenLinePaint.strokeWidth = dp2px(2f)
        mBrokenLinePaint.setShadowLayer(dp2px(5f), 0f, dp2px(5f), Color.GRAY)

        /** 画球*/
        mCirclePaint = Paint()
        mCirclePaint.isAntiAlias = true
        mCirclePaint.color = Color.parseColor("#ff5400")
        mCirclePaint.style = Paint.Style.FILL
        mCirclePaint.strokeWidth = dp2px(5f)
    }

    /**
     * 初始化绘制折线图的宽高
     */
    private fun initNeedDrawWidthAndHeight() {
        mNeedDrawWidth = mViewWidth.toFloat() - mBrokenLineLeft - mBrokenLinerRight
        mNeedDrawHeight = mViewHeight.toFloat() - mBrokenLineTop - mBrokenLineBottom
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBorderLineAndText(canvas)
        drawMonthText(canvas)
        drawBrokenLine(canvas)
        drawLineCircle(canvas)
    }

    private  fun drawMonthText(canvas: Canvas) {
        when {
            valueOld.size > 0 -> {
                var month = defaultStartMonth
                for (i in 1..valueOld.size) {
                    val averageWidth = (mNeedDrawWidth / (valueOld.size + 1)).toInt()
                    val fm = mTextPaint.fontMetrics
                    val mTxtHeight = Math.ceil((fm.leading - fm.ascent).toDouble()).toInt()
                    canvas.drawText(month.toString() + "月", (averageWidth * i).toFloat(), mNeedDrawHeight - mTxtHeight / 2, mTextPaint)
                    month++
                }
            }
        }
    }

    /**
     * 绘制线上的圆
     */
    private fun drawLineCircle(canvas: Canvas) {
        when {
            valueOld.size > 0 && valueNew.size > 0 -> {
                val pointsOld = getPoints(valueOld)
                val pointsNew = getPoints(valueNew)
                for (i in 0 until mAnimatorValue) {
                    val pointOld = pointsOld[i]
                    val pointNew = pointsNew[i]
                    mCirclePaint.color = Color.parseColor("#ff5400")
                    canvas.drawCircle(pointOld.x.toFloat(), pointOld.y.toFloat(), radius, mCirclePaint)
                    mCirclePaint.color = Color.parseColor("#ffff00")
                    canvas.drawCircle(pointNew.x.toFloat(), pointNew.y.toFloat(), radius, mCirclePaint)
                }
            }
        }

    }

    /**
     * 根据值绘制折线
     */
    private fun drawBrokenLine(canvas: Canvas) {
        when {
            valueOld.size > 0 && valueNew.size > 0 -> {
                val mPathOld = Path()
                val mPathNew = Path()
                val mPathOldshadow = Path()
                val mPathNewshadow = Path()
                val pointsOld = getPoints(valueOld)
                val pointsNew = getPoints(valueNew)
                for (i in 0 until mAnimatorValue) {
                    val pointOld = pointsOld[i]
                    val pointNew = pointsNew[i]
                    if (i == 0) {
                        mPathOld.moveTo(pointOld.x.toFloat(), pointOld.y.toFloat())
                        mPathNew.moveTo(pointNew.x.toFloat(), pointNew.y.toFloat())

                        mPathOldshadow.moveTo(pointOld.x.toFloat(), pointOld.y.toFloat())
                        mPathNewshadow.moveTo(pointNew.x.toFloat(), pointNew.y.toFloat())
                    } else {
                        mPathOld.lineTo(pointOld.x.toFloat(), pointOld.y.toFloat())
                        mPathNew.lineTo(pointNew.x.toFloat(), pointNew.y.toFloat())

                        mPathOldshadow.lineTo(pointOld.x.toFloat(), pointOld.y.toFloat())
                        mPathNewshadow.lineTo(pointNew.x.toFloat(), pointNew.y.toFloat())
                    }
                }
                mBrokenLinePaint.color = Color.parseColor("#ff5400")
                canvas.drawPath(mPathOld, mBrokenLinePaint)
                mBrokenLinePaint.color = Color.parseColor("#ffff00")
                canvas.drawPath(mPathNew, mBrokenLinePaint)
            }
        }
    }

    /**
     * 绘制边框线和边框文本
     */
    private fun drawBorderLineAndText(canvas: Canvas) {
        when {
            valueTextY.size > 0 -> {
                val averageHeight = mNeedDrawHeight / (valueTextY.size + 1)
                (1..valueTextY.size + 1).forEach { i ->
                    val nowadayHeight = averageHeight * (valueTextY.size + 1 - i)
                    canvas.drawLine(mBrokenLineLeft, nowadayHeight + mBrokenLineTop, mNeedDrawWidth, nowadayHeight + mBrokenLineTop, mBorderLinePaint)
                    if (i < valueTextY.size + 1) {
                        val fm = mTextPaint.fontMetrics
                        val mTxtHeight = Math.ceil((fm.leading - fm.ascent).toDouble()).toInt()
                        canvas.drawText(valueTextY[valueTextY.size - i].toString() + "万", mBrokenLineLeft, nowadayHeight + mBrokenLineTop - averageHeight / 2 + mTxtHeight / 2, mTextPaint)
                    }
                }

            }
        }
    }

    /**
     * 设置开始月份，默认为1
     */
    fun setStartMonth(month: Int) {
        this.defaultStartMonth = month
        invalidate()
    }

    /**
     * 设置y轴的值
     */
    fun setValueY(value: ArrayList<Int>) {
        this.valueTextY = value
        invalidate()
    }

    /**
     * 设置y轴的值
     */
    fun setPointValues(valueNew: ArrayList<Int>, valueOld: ArrayList<Int>) {
        this.valueNew = valueNew
        this.valueOld = valueOld
        initAnimator(valueNew.size)
    }
    private lateinit var valueAnimator: ValueAnimator
    private var mAnimatorValue: Int = 0
    private lateinit var mUpdateListener: ValueAnimator.AnimatorUpdateListener
    private val defaultDuration = 500

    private fun initAnimator( size:Int) {
        valueAnimator = ValueAnimator.ofInt(0, size).setDuration(defaultDuration.toLong())
        mUpdateListener = ValueAnimator.AnimatorUpdateListener { animation ->
            mAnimatorValue = animation.animatedValue as Int
            invalidate()
        }
        valueAnimator.addUpdateListener(mUpdateListener)
        valueAnimator.start()
    }

    /**
     * 根据值计算在该值的 x，y坐标
     */
    fun getPoints(list: ArrayList<Int>): ArrayList<Point> {
        val avaregwidth = mNeedDrawWidth / (list.size + 1)
        val points = ArrayList<Point>(list.size)
        list.indices.forEach { i ->
            val valueY = list[i].toFloat()
            val averaHeight = (mNeedDrawHeight * 3 / 4 / maxValue).toDouble()
            val drawHeight = mNeedDrawHeight * 3 / 4 - (valueY * averaHeight).toFloat() + mBrokenLineTop
            val pointY = drawHeight.toInt()
            val pointX = ((avaregwidth * (i + 1)).toInt() + mBrokenLineLeft).toInt()
            val point = Point(pointX, pointY)
            points.add(point)
        }
        return points
    }

    /**
     * 将dp值转换为px值
     * @param dp 需要转换的dp值
     * @return px值
     */
    fun dp2px(dp: Float): Float {
        return (resources.displayMetrics.density * dp + 0.5f)
        //        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    /**
     * 将px值转换为dp值
     * @param px 需要转换的px值
     * @return dp值
     */
    fun px2dp(px: Float): Float {
        return (px / resources.displayMetrics.density + 0.5f)
    }

    fun sp2px(sp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)
    }
}