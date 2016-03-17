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

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

public class DayPickerView extends ListView implements AbsListView.OnScrollListener, DatePickerDialog.OnDateChangedListener {
	private static final int LIST_TOP_OFFSET = -1;
	private static final int GOTO_SCROLL_DURATION = 250;
    private static final int SCROLL_CHANGE_DELAY = 40;
	
	private final DatePickerController mController;

    private SimpleMonthAdapter mAdapter;

	private float mFriction = 1.0F;

	private CalendarDay mSelectedDay = new CalendarDay();
	private CalendarDay mTempDay = new CalendarDay();

	@SuppressWarnings("unused")
	private int mCurrentMonthDisplayed;
    
	private ScrollStateRunnable mScrollStateChangedRunnable = new ScrollStateRunnable();
	private Handler mHandler = new Handler();
	
    protected int mCurrentScrollState = 0;
	protected int mPreviousScrollState = 0;

	public DayPickerView(Context context, DatePickerController datePickerController) {
		super(context);

		mController = datePickerController;
		mController.registerOnDateChangedListener(this);

		setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		init();

		onDateChanged();
	}

	private void init() {
		setUpListView();
		setUpAdapter();
	}

	private void setUpListView() {
		setVerticalScrollBarEnabled(false);
		setFadingEdgeLength(0);
		setCacheColorHint(0);
		setDividerHeight(0);
		setSelector(new StateListDrawable());
		setFrictionIfSupported(ViewConfiguration.getScrollFriction() * mFriction);
		setItemsCanFocus(true);
		setOnScrollListener(this);
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setFrictionIfSupported(float friction) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			setFriction(friction);
		}
	}
	
	private void setUpAdapter() {
		if (mAdapter == null) {
			mAdapter = new SimpleMonthAdapter(getContext(), mController);
		}
		mAdapter.setSelectedDay(mSelectedDay);
		setAdapter(mAdapter);
	}

	@Override
	public void onDateChanged() {
		goTo(mController.getSelectedDay(), false, true, true);
	}

	public boolean goTo(CalendarDay day, boolean animate, boolean setSelected, boolean forceScroll) {
		// Set the selected day
		if (setSelected) {
			mSelectedDay.set(day);
		}

        mTempDay.set(day);
		final int position = (day.year - mController.getMinYear()) * SimpleMonthAdapter.MONTHS_IN_YEAR + day.month;

        View child;
        int i = 0;
        int top = 0;
        // Find a child that's completely in the view
		do {
			child = getChildAt(i++);
			if (child == null) {
				break;
			}
			top = child.getTop();
		} while (top < 0);

        // Compute the first and last position visible
        int selectedPosition;
        if (child != null) {
            selectedPosition = getPositionForView(child);
        } else {
        	// top 等于 0
            selectedPosition = 0;
        }

        if (setSelected) {
            mAdapter.setSelectedDay(mSelectedDay);
        }

        // Check if the selected day is now outside of our visible range
        // and if so scroll to the month that contains it
        // 强制滚动
        if (position != selectedPosition || forceScroll) {
			setMonthDisplayed(mTempDay);
			mPreviousScrollState = OnScrollListener.SCROLL_STATE_FLING;
			if (animate && Build.VERSION.SDK_INT >= 11) {
				smoothScrollToPositionFromTop(position, LIST_TOP_OFFSET, GOTO_SCROLL_DURATION);
				return true;
			} else {
				postSetSelection(position);
			}
		} else if (setSelected) {
			setMonthDisplayed(mSelectedDay);
		}
        return false;
    }
    
	private void setMonthDisplayed(CalendarDay calendarDay) {
		this.mCurrentMonthDisplayed = calendarDay.month;
		invalidateViews();
	}

	public void postSetSelection(final int position) {
		clearFocus();
		post(new Runnable() {
			@Override
			public void run() {
				setSelection(position);
			}
		});
		onScrollStateChanged(this, OnScrollListener.SCROLL_STATE_IDLE);
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// use a post to prevent re-entering onScrollStateChanged before it exits
		mScrollStateChangedRunnable.doScrollStateChange(view, scrollState);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		mPreviousScrollState = mCurrentScrollState;
	}

	private class ScrollStateRunnable implements Runnable {
		private int mNewState;

        public void doScrollStateChange(AbsListView view, int scrollState) {
            mHandler.removeCallbacks(this);
            mNewState = scrollState;
            mHandler.postDelayed(this, SCROLL_CHANGE_DELAY);
        }

        @Override
        public void run() {
            mCurrentScrollState = mNewState;
            
            // Fix the position after a scroll or a fling ends
            if (mNewState == OnScrollListener.SCROLL_STATE_IDLE
                    && mPreviousScrollState != OnScrollListener.SCROLL_STATE_IDLE
                    && mPreviousScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                mPreviousScrollState = mNewState;
                
				int i = 0;
				View child = getChildAt(i);
				while (child != null && child.getBottom() <= 0) {
					child = getChildAt(++i);
				}
				// 获取显示的第一个child
                
                if (child == null) {
                    // The view is no longer visible, just return
                    return;
                }
                
                int firstPosition = getFirstVisiblePosition();
                int lastPosition = getLastVisiblePosition();
                boolean scroll = firstPosition != 0 && lastPosition != getCount() - 1;
                final int top = child.getTop();
                final int bottom = child.getBottom();
                final int midpoint = getHeight() / 2;
                
                if (scroll && top < LIST_TOP_OFFSET) {
                    if (bottom > midpoint) {
                        smoothScrollBy(top, GOTO_SCROLL_DURATION);
                    } else {
                        smoothScrollBy(bottom, GOTO_SCROLL_DURATION);
                    }
                }
            } else {
                mPreviousScrollState = mNewState;
            }
        }
    }

	public void onChanged() {
		setUpAdapter();
		mAdapter.notifyDataSetChanged();
	}

	public int getMostVisiblePosition() {
		final int firstPosition = getFirstVisiblePosition();
		final int height = getHeight();

		int maxDisplayedHeight = 0;
		int mostVisibleIndex = 0;
		int i = 0;
		int bottom = 0;
		while (bottom < height) {
			View child = getChildAt(i);
			if (child == null) {
				break;
			}
			bottom = child.getBottom();
			int displayedHeight = Math.min(bottom, height) - Math.max(0, child.getTop());
			if (displayedHeight > maxDisplayedHeight) {
				mostVisibleIndex = i;
				maxDisplayedHeight = displayedHeight;
			}
			i++;
		}
		return firstPosition + mostVisibleIndex;
	}
}