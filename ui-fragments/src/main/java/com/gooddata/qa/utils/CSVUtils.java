package com.gooddata.qa.utils;

import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVUtils {

    private CSVUtils() {
    }

    public static List<List<String>> readCsvFile(final File file) throws IOException {
        List<List<String>> actualResult = new ArrayList<>();
        try (CsvListReader reader = new CsvListReader(new FileReader(file), CsvPreference.STANDARD_PREFERENCE)) {
            List<String> reportResult;
            while ((reportResult = reader.read()) != null) {
                actualResult.add(reportResult);
            }
        }
        return actualResult;
    }
}
