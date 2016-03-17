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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class YearPickerView extends ListView implements AdapterView.OnItemClickListener, DatePickerDialog.OnDateChangedListener {
	private Context mContext;
	private DatePickerController mController;

	private int mViewSize;
	private int mChildSize;
	
	private YearAdapter mAdapter;
	
	private TextViewWithCircularIndicator mSelectedView;

	public YearPickerView(Context context, DatePickerController datePickerController) {
		super(context);

		mContext = context;
		mController = datePickerController;
		mController.registerOnDateChangedListener(this); // 刷新页面

		setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	
		Resources resources = context.getResources();
		mViewSize = resources.getDimensionPixelSize(R.dimen.date_picker_view_animator_height); // 270dp
		mChildSize = resources.getDimensionPixelSize(R.dimen.year_label_height); // 64dp
		
		init();
		
		onDateChanged();
	}

	private void init() {
		setUpListView();
		setUpAdapter();
	}
	
	private void setUpListView() {
		setVerticalFadingEdgeEnabled(true); // 列表阴影
		setFadingEdgeLength(mChildSize / 3);
		setCacheColorHint(0);
		setDividerHeight(0);
		setSelector(new StateListDrawable());
		setOnItemClickListener(this);
	}
	
	private void setUpAdapter() {
		ArrayList<String> years = new ArrayList<String>();
		for (int year = mController.getMinYear(); year <= mController.getMaxYear(); year++) {
			years.add(String.format("%d", year));
		}
		mAdapter = new YearAdapter(mContext, R.layout.oginotihiro_year_label_text_view, years);
		setAdapter(mAdapter);
	}

	@Override
	public void onDateChanged() {
		mAdapter.notifyDataSetChanged();
		postSetSelectionCentered(mController.getSelectedDay().year - mController.getMinYear());
	}
	
	// item垂直方向居中
	public void postSetSelectionCentered(final int position) {
		postSetSelectionFromTop(position, mViewSize / 2 - mChildSize / 2);
	}
	
	public void postSetSelectionFromTop(final int position, final int y) {
		post(new Runnable() {
			@Override
			public void run() {
				setSelectionFromTop(position, y);
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		mController.tryVibrate(); // 震动

		TextViewWithCircularIndicator clickedView = (TextViewWithCircularIndicator) view;
		if (clickedView != null) {
			if (clickedView != mSelectedView) {
				mSelectedView = clickedView;
				mAdapter.notifyDataSetChanged();
			}
			mController.onYearSelected(getYearFromTextView(clickedView));
		}
	}
	
	private static int getYearFromTextView(TextView view) {
		return Integer.valueOf(view.getText().toString());
	}

	private class YearAdapter extends ArrayAdapter<String> {
		public YearAdapter(Context context, int resource, List<String> years) {
			super(context, resource, years);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextViewWithCircularIndicator v = (TextViewWithCircularIndicator) super.getView(position, convertView, parent);

			v.setColor(mController.getColor(), mController.getDarkColor());

			int year = getYearFromTextView(v);
			boolean selected = mController.getSelectedDay().year == year;
			v.drawIndicator(selected);

			if (selected) {
				mSelectedView = v;
			}
			return v;
		}
	}

	public int getFirstPositionOffset() {
		final View firstChild = getChildAt(0);
		if (firstChild == null) {
			return 0;
		}
		return firstChild.getTop();
	}
}