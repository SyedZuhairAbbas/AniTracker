package com.zuhair.animetracker.models;

import org.bson.types.ObjectId;

public class Watchlist {
    private ObjectId id;
    private int malId;
    private String title;
    private String image;
    private int episodeCount;
    private String status;
    private int episodesWatched;
    private float personalRating;


    public Watchlist(ObjectId id, int malId, String title, String image, int episodeCount, String status, int episodesWatched, float personalRating) {
        this.id = id;
        this.malId = malId;
        this.title = title;
        this.image = image;
        this.episodeCount = episodeCount;
        this.status = status;
        this.episodesWatched = episodesWatched;
        this.personalRating = personalRating;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public int getMalId() {
        return malId;
    }

    public void setMalId(int malId) {
        this.malId = malId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getEpisodeCount() {
        return episodeCount;
    }

    public void setEpisodeCount(int episodeCount) {
        this.episodeCount = episodeCount;
    }

    public int getEpisodesWatched() {
        return episodesWatched;
    }

    public void setEpisodesWatched(int episodesWatched) {
        this.episodesWatched = episodesWatched;
    }

    public float getPersonalRating() {
        return personalRating;
    }

    public void setPersonalRating(float personalRating) {
        this.personalRating = personalRating;
    }
}
