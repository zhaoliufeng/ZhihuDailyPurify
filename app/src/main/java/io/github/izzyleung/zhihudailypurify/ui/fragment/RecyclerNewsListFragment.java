package io.github.izzyleung.zhihudailypurify.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.adapter.NewsAdapter;
import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import io.github.izzyleung.zhihudailypurify.task.AccelerateGetNewsTask;
import io.github.izzyleung.zhihudailypurify.task.BaseGetNewsTask;
import io.github.izzyleung.zhihudailypurify.task.OriginalGetNewsTask;

public class RecyclerNewsListFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener, BaseGetNewsTask.UpdateUIListener {
    protected List<DailyNews> newsList = new ArrayList<>();
    NewsAdapter mAdapter;
    private String date;
    private boolean isAutoRefresh;
    private boolean isToday;
    // Fragment is single in PortalActivity
    private boolean isSingle;
    private boolean isRefreshed = false;
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
        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.news_list);
        mRecyclerView.setHasFixedSize(!isToday);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);
        mAdapter = new NewsAdapter(newsList);
        mRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        return view;
    }

    @Override
    public void onRefresh() {
        if (isToday) {
            new OriginalGetNewsTask(date, this).execute();
        } else {
            SharedPreferences sharedPreferences
                    = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if (sharedPreferences.getBoolean("using_accelerate_server?", false)) {
                new AccelerateGetNewsTask(date, this).execute();
            } else {
                new OriginalGetNewsTask(date, this).execute();
            }
        }
    }

    @Override
    public void beforeTaskStart() {
        mSwipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void afterTaskFinished(List<DailyNews> resultList, boolean isRefreshSuccess, boolean isContentSame) {
        mSwipeRefreshLayout.setRefreshing(false);
        isRefreshed = true;

        newsList = resultList;

        mAdapter.updateNewsList(newsList);

        if (isRefreshSuccess) {
            if (!isContentSame) {
                newsList = resultList;

                mAdapter.updateNewsList(newsList);
            }
        } else if (isAdded()) {
            Toast.makeText(getActivity(), getActivity().getString(R.string.network_error), Toast.LENGTH_SHORT).show();
        }
    }
}
