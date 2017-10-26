package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_DROPS_BELOW;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createKpiWidget;
import static java.lang.String.format;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.mail.MessagingException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.jsoup.nodes.Document;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Dataset;
import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.kpi.KpiMDConfiguration;
import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonDirection;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonType;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;

public class KpiValueFormatInAlertEmailTest extends AbstractDashboardTest {

    private static final String DATASET_NAME = "User";

    private List<String> kpiNames = new ArrayList<>();

    @Override
    public void initProperties() {
        super.initProperties();
        // init imap properties
        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"precondition"})
    public void initIndigoDashboardWithKpi() throws JSONException, IOException {
        String csvFilePath = new CsvFile(DATASET_NAME)
                .columns(new CsvFile.Column("firstname"), new CsvFile.Column("number"), new CsvFile.Column("Date"))
                .rows("Khoa", "15000", "2015-09-01")
                .saveToDisc(testParams.getCsvFolder());

        uploadCSV(csvFilePath);

        String numberFactUri = getMdService().getObjUri(getProject(), Fact.class, title("Number"));
        Attribute firstNameAttribute = getMdService().getObj(getProject(), Attribute.class, title("Firstname"));
        String firstNameValueUri = getMdService().getAttributeElements(firstNameAttribute)
                .stream().filter(e -> "Khoa".equals(e.getTitle())).findFirst().get().getUri();

        String dateDatasetUri = getMdService().getObjUri(getProject(), Dataset.class,
                identifier("date.dataset.dt"));

        String maqlExpression = format("SELECT SUM([%s]) WHERE [%s] = [%s]",
                numberFactUri, firstNameAttribute.getUri(), firstNameValueUri);

        List<Metric> metrics = Arrays.asList(
                new Metric(generateUniqueMetricName(), maqlExpression, "[<15000]#,##0.00 $"),
                new Metric(generateUniqueMetricName(), maqlExpression, "[=10000]#,##0 USD"),
                new Metric(generateUniqueMetricName(), maqlExpression, "[>5000]#,##0.0 VND"));

        List<String> kpiUris = new ArrayList<>();

        for (Metric metric : metrics) {
            kpiNames.add(metric.getTitle());

            kpiUris.add(createKpiWidget(getRestApiClient(), testParams.getProjectId(),
                    new KpiMDConfiguration.Builder()
                            .title(metric.getTitle())
                            .metric(getMdService().createObj(getProject(), metric).getUri())
                            .dateDataSet(dateDatasetUri)
                            .comparisonType(ComparisonType.PREVIOUS_PERIOD)
                            .comparisonDirection(ComparisonDirection.GOOD)
                            .build()));
        }

        createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(), kpiUris);
    }

    @Test(dependsOnMethods = "initIndigoDashboardWithKpi", groups = "precondition")
    public void setAlertForAllKpis()
            throws ParseException, IOException, JSONException {
        addUserToProject(imapUser, UserRoles.ADMIN);

        logout();
        signInAtGreyPages(imapUser, imapPassword);

        for (String kpiName : kpiNames) {
            initIndigoDashboardsPageWithWidgets()
                    .getWidgetByHeadline(Kpi.class, kpiName)
                    .openAlertDialog()
                    .selectTriggeredWhen(TRIGGERED_WHEN_DROPS_BELOW)
                    .setThreshold("15000")
                    .setAlert();
        }
    }

    @DataProvider(name = "kpiProvider")
    public Object[][] kpiProvider() {
        return new Object[][] {
            {kpiNames.get(0), "10,000.00 $", true},
            {kpiNames.get(1), "10,000 USD", false},
            {kpiNames.get(2), "10,000.0 VND", false}
        };
    }

    @Test(dependsOnGroups = "precondition", groups = "desktop", dataProvider = "kpiProvider")
    public void checkKpiValueFormatInEmail(String kpiName, String kpiValue, boolean updateCsv)
            throws JSONException, IOException, MessagingException {
        if (updateCsv) {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);

            String updatedCsvFilePath = new CsvFile("Update")
                    .columns(new CsvFile.Column("firstname"), new CsvFile.Column("number"), new CsvFile.Column("Date"))
                    .rows("Khoa", "10000", "2015-09-01")
                    .saveToDisc(testParams.getCsvFolder());

            updateCsvDataset(DATASET_NAME, updatedCsvFilePath);

            logout();
            signInAtGreyPages(imapUser, imapPassword);
        }

        Kpi kpi = initIndigoDashboardsPageWithWidgets().getWidgetByHeadline(Kpi.class, kpiName);

        takeScreenshot(browser, "Kpi-with-name-" + kpiName + "-is-triggered", getClass());

        assertTrue(kpi.isAlertTriggered(), "Kpi with name: " + kpiName + " is not triggered");
        assertEquals(kpi.getValue(), kpiValue);

        if (!testParams.isClusterEnvironment()) return;

        Document alertEmail = getLastAlertEmailContent(GDEmails.NOREPLY, kpiName + " is " + kpiValue);
        assertTrue(doesAlertEmailHaveContent(alertEmail, kpiValue),
                "Kpi value does not appear in alert email or has invalid format");
    }

    private String generateUniqueMetricName() {
        return "sumOfNumber-" + UUID.randomUUID().toString().substring(0, 6);
    }
}
