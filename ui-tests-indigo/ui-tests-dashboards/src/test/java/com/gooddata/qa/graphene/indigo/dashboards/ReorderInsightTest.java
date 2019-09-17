package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.CssUtils.convertCSSClassTojQuerySelector;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;

import java.io.IOException;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.Widget;
import com.gooddata.qa.graphene.utils.ElementUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Widget.DropZone;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;

public class ReorderInsightTest extends AbstractDashboardTest {

    private static final String FIRST_INSIGHT = "First-Insight";
    private static final String SECOND_INSIGHT = "Second-Insight";
    private static final String THIRD_INSIGHT = "Third-Insight";
    private IndigoRestRequest indigoRestRequest;

    @Override
    public void initProperties() {
        projectTitle += "Reorder-Insight-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testAddingInsightsToDashboard() throws JSONException, IOException {
        String dashboardUri = indigoRestRequest.createAnalyticalDashboard(asList(
                createBlankInsightWrapUsingRest(FIRST_INSIGHT),
                createBlankInsightWrapUsingRest(SECOND_INSIGHT),
                createBlankInsightWrapUsingRest(THIRD_INSIGHT)
        ));
        indigoRestRequest.editWidthOfWidget(dashboardUri, 0, 0, 2);
        indigoRestRequest.editWidthOfWidget(dashboardUri, 0, 1, 2);
        indigoRestRequest.editWidthOfWidget(dashboardUri, 0, 2, 2);

        initIndigoDashboardsPageWithWidgets();
        checkInsightOrder(FIRST_INSIGHT, SECOND_INSIGHT, THIRD_INSIGHT, "Adding-Insight-To-Dashboard");
    }

    @Test(dependsOnMethods = {"testAddingInsightsToDashboard"}, groups = {"reorder-test"})
    public void testMovingInsightToFirstPosition() {
        initIndigoDashboardsPageWithWidgets().switchToEditMode();
        dragWidget(indigoDashboardsPage.getWidgetByIndex(Insight.class, 2),
                indigoDashboardsPage.getWidgetByIndex(Insight.class, 0), DropZone.PREV);
        checkInsightOrder(THIRD_INSIGHT, FIRST_INSIGHT, SECOND_INSIGHT, "Test-Moving-Insight-To-First-Position");
    }

    @Test(dependsOnMethods = {"testAddingInsightsToDashboard"}, groups = {"reorder-test"})
    public void testMovingInsightToLastPosition() {
        initIndigoDashboardsPageWithWidgets().switchToEditMode();
        indigoDashboardsPage.dragWidget(indigoDashboardsPage.getWidgetByIndex(Insight.class, 1),
                indigoDashboardsPage.getWidgetByIndex(Insight.class, 2), DropZone.NEXT);
        checkInsightOrder(FIRST_INSIGHT, THIRD_INSIGHT, SECOND_INSIGHT, "Test-Moving-Insight-To-Last-Position");
    }

    @Test(dependsOnMethods = {"testAddingInsightsToDashboard"}, groups = {"reorder-test"})
    public void testMovingInsightToMiddlePosition() {
        initIndigoDashboardsPageWithWidgets().switchToEditMode();
        indigoDashboardsPage.dragWidget(indigoDashboardsPage.getWidgetByIndex(Insight.class, 0),
                indigoDashboardsPage.getWidgetByIndex(Insight.class, 1), DropZone.NEXT);
        checkInsightOrder(SECOND_INSIGHT, FIRST_INSIGHT, THIRD_INSIGHT, "Test-Moving-Insight-To-Middle-Position");
    }

    @Test(dependsOnMethods = {"testAddingInsightsToDashboard"}, dependsOnGroups = {"reorder-test"})
    public void testInsightOrderAfterSaving() throws JSONException, IOException {
        initIndigoDashboardsPageWithWidgets().switchToEditMode();
        indigoDashboardsPage.dragWidget(indigoDashboardsPage.getWidgetByIndex(Insight.class, 0),
                indigoDashboardsPage.getWidgetByIndex(Insight.class, 1), DropZone.NEXT);
        checkInsightOrder(SECOND_INSIGHT, FIRST_INSIGHT, THIRD_INSIGHT, "Insight-Order-Before-Saving");
        indigoDashboardsPage.saveEditModeWithWidgets();
        checkInsightOrder(SECOND_INSIGHT, FIRST_INSIGHT, THIRD_INSIGHT, "Insight-Order-After-Saving");
    }

    @Test(dependsOnMethods = {"testInsightOrderAfterSaving"})
    public void testInsightOrderAfterSwitchingPage() throws JSONException, IOException {
        initIndigoDashboardsPageWithWidgets();
        checkInsightOrder(SECOND_INSIGHT, FIRST_INSIGHT, THIRD_INSIGHT, "Insight-Order-Before-Switching-Page");
        initAnalysePage();
        assertThat(browser.getCurrentUrl(), containsString("/reportId/edit"));
        initIndigoDashboardsPage().waitForDashboardLoad();
        checkInsightOrder(SECOND_INSIGHT, FIRST_INSIGHT, THIRD_INSIGHT, "Insight-Order-After-Switching-Page");
    }

    private String createBlankInsightWrapUsingRest(final String insightTitle) throws JSONException, IOException {
        String insightUri = indigoRestRequest.createInsight(new InsightMDConfiguration(insightTitle, ReportType.HEADLINE));

        return indigoRestRequest.createVisualizationWidget(insightUri, insightTitle);
    }

    private void checkInsightOrder(final String firstWidget, final String secondWidget, final String thirdWidget,
            final String screenShot) {
        takeScreenshot(browser,  screenShot, getClass());
        assertEquals(indigoDashboardsPage.getWidgetByIndex(Insight.class, 0).getHeadline(), firstWidget);
        assertEquals(indigoDashboardsPage.getWidgetByIndex(Insight.class, 1).getHeadline(), secondWidget);
        assertEquals(indigoDashboardsPage.getWidgetByIndex(Insight.class, 2).getHeadline(), thirdWidget);
    }

    private void dragWidget(final Widget source, final Widget target, DropZone dropZone) {
        final String sourceSelector = convertCSSClassTojQuerySelector(source.getRoot().getAttribute("class"));
        final String targetSelector = convertCSSClassTojQuerySelector(target.getRoot().getAttribute("class"));
        final String dropZoneSelector = targetSelector + " " + dropZone.getCss();

        dragAndDropWithCustomBackend(browser, sourceSelector, targetSelector, dropZoneSelector);
    }

    private static void dragAndDropWithCustomBackend(WebDriver driver, String fromSelector, String toSelector, String dropSelector) {
        WebElement source = waitForElementVisible(By.cssSelector(fromSelector), driver);
        Actions driverActions = new Actions(driver);

        driverActions.clickAndHold(source).perform();

        try {
            WebElement target = waitForElementVisible(By.cssSelector(toSelector), driver);
            ElementUtils.moveToElementActions(target, - target.getSize().height / 2 + 1, 1).perform();
            WebElement drop = waitForElementVisible(By.cssSelector(dropSelector), driver);
            driverActions.moveToElement(drop).perform();
        } finally {
            driverActions.release().perform();
        }
    }
}
