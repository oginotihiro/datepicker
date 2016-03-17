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

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextViewWithCircularIndicator extends TextView {
    private Paint mCirclePaint = new Paint();
    private boolean mDrawCircle;

    public TextViewWithCircularIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);

        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStyle(Paint.Style.FILL);
    }

    public void setColor(int color, int darkColor) {
        mCirclePaint.setColor(color);
        mCirclePaint.setAlpha(60);

        int[][] states = new int[][]{
                new int[]{android.R.attr.state_pressed},
                new int[]{-android.R.attr.state_pressed, -android.R.attr.state_selected}
        };
        int[] colors = new int[]{
                darkColor, getResources().getColor(R.color.date_picker_text_normal)

        };
        ColorStateList colorStateList = new ColorStateList(states, colors);
        setTextColor(colorStateList);
    }

    public void drawIndicator(boolean drawIndicator) {
        mDrawCircle = drawIndicator;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawCircle) {
            int width = getWidth();
            int height = getHeight();
            int radius = Math.min(width, height) / 2;
            canvas.drawCircle(width / 2, height / 2, radius, mCirclePaint);
        }
    }
}