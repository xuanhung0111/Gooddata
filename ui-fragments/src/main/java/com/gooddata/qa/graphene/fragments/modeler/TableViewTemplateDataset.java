package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class TableViewTemplateDataset extends AbstractFragment {
    private static final By TABLE_VIEW_TEMPLATE_DATASET = By.className("table-view-template-datasets");

    public static final TableViewTemplateDataset getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(TableViewTemplateDataset.class, waitForElementVisible(TABLE_VIEW_TEMPLATE_DATASET, searchContext));
    }
}
