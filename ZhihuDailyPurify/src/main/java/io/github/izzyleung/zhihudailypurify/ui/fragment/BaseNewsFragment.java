package io.github.izzyleung.zhihudailypurify.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.adapter.NewsAdapter;
import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import taobe.tec.jcc.JChineseConvertor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class BaseNewsFragment extends Fragment
        implements ActionMode.Callback, AbsListView.OnScrollListener {
    protected int longClickItemIndex = 0;
    protected int spinnerSelectedItemIndex = 0;

    protected List<DailyNews> newsList = new ArrayList<DailyNews>();
    protected NewsAdapter listAdapter;

    protected ActionMode mActionMode;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        listAdapter = new NewsAdapter(activity, newsList);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (!isVisibleToUser && isAdded()) {
            clearActionMode();
        }
    }

    protected boolean resetActionMode() {
        if (mActionMode != null) {
            clearActionMode();
            return true;
        } else {
            return false;
        }
    }

    protected void clearActionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
        }

        clearListChoice();
    }

    protected abstract boolean isCleanListChoice();

    protected abstract void clearListChoice();

    protected abstract void checkItemAtPosition(int position);

    protected void listItemOnClick(final int position) {
        if (resetActionMode()) {
            return;
        }

        clearListChoice();

        DailyNews dailyNews = newsList.get(position);
        if (dailyNews.isMulti()) {
            String[] questionTitles = dailyNews.getQuestionTitleList()
                    .toArray(new String[dailyNews.getQuestionTitleList().size()]);

            // Convert title to Traditional Chinese to meet the displaying language
            if (Locale.getDefault().equals(Locale.TRADITIONAL_CHINESE)) {
                JChineseConvertor convertor = null;
                boolean canConvert = true;

                try {
                    convertor = JChineseConvertor.getInstance();
                } catch (IOException e) {
                    canConvert = false;
                }

                if (canConvert) {
                    for (int i = 0; i < questionTitles.length; i++) {
                        questionTitles[i] = convertor.s2t(questionTitles[i]);
                    }
                }
            }

            new AlertDialog.Builder(getActivity())
                    .setTitle(dailyNews.getDailyTitle())
                    .setItems(questionTitles, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goToZhihu(newsList.get(position).getQuestionUrlList().get(which));
                        }
                    }).show();
        } else {
            goToZhihu(dailyNews.getQuestionUrl());
        }
    }

    private void goToZhihu(String url) {
        boolean isUsingClient = PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getBoolean("using_client?", false);

        if (!isUsingClient) {
            openUsingBrowser(url);
        } else {
            //Open using Zhihu's official client
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                browserIntent.setPackage("com.zhihu.android");
                getActivity().startActivity(browserIntent);
            } catch (ActivityNotFoundException e) {
                openUsingBrowser(url);
            }
        }
    }

    private void openUsingBrowser(String url) {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            getActivity().startActivity(browserIntent);
        } catch (ActivityNotFoundException ane) {
            Toast.makeText(getActivity(), getString(R.string.no_browser), Toast.LENGTH_SHORT).show();
        }
    }

    protected boolean listItemOnLongClick(int position) {
        if (mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
        }

        checkItemAtPosition(position);

        longClickItemIndex = position;
        mActionMode = getActivity().startActionMode(this);
        if (newsList.get(position).isMulti()) {
            Spinner spinner = new Spinner(getActivity());
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    getActivity(),
                    android.R.layout.simple_spinner_item,
                    newsList.get(position).getQuestionTitleList());
            adapter.setDropDownViewResource(R.layout.spinner_dropdpwn_item);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    spinnerSelectedItemIndex = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    spinnerSelectedItemIndex = 0;
                }
            });
            mActionMode.setCustomView(spinner);
        } else {
            mActionMode.setTitle(newsList.get(position).getQuestionTitle());
        }

        return true;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mActionMode != null && isCleanListChoice()) {
            clearActionMode();
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        actionMode.getMenuInflater().inflate(R.menu.contextual_news_list, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_share_url:
                Intent share = new Intent(android.content.Intent.ACTION_SEND);
                share.setType("text/plain");
                share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

                String url;
                if (newsList.get(longClickItemIndex).isMulti()) {
                    url = newsList.get(longClickItemIndex).getQuestionUrlList().get(spinnerSelectedItemIndex);
                } else {
                    url = newsList.get(longClickItemIndex).getQuestionUrl();
                }

                share.putExtra(Intent.EXTRA_TEXT, url);
                startActivity(Intent.createChooser(share, getString(R.string.share_to)));
                actionMode.finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        mActionMode = null;
        clearListChoice();
    }
}
