package com.fileforge.controller;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

public class ImageController {

    @FXML private void goToJpgToPng(MouseEvent e) { nav(e, "/view/image/JpgToPngView.fxml"); }
    @FXML private void goToPngToJpg(MouseEvent e) { nav(e, "/view/image/PngToJpgView.fxml"); }
    @FXML private void goToWebpToJpg(MouseEvent e) { nav(e, "/view/image/WebpToJpgView.fxml"); }
    @FXML private void goToImageToPdf(MouseEvent e) { navShared(e, "/view/document/ImageToPdfView.fxml"); }
    @FXML private void goToPdfToImage(MouseEvent e) { navShared(e, "/view/document/PdfToImageView.fxml"); }

    private void navShared(MouseEvent event, String path) {
        NavigationHelper.navigate((javafx.scene.Node) event.getSource(), path, "/view/ImageView.fxml");
    }
    @FXML private void goToResize(MouseEvent e) { nav(e, "/view/image/ResizeImageView.fxml"); }
    @FXML private void goToCompress(MouseEvent e) { nav(e, "/view/image/CompressImageView.fxml"); }
    @FXML private void goToCrop(MouseEvent e) { nav(e, "/view/image/CropImageView.fxml"); }
    @FXML private void goToRotateFlip(MouseEvent e) { nav(e, "/view/image/RotateFlipView.fxml"); }
    @FXML private void goToMetadata(MouseEvent e) { nav(e, "/view/image/MetadataViewerView.fxml"); }

    private void nav(MouseEvent event, String path) {
        NavigationHelper.navigate((javafx.scene.Node) event.getSource(), path);
    }
}