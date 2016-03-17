/*
 * Copyright (C) 2016 oginotihiro
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.oginotihiro.datepicker;

import java.security.InvalidParameterException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.MotionEvent;
import android.view.View;

@SuppressWarnings("deprecation")
public class SimpleMonthView extends View {
	public static final String VIEW_PARAMS_HEIGHT = "height";
	public static final String VIEW_PARAMS_YEAR = "year";
	public static final String VIEW_PARAMS_MONTH = "month";
	public static final String VIEW_PARAMS_WEEK_START = "week_start";
	public static final String VIEW_PARAMS_SELECTED_DAY = "selected_day";
    
	private static int MIN_HEIGHT = 10;
	private static int DEFAULT_HEIGHT = 32;
	private static final int SELECTED_CIRCLE_ALPHA = 60;
    private static final int DEFAULT_NUM_ROWS = 6;
	private static int DAY_SEPARATOR_WIDTH = 1;
	
	private static int MONTH_HEADER_SIZE;
	private static int MONTH_LABEL_TEXT_SIZE;
	private static int MONTH_DAY_LABEL_TEXT_SIZE;
	private static int MINI_DAY_NUMBER_TEXT_SIZE;
	private static int DAY_SELECTED_CIRCLE_SIZE;
    
	private final Calendar mCalendar;
	private final Calendar mDayLabelCalendar;
	private DateFormatSymbols mDateFormatSymbols = new DateFormatSymbols();
	
    private String mDayOfWeekTypeface;
    private String mMonthTitleTypeface;
    
    private int mDayTextColor;
    private int mTodayNumberColor;
    
    private int mRowHeight = DEFAULT_HEIGHT;
    private int mWidth;
    private int mPadding = 0;
    
    private Paint mMonthTitlePaint;
    private Paint mMonthDayLabelPaint;
    private Paint mMonthNumPaint;
    private Paint mSelectedCirclePaint;
    
    private int mYear;
    private int mMonth;
	private int mSelectedDay = -1;
    
	private int mWeekStart = 1;
	private int mDayOfWeekStart = 0;
	
    private int mNumDays = 7;
    private int mNumCells = mNumDays;
    private int mNumRows = DEFAULT_NUM_ROWS;
	
	private boolean mHasToday;
    private int mToday = -1;
    
    private OnDayClickListener mOnDayClickListener; 
    
	public SimpleMonthView(Context context, int color) {
 		super(context);
 		
		mCalendar = Calendar.getInstance();
		mDayLabelCalendar = Calendar.getInstance();
		
		Resources resources = context.getResources();
		
		MONTH_HEADER_SIZE = resources.getDimensionPixelOffset(R.dimen.month_list_item_header_height);
		MONTH_LABEL_TEXT_SIZE = resources.getDimensionPixelSize(R.dimen.month_label_size);
		MONTH_DAY_LABEL_TEXT_SIZE = resources.getDimensionPixelSize(R.dimen.month_day_label_text_size);
		MINI_DAY_NUMBER_TEXT_SIZE = resources.getDimensionPixelSize(R.dimen.day_number_size);
		DAY_SELECTED_CIRCLE_SIZE = resources.getDimensionPixelSize(R.dimen.day_number_select_circle_radius);
		
		mMonthTitleTypeface = resources.getString(R.string.sans_serif); // sans-serif 标题
		mDayOfWeekTypeface = resources.getString(R.string.day_of_week_label_typeface); // sans-serif 日-六

		mDayTextColor = resources.getColor(R.color.date_picker_text_normal);
		mTodayNumberColor = color;
		
		// 270dp - 50dp
		mRowHeight = ((resources.getDimensionPixelOffset(R.dimen.date_picker_view_animator_height) - MONTH_HEADER_SIZE) / DEFAULT_NUM_ROWS);
	
		init();
 	}
	
	private void init() {
		mMonthTitlePaint = new Paint();
		mMonthTitlePaint.setAntiAlias(true);
		mMonthTitlePaint.setFakeBoldText(true);
		mMonthTitlePaint.setTextSize(MONTH_LABEL_TEXT_SIZE);
		mMonthTitlePaint.setTypeface(Typeface.create(mMonthTitleTypeface, Typeface.BOLD));
		mMonthTitlePaint.setColor(mDayTextColor);
		mMonthTitlePaint.setStyle(Style.FILL);
		mMonthTitlePaint.setTextAlign(Align.CENTER);
	
		mMonthDayLabelPaint = new Paint();
		mMonthDayLabelPaint.setAntiAlias(true);
		mMonthDayLabelPaint.setFakeBoldText(true);
		mMonthDayLabelPaint.setTextSize(MONTH_DAY_LABEL_TEXT_SIZE);
		mMonthDayLabelPaint.setTypeface(Typeface.create(mDayOfWeekTypeface, Typeface.NORMAL));
		mMonthDayLabelPaint.setColor(mDayTextColor);
		mMonthDayLabelPaint.setStyle(Style.FILL);
		mMonthDayLabelPaint.setTextAlign(Align.CENTER);

		mMonthNumPaint = new Paint();
		mMonthNumPaint.setAntiAlias(true);
		mMonthNumPaint.setFakeBoldText(false);
		mMonthNumPaint.setTextSize(MINI_DAY_NUMBER_TEXT_SIZE);
		mMonthNumPaint.setStyle(Style.FILL);
		mMonthNumPaint.setTextAlign(Align.CENTER);

		mSelectedCirclePaint = new Paint();
		mSelectedCirclePaint.setAntiAlias(true);
		mSelectedCirclePaint.setFakeBoldText(true);
		mSelectedCirclePaint.setColor(mTodayNumberColor);
		mSelectedCirclePaint.setStyle(Style.FILL);
		mSelectedCirclePaint.setAlpha(SELECTED_CIRCLE_ALPHA);
	}
	
	public void setMonthParams(HashMap<String, Integer> params) {
		if (!params.containsKey(VIEW_PARAMS_MONTH) && !params.containsKey(VIEW_PARAMS_YEAR)) {
			throw new InvalidParameterException("You must specify month and year for this view");
		}
		setTag(params);

		if (params.containsKey(VIEW_PARAMS_HEIGHT)) {
			mRowHeight = params.get(VIEW_PARAMS_HEIGHT);
			if (mRowHeight < MIN_HEIGHT) {
				mRowHeight = MIN_HEIGHT;
			}
		}
		
		mYear = params.get(VIEW_PARAMS_YEAR);
		mMonth = params.get(VIEW_PARAMS_MONTH);
		
		if (params.containsKey(VIEW_PARAMS_SELECTED_DAY)) { // 选中的日期
			mSelectedDay = params.get(VIEW_PARAMS_SELECTED_DAY);
		}

		final Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		
		mHasToday = false;
		mToday = -1;

		mCalendar.set(Calendar.YEAR, mYear);
		mCalendar.set(Calendar.MONTH, mMonth);
		mCalendar.set(Calendar.DAY_OF_MONTH, 1);
		
		// 获取1号是星期几  从1-7对应 日-六
		mDayOfWeekStart = mCalendar.get(Calendar.DAY_OF_WEEK);

		// 获取每周的第一天是星期几 从1-7对应 日-六  现在返回的是1
		if (params.containsKey(VIEW_PARAMS_WEEK_START)) {
			mWeekStart = params.get(VIEW_PARAMS_WEEK_START);
		} else {
			mWeekStart = mCalendar.getFirstDayOfWeek();
		}

		mNumCells = Utils.getDaysInMonth(mYear, mMonth); // 这个月多少天
		for (int i = 0; i < mNumCells; i++) {
			final int day = i + 1;
			if (sameDay(day, today)) {
				mHasToday = true;
				mToday = day;
			}
		}

		mNumRows = calculateNumRows();
	}
	
	private boolean sameDay(int monthDay, Time time) {
		return (mYear == time.year) && (mMonth == time.month) && (monthDay == time.monthDay);
	}
	
	private int calculateNumRows() {
		int offset = findDayOffset();
		int dividend = (offset + mNumCells) / mNumDays;
		int remainder = (offset + mNumCells) % mNumDays;
		return (dividend + (remainder > 0 ? 1 : 0));
	}

	private int findDayOffset() {
		return (mDayOfWeekStart < mWeekStart ? (mDayOfWeekStart + mNumDays) : mDayOfWeekStart) - mWeekStart;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mRowHeight * mNumRows + MONTH_HEADER_SIZE);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mWidth = w;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		drawMonthTitle(canvas);
		drawMonthDayLabels(canvas);
		drawMonthNums(canvas);
	}
	
	/** 
	 * 2015年10月
	 */
	private void drawMonthTitle(Canvas canvas) {
		int x = (mWidth + 2 * mPadding) / 2;
        int y = (MONTH_HEADER_SIZE - MONTH_DAY_LABEL_TEXT_SIZE) / 2 + (MONTH_LABEL_TEXT_SIZE / 3);
        canvas.drawText(getMonthAndYearString(), x, y, mMonthTitlePaint);
	}
	
	private String getMonthAndYearString() {
		int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NO_MONTH_DAY;
		long millis = mCalendar.getTimeInMillis();
		return DateUtils.formatDateRange(getContext(), millis, millis, flags);
	}
	
	/**
	 * 星期日 星期一 ... 星球六
	 * 星期一 星期二 ... 星期日
	 */
	private void drawMonthDayLabels(Canvas canvas) {
		int y = MONTH_HEADER_SIZE - (MONTH_DAY_LABEL_TEXT_SIZE / 2);
		int dayWidthHalf = (mWidth - mPadding * 2) / (mNumDays * 2);

		for (int i = 0; i < mNumDays; i++) {
			int x = (2 * i + 1) * dayWidthHalf + mPadding;
			int calendarDay = (i + mWeekStart) % mNumDays;
			mDayLabelCalendar.set(Calendar.DAY_OF_WEEK, calendarDay);
			canvas.drawText(mDateFormatSymbols.getShortWeekdays()[mDayLabelCalendar
							.get(Calendar.DAY_OF_WEEK)].toUpperCase(Locale
							.getDefault()), x, y, mMonthDayLabelPaint);
		}
	}
	
	private void drawMonthNums(Canvas canvas) {
		int y = (mRowHeight + MINI_DAY_NUMBER_TEXT_SIZE) / 2 - DAY_SEPARATOR_WIDTH + MONTH_HEADER_SIZE;
		int paddingDay = (mWidth - 2 * mPadding) / (2 * mNumDays);
		int dayOffset = findDayOffset();
		int day = 1;

		while (day <= mNumCells) {
			int x = paddingDay * (1 + dayOffset * 2) + mPadding;

			if (mSelectedDay == day) {
				canvas.drawCircle(x, y - MINI_DAY_NUMBER_TEXT_SIZE / 3, DAY_SELECTED_CIRCLE_SIZE, mSelectedCirclePaint);
			}

			if (mHasToday && (mToday == day)) {
				mMonthNumPaint.setColor(mTodayNumberColor);
			} else {
				mMonthNumPaint.setColor(mDayTextColor);
			}

			canvas.drawText(String.format("%d", day), x, y, mMonthNumPaint);

			dayOffset++;
			if (dayOffset == mNumDays) {
				dayOffset = 0;
				y += mRowHeight;
			}
			day++;
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			CalendarDay calendarDay = getDayFromLocation(event.getX(), event.getY());
			if (calendarDay != null) {
				onDayClick(calendarDay);
			}
		}
		return true;
	}

	public CalendarDay getDayFromLocation(float x, float y) {
		int padding = mPadding;
		if ((x < padding) || (x > mWidth - mPadding)) {
			return null;
		}

		int yDay = (int) (y - MONTH_HEADER_SIZE) / mRowHeight;
		int day = 1 + ((int) ((x - padding) * mNumDays / (mWidth - padding - mPadding)) - findDayOffset()) + yDay * mNumDays;

		if (day < 1 || day > mNumCells) return null;
		
		return new CalendarDay(mYear, mMonth, day);
	}
	
	private void onDayClick(CalendarDay calendarDay) {
		if (mOnDayClickListener != null) {
			mOnDayClickListener.onDayClick(this, calendarDay);
		}
	}
	
	public void setOnDayClickListener(OnDayClickListener onDayClickListener) {
		mOnDayClickListener = onDayClickListener;
	}

	public interface OnDayClickListener {
		void onDayClick(SimpleMonthView simpleMonthView, CalendarDay calendarDay);
	}
}