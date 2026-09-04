package com.fileforge.util;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfThumbnailUtil {

    private static final float THUMBNAIL_DPI = 45f;

    /** Renders a thumbnail for every page of the document. */
    public static List<Image> renderAllThumbnails(PDDocument document) throws IOException {
        PDFRenderer renderer = new PDFRenderer(document);
        List<Image> thumbnails = new ArrayList<>();

        for (int i = 0; i < document.getNumberOfPages(); i++) {
            BufferedImage bufferedImage = renderer.renderImageWithDPI(i, THUMBNAIL_DPI, ImageType.RGB);
            thumbnails.add(toFxImage(bufferedImage));
        }
        return thumbnails;
    }

    /** Renders just the first page — used for PDF Merger's per-file preview. */
    public static Image renderFirstPageThumbnail(PDDocument document) throws IOException {
        PDFRenderer renderer = new PDFRenderer(document);
        BufferedImage bufferedImage = renderer.renderImageWithDPI(0, THUMBNAIL_DPI, ImageType.RGB);
        return toFxImage(bufferedImage);
    }

    /**
     * Manual BufferedImage -> JavaFX Image conversion, avoiding the javafx-swing dependency.
     */
    private static Image toFxImage(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        int[] pixels = bufferedImage.getRGB(0, 0, width, height, null, 0, width);
        pixelWriter.setPixels(0, 0, width, height,
                javafx.scene.image.PixelFormat.getIntArgbInstance(), pixels, 0, width);

        return writableImage;
    }
}