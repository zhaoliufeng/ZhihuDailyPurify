package io.github.izzyleung.zhihudailypurify.ui.fragment;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.ZhihuDailyPurifyApplication;
import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import io.github.izzyleung.zhihudailypurify.support.lib.MyAsyncTask;
import io.github.izzyleung.zhihudailypurify.support.util.URLUtils;
import io.github.izzyleung.zhihudailypurify.task.BaseDownloadTask;
import io.github.izzyleung.zhihudailypurify.task.SaveNewsListTask;
import io.github.izzyleung.zhihudailypurify.task.Server;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NewsListFragment extends BaseNewsFragment implements OnRefreshListener {
    private String date;

    private boolean isAutoRefresh;
    private boolean isToday;

    // Fragment is single in PortalActivity
    private boolean isSingle;
    private boolean isRefreshed = false;

    private ListView listView;
    private PullToRefreshLayout mPullToRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            Bundle bundle = getArguments();
            date = bundle.getString("date");
            isToday = bundle.getBoolean("first_page?");
            isSingle = bundle.getBoolean("single?");

            setRetainInstance(true);
            setHasOptionsMenu(isSingle);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_list, container, false);
        assert view != null;
        listView = (ListView) view.findViewById(R.id.news_list);
        listView.setAdapter(listAdapter);
        listView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), false, true, onScrollListener));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listItemOnClick(position);
            }
        });
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return listItemOnLongClick(position);
            }
        });

        mPullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.from(getActivity())
                .allChildrenArePullable()
                .listener(this)
                .setup(mPullToRefreshLayout);

        new RecoverNewsListTask().executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        clearActionMode();

        SharedPreferences pref = PreferenceManager.
                getDefaultSharedPreferences(getActivity());
        isAutoRefresh = pref.getBoolean("auto_refresh?", true);

        if (isSingle) {
            if (isAutoRefresh && !isRefreshed) {
                refresh();
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            if (isAutoRefresh && !isRefreshed) {
                refresh();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Crouton.cancelAllCroutons();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mPullToRefreshLayout.getHeaderTransformer().
                onConfigurationChanged(getActivity(), newConfig);
    }

    @Override
    public void onRefreshStarted(View view) {
        refresh();
    }

    @Override
    protected boolean isCleanListChoice() {
        int position = listView.getCheckedItemPosition();
        return listView.getFirstVisiblePosition() > position || listView.getLastVisiblePosition() < position;
    }

    @Override
    protected void clearListChoice() {
        listView.clearChoices();
        listAdapter.notifyDataSetChanged();
    }

    @Override
    protected void checkItemAtPosition(int position) {
        listView.setItemChecked(position, true);
    }

    public void refresh() {
        if (isToday) {
            new OriginalGetNewsTask().execute();
        } else {
            if (getActivity() != null) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(getActivity());

                if (sharedPreferences.getBoolean("using_accelerate_server?", false)) {
                    Server server;
                    if (Integer.parseInt(sharedPreferences.getString("which_accelerate_server", "1")) == 1) {
                        server = Server.SAE;
                    } else {
                        server = Server.HEROKU;
                    }

                    new AccelerateGetNewsTask(server).execute();
                } else {
                    new OriginalGetNewsTask().execute();
                }
            }
        }
    }

    private class RecoverNewsListTask extends MyAsyncTask<Void, Void, List<DailyNews>> {

        @Override
        protected List<DailyNews> doInBackground(Void... params) {
            return ZhihuDailyPurifyApplication.getInstance().getDataSource().getDailyNewsList(date);
        }

        @Override
        protected void onPostExecute(List<DailyNews> newsListRecovered) {
            if (newsListRecovered != null) {
                newsList = newsListRecovered;
                listAdapter.updateNewsList(newsListRecovered);
            }

            if (isToday) {
                refresh();
            }
        }
    }

    private abstract class BaseGetNewsTask extends BaseDownloadTask<Void, Void, Void> {
        protected boolean isRefreshSuccess = true;
        protected boolean isTheSameContent = false;

        protected List<DailyNews> resultNewsList = new ArrayList<DailyNews>();

        @Override
        protected void onPreExecute() {
            mPullToRefreshLayout.setRefreshing(true);
        }

        @Override
        protected void onPostExecute(Void result) {
            if (isRefreshSuccess && !newsList.equals(resultNewsList)) {
                isTheSameContent = false;
                newsList = resultNewsList;
                if (getActivity() != null && isAdded()) {
                    listAdapter.updateNewsList(newsList);
                }
            }

            if (isRefreshSuccess && !isTheSameContent) {
                new SaveNewsListTask(date, newsList).execute();
            }

            mPullToRefreshLayout.setRefreshComplete();
            isRefreshed = true;

            if (!isRefreshSuccess && isAdded()) {
                warning();
            }
        }

        protected void warning() {
            Crouton.makeText(getActivity(), getActivity().getString(R.string.network_error), Style.ALERT).show();
        }
    }

    private class OriginalGetNewsTask extends BaseGetNewsTask {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONObject contents = new JSONObject(downloadStringFromUrl(URLUtils.ZHIHU_DAILY_BEFORE_URL + date));

                JSONArray newsArray = contents.getJSONArray("stories");
                for (int i = 0; i < newsArray.length(); i++) {
                    JSONObject singleNews = newsArray.getJSONObject(i);

                    DailyNews dailyNews = new DailyNews();
                    dailyNews.setThumbnailUrl(singleNews.has("images")
                            ? (String) singleNews.getJSONArray("images").get(0)
                            : null);
                    dailyNews.setDailyTitle(singleNews.getString("title"));
                    String newsInfoJson = downloadStringFromUrl(URLUtils.ZHIHU_DAILY_OFFLINE_NEWS_URL
                            + singleNews.getString("id"));
                    JSONObject newsDetail = new JSONObject(newsInfoJson);
                    if (newsDetail.has("body")) {
                        Document doc = Jsoup.parse(newsDetail.getString("body"));
                        if (updateDailyNews(doc, singleNews.getString("title"), dailyNews)) {
                            isTheSameContent = false;
                            resultNewsList.add(dailyNews);
                        }
                    }

                }
            } catch (JSONException e) {
                isRefreshSuccess = false;
            } catch (IOException e) {
                isRefreshSuccess = false;
            }

            return null;
        }

        private boolean updateDailyNews(
                Document doc,
                String dailyTitle,
                DailyNews dailyNews) throws JSONException {
            Elements viewMoreElements = doc.getElementsByClass("view-more");

            if (viewMoreElements.size() > 1) {
                dailyNews.setMulti(true);
                Elements questionTitleElements =
                        doc.getElementsByClass("question-title");

                for (int j = 0; j < viewMoreElements.size(); j++) {
                    if (questionTitleElements.get(j).text().length() == 0) {
                        dailyNews.addQuestionTitle(dailyTitle);
                    } else {
                        dailyNews.addQuestionTitle(questionTitleElements.get(j).text());
                    }

                    Elements viewQuestionElement = viewMoreElements.get(j).
                            select("a");

                    if (viewQuestionElement.text().equals("查看知乎讨论")) {
                        dailyNews.addQuestionUrl(viewQuestionElement.attr("href"));
                    } else {
                        return false;
                    }
                }
            } else if (viewMoreElements.size() == 1) {
                dailyNews.setMulti(false);

                Elements viewQuestionElement = viewMoreElements.select("a");
                if (viewQuestionElement.text().equals("查看知乎讨论")) {
                    dailyNews.setQuestionUrl(viewQuestionElement.attr("href"));
                } else {
                    return false;
                }

                //Question title is the same with daily title
                if (doc.getElementsByClass("question-title").text().length() == 0) {
                    dailyNews.setQuestionTitle(dailyTitle);
                } else {
                    dailyNews.setQuestionTitle(doc.
                            getElementsByClass("question-title").text());
                }
            } else {
                return false;
            }

            return true;
        }
    }

    private class AccelerateGetNewsTask extends BaseGetNewsTask {
        private Server server;

        public AccelerateGetNewsTask(Server server) {
            this.server = server;
        }

        @Override
        protected Void doInBackground(Void... aVoid) {
            Type listType = new TypeToken<List<DailyNews>>() {

            }.getType();

            String jsonFromWeb;
            try {
                if (server == Server.SAE) {
                    jsonFromWeb = downloadStringFromUrl(URLUtils.
                            ZHIHU_DAILY_PURIFY_SAE_BEFORE_URL + date);
                } else {
                    jsonFromWeb = downloadStringFromUrl(URLUtils.
                            ZHIHU_DAILY_PURIFY_HEROKU_BEFORE_URL + date);
                }
            } catch (IOException e) {
                isRefreshSuccess = false;
                return null;
            }

            String newsListJSON = Html.fromHtml(
                    Html.fromHtml(jsonFromWeb).toString()).toString();

            if (!TextUtils.isEmpty(newsListJSON)) {
                try {
                    resultNewsList = new GsonBuilder().create().
                            fromJson(newsListJSON, listType);
                } catch (JsonSyntaxException e) {
                    isRefreshSuccess = false;
                }
            } else {
                isRefreshSuccess = false;
            }

            return null;
        }
    }
}