package io.github.izzyleung.zhihudailypurify.observable;

import com.annimon.stream.Stream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import io.github.izzyleung.zhihudailypurify.bean.Question;
import io.github.izzyleung.zhihudailypurify.bean.Story;
import io.github.izzyleung.zhihudailypurify.support.Constants;
import io.github.izzyleung.zhihudailypurify.support.lib.optional.Optional;
import rx.Observable;

import static io.github.izzyleung.zhihudailypurify.observable.Helper.getHtml;

public class DailyNewsFromZhihuObservable {
    private static final String QUESTION_SELECTOR = "div.question";
    private static final String QUESTION_TITLES_SELECTOR = "h2.question-title";
    private static final String QUESTION_LINKS_SELECTOR = "div.view-more a";

    public static Observable<List<DailyNews>> ofDate(String date) {
        return getHtml(Constants.Urls.ZHIHU_DAILY_BEFORE, date)
                .flatMap(DailyNewsFromZhihuObservable::getStoriesJsonArray)
                .flatMap(jsonArray -> getStoriesObservable(jsonArray, date))
                .flatMap(DailyNewsFromZhihuObservable::updateNewsDocument)
                .map(DailyNewsFromZhihuObservable::convertStoryToDailyNews)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(dailyNews -> dailyNews.getQuestions() != null)
                .toList();
    }

    private static Observable<JSONArray> getStoriesJsonArray(String html) {
        return Observable.create(subscriber -> {
            try {
                subscriber.onNext(new JSONObject(html).getJSONArray("stories"));
                subscriber.onCompleted();
            } catch (JSONException e) {
                subscriber.onError(e);
            }
        });
    }

    private static Observable<Story> getStoriesObservable(JSONArray newsArray, String date) {
        return Observable.create(subscriber -> {
            try {
                for (int i = 0; i < newsArray.length(); i++) {
                    JSONObject newsJson = newsArray.getJSONObject(i);
                    subscriber.onNext(getStoryFromJSON(newsJson, date));
                }

                subscriber.onCompleted();
            } catch (JSONException e) {
                subscriber.onError(e);
            }
        });
    }

    private static Story getStoryFromJSON(JSONObject jsonStory, String date) throws JSONException {
        Story story = new Story();

        story.setDate(date);
        story.setStoryId(jsonStory.getInt("id"));
        story.setDailyTitle(jsonStory.getString("title"));
        story.setThumbnailUrl(getThumbnailUrlForStory(jsonStory));

        return story;
    }

    private static String getThumbnailUrlForStory(JSONObject jsonStory) throws JSONException {
        if (jsonStory.has("images")) {
            return (String) jsonStory.getJSONArray("images").get(0);
        } else {
            return null;
        }
    }

    private static Observable<Story> updateNewsDocument(Story news) {
        return getHtml(Constants.Urls.ZHIHU_DAILY_OFFLINE_NEWS, news.getStoryId())
                .map(DailyNewsFromZhihuObservable::getStoryDocument)
                .map(news::updateDocument);
    }

    private static Document getStoryDocument(String json) {
        try {
            JSONObject newsJson = new JSONObject(json);
            return newsJson.has("body") ? Jsoup.parse(newsJson.getString("body")) : null;
        } catch (JSONException e) {
            return null;
        }
    }

    private static Optional<DailyNews> convertStoryToDailyNews(Story story) {
        Optional<DailyNews> newsOptional = DailyNews.createFromStory(story);

        if (newsOptional.isPresent()) {
            List<Question> questions = getQuestions(story.getDocument(), newsOptional.get().getDailyTitle());
            if (Stream.of(questions).allMatch(q -> isLinkToZhihu(q.getUrl()))) {
                newsOptional.get().setQuestions(questions);
            }
        }

        return newsOptional;
    }

    private static List<Question> getQuestions(Document document, String dailyTitle) {
        List<Question> result = new ArrayList<>();
        Elements questionElements = getQuestionElements(document);

        for (Element questionElement : questionElements) {
            Question question = new Question();

            String questionTitle = getQuestionTitleFromQuestionElement(questionElement);
            String questionUrl = getQuestionUrlFromQuestionElement(questionElement);
            questionTitle = questionTitle == null ? dailyTitle : questionTitle;

            question.setTitle(questionTitle);
            question.setUrl(questionUrl);

            result.add(question);
        }

        return result;
    }

    private static Elements getQuestionElements(Document document) {
        return document.select(QUESTION_SELECTOR);
    }

    private static String getQuestionTitleFromQuestionElement(Element questionElement) {
        Element questionTitleElement = questionElement.select(QUESTION_TITLES_SELECTOR).first();

        if (questionTitleElement == null) {
            return null;
        } else {
            return questionTitleElement.text();
        }
    }

    private static String getQuestionUrlFromQuestionElement(Element questionElement) {
        Element viewMoreElement = questionElement.select(QUESTION_LINKS_SELECTOR).first();

        if (viewMoreElement == null) {
            return null;
        } else {
            return viewMoreElement.attr("href");
        }
    }

    private static boolean isLinkToZhihu(String url) {
        return url != null && url.startsWith(Constants.Strings.ZHIHU_QUESTION_LINK_PREFIX);
    }
}
