package com.fileforge.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;

public class ImageConversionUtil {

    public static void convert(File inputFile, File outputFile, String targetFormat) throws IOException {
        BufferedImage sourceImage = ImageIO.read(inputFile);
        if (sourceImage == null) {
            throw new IOException("Could not read image file (unsupported or corrupted).");
        }

        BufferedImage outputImage = sourceImage;

        // JPG doesn't support transparency, so flatten any alpha channel onto a white background
        if (targetFormat.equalsIgnoreCase("jpg") || targetFormat.equalsIgnoreCase("jpeg")) {
            if (sourceImage.getColorModel().hasAlpha()) {
                BufferedImage flattened = new BufferedImage(
                        sourceImage.getWidth(), sourceImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g = flattened.createGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, sourceImage.getWidth(), sourceImage.getHeight());
                g.drawImage(sourceImage, 0, 0, null);
                g.dispose();
                outputImage = flattened;
            }
        }

        boolean success = ImageIO.write(outputImage, targetFormat, outputFile);
        if (!success) {
            throw new IOException("No writer available for format: " + targetFormat);
        }
    }
}