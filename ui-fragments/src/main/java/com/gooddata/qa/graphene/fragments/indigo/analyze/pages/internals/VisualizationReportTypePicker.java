package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebDriver;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;

public class VisualizationReportTypePicker extends AbstractFragment {

    public void setReportType(final ReportType type) {
        if (isSelected(type))
            return;

        waitForElementVisible(type.getLocator(), browser).click();

        Predicate<WebDriver> visualizationIsSelected = browser -> isSelected(type);
        Graphene.waitGui().until(visualizationIsSelected);
    }

    public boolean isSelected(ReportType type) {
        return waitForElementVisible(type.getLocator(), browser).getAttribute("class").contains("is-selected");
    }
}
