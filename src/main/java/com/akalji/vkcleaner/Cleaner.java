package com.akalji.vkcleaner;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.vk.api.sdk.client.ClientResponse;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiCaptchaException;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.newsfeed.NewsfeedItemType;
import com.vk.api.sdk.objects.wall.responses.GetCommentsResponse;


import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

public class Cleaner {
    private VkApiClient vk;
    private String TOKEN;
    private int userId;
    private UserActor actor;
    private Queue<NewsfeedItemExtended> newsfeedItems = new ArrayDeque<>();


    void clean() {
        init();

        updateNewsQueue();
        cleanItems();
    }

    private void init() {
        vk = new VkApiClient(HttpTransportClient.getInstance());
        actor = new UserActor(userId, TOKEN);
    }

    private void updateNewsQueue() {
        try {
            ClientResponse commentsRAW = vk.newsfeed()
                    .getComments(actor)
                    .executeAsRaw();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(commentsRAW.getContent());
            JsonNode locatedNode = rootNode.findPath("items");
            for (JsonNode jsonNode : locatedNode) {
                newsfeedItems.offer(new Gson().fromJson(jsonNode.toString(), NewsfeedItemExtended.class));
            }

        } catch (ClientException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cleanItems() {
        while (!newsfeedItems.isEmpty()) {
            NewsfeedItemExtended newsfeedItem = newsfeedItems.poll();
            try {
                switch (newsfeedItem.getType()) {
                    case POST:

                        GetCommentsResponse commentsResponse = vk.wall()
                                .getComments(actor, newsfeedItem.getPostId())
                                .ownerId(newsfeedItem.getSourceId())
                                .execute();
                        commentsResponse.getItems().stream()
                                .filter(item -> item.getFromId().equals(userId))
                                .forEach(System.out::println);
                        break;
                    case NOTE:
                        break;
                    case PHOTO:
                        break;
                    case WALL_PHOTO:
                        break;
                    case TOPIC:
                        break;
                    case VIDEO:
                        break;
                }
                Thread.sleep(300);
            } catch (ApiCaptchaException e) {
                newsfeedItems.offer(newsfeedItem);
                solveCaptcha(e);
            } catch (ApiException e) {
                e.printStackTrace();
            } catch (ClientException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void solveCaptcha(ApiCaptchaException e) {
        e.getImage();
        e.getSid();
    }

    public void saveComment(NewsfeedItemType type, int sourceId, int placeId){

    }

    public void setToken(String token) {
        this.TOKEN = token;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
