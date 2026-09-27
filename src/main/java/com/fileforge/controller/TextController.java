package com.fileforge.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fileforge.util.AppExecutor;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class TextController {

    @FXML
    private TextArea inputArea;

    @FXML
    private TextArea resultArea;

    @FXML
    private Label translateStatusLabel;

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final String TARGET_LANG_CODE = "bn"; // Bangla
    private static final double MIN_MATCH_CONFIDENCE = 0.5;

    @FXML
    private void wordCount() {
        String text = inputArea.getText();
        if (text == null || text.isBlank()) {
            resultArea.setText("Word count: 0");
            return;
        }
        String[] words = text.trim().split("\\s+");
        resultArea.setText("Word count: " + words.length);
    }

    @FXML
    private void charCount() {
        String text = inputArea.getText();
        int count = (text == null) ? 0 : text.length();
        resultArea.setText("Character count: " + count);
    }

    @FXML
    private void titleCase() {
        String text = inputArea.getText();
        if (text == null || text.isBlank()) {
            resultArea.setText("");
            return;
        }
        StringBuilder result = new StringBuilder();
        String[] words = text.split(" ", -1);
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase());
            }
            if (i < words.length - 1) {
                result.append(" ");
            }
        }
        resultArea.setText(result.toString());
    }

    @FXML
    private void upperCase() {
        String text = inputArea.getText();
        resultArea.setText(text == null ? "" : text.toUpperCase());
    }

    @FXML
    private void lowerCase() {
        String text = inputArea.getText();
        resultArea.setText(text == null ? "" : text.toLowerCase());
    }

    @FXML
    private void copyResult() {
        String text = resultArea.getText();
        if (text == null || text.isBlank()) {
            return;
        }
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
    }

    @FXML
    private void clearAll() {
        inputArea.clear();
        resultArea.clear();
        if (translateStatusLabel != null) {
            translateStatusLabel.setText("");
        }
    }


    @FXML
    private void translateText() {
        String text = inputArea.getText();
        if (text == null || text.isBlank()) {
            translateStatusLabel.setText("Enter some text to translate.");
            return;
        }

        translateStatusLabel.setText("Translating to Bangla...");

        Task<String> translationTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
                String url = "https://api.mymemory.translated.net/get?q="
                        + encodedText + "&langpair=en%7C" + TARGET_LANG_CODE;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Accept-Charset", "UTF-8")
                        .GET()
                        .build();

                HttpResponse<String> response =
                        HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

                if (response.statusCode() != 200) {
                    throw new RuntimeException("API returned status " + response.statusCode());
                }

                JsonNode root = JSON_MAPPER.readTree(response.body());
                JsonNode dataNode = root.path("responseData");
                JsonNode translatedNode = dataNode.path("translatedText");
                double matchScore = dataNode.path("match").asDouble(0.0);

                if (translatedNode.isMissingNode() || translatedNode.asText().isBlank()) {
                    throw new RuntimeException("No translation returned by the API.");
                }

                if (matchScore < MIN_MATCH_CONFIDENCE) {
                    throw new RuntimeException(
                            "Low-confidence match (" + matchScore + ") — try a fuller sentence.");
                }

                return translatedNode.asText();
            }
        };

        translationTask.setOnSucceeded(e -> {
            String translated = translationTask.getValue();
            resultArea.setText(translated);
            translateStatusLabel.setText("Translated to Bangla successfully.");
        });

        translationTask.setOnFailed(e -> {
            Throwable ex = translationTask.getException();
            Platform.runLater(() ->
                    translateStatusLabel.setText("Translation failed: "
                            + (ex != null ? ex.getMessage() : "unknown error")));
        });

        AppExecutor.get().submit(translationTask);
    }
}