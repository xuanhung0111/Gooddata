package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class Layout extends AbstractFragment {
    private static final String LAYOUT = "gdc-ldm-layout";

    @FindBy(className = "s-isLoading")
    private WebElement loading;

    @FindBy(className = "gdc-ldm-toolbar")
    private ToolBar toolBar;

    @FindBy(id = "paper-container")
    private Canvas canvas;

    @FindBy(css = ".gdc-initial-state .gdc-ldm-blank-canvas-message")
    private WebElement blankCanvas;

    public static Layout getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                Layout.class, waitForElementVisible(className(LAYOUT), searchContext));
    }

    public Canvas getCanvas() {
        waitForElementVisible(canvas.getRoot());
        return canvas;
    }

    public String getTextBlankCanvas() {
        waitForElementVisible(blankCanvas);
        return blankCanvas.getText();
    }

    public ToolBar getToolbar() {
        return toolBar;
    }

    public Layout waitForLoading() {
        waitForElementNotVisible(loading);
        return this;
    }
}
