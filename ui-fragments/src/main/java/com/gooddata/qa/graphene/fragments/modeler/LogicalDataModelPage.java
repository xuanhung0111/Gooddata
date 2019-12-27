package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;

import org.springframework.web.util.UriTemplate;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;


public class LogicalDataModelPage extends AbstractFragment {
    public static final String URI_TEMPLATE = "/admin/modeler/#/projects/{projectId}";
    private static final String GDC_MODELER = "gdc-modeler";

    public static String getUri(String projectId) {
        return new UriTemplate(URI_TEMPLATE).expand(projectId).toString();
    }

    public static LogicalDataModelPage getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                LogicalDataModelPage.class, waitForElementVisible(className(GDC_MODELER), searchContext));
    }

    public Canvas getCanvas() {
        return Canvas.getInstance(browser);
    }

    public Sidebar getSidebar() {
        return Sidebar.getInstance(browser);
    }

    public ToolBar getToolbar() {
        return ToolBar.getInstance(browser);
    }
}
