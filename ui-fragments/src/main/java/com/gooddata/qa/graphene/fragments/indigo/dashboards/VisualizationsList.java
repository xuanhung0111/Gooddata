package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import org.openqa.selenium.support.FindBy;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import java.util.List;
import org.openqa.selenium.WebElement;

public class VisualizationsList extends AbstractFragment {

    public static final String MAIN_CLASS = "visualizations-list-item";

    @FindBy(className = MAIN_CLASS)
    private List<WebElement> visualizationsListItems;

    public List<WebElement> getVisualizationsListItems() {
        return visualizationsListItems;
    }
}
