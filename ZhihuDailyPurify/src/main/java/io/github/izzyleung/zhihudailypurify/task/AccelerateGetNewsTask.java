package io.github.izzyleung.zhihudailypurify.task;

import android.text.TextUtils;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import io.github.izzyleung.zhihudailypurify.support.Constants;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AccelerateGetNewsTask extends BaseGetNewsTask {
    private String serverCode;

    public AccelerateGetNewsTask(String serverCode, String date, UpdateUIListener callback) {
        super(date, callback);
        this.serverCode = serverCode;
    }

    @Override
    protected List<DailyNews> doInBackground(Void... params) {
        List<DailyNews> resultNewsList = new ArrayList<DailyNews>();

        Type listType = new TypeToken<List<DailyNews>>() {

        }.getType();

        String baseUrl, jsonFromWeb;
        if (serverCode.equals(ServerCode.SAE)) {
            baseUrl = Constants.ZHIHU_DAILY_PURIFY_SAE_BEFORE_URL;
        } else {
            baseUrl = Constants.ZHIHU_DAILY_PURIFY_HEROKU_BEFORE_URL;
        }

        try {
            jsonFromWeb = downloadStringFromUrl(baseUrl + date);
        } catch (IOException e) {
            isRefreshSuccess = false;
            return null;
        }

        String newsListJSON = convert(jsonFromWeb);

        if (!TextUtils.isEmpty(newsListJSON)) {
            try {
                resultNewsList = new GsonBuilder().create().fromJson(newsListJSON, listType);
            } catch (JsonSyntaxException ignored) {

            }
        } else {
            isRefreshSuccess = false;
        }

        isContentSame = checkIsContentSame(resultNewsList);
        return resultNewsList;
    }
}
