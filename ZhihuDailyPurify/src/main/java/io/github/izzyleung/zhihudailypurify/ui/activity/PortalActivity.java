package io.github.izzyleung.zhihudailypurify.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.support.util.DateUtils;
import io.github.izzyleung.zhihudailypurify.ui.fragment.NewsListFragment;
import io.github.izzyleung.zhihudailypurify.ui.fragment.PickDateFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PortalActivity extends FragmentActivity implements PickDateFragment.PickDateListener {
    private String dateForFragment;
    private Calendar calendar = Calendar.getInstance();
    private MenuItem prev, next;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (pref.getBoolean("accelerate_server_hint", true)) {
            showDialogOnFirstLaunch(pref);
        } else {
            showPickDateFragment();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Crouton.cancelAllCroutons();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            this.finish();
        } else {
            for (int i = getSupportFragmentManager().getBackStackEntryCount(); i > 0; i--) {
                getSupportFragmentManager().popBackStack();
            }

            getActionBar().setTitle(R.string.activity_pick_date);
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
                if (DateUtils.isSameDay(Calendar.getInstance(), calendar)) {
                    showCrouton(R.string.this_is_today, Style.INFO);
                    return true;
                }
                updateFields(ACTION.ACTION_NEXT_DAY);
                updateView();
                return true;
            case R.id.back:
                if (DateUtils.isSameDay(DateUtils.birthDay, calendar)) {
                    showCrouton(R.string.this_is_birthday, Style.INFO);
                    return true;
                }
                updateFields(ACTION.ACTION_PREVIOUS_DAY);
                updateView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDialogOnFirstLaunch(final SharedPreferences pref) {
        pref.edit().putBoolean("accelerate_server_hint", false).commit();
        AlertDialog.Builder dialog = new AlertDialog.Builder(this).setCancelable(false);
        dialog.setTitle(getString(R.string.accelerate_server_hint_dialog_title));
        dialog.setMessage(getString(R.string.accelerate_server_hint_dialog_message));
        dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pref.edit().putBoolean("using_accelerate_server?", true).commit();
                showPickDateFragment();
            }
        });

        dialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showPickDateFragment();
            }
        });

        dialog.show();
    }

    private void showPickDateFragment() {
        Bundle bundle = new Bundle();
        bundle.putString("date", DateUtils.simpleDateFormat.format(calendar.getTime()));

        Fragment displayFragment = new PickDateFragment();
        displayFragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, displayFragment)
                .commit();

        getActionBar().setTitle(R.string.activity_pick_date);
    }

    private void updateFields(ACTION action) {
        if (action == ACTION.ACTION_NEXT_DAY) {
            calendar.add(Calendar.DAY_OF_YEAR, 2);
            dateForFragment = DateUtils.simpleDateFormat.format(calendar.getTime());
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        } else {
            dateForFragment = DateUtils.simpleDateFormat.format(calendar.getTime());
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }
    }

    private void updateView() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("auto_refresh?", true);
        bundle.putBoolean("single?", true);
        bundle.putString("date", dateForFragment);

        if (DateUtils.isSameDay(calendar, Calendar.getInstance())) {
            bundle.putBoolean("first_page?", true);
        } else {
            bundle.putBoolean("first_page?", false);
        }

        Fragment displayFragment = new NewsListFragment();
        displayFragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, displayFragment)
                .addToBackStack(null)
                .commit();

        String displayDate = new SimpleDateFormat(getString(R.string.display_format)).
                format(calendar.getTime());

        getActionBar().setTitle(displayDate);
    }

    @Override
    public void onValidDateSelected(Date date) {
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        dateForFragment = DateUtils.simpleDateFormat.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_YEAR, -1);

        prev.setVisible(true);
        next.setVisible(true);

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

    private enum ACTION {ACTION_PREVIOUS_DAY, ACTION_NEXT_DAY}
}