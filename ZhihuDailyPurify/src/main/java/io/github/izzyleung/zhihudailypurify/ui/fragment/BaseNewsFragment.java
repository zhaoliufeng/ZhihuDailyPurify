package io.github.izzyleung.zhihudailypurify.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.widget.ListView;
import android.widget.Toast;
import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.adapter.NewsAdapter;
import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import taobe.tec.jcc.JChineseConvertor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BaseNewsFragment extends Fragment {
    protected List<DailyNews> newsList = new ArrayList<DailyNews>();
    protected NewsAdapter listAdapter;
    protected ListView listView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        listAdapter = new NewsAdapter(activity, newsList);
    }

    protected void listItemOnclick(final int position) {
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
}
