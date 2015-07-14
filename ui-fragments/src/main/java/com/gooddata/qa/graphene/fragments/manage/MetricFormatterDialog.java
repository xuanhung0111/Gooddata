package com.gooddata.qa.graphene.fragments.manage;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class MetricFormatterDialog extends AbstractFragment {

    @FindBy(className = "container-close")
    private WebElement closeButton;

    @FindBy(className = "s-btn-cancel")
    private WebElement cancelButton;

    @FindBy(className = "s-btn-apply")
    private WebElement applyButton;

    @FindBy(className = "formatter-preset-image-bars")
    private WebElement barsFormatter;

    @FindBy(className = "formatter-preset-image-default")
    private WebElement defaultFormatter;

    @FindBy(className = "formatter-preset-image-large")
    private WebElement truncateLargeNumbersFormatter;

    @FindBy(className = "formatter-preset-image-colors")
    private WebElement colorsFormatter;

    @FindBy(className = "formatter-format")
    private WebElement input;

    public static final By LOCATOR = By.className("c-formatterDialog");

    public MetricFormatterDialog changeFormat(String newFormat) {
        waitForElementVisible(input).clear();
        input.sendKeys(newFormat);
        return submit();
    }

    public MetricFormatterDialog changeFormat(Formatter format) {
        waitForElementVisible(getPresetFormatterFrom(format)).click();
        return submit();
    }

    public MetricFormatterDialog changeFormatButDiscard(Formatter format) {
        waitForElementVisible(getPresetFormatterFrom(format)).click();
        return discard();
    }

    private WebElement getPresetFormatterFrom(Formatter format) {
        switch (format) {
            case BARS:
                return barsFormatter;
            case TRUNCATE_NUMBERS:
                return truncateLargeNumbersFormatter;
            case COLORS:
                return colorsFormatter;
            default:
                return defaultFormatter;
        }
    }

    private MetricFormatterDialog submit() {
        waitForElementVisible(applyButton).click();
        waitForFragmentNotVisible(this);
        return this;
    }

    private MetricFormatterDialog discard() {
        waitForElementVisible(cancelButton).click();
        waitForFragmentNotVisible(this);
        return this;
    }

    public static enum Formatter {
        DEFAULT("#,##0.00"),
        BARS(new StringBuilder("[>=9][color=2190c0]██████████;")
            .append("[>=8][color=2190c0]█████████░;")
            .append("[>=7][color=2190c0]████████░░;")
            .append("[>=6][color=2190c0]███████░░░;")
            .append("[>=5][color=2190c0]██████░░░░;")
            .append("[>=4][color=2190c0]█████░░░░░;")
            .append("[>=3][color=2190c0]████░░░░░░;")
            .append("[>=2][color=2190c0]███░░░░░░░;")
            .append("[>=1][color=2190c0]██░░░░░░░░;")
            .append("[color=2190c0]█░░░░░░░░░")
            .toString()),
        TRUNCATE_NUMBERS(new StringBuilder("[>=1000000000]$#,,,.0 B;")
            .append("[<=-1000000000]-$#,,,.0 B;")
            .append("[>=1000000]$#,,.0 M;")
            .append("[<=-1000000]-$#,,.0 M;")
            .append("[>=1000]$#,.0 K;")
            .append("[<=-1000]-$#,.0 K;")
            .append("$#,##0")
            .toString()),
        COLORS(new StringBuilder("[<0][red]$#,#.##;")
            .append("[<1000][blue]$#,#.##;")
            .append("[>=1000][green]$#,#.##")
            .toString());

        private String text;

        private Formatter(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
