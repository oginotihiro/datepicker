package com.oginotihiro.datepicker.sample;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.oginotihiro.datepicker.DatePickerDialog;

import java.util.Calendar;

public class MainActivity extends FragmentActivity implements DatePickerDialog.OnDateSetListener, View.OnClickListener {
    public static final String DATEPICKER_TAG = "datepicker";

    private TextView colorTv;

    final Calendar calendar = Calendar.getInstance();
    final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this,
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        colorTv = (TextView) findViewById(R.id.colorTv);

        findViewById(R.id.dateButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.setVibrate(isVibrate());
                datePickerDialog.setYearRange(1980, 2020);
                datePickerDialog.setCloseOnSingleTapDay(isCloseOnSingleTapDay());
                datePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
            }
        });

        findViewById(R.id.redBtn).setOnClickListener(this);
        findViewById(R.id.deepPurpleBtn).setOnClickListener(this);
        findViewById(R.id.blueBtn).setOnClickListener(this);
        findViewById(R.id.greenBtn).setOnClickListener(this);
        findViewById(R.id.deepOrangeBtn).setOnClickListener(this);
        findViewById(R.id.blueGreyBtn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.redBtn) {
            datePickerDialog.setColor(getResources().getColor(R.color.red));
            datePickerDialog.setDarkColor(getResources().getColor(R.color.red_dark));
            changeColor("red", getResources().getColor(R.color.red));
        } else if (id == R.id.deepPurpleBtn) {
            datePickerDialog.setColor(getResources().getColor(R.color.deep_purple));
            datePickerDialog.setDarkColor(getResources().getColor(R.color.deep_purple_dark));
            changeColor("deep purple", getResources().getColor(R.color.deep_purple));
        } else if (id == R.id.blueBtn) {
            datePickerDialog.setColor(getResources().getColor(R.color.blue));
            datePickerDialog.setDarkColor(getResources().getColor(R.color.blue_dark));
            changeColor("blue", getResources().getColor(R.color.blue));
        } else if (id == R.id.greenBtn) {
            datePickerDialog.setColor(getResources().getColor(R.color.green));
            datePickerDialog.setDarkColor(getResources().getColor(R.color.green_dark));
            changeColor("green", getResources().getColor(R.color.green));
        } else if (id == R.id.deepOrangeBtn) {
            datePickerDialog.setColor(getResources().getColor(R.color.deep_orange));
            datePickerDialog.setDarkColor(getResources().getColor(R.color.deep_orange_dark));
            changeColor("deep orange", getResources().getColor(R.color.deep_orange));
        } else if (id == R.id.blueGreyBtn) {
            datePickerDialog.setColor(getResources().getColor(R.color.blue_grey));
            datePickerDialog.setDarkColor(getResources().getColor(R.color.blue_grey_dark));
            changeColor("blue grey", getResources().getColor(R.color.blue_grey));
        }
    }

    private void changeColor(String colorStr, int color) {
        Toast.makeText(MainActivity.this, colorStr, Toast.LENGTH_SHORT).show();
        colorTv.setTextColor(color);
        colorTv.setText(colorStr);
    }

    private boolean isVibrate() {
        return ((CheckBox) findViewById(R.id.vibrateCheckBox)).isChecked();
    }

    private boolean isCloseOnSingleTapDay() {
        return ((CheckBox) findViewById(R.id.closeOnSingleTapDayCheckBox)).isChecked();
    }

    @Override
    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
        Toast.makeText(MainActivity.this, "new date:" + year + "-" + monthOfYear + "-" + dayOfMonth, Toast.LENGTH_SHORT).show();
    }
}