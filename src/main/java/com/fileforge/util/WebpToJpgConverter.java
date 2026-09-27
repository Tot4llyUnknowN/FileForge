package com.fileforge.util;

import java.awt.image.BufferedImage;

public class WebpToJpgConverter extends AbstractImageConverter {
    @Override
    protected String targetFormat() {
        return "jpeg";
    }

    @Override
    protected BufferedImage prepareImage(BufferedImage source) {
        return flattenAlpha(source);
    }
}