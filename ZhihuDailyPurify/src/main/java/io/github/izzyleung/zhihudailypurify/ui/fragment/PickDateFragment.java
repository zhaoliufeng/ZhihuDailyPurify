package io.github.izzyleung.zhihudailypurify.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.squareup.timessquare.CalendarPickerView;
import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.support.util.DateUtils;

import java.util.Calendar;
import java.util.Date;

public class PickDateFragment extends Fragment {
    public static String TAG = "PickDateFragment";

    private OnDateSelectedListener mOnDateSelectedListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.fragment_pick_date, null);
        assert view != null;
        CalendarPickerView calendarPickerView = (CalendarPickerView) view.findViewById(R.id.calendar_view);
        Calendar nextDay = Calendar.getInstance();
        nextDay.add(Calendar.DAY_OF_YEAR, 1);
        calendarPickerView.init(DateUtils.birthDay.getTime(), nextDay.getTime())
                .withSelectedDate(mOnDateSelectedListener.getDate());
        calendarPickerView.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                mOnDateSelectedListener.onValidDateSelected(date);
            }

            @Override
            public void onDateUnselected(Date date) {

            }
        });
        calendarPickerView.setOnInvalidDateSelectedListener(new CalendarPickerView.OnInvalidDateSelectedListener() {
            @Override
            public void onInvalidDateSelected(Date date) {
                mOnDateSelectedListener.onInvalidDateSelected(date);
            }
        });
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mOnDateSelectedListener = (OnDateSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnDateSelectedListener");
        }
    }

    public interface OnDateSelectedListener {
        public void onValidDateSelected(Date date);

        public void onInvalidDateSelected(Date date);

        public Date getDate();
    }
}
