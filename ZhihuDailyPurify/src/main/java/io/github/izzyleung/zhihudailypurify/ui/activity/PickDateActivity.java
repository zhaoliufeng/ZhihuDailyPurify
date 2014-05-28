package io.github.izzyleung.zhihudailypurify.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.Toast;
import com.squareup.timessquare.CalendarPickerView;
import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.support.util.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PickDateActivity extends ActionBarActivity {
    private Calendar pickedDate = Calendar.getInstance();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_date);

        try {
            pickedDate.setTime(DateUtils.simpleDateFormat.parse(getIntent().getStringExtra("date")));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        CalendarPickerView calendar = (CalendarPickerView) findViewById(R.id.calendar_view);
        Calendar nextDay = Calendar.getInstance();
        nextDay.add(Calendar.DAY_OF_YEAR, 1);
        calendar.init(DateUtils.birthDay.getTime(), nextDay.getTime()).withSelectedDate(pickedDate.getTime());
        calendar.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                pickedDate.setTime(date);
                goToDate();
            }

            @Override
            public void onDateUnselected(Date date) {

            }
        });

        calendar.setOnInvalidDateSelectedListener(new CalendarPickerView.OnInvalidDateSelectedListener() {
            @Override
            public void onInvalidDateSelected(Date date) {
                invalidDate(date);
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    private void goToDate() {
        pickedDate.add(Calendar.DAY_OF_YEAR, 1);

        String date = DateUtils.simpleDateFormat.
                format(pickedDate.getTime());

        //Recover time
        pickedDate.add(Calendar.DAY_OF_YEAR, -1);

        String displayDate = new SimpleDateFormat(getString(R.string.display_format)).
                format(pickedDate.getTime());

        Intent intent = new Intent();
        intent.setClass(PickDateActivity.this, PortalActivity.class);
        intent.putExtra("date", date);
        intent.putExtra("display_date", displayDate);
        startActivity(intent);
        this.finish();
    }

    private void invalidDate(Date date) {
        if (date.after(new Date())) {
            Toast.makeText(PickDateActivity.this, getString(R.string.not_coming), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(PickDateActivity.this, getString(R.string.not_born), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
           default:
                return super.onOptionsItemSelected(item);
        }
    }
}