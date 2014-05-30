package io.github.izzyleung.zhihudailypurify.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.adapter.NewsAdapter;
import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import io.github.izzyleung.zhihudailypurify.support.util.CommonUtils;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    private List<String> dateResultList = new ArrayList<String>();
    private List<DailyNews> newsList = new ArrayList<DailyNews>();
    private NewsAdapter newsAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        newsAdapter = new NewsAdapter(
                activity,
                newsList,
                dateResultList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.fragment_search, null);
        assert view != null;
        StickyListHeadersListView listView = (StickyListHeadersListView)
                view.findViewById(R.id.result_list);

        listView.setAdapter(newsAdapter);
        listView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), false, true));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                CommonUtils.listOnClick(getActivity(), newsList.get(position));
            }
        });

        return view;
    }

    public void updateContent(List<String> dateResultList, List<DailyNews> newsList) {
        this.dateResultList = dateResultList;
        this.newsList = newsList;

        newsAdapter.setDateResultList(dateResultList);
        newsAdapter.setNewsList(newsList);

        newsAdapter.notifyDataSetChanged();
    }
}
