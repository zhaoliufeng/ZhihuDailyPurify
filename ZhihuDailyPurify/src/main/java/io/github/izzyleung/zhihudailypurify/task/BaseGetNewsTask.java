package io.github.izzyleung.zhihudailypurify.task;

import io.github.izzyleung.zhihudailypurify.ZhihuDailyPurifyApplication;
import io.github.izzyleung.zhihudailypurify.bean.DailyNews;

import java.util.List;

public abstract class BaseGetNewsTask extends BaseDownloadTask<Void, Void, List<DailyNews>> {
    protected boolean isRefreshSuccess = true;
    protected boolean isContentSame = false;
    protected String date;

    private OnTaskFinishedCallback mCallback;

    public BaseGetNewsTask(String date, OnTaskFinishedCallback callback) {
        this.date = date;
        this.mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        mCallback.beforeTaskStart();
    }

    @Override
    protected void onPostExecute(List<DailyNews> resultNewsList) {
        mCallback.afterTaskFinished(resultNewsList, isRefreshSuccess, isContentSame);
    }

    protected boolean checkIsNewsListEquals(List<DailyNews> newsListFromWeb) {
        return newsListFromWeb.equals(ZhihuDailyPurifyApplication.getInstance().getDataSource().newsOfTheDay(date));
    }

    public interface OnTaskFinishedCallback {
        public void beforeTaskStart();

        public void afterTaskFinished(List<DailyNews> resultList, boolean isRefreshSuccess, boolean isContentSame);
    }
}
