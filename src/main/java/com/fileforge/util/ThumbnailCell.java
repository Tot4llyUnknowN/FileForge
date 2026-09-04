package com.fileforge.util;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;

import java.util.function.Function;

/**
 * A ListCell showing a thumbnail image + caption. Supports optional drag-and-drop
 * reordering within its own ListView, and optional external selection highlighting
 * (re-evaluated on every updateItem, so it survives cell recycling during scrolling).
 */
public class ThumbnailCell<T> extends ListCell<T> {

    private final Function<T, Image> thumbnailProvider;
    private final Function<T, String> captionProvider;
    private final ObservableList<T> backingList;

    private Function<T, Boolean> selectedProvider; // nullable — set via setSelectedProvider

    private final ImageView imageView = new ImageView();
    private final Label captionLabel = new Label();
    private final VBox container = new VBox(4);

    public ThumbnailCell(Function<T, Image> thumbnailProvider,
                         Function<T, String> captionProvider,
                         ObservableList<T> backingList,
                         boolean draggable) {
        this.thumbnailProvider = thumbnailProvider;
        this.captionProvider = captionProvider;
        this.backingList = backingList;

        imageView.setFitWidth(90);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        container.setAlignment(Pos.CENTER);
        container.getChildren().addAll(imageView, captionLabel);
        container.getStyleClass().add("thumbnail-cell");

        if (draggable) {
            setupDragAndDrop();
        }
    }

    public void setSelectedProvider(Function<T, Boolean> selectedProvider) {
        this.selectedProvider = selectedProvider;
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
            setText(null);
            return;
        }

        Image thumbnail = thumbnailProvider.apply(item);
        imageView.setImage(thumbnail);
        captionLabel.setText(captionProvider.apply(item));

        boolean selected = selectedProvider != null && selectedProvider.apply(item);
        applySelectedStyle(selected);

        setGraphic(container);
        setText(null);
    }

    private void applySelectedStyle(boolean selected) {
        if (selected) {
            if (!container.getStyleClass().contains("thumbnail-selected")) {
                container.getStyleClass().add("thumbnail-selected");
            }
        } else {
            container.getStyleClass().remove("thumbnail-selected");
        }
    }

    private void setupDragAndDrop() {
        setOnDragDetected(event -> {
            if (getItem() == null) return;
            Dragboard db = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(getIndex()));
            db.setContent(content);
            event.consume();
        });

        setOnDragOver(event -> {
            if (event.getGestureSource() != this && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        setOnDragEntered(event -> {
            if (event.getGestureSource() != this && event.getDragboard().hasString()) {
                container.setOpacity(0.5);
            }
        });

        setOnDragExited(event -> container.setOpacity(1.0));

        setOnDragDropped(event -> {
            if (getItem() == null) {
                event.setDropCompleted(false);
                event.consume();
                return;
            }

            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString()) {
                int draggedIndex = Integer.parseInt(db.getString());
                int thisIndex = getIndex();

                T draggedItem = backingList.get(draggedIndex);
                backingList.remove(draggedIndex);
                backingList.add(thisIndex > draggedIndex ? thisIndex - 1 : thisIndex, draggedItem);

                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });

        setOnDragDone(event -> event.consume());
    }
}