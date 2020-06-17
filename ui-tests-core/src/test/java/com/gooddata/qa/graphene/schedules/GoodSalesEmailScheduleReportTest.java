package com.gooddata.qa.graphene.schedules;

import com.gooddata.sdk.model.md.AttributeElement;
import com.gooddata.sdk.model.md.report.AttributeInGrid;
import com.gooddata.sdk.model.md.report.Filter;
import com.gooddata.sdk.model.md.report.GridReportDefinitionContent;
import com.gooddata.sdk.model.md.report.MetricElement;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.gooddata.sdk.model.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_MONTH_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_LOST_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPEN_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPPORTUNITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_ACTIVITY_LEVEL;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_STAGE_NAME;
import static com.gooddata.qa.utils.mail.ImapUtils.getLastEmail;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class GoodSalesEmailScheduleReportTest extends AbstractGoodSalesEmailSchedulesTest {

    private static final String REPORT_APPLIES_FILTER = "Report applies filter";
    private static final String F_STAGE_NAME = "FStage Name";
    private static final String SHORT_LIST = "Short List";
    private static final String CLOSED_LOST = "Closed Lost";
    private String promptUri;
    private CommonRestRequest commonRestRequest;
    private String today;

    @BeforeClass
    public void setUp() {
        identification = ": " + testParams.getHost() + " - " + testParams.getTestIdentification();
        attachmentsDirectory =
                new File(System.getProperty("maven.project.build.directory", "./target/attachments"));
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws IOException, JSONException {
        addUserToProject(imapUser, UserRoles.ADMIN);
    }

    @Override
    protected void customizeProject() throws Throwable {
        commonRestRequest = new CommonRestRequest(getAdminRestClient(), testParams.getProjectId());
        getMetricCreator().createAmountMetric();
        getMetricCreator().createNumberOfLostOppsMetric();
        getMetricCreator().createNumberOfOpenOppsMetric();
        getMetricCreator().createNumberOfOpportunitiesMetric();
        getReportCreator().createActiveLevelReport();
        getReportCreator().createAmountByStageNameReport();
        getReportCreator().createEmptyReport();
        getReportCreator().createTooLargeReport();
        promptUri = getVariableCreator().createFilterVariable(F_STAGE_NAME, getAttributeByTitle(ATTR_STAGE_NAME).getUri(),
                format("[%s] IN (%s)", getAttributeByTitle(ATTR_STAGE_NAME).getUri(),
                        getAttributeElements(ATTR_STAGE_NAME, SHORT_LIST, CLOSED_LOST).stream()
                                .map(element -> "[" + element.getUri() + "]")
                                .collect(Collectors.joining(", "))));
    }

    @Test(dependsOnGroups = "createProject", groups = "schedules")
    public void signInImapUser() throws JSONException, IOException {
        logout();
        signInAtGreyPages(imapUser, imapPassword);
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .updateProjectConfiguration("newUIEnabled", "classic");
    }

    @Test(dependsOnMethods = "signInImapUser")
    public void sendScheduleReportHasFilter() throws IOException, MessagingException {
        String emailSubject = "Mixing-Report" + identification;
        String reportApplyPromptFilter = createReportAppliesFilter(new Filter(format("[%s]", promptUri)));
        String reportApplyRankingFilter = createReportAppliesFilter(new Filter(
                format("TOP(3) IN (SELECT [%s] BY [%s], ALL OTHER)",
                        getMetricByTitle(METRIC_AMOUNT).getUri(), getAttributeByTitle(ATTR_STAGE_NAME).getUri())));
        String reportApplyRangeFilter = createReportAppliesFilter(new Filter(
                format("(SELECT [%s] BY [%s], ALL OTHER) >= 20000000",
                        getMetricByTitle(METRIC_AMOUNT).getUri(), getAttributeByTitle(ATTR_STAGE_NAME).getUri())));
        initEmailSchedulesPage().scheduleNewReportEmail(singletonList(imapUser), emailSubject,
                "Scheduled email test - reports.",
                asList(reportApplyPromptFilter, reportApplyRangeFilter, reportApplyRankingFilter), ExportFormat.ALL);
        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        today = DateRange.getCurrentDate();
        List<Message> messages = waitForScheduleMessages(emailSubject, 1);

        File pdfFile = getPdfFileFrom(messages.get(0), reportApplyPromptFilter);
        assertEquals(asList(getContentFrom(pdfFile).split("\n")),
                asList("Stage Name Amount", "Short List $5,612,062.60", "Closed Lost $42,470,571.16",
                        reportApplyPromptFilter + " " + today, "Page 1/1"));

        String regex = ".*[0-3]?[0-9]-[0-3]?[0-9]-[0-9]{4} [0-9]{4}[a|p]m.pdf";
        assertTrue(Pattern.compile(regex).matcher(pdfFile.getName()).matches(), "File name is wrong format");
        assertEquals(asList(getContentFrom(getPdfFileFrom(messages.get(0), reportApplyRankingFilter)).split("\n")),
                asList("Stage Name Amount", "Interest $18,447,266.14", "Closed Won $38,310,753.45",
                        "Closed Lost $42,470,571.16", reportApplyRankingFilter + " " + today, "Page 1/1"));
        assertEquals(asList(getContentFrom(getPdfFileFrom(messages.get(0), reportApplyRangeFilter)).split("\n")),
                asList("Stage Name Amount", "Closed Won $38,310,753.45", "Closed Lost $42,470,571.16",
                        reportApplyRangeFilter + " " + today, "Page 1/1"));
    }

    @Test(dependsOnMethods = "signInImapUser")
    public void sendScheduleReportHasLongTitle() throws IOException, MessagingException {
        String longTitle = "Amount by stage name report with over length of title";
        String specialTitle = "!@#$%^&*%s;,.:'";
        String emailSubject = "Special-Title-Report" + identification;

        createReportAppliesFilter(longTitle, new Filter(format("[%s]", promptUri)));
        createReportAppliesFilter(specialTitle, new Filter(
                format("TOP(3) IN (SELECT [%s] BY [%s], ALL OTHER)",
                        getMetricByTitle(METRIC_AMOUNT).getUri(), getAttributeByTitle(ATTR_STAGE_NAME).getUri())));
        initEmailSchedulesPage().scheduleNewReportEmail(singletonList(imapUser), emailSubject,
                "Scheduled email test - reports.", asList(longTitle, specialTitle), ExportFormat.ALL);
        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        today = DateRange.getCurrentDate();
        List<Message> messages = waitForScheduleMessages(emailSubject, 1);
        assertEquals(getNumberOfPartsFrom(messages.get(0)), 6);

        File pdfFile = getPdfFileFrom(messages.get(0), longTitle.substring(0, 50));
        assertEquals(asList(getContentFrom(pdfFile).split("\n")), asList("Stage Name Amount",
                "Short List $5,612,062.60", "Closed Lost $42,470,571.16", longTitle + " " + today, "Page 1/1"));
        assertEquals(pdfFile.getName(), longTitle.substring(0, 50) + ".pdf");
        assertEquals(asList(getContentFrom(getPdfFileFrom(messages.get(0), "!@#$%^&_%s;___")).split("\n")),
                asList("Stage Name Amount", "Interest $18,447,266.14", "Closed Won $38,310,753.45",
                        "Closed Lost $42,470,571.16", specialTitle + " " + today, "Page 1/1"));
    }

    @Test(dependsOnMethods = "signInImapUser")
    public void sendScheduleHeadLineReport() throws IOException, MessagingException {
        String emailSubject = "Headline report" + identification;
        String reportApplyPromptFilter = createReportAppliesFilter(new Filter(format("[%s]", promptUri)));
        String reportApplyRankingFilter = createReportAppliesFilter(new Filter(
                format("TOP(3) IN (SELECT [%s] BY [%s], ALL OTHER)",
                        getMetricByTitle(METRIC_AMOUNT).getUri(), getAttributeByTitle(ATTR_STAGE_NAME).getUri())));
        String reportApplyRangeFilter = createReportAppliesFilter(new Filter(
                format("(SELECT [%s] BY [%s], ALL OTHER) >= 20000000",
                        getMetricByTitle(METRIC_AMOUNT).getUri(), getAttributeByTitle(ATTR_STAGE_NAME).getUri())));
        initReportsPage().openReport(reportApplyPromptFilter).selectReportVisualisation(ReportTypes.HEADLINE).saveReport();
        initReportsPage().openReport(reportApplyRankingFilter).selectReportVisualisation(ReportTypes.HEADLINE).saveReport();
        initReportsPage().openReport(reportApplyRangeFilter).selectReportVisualisation(ReportTypes.HEADLINE).saveReport();

        initEmailSchedulesPage().scheduleNewReportEmail(singletonList(imapUser), emailSubject,
                "Scheduled email test - reports.",
                asList(reportApplyPromptFilter, reportApplyRangeFilter, reportApplyRankingFilter), ExportFormat.PDF);
        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        today = DateRange.getCurrentDate();
        List<Message> messages = waitForScheduleMessages(emailSubject, 1);

        assertEquals(getNumberOfPartsFrom(messages.get(0)), 3);
        assertEquals(asList(getContentFrom(getPdfFileFrom(messages.get(0), reportApplyPromptFilter)).split("\n")),
                asList("Amount", "Amount $48,082,633.76", reportApplyPromptFilter + " " + today, "Page 1/1"));
        assertEquals(asList(getContentFrom(getPdfFileFrom(messages.get(0), reportApplyRankingFilter)).split("\n")),
                asList("Amount", "Amount $99,228,590.75", reportApplyRankingFilter + " " + today, "Page 1/1"));
        assertEquals(asList(getContentFrom(getPdfFileFrom(messages.get(0), reportApplyRangeFilter)).split("\n")),
                asList("Amount", "Amount $80,781,324.61", reportApplyRangeFilter + " " + today, "Page 1/1"));
    }

    @Test(dependsOnMethods = "signInImapUser")
    public void sendScheduleChartReport() throws IOException, MessagingException {
        String emailSubject = "Chart report" + identification;
        String reportApplyPromptFilter = createReportAppliesFilter(new Filter(format("[%s]", promptUri)));
        String reportApplyRankingFilter = createReportAppliesFilter(new Filter(
                format("TOP(3) IN (SELECT [%s] BY [%s], ALL OTHER)",
                        getMetricByTitle(METRIC_AMOUNT).getUri(), getAttributeByTitle(ATTR_STAGE_NAME).getUri())));
        String reportApplyRangeFilter = createReportAppliesFilter(new Filter(
                format("(SELECT [%s] BY [%s], ALL OTHER) >= 20000000",
                        getMetricByTitle(METRIC_AMOUNT).getUri(), getAttributeByTitle(ATTR_STAGE_NAME).getUri())));
        initReportsPage().openReport(reportApplyPromptFilter).selectReportVisualisation(ReportTypes.LINE).saveReport();
        initReportsPage().openReport(reportApplyRankingFilter).selectReportVisualisation(ReportTypes.LINE).saveReport();
        initReportsPage().openReport(reportApplyRangeFilter).selectReportVisualisation(ReportTypes.LINE).saveReport();

        initEmailSchedulesPage().scheduleNewReportEmail(singletonList(imapUser), emailSubject,
                "Scheduled email test - reports.",
                asList(reportApplyPromptFilter, reportApplyRangeFilter, reportApplyRankingFilter), ExportFormat.PDF);
        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        today = DateRange.getCurrentDate();
        List<Message> messages = waitForScheduleMessages(emailSubject, 1);

        assertEquals(asList(getContentFrom(getPdfFileFrom(messages.get(0), reportApplyPromptFilter)).split("\n")),
                asList("Short List Closed Lost", "Stage Name", "A", "m", "ou", "nt", "$0.00", "$11,000,000.00",
                        "$22,000,000.00", "$33,000,000.00", "$44,000,000.00", reportApplyPromptFilter + " " + today, "Page 1/1"));
        assertEquals(asList(getContentFrom(getPdfFileFrom(messages.get(0), reportApplyRankingFilter)).split("\n")),
                asList("Interest Closed Won Closed Lost", "Stage Name", "A", "m", "ou", "nt", "$0.00", "$11,000,000.00",
                        "$22,000,000.00", "$33,000,000.00", "$44,000,000.00", reportApplyRankingFilter + " " + today, "Page 1/1"));
        assertEquals(asList(getContentFrom(getPdfFileFrom(messages.get(0), reportApplyRangeFilter)).split("\n")),
                asList("Closed Won Closed Lost", "Stage Name", "A", "m", "ou", "nt", "$0.00", "$11,000,000.00",
                        "$22,000,000.00", "$33,000,000.00", "$44,000,000.00", reportApplyRangeFilter + " " + today, "Page 1/1"));
    }

    @Test(dependsOnMethods = "signInImapUser")
    public void editFormatScheduleReport() throws IOException, MessagingException {
        String emailSubject = "Report to XLSX" + identification;
        initEmailSchedulesPage().scheduleNewReportEmail(singletonList(imapUser), emailSubject,
                "Scheduled email test - reports.",
                asList(REPORT_ACTIVITY_LEVEL, REPORT_AMOUNT_BY_STAGE_NAME), ExportFormat.SCHEDULES_EMAIL_EXCEL_XLSX);
        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        today = DateRange.getCurrentDate();
        List<Message> messages = waitForScheduleMessages(emailSubject, 1);
        assertEquals(getNumberOfPartsFrom(messages.get(0)), 2);
    }

    @Test(dependsOnMethods = "signInImapUser")
    public void sendScheduleReportWithLargeSize() throws IOException, MessagingException {
        String emailSubject = "Large-Report" + identification;
        String reportTitle = "Large Report";
        createReport(GridReportDefinitionContent.create("Large Report",
                singletonList(METRIC_GROUP),
                Arrays.asList(
                        new AttributeInGrid(getAttributeByTitle(ATTR_MONTH_SNAPSHOT)),
                        new AttributeInGrid(getAttributeByTitle(ATTR_YEAR_SNAPSHOT)),
                        new AttributeInGrid(getAttributeByTitle(ATTR_PRODUCT))),
                asList(
                        new MetricElement(getMetricByTitle(METRIC_NUMBER_OF_LOST_OPPS)),
                        new MetricElement(getMetricByTitle(METRIC_NUMBER_OF_OPEN_OPPS)),
                        new MetricElement(getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES)))));

        initEmailSchedulesPage().scheduleNewReportEmail(singletonList(imapUser), emailSubject,
                "Scheduled email test - reports.", singletonList("Large Report"), ExportFormat.ALL);
        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        today = DateRange.getCurrentDate();
        List<Message> messages = waitForScheduleMessages(emailSubject, 1);

        assertEquals(asList(getContentFrom(getPdfFileFrom(messages.get(0), "Large Report")).split("\n")), asList(
                "Month (Snapshot) Year (Snapshot) Product # of Lost Opps. # of Open Opps.", "Jan 2011 CompuSci 133 57",
                "Educationly 136 65", "Explorer 159 45", "Grammar Plus 37 26", "PhoenixSoft 60 24", "WonderKid 53 30",
                "2012 CompuSci 343 138", "Educationly 353 151", "Explorer 393 153", "Grammar Plus 113 48",
                "PhoenixSoft 139 54", "WonderKid 132 68", "Feb 2011 CompuSci 140 61", "Educationly 145 66",
                "Explorer 169 46", "Grammar Plus 40 27", "PhoenixSoft 62 26", "WonderKid 56 32", "2012 CompuSci 353 156",
                "Educationly 367 169", "Explorer 413 170", "Grammar Plus 120 48", "PhoenixSoft 143 61", "WonderKid 132 71",
                "Mar 2011 CompuSci 153 63", "Educationly 167 70", "Explorer 190 51", "Grammar Plus 47 27", "PhoenixSoft 74 27",
                "WonderKid 67 34", "2012 CompuSci 374 167", "Educationly 384 181", "Explorer 435 186", "Grammar Plus 132 52",
                "PhoenixSoft 149 64", "WonderKid 139 80", "Apr 2011 CompuSci 167 67", "Educationly 178 73", "Explorer 198 59",
                "Grammar Plus 53 28", "PhoenixSoft 76 26", "WonderKid 69 33", "2012 CompuSci 404 187", "Educationly 403 209",
                "Explorer 456 202", "Grammar Plus 141 67", "PhoenixSoft 157 68", "WonderKid 149 86", "May 2011 CompuSci 176 74",
                "Educationly 188 78", reportTitle + " " + today, "Page 1/6",
                "Month (Snapshot) Year (Snapshot) Product # of Lost Opps. # of Open Opps.", "May 2011", "Explorer 202 69",
                "Grammar Plus 55 31", "PhoenixSoft 80 28", "WonderKid 75 34", "2012 CompuSci 419 246", "Educationly 411 224",
                "Explorer 474 214", "Grammar Plus 146 70", "PhoenixSoft 163 72", "WonderKid 157 92", "Jun 2010 CompuSci 21 31",
                "Educationly 25 40", "Explorer 35 30", "Grammar Plus 3 13", "PhoenixSoft 13 16", "WonderKid 8 19",
                "2011 CompuSci 195 73", "Educationly 202 86", "Explorer 223 77", "Grammar Plus 60 32", "PhoenixSoft 85 31",
                "WonderKid 77 39", "Jul 2010 CompuSci 35 34", "Educationly 39 44", "Explorer 52 33", "Grammar Plus 8 15",
                "PhoenixSoft 18 16", "WonderKid 18 21", "2011 CompuSci 208 82", "Educationly 216 94", "Explorer 231 89",
                "Grammar Plus 65 33", "PhoenixSoft 87 34", "WonderKid 78 42", "Aug 2010 CompuSci 55 42", "Educationly 61 53",
                "Explorer 72 36", "Grammar Plus 15 19", "PhoenixSoft 25 14", "WonderKid 30 22", "2011 CompuSci 217 82",
                "Educationly 228 104", "Explorer 240 94", "Grammar Plus 69 32", "PhoenixSoft 88 37", "WonderKid 83 46",
                "Sep 2010 CompuSci 77 50", "Educationly 75 64", "Explorer 94 39", "Grammar Plus 21 20", "Page 2/6",
                "Month (Snapshot) Year (Snapshot) Product # of Lost Opps. # of Open Opps.",
                "Sep 2010", "PhoenixSoft 32 16", "WonderKid 39 21", "2011 CompuSci 241 94", "Educationly 254 115",
                "Explorer 266 107", "Grammar Plus 78 34", "PhoenixSoft 102 38", "WonderKid 89 49", "Oct 2010 CompuSci 88 51",
                "Educationly 86 65", "Explorer 111 41", "Grammar Plus 26 21", "PhoenixSoft 42 19", "WonderKid 41 25",
                "2011 CompuSci 254 102", "Educationly 268 126", "Explorer 286 113", "Grammar Plus 81 35", "PhoenixSoft 110 44",
                "WonderKid 90 56", "Nov 2010 CompuSci 105 56", "Educationly 99 65", "Explorer 123 44", "Grammar Plus 27 25",
                "PhoenixSoft 46 20", "WonderKid 43 27", "2011 CompuSci 277 110", "Educationly 286 134", "Explorer 308 120",
                "Grammar Plus 89 39", "PhoenixSoft 114 45", "WonderKid 98 59", "Dec 2010 CompuSci 127 53", "Educationly 120 65",
                "Explorer 147 40", "Grammar Plus 33 25", "PhoenixSoft 51 22", "WonderKid 48 27", "2011 CompuSci 324 119",
                "Educationly 339 137", "Explorer 366 128", "Grammar Plus 107 39", "PhoenixSoft 130 48", "WonderKid 122 63",
                "Page 3/6", "Month (Snapshot) Year (Snapshot) Product", "Jan 2011 CompuSci", "Educationly",
                "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid", "2012 CompuSci", "Educationly", "Explorer",
                "Grammar Plus", "PhoenixSoft", "WonderKid", "Feb 2011 CompuSci", "Educationly", "Explorer", "Grammar Plus",
                "PhoenixSoft", "WonderKid", "2012 CompuSci", "Educationly", "Explorer", "Grammar Plus", "PhoenixSoft",
                "WonderKid", "Mar 2011 CompuSci", "Educationly", "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid",
                "2012 CompuSci", "Educationly", "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid", "Apr 2011 CompuSci",
                "Educationly", "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid", "2012 CompuSci", "Educationly",
                "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid", "May 2011 CompuSci", "Educationly",
                "# of Opportunities", "413", "430", "424", "144", "177", "170", "1,123", "1,143", "1,194", "404",
                "449", "419", "460", "478", "465", "165", "198", "188", "1,187", "1,220", "1,267", "424", "476",
                "433", "503", "540", "524", "181", "222", "219", "1,255", "1,285", "1,335", "449", "497", "465",
                "556", "577", "577", "194", "235", "230", "1,349", "1,376", "1,410", "485", "531", "495", "619",
                "640", "Page 4/6", "Month (Snapshot) Year (Snapshot) Product", "May 2011", "Explorer", "Grammar Plus",
                "PhoenixSoft", "WonderKid", "2012 CompuSci", "Educationly", "Explorer", "Grammar Plus", "PhoenixSoft",
                "WonderKid", "Jun 2010 CompuSci", "Educationly", "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid",
                "2011 CompuSci", "Educationly", "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid", "Jul 2010 CompuSci",
                "Educationly", "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid", "2011 CompuSci", "Educationly",
                "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid", "Aug 2010 CompuSci", "Educationly", "Explorer",
                "Grammar Plus", "PhoenixSoft", "WonderKid", "2011 CompuSci", "Educationly", "Explorer", "Grammar Plus",
                "PhoenixSoft", "WonderKid", "Sep 2010 CompuSci", "Educationly", "Explorer", "Grammar Plus",
                "# of Opportunities", "637", "217", "257", "248", "1,516", "1,425", "1,465", "505", "560", "521",
                "66", "79", "82", "21", "34", "32", "659", "680", "700", "231", "280", "262", "98", "114", "120",
                "41", "47", "51", "711", "737", "759", "252", "293", "279", "168", "179", "178", "62", "68", "81",
                "764", "803", "812", "272", "311", "302", "230", "229", "226", "76", "Page 5/6",
                "Month (Snapshot) Year (Snapshot) Product", "Sep 2010", "PhoenixSoft", "WonderKid", "2011 CompuSci",
                "Educationly", "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid", "Oct 2010 CompuSci", "Educationly",
                "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid", "2011 CompuSci", "Educationly", "Explorer",
                "Grammar Plus", "PhoenixSoft", "WonderKid", "Nov 2010 CompuSci", "Educationly", "Explorer", "Grammar Plus",
                "PhoenixSoft", "WonderKid", "2011 CompuSci", "Educationly", "Explorer", "Grammar Plus", "PhoenixSoft",
                "WonderKid", "Dec 2010 CompuSci", "Educationly", "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid",
                "2011 CompuSci", "Educationly", "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid", "# of Opportunities",
                "85", "97", "821", "864", "878", "297", "334", "317", "268", "273", "267", "97", "111", "112", "880",
                "924", "942", "317", "355", "336", "320", "322", "327", "109", "130", "130", "955", "989", "1,005",
                "342", "381", "353", "371", "373", "374", "126", "146", "147", "1,046", "1,079", "1,103", "373",
                "420", "388", "Page 6/6"));
    }

    @Test(dependsOnMethods = "signInImapUser")
    public void sendScheduleReportHasOnlyAttribute() throws IOException, MessagingException {
        String reportHasAttribute = "Report has only attribute";
        createReport(GridReportDefinitionContent.create(reportHasAttribute,
                Collections.emptyList(),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_DEPARTMENT))),
                Collections.emptyList()));
        String emailSubject = reportHasAttribute + identification;
        initEmailSchedulesPage().scheduleNewReportEmail(singletonList(imapUser), emailSubject,
                "Scheduled email test - reports.",
                singletonList(reportHasAttribute), ExportFormat.SCHEDULES_EMAIL_INLINE_MESSAGE);
        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        today = DateRange.getCurrentDate();
        Message message = waitForScheduleMessages(emailSubject, 1).get(0);
        final String messageSubject = doActionWithImapClient(imapClient ->
                getLastEmail(imapClient, GDEmails.NOREPLY, emailSubject, 1).getSubject());
        final String messageBody = doActionWithImapClient(imapClient ->
                getLastEmail(imapClient, GDEmails.NOREPLY, emailSubject, 1).getBody());
        assertEquals(getNumberOfPartsFrom(message), 0);
        assertThat(messageSubject, not(containsString("Scheduled e-mail failed")));
        assertThat(messageBody, containsString(reportHasAttribute));
    }

    private String createReportAppliesFilter(Filter filter) {
        String reportTitle = REPORT_APPLIES_FILTER + " " + generateHashString();
        createReportAppliesFilter(reportTitle, filter);
        return reportTitle;
    }

    private void createReportAppliesFilter(String reportTitle, Filter filter) {
        createReport(GridReportDefinitionContent.create(reportTitle,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_STAGE_NAME))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT))),
                singletonList(filter)));
    }

    private List<AttributeElement> getAttributeElements(String attribute, String... filteredValues) {
        return getMdService().getAttributeElements(getAttributeByTitle(attribute)).stream()
                .filter(element -> asList(filteredValues).contains(element.getTitle()))
                .collect(Collectors.toList());
    }
}
