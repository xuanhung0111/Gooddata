package com.gooddata.qa.graphene.filters;

import com.gooddata.md.Attribute;
import com.gooddata.md.AttributeElement;
import com.gooddata.qa.graphene.AbstractDashboardWidgetTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.*;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardsPage;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.utils.graphene.Screenshots;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DIMENSION_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_PRODUCT;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Calendar.YEAR;
import static java.util.Collections.singletonList;

public class GroupFiltersOnDashboardTest extends AbstractDashboardWidgetTest {

    private static final String VARIABLE_PRODUCT = "Product";
    private static final String DATE_START = "01/01/2008";
    private static final String DATE_END = "12/31/2008";
    private static final int YEAR_2010 = 2010;

    @Override
    protected void customizeProject() throws Throwable {
        createAmountByProductReport();

        Attribute product = getAttributeByTitle(ATTR_PRODUCT);
        List<String> attElements = asList("CompuSci", "Educationly", "Explorer");

        String filterExpression = format("[%s] IN (%s)", product.getUri(),
                getMdService().getAttributeElements(product).stream()
                        .filter(e -> attElements.contains(e.getTitle()))
                        .map(AttributeElement::getUri)
                        .map(uri -> "[" + uri + "]")
                        .collect(Collectors.joining(", ")));

        createFilterVarIfNotExist(getRestApiClient(), testParams.getProjectId(),
                VARIABLE_PRODUCT, product.getUri(), filterExpression);

        initReportsPage().openReport(REPORT_AMOUNT_BY_PRODUCT)
                .addFilter(FilterItem.Factory.createPromptFilter(VARIABLE_PRODUCT))
                .saveReport();

        initDashboardsPage().editDashboard()
                .addReportToDashboard(REPORT_AMOUNT_BY_PRODUCT)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_ACCOUNT)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.PROMPT, VARIABLE_PRODUCT);

        DashboardWidgetDirection.UP.moveElementToRightPlace(getFilter(VARIABLE_PRODUCT).getRoot());
        DashboardWidgetDirection.MIDDLE.moveElementToRightPlace(getFilter(ATTR_ACCOUNT).getRoot());
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(
                dashboardsPage.getContent().getLatestReport(TableReport.class).getRoot());

        dashboardsPage.addTimeFilterToDashboard(DATE_DIMENSION_CREATED, TimeFilterPanel.DateGranularity.YEAR,
                format("%s ago", Calendar.getInstance().get(YEAR) - YEAR_2010), DashboardWidgetDirection.LEFT);

        dashboardsPage.groupFiltersOnDashboard(ATTR_ACCOUNT, VARIABLE_PRODUCT, DATE_DIMENSION_CREATED)
                .saveDashboard();
    }

    @DataProvider(name = "time-filter-types")
    public Object[][] getTimeFilterType() {
        return new Object[][]{
                {DashboardMode.VIEW, TimeFilterType.TIMELINE},
                {DashboardMode.VIEW, TimeFilterType.DATE_RANGE},
                {DashboardMode.EDIT, TimeFilterType.TIMELINE},
                {DashboardMode.EDIT, TimeFilterType.DATE_RANGE}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "time-filter-types")
    public void addChangeToAllFilters(DashboardMode mode, TimeFilterType type) {
        openDashboard(mode);
        changeTimeFilterValue(type, getFilter(DATE_DIMENSION_CREATED), DATE_START, DATE_END);
        getFilter(ATTR_ACCOUNT).openPanel()
                .changeAttributeFilterValues("1000Bulbs.com", "101 Financial", "Zther Interactive");
        getFilter(VARIABLE_PRODUCT).openPanel().changeAttributeFilterValues("Educationly", "Explorer");
        dashboardsPage.applyValuesForGroupFilter();

        TableReport report = dashboardsPage.getContent().getLatestReport(TableReport.class);
        Assert.assertEquals(report.getAttributeValues(), singletonList("Educationly"));
        Assert.assertEquals(report.getRawMetricValues(), singletonList("$24,000.00"));
    }

    @DataProvider
    public Object[][] getAttributeFilterValue() {
        return new Object[][] {
                {ATTR_ACCOUNT, "1-800 Postcards", Pair.of("Educationly", "")},
                {ATTR_PRODUCT, "CompuSci", Pair.of("CompuSci", "$27,222,899.64")}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "getAttributeFilterValue")
    public void addChangeToAttributeFilter(String filter, String changeValues, Pair<String, String> expectedValues) {
        openDashboard(DashboardMode.EDIT);
        dashboardsPage.getDashboardEditBar().getDashboardEditFilter().deleteTimeFilter();

        getFilter(filter).openPanel().changeAttributeFilterValues(changeValues);
        dashboardsPage.applyValuesForGroupFilter();

        TableReport report = dashboardsPage.getContent().getLatestReport(TableReport.class);
        Screenshots.takeScreenshot(browser, "addChangeToAttributeFilter - " + filter, getClass());
        Assert.assertEquals(report.getAttributeValues(), singletonList(expectedValues.getLeft()));
        Assert.assertEquals(report.getRawMetricValues(), singletonList(expectedValues.getRight()));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addChangeToTimeFilter() {
        openDashboard(DashboardMode.VIEW);
        getFilter(DATE_DIMENSION_CREATED).changeTimeFilterValueByClickInTimeLine("2008");
        dashboardsPage.getContent().applyValuesForGroupFilter();

        TableReport report = dashboardsPage.getContent().getLatestReport(TableReport.class).waitForLoaded();
        Screenshots.takeScreenshot(browser, "addChangeToTimeFilter", getClass());
        Assert.assertEquals(report.getAttributeValues(), asList("CompuSci", "Educationly", "Explorer"));
        Assert.assertEquals(report.getRawMetricValues(), asList("$152,462.56", "$1,239,910.53", "$825,215.13"));
    }

    @DataProvider
    public Object[][] getPartialGroupTestData() {
        return new Object[][]{
                {"2008", "1-888-OhioComp", null, Pair.of(singletonList("Explorer"), singletonList("$40,000.00"))},
                {"2008", null, "Explorer", Pair.of(singletonList("Explorer"), singletonList("$825,215.13"))},
                {null, "14 West", "CompuSci", Pair.of(singletonList("CompuSci"), singletonList("$5,534.96"))}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "getPartialGroupTestData")
    public void addChangeToPartialGroup(String timeFilterValue, String attFilterValue, String variableFilterValue,
                                        Pair<List<String>, List<String>> expectedValues) {
        openDashboard(DashboardMode.VIEW);
        // change timeline to 2012 to make test result more meaningful
        getFilter(DATE_DIMENSION_CREATED).changeTimeFilterValueByClickInTimeLine("2012");
        dashboardsPage.applyValuesForGroupFilter();

        addChangeToGroupFilters(timeFilterValue, attFilterValue, variableFilterValue);
        dashboardsPage.applyValuesForGroupFilter();

        TableReport report = dashboardsPage.getContent().getLatestReport(TableReport.class).waitForLoaded();
        Assert.assertEquals(Pair.of(report.getAttributeValues(), report.getRawMetricValues()), expectedValues);
    }

    private void addChangeToGroupFilters(String timeFilterValue, String attFilterValue, String variableFilterValue) {
        if(Objects.nonNull(timeFilterValue))
            getFilter(DATE_DIMENSION_CREATED).changeTimeFilterValueByClickInTimeLine(timeFilterValue);

        if(Objects.nonNull(attFilterValue))
            getFilter(ATTR_ACCOUNT).openPanel().changeAttributeFilterValues(attFilterValue);

        if(Objects.nonNull(variableFilterValue))
            getFilter(VARIABLE_PRODUCT).changeAttributeFilterValues(variableFilterValue);
    }

    private DashboardsPage openDashboard(DashboardMode mode) {
        DashboardsPage page = initDashboardsPage();
        if (mode == DashboardMode.EDIT)
            page.editDashboard();

        return page;
    }

    private void changeTimeFilterValue(TimeFilterType type, FilterWidget widget, String start, String end) {
        Pair<String, String> timelinePair = convertToTimeLineValue(start, end);
        TimeFilterPanel panel = widget.openPanel().getTimeFilterPanel();

        if (type == TimeFilterType.TIMELINE)
            panel.selectRange(timelinePair.getLeft(), timelinePair.getRight());
        else if (type == TimeFilterType.DATE_RANGE)
            panel.changeValueByEnterFromDateAndToDate(start, end);
    }

    private Pair<String, String> convertToTimeLineValue(String start, String end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        return Pair.of(String.valueOf(LocalDate.parse(start, formatter).getYear()),
                String.valueOf(LocalDate.parse(end, formatter).getYear()));
    }

    private enum TimeFilterType {
        TIMELINE,
        DATE_RANGE
    }

    private enum DashboardMode {
        VIEW,
        EDIT
    }
}
