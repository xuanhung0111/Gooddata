package com.gooddata.qa.utils;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public final class PdfUtils {

    private PdfUtils() {
    }

    public static String getTextContentFrom(File pdfFile) {
        try {
            return new PDFTextStripper().getText(PDDocument.load(pdfFile));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
