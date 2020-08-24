package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;

public class TableView extends AbstractFragment {
    private static final By TABLE_VIEW = By.className("gdc-ldm-table-view");

    @FindBy(className = "table-view-datasets")
    TableViewDataset tableViewDataset;

    @FindBy(className = "table-view-template-datasets")
    TableViewTemplateDataset tableViewTemplateDataset;

    public static final TableView getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(TableView.class, waitForElementVisible(TABLE_VIEW, searchContext));
    }

    public TableViewDataset getTableViewDataset() {
        waitForFragmentVisible(tableViewDataset);
        return tableViewDataset;
    }

    public TableViewTemplateDataset getTableViewDateDataset() {
        waitForFragmentVisible(tableViewTemplateDataset);
        return tableViewTemplateDataset;
    }
}
