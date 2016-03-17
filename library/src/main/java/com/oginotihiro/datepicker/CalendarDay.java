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

import java.util.Calendar;

public class CalendarDay {
	protected int year;
	protected int month;
	protected int day;

	public CalendarDay() {
		setTime(System.currentTimeMillis());
	}

	public CalendarDay(long timeInMillis) {
		setTime(timeInMillis);
	}

	private void setTime(long timeInMillis) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeInMillis);

		setDay(calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH));
	}

	public CalendarDay(int year, int month, int day) {
		setDay(year, month, day);
	}

	public CalendarDay(Calendar calendar) {
		setDay(calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH));
	}

	public void set(CalendarDay calendarDay) {
		setDay(calendarDay.year, calendarDay.month, calendarDay.day);
	}

	public void setDay(int year, int month, int day) {
		this.year = year;
		this.month = month;
		this.day = day;
	}
}