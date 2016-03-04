package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
/**
 * Visualization
 */
public class Visualization extends AbstractFragment {
    public static final String MAIN_SELECTOR = ".dash-item.type-visualization";

    @FindBy(className = "visualization-delete")
    private WebElement deleteButton;

    public void clickDelete() {
        deleteButton.click();
    }
}
