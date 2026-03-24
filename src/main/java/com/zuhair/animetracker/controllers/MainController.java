package com.zuhair.animetracker.controllers;

import com.zuhair.animetracker.api.JikanApi;
import com.zuhair.animetracker.database.DatabaseHelper;
import com.zuhair.animetracker.models.Anime;
import com.zuhair.animetracker.models.Watchlist;
import javafx.animation.FadeTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.bson.types.ObjectId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.control.ProgressBar;

public class MainController {
    @FXML private AnchorPane dashboardPane;
    @FXML private AnchorPane searchPane;
    @FXML private AnchorPane libraryPane;
    @FXML private Button dashboardBtn;
    @FXML private Button searchBtn;
    @FXML private Button libraryBtn;
    @FXML private FlowPane searchResults;
    @FXML private TextField searchField;
    @FXML private Button searchFieldBtn;
    @FXML private VBox libraryResults;
    @FXML private Button planToWatchBtn;
    @FXML private Button watchingBtn;
    @FXML private Button completedBtn;
    @FXML private Button droppedBtn;
    @FXML private FlowPane continueWatchingPane;
    @FXML private BorderPane rootPane;
    @FXML private VBox topAiringPane;
    @FXML private HBox bannerBox;
    @FXML private ScrollPane dashboardScroll;
    @FXML private Label totalShowsLabel;
    @FXML private Label totalEpisodesLabel;
    @FXML private Label meanScoreLabel;
    @FXML private VBox chartContainer;
    @FXML private TextField librarySearchField;

    @FXML
    public void initialize() {
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                searchResults();
            }
        });
        loadDashboard();
        dashboardBtn.getStyleClass().add("sidebar-btn-active");
    }

    private List<Watchlist> allWatchlistEntries = new ArrayList<>();
    private JikanApi jikanApi = new JikanApi();
    private DatabaseHelper databaseHelper = new DatabaseHelper();
    private String currentTab = "Plan to watch";

    private void showPane(AnchorPane pane){
        dashboardPane.setVisible(false);
        dashboardPane.setManaged(false);
        searchPane.setVisible(false);
        searchPane.setManaged(false);
        libraryPane.setVisible(false);
        libraryPane.setManaged(false);
        pane.setVisible(true);
        pane.setManaged(true);
        dashboardBtn.getStyleClass().remove("sidebar-btn-active");
        searchBtn.getStyleClass().remove("sidebar-btn-active");
        libraryBtn.getStyleClass().remove("sidebar-btn-active");

        if (pane == dashboardPane) dashboardBtn.getStyleClass().add("sidebar-btn-active");
        else if (pane == searchPane) searchBtn.getStyleClass().add("sidebar-btn-active");
        else if (pane == libraryPane) libraryBtn.getStyleClass().add("sidebar-btn-active");
    }

    @FXML
    public void dashboardBtnHandler(){
        showPane(dashboardPane);
        loadDashboard();
    }

    @FXML
    public void searchBtnHandler() {
        showPane(searchPane);
        if (searchResults.getChildren().isEmpty()) {
            Label loading = new Label("Loading trending anime...");
            loading.setStyle("-fx-text-fill: #8b8fa8; -fx-font-size: 14px;");
            searchResults.getChildren().add(loading);

            Task<List<Anime>> trendingTask = new Task<>() {
                @Override
                protected List<Anime> call() throws Exception {
                    return jikanApi.fetchTrending();
                }
            };
            trendingTask.setOnSucceeded(e -> {
                searchResults.getChildren().clear();
                for (Anime anime : trendingTask.getValue()) {
                    searchResults.getChildren().add(createAnimeCard(anime));
                }
            });
            trendingTask.setOnFailed(e -> {
                searchResults.getChildren().clear();
                Label error = new Label("Failed to load trending.");
                error.setStyle("-fx-text-fill: #ff1744; -fx-font-size: 14px;");
                searchResults.getChildren().add(error);
            });
            new Thread(trendingTask).start();
        }
    }

    @FXML
    public void libraryBtnHandler(){
        showPane(libraryPane);
        loadLibrary();
    }

    @FXML public void planToWatchHandler(){
        currentTab = "Plan to watch";
        showTab(currentTab);
    }

    @FXML public void watchingHandler(){
        currentTab = "Watching";
        showTab(currentTab);
    }
    @FXML public void completedHandler(){
        currentTab = "Completed";
        showTab(currentTab);
    }
    @FXML public void droppedHandler(){
        currentTab = "Dropped";
        showTab(currentTab);
    }

    @FXML
    public void searchResults(){
        String searchText = searchField.getText();

        // Show loading
        searchResults.getChildren().clear();
        Label loading = new Label("Searching...");
        loading.setStyle("-fx-text-fill: #8b8fa8; -fx-font-size: 14px;");
        searchResults.getChildren().add(loading);

        Task<List<Anime>> searchTask = new Task<>() {
            @Override
            protected List<Anime> call() throws Exception {
                return jikanApi.searchAnime(searchText);
            }
        };

        searchTask.setOnSucceeded(e -> {
            searchResults.getChildren().clear();
            List<Anime> results = searchTask.getValue();
            for (Anime anime : results) {
                searchResults.getChildren().add(createAnimeCard(anime));
            }
        });

        searchTask.setOnFailed(e -> {
            searchResults.getChildren().clear();
            Label error = new Label("Search failed. Check your connection.");
            error.setStyle("-fx-text-fill: #ff1744; -fx-font-size: 14px;");
            searchResults.getChildren().add(error);
        });

        new Thread(searchTask).start();
    }

    private VBox createAnimeCard(Anime anime) {
        VBox card = new VBox();
        Image image = new Image(anime.getImage(), true); // true = load in background
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(150);
        imageView.setFitHeight(220);
        Label title = new Label();
        title.setText(anime.getTitle());
        Label score = new Label();
        score.setText("Score: " + anime.getRating());
        Button addToWatchlist = new Button();
        addToWatchlist.setText("Add to Watchlist");
        addToWatchlist.setOnAction(e -> {
            if (databaseHelper.isInWatchlist(anime.getMalId())) {
                showToast("Already in Watchlist!", "#f5c518");
                return;
            }
            Watchlist entry = new Watchlist(
                    ObjectId.get(),
                    anime.getMalId(),
                    anime.getTitle(),
                    anime.getImage(),
                    anime.getEpisodeCount(),
                    "Plan to watch",
                    0,
                    0
            );
            databaseHelper.addToWatchlist(entry);
            showToast("✓ Added to Watchlist!", "#00e676");
        });
        card.getChildren().addAll(imageView , title , score , addToWatchlist);
        card.getStyleClass().add("anime-card");
        title.getStyleClass().add("card-title");
        score.getStyleClass().add("card-score");
        addToWatchlist.getStyleClass().add("card-button");
        return card;
    }

    public void loadLibrary() {
        allWatchlistEntries = databaseHelper.getAllWatchlist();
        showTab(currentTab);
        librarySearchField.setOnKeyReleased(e -> showTab(currentTab));
        loadStats();
    }

    public HBox createLibraryCard(Watchlist entry) {
        HBox card = new HBox(15);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #1e2130; -fx-background-radius: 10; -fx-padding: 10;");
        card.setMinHeight(90);
        card.setMaxWidth(Double.MAX_VALUE);

        // Thumbnail
        ImageView imageView = new ImageView(new Image(entry.getImage(), true));
        imageView.setFitWidth(60);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(true);

        // Episode count — declared FIRST so titleBox can use it
        Label episodeCount = new Label("Ep " + entry.getEpisodesWatched() + " / " + entry.getEpisodeCount());
        episodeCount.setStyle("-fx-text-fill: #8b8fa8; -fx-font-size: 12px;");
        episodeCount.setMinWidth(80);

        // Title + Progress Bar
        VBox titleBox = new VBox(5);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Label title = new Label(entry.getTitle());
        title.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        title.setWrapText(true);

        double progress = entry.getEpisodeCount() == 0 ? 0 :
                (double) entry.getEpisodesWatched() / entry.getEpisodeCount();
        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-accent: #4f8ef7;");

        HBox starRating = createStarRating(entry);
        titleBox.getChildren().addAll(title, progressBar, episodeCount, starRating); // ✅ all declared

        // Status dropdown
        ComboBox<String> statusDropDown = new ComboBox<>();
        statusDropDown.getItems().addAll("Plan to watch", "Watching", "Completed", "Dropped");
        statusDropDown.setValue(entry.getStatus());
        statusDropDown.setOnAction(e -> {
            String newStatus = statusDropDown.getValue();
            databaseHelper.updateStatus(entry.getId(), newStatus);
            if (newStatus.equals("Completed") && entry.getEpisodeCount() > 0) {
                databaseHelper.updateEpisodesWatched(entry.getId(), entry.getEpisodeCount());
                entry.setEpisodesWatched(entry.getEpisodeCount());
            }
            loadLibrary();
        });

        // +1 button
        Button addOneEpisode = new Button("+1");
        addOneEpisode.getStyleClass().add("card-button");
        addOneEpisode.setOnAction(e -> {
            if (entry.getEpisodeCount() > 0 && entry.getEpisodesWatched() >= entry.getEpisodeCount()) return;
            int episode = entry.getEpisodesWatched() + 1;
            databaseHelper.updateEpisodesWatched(entry.getId(), episode);
            entry.setEpisodesWatched(episode);
            episodeCount.setText("Ep " + episode + " / " + entry.getEpisodeCount());
            double newProgress = entry.getEpisodeCount() == 0 ? 0 :
                    (double) episode / entry.getEpisodeCount();
            progressBar.setProgress(newProgress);
        });

        // -1 button
        Button minusOneEpisode = new Button("-1");
        minusOneEpisode.setStyle("-fx-background-color: #2a2d3e; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
        minusOneEpisode.setOnAction(e -> {
            if (entry.getEpisodesWatched() <= 0) return;
            int episode = entry.getEpisodesWatched() - 1;
            databaseHelper.updateEpisodesWatched(entry.getId(), episode);
            entry.setEpisodesWatched(episode);
            episodeCount.setText("Ep " + episode + " / " + entry.getEpisodeCount());
            double newProgress = entry.getEpisodeCount() == 0 ? 0 :
                    (double) episode / entry.getEpisodeCount();
            progressBar.setProgress(newProgress);
        });

        // Delete button
        Button delete = new Button("✕");
        delete.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
        delete.setOnAction(e -> {
            databaseHelper.deleteFromWatchlist(entry.getId());
            showToast("✕ Removed from Watchlist", "#ff1744");
            loadLibrary();
        });

        card.getChildren().addAll(imageView, titleBox, statusDropDown, minusOneEpisode, addOneEpisode, delete);
        return card;
    }

    public void showTab(String status) {
        planToWatchBtn.getStyleClass().remove("tab-active");
        watchingBtn.getStyleClass().remove("tab-active");
        completedBtn.getStyleClass().remove("tab-active");
        droppedBtn.getStyleClass().remove("tab-active");

        switch (status) {
            case "Plan to watch" -> planToWatchBtn.getStyleClass().add("tab-active");
            case "Watching" -> watchingBtn.getStyleClass().add("tab-active");
            case "Completed" -> completedBtn.getStyleClass().add("tab-active");
            case "Dropped" -> droppedBtn.getStyleClass().add("tab-active");
        }

        String query = librarySearchField.getText().toLowerCase().trim();

        libraryResults.getChildren().clear();
        List<Watchlist> filtered = allWatchlistEntries.stream()
                .filter(w -> w.getStatus().equals(status))
                .filter(w -> query.isEmpty() || w.getTitle().toLowerCase().contains(query))
                .collect(Collectors.toList());

        for (Watchlist entry : filtered) {
            libraryResults.getChildren().add(createLibraryCard(entry));
        }
    }


    public void loadDashboard() {
        // Continue Watching from MongoDB
        List<Watchlist> all = databaseHelper.getAllWatchlist();
        List<Watchlist> watching = all.stream()
                .filter(w -> w.getStatus().equals("Watching"))
                .collect(Collectors.toList());
        continueWatchingPane.getChildren().clear();
        for (Watchlist entry : watching) {
            continueWatchingPane.getChildren().add(createContinueWatchingCard(entry));
        }

        // Fetch banner + top airing from API
        Task<List<Anime>> fetchBanner = new Task<>() {
            @Override
            protected List<Anime> call() throws Exception {
                return jikanApi.fetchTopAiring();
            }
        };

        fetchBanner.setOnSucceeded(e -> {
            List<Anime> results = fetchBanner.getValue();
            if (results.isEmpty()) return;
            Anime featured = results.get(0);

            // Build banner
            bannerBox.getChildren().clear();
            bannerBox.setStyle(
                    "-fx-background-color: linear-gradient(to right, #1a3a6e, #2a5298, #1a1d2e);" +
                            "-fx-background-radius: 14;" +
                            "-fx-padding: 25;"
            );
            bannerBox.setMinHeight(200);
            bannerBox.setPrefHeight(200);
            bannerBox.setSpacing(20);
            bannerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            // Left text content
            VBox textBox = new VBox(8);
            HBox.setHgrow(textBox, Priority.ALWAYS);

            Label featuredLabel = new Label("Featured This Season");
            featuredLabel.setStyle("-fx-text-fill: #a0c4ff; -fx-font-size: 12px;");

            Label titleLabel = new Label(featured.getTitle());
            titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");
            titleLabel.setWrapText(true);

            String synopsis = featured.getSynopsis().length() > 150
                    ? featured.getSynopsis().substring(0, 150) + "..."
                    : featured.getSynopsis();
            Label synopsisLabel = new Label(synopsis);
            synopsisLabel.setStyle("-fx-text-fill: #8b8fa8; -fx-font-size: 12px;");
            synopsisLabel.setWrapText(true);
            synopsisLabel.setMaxWidth(400);

            // Star rating
            float score = featured.getRating();
            int stars = Math.round(score / 2);
            StringBuilder starStr = new StringBuilder();
            for (int i = 0; i < 5; i++) starStr.append(i < stars ? "★" : "☆");
            Label starLabel = new Label(starStr.toString());
            starLabel.setStyle("-fx-text-fill: #f5c518; -fx-font-size: 16px;");

            textBox.getChildren().addAll(featuredLabel, titleLabel, synopsisLabel, starLabel);

            // Right image with clip
            ImageView bannerImage = new ImageView(new Image(featured.getImage(), true));
            bannerImage.setFitHeight(200);
            bannerImage.setFitWidth(200);
            bannerImage.setPreserveRatio(false);

            // Clip to rounded rectangle
            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(200, 200);
            clip.setArcWidth(14);
            clip.setArcHeight(14);
            bannerImage.setClip(clip);

        // Stack image with gradient overlay for smooth transition
            javafx.scene.shape.Rectangle gradientOverlay = new javafx.scene.shape.Rectangle(200, 200);
            gradientOverlay.setFill(new javafx.scene.paint.LinearGradient(
                    0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                    new javafx.scene.paint.Stop(0, javafx.scene.paint.Color.web("#1a3a6e", 1.0)),
                    new javafx.scene.paint.Stop(0.3, javafx.scene.paint.Color.web("#1a3a6e", 0.0))
            ));

            StackPane imageStack = new StackPane(bannerImage, gradientOverlay);
            bannerBox.getChildren().addAll(textBox, imageStack);

            // Top Airing sidebar
            topAiringPane.getChildren().clear();
            for (int i = 1; i < results.size(); i++) {
                topAiringPane.getChildren().add(createTopAiringCard(results.get(i)));
            }
        });

        fetchBanner.setOnFailed(e -> System.out.println("Banner failed: " + fetchBanner.getException()));
        new Thread(fetchBanner).start();
    }

    private HBox createContinueWatchingCard(Watchlist entry) {
        HBox card = new HBox(10);
        card.setStyle("-fx-background-color: #1e2130; -fx-background-radius: 10; -fx-padding: 10;");
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setPrefWidth(420);
        card.setMaxWidth(420);

        ImageView imageView = new ImageView(new Image(entry.getImage(), true));
        imageView.setFitWidth(75);
        imageView.setFitHeight(95);
        imageView.setPreserveRatio(true);

        VBox info = new VBox(5);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label title = new Label(entry.getTitle());
        title.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
        title.setWrapText(true);
        title.setMaxWidth(200);

        double progress = entry.getEpisodeCount() == 0 ? 0 :
                (double) entry.getEpisodesWatched() / entry.getEpisodeCount();
        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(8);
        progressBar.setMinHeight(8);
        progressBar.setStyle("-fx-accent: #4f8ef7;");

        Label epLabel = new Label("Ep " + entry.getEpisodesWatched() + (entry.getEpisodeCount() == 0 ? "" : " / " + entry.getEpisodeCount()));
        epLabel.setStyle("-fx-text-fill: #8b8fa8; -fx-font-size: 11px;");

        // Star rating
        float score = entry.getPersonalRating();
        int stars = Math.round(score / 2);
        StringBuilder starStr = new StringBuilder();
        for (int i = 0; i < 5; i++) starStr.append(i < stars ? "★" : "☆");
        Label starLabel = new Label(starStr.toString());
        starLabel.setStyle("-fx-text-fill: #f5c518; -fx-font-size: 13px;");

        info.getChildren().addAll(title, progressBar, epLabel, starLabel);
        card.getChildren().addAll(imageView, info);
        return card;
    }

    public VBox createTopAiringCard(Anime anime) {
        HBox card = new HBox(10);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #252840; -fx-background-radius: 8; -fx-padding: 8;");
        card.setMaxWidth(Double.MAX_VALUE);

        ImageView imageView = new ImageView(new Image(anime.getImage(), true));
        imageView.setFitWidth(45);
        imageView.setFitHeight(60);
        imageView.setPreserveRatio(true);

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label title = new Label(anime.getTitle());
        title.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;");
        title.setWrapText(true);

        float score = anime.getRating();
        int stars = Math.round(score / 2);
        StringBuilder starStr = new StringBuilder();
        for (int i = 0; i < 5; i++) starStr.append(i < stars ? "★" : "☆");
        Label starLabel = new Label(starStr.toString() + "  " + score);
        starLabel.setStyle("-fx-text-fill: #f5c518; -fx-font-size: 11px;");

        Label epLabel = new Label(anime.getEpisodeCount() == 0 ? "Ongoing" : anime.getEpisodeCount() + " Episodes");
        epLabel.setStyle("-fx-text-fill: #8b8fa8; -fx-font-size: 10px;");

        info.getChildren().addAll(title, starLabel, epLabel);
        card.getChildren().addAll(imageView, info);

        // Wrap in VBox to return VBox type
        VBox wrapper = new VBox(card);
        return wrapper;
    }

    private void showToast(String message, String color) {
        Label toast = new Label(message);
        toast.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + color + ";" +
                        "-fx-background-color: #1e2130;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 12 20 12 20;"
        );

        AnchorPane currentPane;
        if (searchPane.isVisible()) currentPane = searchPane;
        else if (libraryPane.isVisible()) currentPane = libraryPane;
        else currentPane = dashboardPane;

        currentPane.getChildren().add(toast);
        AnchorPane.setBottomAnchor(toast, 30.0);
        AnchorPane.setRightAnchor(toast, 30.0);
        toast.toFront();

        FadeTransition fade = new FadeTransition(Duration.seconds(1.5), toast);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setDelay(Duration.seconds(2));
        fade.setOnFinished(e -> currentPane.getChildren().remove(toast));
        fade.play();
    }


    private void loadStats() {
        List<Watchlist> all = databaseHelper.getAllWatchlist();

        int totalShows = all.size();

        int totalEpisodes = all.stream()
                .mapToInt(Watchlist::getEpisodesWatched)
                .sum();

        double meanScore = all.stream()
                .filter(w -> w.getPersonalRating() > 0)
                .mapToDouble(Watchlist::getPersonalRating)
                .average()
                .orElse(0.0);

        totalShowsLabel.setText(String.valueOf(totalShows));
        totalEpisodesLabel.setText(String.valueOf(totalEpisodes));
        meanScoreLabel.setText(String.format("%.1f", meanScore));
        buildRatingChart(all);
    }

    private HBox createStarRating(Watchlist entry) {
        HBox stars = new HBox(3);
        Label[] starLabels = new Label[5];

        int currentRating = Math.round(entry.getPersonalRating() / 2);

        for (int i = 0; i < 5; i++) {
            final int starIndex = i + 1;
            Label star = new Label(i < currentRating ? "★" : "☆");
            star.setStyle("-fx-text-fill: #f5c518; -fx-font-size: 18px; -fx-cursor: hand;");
            starLabels[i] = star;

            star.setOnMouseClicked(e -> {
                // Update all stars visually
                for (int j = 0; j < 5; j++) {
                    starLabels[j].setText(j < starIndex ? "★" : "☆");
                }
                // Save rating (starIndex * 2 converts 1-5 to 1-10 scale)
                float newRating = starIndex * 2;
                entry.setPersonalRating(newRating);
                databaseHelper.updatePersonalRating(entry.getId(), newRating);
            });

            // Hover effect
            final int idx = i;
            star.setOnMouseEntered(e -> {
                for (int j = 0; j < 5; j++) {
                    starLabels[j].setText(j <= idx ? "★" : "☆");
                }
            });
            star.setOnMouseExited(e -> {
                int rating = Math.round(entry.getPersonalRating() / 2);
                for (int j = 0; j < 5; j++) {
                    starLabels[j].setText(j < rating ? "★" : "☆");
                }
            });

            stars.getChildren().add(star);
        }
        return stars;
    }

    private void buildRatingChart(List<Watchlist> all) {
        chartContainer.getChildren().clear();

        int[] counts = new int[5];
        for (Watchlist w : all) {
            int stars = Math.round(w.getPersonalRating() / 2);
            if (stars >= 1 && stars <= 5) counts[stars - 1]++;
        }
        int max = 1;
        for (int c : counts) if (c > max) max = c;

        Label chartTitle = new Label("Rating Distribution");
        chartTitle.setStyle("-fx-text-fill: #8b8fa8; -fx-font-size: 12px;");
        chartContainer.getChildren().add(chartTitle);

        for (int i = 4; i >= 0; i--) {
            HBox row = new HBox(8);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label starLabel = new Label((i + 1) + "★");
            starLabel.setStyle("-fx-text-fill: #f5c518; -fx-font-size: 11px;");
            starLabel.setMinWidth(25);

            double barWidth = counts[i] == 0 ? 2 : (double) counts[i] / max * 140;
            javafx.scene.shape.Rectangle bar = new javafx.scene.shape.Rectangle(barWidth, 12);
            bar.setFill(javafx.scene.paint.Color.web("#4f8ef7"));
            bar.setArcWidth(4);
            bar.setArcHeight(4);

            Label countLabel = new Label(String.valueOf(counts[i]));
            countLabel.setStyle("-fx-text-fill: #8b8fa8; -fx-font-size: 11px;");

            row.getChildren().addAll(starLabel, bar, countLabel);
            chartContainer.getChildren().add(row);
        }
    }
}
