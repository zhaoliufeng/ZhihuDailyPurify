package io.github.izzyleung.zhihudailypurify.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.squareup.timessquare.CalendarPickerView;

import java.util.Calendar;
import java.util.Date;

import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.support.Constants;

public class PickDateActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        layoutResID = R.layout.activity_pick_date;

        super.onCreate(savedInstanceState);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Calendar nextDay = Calendar.getInstance();
        nextDay.add(Calendar.DAY_OF_YEAR, 1);

        CalendarPickerView calendarPickerView = (CalendarPickerView) findViewById(R.id.calendar_view);
        calendarPickerView.init(Constants.Date.birthday, nextDay.getTime())
                .withSelectedDate(Calendar.getInstance().getTime());
        calendarPickerView.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.DAY_OF_YEAR, 1);

                Intent intent = new Intent(PickDateActivity.this, SingleDayNewsActivity.class);
                intent.putExtra("date", Constants.Date.simpleDateFormat.format(calendar.getTime()));
                startActivity(intent);
            }

            @Override
            public void onDateUnselected(Date date) {

            }
        });
        calendarPickerView.setOnInvalidDateSelectedListener(new CalendarPickerView.OnInvalidDateSelectedListener() {
            @Override
            public void onInvalidDateSelected(Date date) {
                if (date.after(new Date())) {
                    showSnackbar(R.string.not_coming);
                } else {
                    showSnackbar(R.string.not_born);
                }
            }
        });
    }
}
