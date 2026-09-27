package com.fileforge.util;

import java.io.File;
import java.io.IOException;

public interface ImageConverter {
    void convert(File inputFile, File outputFile) throws IOException;
}