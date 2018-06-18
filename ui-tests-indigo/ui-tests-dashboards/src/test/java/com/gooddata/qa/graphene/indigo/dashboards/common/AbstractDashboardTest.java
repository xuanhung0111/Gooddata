package com.gooddata.qa.graphene.indigo.dashboards.common;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_LOST;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.mail.ImapUtils.getEmailBody;
import static com.gooddata.qa.utils.mail.ImapUtils.waitForMessages;
import static com.google.common.collect.Iterables.getLast;
import static java.lang.String.format;
import static java.util.Collections.singletonList;

import java.io.IOException;
import java.util.List;

import javax.mail.Message;

import com.gooddata.md.Dataset;
import com.gooddata.md.Metric;
import com.gooddata.md.ObjNotFoundException;
import com.gooddata.qa.graphene.entity.kpi.KpiMDConfiguration;

import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.support.FindBy;

import com.gooddata.md.Attribute;
import com.gooddata.md.Restriction;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;

public abstract class AbstractDashboardTest extends GoodSalesAbstractTest {

    protected static final String DATE_FILTER_ALL_TIME = "All time";
    protected static final String DATE_FILTER_THIS_MONTH = "This month";
    protected static final String DATE_FILTER_LAST_MONTH = "Last month";
    protected static final String DATE_FILTER_THIS_QUARTER = "This quarter";
    protected static final String DATE_FILTER_LAST_QUARTER = "Last quarter";
    protected static final String DATE_FILTER_THIS_YEAR = "This year";
    protected static final String DATE_FILTER_LAST_YEAR = "Last year";

    @FindBy(id = IndigoDashboardsPage.MAIN_ID)
    protected IndigoDashboardsPage indigoDashboardsPage;

    @Override
    protected void initProperties() {
        super.initProperties(); // use GoodSales by default
        validateAfterClass = false;
    }

    protected String addWidgetToWorkingDashboard(final String widgetUri) throws JSONException, IOException {
        final IndigoRestRequest indigoRestRequest = new IndigoRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        if (indigoRestRequest.getAnalyticalDashboards().isEmpty()) {
            indigoRestRequest.createAnalyticalDashboard(singletonList(widgetUri));
        } else {
            indigoRestRequest.addWidgetToAnalyticalDashboard(getWorkingDashboardUri(), widgetUri);
        }
        // need widget uri in most of cases which use this helper method
        return widgetUri;
    }

    protected void setAlertForLastKpi(String triggeredWhen, String threshold) {
        waitForFragmentVisible(indigoDashboardsPage)
            .getLastWidget(Kpi.class)
            .openAlertDialog()
            .selectTriggeredWhen(triggeredWhen)
            .setThreshold(threshold)
            .setAlert();
    }

    protected void deleteAlertForLastKpi() {
        waitForFragmentVisible(indigoDashboardsPage)
            .getLastWidget(Kpi.class)
            .openAlertDialog()
            .deleteAlert();
    }

    protected String getWorkingDashboardUri() throws JSONException, IOException {
        // if having more than 1 dashboard, the first one will be working project by default
        return new IndigoRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId()).getAnalyticalDashboards().get(0);
    }

    protected String createInsightWidget(InsightMDConfiguration insightConfig) {
        String visualizationWidgetWrap;
        final IndigoRestRequest indigoRestRequest = new IndigoRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        try {
            visualizationWidgetWrap = indigoRestRequest.createVisualizationWidget(
                    indigoRestRequest.createInsight(insightConfig),
                    insightConfig.getTitle());
        } catch (JSONException | IOException e){
            throw new RuntimeException("There is error while creating Visualization widget" , e);
        }
        return visualizationWidgetWrap;
    }

    protected Document getLastAlertEmailContent(GDEmails from, String subject) {
        return doActionWithImapClient(imapClient -> {
                List<Message> messages = waitForMessages(imapClient, GDEmails.NOREPLY, subject);
                return Jsoup.parse(getEmailBody(getLast(messages)));
        });
    }

    protected boolean doesAlertEmailHaveContent(Document email, String content) {
        return email.getElementsByTag("td")
                .stream()
                .filter(e -> e.text().contains(content))
                .findFirst()
                .isPresent();
    }

    protected String getAttributeDisplayFormUri(String attribute) {
        return getMdService()
                .getObj(getProject(), Attribute.class, Restriction.title(attribute))
                .getDefaultDisplayForm()
                .getUri();
    }

    protected String getDateDatasetUri(final String dataset) {
        return getMdService().getObjUri(getProject(), Dataset.class, title(format("Date (%s)", dataset)));
    }

    protected String createAmountKpi() {
        try {
            getMetricByTitle(METRIC_AMOUNT);
        } catch (ObjNotFoundException e) {
            getMetricCreator().createAmountMetric();
        }

        return createKpiUsingRest(new KpiMDConfiguration.Builder()
                .title(getMetricByTitle(METRIC_AMOUNT).getTitle())
                .metric(getMetricByTitle(METRIC_AMOUNT).getUri())
                .dateDataSet(getDateDatasetUri(DATE_DATASET_CREATED))
                .comparisonType(Kpi.ComparisonType.NO_COMPARISON)
                .comparisonDirection(Kpi.ComparisonDirection.NONE)
                .build());
    }

    protected String createLostKpi() {
        try {
            getMetricByTitle(METRIC_LOST);
        } catch (ObjNotFoundException e) {
            getMetricCreator().createLostMetric();
        }

        return createKpiUsingRest(new KpiMDConfiguration.Builder()
                .title(getMetricByTitle(METRIC_LOST).getTitle())
                .metric(getMetricByTitle(METRIC_LOST).getUri())
                .dateDataSet(getDateDatasetUri(DATE_DATASET_CREATED))
                .comparisonType(Kpi.ComparisonType.LAST_YEAR)
                .comparisonDirection(Kpi.ComparisonDirection.BAD)
                .build());
    }

    protected String createNumOfActivitiesKpi() {
        try {
            getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES);
        } catch (ObjNotFoundException e) {
            getMetricCreator().createNumberOfActivitiesMetric();
        }

        return createKpiUsingRest(new KpiMDConfiguration.Builder()
                .title(getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getTitle())
                .metric(getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri())
                .dateDataSet(getDateDatasetUri(DATE_DATASET_CREATED))
                .comparisonType(Kpi.ComparisonType.PREVIOUS_PERIOD)
                .comparisonDirection(Kpi.ComparisonDirection.GOOD)
                .build());
    }

    protected KpiMDConfiguration createDefaultKpiConfiguration(final Metric metric, final String dateDataset) {
        return new KpiMDConfiguration.Builder()
                .title(metric.getTitle())
                .metric(metric.getUri())
                .dateDataSet(getDateDatasetUri(dateDataset))
                .comparisonType(Kpi.ComparisonType.PREVIOUS_PERIOD)
                .comparisonDirection(Kpi.ComparisonDirection.GOOD)
                .build();
    }

    protected String createKpiUsingRest(final KpiMDConfiguration kpiConfig) {
        String kpiWidget;
        try{
            kpiWidget = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .createKpiWidget(kpiConfig);
        } catch (JSONException | IOException e) {
            throw new RuntimeException("There is error while create Kpi Widget");
        }
        return kpiWidget;
    }

    protected String getKpiDashboardIdentifierByTitle(String title) throws IOException, JSONException {
        return new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .getAnalyticalDashboardIdentifier(title);
    }
}
