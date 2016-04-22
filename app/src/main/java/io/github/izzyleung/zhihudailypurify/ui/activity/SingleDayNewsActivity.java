package io.github.izzyleung.zhihudailypurify.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.support.Constants;
import io.github.izzyleung.zhihudailypurify.ui.fragment.NewsListFragment;

public class SingleDayNewsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        Fragment newFragment = new NewsListFragment();

        String dateString = bundle.getString(Constants.BundleKeys.DATE);
        Date date;
        try {
            date = Constants.Dates.simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            // This shall never happen.
            date = Constants.Dates.birthday;
        }

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(DateFormat.getDateInstance().format(date));

        bundle.putString(Constants.BundleKeys.DATE, dateString);
        bundle.putBoolean(Constants.BundleKeys.IS_SINGLE, true);

        newFragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_frame, newFragment)
                .commit();
    }
}
