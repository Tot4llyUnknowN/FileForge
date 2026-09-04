package com.fileforge.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class TextController {

    @FXML
    private TextArea inputArea;

    @FXML
    private TextArea resultArea;

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
    }
}