package io.github.izzyleung.zhihudailypurify.task;

import android.text.Html;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import io.github.izzyleung.zhihudailypurify.support.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BaseSearchTask extends BaseDownloadTask<String, Void, Void> {
    protected boolean isSearchSuccess = true;
    protected boolean isResultNull = false;

    protected List<String> dateResultList = new ArrayList<String>();
    protected List<DailyNews> newsList = new ArrayList<DailyNews>();

    private Type newsType = new TypeToken<DailyNews>() {

    }.getType();

    private Gson gson = new GsonBuilder().create();

    private SimpleDateFormat simpleDateFormat;

    public BaseSearchTask(String dateFormat) {
        this.simpleDateFormat = new SimpleDateFormat(dateFormat);
    }

    @Override
    protected Void doInBackground(String... params) {
        String result;
        try {
            //noinspection deprecation
            result = convert(downloadStringFromUrl(Constants.SEARCH_URL
                    + URLEncoder.encode(params[0].trim()).replace("+", "%20")));
            if (!TextUtils.isEmpty(result) && !isCancelled()) {
                JSONArray resultArray = new JSONArray(result);

                if (resultArray.length() == 0) {
                    isResultNull = true;
                    return null;
                } else {
                    for (int i = 0; i < resultArray.length(); i++) {
                        JSONObject newsObject = resultArray.getJSONObject(i);
                        String date = newsObject.getString("date");
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(Constants.simpleDateFormat.parse(date));
                        calendar.add(Calendar.DAY_OF_YEAR, -1);
                        dateResultList.add(simpleDateFormat.format(calendar.getTime()));
                        DailyNews news = gson.fromJson(newsObject.getString("content"), newsType);
                        newsList.add(news);
                    }
                }
            } else {
                isSearchSuccess = false;
            }
        } catch (IOException e) {
            isSearchSuccess = false;
        } catch (JSONException e) {
            isSearchSuccess = false;
        } catch (ParseException ignored) {
            isSearchSuccess = false;
        }

        return null;
    }

    private String convert(String in) {
        return Html.fromHtml(Html.fromHtml(in).toString()).toString();
    }
}
