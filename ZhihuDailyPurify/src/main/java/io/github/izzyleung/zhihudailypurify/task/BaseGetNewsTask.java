package io.github.izzyleung.zhihudailypurify.task;

import io.github.izzyleung.zhihudailypurify.ZhihuDailyPurifyApplication;
import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import io.github.izzyleung.zhihudailypurify.support.lib.MyAsyncTask;

import java.util.List;

public abstract class BaseGetNewsTask extends BaseDownloadTask<Void, Void, List<DailyNews>> {
    protected boolean isRefreshSuccess = true;
    protected boolean isContentSame = false;
    protected String date;

    private UpdateUIListener mListener;

    public BaseGetNewsTask(String date, UpdateUIListener callback) {
        this.date = date;
        this.mListener = callback;
    }

    @Override
    protected void onPreExecute() {
        mListener.beforeTaskStart();
    }

    @Override
    protected void onPostExecute(List<DailyNews> resultNewsList) {
        if (isRefreshSuccess && !isContentSame) {
            new SaveNewsListTask(date, resultNewsList).executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }

        mListener.afterTaskFinished(resultNewsList, isRefreshSuccess, isContentSame);
        mListener = null;
    }

    protected boolean checkIsContentSame(List<DailyNews> externalNewsList) {
        return externalNewsList.equals(ZhihuDailyPurifyApplication.getInstance().getDataSource().newsOfTheDay(date));
    }

    public static interface UpdateUIListener {
        public void beforeTaskStart();

        public void afterTaskFinished(List<DailyNews> resultList, boolean isRefreshSuccess, boolean isContentSame);
    }
}
