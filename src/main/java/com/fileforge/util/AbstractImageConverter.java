package com.fileforge.util;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public abstract class AbstractImageConverter implements ImageConverter {

    protected abstract String targetFormat();

    @Override
    public void convert(File inputFile, File outputFile) throws IOException {
        BufferedImage sourceImage = ImageIO.read(inputFile);
        if (sourceImage == null) {
            throw new IOException("Could not read image file (unsupported or corrupted).");
        }

        BufferedImage prepared = prepareImage(sourceImage);

        boolean success = ImageIO.write(prepared, targetFormat(), outputFile);
        if (!success) {
            throw new IOException("No writer available for format: " + targetFormat());
        }
    }

    /** Hook for subclasses — default is a no-op passthrough. */
    protected BufferedImage prepareImage(BufferedImage source) {
        return source;
    }

    /** Shared helper: flattens transparency onto a white background (JPEG can't hold alpha). */
    protected BufferedImage flattenAlpha(BufferedImage source) {
        if (!source.getColorModel().hasAlpha()) {
            return source;
        }
        BufferedImage flattened = new BufferedImage(
                source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = flattened.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, source.getWidth(), source.getHeight());
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return flattened;
    }
}