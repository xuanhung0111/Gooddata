package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

/**
 * button is hidden in DOM, find it when setReportType in order to click it properly
 */
public class VisualizationReportTypePickerReact extends AbstractFragment {

    public void setReportType(final ReportType type) {
        if (isSelected(type))
            return;

        waitForElementVisible(type.getLocator(), browser).findElement(By.tagName("button")).click();

        Predicate<WebDriver> visualizationIsSelected = browser -> isSelected(type);
        Graphene.waitGui().until(visualizationIsSelected);
    }

    public boolean isSelected(ReportType type) {
        return waitForElementVisible(type.getLocator(), browser).getAttribute("class").contains("is-selected");
    }
}
