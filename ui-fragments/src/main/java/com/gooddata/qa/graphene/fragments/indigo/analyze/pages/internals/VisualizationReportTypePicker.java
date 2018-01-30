package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebDriver;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import java.util.function.Function;

/**
 * button is hidden in DOM, find it when setReportType in order to click it properly
 */
public class VisualizationReportTypePicker extends AbstractFragment {

    public void setReportType(final ReportType type) {
        if (isSelected(type))
            return;

        waitForElementVisible(type.getLocator(), browser).click();

        Function<WebDriver, Boolean> visualizationIsSelected = browser -> isSelected(type);
        Graphene.waitGui().until(visualizationIsSelected);
    }

    public boolean isSelected(ReportType type) {
        return waitForElementVisible(type.getLocator(), browser).getAttribute("class").contains("is-selected");
    }
}
