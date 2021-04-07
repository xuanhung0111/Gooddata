package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;

import com.gooddata.qa.graphene.entity.kpi.KpiMDConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonDirection;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonType;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;

import com.gooddata.qa.graphene.utils.ElementUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;

public class DragWidgetsTest extends AbstractDashboardTest {

    private static final String WIDGET_SELECTOR_FORMATTER = ".dash-item-%1d";
    private static final String WIDGET_DROPZONE_FORMATTER = ".dropzone.%1s";

    private static final String DROPZONE_NEXT = "next";
    private static final String DROPZONE_PREV = "prev";

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        getMetricCreator().createAmountMetric();

        String dateDimensionUri = getDateDatasetUri(DATE_DATASET_CREATED);

        IndigoRestRequest indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)),
                testParams.getProjectId());
        List<String> kpiUris = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            kpiUris.add(indigoRestRequest.createKpiWidget(
                    new KpiMDConfiguration.Builder()
                            .title("Drag-Drop-" + i)
                            .metric(getMetricByTitle(METRIC_AMOUNT).getUri())
                            .dateDataSet(dateDimensionUri)
                            .comparisonType(ComparisonType.PREVIOUS_PERIOD)
                            .comparisonDirection(ComparisonDirection.GOOD)
                            .build()));
        }

        indigoRestRequest.createAnalyticalDashboard(kpiUris);
    }

    @DataProvider(name = "dragKpiProvider")
    public Object[][] dragKpiProvider() {
        return new Object[][] {
            {0, 5, DROPZONE_NEXT, "Drag-drop-kpi-to-an-other-in-dropzone-next"},
            {0, 5, DROPZONE_PREV, "Drag-drop-kpi-to-an-other-in-dropzone-previous"},
            {4, 0, DROPZONE_NEXT, "Drag-drop-kpi-to-beginning-of-line-in-dropzone-next"},
            {4, 0, DROPZONE_PREV, "Drag-drop-kpi-to-beginning-of-line-in-dropzone-previous"},
            {3, 9, DROPZONE_NEXT, "Drag-drop-kpi-to-different-line"}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "dragKpiProvider", groups = {"desktop"})
    public void basicDragDropTest(int fromIndex, int toIndex, String dropzone, String screenshotName) {
        initIndigoDashboardsPageWithWidgets()
                .switchToEditMode();

        String sourceKpiHeadline = waitForFragmentVisible(indigoDashboardsPage)
                .getWidgetByIndex(Kpi.class, fromIndex)
                .getHeadline();
        String targetKpiHeadline = indigoDashboardsPage
                .getWidgetByIndex(Kpi.class, toIndex)
                .getHeadline();

        dragWidgets(fromIndex, toIndex, dropzone);
        indigoDashboardsPage.saveEditModeWithWidgets();

        takeScreenshot(browser, screenshotName, getClass());

        assertEquals(
                indigoDashboardsPage
                        .getWidgetByIndex(Kpi.class, getSourceKpiIndexAfterDragDropTo(toIndex, dropzone))
                        .getHeadline(),
                sourceKpiHeadline);

        assertEquals(
                indigoDashboardsPage
                        .getWidgetByIndex(Kpi.class, getTargetKpiIndexAfterDragDropTo(toIndex, dropzone))
                        .getHeadline(),
                targetKpiHeadline);
    }

    private void dragWidgets(int fromIndex, int toIndex, String dropzoneType) {
        String from = format(WIDGET_SELECTOR_FORMATTER, fromIndex);

        String to = format(WIDGET_SELECTOR_FORMATTER, toIndex);
        int toTarget = 0;
        if (toIndex != 0)
            toTarget = toIndex - 1;
        String drop = format(WIDGET_SELECTOR_FORMATTER, toTarget) + ' ' +
                format(WIDGET_DROPZONE_FORMATTER, dropzoneType);

        dragAndDropWithCustomBackend(browser, from, to, drop);
    }

    private static void dragAndDropWithCustomBackend(WebDriver driver, String fromSelector, String toSelector, String dropSelector) {
        WebElement source = waitForElementVisible(By.cssSelector(fromSelector), driver);
        Actions driverActions = new Actions(driver);

        driverActions.clickAndHold(source).perform();

        try {
            WebElement target = waitForElementVisible(By.cssSelector(toSelector), driver);
            ElementUtils.moveToElementActions(target, target.getSize().height / 2 + 1, 1).perform();
            WebElement drop = waitForElementVisible(By.cssSelector(dropSelector), driver);
            driverActions.moveToElement(drop).perform();
        } finally {
            driverActions.release().perform();
        }
    }

    private int getSourceKpiIndexAfterDragDropTo(int index, String dropzone) {
        if (index == 0) {
            if (dropzone.equals(DROPZONE_PREV))
                return index;
            return index + 1;
        }

        if (dropzone.equals(DROPZONE_NEXT))
            return index;
        return index - 1;
    }

    private int getTargetKpiIndexAfterDragDropTo(int index, String dropzone) {
        if (index == 0) {
            if (dropzone.equals(DROPZONE_NEXT))
                return index;
            return index + 1;
        }

        if (dropzone.equals(DROPZONE_PREV))
            return index;
        return index - 1;
    }
}
