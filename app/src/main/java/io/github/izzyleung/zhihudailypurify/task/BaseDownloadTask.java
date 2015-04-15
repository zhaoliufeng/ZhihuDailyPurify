package io.github.izzyleung.zhihudailypurify.task;

import android.os.AsyncTask;
import android.text.Html;

public abstract class BaseDownloadTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    protected String decodeHtml(String in) {
        return Html.fromHtml(Html.fromHtml(in).toString()).toString();
    }
}
