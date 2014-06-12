package io.github.izzyleung.zhihudailypurify.ui.fragment;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.ZhihuDailyPurifyApplication;
import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import io.github.izzyleung.zhihudailypurify.support.lib.MyAsyncTask;
import io.github.izzyleung.zhihudailypurify.task.*;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import java.util.List;

public class NewsListFragment extends BaseNewsFragment implements OnRefreshListener {
    private String date;

    private boolean isAutoRefresh;
    private boolean isToday;

    // Fragment is single in PortalActivity
    private boolean isSingle;
    private boolean isRefreshed = false;
    private BaseGetNewsTask.GetNewsUpdateUIListener mCallback = new BaseGetNewsTask.GetNewsUpdateUIListener() {
        @Override
        public void beforeTaskStart() {
            mPullToRefreshLayout.setRefreshing(true);
        }

        @Override
        public void afterTaskFinished(List<DailyNews> resultList, boolean isRefreshSuccess, boolean isContentSame) {
            if (isRefreshSuccess && !isContentSame) {
                newsList = resultList;
                listAdapter.updateNewsList(newsList);
                new SaveNewsListTask(date, newsList).execute();
            }

            mPullToRefreshLayout.setRefreshComplete();
            isRefreshed = true;

            if (!isRefreshSuccess && isAdded()) {
                Crouton.makeText(getActivity(), getActivity().getString(R.string.network_error), Style.ALERT).show();
            }
        }
    };
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

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        isAutoRefresh = pref.getBoolean("auto_refresh?", true);

        refresh(isSingle);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        refresh(isVisibleToUser);
    }

    @Override
    public void onDestroy() {
        Crouton.cancelAllCroutons();

        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mPullToRefreshLayout.getHeaderTransformer().onConfigurationChanged(getActivity(), newConfig);
    }

    @Override
    public void onRefreshStarted(View view) {
        refresh(true);
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

    public void refresh(boolean prerequisite) {
        if (prerequisite && isAutoRefresh && !isRefreshed) {
            if (isToday) {
                new OriginalGetNewsTask(date, mCallback).execute();
            } else {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

                if (sharedPreferences.getBoolean("using_accelerate_server?", false)) {
                    Server server;

                    // 1 for SAE
                    if (sharedPreferences.getString("which_accelerate_server", "1").equals("1")) {
                        server = Server.SAE;
                    } else {
                        server = Server.HEROKU;
                    }

                    new AccelerateGetNewsTask(server, date, mCallback).execute();
                } else {
                    new OriginalGetNewsTask(date, mCallback).execute();
                }
            }
        }
    }

    private class RecoverNewsListTask extends MyAsyncTask<Void, Void, List<DailyNews>> {

        @Override
        protected List<DailyNews> doInBackground(Void... params) {
            return ZhihuDailyPurifyApplication.getInstance().getDataSource().newsOfTheDay(date);
        }

        @Override
        protected void onPostExecute(List<DailyNews> newsListRecovered) {
            if (newsListRecovered != null) {
                newsList = newsListRecovered;
                listAdapter.updateNewsList(newsListRecovered);
            }

            refresh(isToday);
        }
    }
}