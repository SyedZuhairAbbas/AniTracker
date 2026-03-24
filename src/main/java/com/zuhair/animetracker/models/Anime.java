package com.zuhair.animetracker.models;

public class Anime {
    private String title;
    private int episodeCount;
    private float rating;
    private String image;
    private int malId;
    private String synopsis;

    public Anime(String title , int episodeCount , float rating , String image , int malId , String synopsis){
        this.title = title;
        this.episodeCount = episodeCount;
        this.rating = rating;
        this.image = image;
        this.malId = malId;
        this.synopsis = synopsis;
    }


    public String getTitle(){return title;}
    public void setTitle(String title){this.title = title;}

    public int getEpisodeCount(){return episodeCount;}
    public void setEpisodeCount(int episodeCount){this.episodeCount = episodeCount;}

    public float getRating(){return rating;}
    public void setRating(float rating){this.rating = rating;}

    public String getImage(){return image;}
    public void setImage(String image){this.image = image;}

    public int getMalId(){return malId;}
    public void setMalId(int malId){this.malId = malId;}

    public String getSynopsis(){return synopsis;}
    public void setSynopsis(String synopsis){this.synopsis = synopsis;}
}

