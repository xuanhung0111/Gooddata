package com.gooddata.qa.graphene.modeler;

import com.gooddata.qa.graphene.fragments.modeler.Canvas;
import com.gooddata.qa.graphene.fragments.modeler.LogicalDataModelPage;
import com.gooddata.qa.graphene.fragments.modeler.Sidebar;
import com.gooddata.qa.graphene.fragments.modeler.ToolBar;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class LogicalDataModelPageTest extends AbstractLDMPageTest {
    private LogicalDataModelPage ldmPage;

    @Test(dependsOnGroups = {"createProject"})
    public void initTest(){
        ldmPage = initLogicalDataModelPage();
    }

    @Test(dependsOnMethods = {"initTest"})
    public void initialPageTest() {
        Canvas canvas = ldmPage.getCanvas();
        String textCanvas = canvas.getTextBlankCanvas();
        assertThat(textCanvas, containsString("Get started"));
        assertThat(textCanvas, containsString("Drag items from the left panel to\n" +
                "canvas to build your model."));

        Sidebar sidebar = ldmPage.getSidebar();
        assertTrue(sidebar.isDatasetButtonVisible());
        assertTrue(sidebar.isDateButtonVisible());

        ToolBar toolBar = ldmPage.getToolbar();
        assertTrue(toolBar.isButtonsVisible());
    }

}
