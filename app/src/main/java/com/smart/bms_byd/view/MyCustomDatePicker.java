package com.smart.bms_byd.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.smart.bms_byd.R;
import com.smart.bms_byd.util.DateFormatUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MyCustomDatePicker extends LinearLayout implements PickerView.OnSelectListener {

    private Context mContext;
    private Callback mCallback;
    private Calendar mBeginTime, mEndTime;
    public Calendar mSelectedTime;

    private PickerView mDpvYear, mDpvMonth, mDpvDay, mDpvHour, mDpvMinute;
    private TextView mTvHourUnit, mTvMinuteUnit;

    private int mBeginYear, mBeginMonth, mBeginDay, mBeginHour, mBeginMinute,
            mEndYear, mEndMonth, mEndDay, mEndHour, mEndMinute;
    private List<String> mYearUnits = new ArrayList<>(), mMonthUnits = new ArrayList<>(), mDayUnits = new ArrayList<>(),
            mHourUnits = new ArrayList<>(), mMinuteUnits = new ArrayList<>();
    private DecimalFormat mDecimalFormat = new DecimalFormat("00");

    private boolean mCanShowPreciseTime;
    private int mScrollUnits = SCROLL_UNIT_HOUR + SCROLL_UNIT_MINUTE;

    /**
     * 时间单位：时、分
     */
    private static final int SCROLL_UNIT_HOUR = 0b1;
    private static final int SCROLL_UNIT_MINUTE = 0b10;

    /**
     * 时间单位的最大显示值
     */
    private static final int MAX_MINUTE_UNIT = 59;
    private static final int MAX_HOUR_UNIT = 23;
    private static final int MAX_MONTH_UNIT = 12;

    /**
     * 级联滚动延迟时间
     */
    private static final long LINKAGE_DELAY_DEFAULT = 100L;

    /**
     * 时间选择结果回调接口
     */
    public interface Callback {
        void onTimeSelected(long timestamp);
    }


    public MyCustomDatePicker(Context context) {
        super(context);
        this.mContext = context;
    }

    public MyCustomDatePicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        LinearLayout.inflate(context, R.layout.my_date_picker,this);
    }

    public MyCustomDatePicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;

    }

    public void initView(Long beginTimestamp, Long endTimestamp, Callback callback) {
        mBeginTime = Calendar.getInstance();
        mBeginTime.setTimeInMillis(beginTimestamp);
        mEndTime = Calendar.getInstance();
        mEndTime.setTimeInMillis(endTimestamp);
        mSelectedTime = Calendar.getInstance();
        mCallback = callback;
        initView();
        initData();

    }

    public void updateSelectTime(String strTime) {
        mSelectedTime.setTimeInMillis(DateFormatUtils.str2Long(strTime, false));
        int iYear = mSelectedTime.get(Calendar.YEAR);
        for (int iPos = 0;iPos<mYearUnits.size();iPos++) {
            if (Integer.parseInt(mYearUnits.get(iPos)) == iYear) {
                mDpvYear.setSelected(iPos);
            }
        }
        linkageMonthUnit(true, LINKAGE_DELAY_DEFAULT);
    }

    private void initView() {
        mTvHourUnit = findViewById(R.id.tv_hour_unit);
        mTvMinuteUnit = findViewById(R.id.tv_minute_unit);

        mDpvYear = findViewById(R.id.dpv_year);
        mDpvYear.setOnSelectListener(this);
        mDpvMonth = findViewById(R.id.dpv_month);
        mDpvMonth.setOnSelectListener(this);
        mDpvDay = findViewById(R.id.dpv_day);
        mDpvDay.setOnSelectListener(this);
        mDpvHour = findViewById(R.id.dpv_hour);
        mDpvHour.setOnSelectListener(this);
        mDpvMinute = findViewById(R.id.dpv_minute);
        mDpvMinute.setOnSelectListener(this);
    }

    private void initData() {
        mSelectedTime.setTimeInMillis(mEndTime.getTimeInMillis());

        mBeginYear = mBeginTime.get(Calendar.YEAR);
        // Calendar.MONTH 值为 0-11
        mBeginMonth = mBeginTime.get(Calendar.MONTH) + 1;
        mBeginDay = mBeginTime.get(Calendar.DAY_OF_MONTH);
        mBeginHour = mBeginTime.get(Calendar.HOUR_OF_DAY);
        mBeginMinute = mBeginTime.get(Calendar.MINUTE);

        mEndYear = mEndTime.get(Calendar.YEAR);
        mEndMonth = mEndTime.get(Calendar.MONTH) + 1;
        mEndDay = mEndTime.get(Calendar.DAY_OF_MONTH);
        mEndHour = mEndTime.get(Calendar.HOUR_OF_DAY);
        mEndMinute = mEndTime.get(Calendar.MINUTE);

        boolean canSpanYear = mBeginYear != mEndYear;
        boolean canSpanMon = !canSpanYear && mBeginMonth != mEndMonth;
        boolean canSpanDay = !canSpanMon && mBeginDay != mEndDay;
        boolean canSpanHour = !canSpanDay && mBeginHour != mEndHour;
        boolean canSpanMinute = !canSpanHour && mBeginMinute != mEndMinute;
        if (canSpanYear) {
            initDateUnits(MAX_MONTH_UNIT, mBeginTime.getActualMaximum(Calendar.DAY_OF_MONTH), MAX_HOUR_UNIT, MAX_MINUTE_UNIT);
        } else if (canSpanMon) {
            initDateUnits(mEndMonth, mBeginTime.getActualMaximum(Calendar.DAY_OF_MONTH), MAX_HOUR_UNIT, MAX_MINUTE_UNIT);
        } else if (canSpanDay) {
            initDateUnits(mEndMonth, mEndDay, MAX_HOUR_UNIT, MAX_MINUTE_UNIT);
        } else if (canSpanHour) {
            initDateUnits(mEndMonth, mEndDay, mEndHour, MAX_MINUTE_UNIT);
        } else if (canSpanMinute) {
            initDateUnits(mEndMonth, mEndDay, mEndHour, mEndMinute);
        }
    }

    private void initDateUnits(int endMonth, int endDay, int endHour, int endMinute) {
        for (int i = mBeginYear; i <= mEndYear; i++) {
            mYearUnits.add(String.valueOf(i));
        }

        for (int i = mBeginMonth; i <= endMonth; i++) {
            mMonthUnits.add(mDecimalFormat.format(i));
        }

        for (int i = mBeginDay; i <= endDay; i++) {
            mDayUnits.add(mDecimalFormat.format(i));
        }

        if ((mScrollUnits & SCROLL_UNIT_HOUR) != SCROLL_UNIT_HOUR) {
            mHourUnits.add(mDecimalFormat.format(mBeginHour));
        } else {
            for (int i = mBeginHour; i <= endHour; i++) {
                mHourUnits.add(mDecimalFormat.format(i));
            }
        }

        if ((mScrollUnits & SCROLL_UNIT_MINUTE) != SCROLL_UNIT_MINUTE) {
            mMinuteUnits.add(mDecimalFormat.format(mBeginMinute));
        } else {
            for (int i = mBeginMinute; i <= endMinute; i++) {
                mMinuteUnits.add(mDecimalFormat.format(i));
            }
        }

        mDpvYear.setDataList(mYearUnits);
        mDpvYear.setSelected(0);
        mDpvMonth.setDataList(mMonthUnits);
        mDpvMonth.setSelected(0);
        mDpvDay.setDataList(mDayUnits);
        mDpvDay.setSelected(0);
        mDpvHour.setDataList(mHourUnits);
        mDpvHour.setSelected(0);
        mDpvMinute.setDataList(mMinuteUnits);
        mDpvMinute.setSelected(0);

        setCanScroll();
    }

    private void setCanScroll() {
        mDpvYear.setCanScroll(mYearUnits.size() > 1);
        mDpvMonth.setCanScroll(mMonthUnits.size() > 1);
        mDpvDay.setCanScroll(mDayUnits.size() > 1);
        mDpvHour.setCanScroll(mHourUnits.size() > 1 && (mScrollUnits & SCROLL_UNIT_HOUR) == SCROLL_UNIT_HOUR);
        mDpvMinute.setCanScroll(mMinuteUnits.size() > 1 && (mScrollUnits & SCROLL_UNIT_MINUTE) == SCROLL_UNIT_MINUTE);
    }


    @Override
    public void onSelect(View view, String selected) {
        if (view == null || TextUtils.isEmpty(selected)) return;

        int timeUnit;
        try {
            timeUnit = Integer.parseInt(selected);
        } catch (Throwable ignored) {
            return;
        }

        switch (view.getId()) {
            case R.id.dpv_year:
                mSelectedTime.set(Calendar.YEAR, timeUnit);
                linkageMonthUnit(true, LINKAGE_DELAY_DEFAULT);
                break;

            case R.id.dpv_month:
                // 防止类似 2018/12/31 滚动到11月时因溢出变成 2018/12/01
                int lastSelectedMonth = mSelectedTime.get(Calendar.MONTH) + 1;
                mSelectedTime.add(Calendar.MONTH, timeUnit - lastSelectedMonth);
                linkageDayUnit(true, LINKAGE_DELAY_DEFAULT);
                break;

            case R.id.dpv_day:
                mSelectedTime.set(Calendar.DAY_OF_MONTH, timeUnit);
                linkageHourUnit(true, LINKAGE_DELAY_DEFAULT);
                break;

            case R.id.dpv_hour:
                mSelectedTime.set(Calendar.HOUR_OF_DAY, timeUnit);
                linkageMinuteUnit(true);
                break;

            case R.id.dpv_minute:
                mSelectedTime.set(Calendar.MINUTE, timeUnit);
                break;
        }
        mCallback.onTimeSelected(mSelectedTime.getTimeInMillis());
    }

    /**
     * 联动“月”变化
     *
     * @param showAnim 是否展示滚动动画
     * @param delay    联动下一级延迟时间
     */
    private void linkageMonthUnit(final boolean showAnim, final long delay) {
        int minMonth;
        int maxMonth;
        int selectedYear = mSelectedTime.get(Calendar.YEAR);
        if (mBeginYear == mEndYear) {
            minMonth = mBeginMonth;
            maxMonth = mEndMonth;
        } else if (selectedYear == mBeginYear) {
            minMonth = mBeginMonth;
            maxMonth = MAX_MONTH_UNIT;
        } else if (selectedYear == mEndYear) {
            minMonth = 1;
            maxMonth = mEndMonth;
        } else {
            minMonth = 1;
            maxMonth = MAX_MONTH_UNIT;
        }

        // 重新初始化时间单元容器
        mMonthUnits.clear();
        for (int i = minMonth; i <= maxMonth; i++) {
            mMonthUnits.add(mDecimalFormat.format(i));
        }
        mDpvMonth.setDataList(mMonthUnits);

        // 确保联动时不会溢出或改变关联选中值
        int selectedMonth = getValueInRange(mSelectedTime.get(Calendar.MONTH) + 1, minMonth, maxMonth);
        mSelectedTime.set(Calendar.MONTH, selectedMonth - 1);
        mDpvMonth.setSelected(selectedMonth - minMonth);
        if (showAnim) {
            mDpvMonth.startAnim();
        }

        // 联动“日”变化
        mDpvMonth.postDelayed(new Runnable() {
            @Override
            public void run() {
                linkageDayUnit(showAnim, delay);
            }
        }, delay);
    }

    /**
     * 联动“日”变化
     *
     * @param showAnim 是否展示滚动动画
     * @param delay    联动下一级延迟时间
     */
    private void linkageDayUnit(final boolean showAnim, final long delay) {
        int minDay;
        int maxDay;
        int selectedYear = mSelectedTime.get(Calendar.YEAR);
        int selectedMonth = mSelectedTime.get(Calendar.MONTH) + 1;
        if (mBeginYear == mEndYear && mBeginMonth == mEndMonth) {
            minDay = mBeginDay;
            maxDay = mEndDay;
        } else if (selectedYear == mBeginYear && selectedMonth == mBeginMonth) {
            minDay = mBeginDay;
            maxDay = mSelectedTime.getActualMaximum(Calendar.DAY_OF_MONTH);
        } else if (selectedYear == mEndYear && selectedMonth == mEndMonth) {
            minDay = 1;
            maxDay = mEndDay;
        } else {
            minDay = 1;
            maxDay = mSelectedTime.getActualMaximum(Calendar.DAY_OF_MONTH);
        }

        mDayUnits.clear();
        for (int i = minDay; i <= maxDay; i++) {
            mDayUnits.add(mDecimalFormat.format(i));
        }
        mDpvDay.setDataList(mDayUnits);

        int selectedDay = getValueInRange(mSelectedTime.get(Calendar.DAY_OF_MONTH), minDay, maxDay);
        mSelectedTime.set(Calendar.DAY_OF_MONTH, selectedDay);
        mDpvDay.setSelected(selectedDay - minDay);
        if (showAnim) {
            mDpvDay.startAnim();
        }

        mDpvDay.postDelayed(new Runnable() {
            @Override
            public void run() {
                linkageHourUnit(showAnim, delay);
            }
        }, delay);
    }

    /**
     * 联动“时”变化
     *
     * @param showAnim 是否展示滚动动画
     * @param delay    联动下一级延迟时间
     */
    private void linkageHourUnit(final boolean showAnim, final long delay) {
        if ((mScrollUnits & SCROLL_UNIT_HOUR) == SCROLL_UNIT_HOUR) {
            int minHour;
            int maxHour;
            int selectedYear = mSelectedTime.get(Calendar.YEAR);
            int selectedMonth = mSelectedTime.get(Calendar.MONTH) + 1;
            int selectedDay = mSelectedTime.get(Calendar.DAY_OF_MONTH);
            if (mBeginYear == mEndYear && mBeginMonth == mEndMonth && mBeginDay == mEndDay) {
                minHour = mBeginHour;
                maxHour = mEndHour;
            } else if (selectedYear == mBeginYear && selectedMonth == mBeginMonth && selectedDay == mBeginDay) {
                minHour = mBeginHour;
                maxHour = MAX_HOUR_UNIT;
            } else if (selectedYear == mEndYear && selectedMonth == mEndMonth && selectedDay == mEndDay) {
                minHour = 0;
                maxHour = mEndHour;
            } else {
                minHour = 0;
                maxHour = MAX_HOUR_UNIT;
            }

            mHourUnits.clear();
            for (int i = minHour; i <= maxHour; i++) {
                mHourUnits.add(mDecimalFormat.format(i));
            }
            mDpvHour.setDataList(mHourUnits);

            int selectedHour = getValueInRange(mSelectedTime.get(Calendar.HOUR_OF_DAY), minHour, maxHour);
            mSelectedTime.set(Calendar.HOUR_OF_DAY, selectedHour);
            mDpvHour.setSelected(selectedHour - minHour);
            if (showAnim) {
                mDpvHour.startAnim();
            }
        }

        mDpvHour.postDelayed(new Runnable() {
            @Override
            public void run() {
                linkageMinuteUnit(showAnim);
            }
        }, delay);
    }

    /**
     * 联动“分”变化
     *
     * @param showAnim 是否展示滚动动画
     */
    private void linkageMinuteUnit(final boolean showAnim) {
        if ((mScrollUnits & SCROLL_UNIT_MINUTE) == SCROLL_UNIT_MINUTE) {
            int minMinute;
            int maxMinute;
            int selectedYear = mSelectedTime.get(Calendar.YEAR);
            int selectedMonth = mSelectedTime.get(Calendar.MONTH) + 1;
            int selectedDay = mSelectedTime.get(Calendar.DAY_OF_MONTH);
            int selectedHour = mSelectedTime.get(Calendar.HOUR_OF_DAY);
            if (mBeginYear == mEndYear && mBeginMonth == mEndMonth && mBeginDay == mEndDay && mBeginHour == mEndHour) {
                minMinute = mBeginMinute;
                maxMinute = mEndMinute;
            } else if (selectedYear == mBeginYear && selectedMonth == mBeginMonth && selectedDay == mBeginDay && selectedHour == mBeginHour) {
                minMinute = mBeginMinute;
                maxMinute = MAX_MINUTE_UNIT;
            } else if (selectedYear == mEndYear && selectedMonth == mEndMonth && selectedDay == mEndDay && selectedHour == mEndHour) {
                minMinute = 0;
                maxMinute = mEndMinute;
            } else {
                minMinute = 0;
                maxMinute = MAX_MINUTE_UNIT;
            }

            mMinuteUnits.clear();
            for (int i = minMinute; i <= maxMinute; i++) {
                mMinuteUnits.add(mDecimalFormat.format(i));
            }
            mDpvMinute.setDataList(mMinuteUnits);

            int selectedMinute = getValueInRange(mSelectedTime.get(Calendar.MINUTE), minMinute, maxMinute);
            mSelectedTime.set(Calendar.MINUTE, selectedMinute);
            mDpvMinute.setSelected(selectedMinute - minMinute);
            if (showAnim) {
                mDpvMinute.startAnim();
            }
        }

        setCanScroll();
    }

    private int getValueInRange(int value, int minValue, int maxValue) {
        if (value < minValue) {
            return minValue;
        } else if (value > maxValue) {
            return maxValue;
        } else {
            return value;
        }
    }

    /**
     * 设置日期控件是否显示时和分
     */
    public void setCanShowPreciseTime(boolean canShowPreciseTime) {

        if (canShowPreciseTime) {
            initScrollUnit();
            mDpvHour.setVisibility(View.VISIBLE);
            mTvHourUnit.setVisibility(View.VISIBLE);
            mDpvMinute.setVisibility(View.VISIBLE);
            mTvMinuteUnit.setVisibility(View.VISIBLE);
        } else {
            initScrollUnit(SCROLL_UNIT_HOUR, SCROLL_UNIT_MINUTE);
            mDpvHour.setVisibility(View.GONE);
            mTvHourUnit.setVisibility(View.GONE);
            mDpvMinute.setVisibility(View.GONE);
            mTvMinuteUnit.setVisibility(View.GONE);
        }
        mCanShowPreciseTime = canShowPreciseTime;
    }

    private void initScrollUnit(Integer... units) {
        if (units == null || units.length == 0) {
            mScrollUnits = SCROLL_UNIT_HOUR + SCROLL_UNIT_MINUTE;
        } else {
            for (int unit : units) {
                mScrollUnits ^= unit;
            }
        }
    }

    /**
     * 设置日期控件是否可以循环滚动
     */
    public void setScrollLoop(boolean canLoop) {
        mDpvYear.setCanScrollLoop(canLoop);
        mDpvMonth.setCanScrollLoop(canLoop);
        mDpvDay.setCanScrollLoop(canLoop);
        mDpvHour.setCanScrollLoop(canLoop);
        mDpvMinute.setCanScrollLoop(canLoop);
    }

    /**
     * 设置日期控件是否展示滚动动画
     */
    public void setCanShowAnim(boolean canShowAnim) {
        mDpvYear.setCanShowAnim(canShowAnim);
        mDpvMonth.setCanShowAnim(canShowAnim);
        mDpvDay.setCanShowAnim(canShowAnim);
        mDpvHour.setCanShowAnim(canShowAnim);
        mDpvMinute.setCanShowAnim(canShowAnim);
    }



}
