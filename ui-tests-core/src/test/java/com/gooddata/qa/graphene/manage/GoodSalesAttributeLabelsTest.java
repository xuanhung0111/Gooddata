package com.gooddata.qa.graphene.manage;

import com.gooddata.md.Attribute;
import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.md.report.Report;
import com.gooddata.qa.graphene.AbstractDashboardWidgetTest;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.enums.AttributeLabelTypes;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.manage.AttributeDetailPage;
import com.gooddata.qa.graphene.fragments.manage.AttributeDetailPage.AttributeLabel;
import com.gooddata.qa.graphene.fragments.manage.VariableDetailPage;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.CellType;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collection;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DATE_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class GoodSalesAttributeLabelsTest extends AbstractDashboardWidgetTest {

    private static final String LABEL_SECTION_INFO =
            "Edit a label's type to determine how this attribute can be displayed in a report (or Geo chart widget).";

    private static final String LABEL_TYPE_TOOLTIP = "An attribute label's type determines how that attribute's "
            + "values are displayed: as plain text, hyperlinks, or images (in reports), or as geographic "
            + "regions (in Geo chart widgets). Type options are unavailable for date attributes. More info.";

    private static final String MORE_INFO_LINK = "https://help.gooddata.com/display/doc/GoodData+Help?lang=en";

    private static final String EDUCATIONLY = "Educationly";
    private static final String DIRECT_SALES = "Direct Sales";

    private static final String DATE_CREATED_LABEL = "mm/dd/yyyy (Created)";
    private static final String NEW_DATE_CREATED_LABEL = "new-title";
    private static final String PRODUCT_LABEL = "Product Name";
    private static final String DEPARTMENT_LABEL = "Department";

    private static final String MULTIPLE_VALUES = "Multiple-Values";
    private static final String ONE_VALUE = "One-Value";
    private static final String GROUP_FILTER = "Group-Filter";
    private static final String CASCADING_FILTER = "Cascading-Filter";

    private static final String ATTR_FIRSTNAME = "Firstname";
    private static final String FIRSTNAME_LABEL = "Firstname";
    private static final String FIRSTNAME_VALUE = "Gooddata";
    private static final String FIRSTNAME_VALUE_UPDATE = "Gooddata-update";
    private static final String FACT_NUMBER = "Number";
    private static final String DATASET = "User";

    private Report reportWithSingleAttribute;
    private Report reportWithMultipleAttribute;

    @Override
    protected void customizeProject() throws Throwable {
        Metric amountMetric = createAmountMetric();

        Attribute productAttribute = getMdService().getObj(getProject(), Attribute.class, title(ATTR_PRODUCT));
        Attribute departmentAttribute = getMdService().getObj(getProject(), Attribute.class, title(ATTR_DEPARTMENT));

        reportWithSingleAttribute = createReportViaRest(GridReportDefinitionContent.create("Report-single-attribute",
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(productAttribute.getDefaultDisplayForm().getUri(), productAttribute.getTitle())),
                singletonList(new MetricElement(amountMetric))));

        reportWithMultipleAttribute = createReportViaRest(GridReportDefinitionContent.create("Report-multiple-attribute",
                singletonList(METRIC_GROUP),
                asList(new AttributeInGrid(productAttribute.getDefaultDisplayForm().getUri(), productAttribute.getTitle()),
                        new AttributeInGrid(departmentAttribute.getDefaultDisplayForm().getUri(), departmentAttribute.getTitle())),
                singletonList(new MetricElement(amountMetric))));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void changeLabelOfDateAttribute() {
        AttributeLabel attributeLabel = initAttributePage()
                .initAttribute(ATTR_DATE_CREATED)
                .getLabel(DATE_CREATED_LABEL)
                .changeTitle(NEW_DATE_CREATED_LABEL);

        takeScreenshot(browser, "Date-attribute-label-title-changed", getClass());
        assertEquals(attributeLabel.getTitle(), NEW_DATE_CREATED_LABEL);

        boolean canSelectType = attributeLabel.clickEditButton().canSelectType();
        takeScreenshot(browser, "Date-attribute-label-type-is-disabled", getClass());
        assertFalse(canSelectType, "Date attribute label type is not disabled");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void changeLabelOfAttributeUsedInReport() {
        try {
            setAttributeLabelType(ATTR_PRODUCT, PRODUCT_LABEL, AttributeLabelTypes.HYPERLINK);
            initAttributePage().initAttribute(ATTR_PRODUCT).setDrillToExternalPage();

            TableReport report = initReportsPage()
                    .openReport(reportWithSingleAttribute.getTitle())
                    .getTableReport();
            takeScreenshot(browser, "Report-updated-correctly-after-changing-attribute-label", getClass());
            assertTrue(report.isDrillableToExternalPage(CellType.ATTRIBUTE_VALUE), "cannot drill report to external page");

        } finally {
            setAttributeLabelType(ATTR_PRODUCT, PRODUCT_LABEL, AttributeLabelTypes.TEXT);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void changeAttributeLabelUsedInPrompt() {
        final String variableName = "F-var";
        final Collection<String> isWonValues = asList("false", "true");

        initVariablePage().createVariable(new AttributeVariable(variableName)
                .withAttribute(ATTR_IS_WON)
                .withAttributeValues(isWonValues));

        AttributeLabel attributeLabel = initAttributePage()
                .initAttribute(ATTR_IS_WON)
                .getLabel(ATTR_IS_WON)
                .selectType(AttributeLabelTypes.IMAGE);

        takeScreenshot(browser, "Attribute-label-change-to-image", getClass());
        assertEquals(attributeLabel.getType(), AttributeLabelTypes.IMAGE.getlabel());

        VariableDetailPage variableDetailPage = initVariablePage().openVariableFromList(variableName);
        takeScreenshot(browser, "Values-in-variable-displays-correctly-after-change-attribute-label-type", getClass());
        assertEquals(variableDetailPage.getDefaultAttributeValues(), isWonValues);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkEditorCannotEditLabel() throws JSONException {
        logoutAndLoginAs(true, UserRoles.EDITOR);

        try {
            AttributeLabel attributeLabel = initAttributePage()
                    .initAttribute(ATTR_IS_WON)
                    .getLabel(ATTR_IS_WON);

            takeScreenshot(browser, "Editor-cannot-edit-label", getClass());
            assertFalse(attributeLabel.canEdit(), "Editor can edit attribute label");

        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkInfoInLabelSection() {
        AttributeDetailPage attributeDetailPage = initAttributePage().initAttribute(ATTR_IS_WON);
        assertEquals(attributeDetailPage.getLabelSectionInfo(), LABEL_SECTION_INFO);

        String tooltip = attributeDetailPage.getLabelTypeTooltip();
        takeScreenshot(browser, "Attribute-label-type-tooltip-shows", getClass());
        assertEquals(tooltip, LABEL_TYPE_TOOLTIP);

        String link = attributeDetailPage.getLabelTypeInfoLink();
        takeScreenshot(browser, "More-info-link-shows", getClass());
        assertEquals(link, MORE_INFO_LINK); // English language is used by default
    }

    @DataProvider(name = "filterSelectionTypeProvider")
    public Object[][] getFilterSelectionTypeProvider() {
        return new Object[][] {
            {reportWithSingleAttribute, MULTIPLE_VALUES},
            {reportWithSingleAttribute, ONE_VALUE}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "filterSelectionTypeProvider")
    public void changeAttributeLabelUsedInDashboardSingleFilter(Report report, String filterSelectionType) {
        final String dashboard = generateDashboardName();

        initDashboardsPage()
                .addNewDashboard(dashboard)
                .addReportToDashboard(report.getTitle())
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_PRODUCT, PRODUCT_LABEL);
        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(report.getTitle()).getRoot());

        if (ONE_VALUE.equals(filterSelectionType)) {
            getFilter(ATTR_PRODUCT).changeSelectionToOneValue();
        }

        dashboardsPage.saveDashboard();
        setAttributeLabelType(ATTR_PRODUCT, PRODUCT_LABEL, AttributeLabelTypes.HYPERLINK);

        try {
            initDashboardsPage()
                    .selectDashboard(dashboard)
                    .getFilterWidgetByName(ATTR_PRODUCT)
                    .changeAttributeFilterValues(EDUCATIONLY);

            getReport(report.getTitle()).waitForLoaded();
            takeScreenshot(browser, "Selection-type:" + filterSelectionType + 
                    "-works-correctly-after-change-attribute-label-type", getClass());
            assertEquals(getFilter(ATTR_PRODUCT).getCurrentValue(), EDUCATIONLY);
            assertEquals(getReport(report.getTitle()).getAttributeValues(), singletonList(EDUCATIONLY));

        } finally {
            setAttributeLabelType(ATTR_PRODUCT, PRODUCT_LABEL, AttributeLabelTypes.TEXT);
        }
    }

    @DataProvider(name = "multipleFilterTypeProvider")
    public Object[][] getMultipleFilterTypeProvider() {
        return new Object[][] {
            {reportWithMultipleAttribute, GROUP_FILTER},
            {reportWithMultipleAttribute, CASCADING_FILTER}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "multipleFilterTypeProvider")
    public void changeAttributeLabelUsedInDashboardMultipleFilter(Report report, String filterType) {
        final String dashboard = generateDashboardName();

        initDashboardsPage()
                .addNewDashboard(dashboard)
                .addReportToDashboard(report.getTitle())
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_PRODUCT, PRODUCT_LABEL)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_DEPARTMENT);

        if (GROUP_FILTER.equals(filterType)) {
            dashboardsPage.groupFiltersOnDashboard(ATTR_PRODUCT, ATTR_DEPARTMENT);
        } else {
            dashboardsPage.setParentsForFilter(ATTR_DEPARTMENT, ATTR_PRODUCT);
        }

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(report.getTitle()).getRoot());
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(getFilter(ATTR_DEPARTMENT).getRoot());
        dashboardsPage.saveDashboard();

        setAttributeLabelType(ATTR_PRODUCT, PRODUCT_LABEL, AttributeLabelTypes.HYPERLINK);
        setAttributeLabelType(ATTR_DEPARTMENT, DEPARTMENT_LABEL, AttributeLabelTypes.HYPERLINK);

        try {
            initDashboardsPage().selectDashboard(dashboard);
            getFilter(ATTR_PRODUCT).changeAttributeFilterValues(EDUCATIONLY);
            getFilter(ATTR_DEPARTMENT).changeAttributeFilterValues(DIRECT_SALES);

            if (GROUP_FILTER.equals(filterType)) {
                applyValuesForGroupFilter();
            }

            getReport(report.getTitle()).waitForLoaded();
            takeScreenshot(browser,
                    "Multiple-filter:" + filterType + "-works-correctly-after-change-attribute-label-type", getClass());
            assertEquals(getFilter(ATTR_PRODUCT).getCurrentValue(), EDUCATIONLY);
            assertEquals(getFilter(ATTR_DEPARTMENT).getCurrentValue(), DIRECT_SALES);
            assertEquals(getReport(report.getTitle()).getAttributeValues(), asList(EDUCATIONLY, DIRECT_SALES));

        } finally {
            setAttributeLabelType(ATTR_PRODUCT, PRODUCT_LABEL, AttributeLabelTypes.TEXT);
            setAttributeLabelType(ATTR_DEPARTMENT, DEPARTMENT_LABEL, AttributeLabelTypes.TEXT);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void updateDataAfterChangingAttributeLabel() throws IOException {
        String currentProjectId = testParams.getProjectId();
        String newProjectId = ProjectRestUtils.createBlankProject(getGoodDataClient(), projectTitle,
                testParams.getAuthorizationToken(), testParams.getProjectDriver(), testParams.getProjectEnvironment());
        testParams.setProjectId(newProjectId);

        try {
            String csvFilePath = new CsvFile(DATASET)
                    .columns(new CsvFile.Column(ATTR_FIRSTNAME), new CsvFile.Column(FACT_NUMBER))
                    .rows(FIRSTNAME_VALUE, "50")
                    .rows("test", "50")
                    .saveToDisc(testParams.getCsvFolder());
            uploadCSV(csvFilePath);

            Attribute firstnameAttribute = getMdService().getObj(getProject(), Attribute.class, title(ATTR_FIRSTNAME));
            String numberFactUri = getMdService().getObjUri(getProject(), Fact.class, title(FACT_NUMBER));

            Metric numberMetric = createMetric("numberMetric", format("SELECT SUM([%s])", numberFactUri), "#,##0");
            Report report = createReportViaRest(GridReportDefinitionContent.create("report-simple",
                    singletonList(METRIC_GROUP),
                    singletonList(new AttributeInGrid(firstnameAttribute.getDefaultDisplayForm().getUri(), firstnameAttribute.getTitle())),
                    singletonList(new MetricElement(numberMetric))));

            initDashboardsPage()
                    .addReportToDashboard(report.getTitle())
                    .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_FIRSTNAME);
            DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(report.getTitle()).getRoot());
            dashboardsPage.saveDashboard();

            getFilter(ATTR_FIRSTNAME).changeAttributeFilterValues(FIRSTNAME_VALUE);
            assertEquals(getFilter(ATTR_FIRSTNAME).getCurrentValue(), FIRSTNAME_VALUE);
            assertEquals(getReport(report.getTitle()).getAttributeValues(), singletonList(FIRSTNAME_VALUE));

            setAttributeLabelType(ATTR_FIRSTNAME, FIRSTNAME_LABEL, AttributeLabelTypes.HYPERLINK);

            String updatedCsvFilePath = new CsvFile("User-update")
                    .columns(new CsvFile.Column(ATTR_FIRSTNAME), new CsvFile.Column(FACT_NUMBER))
                    .rows(FIRSTNAME_VALUE_UPDATE, "50")
                    .rows("test", "50")
                    .saveToDisc(testParams.getCsvFolder());
            updateCsvDataset(DATASET, updatedCsvFilePath);

            initDashboardsPage();
            getFilter(ATTR_FIRSTNAME).changeAttributeFilterValues(FIRSTNAME_VALUE_UPDATE);
            getReport(report.getTitle()).waitForLoaded();

            takeScreenshot(browser, "Data-updated-correctly-after-changing-attribute-label", getClass());
            assertEquals(getFilter(ATTR_FIRSTNAME).getCurrentValue(), FIRSTNAME_VALUE_UPDATE);
            assertEquals(getReport(report.getTitle()).getAttributeValues(), singletonList(FIRSTNAME_VALUE_UPDATE));

        } finally {
            testParams.setProjectId(currentProjectId);
            ProjectRestUtils.deleteProject(getGoodDataClient(), newProjectId);
        }
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    private void setAttributeLabelType(String attribute, String attributeLabel, AttributeLabelTypes type) {
        initAttributePage()
                .initAttribute(attribute)
                .getLabel(attributeLabel)
                .selectType(type);
    }

    private void applyValuesForGroupFilter() {
        waitForElementVisible(By.className("s-btn-apply"), browser).click();
    }
}
