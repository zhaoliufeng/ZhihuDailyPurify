package io.github.izzyleung.zhihudailypurify.bean;

import org.jsoup.nodes.Document;

import io.github.izzyleung.zhihudailypurify.support.lib.optional.Optional;

public class StoryWithDocument {
    private Story story;
    private Document document;

    private StoryWithDocument() {

    }

    public static Optional<StoryWithDocument> create(Story story, Document document) {
        StoryWithDocument result = null;

        if (document != null) {
            result = new StoryWithDocument();
            result.story = story;
            result.document = document;
        }

        return Optional.ofNullable(result);
    }

    public Story getStory() {
        return story;
    }

    public Document getDocument() {
        return document;
    }
}

