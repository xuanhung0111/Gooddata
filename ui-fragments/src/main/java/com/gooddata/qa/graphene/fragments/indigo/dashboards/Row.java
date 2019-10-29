package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class Row extends AbstractFragment {

    @FindBy(className = ROW_HEADER_CONTAINER)
    private RowHeader rowheader;

    private static final String ROWS_ROOT = ".gd-fluidlayout-row:not(.s-fluid-layout-row-dropzone)";
    private static final String ROW_HEADER_CONTAINER = "gd-fluid-layout-row-header-container";

    public static Row getInstance(SearchContext context) {
        return Graphene.createPageFragment(Row.class,
                waitForElementVisible(By.cssSelector(ROWS_ROOT), context));
    }

    public RowHeader getRowHeader() {
        return rowheader;
    }

    public boolean hasHeader() {
        return isElementVisible(By.className(ROW_HEADER_CONTAINER),browser);
    }
}
