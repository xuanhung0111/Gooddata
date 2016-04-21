package com.gooddata.qa.browser;

import com.gooddata.qa.utils.browser.BrowserUtils;
import com.gooddata.qa.utils.io.ResourceUtils;
import org.openqa.selenium.WebDriver;

public class DragAndDropUtils {

    public static void dragAndDrop(WebDriver driver, String fromSelector, String toSelector) {
        // just to be sure, load dragdrop simulation suppport every time dragdrop is needed
        String scriptDragDropSetup = "/scripts/setupDragDropSimulation.js";
        String scriptContent = ResourceUtils.getResourceAsString(scriptDragDropSetup);

        BrowserUtils.runScript(driver, scriptContent);

        String dragScript = String.format("jQuery('%1s').simulateDragDrop({ dropTarget: '%2s'});",
                fromSelector, toSelector);
        BrowserUtils.runScript(driver, dragScript);
    }
}
