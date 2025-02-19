package com.example.algo_huffman;


import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

/**
 * This JavaFX application facilitates file compression and decompression using Huffman Coding, a popular
 * lossless data compression algorithm. Through an interactive graphical user interface, users can choose
 * files to compress into Huffman encoded formats or decompress previously encoded files. The application
 * provides visual feedback through animations and is equipped with error handling to ensure robust file
 * operations. It aims to deliver a user-friendly experience while showcasing the efficiency of Huffman
 * Coding for reducing file sizes.
 */
public class Main extends Application {
    File file; // To hold the reference to the selected file

    public void start(Stage stage) throws Exception {
        BorderPane bp = new BorderPane(); // Main layout pane
        // Add a background image
        Image backgroundImage = new Image("file:///C:/Users/user/IdeaProjects/Algo_huffman/src/main/resources/com/example/algo_huffman/WhatsApp Image 2024-11-11 at 20.58.58_b02e5a96.jpg");
        BackgroundImage bgImage = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, false, true)
        );
        bp.setBackground(new Background(bgImage));

        Image logoImage = new Image("file:///C:/Users/user/IdeaProjects/Algo_huffman/src/main/resources/com/example/algo_huffman/compressed-file.png");
        ImageView logoView = new ImageView(logoImage);
        logoView.setFitHeight(logoImage.getHeight() / 2);
        logoView.setFitWidth(logoImage.getWidth() / 2);

        VBox vBox = new VBox(10, logoView);
        vBox.setAlignment(Pos.CENTER);
        bp.setCenter(vBox); // Center the VBox in the border pane

        Button compressButton = new Button("Compress File");
        compressButton.setStyle("-fx-font-size: 18px; -fx-background-color: #2a2d85; -fx-text-fill: white;");
        Button decompressButton = new Button("Decompress File");
        decompressButton.setStyle("-fx-font-size: 18px; -fx-background-color: #2a2d85; -fx-text-fill: white;");
        HBox optionsBox = new HBox(10, compressButton, decompressButton);
        optionsBox.setAlignment(Pos.CENTER);
        bp.setBottom(optionsBox); // Set the HBox with buttons at the bottom of the border pane

        Scene scene = new Scene(bp, 1200, 600);
        bp.setPadding(new Insets(15, 15, 15, 15)); // Padding around the border pane

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("File Chooser");

        // Event handler for compressing files
        compressButton.setOnAction(e -> {
            ExtensionFilter filterAll = new ExtensionFilter("Text files", "*");
            ExtensionFilter filterTXT = new ExtensionFilter("Text files", "*.txt");
            fileChooser.getExtensionFilters().clear();
            fileChooser.getExtensionFilters().addAll(filterTXT, filterAll);

            file = fileChooser.showOpenDialog(stage);

            try {
                if (file == null) {
                    return; // No file was selected, just exit
                }

                // Check if the file extension is ".huff"
                if (file.getName().toLowerCase().endsWith(".huff")) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Invalid File");
                    alert.setContentText("You cannot select a .huff file for compression. Please choose a valid file.");
                    alert.showAndWait();
                    return; // Stop further execution
                }

                if (file.length() == 0) throw new IOException(); // Check if file is empty

                // Proceed to compression
                Compress compressScene = new Compress(stage, scene, file);
                stage.setScene(compressScene);

            } catch (Exception e2) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("File is empty, try another valid file");
                alert.showAndWait();
            }
        });

        // Event handler for decompressing files
        decompressButton.setOnAction(e -> {
            ExtensionFilter filterHUFF = new ExtensionFilter("Text files", "*huff");
            fileChooser.getExtensionFilters().clear();
            fileChooser.getExtensionFilters().add(filterHUFF);

            file = fileChooser.showOpenDialog(stage);
            try {
                if (file.length() == 0) throw new IOException();
                Decompress decompressScene = new Decompress(stage, scene, file);
                stage.setScene(decompressScene);
            } catch (Exception e2) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("File is empty, try another valid file");
                alert.showAndWait();
            }
        });

        stage.setScene(scene);
        stage.setTitle("Huffman Compression");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}