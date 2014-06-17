package io.github.izzyleung.zhihudailypurify.ui.fragment;

import android.content.SharedPreferences;
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
import io.github.izzyleung.zhihudailypurify.ui.widget.SwipeRefreshLayout;

import java.util.List;

public class NewsListFragment extends BaseNewsFragment implements SwipeRefreshLayout.OnRefreshListener {
    private String date;

    private boolean isAutoRefresh;
    private boolean isToday;

    // Fragment is single in PortalActivity
    private boolean isSingle;
    private boolean isRefreshed = false;
    private BaseGetNewsTask.UpdateUIListener mCallback = new BaseGetNewsTask.UpdateUIListener() {

        @Override
        public void beforeTaskStart() {
            mSwipeRefreshLayout.setRefreshing(true);
        }

        @Override
        public void afterTaskFinished(List<DailyNews> resultList, boolean isRefreshSuccess, boolean isContentSame) {
            if (isRefreshSuccess && !isContentSame) {
                newsList = resultList;
                listAdapter.updateNewsList(newsList);
                new SaveNewsListTask(date, newsList).execute();
            }

            mSwipeRefreshLayout.setRefreshing(false);
            isRefreshed = true;

            if (!isRefreshSuccess && isAdded()) {
                Crouton.makeText(getActivity(), getActivity().getString(R.string.network_error), Style.ALERT).show();
            }
        }
    };

    private ListView mListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

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
        mListView = (ListView) view.findViewById(R.id.news_list);
        mListView.setAdapter(listAdapter);
        mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), false, true, onScrollListener));

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listItemOnClick(position);
            }
        });
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return listItemOnLongClick(position);
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorScheme(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        new RecoverNewsListTask().executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        clearActionMode();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        isAutoRefresh = pref.getBoolean("auto_refresh?", true);

        refresh((isToday || isSingle) && isAutoRefresh && !isRefreshed);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        refresh(isVisibleToUser && isAutoRefresh && !isRefreshed);
    }

    @Override
    public void onDestroy() {
        Crouton.cancelAllCroutons();

        super.onDestroy();
    }

    @Override
    protected boolean isCleanListChoice() {
        int position = mListView.getCheckedItemPosition();
        return mListView.getFirstVisiblePosition() > position || mListView.getLastVisiblePosition() < position;
    }

    @Override
    protected void clearListChoice() {
        mListView.clearChoices();
        listAdapter.notifyDataSetChanged();
    }

    @Override
    protected void checkItemAtPosition(int position) {
        mListView.setItemChecked(position, true);
    }

    private void refresh(boolean prerequisite) {
        if (prerequisite) {
            doRefresh();
        }
    }

    private void doRefresh() {
        if (isToday) {
            new OriginalGetNewsTask(date, mCallback).execute();
        } else {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if (sharedPreferences.getBoolean("using_accelerate_server?", false)) {
                String serverCode;
                if (sharedPreferences.getString("which_accelerate_server", ServerCode.SAE).equals(ServerCode.SAE)) {
                    serverCode = ServerCode.SAE;
                } else {
                    serverCode = ServerCode.HEROKU;
                }
                new AccelerateGetNewsTask(serverCode, date, mCallback).execute();
            } else {
                new OriginalGetNewsTask(date, mCallback).execute();
            }
        }
    }

    @Override
    public void onRefresh() {
        doRefresh();
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
        }
    }
}