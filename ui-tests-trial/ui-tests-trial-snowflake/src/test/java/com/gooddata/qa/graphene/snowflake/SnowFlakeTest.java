package com.gooddata.qa.graphene.snowflake;

import com.gooddata.qa.graphene.utils.ElementUtils;
import com.gooddata.qa.graphene.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import static org.openqa.selenium.By.id;
import static org.openqa.selenium.By.xpath;
import static org.testng.Assert.assertEquals;

public class SnowFlakeTest extends AbstractSnowFlakeTest {

    private static final By BY_DATASOURCE_SWITCHER_BUTTON = id("datasource-switcher");
    private static final By BY_DATASOURCE_SWITCHER_ITEM_SNOWFLAKE_BUTTON = id("datasource-switcher-item-Snowflake");
    private static final By BY_COPY_TO_CLIPBOARD_BUTTON = xpath("//button[contains(text(),'Copy to clipboard')]");
    private static final String DEVELOPER_GOODDATA_ADDRESS = "https://developer.gooddata.com/platform-trial/getting-started";
    private static final String EXPECTED_CONTENT_OF_REQUEST_BODY = "-- Create “gdtrial” database\n"+
        "CREATE OR REPLACE DATABASE gdtrial;\n"+
        "\n"+
        "-- Create “gdtrial” schema\n"+
        "CREATE OR REPLACE SCHEMA gdtrial;\n"+
        "\n"+
        "-- Create “out_csv_order_lines” table\n"+
        "CREATE OR REPLACE TABLE gdtrial.out_csv_order_lines (\n"+
        "a__order_line_id VARCHAR(255),\n"+
        "a__order_id VARCHAR(255),\n"+
        "d__date date,\n"+
        "a__order_status VARCHAR(255),\n"+
        "a__customer_id VARCHAR(255),\n"+
        "a__customer_name VARCHAR(255),\n"+
        "a__state VARCHAR(255),\n"+
        "a__product_id VARCHAR(255),\n"+
        "a__product_name VARCHAR(255),\n"+
        "a__category VARCHAR(255),\n"+
        "f__price DECIMAL(12,2),\n"+
        "f__quantity DECIMAL(15,6)\n"+
        ");\n"+
        "\n"+
        "-- Create file format describing format of order_lines.csv file\n"+
        "CREATE OR REPLACE FILE FORMAT \"GDTRIAL\".\"GDTRIAL\".gdtrialfileformat TYPE = 'CSV' " +
        "COMPRESSION = 'NONE' FIELD_DELIMITER = ',' RECORD_DELIMITER = '\\n' SKIP_HEADER = 1 " +
        "FIELD_OPTIONALLY_ENCLOSED_BY = 'NONE' TRIM_SPACE = FALSE ERROR_ON_COLUMN_COUNT_MISMATCH = TRUE " +
        "ESCAPE = 'NONE' ESCAPE_UNENCLOSED_FIELD = '\\134' DATE_FORMAT = 'AUTO' TIMESTAMP_FORMAT = 'AUTO' " +
        "NULL_IF = ('\\\\N');\n"+
        "\n"+
        "-- Create S3 stage with sample data\n"+
        "CREATE OR REPLACE STAGE gdstage\n"+
        "file_format = gdtrialfileformat\n"+
        "url = 's3://gdc-prod-gdtrial/platform-trial';\n"+
        "\n"+
        "-- Copy sample CSV from S3 into table\n"+
        "COPY INTO out_csv_order_lines\n"+
        "FROM @gdstage/step1/order_lines.csv\n"+
        "file_format = (format_name = gdtrialfileformat);\n";

    @Override
    protected void initProperties() {
        validateAfterClass = false;
    }

    @Test
    public void shouldCopyContentWhenClickingCopyToClipboardButton() throws IOException, UnsupportedFlavorException {
        browser.get(DEVELOPER_GOODDATA_ADDRESS);

        WaitUtils.waitForElementVisible(BY_DATASOURCE_SWITCHER_BUTTON, browser).click();
        WaitUtils.waitForElementVisible(BY_DATASOURCE_SWITCHER_ITEM_SNOWFLAKE_BUTTON, browser).click();
        WebElement copyToClipboard = WaitUtils.waitForElementVisible(BY_COPY_TO_CLIPBOARD_BUTTON, browser);
        ElementUtils.scrollElementIntoView(copyToClipboard, browser);

        copyToClipboard.click();

        String data = (String) Toolkit.getDefaultToolkit()
            .getSystemClipboard().getData(DataFlavor.stringFlavor);
        assertEquals(data, EXPECTED_CONTENT_OF_REQUEST_BODY);
    }
}
