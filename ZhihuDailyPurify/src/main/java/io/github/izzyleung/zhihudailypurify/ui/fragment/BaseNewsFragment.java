package io.github.izzyleung.zhihudailypurify.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*;
import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.adapter.NewsAdapter;
import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import taobe.tec.jcc.JChineseConvertor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class BaseNewsFragment extends Fragment {
    protected List<DailyNews> newsList = new ArrayList<DailyNews>();
    protected NewsAdapter listAdapter;
    protected ListView listView;
    protected StickyListHeadersListView stickyListHeadersListView;

    protected int longClickItemIndex = 0;

    protected ActionMode mActionMode;
    protected ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

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
                    if (actionMode.getCustomView() != null) {
                        ((Spinner) actionMode.getCustomView()).getSelectedItemPosition();
                    } else {
                        Intent share = new Intent(android.content.Intent.ACTION_SEND);
                        share.setType("text/plain");
                        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

                        share.putExtra(Intent.EXTRA_SUBJECT, "This is extra subject");
                        share.putExtra(Intent.EXTRA_TEXT, newsList.get(longClickItemIndex).getQuestionUrl());

                        startActivity(Intent.createChooser(share, "Share to..."));
                    }
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
    };

    AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (mActionMode != null) {
                if (listView != null) {
                    int position = listView.getCheckedItemPosition();
                    if (listView.getFirstVisiblePosition() > position
                            || listView.getLastVisiblePosition() < position) {
                        clearActionMode();
                    }
                } else {
                    int position = stickyListHeadersListView.getCheckedItemPosition();
                    if (stickyListHeadersListView.getFirstVisiblePosition() > position
                            || stickyListHeadersListView.getLastVisiblePosition() < position) {
                        clearActionMode();
                    }
                }
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        listAdapter = new NewsAdapter(activity, newsList);
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

    protected void clearListChoice() {
        if (listView != null) {
            listView.clearChoices();
            listAdapter.notifyDataSetChanged();
        } else {
//            stickyListHeadersListView.clearChoices();
            listAdapter.notifyDataSetChanged();
        }
    }

    protected void listItemOnClick(final int position) {
        if (resetActionMode()) {
            return;
        }

        clearListChoice();

        DailyNews dailyNews = newsList.get(position);

        if (dailyNews.isMulti()) {
            String[] questionTitles = dailyNews.
                    getQuestionTitleList().
                    toArray(new String[dailyNews.getQuestionTitleList().size()]);

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
            //Or, just go to Zhihu
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

        if (listView != null) {
            listView.setItemChecked(position, true);
        } else {
            stickyListHeadersListView.setItemChecked(position, true);
        }

        longClickItemIndex = position;
        mActionMode = ((ActionBarActivity) getActivity()).startSupportActionMode(mActionModeCallback);
        if (newsList.get(position).isMulti()) {
            Spinner spinner = new Spinner(getActivity());
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    getActivity(),
                    android.R.layout.simple_spinner_item,
                    newsList.get(position).getQuestionTitleList());
            adapter.setDropDownViewResource(R.layout.action_bar_spinner_item);
            spinner.setAdapter(adapter);
            mActionMode.setCustomView(spinner);
        } else {
            mActionMode.setTitle(newsList.get(position).getDailyTitle());
        }

        return true;
    }
}
