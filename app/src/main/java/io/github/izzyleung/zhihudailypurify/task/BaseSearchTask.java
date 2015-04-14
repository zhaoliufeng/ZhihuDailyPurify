package io.github.izzyleung.zhihudailypurify.task;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import io.github.izzyleung.zhihudailypurify.support.Constants;
import io.github.izzyleung.zhihudailypurify.support.lib.Http;

public class BaseSearchTask extends BaseHttpTask<String, Void, List<DailyNews>> {
    protected boolean isSearchSuccess = false;

    @Override
    protected List<DailyNews> doInBackground(String... params) {
        Gson gson = new GsonBuilder().create();

        Type newsType = new TypeToken<DailyNews>() {

        }.getType();

        try {
            String result = decodeHtml(Http.get(Constants.Url.SEARCH, params[0].trim(), true));

            List<DailyNews> newsList = new ArrayList<>();

            if (!TextUtils.isEmpty(result) && !isCancelled()) {
                JSONArray resultArray = new JSONArray(result);

                if (resultArray.length() == 0) {
                    return null;
                } else {
                    for (int i = 0; i < resultArray.length(); i++) {
                        JSONObject newsObject = resultArray.getJSONObject(i);
                        DailyNews news = gson.fromJson(newsObject.getString("content"), newsType);
                        assert news != null;
                        news.setDate(newsObject.getString("date"));
                        newsList.add(news);
                    }

                    isSearchSuccess = true;
                    return newsList;
                }
            }
        } catch (IOException | JSONException ignored) {

        }

        return null;
    }
}
