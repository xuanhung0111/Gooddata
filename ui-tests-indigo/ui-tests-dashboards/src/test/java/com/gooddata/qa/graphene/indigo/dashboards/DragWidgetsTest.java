package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.entity.kpi.KpiMDConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonDirection;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonType;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardsGeneralTest;
import com.gooddata.qa.browser.DragAndDropUtils;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.*;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;

public class DragWidgetsTest extends DashboardsGeneralTest {

    private static final String WIDGET_SELECTOR_FORMATTER = ".dash-item-%1d";
    private static final String WIDGET_DROPZONE_FORMATTER = ".dropzone.%1s";

    private static final String DROPZONE_NEXT = "next";
    private static final String DROPZONE_PREV = "prev";

    @Test(dependsOnMethods = "createProject")
    public void initIndigoDashboardWithKpis() throws JSONException, IOException {
        Metric metric = getMdService().getObj(getProject(), Metric.class, title("Amount"));

        String dateDimensionUri = getDateDataSetCreatedUri(getGoodDataClient(), testParams.getProjectId());

        List<String> kpiUris = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            kpiUris.add(createKpiWidget(getRestApiClient(), testParams.getProjectId(),
                    new KpiMDConfiguration.Builder()
                            .title("Drag-Drop-" + i)
                            .metric(metric.getUri())
                            .dateDataSet(dateDimensionUri)
                            .comparisonType(ComparisonType.PREVIOUS_PERIOD)
                            .comparisonDirection(ComparisonDirection.GOOD)
                            .build()));
        }

        createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(), kpiUris);
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

    @Test(dependsOnMethods = {"initIndigoDashboardWithKpis"}, dataProvider = "dragKpiProvider", groups = {"desktop"})
    public void basicDragDropTest(int fromIndex, int toIndex, String dropzone, String screenshotName) {
        initIndigoDashboardsPageWithWidgets()
                .switchToEditMode();

        String sourceKpiHeadline = waitForFragmentVisible(indigoDashboardsPage)
                .getKpiByIndex(fromIndex)
                .getHeadline();
        String targetKpiHeadline = indigoDashboardsPage
                .getKpiByIndex(toIndex)
                .getHeadline();

        dragWidgets(fromIndex, toIndex, dropzone);
        indigoDashboardsPage.saveEditModeWithKpis();

        takeScreenshot(browser, screenshotName, getClass());

        assertEquals(indigoDashboardsPage
                .getKpiTitle(getSourceKpiIndexAfterDragDropTo(toIndex, dropzone)), sourceKpiHeadline);

        assertEquals(indigoDashboardsPage
                .getKpiTitle(getTargetKpiIndexAfterDragDropTo(toIndex, dropzone)), targetKpiHeadline);
    }

    private void dragWidgets(int fromIndex, int toIndex, String dropzoneType) {
        String from = format(WIDGET_SELECTOR_FORMATTER, fromIndex);

        String to = format(WIDGET_SELECTOR_FORMATTER, toIndex) + ' ' +
                format(WIDGET_DROPZONE_FORMATTER, dropzoneType);

        DragAndDropUtils.dragAndDrop(browser, from, to);
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
