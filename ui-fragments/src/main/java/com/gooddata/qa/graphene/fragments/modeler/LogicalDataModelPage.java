package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.springframework.web.util.UriTemplate;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.id;


public class LogicalDataModelPage extends AbstractFragment {
    public static final String URI_TEMPLATE = "/admin/modeler/#/projects/{projectId}";
    private static final String GDC_MODELER = "app-modeler";

    @FindBy(className = "gdc-data-content")
    private DataContent dataContent;

    public static String getUri(String projectId) {
        return new UriTemplate(URI_TEMPLATE).expand(projectId).toString();
    }

    public static LogicalDataModelPage getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                LogicalDataModelPage.class, waitForElementVisible(id(GDC_MODELER), searchContext));
    }

    public DataContent getDataContent() {
        waitForElementVisible(dataContent.getRoot());
        return dataContent;
    }

    public LogicalDataModelPage dragItem(WebElement parentSource, WebElement childsource, WebElement target) {
        getActions().clickAndHold(parentSource).moveToElement(childsource).release(target).build().perform();
        return this;
    }
}
