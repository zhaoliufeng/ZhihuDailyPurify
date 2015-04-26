package io.github.izzyleung.zhihudailypurify.ui.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.List;

import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import io.github.izzyleung.zhihudailypurify.task.BaseSearchTask;
import io.github.izzyleung.zhihudailypurify.ui.fragment.SearchNewsFragment;
import io.github.izzyleung.zhihudailypurify.ui.widget.IzzySearchView;

public class SearchActivity extends BaseActivity {
    private IzzySearchView searchView;
    private SearchNewsFragment searchNewsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();

        searchNewsFragment = new SearchNewsFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_frame, searchNewsFragment)
                .commit();
    }

    @Override
    public void onDestroy() {
        searchNewsFragment = null;

        super.onDestroy();
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

    private void initView() {
        searchView = new IzzySearchView(this);
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        searchView.setOnQueryTextListener(new IzzySearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                new SearchTask().execute(query);
                searchView.clearFocus();
                return true;
            }
        });

        RelativeLayout relative = new RelativeLayout(this);
        relative.addView(searchView);

        mToolBar.addView(relative);

        setSupportActionBar(mToolBar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    class SearchTask extends BaseSearchTask {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(SearchActivity.this);
            dialog.setMessage(getString(R.string.searching));
            dialog.setCancelable(true);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    SearchTask.this.cancel(true);
                }
            });
            dialog.show();
        }

        @Override
        protected void onPostExecute(List<DailyNews> newsList) {
            if (dialog != null) {
                dialog.dismiss();
                dialog = null;
            }

            if (!isCancelled()) {
                if (isSearchSuccess) {
                    searchNewsFragment.updateContent(newsList);
                } else {
                    Toast.makeText(SearchActivity.this,
                            getString(R.string.no_result_found),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
