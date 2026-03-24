package com.zuhair.animetracker.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.zuhair.animetracker.models.Watchlist;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private MongoDatabase database = DatabaseConnection.getInstance().getDatabase();


    public void addToWatchlist(Watchlist watchlist){
        MongoCollection<Document> collection = database.getCollection("watchlist");
        Document doc = new Document("_id", watchlist.getId())
                .append("malId", watchlist.getMalId())
                .append("title", watchlist.getTitle())
                .append("image", watchlist.getImage())
                .append("episodeCount", watchlist.getEpisodeCount())
                .append("status", watchlist.getStatus())
                .append("episodesWatched", watchlist.getEpisodesWatched())
                .append("personalRating" , watchlist.getPersonalRating());
        collection.insertOne(doc);
    }

    public List<Watchlist> getAllWatchlist(){
        MongoCollection<Document> collection = database.getCollection("watchlist");
        List<Watchlist> watchlists = new ArrayList<>();
        for (Document doc : collection.find()) {
            Watchlist w = new Watchlist(
                     doc.getObjectId("_id"),
                     doc.getInteger("malId"),
                     doc.getString("title"),
                    doc.getString("image"),
                    doc.getInteger("episodeCount"),
                    doc.getString("status"),
                    doc.getInteger("episodesWatched"),
                    ((Double) doc.get("personalRating")).floatValue()
            );
            watchlists.add(w);
        }
        return watchlists;
    }

    public Watchlist getRecentlyAdded(){
        MongoCollection<Document> collection = database.getCollection("watchlist");
        Document doc = collection.find()
                .sort(new Document("_id", -1))
                .limit(1)
                .first();

        if(doc == null){
            return null;
        }

        Watchlist w = new Watchlist(
                doc.getObjectId("_id"),
                doc.getInteger("malId"),
                doc.getString("title"),
                doc.getString("image"),
                doc.getInteger("episodeCount"),
                doc.getString("status"),
                doc.getInteger("episodesWatched"),
                ((Double) doc.get("personalRating")).floatValue()
        );
        return w;
    }

    public void updateStatus(ObjectId id , String newStatus){
        MongoCollection<Document> collection = database.getCollection("watchlist");
        collection.updateOne(
                new Document("_id", id),
                new Document("$set", new Document("status", newStatus))
        );
    }

    public void updateEpisodesWatched(ObjectId id , int newEpisodeCount){
        MongoCollection<Document> collection = database.getCollection("watchlist");
        collection.updateOne(
                new Document("_id" , id),
                new Document("$set", new Document("episodesWatched" , newEpisodeCount)) // ← fix here
        );
    }

    public void deleteFromWatchlist(ObjectId id){
        MongoCollection<Document> collection = database.getCollection("watchlist");
        collection.deleteOne(new Document("_id", id));
    }

    public void updatePersonalRating(ObjectId id, float rating) {
        MongoCollection<Document> collection = database.getCollection("watchlist");
        collection.updateOne(
                new Document("_id", id),
                new Document("$set", new Document("personalRating", rating))
        );
    }

    public boolean isInWatchlist(int malId){
        MongoCollection<Document> collection = database.getCollection("watchlist");
        Document found = collection.find(new Document("malId" , malId)).first();
        return found != null;
    }
}
