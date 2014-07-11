package io.github.izzyleung.zhihudailypurify;

import android.app.Application;
import android.content.Context;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import io.github.izzyleung.zhihudailypurify.db.DailyNewsDataSource;

public final class ZhihuDailyPurifyApplication extends Application {
    private static ZhihuDailyPurifyApplication applicationContext;
    private DailyNewsDataSource dataSource;

    public static void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .denyCacheImageMultipleSizesInMemory()
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();
        ImageLoader.getInstance().init(config);
    }

    public static ZhihuDailyPurifyApplication getInstance() {
        return applicationContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;

        initImageLoader(getApplicationContext());
        dataSource = new DailyNewsDataSource(getApplicationContext());
        dataSource.open();
    }

    public DailyNewsDataSource getDataSource() {
        return dataSource;
    }
}
