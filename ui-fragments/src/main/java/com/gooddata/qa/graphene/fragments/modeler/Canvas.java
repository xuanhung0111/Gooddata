package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class Canvas extends AbstractFragment {
    private static final By CANVAS =  By.className("gdc-ldm-canvas");

    @FindBy(className = "gdc-ldm-blank-canvas-message")
    private WebElement blankCanvas;

    public static final Canvas getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(Canvas.class, waitForElementVisible(CANVAS, searchContext));
    }

    public String getTextBlankCanvas() {
        waitForElementVisible(blankCanvas);
        return blankCanvas.getText();
    }
}
