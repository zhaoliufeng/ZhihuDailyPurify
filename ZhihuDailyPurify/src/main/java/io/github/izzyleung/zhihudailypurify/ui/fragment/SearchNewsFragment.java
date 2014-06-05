package io.github.izzyleung.zhihudailypurify.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import java.util.ArrayList;
import java.util.List;

public class SearchNewsFragment extends BaseNewsFragment {
    private List<String> dateResultList = new ArrayList<String>();

    private StickyListHeadersListView stickyListHeadersListView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        listAdapter.updateDateResultList(dateResultList);
    }

    @Override
    protected boolean isCleanListChoice() {
        int position = stickyListHeadersListView.getCheckedItemPosition();
        return stickyListHeadersListView.getFirstVisiblePosition() > position
                || stickyListHeadersListView.getLastVisiblePosition() < position;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        assert view != null;
        stickyListHeadersListView = (StickyListHeadersListView)
                view.findViewById(R.id.result_list);
        stickyListHeadersListView.setAdapter(listAdapter);
        stickyListHeadersListView.setOnScrollListener(
                new PauseOnScrollListener(ImageLoader.getInstance(),
                        false,
                        true,
                        onScrollListener));
        stickyListHeadersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                listItemOnClick(position);
            }
        });
        stickyListHeadersListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        stickyListHeadersListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return listItemOnLongClick(position);
            }
        });

        return view;
    }

    @Override
    protected void clearListChoice() {
        for (int i = 0; i < newsList.size(); i++) {
            stickyListHeadersListView.setItemChecked(i, false);
        }

        listAdapter.notifyDataSetChanged();
    }

    @Override
    protected void checkItemAtPosition(int position) {
        stickyListHeadersListView.setItemChecked(position, true);
    }

    public void updateContent(List<DailyNews> newsList, List<String> dateResultList) {
        this.newsList = newsList;
        this.dateResultList = dateResultList;

        listAdapter.updateContents(newsList, dateResultList);
    }
}
