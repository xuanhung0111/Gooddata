package com.gooddata.qa.graphene.manage;

import static com.gooddata.qa.browser.BrowserUtils.switchToMainWindow;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_OPP_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_STAGE_DURATION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_STAGE_VELOCITY;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;

import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.fragments.manage.MetricDetailsPage;
import com.gooddata.qa.graphene.fragments.manage.MetricEditorDialog;
import com.gooddata.qa.graphene.fragments.manage.MetricEditorDialog.ElementType;
import com.gooddata.qa.utils.browser.BrowserUtils;

public class GoodSalesMetricEditorTest extends GoodSalesAbstractTest {

    private static final String MY_METRICS = "My Metrics";
    private static final String LABEL_ACCOUNT = "Account";

    @Override
    protected void initProperties() {
        super.initProperties();
        projectTitle += "Metric-Editor-Improvement-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        createNumberOfActivitiesMetric();
        createStageDurationMetric();
        createStageVelocityMetric();
    }

    @DataProvider
    public Object[][] searchValueProvider() {
        return new Object[][] {
                {ElementType.FACTS, "Opp",
                        asList("Opp. Close (Date)", "Opp. Created (Date)", "Opp. Snapshot (Date)")},
                {ElementType.METRICS, "Stage",
                            asList(METRIC_STAGE_DURATION, METRIC_STAGE_VELOCITY)},
                {ElementType.ATTRIBUTES, "Type",
                                singletonList(ATTR_ACTIVITY_TYPE)}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "searchValueProvider")
    public void testSearchElement(ElementType type, String searchValue, List<String> results) {
        assertTrue(isEqualCollection(initMetricPage().openMetricEditor().clickCustomMetricLink()
                .selectElementType(type).waitForElementsLoading().search(searchValue).getElementValues(), results),
                "The search results are not correct");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testValuesLoading() {
        // try to open list attribute values that has many values (e.g.: Opp.Snapshot)
        assertTrue(initMetricPage().openMetricEditor().clickCustomMetricLink()
                .selectElementType(ElementType.ATTRIBUTE_VALUES).selectElement(ATTR_OPP_SNAPSHOT)
                .getElementValues().size() > 0, "The expected values are not loaded");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void makeSureSearchFieldNotDisplayedInVariableType() {
        MetricEditorDialog dialog = initMetricPage().openMetricEditor().clickCustomMetricLink()
                .selectElementType(ElementType.VARIABLES);

        takeScreenshot(browser, "Test-No-Search-Field-On-Variable", getClass());
        assertFalse(dialog.isSearchFieldPresent(), "The search field is displayed on variable type ");
    }

    @Test(dependsOnGroups = {"createProject"}, description = "CL-10148: Missing global metric in list")
    public void makeSureGlobalMetricAlwaysVisible() {
        String metricName = "Global-Metric";
        initReportCreation().openWhatPanel().clickAddAdvanceMetric()
                .configureShareMetric(metricName, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE)
                .addToGlobalMetrics().addNewFolder(MY_METRICS).submit();

        switchToMainWindow(browser);
        //after switch from Metric editor, report page is scrolled up and can't click Advanced button on SndPanel
        //solution is use Javascript to scroll page
        waitForElementNotPresent(MetricEditorDialog.IFRAME, browser);
        BrowserUtils.runScript(browser, "window.scrollTo(0, 0)");

        assertEquals(
                reportPage.clickAddAdvanceMetric().clickCustomMetricLink().selectElementType(ElementType.METRICS)
                        .search(metricName).getElementValues(),
                singletonList(metricName), "The global metric is displayed");
    }

    @Test(dependsOnGroups = {"createProject"},
            description = "CL-10125: Bolder object shouldn't be remembered after choosing other object")
    public void makeSureBolderObjNotRememberedAfterChooseOtherObj() {
        // choose 2 atts in view port to avoid using search
        assertTrue(isEqualCollection(
                initMetricPage().openMetricEditor()
                        .configureShareMetric("Not-Remembered-Bold-Metric", METRIC_NUMBER_OF_ACTIVITIES,
                                ATTR_ACTIVITY_TYPE)
                        .selectElement(ATTR_ACCOUNT).getSelectedValues(),
                singletonList(ATTR_ACCOUNT)), "The bold object is remembered");
    }

    @Test(dependsOnGroups = {"createProject"}, description = "CL-10114: Folder is in white when hovering on it")
    public void testHeaderColorWhenHovering() {
        String header = "My Metrics";
        MetricEditorDialog dialog =
                initMetricPage().openMetricEditor().clickShareMetricLink().hoverOnHeader(header);

        takeScreenshot(browser, "Test-Header-Color-Text-When-Hovering", getClass());
        assertEquals(dialog.getHeaderTextColor(header), "rgba(0, 0, 0, 1)", "Text color is not black");
    }

    @Test(dependsOnGroups = {"createProject"},
            description = "CL-10120: User edit metric A, metric editor still show A in list metric instead of hidden")
    public void removeEditingMetricFromElementList() {
        String metricName = "Editing-Metric";
        initReportCreation().openWhatPanel().clickAddAdvanceMetric().createShareMetric(metricName,
                METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACCOUNT);

        switchToMainWindow(browser);
        waitForElementNotPresent(MetricEditorDialog.IFRAME, browser);
        assertTrue(reportPage.editMetric(metricName).waitForElementsLoading().search(metricName)
                .isEmptyMessagePresent(), "The empty message is not displayed");
    }

    @Test(dependsOnGroups = {"createProject"},
            description = "CL-10146: List metric object loading forever when Opening metric editor")
    public void testElementListLoadingAfterAddingNewMetric() {
        initMetricPage().openMetricEditor().createShareMetric("New-Metric", METRIC_NUMBER_OF_ACTIVITIES,
                ATTR_ACTIVITY_TYPE);

        switchToMainWindow(browser);
        //sleep for 2 seconds because of QA-6239
        //the metric details page is reloaded in a moment after metric is created
        sleepTightInSeconds(2);

        assertTrue(MetricDetailsPage.getInstance(browser).openMetricEditor().getElementValues().size() > 0,
                "The elements values are not loaded");
    }

    @Test(dependsOnGroups = {"createProject"},
            description = "CL-10117: Click on Attribute Label show Loading instead of List Attribute Label")
    public void loadAttributeListAfterSelectingAttributeLabelOnEditor() {
        MetricEditorDialog dialog = initReportCreation().openWhatPanel()
                .clickAddAdvanceMetric()
                .clickCustomMetricLink();

        dialog.selectElementType(ElementType.ATTRIBUTE_LABELS).waitForElementsLoading()
                .selectElement(ATTR_ACCOUNT)
                .addAttributeLabelToEditor(LABEL_ACCOUNT)
                .selectCodeMirrorWidget(ATTR_ACCOUNT + " [" + LABEL_ACCOUNT + "]");

        takeScreenshot(browser, "Element-List-After-Selecting-Attribute-Lavel", getClass());
        assertTrue(
                dialog.getElementValues().size() > 2
                        && dialog.getElementListTitle().equals(ElementType.ATTRIBUTES.toString()),
                "The Attribute list is not loaded");
    }
}
