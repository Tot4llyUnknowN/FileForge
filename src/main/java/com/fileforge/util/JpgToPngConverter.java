package com.fileforge.util;

public class JpgToPngConverter extends AbstractImageConverter {
    @Override
    protected String targetFormat() {
        return "png";
    }
}