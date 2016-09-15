package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static org.openqa.selenium.By.className;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.google.common.base.Predicate;

/**
 * Kpi - key performance indicator widget
 */
public class Kpi extends Widget {
    // TODO: when having more widget types, separate, keep "Add widget" in mind
    public static final String MAIN_SELECTOR = ".dash-item.type-kpi";
    public static final String KPI_CSS_SELECTOR = MAIN_SELECTOR + ":not(.is-placeholder)";
    public static final String KPI_POP_SECTION_CLASS = "kpi-pop-section";
    public static final String KPI_ALERT_BUTTON_CLASS = "dash-item-action-alert";
    public static final String KPI_HAS_SET_ALERT_BUTTON = "has-set-alert";
    public static final String KPI_IS_ALERT_TRIGGERED = "is-alert-triggered";
    public static final String KPI_IS_EMPTY_VALUE = "is-empty-value";
    public static final String KPI_IS_ERROR_VALUE = "is-error-value";
    public static final String KPI_ALERT_DIALOG_CLASS = "kpi-alert-dialog";

    public static final By IS_NOT_EDITABLE = By.cssSelector(MAIN_SELECTOR + " .kpi:not(.is-editable)");
    public static final By ALERT_DIALOG = By.className(KPI_ALERT_DIALOG_CLASS);

    @FindBy(className = KPI_ALERT_BUTTON_CLASS)
    private WebElement alertButton;

    @FindBy(className = "kpi-value")
    private WebElement value;

    @FindBy(className = KPI_POP_SECTION_CLASS)
    private KpiPopSection popSection;

    public static Kpi getInstance(final WebElement root) {
        return Graphene.createPageFragment(Kpi.class, waitForElementVisible(root));
    }

    public String getValue() {
        waitForContentLoading();
        return waitForElementVisible(value).getText();
    }

    public String getTooltipOfValue() {
        waitForContentLoading();
        return waitForElementVisible(value).getAttribute("title");
    }

    public Kpi clickKpiValue() {
        waitForContentLoading();
        waitForElementVisible(value).click();

        return this;
    }

    public boolean hasPopSection() {
        return isElementPresent(className(KPI_POP_SECTION_CLASS), getRoot());
    }

    public KpiPopSection getPopSection() {
        waitForContentLoading();
        return waitForFragmentVisible(popSection);
    }

    public Kpi waitForAlertButtonVisible() {
        waitForElementVisible(alertButton);

        return this;
    }

    public Kpi waitForAlertButtonNotVisible() {
        waitForElementNotVisible(alertButton);

        return this;
    }

    public boolean hasAlertDialogOpen() {
        return isElementPresent(ALERT_DIALOG, browser);
    }

    public boolean hasSetAlert() {
        return isElementPresent(By.className(KPI_HAS_SET_ALERT_BUTTON), this.getRoot());
    }

    public boolean isAlertTriggered() {
        return isElementPresent(By.className(KPI_IS_ALERT_TRIGGERED), this.getRoot());
    }

    public boolean isEmptyValue() {
        return isElementPresent(By.className(KPI_IS_EMPTY_VALUE), this.getRoot());
    }

    public boolean isErrorValue() {
        return isElementPresent(By.className(KPI_IS_ERROR_VALUE), this.getRoot());
    }

    public KpiAlertDialog openAlertDialog() {
        if (!hasAlertDialogOpen()) {
            hoverAndClickKpiAlertButton();
        }

        return Graphene.createPageFragment(KpiAlertDialog.class,
                waitForElementVisible(KpiAlertDialog.LOCATOR, browser));
    }

    public Kpi hoverAndClickKpiAlertButton() {
        // In case after edit Indigo dashboard and save, there should have a piece of time to wait for
        // Kpi until its position is fixed on dashboard.
        // And in that time, any actions like hover may not work properly because the Kpi is still running.
        // Consider this is a work around to wait until the Kpi's position is fixed on dashboard.
        sleepTightInSeconds(3);
        // TODO: Find a way to fix this issue that described in https://jira.intgdc.com/browse/CL-10042

        getActions().moveToElement(value).moveToElement(alertButton).click().build().perform();

        return this;
    }

    public Kpi waitForContentLoading() {
        Predicate<WebDriver> isContentLoaded = browser -> !waitForElementVisible(className("kpi"), getRoot())
                .getAttribute("class").contains("content-loading");
        Graphene.waitGui().until(isContentLoaded);
        return this;
    }

    public boolean hasHintForEditName() {
        return isElementPresent(HINT_LOCATOR, this.getRoot());
    }

    public void delete() {
        waitForContentLoading().clickOnContent().clickDeleteButton();
        ConfirmDialog.getInstance(browser).submitClick();
    }

    public static boolean isKpi(final Widget widget) {
        return widget.getRoot().getAttribute("class").contains("type-kpi");
    }

    public enum ComparisonType {
        NO_COMPARISON("none", "No comparison"),
        LAST_YEAR("lastYear", "Same period in previous year"),
        PREVIOUS_PERIOD("previousPeriod", "Previous period");

        private String jsonKey;
        private String uiText;

        private ComparisonType(String jsonKey, String uiText) {
            this.jsonKey = jsonKey;
            this.uiText = uiText;
        }

        public String getJsonKey() {
            return jsonKey;
        }

        @Override
        public String toString() {
            return uiText;
        }
    }

    public enum ComparisonDirection {
        NONE,
        GOOD("growIsGood"),
        BAD("growIsBad");

        private String text;

        private ComparisonDirection(String text) {
            this.text = text;
        }

        private ComparisonDirection() {
            text = "";
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
