package io.github.izzyleung.zhihudailypurify.support;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.github.izzyleung.zhihudailypurify.bean.DailyNews;

public final class Constants {
    private Constants() {

    }

    public static final class Urls {
        public static final String ZHIHU_DAILY_BEFORE = "http://news.at.zhihu.com/api/4/news/before/";
        public static final String ZHIHU_DAILY_OFFLINE_NEWS = "http://news-at.zhihu.com/api/4/news/";
        public static final String ZHIHU_DAILY_PURIFY_BEFORE = "http://zhihu-daily-purify.herokuapp.com/raw/";
        public static final String SEARCH = "http://zhihu-daily-purify.herokuapp.com/search/";
    }

    public static final class Dates {
        public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
        @SuppressWarnings("deprecation")
        public static final Date birthday = new java.util.Date(113, 4, 19); // May 19th, 2013
    }

    public static final class Types {
        public static final Type newsType = new TypeToken<DailyNews>() {

        }.getType();

        public static final Type newsListType = new TypeToken<List<DailyNews>>() {

        }.getType();
    }

    public static final class Strings {
        public static final String VIEW_ZHIHU_DISCUSSION = "查看知乎讨论";
        public static final String ORIGINAL_DESCRIPTION = "原题描述";
    }
}
