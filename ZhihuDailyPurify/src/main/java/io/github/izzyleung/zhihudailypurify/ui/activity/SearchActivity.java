package io.github.izzyleung.zhihudailypurify.ui.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import io.github.izzyleung.zhihudailypurify.support.util.DateUtils;
import io.github.izzyleung.zhihudailypurify.support.util.URLUtils;
import io.github.izzyleung.zhihudailypurify.task.BaseDownloadTask;
import io.github.izzyleung.zhihudailypurify.ui.fragment.SearchNewsFragment;
import io.github.izzyleung.zhihudailypurify.ui.widget.IzzySearchView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SearchActivity extends FragmentActivity {
    private IzzySearchView searchView;
    private SearchNewsFragment searchNewsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common);

        initView();

        searchNewsFragment = new SearchNewsFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, searchNewsFragment)
                .commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Crouton.cancelAllCroutons();
        searchNewsFragment = null;
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
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowCustomEnabled(true);

        searchView = new IzzySearchView(this);
        searchView.setOnCloseListener(new IzzySearchView.OnCloseListener() {
            public boolean onClose() {
                return true;
            }
        });
        searchView.setQueryHint(getResources().getString(R.string.search_hint));

        searchView.setOnQueryTextListener(new IzzySearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                new SearchTask().execute(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        RelativeLayout relative = new RelativeLayout(this);
        relative.addView(searchView);
        getActionBar().setCustomView(relative);
    }

    class SearchTask extends BaseDownloadTask<String, Void, Void> {
        private boolean isSearchSuccess = true;
        private boolean isResultNull = false;

        private List<String> dateResultList = new ArrayList<String>();
        private List<DailyNews> newsList = new ArrayList<DailyNews>();

        private ProgressDialog dialog;

        private Type newsType = new TypeToken<DailyNews>() {

        }.getType();

        private Gson gson = new GsonBuilder().create();

        private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.display_format));

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
        protected Void doInBackground(String... params) {
            String result;
            try {
                result = Html.fromHtml(Html.fromHtml(downloadStringFromUrl(
                        URLUtils.SEARCH_URL + params[0])).toString()).toString();
                if (!TextUtils.isEmpty(result) && !isCancelled()) {
                    JSONArray resultArray = new JSONArray(result);

                    if (resultArray.length() == 0) {
                        isResultNull = true;
                        return null;
                    } else {
                        for (int i = 0; i < resultArray.length(); i++) {
                            JSONObject newsObject = resultArray.getJSONObject(i);
                            String date = newsObject.getString("date");
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(DateUtils.simpleDateFormat.parse(date));
                            calendar.add(Calendar.DAY_OF_YEAR, -1);
                            dateResultList.add(simpleDateFormat.format(calendar.getTime()));
                            DailyNews news = gson.fromJson(newsObject.getString("content"), newsType);
                            newsList.add(news);
                        }
                    }
                } else {
                    isSearchSuccess = false;
                }
            } catch (IOException e) {
                isSearchSuccess = false;
            } catch (JSONException e) {
                isSearchSuccess = false;
            } catch (ParseException ignored) {
                isSearchSuccess = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (dialog != null) {
                dialog.dismiss();
                dialog = null;
            }

            if (isSearchSuccess && !isCancelled()) {
                searchNewsFragment.updateContent(newsList, dateResultList);
            } else {
                Crouton.makeText(SearchActivity.this,
                        getString(R.string.network_error),
                        Style.ALERT).show();
            }

            if (isResultNull && !isCancelled()) {
                Crouton.makeText(SearchActivity.this,
                        getString(R.string.no_result_found),
                        Style.ALERT).show();
            }

            searchView.clearFocus();
        }
    }
}
