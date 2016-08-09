package com.gooddata.qa.browser;

import com.gooddata.qa.utils.browser.BrowserUtils;
import com.gooddata.qa.utils.io.ResourceUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import static com.gooddata.qa.graphene.utils.WaitUtils.*;

public class DragAndDropUtils {

    /**
     * Drag via script, works with React-dnd HTML5 backend
     * @param driver WebDriver driver
     * @param fromSelector from element
     * @param toSelector to element (drop element)
     */
    public static void dragAndDropWithHTMLBackend(WebDriver driver, String fromSelector, String toSelector) {
        // just to be sure, load dragdrop simulation suppport every time dragdrop is needed
        String scriptDragDropSetup = "/scripts/setupDragDropSimulation.js";
        String scriptContent = ResourceUtils.getResourceAsString(scriptDragDropSetup);

        BrowserUtils.runScript(driver, scriptContent);

        String dragScript = String.format("jQuery('%1s').simulateDragDrop({ dropTarget: '%2s'});",
                fromSelector, toSelector);
        BrowserUtils.runScript(driver, dragScript);
    }

    /**
     * Drag and drop for custom touch backend
     * Covers the scenario when dragging fromSelector to toSelector, then waiting
     * until the dropSelector element appears and then dropping.
     *
     * (If the element on which we should drop is already present,
     *  use the same dropSelector as toSelector)
     *
     * @param driver WebDriver instance
     * @param fromSelector css selector of an element to be dragged
     * @param toSelector css selector of an element to which we should drag
     * @param dropSelector css selector of an element onto which we should drop
     */
    public static void dragAndDropWithCustomBackend(WebDriver driver, String fromSelector, String toSelector, String dropSelector) {
        WebElement source = waitForElementVisible(By.cssSelector(fromSelector), driver);
        Actions driverActions = new Actions(driver);

        driverActions.clickAndHold(source).perform();

        try {
            WebElement target = waitForElementVisible(By.cssSelector(toSelector), driver);
            driverActions.moveToElement(target).perform();

            WebElement drop = waitForElementVisible(By.cssSelector(dropSelector), driver);
            driverActions.moveToElement(drop).perform();
        } finally {
            driverActions.release().perform();
        }
    }
}
