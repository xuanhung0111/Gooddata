package com.gooddata.qa.graphene.dashboards;

import com.gooddata.qa.graphene.AbstractDashboardWidgetTest;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.filter.FilterItemContent;
import com.gooddata.qa.mdObjects.dashboard.tab.FilterItem;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.java.Builder;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class DynamicImageOnDashboardTest extends AbstractDashboardWidgetTest {

    private final static String IMAGE = "Image";
    private final static String IMAGE_TITLE = "IMAGE";
    private final static String IMAGE_SOURCE_1 = "source=web&url=https://s3.amazonaws.com/gdc-testing-public/images/publicImage.png";
    private final static String IMAGE_SOURCE_2 = "source=web&url=https://s3.amazonaws.com/gdc-testing-public/images/publicImage2.png";
    private final static String IMAGE_SOURCE_3 = "source=web&url=https://s3.amazonaws.com/gdc-testing-public/images/publicImage3.png";
    private String dashboardName;
    private DashboardRestRequest dashboardRequest;

    @Override
    protected void customizeProject() throws Throwable {
        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
        String filePath = getFilePathFromResource("/" + ResourceDirectory.DYNAMIC_IMAGES + "/image_url.csv");
        uploadCSV(filePath);
        takeScreenshot(browser, "uploaded-image-file", getClass());
        initAttributePage().initAttribute(IMAGE).selectLabelType(IMAGE);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addDynamicFromPubliclyAccessibleImages() throws IOException {
        createDashboardHasFilter(createMultipleValuesFilter(getAttributeByTitle(IMAGE)));
        initDashboardsPage().selectDashboard(dashboardName).addWebContentToDashboard(
                "https://" + testParams.getHost() + "/gdc/app/projects/%CURRENT_PROJECT_HASH%/images?displayFormIdentifier=" +
                "label.csv_image_url.image&attributeElementUri=%FILTER_VALUE(attr.csv_image_url.image)%");
        getFilter(IMAGE_TITLE).openPanel().changeAttributeFilterValues(IMAGE_SOURCE_1);
        Screenshots.takeScreenshot(browser, "add-dynamic-from-public-accessible-images", getClass());
        assertTrue(IMAGE_SOURCE_1.contains(dashboardsPage.getLastEmbeddedWidget().getImageUri()),
                "Image should be loaded");

        getFilter(IMAGE_TITLE).openPanel().changeAttributeFilterValues(IMAGE_SOURCE_1, IMAGE_SOURCE_2);
        assertEquals(dashboardsPage.getLastEmbeddedWidget().getContentBodyAsText(), "No image available for this option");

        getFilter(IMAGE_TITLE).openPanel().changeAttributeFilterValues(IMAGE_SOURCE_3);
        assertEquals(dashboardsPage.getLastEmbeddedWidget().getContentBodyAsText(), "We're sorry but something went wrong...");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addDynamicViaMacroForHostname() throws IOException {
        createDashboardHasFilter(createMultipleValuesFilter(getAttributeByTitle(IMAGE)));
        initDashboardsPage().selectDashboard(dashboardName).addWebContentToDashboard(
                "https://%HOST_NAME%/gdc/app/projects/%CURRENT_PROJECT_HASH%/images?displayFormIdentifier=" +
                "label.csv_image_url.image&attributeElementUri=%FILTER_VALUE(attr.csv_image_url.image)%");
        getFilter("IMAGE").openPanel().changeAttributeFilterValues(IMAGE_SOURCE_1);
        Screenshots.takeScreenshot(browser, "add-dynamic-via-macro-for-host-name", getClass());
        assertTrue(IMAGE_SOURCE_1.contains(dashboardsPage.getLastEmbeddedWidget().getImageUri()),
                "Image should be loaded");

        getFilter(IMAGE_TITLE).openPanel().changeAttributeFilterValues(IMAGE_SOURCE_1, IMAGE_SOURCE_2);
        assertEquals(dashboardsPage.getLastEmbeddedWidget().getContentBodyAsText(), "No image available for this option");

        getFilter(IMAGE_TITLE).openPanel().changeAttributeFilterValues(IMAGE_SOURCE_3);
        assertEquals(dashboardsPage.getLastEmbeddedWidget().getContentBodyAsText(), "We're sorry but something went wrong...");
    }

    private void createDashboardHasFilter(FilterItemContent filter) throws IOException {
        dashboardName = generateDashboardName();
        Dashboard dashboard =
                Builder.of(Dashboard::new).with(dash -> {
                    dash.setName(dashboardName);
                    dash.addTab(Builder.of(com.gooddata.qa.mdObjects.dashboard.tab.Tab::new)
                            .with(tab -> {
                                FilterItem filterItem = Builder.of(FilterItem::new).with(item -> {
                                    item.setContentId(filter.getId());
                                    item.setPosition(TabItem.ItemPosition.LEFT);
                                }).build();
                                tab.addItem(filterItem);
                            })
                            .build());
                    dash.addFilter(filter);
                }).build();

        dashboardRequest.createDashboard(dashboard.getMdObject());
    }
}
