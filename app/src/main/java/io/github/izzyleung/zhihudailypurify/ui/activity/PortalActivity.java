package io.github.izzyleung.zhihudailypurify.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.support.Constants;
import io.github.izzyleung.zhihudailypurify.ui.fragment.NewsListFragment;
import io.github.izzyleung.zhihudailypurify.ui.fragment.PickDateFragment;

public class PortalActivity extends ActionBarActivity
        implements PickDateFragment.PickDateListener {
    private static final int ACTION_PREVIOUS_DAY = 0, ACTION_NEXT_DAY = 1;

    private String dateForFragment;

    private Calendar calendar = Calendar.getInstance();
    private MenuItem prev, next;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        showPickDateFragment();
    }

    @Override
    protected void onDestroy() {
        Crouton.cancelAllCroutons();

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            this.finish();
        } else {
            for (int i = getSupportFragmentManager().getBackStackEntryCount(); i > 0; i--) {
                getSupportFragmentManager().popBackStack();
            }

            getSupportActionBar().setTitle(R.string.action_pick_date);
            prev.setVisible(false);
            next.setVisible(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.browse_date, menu);
        prev = menu.findItem(R.id.back);
        next = menu.findItem(R.id.forward);

        prev.setVisible(false);
        next.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.forward:
                if (isSameDay(Calendar.getInstance(), calendar)) {
                    showCrouton(R.string.this_is_today, Style.INFO);
                    return true;
                }
                updateFields(ACTION_NEXT_DAY);
                updateView();
                return true;
            case R.id.back:
                if (isSameDay(Constants.Date.birthday, calendar.getTime())) {
                    showCrouton(R.string.this_is_birthday, Style.INFO);
                    return true;
                }
                updateFields(ACTION_PREVIOUS_DAY);
                updateView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showPickDateFragment() {
        Bundle bundle = new Bundle();
        bundle.putString("date", Constants.Date.simpleDateFormat.format(calendar.getTime()));

        Fragment displayFragment = new PickDateFragment();
        displayFragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, displayFragment)
                .commit();

        getSupportActionBar().setTitle(R.string.action_pick_date);
    }

    private void updateFields(int action) {
        if (action == ACTION_NEXT_DAY) {
            calendar.add(Calendar.DAY_OF_YEAR, 2);
            dateForFragment = Constants.Date.simpleDateFormat.format(calendar.getTime());
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        } else if (action == ACTION_PREVIOUS_DAY) {
            dateForFragment = Constants.Date.simpleDateFormat.format(calendar.getTime());
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }
    }

    private void updateView() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("auto_refresh?", true);
        bundle.putBoolean("single?", true);
        bundle.putString("date", dateForFragment);

        if (isSameDay(calendar, Calendar.getInstance())) {
            bundle.putBoolean("first_page?", true);
        } else {
            bundle.putBoolean("first_page?", false);
        }

        Fragment displayFragment = new NewsListFragment();
        displayFragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, displayFragment)
                .addToBackStack(null)
                .commit();

        String displayDate = new SimpleDateFormat(getString(R.string.display_format)).
                format(calendar.getTime());

        getSupportActionBar().setTitle(displayDate);
    }

    private boolean isSameDay(Calendar first, Calendar second) {
        return (first.get(Calendar.YEAR) == second.get(Calendar.YEAR)) &&
                (first.get(Calendar.MONTH) == second.get(Calendar.MONTH)) &&
                (first.get(Calendar.DAY_OF_MONTH) == second.get(Calendar.DAY_OF_MONTH));
    }

    private boolean isSameDay(Date first, Date second) {
        return first.equals(second);
    }

    @Override
    public void onValidDateSelected(Date date) {
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        dateForFragment = Constants.Date.simpleDateFormat.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_YEAR, -1);

        prev.setVisible(true);
        next.setVisible(true);

        Crouton.cancelAllCroutons();

        updateView();
    }

    @Override
    public void onInvalidDateSelected(Date date) {
        if (date.after(new Date())) {
            showCrouton(R.string.not_coming, Style.ALERT);
        } else {
            showCrouton(R.string.not_born, Style.ALERT);
        }
    }

    @Override
    public Date getDate() {
        return calendar.getTime();
    }

    private void showCrouton(int resId, Style style) {
        Crouton.makeText(PortalActivity.this, getString(resId), style).show();
    }
}