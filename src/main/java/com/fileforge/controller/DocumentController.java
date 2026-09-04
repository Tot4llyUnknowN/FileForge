package com.fileforge.controller;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

public class DocumentController {

    @FXML private void goToDocInfo(MouseEvent e) { nav(e, "/view/document/DocInfoView.fxml"); }
    @FXML private void goToMerger(MouseEvent e) { nav(e, "/view/document/PdfMergerView.fxml"); }
    @FXML private void goToSplit(MouseEvent e) { nav(e, "/view/document/SplitPdfView.fxml"); }
    @FXML private void goToImageToPdf(MouseEvent e) { nav(e, "/view/document/ImageToPdfView.fxml"); }
    @FXML private void goToPdfToImage(MouseEvent e) { nav(e, "/view/document/PdfToImageView.fxml"); }
    @FXML private void goToDeletePages(MouseEvent e) { nav(e, "/view/document/DeletePagesView.fxml"); }
    @FXML private void goToReorderPages(MouseEvent e) { nav(e, "/view/document/ReorderPagesView.fxml"); }
    @FXML private void goToLockPdf(MouseEvent e) { nav(e, "/view/document/LockPdfView.fxml"); }
    @FXML private void goToUnlockPdf(MouseEvent e) { nav(e, "/view/document/UnlockPdfView.fxml"); }
    @FXML private void goToExcelToCsv(MouseEvent e) { nav(e, "/view/document/ExcelToCsvView.fxml"); }

    private void nav(MouseEvent event, String path) {
        NavigationHelper.navigate((javafx.scene.Node) event.getSource(), path);
    }
}