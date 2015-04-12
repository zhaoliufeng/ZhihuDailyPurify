package io.github.izzyleung.zhihudailypurify.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import io.github.izzyleung.zhihudailypurify.support.Check;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private List<DailyNews> newsList;
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.noimage)
            .showImageOnFail(R.drawable.noimage)
            .showImageForEmptyUri(R.drawable.lks_for_blank_url)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .build();
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

    public RecyclerViewAdapter(List<DailyNews> newsList) {
        this.newsList = newsList;
    }

    public void setNewsList(List<DailyNews> newsList) {
        this.newsList = newsList;
    }

    public void updateNewsList(List<DailyNews> newsList) {
        setNewsList(newsList);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.news_list_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final DailyNews dailyNews = new DailyNews(newsList.get(position));
        imageLoader.displayImage(dailyNews.getThumbnailUrl(), holder.newsImage, options, animateFirstListener);

        if (dailyNews.isMulti()) {
            holder.questionTitle.setText(dailyNews.getDailyTitle());
            String simplifiedMultiQuestion = "这里包含多个知乎讨论，请点击后选择";
            holder.dailyTitle.setText(simplifiedMultiQuestion);
        } else {
            holder.questionTitle.setText(dailyNews.getQuestionTitle());
            holder.dailyTitle.setText(dailyNews.getDailyTitle());
        }

        holder.whole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                browseOrShare(holder.whole.getContext(), position, true);
            }
        });

        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(holder.overflow.getContext(), holder.overflow);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.contextual_news_list, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_share_url:
                                browseOrShare(holder.overflow.getContext(), position, false);
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    private void browseOrShare(final Context context, int position, final boolean browse) {
        final DailyNews dailyNews = newsList.get(position);
        if (dailyNews.isMulti()) {
            String[] questionTitles = dailyNews.getQuestionTitleList()
                    .toArray(new String[dailyNews.getQuestionTitleList().size()]);

            new AlertDialog.Builder(context)
                    .setTitle(dailyNews.getDailyTitle())
                    .setItems(questionTitles, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (browse) {
                                goToZhihu(context, dailyNews.getQuestionUrlList().get(which));
                            } else {
                                String questionTitle, questionUrl;

                                if (dailyNews.isMulti()) {
                                    questionTitle = dailyNews.getQuestionTitleList().get(which);
                                    questionUrl = dailyNews.getQuestionUrlList().get(which);
                                } else {
                                    questionTitle = dailyNews.getQuestionTitle();
                                    questionUrl = dailyNews.getQuestionUrl();
                                }

                                share(context, questionTitle, questionUrl);
                            }
                        }
                    }).show();
        } else {
            if (browse) {
                goToZhihu(context, dailyNews.getQuestionUrl());
            } else {
                String questionTitle = dailyNews.getQuestionTitle(),
                        questionUrl = dailyNews.getQuestionUrl();

                share(context, questionTitle, questionUrl);
            }
        }
    }

    private void goToZhihu(Context context, String url) {
        if (!PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("using_client?", false)) {
            openUsingBrowser(context, url);
        } else if (Check.isZhihuClientInstalled()) {
            //Open using Zhihu's official client
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            browserIntent.setPackage("com.zhihu.android");
            context.startActivity(browserIntent);
        } else {
            openUsingBrowser(context, url);
        }
    }

    private void openUsingBrowser(Context context, String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

        if (Check.isIntentSafe(browserIntent)) {
            context.startActivity(browserIntent);
        } else {
            Toast.makeText(context, context.getString(R.string.no_browser), Toast.LENGTH_SHORT).show();
        }
    }

    private void share(Context context, String questionTitle, String questionUrl) {
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        //noinspection deprecation
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        share.putExtra(Intent.EXTRA_TEXT, questionTitle + " " + questionUrl + " 分享自知乎网");
        context.startActivity(Intent.createChooser(share, context.getString(R.string.share_to)));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View whole;
        public ImageView newsImage;
        public TextView questionTitle;
        public TextView dailyTitle;
        public ImageView overflow;

        public ViewHolder(View v) {
            super(v);

            whole = v;
            newsImage = (ImageView) v.findViewById(R.id.thumbnail_image);
            questionTitle = (TextView) v.findViewById(R.id.question_title);
            dailyTitle = (TextView) v.findViewById(R.id.daily_title);
            overflow = (ImageView) v.findViewById(R.id.card_share_overflow);
        }
    }

    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {
        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }
}
