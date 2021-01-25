package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomMeasureFormatDialog extends AbstractFragment {

    @FindBy(className = "s-cancel")
    private WebElement cancel;

    @FindBy(className = "s-apply")
    private WebElement apply;

    @FindBy(className = "gd-measure-format-templates")
    private WebElement templatesFormatButton;

    @FindBy(className = "gd-measure-number-format-dropdown-body")
    private WebElement templatesFormatDropdown;
    
    @FindBy(className = "s-custom-format-input")
    private WebElement customFormatInput;

    @FindBy(className = "gd-measure-custom-format-dialog-section-help")
    private WebElement howToFormatHelp;

    @FindBy(className = "gd-measure-custom-format-dialog-preview")
    private WebElement previewSection;

    @FindBy(className = "s-custom-format-dialog-extended-preview-button")
    private WebElement showMoreButton;

    @FindBy(className = "gd-measure-format-extended-preview-row")
    private List<WebElement> rowPreviewLabels;

    @FindBy(className = "gd-list-item.gd-format-preset")
    private List<WebElement> listItem;

    public static final By ROOT_LOCATOR = className("s-custom-format-dialog-body");
    public static final By PREVIEW_INPUT = className("gd-measure-custom-format-dialog-preview input");
    public static final By PREVIEW_LABEL = className("s-custom-format-dialog-preview-formatted");
    public static final By NUMBER_FORMAT_PREVIEW = className("s-number-format-preview-formatted");
    public static final By PREVIEW_IN_TOOLTIP = cssSelector(".s-custom-format-dialog-preview-formatted span");

    public static CustomMeasureFormatDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(CustomMeasureFormatDialog.class,
                waitForElementVisible(ROOT_LOCATOR, context));
    }

    public boolean isInputCustomFormatVisible() {
        return isElementVisible(customFormatInput);
    }

    public boolean isCancelButtonVisible() {
        return isElementVisible(cancel);
    }

    public boolean isSaveButtonVisible() {
        return isElementVisible(apply);
    }

    public boolean isPreviewSectionVisible() {
        return isElementVisible(previewSection);
    }

    public String getCurrentFormatOption() {
        return waitForElementVisible(customFormatInput).getText();
    }

    public String getPreviewInputText() {
        return waitForElementVisible(PREVIEW_INPUT, getRoot()).getText();
    }

    public String getPreviewLabelText() {
        return waitForElementVisible(PREVIEW_LABEL, getRoot()).getText();
    }

    public List<String> getExtendedPreviewLabel() {
        List<String> listValue = new ArrayList<>();
        rowPreviewLabels.stream().forEach(
            row -> listValue.add(row.findElement(NUMBER_FORMAT_PREVIEW).getText()));
        log.info("list Value of Column:" + listValue.toString());
        return listValue;
    }

    public String getColorPreviewLabel() {
        return waitForElementVisible(getRoot().findElement(PREVIEW_IN_TOOLTIP)).getAttribute("style");
    }

    public CustomMeasureFormatDialog setCustomMeasureFormat(String newFormat) {
        waitForElementVisible(customFormatInput).click();
        ElementUtils.clear(customFormatInput);
        getActions().sendKeys(newFormat).perform();
        return this;
    }

    public CustomMeasureFormatDialog cancel() {
        waitForElementPresent(cancel).click();
        return this;
    }

    public CustomMeasureFormatDialog apply() {
        waitForElementPresent(apply).click();
        return this;
    }

    public CustomMeasureFormatDialog clickShowMoreButton() {
        waitForElementVisible(showMoreButton).click();
        return this;
    }

    public CustomMeasureFormatDialog clickTemplatesButton() {
        waitForElementVisible(templatesFormatButton).click();
        return this;
    }

    public CustomMeasureFormatDialog clickHowToFormatButton() {
        waitForElementVisible(howToFormatHelp).click();
        return this;
    }

    public CustomMeasureFormatDialog selectTemplateFormat(TemplatesFormat templateFormat) {
        clickTemplatesButton();
        waitForElementVisible(cssSelector(templateFormat.toString()), browser).click();
        return this;
    }

    public List<List<String>> getToolTipFromQuestionIconTemplateFormat(TemplatesFormat templateFormat) {
        clickTemplatesButton();
        Actions driverActions =  new Actions(browser);
        WebElement hoverElement = waitForElementVisible(cssSelector(templateFormat.toString()), browser);
        driverActions.moveToElement(hoverElement).moveByOffset(1, 1).perform();

        WebElement target = waitForElementVisible(cssSelector(templateFormat.toString() + " .icon-circle-question"), browser);
        driverActions.moveToElement(target).moveByOffset(1, 1).perform();

        return Graphene.createPageFragment(TemplatePreviewPanel.class,
            waitForElementVisible(TemplatePreviewPanel.BY_LOCATOR, browser)).getTemplatePreviewValues();
    }

    public static class TemplatePreviewPanel extends AbstractFragment {

        @FindBy(tagName = "h3")
        private WebElement title;

        @FindBy(className = "gd-measure-format-template-preview-bubble-subtitle")
        private WebElement subTitle;

        @FindBy(className = "gd-measure-format-extended-preview-row")
        private List<WebElement> values;

        private static final By BY_LOCATOR = className("gd-measure-format-template-preview-bubble");
        private static final By BY_TOOLTIP_ITEM = className("gd-measure-format-extended-preview-row");
        private static final By BY_PREVIEW_NUMBER = className("gd-measure-format-extended-preview-number");
        private static final By PREVIEW_FORMATED = className("gd-measure-format-preview-formatted");
        private static final String NEW_LINE = "\n";

        public String getTooltipPreviewTemplateFormat() {
            waitForDataLoaded();
            return getTitlePreviewTemplate().append(getTemplatePreviewValues()).append(NEW_LINE).toString();
        }

        private StringBuilder getTitlePreviewTemplate() {
            return new StringBuilder().append(waitForElementVisible(title).getText()).append(NEW_LINE)
                .append(waitForElementVisible(subTitle).getText()).append(NEW_LINE);
        }

        private void waitForDataLoaded() {
            waitForElementPresent(BY_LOCATOR, browser);
        }
    
        public List<List<String>> getTemplatePreviewValues() {
            WebElement tooltip = browser.findElement(BY_LOCATOR);
            return waitForCollectionIsNotEmpty(tooltip.findElements(BY_TOOLTIP_ITEM)).stream()
                .map(item -> asList(item.findElement(BY_PREVIEW_NUMBER).getText(), item.findElement(PREVIEW_FORMATED).getText()))
                .collect(Collectors.toList());
        }
    }
    
    public enum Formatter {

        DEFAULT("#,##0.00"),
        GDC("GDC#,##0.00"),
        BARS(new StringBuilder("[>=9000000][color=2190c0]██████████;")
            .append("[>=8000000][color=2190c0]█████████░;")
            .append("[>=7000000][color=2190c0]████████░░;")
            .append("[>=6000000][color=2190c0]███████░░░;")
            .append("[>=5000000][color=2190c0]██████░░░░;")
            .append("[>=4000000][color=2190c0]█████░░░░░;")
            .append("[>=3000000][color=2190c0]████░░░░░░;")
            .append("[>=2000000][color=2190c0]███░░░░░░░;")
            .append("[>=1000000][color=2190c0]██░░░░░░░░;")
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
        CONDITION_NULL("[=Null][backgroundcolor=DDDDDD][red]No Value"),
        COLORS(new StringBuilder("[<0][red]$#,#.##;")
            .append("[<1000][blue]$#,#.##;")
            .append("[>=1000][green]$#,#.##")
            .toString()),
        UTF_8("#'##0.00 kiểm tra nghiêm khắc"),
        COLORS_FORMAT("[BLUE] #,###.###"),
        BACKGROUND_COLOR_FORMAT("[RED][backgroundColor=aff8ef]#,##0.00"),
        XSS("<button>#,##0.00</button>"),
        NULL_VALUE("#'##0,00 formatted; [=null] null value!"),
        LONG("$#,##0,00 long format long format long format long format long format long format long format"),
        UNIT_CONVERSION("{{{86400||#}}} days\\, {{{3600|24|00}}}:{{{60|60|00}}}:{{{|60.|00.000}}} hours"),
        REGION("# ##0,00");

        private String text;

        Formatter(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public enum TemplatesFormat {

        ROUNDED(".s-measure-format-template-rounded"),
        DECIMAL_1(".s-measure-format-template-decimal__1_"),
        DECIMAL_2(".s-measure-format-template-decimal__2_"),
        PERCENT_ROUNDED(".s-measure-format-template-percent__rounded_"),
        PERCENT_1(".s-measure-format-template-percent__1_"),
        PERCENT_2(".s-measure-format-template-percent__2_"),
        CURRENCY(".s-measure-format-template-currency"),
        CURRENCY_SHORTENED(".s-measure-format-template-currency_shortened"),
        LARGE_NUMBERS_SHORTENED(".s-measure-format-template-large_numbers_shortened"),
        LARGE_NUMBERS_SHORTENED_WITH_COLORS(".s-measure-format-template-large_numbers_shortened_with_colors"),
        NEGATIVE_NUMBERS_IN_RED(".s-measure-format-template-negative_numbers_in_red"),
        FINANCIAL(".s-measure-format-template-financial"),
        DECIMAL_2_WITHOUT_THOUSANDS_SEPARATOR(".s-measure-format-template-decimal__2__without_thousands_separator"),
        CONDITIONAL_COLORS(".s-measure-format-template-conditional_colors"),
        TREND_SYMBOLS(".s-measure-format-template-trend_symbols"),
        TIME_FROM_SECONDS(".s-measure-format-template-time__from_seconds_"),
        ZERO_INSTEAD_OF_BLANK_VALUE(".s-measure-format-template-zero_instead_of_blank_value");

        private String type;

        TemplatesFormat(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }
}
