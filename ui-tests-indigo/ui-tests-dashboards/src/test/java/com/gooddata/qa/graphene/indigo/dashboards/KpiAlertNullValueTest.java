package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_GOES_ABOVE;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_DROPS_BELOW;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createKpiWidget;
import static java.lang.String.format;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.mail.MessagingException;

import com.gooddata.md.AttributeElement;
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

public class KpiAlertNullValueTest extends AbstractDashboardTest {

    private static final String DATASET_NAME = "User";

    private static final String DROP_BELOW_LIMIT = "has dropped below the set limit";
    private static final String GONE_ABOVE_LIMIT = "has gone above the set limit";

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
                .rows("GoodData1", "1000", "2015-09-01")
                .rows("GoodData2", "-1000", "2015-09-01")
                .saveToDisc(testParams.getCsvFolder());

        uploadCSV(csvFilePath);

        String numberFactUri = getMdService().getObjUri(getProject(), Fact.class, title("Number"));
        Attribute firstNameAttribute = getMdService().getObj(getProject(), Attribute.class, title("Firstname"));

        List<String> firstNameValues = getMdService()
                .getAttributeElements(firstNameAttribute)
                .stream()
                .map(AttributeElement::getUri)
                .collect(toList());

        String dateDatasetUri = getMdService().getObjUri(getProject(), Dataset.class,
                identifier("date.dataset.dt"));

        String maqlExpression1 = format("SELECT SUM([%s]) WHERE [%s] = [%s]",
                numberFactUri, firstNameAttribute.getUri(), firstNameValues.get(0));
        String maqlExpression2 = format("SELECT SUM([%s]) WHERE [%s] = [%s]",
                numberFactUri, firstNameAttribute.getUri(), firstNameValues.get(1));

        List<Metric> metrics = Arrays.asList(
                new Metric(generateUniqueMetricName(), maqlExpression1, "#,##0"),
                new Metric(generateUniqueMetricName(), maqlExpression2, "#,##0"),
                new Metric(generateUniqueMetricName(), maqlExpression2, "[=NULL]empty;#,##0"),
                new Metric(generateUniqueMetricName(), maqlExpression1, "#,##0%"),
                new Metric(generateUniqueMetricName(), maqlExpression2, "#,##0%"));

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

    @DataProvider(name = "kpiAlertProvider")
    public Object[][] kpiAlertProvider() {
        return new Object[][] {
            {kpiNames.get(0), TRIGGERED_WHEN_DROPS_BELOW, "2", true},
            {kpiNames.get(1), TRIGGERED_WHEN_GOES_ABOVE, "-2", false},
            {kpiNames.get(2), TRIGGERED_WHEN_GOES_ABOVE, "-2", false},
            {kpiNames.get(3), TRIGGERED_WHEN_DROPS_BELOW, "10", false},
            {kpiNames.get(4), TRIGGERED_WHEN_GOES_ABOVE, "-90", false}
        };
    }

    @Test(dependsOnMethods = "initIndigoDashboardWithKpi", dataProvider = "kpiAlertProvider",
            groups = "precondition")
    public void setAlertForAllKpis(String name, String triggerCondition, String threshold, boolean inviteUser)
            throws ParseException, IOException, JSONException {
        if (inviteUser) {
            addUserToProject(imapUser, UserRoles.ADMIN);

            logout();
            signInAtGreyPages(imapUser, imapPassword);
        }

        initIndigoDashboardsPageWithWidgets()
                .getWidgetByHeadline(Kpi.class, name)
                .openAlertDialog()
                .selectTriggeredWhen(triggerCondition)
                .setThreshold(threshold)
                .setAlert();
    }

    @DataProvider(name = "alertEmailProvider")
    public Object[][] alertEmailProvider() {
        return new Object[][] {
            {kpiNames.get(0), " is -", DROP_BELOW_LIMIT + " 2", true},
            {kpiNames.get(1), " is -", GONE_ABOVE_LIMIT + " -2", false},
            {kpiNames.get(2), " is - empty", GONE_ABOVE_LIMIT + " 2", false},
            {kpiNames.get(3), " is -", DROP_BELOW_LIMIT + " 10%", false},
            {kpiNames.get(4), " is -", GONE_ABOVE_LIMIT + " -90%", false}
        };
    }

    @Test(dependsOnGroups = "precondition", groups = "desktop", dataProvider = "alertEmailProvider")
    public void checkKpiAlertOnNullValue(String name, String emailSubject, String emailContent, boolean updateCsv)
            throws JSONException, MessagingException, IOException {
        if (updateCsv) {
            String updateCsvFilePath = new CsvFile("User-update")
                    .columns(new CsvFile.Column("firstname"), new CsvFile.Column("number"),
                            new CsvFile.Column("Date"))
                    .rows("Chi2", "10100000", "2015-09-01")
                    .saveToDisc(testParams.getCsvFolder());

            logout();
            signIn(canAccessGreyPage(browser), UserRoles.ADMIN);

            updateCsvDataset(DATASET_NAME, updateCsvFilePath);

            logout();
            signInAtGreyPages(imapUser, imapPassword);
        }

        Kpi kpi = initIndigoDashboardsPageWithWidgets().getWidgetByHeadline(Kpi.class, name);

        takeScreenshot(browser, "Kpi-with-name-" + name + "-is-triggered", getClass());
        assertTrue(kpi.isAlertTriggered(), "Kpi with name: " + name + " is not triggered");

        if (!testParams.isClusterEnvironment()) return;

        Document alertEmail = getLastAlertEmailContent(GDEmails.NOREPLY, name + emailSubject);
        assertTrue(doesAlertEmailHaveContent(alertEmail, name + " " + emailContent),
                "Alert email contains invalid content");
    }

    private String generateUniqueMetricName() {
        return "sumOfNumber-" + UUID.randomUUID().toString().substring(0, 6);
    }
}
