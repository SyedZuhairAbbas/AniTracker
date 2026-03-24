package com.zuhair.animetracker.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.zuhair.animetracker.models.Anime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JikanApi {
    private OkHttpClient client = new OkHttpClient();

    public List<Anime> searchAnime(String query) throws Exception {
        String url = "https://api.jikan.moe/v4/anime?q=" + query;

        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        String jsonBody = response.body().string();

        JsonObject root = JsonParser.parseString(jsonBody).getAsJsonObject();
        JsonArray data = root.getAsJsonArray("data");

        List<Anime> animeList = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            JsonObject animeObj = data.get(i).getAsJsonObject();
            int malId = animeObj.get("mal_id").getAsInt();
            String title = animeObj.get("title").getAsString();
            int episodeCount = animeObj.get("episodes").isJsonNull() ? 0 : animeObj.get("episodes").getAsInt();
            float score = animeObj.get("score").isJsonNull() ? 0.0f : animeObj.get("score").getAsFloat();
            String imageUrl = animeObj.getAsJsonObject("images")
                    .getAsJsonObject("jpg")
                    .get("image_url").getAsString();
            String synopsis = animeObj.get("synopsis").isJsonNull() ? "No synopsis available." : animeObj.get("synopsis").getAsString();

            Anime anime = new Anime(title , episodeCount , score , imageUrl , malId , synopsis);
            animeList.add(anime);
        }
        return animeList;
    }

    public List<Anime> fetchTopAiring() throws IOException {
        String url = "https://api.jikan.moe/v4/top/anime?filter=airing&limit=10";
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        String jsonBody = response.body().string();
        JsonObject root = JsonParser.parseString(jsonBody).getAsJsonObject();
        JsonArray data = root.getAsJsonArray("data");

        List<Anime> animeList = new ArrayList<>();

        for(int i = 0; i < data.size(); i++){
            JsonObject animeObj = data.get(i).getAsJsonObject();
            int malId = animeObj.get("mal_id").getAsInt();
            String title = animeObj.get("title").getAsString();
            int episodeCount = animeObj.get("episodes").isJsonNull() ? 0 : animeObj.get("episodes").getAsInt();
            float score = animeObj.get("score").isJsonNull() ? 0.0f : animeObj.get("score").getAsFloat();
            String imageUrl = animeObj.getAsJsonObject("images")
                    .getAsJsonObject("jpg")
                    .get("image_url").getAsString();
            String synopsis = animeObj.get("synopsis").isJsonNull() ? "No synopsis available." : animeObj.get("synopsis").getAsString();

            Anime anime = new Anime(title , episodeCount , score , imageUrl , malId , synopsis);
            animeList.add(anime);
        }
        return animeList;
    }

    public List<Anime> fetchTrending() throws Exception {
        String url = "https://api.jikan.moe/v4/top/anime?filter=bypopularity&limit=20";
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        String jsonBody = response.body().string();
        JsonObject root = JsonParser.parseString(jsonBody).getAsJsonObject();
        JsonArray data = root.getAsJsonArray("data");

        List<Anime> animeList = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JsonObject animeObj = data.get(i).getAsJsonObject();
            int malId = animeObj.get("mal_id").getAsInt();
            String title = animeObj.get("title").getAsString();
            int episodeCount = animeObj.get("episodes").isJsonNull() ? 0 : animeObj.get("episodes").getAsInt();
            float score = animeObj.get("score").isJsonNull() ? 0.0f : animeObj.get("score").getAsFloat();
            String imageUrl = animeObj.getAsJsonObject("images")
                    .getAsJsonObject("jpg")
                    .get("image_url").getAsString();
            String synopsis = animeObj.get("synopsis").isJsonNull() ? "" : animeObj.get("synopsis").getAsString();
            animeList.add(new Anime(title, episodeCount, score, imageUrl, malId, synopsis));
        }
        return animeList;
    }
}

