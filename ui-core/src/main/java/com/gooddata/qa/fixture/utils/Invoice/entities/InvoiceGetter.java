package com.gooddata.qa.fixture.utils.Invoice.entities;

import com.gooddata.fixture.ResourceManagement;
import com.gooddata.qa.fixture.FixtureException;
import com.gooddata.qa.graphene.enums.DateRange;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.gooddata.fixture.ResourceManagement.ResourceTemplate.SINGLE_INVOICE;

public class InvoiceGetter {
    // keep it below 2000 records to avoid handling unnecessary performance issues
    private final int MAXIMUM_NUMBER_OF_RECORDS = 2000;
    private final String DELIMITER = ",";
    private final String DATE_FORMAT = "yyyy-MM-dd";
    private static final int FIXTURE_DEFAULT_VERSION = 1;

    private List<String> rawDataLines;

    private final ResourceManagement resourceManagement = new ResourceManagement();

    // there is only one csv file;
    private String invoiceEntry = new ResourceManagement()
            .getCsvEntryNames(SINGLE_INVOICE, FIXTURE_DEFAULT_VERSION).get(0);

    public InvoiceGetter() {
        try (InputStream stream = resourceManagement.getFileContent(invoiceEntry)) {
            this.rawDataLines = IOUtils.readLines(stream);
        } catch (IOException e) {
            throw new FixtureException("Can't read raw data located in gdc-test-fixture", e);
        }

        if (this.rawDataLines.size() > MAXIMUM_NUMBER_OF_RECORDS) {
            throw new RuntimeException("Having more than " + MAXIMUM_NUMBER_OF_RECORDS
                    + " records could lead to performance issue, getter is not designed to work with big data");
        }
    }

    public List<Invoice> getInvoices() {
        List<String> lines = new ArrayList<>(rawDataLines);
        HashMap<ColHeader, Integer> mappedIndexes = getMappedIndexes(lines.remove(0));

        return lines.parallelStream().map(line -> initInvoice(line, mappedIndexes)).collect(Collectors.toList());
    }

    public List<Invoice> getInvoices(DateRange dateRangeFilter) {
        return getInvoices().parallelStream()
                .filter(invoice -> invoice.getDate().isAfter(dateRangeFilter.getFrom().minus(1, ChronoUnit.DAYS))
                        && invoice.getDate().isBefore(dateRangeFilter.getTo().plus(1, ChronoUnit.DAYS)))
                .collect(Collectors.toList());
    }

    private Invoice initInvoice(String rawDataLine, HashMap<ColHeader, Integer> mappedIndexes) {
        String[] parts = rawDataLine.split(DELIMITER);

        return new Invoice(
                parts[mappedIndexes.get(ColHeader.INVOICE_NAME)],
                Integer.parseInt(parts[mappedIndexes.get(ColHeader.INVOICE_TOTAL)]),
                new Person(
                        parts[mappedIndexes.get(ColHeader.PERSON_FULLNAME)],
                        Integer.parseInt(parts[mappedIndexes.get(ColHeader.PERSON_ID)]),
                        parts[mappedIndexes.get(ColHeader.PERSON_FNAME)],
                        parts[mappedIndexes.get(ColHeader.PERSON_SNAME)]),
                new InvoiceItem(
                        Integer.parseInt(parts[mappedIndexes.get(ColHeader.ITEM_TOTAL)]),
                        Integer.parseInt(parts[mappedIndexes.get(ColHeader.ITEM_QUANTITY)])),
                LocalDate.parse(
                        parts[mappedIndexes.get(ColHeader.INVOICE_DATE)], DateTimeFormatter.ofPattern(DATE_FORMAT)));
    }

    private HashMap<ColHeader, Integer> getMappedIndexes(String rawColHeaders) {
        String[] parts = rawColHeaders.split(DELIMITER);

        if (ColHeader.values().length != parts.length)
            throw new IllegalArgumentException("input data is not mapped with defined column header");

        HashMap<ColHeader, Integer> hashMap = new HashMap<>();
        for (ColHeader header : ColHeader.values()) {
            for (int i = 0; i < parts.length; i++) {
                if (header.getName().equalsIgnoreCase(parts[i].replaceAll("^\"|\"$", ""))) {
                    hashMap.put(header, i);
                    break;
                } else if (i == parts.length - 1) {
                    throw new RuntimeException("Can't find col header " + header.getName());
                }
            }
        }

        return hashMap;
    }

    private enum ColHeader {
        ITEM_QUANTITY("tab_inv_item.col_quantity"),
        ITEM_TOTAL("tab_inv_item.col_total"),
        INVOICE_TOTAL("tab_inv.col_total"),
        INVOICE_NAME("tab_inv.col_name"),
        INVOICE_DATE("tab_inv.dt_invoice"),
        PERSON_ID("tab_pers.col_bn"),
        PERSON_FNAME("tab_pers.col_fn"),
        PERSON_SNAME("tab_pers.col_sn"),
        PERSON_FULLNAME("tab_pers.col_hn");

        private String name;

        ColHeader(String header) {
            this.name = header;
        }

        public String getName() {
            return name;
        }
    }
}
