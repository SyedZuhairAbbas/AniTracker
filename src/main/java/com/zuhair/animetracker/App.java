package com.zuhair.animetracker;

import com.mongodb.client.MongoDatabase;
import com.zuhair.animetracker.database.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/NewMainWindow.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.setTitle("AniTracker");
        stage.setMaximized(true);
        Image icon = new Image(getClass().getResourceAsStream("/images/Icon.png"));
        stage.getIcons().add(icon);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
