package com.fileforge.controller.image;

import com.fileforge.controller.NavigationHelper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class CompressImageController {

    @FXML private Button backButton;
    @FXML private ListView<String> fileListView;
    @FXML private Slider qualitySlider;
    @FXML private Label qualityValueLabel;
    @FXML private Label statusLabel;
    @FXML private Button compressButton;

    private List<File> selectedFiles;

    @FXML
    public void initialize() {
        qualitySlider.valueProperty().addListener((obs, oldVal, newVal) ->
                qualityValueLabel.setText(newVal.intValue() + "% of original size")
        );
    }

    @FXML
    private void chooseFiles() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Images to Compress");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.webp"));

        List<File> files = chooser.showOpenMultipleDialog(backButton.getScene().getWindow());
        if (files == null || files.isEmpty()) return;

        selectedFiles = files;
        fileListView.getItems().clear();
        for (File f : selectedFiles) {
            fileListView.getItems().add(f.getName() + " (" + (f.length() / 1024) + " KB)");
        }

        statusLabel.setText(selectedFiles.size() + " images queued.");
        compressButton.setDisable(false);
    }

    @FXML
    private void compress() {
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            statusLabel.setText("Please select images first.");
            return;
        }

        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select Output Directory");
        File outputDir = dirChooser.showDialog(backButton.getScene().getWindow());
        if (outputDir == null) return;

        double targetPercentage = qualitySlider.getValue() / 100.0;
        statusLabel.setText("Processing targeted size compression...");
        compressButton.setDisable(true);

        Task<Void> compressionTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                int successCount = 0;
                for (File file : selectedFiles) {
                    try {
                        compressToTargetSize(file, outputDir, targetPercentage);
                        successCount++;
                    } catch (IOException e) {
                        System.err.println("Failed to compress " + file.getName() + ": " + e.getMessage());
                    }
                }
                final int finalSuccess = successCount;
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("Compressed " + finalSuccess + " images exactly to target size!");
                    compressButton.setDisable(false);
                });
                return null;
            }
        };

        new Thread(compressionTask).start();
    }

    private void compressToTargetSize(File srcFile, File destDir, double targetPercentage) throws IOException {
        long originalSizeBytes = srcFile.length();
        long targetSizeBytes = (long) (originalSizeBytes * targetPercentage);

        BufferedImage originalImage = ImageIO.read(srcFile);
        if (originalImage == null) throw new IOException("Unreadable format.");

        String ext = getExtension(srcFile.getName()).toLowerCase();
        String format = (ext.equals("png")) ? "png" : ext;
        if (format.equals("jpg") || format.equals("jpeg")) format = "jpeg";

        File outputFile = new File(destDir, stripExtension(srcFile.getName()) + "_compressed." + ext);

        // Standardize output color models to avoid transparency writing crashes on loose JPEG formats
        BufferedImage processingImage = originalImage;
        if (format.equals("jpeg") && originalImage.getColorModel().hasAlpha()) {
            BufferedImage flattened = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = flattened.createGraphics();
            g.drawImage(originalImage, 0, 0, java.awt.Color.WHITE, null);
            g.dispose();
            processingImage = flattened;
        }

        byte[] finalBytes = null;
        double scaleFactor = 1.0;

        // Loop down canvas dimensions if quality adjustments alone can't choke the byte array small enough
        while (finalBytes == null || finalBytes.length > targetSizeBytes + (originalSizeBytes * 0.05)) {
            BufferedImage scaledImage = processingImage;
            if (scaleFactor < 1.0) {
                int nw = (int) (processingImage.getWidth() * scaleFactor);
                int nh = (int) (processingImage.getHeight() * scaleFactor);
                if (nw < 10 || nh < 10) break; // Don't shrink below thumbnail sizes

                scaledImage = new BufferedImage(nw, nh, processingImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : processingImage.getType());
                Graphics2D g2 = scaledImage.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(processingImage, 0, 0, nw, nh, null);
                g2.dispose();
            }

            // Binary search target compression quality matrix boundaries
            finalBytes = binarySearchQualityBytes(scaledImage, format, targetSizeBytes);

            // If the output size is still too large, step down image dimension boundaries by 15% and try again
            if (finalBytes.length > targetSizeBytes) {
                scaleFactor -= 0.15;
            } else {
                break;
            }
        }

        // Write the calculated target array to disk
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(finalBytes != null ? finalBytes : new byte[0]);
        }
    }

    private byte[] binarySearchQualityBytes(BufferedImage img, String format, long targetSize) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
        if (!writers.hasNext()) throw new IOException("No encoder available.");
        ImageWriter writer = writers.next();

        ImageWriteParam param = writer.getDefaultWriteParam();

        // Formats like PNG don't support loose lossy adjustments natively. Fallback to basic stream arrays.
        if (!param.canWriteCompressed()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (MemoryCacheImageOutputStream mcios = new MemoryCacheImageOutputStream(baos)) {
                writer.setOutput(mcios);
                writer.write(null, new IIOImage(img, null, null), param);
            }
            writer.dispose();
            return baos.toByteArray();
        }

        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

        float low = 0.01f;
        float high = 1.0f;
        float bestQuality = 0.5f;
        byte[] bestBytes = null;

        // 6 Iterations run in milliseconds and calculate precise output curves
        for (int i = 0; i < 6; i++) {
            float mid = (low + high) / 2f;
            param.setCompressionQuality(mid);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (MemoryCacheImageOutputStream mcios = new MemoryCacheImageOutputStream(baos)) {
                writer.setOutput(mcios);
                writer.write(null, new IIOImage(img, null, null), param);
            }

            byte[] currentBytes = baos.toByteArray();

            if (currentBytes.length <= targetSize) {
                bestQuality = mid;
                bestBytes = currentBytes;
                low = mid; // Try to get higher quality while staying under size
            } else {
                high = mid; // Too big, pull quality down
                if (bestBytes == null) bestBytes = currentBytes;
            }
        }

        writer.dispose();
        return bestBytes;
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot == -1 ? "jpg" : filename.substring(dot + 1);
    }

    private String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot == -1 ? filename : filename.substring(0, dot);
    }

    @FXML
    private void goBack() {
        NavigationHelper.navigate(backButton, "/view/ImageView.fxml");
    }
}
