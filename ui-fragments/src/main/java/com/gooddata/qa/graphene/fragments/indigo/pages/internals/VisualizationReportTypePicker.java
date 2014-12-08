package com.gooddata.qa.graphene.fragments.indigo.pages.internals;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebDriver;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;

public class VisualizationReportTypePicker extends AbstractFragment {

    private static final String SELECTED = "is-selected";

    public void setReportType(final ReportType type) {
        if (isSelected(type))
            return;

        waitForElementVisible(type.getLocator(), browser).click();

        Graphene.waitGui().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return isSelected(type);
            }
        });
    }

    public boolean isSelected(ReportType type) {
        return waitForElementVisible(type.getLocator(), browser).getAttribute("class").contains(
                SELECTED);
    }
}
