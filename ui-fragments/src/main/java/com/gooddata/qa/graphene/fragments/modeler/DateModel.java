package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.lang.String.format;
import static org.openqa.selenium.By.cssSelector;

public class DateModel extends AbstractFragment {
    private static final String ID_MODEL = "g[id = '%s']";

    @FindBy(className = "actions")
    private ModelAction modelAction;

    @FindBy(className = "edit-date-dimension-dialog")
    private EditDateDimensionDialog editDateDimensionDialog;

    public static DateModel getInstance(SearchContext searchContext, String id) {
        return Graphene.createPageFragment(
                DateModel.class, waitForElementVisible(cssSelector((format(ID_MODEL, id))), searchContext));
    }

    public ModelAction getModelAction() {
        waitForElementVisible(modelAction.getRoot());
        return modelAction;
    }

    public EditDateDimensionDialog openEditDateDimensionDialog() {
        Actions driverActions = new Actions(browser);
        driverActions.click(this.getModelAction().editDate()).perform();
        return editDateDimensionDialog;
    }

    public void editURNDate(String newUrn) {
        openEditDateDimensionDialog().changeDateURN(newUrn);
    }
}
