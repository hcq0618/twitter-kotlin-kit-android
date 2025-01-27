/*
 * Copyright (C) 2015 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.twitter.sdk.android.tweetui;

import androidx.annotation.NonNull;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.TweetBuilder;
import com.twitter.sdk.android.core.models.TwitterCollection;
import com.twitter.sdk.android.core.models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import retrofit2.Call;

/**
 * CollectionTimeline provides a timeline of tweets from the collections/collection API source.
 */
public class CollectionTimeline extends BaseTimeline implements Timeline<Tweet> {
    static final String COLLECTION_PREFIX = "custom-";

    final TwitterCore twitterCore;
    final String collectionIdentifier;
    final int maxItemsPerRequest;

    CollectionTimeline(TwitterCore twitterCore, long collectionId, int maxItemsPerRequest) {
        // prefix the collection id with the collection prefix
        this.collectionIdentifier = COLLECTION_PREFIX + collectionId;
        this.twitterCore = twitterCore;
        this.maxItemsPerRequest = maxItemsPerRequest;
    }

    /**
     * Loads items with position greater than minPosition. If minPosition is null, loads items
     * with the greatest ids.
     *
     * @param minPosition minimum position of the items to load (exclusive).
     * @param cb          callback.
     */
    @Override
    public void next(Long minPosition, Callback<TimelineResult<Tweet>> cb) {
        createCollectionRequest(minPosition, null).enqueue(new CollectionCallback(cb));
    }

    /**
     * Loads items with position less than or equal to maxPosition.
     *
     * @param maxPosition maximum position of the items to load (exclusive).
     * @param cb          callback.
     */
    @Override
    public void previous(Long maxPosition, Callback<TimelineResult<Tweet>> cb) {
        createCollectionRequest(null, maxPosition).enqueue(new CollectionCallback(cb));
    }

    Call<TwitterCollection> createCollectionRequest(final Long minPosition,
                                                    final Long maxPosition) {
        return twitterCore.getApiClient().getCollectionService()
                .collection(collectionIdentifier, maxItemsPerRequest, maxPosition, minPosition);
    }


    /**
     * Wrapper callback which unpacks a TwitterCollection into a TimelineResult (cursor and items).
     */
    static class CollectionCallback extends Callback<TwitterCollection> {
        final Callback<TimelineResult<Tweet>> cb;

        /**
         * Constructs a CollectionCallback
         *
         * @param cb A Callback which expects a TimelineResult
         */
        CollectionCallback(Callback<TimelineResult<Tweet>> cb) {
            this.cb = cb;
        }

        @Override
        public void success(Result<TwitterCollection> result) {
            final TimelineCursor timelineCursor = getTimelineCursor(result.getData());
            final List<Tweet> tweets = getOrderedTweets(result.getData());
            final TimelineResult<Tweet> timelineResult;
            if (timelineCursor != null) {
                timelineResult = new TimelineResult<>(timelineCursor, tweets);
            } else {
                timelineResult = new TimelineResult<>(null, Collections.<Tweet>emptyList());
            }
            if (cb != null) {
                cb.success(new Result(timelineResult, result.getResponse()));
            }
        }

        @Override
        public void failure(@NonNull TwitterException exception) {
            if (cb != null) {
                cb.failure(exception);
            }
        }
    }


    static List<Tweet> getOrderedTweets(TwitterCollection collection) {
        if (collection == null || collection.getContents() == null ||
                collection.getContents().tweetMap == null || collection.getContents().userMap == null ||
                collection.getContents().tweetMap.isEmpty() || collection.getContents().userMap.isEmpty() ||
                collection.getMetadata() == null || collection.getMetadata().getTimelineItems() == null ||
                collection.getMetadata().getPosition() == null) {
            return Collections.emptyList();
        }
        final List<Tweet> tweets = new ArrayList<>();
        for (TwitterCollection.TimelineItem item : collection.getMetadata().getTimelineItems()) {
            final Tweet trimmedTweet = collection.getContents().tweetMap.get(item.getTweetItem().getId());
            final Tweet tweet = mapTweetToUsers(trimmedTweet, collection.getContents().userMap);
            tweets.add(tweet);
        }
        return tweets;
    }

    static Tweet mapTweetToUsers(Tweet trimmedTweet, Map<Long, User> userMap) {
        // read user id from the trimmed Tweet
        final Long userId = trimmedTweet.user.getId();
        // lookup User in the collection response's UserMap
        final User user = userMap.get(userId);
        // build the Tweet with the User
        final TweetBuilder builder = new TweetBuilder().copy(trimmedTweet).setUser(user);
        // Repeat process for any quote tweets
        if (trimmedTweet.quotedStatus != null) {
            final Tweet quoteStatus = mapTweetToUsers(trimmedTweet.quotedStatus, userMap);
            builder.setQuotedStatus(quoteStatus);
        }

        return builder.build();
    }

    static TimelineCursor getTimelineCursor(TwitterCollection twitterCollection) {
        if (twitterCollection == null || twitterCollection.getMetadata() == null ||
                twitterCollection.getMetadata().getPosition() == null) {
            return null;
        }
        final Long minPosition = twitterCollection.getMetadata().getPosition().getMinPosition();
        final Long maxPosition = twitterCollection.getMetadata().getPosition().getMaxPosition();
        return new TimelineCursor(minPosition, maxPosition);
    }

    /**
     * CollectionTimeline Builder.
     */
    public static class Builder {
        private final TwitterCore twitterCore;
        private long collectionId;
        private int maxItemsPerRequest = 30;

        /**
         * Constructs a Builder.
         */
        public Builder() {
            twitterCore = TwitterCore.getInstance();
        }

        // For testing
        Builder(TwitterCore twitterCore) {
            this.twitterCore = twitterCore;
        }

        /**
         * Sets the id for the CollectionTimeline.
         *
         * @param collectionId The collection id such as 539487832448843776.
         */
        public Builder id(long collectionId) {
            this.collectionId = collectionId;
            return this;
        }

        /**
         * Sets the number of Tweets returned per request for the CollectionTimeline.
         *
         * @param maxItemsPerRequest The number of tweets to return per request, up to a maximum of
         *                           200.
         */
        public Builder maxItemsPerRequest(int maxItemsPerRequest) {
            this.maxItemsPerRequest = maxItemsPerRequest;
            return this;
        }

        /**
         * Builds a CollectionTimeline from the Builder parameters.
         *
         * @return a CollectionTimeline
         * @throws java.lang.IllegalStateException if query is not set (is null).
         */
        public CollectionTimeline build() {
            return new CollectionTimeline(twitterCore, collectionId, maxItemsPerRequest);
        }
    }
}
