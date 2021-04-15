package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class ToolBarActionMenu extends AbstractFragment {

    private static final By TOOLBAR_MENU_ACTION = By.className("tool-bar-action-menu");
    private static final By EXPORT_MENU = By.className("actions-menu-export-model-item");
    private static final By GENERATE_MENU = By.className("actions-menu-generate-import-model-item");
    private static final By GENERATE_OUTPUT_STAGE_MENU = By.className("actions-menu-generate-output-stage-item");

    public static final ToolBarActionMenu getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(ToolBarActionMenu.class, waitForElementVisible(TOOLBAR_MENU_ACTION, searchContext));
    }
    public boolean isExportModelButtonExist() {
        return isElementPresent(EXPORT_MENU, this.getRoot());
    }

    public boolean isImportModelButtonExist() {
        return isElementPresent(GENERATE_MENU, this.getRoot());
    }

    public boolean isGenerateOutputStageItemButtonExist() {
        return isElementPresent(GENERATE_OUTPUT_STAGE_MENU, this.getRoot());
    }
}
