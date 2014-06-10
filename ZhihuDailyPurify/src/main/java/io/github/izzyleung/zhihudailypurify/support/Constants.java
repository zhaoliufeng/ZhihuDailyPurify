package io.github.izzyleung.zhihudailypurify.support;

import java.text.SimpleDateFormat;
import java.util.Date;

public interface Constants {
    String ZHIHU_DAILY_BEFORE_URL = "http://news.at.zhihu.com/api/3/news/before/";
    String ZHIHU_DAILY_OFFLINE_NEWS_URL = "http://news-at.zhihu.com/api/3/news/";
    String ZHIHU_DAILY_PURIFY_HEROKU_BEFORE_URL = "http://zhihu-daily-purify.herokuapp.com/raw/";
    String ZHIHU_DAILY_PURIFY_SAE_BEFORE_URL = "http://zhihudailypurify.sinaapp.com/raw/";
    String SEARCH_URL = "http://zhihudailypurify.sinaapp.com/search/";

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
    Date birthday = new Date(113, 4, 19); // May 19th, 2013
}
