package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

public class DragWidgetsTest extends DashboardWithWidgetsTest {
    private static final String WIDGET_SELECTOR_FORMATTER = ".dash-item-%1d";
    private static final String WIDGET_DROPZONE_FORMATTER = ".dropzone.%1s";

    private static final String DROPZONE_NEXT = "next";
    private static final String DROPZONE_PREV = "prev";

    private void dragWidgets(int fromIndex, int toIndex, String dropzoneType) {
        String from = String.format(WIDGET_SELECTOR_FORMATTER, fromIndex);

        String to = String.format(WIDGET_SELECTOR_FORMATTER, toIndex) + ' ' +
                String.format(WIDGET_DROPZONE_FORMATTER, dropzoneType);

        BrowserUtils.dragAndDrop(browser, from, to);
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void basicDragDropTest() {
        initIndigoDashboardsPageWithWidgets()
                .switchToEditMode();

        takeScreenshot(browser, "basicDragDropTest-beforeDragDrop", getClass());
        assertEquals(indigoDashboardsPage.getKpiTitle(0), "Amount");
        assertEquals(indigoDashboardsPage.getKpiTitle(1), "Lost");
        assertEquals(indigoDashboardsPage.getKpiTitle(2), "# of Activities");

        dragWidgets(0, 2, DROPZONE_NEXT);
        indigoDashboardsPage.saveEditModeWithKpis();

        initIndigoDashboardsPageWithWidgets();
        takeScreenshot(browser, "basicDragDropTest-afterDragDrop", getClass());
        assertEquals(indigoDashboardsPage.getKpiTitle(0), "Lost");
        assertEquals(indigoDashboardsPage.getKpiTitle(1), "# of Activities");
        assertEquals(indigoDashboardsPage.getKpiTitle(2), "Amount");
    }

}
