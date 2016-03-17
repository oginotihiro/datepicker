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

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

import com.nineoldandroids.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewAnimator;

public class DatePickerDialog extends DialogFragment implements View.OnClickListener, DatePickerController {
	private static final String KEY_SELECTED_YEAR = "year";
	private static final String KEY_SELECTED_MONTH = "month";
	private static final String KEY_SELECTED_DAY = "day";

	private static final String KEY_YEAR_START = "year_start";
	private static final String KEY_YEAR_END = "year_end";
	private static final String KEY_WEEK_START = "week_start";

	private static final String KEY_CURRENT_VIEW = "current_view";
	private static final String KEY_LIST_POSITION = "list_position";
	private static final String KEY_LIST_POSITION_OFFSET = "list_position_offset";

	private static final String KEY_VIBRATE = "vibrate";
	private static final String KEY_COLOR = "color";
	private static final String KEY_DARK_COLOR = "dark_color";

	private static final int DEFAULT_COLOR = 0xFFF44336;
	private static final int DEFAULT_DARK_COLOR = 0xFFB71C1C;

	private static final int MAX_YEAR = 2037;
	private static final int MIN_YEAR = 1902;

	private static final int UNINITIALIZED = -1;
	private static final int MONTH_AND_DAY_VIEW = 0;
	private static final int YEAR_VIEW = 1;

	public static final int ANIMATION_DELAY = 500;

	public interface OnDateSetListener {
		void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth);
	}

	public interface OnDateChangedListener {
		void onDateChanged();
	}

	private OnDateSetListener mCallBack;

	private HashSet<OnDateChangedListener> mListeners = new HashSet<OnDateChangedListener>();

	private final Calendar mCalendar = Calendar.getInstance();
	private Vibrator mVibrator;
	private DateFormatSymbols mDateFormatSymbols = new DateFormatSymbols();
	private static SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("dd", Locale.getDefault());
	private static SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy", Locale.getDefault());

	private boolean mVibrate = true;
    private long mLastVibrate;

    private int mColor;
    private int mDarkColor;

    private boolean mCloseOnSingleTapDay;

	private boolean mDelayAnimation = true;

    private int mMaxYear = MAX_YEAR;
    private int mMinYear = MIN_YEAR;
	private int mWeekStart = mCalendar.getFirstDayOfWeek();

	private int mCurrentView = UNINITIALIZED;

	private TextView mDayOfWeekView;
	private LinearLayout mMonthAndDayView;
	private TextView mSelectedMonthTv;
	private TextView mSelectedDayTv;
	private TextView mSelectedYearTv;
	private ViewAnimator mAnimator;
	private YearPickerView mYearPickerView;
	private DayPickerView mDayPickerView;
	private Button mDoneButton;

	public DatePickerDialog() {}

	public static DatePickerDialog newInstance(OnDateSetListener onDateSetListener, int year, int monthOfYear, int dayOfMonth) {
		return newInstance(onDateSetListener, year, monthOfYear, dayOfMonth, true, DEFAULT_COLOR, DEFAULT_DARK_COLOR);
	}

	public static DatePickerDialog newInstance(OnDateSetListener onDateSetListener, int year, int monthOfYear, int dayOfMonth, int color, int darkColor) {
		return newInstance(onDateSetListener, year, monthOfYear, dayOfMonth, true, color, darkColor);
	}

	public static DatePickerDialog newInstance(OnDateSetListener onDateSetListener, int year, int monthOfYear, int dayOfMonth, boolean vibrate) {
		return newInstance(onDateSetListener, year, monthOfYear, dayOfMonth, vibrate, DEFAULT_COLOR, DEFAULT_DARK_COLOR);
	}

	public static DatePickerDialog newInstance(OnDateSetListener onDateSetListener, int year, int monthOfYear, int dayOfMonth, boolean vibrate, int color, int darkColor) {
		DatePickerDialog datePickerDialog = new DatePickerDialog();
		datePickerDialog.initialize(onDateSetListener, year, monthOfYear, dayOfMonth, vibrate, color, darkColor);
		return datePickerDialog;
	}

	public void initialize(OnDateSetListener onDateSetListener, int year, int monthOfYear, int dayOfMonth, boolean vibrate, int color, int darkColor) {
		if (year > MAX_YEAR)
			throw new IllegalArgumentException("year end must < " + MAX_YEAR);
		if (year < MIN_YEAR)
			throw new IllegalArgumentException("year end must > " + MIN_YEAR);

		mCallBack = onDateSetListener;

		mCalendar.set(Calendar.YEAR, year);
		mCalendar.set(Calendar.MONTH, monthOfYear);
		mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

		mVibrate = vibrate;

		mColor = color;
		mDarkColor = darkColor;
	}

    public void setOnDateSetListener(OnDateSetListener onDateSetListener) {
        mCallBack = onDateSetListener;
    }

	public void setVibrate(boolean vibrate) {
		mVibrate = vibrate;
	}

    public void setColor(int color) {
        mColor = color;
    }

    public void setDarkColor(int darkColor) {
        mDarkColor = darkColor;
    }

	public void setCloseOnSingleTapDay(boolean closeOnSingleTapDay) {
		mCloseOnSingleTapDay = closeOnSingleTapDay;
	}

	public void setYearRange(int minYear, int maxYear) {
		if (maxYear < minYear)
			throw new IllegalArgumentException("Year end must be larger than year start");
		if (maxYear > MAX_YEAR)
			throw new IllegalArgumentException("max year end must < " + MAX_YEAR);
		if (minYear < MIN_YEAR)
			throw new IllegalArgumentException("min year end must > " + MIN_YEAR);
		mMinYear = minYear;
		mMaxYear = maxYear;
		if (mDayPickerView != null) {
			mDayPickerView.onChanged();
		}
	}

	public void setFirstDayOfWeek(int startOfWeek) {
		if (startOfWeek < Calendar.SUNDAY || startOfWeek > Calendar.SATURDAY) {
			throw new IllegalArgumentException("Value must be between Calendar.SUNDAY and " + "Calendar.SATURDAY");
		}
		mWeekStart = startOfWeek;
		if (mDayPickerView != null) {
			mDayPickerView.onChanged();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Activity activity = getActivity();
		activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		mVibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);

		if (savedInstanceState != null) {
			mCalendar.set(Calendar.YEAR, savedInstanceState.getInt(KEY_SELECTED_YEAR));
			mCalendar.set(Calendar.MONTH, savedInstanceState.getInt(KEY_SELECTED_MONTH));
			mCalendar.set(Calendar.DAY_OF_MONTH, savedInstanceState.getInt(KEY_SELECTED_DAY));

			mMinYear = savedInstanceState.getInt(KEY_YEAR_START);
			mMaxYear = savedInstanceState.getInt(KEY_YEAR_END);
			mWeekStart = savedInstanceState.getInt(KEY_WEEK_START);

			mVibrate = savedInstanceState.getBoolean(KEY_VIBRATE);
			mColor = savedInstanceState.getInt(KEY_COLOR);
			mDarkColor = savedInstanceState.getInt(KEY_DARK_COLOR);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		int currentView = MONTH_AND_DAY_VIEW;
		int listPosition = -1;
		int listPositionOffset = 0;

		if (savedInstanceState != null) {
			currentView = savedInstanceState.getInt(KEY_CURRENT_VIEW);
			listPosition = savedInstanceState.getInt(KEY_LIST_POSITION);
			listPositionOffset = savedInstanceState.getInt(KEY_LIST_POSITION_OFFSET);
		}

		View view = inflater.inflate(R.layout.oginotihiro_date_picker_dialog, container, false);

		mDayOfWeekView = (TextView) view.findViewById(R.id.datePickerHeader);

		mMonthAndDayView = (LinearLayout) view.findViewById(R.id.date_picker_month_and_day);
		mMonthAndDayView.setOnClickListener(this);

		mSelectedMonthTv = (TextView) view.findViewById(R.id.date_picker_month);
		mSelectedDayTv = (TextView) view.findViewById(R.id.date_picker_day);
		mSelectedYearTv = (TextView) view.findViewById(R.id.date_picker_year);
		mSelectedYearTv.setOnClickListener(this);

		int[][] states = new int[][]{
				new int[]{-android.R.attr.state_pressed, -android.R.attr.state_selected},
				new int[]{-android.R.attr.state_pressed, android.R.attr.state_selected},
				new int[]{android.R.attr.state_pressed}
		};
		int[] colors = new int[]{
				getResources().getColor(R.color.date_picker_text_normal),
				mColor, mDarkColor
		};
		ColorStateList csl = new ColorStateList(states, colors);

		mSelectedMonthTv.setTextColor(csl);
		mSelectedDayTv.setTextColor(csl);
		mSelectedYearTv.setTextColor(csl);

		Activity activity = getActivity();
        mYearPickerView = new YearPickerView(activity, this);
        mDayPickerView = new DayPickerView(activity, this);

		mAnimator = (ViewAnimator) view.findViewById(R.id.animator);
		mAnimator.addView(mDayPickerView);
		mAnimator.addView(mYearPickerView);

		AlphaAnimation inAlphaAnimation = new AlphaAnimation(0.0F, 1.0F);
		inAlphaAnimation.setDuration(300);
		mAnimator.setInAnimation(inAlphaAnimation);

		AlphaAnimation outAlphaAnimation = new AlphaAnimation(1.0F, 0.0F);
		outAlphaAnimation.setDuration(300);
		mAnimator.setOutAnimation(outAlphaAnimation);

		mDoneButton = (Button) view.findViewById(R.id.done);

		StateListDrawable sld = new StateListDrawable();
		sld.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(Color.argb(60, Color.red(mColor), Color.green(mColor), Color.blue(mColor))));

		mDoneButton.setOnClickListener(this);

		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			mDoneButton.setBackgroundDrawable(sld);
		} else {
			mDoneButton.setBackground(sld);
		}

		updateDisplay();
		setCurrentView(currentView, true);

		if (listPosition != -1) {
			if (currentView == MONTH_AND_DAY_VIEW) {
				mDayPickerView.postSetSelection(listPosition);
			}
			if (currentView == YEAR_VIEW) {
				mYearPickerView.postSetSelectionFromTop(listPosition, listPositionOffset);
			}
		}
		return view;
	}

	private void updateDisplay() {
		if (this.mDayOfWeekView != null) {
			this.mCalendar.setFirstDayOfWeek(mWeekStart);
			this.mDayOfWeekView.setText(mDateFormatSymbols.getWeekdays()[this.mCalendar.get(Calendar.DAY_OF_WEEK)].toUpperCase(Locale.getDefault()));
		}

        this.mSelectedMonthTv.setText(mDateFormatSymbols.getMonths()[this.mCalendar.get(Calendar.MONTH)].toUpperCase(Locale.getDefault()));

		this.mSelectedDayTv.setText(DAY_FORMAT.format(mCalendar.getTime()));
		this.mSelectedYearTv.setText(YEAR_FORMAT.format(mCalendar.getTime()));
	}

	private void updatePickers() {
		Iterator<OnDateChangedListener> iterator = mListeners.iterator();
		while (iterator.hasNext()) {
			iterator.next().onDateChanged();
		}
	}

	private void setCurrentView(int currentView) {
		setCurrentView(currentView, false);
	}

	private void setCurrentView(int currentView, boolean forceRefresh) {
		switch (currentView) {
		case MONTH_AND_DAY_VIEW:
			ObjectAnimator monthDayAnim = Utils.getPulseAnimator(mMonthAndDayView, 0.9F, 1.05F);
			if (mDelayAnimation) {
				monthDayAnim.setStartDelay(ANIMATION_DELAY);
				mDelayAnimation = false;
			}
			mDayPickerView.onDateChanged();
			if (mCurrentView != currentView || forceRefresh) {
				mMonthAndDayView.setSelected(true);
				mSelectedYearTv.setSelected(false);
				mAnimator.setDisplayedChild(MONTH_AND_DAY_VIEW);
				mCurrentView = currentView;
			}
			monthDayAnim.start();
            break;
		case YEAR_VIEW:
			ObjectAnimator yearAnim = Utils.getPulseAnimator(mSelectedYearTv, 0.85F, 1.1F);
			if (mDelayAnimation) {
				yearAnim.setStartDelay(ANIMATION_DELAY);
				mDelayAnimation = false;
			}
			mYearPickerView.onDateChanged();
			if (mCurrentView != currentView  || forceRefresh) {
				mMonthAndDayView.setSelected(false);
				mSelectedYearTv.setSelected(true);
				mAnimator.setDisplayedChild(YEAR_VIEW);
				mCurrentView = currentView;
			}
			yearAnim.start();
            break;
		}
	}

	@Override
	public void onClick(View v) {
		tryVibrate();
		if (v.getId() == R.id.date_picker_year) {
			setCurrentView(YEAR_VIEW);
		} else if (v.getId() == R.id.date_picker_month_and_day) {
			setCurrentView(MONTH_AND_DAY_VIEW);
		} else if (v.getId() == R.id.done) {
			onDoneButtonClick();
		}
	}

	private void onDoneButtonClick() {
		if (mCallBack != null) {
			mCallBack.onDateSet(this, mCalendar.get(Calendar.YEAR),
					mCalendar.get(Calendar.MONTH),
					mCalendar.get(Calendar.DAY_OF_MONTH));
		}
		dismiss();
	}

	@Override
	public int getMaxYear() {
		return mMaxYear;
	}

	@Override
	public int getMinYear() {
		return mMinYear;
	}

	@Override
	public int getFirstDayOfWeek() {
		return mWeekStart;
	}

	@Override
	public CalendarDay getSelectedDay() {
		return new CalendarDay(mCalendar);
	}

	@Override
	public int getColor() {
		return mColor;
	}

	@Override
	public int getDarkColor() {
		return mDarkColor;
	}

	@Override
	public void tryVibrate() {
		if (mVibrator != null && mVibrate) {
			long timeInMillis = SystemClock.uptimeMillis();
			if (timeInMillis - mLastVibrate >= 125L) {
				mVibrator.vibrate(20L);
				mLastVibrate = timeInMillis;
			}
		}
	}

	@Override
	public void onDayOfMonthSelected(int year, int month, int day) {
		mCalendar.set(Calendar.YEAR, year);
		mCalendar.set(Calendar.MONTH, month);
		mCalendar.set(Calendar.DAY_OF_MONTH, day);

		updatePickers();
		updateDisplay();

		if (mCloseOnSingleTapDay) {
			onDoneButtonClick();
		}
	}

	@Override
	public void onYearSelected(int year) {
		mCalendar.set(Calendar.YEAR, year);

		adjustDayInMonthIfNeeded(mCalendar.get(Calendar.MONTH), year);

		updatePickers();
		setCurrentView(MONTH_AND_DAY_VIEW);
		updateDisplay();
	}

	private void adjustDayInMonthIfNeeded(int month, int year) {
		int day = mCalendar.get(Calendar.DAY_OF_MONTH);
		int daysInMonth = Utils.getDaysInMonth(year, month);
		if (day > daysInMonth) {
			mCalendar.set(Calendar.DAY_OF_MONTH, daysInMonth);
		}
	}

	@Override
	public void registerOnDateChangedListener(OnDateChangedListener onDateChangedListener) {
		mListeners.add(onDateChangedListener);
	}

	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		bundle.putInt(KEY_SELECTED_YEAR, mCalendar.get(Calendar.YEAR));
		bundle.putInt(KEY_SELECTED_MONTH, mCalendar.get(Calendar.MONTH));
		bundle.putInt(KEY_SELECTED_DAY, mCalendar.get(Calendar.DAY_OF_MONTH));
		bundle.putInt(KEY_YEAR_START, mMinYear);
		bundle.putInt(KEY_YEAR_END, mMaxYear);
		bundle.putInt(KEY_WEEK_START, mWeekStart);
		bundle.putInt(KEY_CURRENT_VIEW, mCurrentView);

		int listPosition = -1;
		if (mCurrentView == MONTH_AND_DAY_VIEW) {
			listPosition = mDayPickerView.getMostVisiblePosition();
        } if (mCurrentView == YEAR_VIEW) {
			listPosition = mYearPickerView.getFirstVisiblePosition();
			bundle.putInt(KEY_LIST_POSITION_OFFSET, mYearPickerView.getFirstPositionOffset());
		}
        bundle.putInt(KEY_LIST_POSITION, listPosition);
    	bundle.putBoolean(KEY_VIBRATE, mVibrate);
		bundle.putInt(KEY_COLOR, mColor);
		bundle.putInt(KEY_DARK_COLOR, mDarkColor);
	}
}