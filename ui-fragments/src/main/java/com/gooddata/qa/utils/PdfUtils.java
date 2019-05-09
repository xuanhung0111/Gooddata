package com.gooddata.qa.utils;

import java.io.File;
import java.io.IOException;

import com.testautomationguru.utility.CompareMode;
import com.testautomationguru.utility.PDFUtil;
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

    /**
     * @return A path of folder store pdf changed.
     */
    private static String createFolderSavePDFDifference() {
        File file = new File(System.getProperty("maven.project.build.directory", "./target/failures/PDFDifference"));
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    /**
     * @param exportPath   A path of file is exported from Dashboard.
     * @param templatePath A path of file is stored in template folder.
     * @return true if compare pdf didn't change. Otherwise, return false.
     * @throws IOException
     */
    public static boolean comparePDF(String exportPath, String templatePath) throws IOException {
        String path = createFolderSavePDFDifference();
        PDFUtil pdfUtil = new PDFUtil();
        pdfUtil.setCompareMode(CompareMode.VISUAL_MODE);
        pdfUtil.highlightPdfDifference(true);
        pdfUtil.setImageDestinationPath(path);
        return pdfUtil.compare(exportPath, templatePath);
    }
}
