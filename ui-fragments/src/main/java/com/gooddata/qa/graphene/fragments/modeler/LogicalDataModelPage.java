package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.springframework.web.util.UriTemplate;

import java.util.ArrayList;
import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.id;
import static java.lang.String.format;

public class LogicalDataModelPage extends AbstractFragment {
    public static final String URI_TEMPLATE = "/modeler/#/projects/{projectId}";
    private static final String GDC_MODELER = "app-modeler";

    @FindBy(className = "gdc-data-content")
    private DataContent dataContent;

    @FindBy(className = "gd-header-project")
    private WebElement discLink;

    @FindBy(className = "gd-header-menu-item")
    private List<WebElement> headerItemList;

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

    public String getLinkDISC() {
        return discLink.getAttribute("href").toString();
    }

    public List<String> getMenuItems() {
        List<String> listItemName = new ArrayList<String>();
        for(WebElement item: headerItemList) {
            listItemName.add(item.getText());
        }
        return listItemName;
    }

    public boolean isOpenCorrectModelerPage(String id) {
        String currentUrl = browser.getCurrentUrl();
        return currentUrl.contains(format(URI_TEMPLATE, id));
    }
}
