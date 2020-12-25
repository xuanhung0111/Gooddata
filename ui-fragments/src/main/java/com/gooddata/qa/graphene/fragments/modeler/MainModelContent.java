package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.ElementUtils.scrollElementIntoView;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementEnabled;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class MainModelContent extends AbstractFragment {
    private static final String JOINT_PAPER = "joint-paper";

    @FindBy(css = ".joint-text-editor > .textarea-container > textarea")
    private WebElement textEditor;

    @FindBy(css = "#v-2 .joint-layers")
    private JointLayers jointLayers;

    @FindBy(className = "joint-halo")
    private InteractiveAction interactiveAction;

    public static MainModelContent getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                MainModelContent.class, waitForElementVisible(className(JOINT_PAPER), searchContext));
    }

    public JointLayers getJointLayers() {
        waitForElementVisible(jointLayers.getRoot());
        return jointLayers;
    }

    public InteractiveAction getInteractiveAction() {
        waitForElementVisible(interactiveAction.getRoot());
        return interactiveAction;
    }

    public Model getModel(String modelName) {
        return this.getJointLayers().getJointCellsLayer().getModel(modelName);
    }

    public DateModel getDateModel(String date) {
        return this.getJointLayers().getJointCellsLayer().getDateModel(date);
    }

    public void addName(String name) {
        Actions driverActions = new Actions(browser);
        waitForElementEnabled(textEditor);
        driverActions.moveToElement(textEditor).sendKeys(name).sendKeys(Keys.ENTER).build().perform();
    }

    public void focusOnDataset(String modelName) {
        Model model = this.getJointLayers().getJointCellsLayer().getModel(modelName);
        Actions driverActions = new Actions(browser);
        driverActions.click(model.getRoot()).pause(1000).perform();
    }

    public void focusOnDateDataset(String modelName) {
        DateModel model = this.getJointLayers().getJointCellsLayer().getDateModel(modelName);
        Actions driverActions = new Actions(browser);
        driverActions.doubleClick(model.getRoot()).pause(1000).perform();
    }

    public void addAttributeToDataset(String attributeName, String modelName) {
        Model model = this.getJointLayers().getJointCellsLayer().getModel(modelName);
        Actions driverActions = new Actions(browser);
        driverActions.click(model.getModelAction().addAttribute()).pause(1000).perform();
        waitForElementEnabled(textEditor);
        driverActions.sendKeys(attributeName).sendKeys(Keys.ENTER).build().perform();
    }

    public void addFactToDataset(String factName, String modelName) {
        Model model = this.getJointLayers().getJointCellsLayer().getModel(modelName);
        Actions driverActions = new Actions(browser);
        driverActions.click(model.getModelAction().addFact()).pause(1000).perform();
        waitForElementEnabled(textEditor);
        driverActions.moveToElement(textEditor).sendKeys(factName).sendKeys(Keys.ENTER).build().perform();
    }
}
