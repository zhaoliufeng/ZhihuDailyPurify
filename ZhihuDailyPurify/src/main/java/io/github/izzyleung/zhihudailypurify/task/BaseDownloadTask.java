package io.github.izzyleung.zhihudailypurify.task;

import android.text.Html;
import io.github.izzyleung.zhihudailypurify.support.lib.MyAsyncTask;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public abstract class BaseDownloadTask<Params, Progress, Result> extends MyAsyncTask<Params, Progress, Result> {
    protected String downloadStringFromUrl(String url) throws IOException {
        HttpClient client = new DefaultHttpClient();

        HttpParams params = client.getParams();
        HttpConnectionParams.setConnectionTimeout(params, 5 * 1000);
        HttpConnectionParams.setSoTimeout(params, 5 * 1000);

        HttpGet request = new HttpGet(url);
        String result = "";

        try {
            HttpResponse httpResponse = client.execute(request);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            }
        } finally {
            client.getConnectionManager().shutdown();
        }

        return result;
    }

    protected String convert(String in) {
        return Html.fromHtml(Html.fromHtml(in).toString()).toString();
    }
}
