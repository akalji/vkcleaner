package com.akalji.vkcleaner;

import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.newsfeed.NewsfeedItem;

public class NewsfeedItemExtended extends NewsfeedItem {
    @SerializedName("post_id")
    private int postId;

    public int getPostId() {
        return postId;
    }

    public String toString() {
        return "NewsfeedItem{" + "type='" + getType() + "'" +
                ", sourceId=" + getSourceId() +
                ", postId=" + postId +
                ", date=" + getDate() +
                '}';
    }
}
