package com.gooddata.qa.graphene.reports;

import com.gooddata.md.Attribute;
import com.gooddata.md.AttributeElement;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.addMufToUser;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.createMufObjectByUri;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.joda.time.DateTime.now;
import static org.testng.Assert.assertEquals;

import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;

public class GoodsalesMufReportTest extends GoodSalesAbstractTest {
    private Attribute stageNameAttribute;
    private AttributeElement stageNameValue;

    private Attribute yearCreatedAttribute;
    private AttributeElement yearCreatedValue;

    private String combinedMufExpression;

    @Override
    protected void customizeProject() throws Throwable {
        stageNameAttribute = getMdService().getObj(getProject(), Attribute.class, identifier("attr.stage.name"));
        stageNameValue = getMdService()
                .getAttributeElements(stageNameAttribute)
                .stream()
                .filter(element -> element.getTitle().equals("Interest"))
                .findFirst()
                .get();

        yearCreatedAttribute = getMdService().getObj(getProject(), Attribute.class, identifier("created.year"));
        yearCreatedValue = getMdService()
                .getAttributeElements(yearCreatedAttribute)
                .stream()
                .filter(element -> element.getTitle().equals(valueOf(now().getYear() - 5)))
                .findFirst()
                .get();
    }

    @DataProvider(name = "mufProvider")
    public Object [][] getMufProvider() {
        final String simpleMufExpression = format("[%s] = [%s]", stageNameAttribute.getUri(), stageNameValue.getUri());
        final String mufWithTimeMacroExpression = format("[%s] = THIS - 5", yearCreatedAttribute.getUri());

        combinedMufExpression = simpleMufExpression + " AND " + mufWithTimeMacroExpression;

        return new Object [][] {
            {"simpleMuf", simpleMufExpression, stageNameAttribute.getTitle(), stageNameValue.getTitle()},
            {"mufWithTimeMacro", mufWithTimeMacroExpression, yearCreatedAttribute.getTitle(), yearCreatedValue.getTitle()}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "mufProvider", groups = "combinedMuf")
    public void checkMufApplied(String mufName, String expression, String attribute, String attributeValue)
            throws ParseException, JSONException, IOException {

        final String mufUri =
                createMufObjectByUri(getRestApiClient(), testParams.getProjectId(), mufName, expression);

        addMufToUser(getRestApiClient(), testParams.getProjectId(),
                UserManagementRestUtils.getCurrentUserProfileUri(getRestApiClient()), mufUri);

        final String uniqueString = UUID.randomUUID().toString().substring(0, 6);
        final String reportName = "Report" + uniqueString;

        createReport(new UiReportDefinition()
                .withName(reportName)
                .withHows(attribute),
                reportName);

        List<String> attributeElements = reportPage.getTableReport().getAttributeValues();

        takeScreenshot(browser, mufName + "-applied-with-report", getClass());
        assertEquals(attributeElements.size(), 1);
        assertThat(attributeElements, hasItem(attributeValue));

        initDashboardsPage();

        dashboardsPage.addNewDashboard("New-Dashboard-" + uniqueString)
                .editDashboard()
                .addReportToDashboard(reportName)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, attribute)
                .saveDashboard();

        attributeElements = dashboardsPage
                .getContent()
                .getLatestReport(TableReport.class)
                .getAttributeValues();

        takeScreenshot(browser, mufName + "-applied-with-report-in-dashboard", getClass());
        assertEquals(attributeElements.size(), 1);
        assertThat(attributeElements, hasItem(attributeValue));

        attributeElements = dashboardsPage
                .getFilterWidget(simplifyText(attribute))
                .getAllAttributeValues();

        assertEquals(attributeElements.size(), 1);
        assertThat(attributeElements, hasItem(attributeValue));
    }

    @Test(dependsOnGroups = "combinedMuf")
    public void checkCombinedMuf() throws ParseException, IOException, JSONException {
        final String combinedMufUri =
                createMufObjectByUri(getRestApiClient(), testParams.getProjectId(), "combinedMuf", combinedMufExpression);

        addMufToUser(getRestApiClient(), testParams.getProjectId(),
                UserManagementRestUtils.getCurrentUserProfileUri(getRestApiClient()), combinedMufUri);

        final String uniqueString = UUID.randomUUID().toString().substring(0, 6);
        final String reportName = "Report" + uniqueString;

        createReport(new UiReportDefinition()
                .withName(reportName)
                .withHows(stageNameAttribute.getTitle(), yearCreatedAttribute.getTitle()),
                reportName);

        final List<String> attributeElements = reportPage.getTableReport().getAttributeValues();

        takeScreenshot(browser, "Combined-muf-applied-with-report", getClass());
        assertEquals(attributeElements.size(), 2);
        assertThat(attributeElements, hasItems(stageNameValue.getTitle(), yearCreatedValue.getTitle()));
    }
}
