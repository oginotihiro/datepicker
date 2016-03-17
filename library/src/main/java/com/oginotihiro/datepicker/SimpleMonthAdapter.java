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

import java.util.HashMap;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;

public class SimpleMonthAdapter extends BaseAdapter implements SimpleMonthView.OnDayClickListener {
	public static final int MONTHS_IN_YEAR = 12;

	private final Context mContext;
	private final DatePickerController mController;

	private CalendarDay mSelectedDay;

	public SimpleMonthAdapter(Context context, DatePickerController datePickerController) {
		mContext = context;
		mController = datePickerController;

		init();

		setSelectedDay(mController.getSelectedDay());
	}
	
	private void init() {
		mSelectedDay = new CalendarDay(System.currentTimeMillis());
	}
	
	public void setSelectedDay(CalendarDay calendarDay) {
		mSelectedDay = calendarDay;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return ((mController.getMaxYear() - mController.getMinYear()) + 1) * MONTHS_IN_YEAR;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SimpleMonthView v;
		HashMap<String, Integer> drawingParams = null;

		if (convertView != null) {
			v = (SimpleMonthView) convertView;
			drawingParams = (HashMap<String, Integer>) v.getTag();
		} else {
			v = new SimpleMonthView(mContext, mController.getColor());
			v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			v.setClickable(true);
			v.setOnDayClickListener(this);
		}
		if (drawingParams == null) {
			drawingParams = new HashMap<String, Integer>();
		}
		drawingParams.clear();
		
		final int month = position % MONTHS_IN_YEAR;
		final int year = position / MONTHS_IN_YEAR + mController.getMinYear();

		int selectedDay = -1;
		if (isSelectedDayInMonth(year, month)) {
			selectedDay = mSelectedDay.day;
		}

		drawingParams.put(SimpleMonthView.VIEW_PARAMS_YEAR, year);
		drawingParams.put(SimpleMonthView.VIEW_PARAMS_MONTH, month);
		drawingParams.put(SimpleMonthView.VIEW_PARAMS_SELECTED_DAY, selectedDay);
		drawingParams.put(SimpleMonthView.VIEW_PARAMS_WEEK_START, mController.getFirstDayOfWeek());
		v.setMonthParams(drawingParams);
		v.invalidate();
		
		return v;
	}

	private boolean isSelectedDayInMonth(int year, int month) {
		return (mSelectedDay.year == year) && (mSelectedDay.month == month);
	}

	@Override
	public void onDayClick(SimpleMonthView simpleMonthView, CalendarDay calendarDay) {
		if (calendarDay != null) {
			onDayTapped(calendarDay);
		}
	}

	private void onDayTapped(CalendarDay calendarDay) {
		mController.tryVibrate();
		mController.onDayOfMonthSelected(calendarDay.year, calendarDay.month, calendarDay.day);
		setSelectedDay(calendarDay);
	}
}