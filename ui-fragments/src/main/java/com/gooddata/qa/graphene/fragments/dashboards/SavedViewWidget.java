package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

/**
 * Fragment represents saved view link in dashboard when
 * turn on saved view mode
 *
 */
public class SavedViewWidget extends AbstractFragment{

    @FindBy(xpath = "//div[contains(@class,'savedFilters')]/button[contains(@class,'s-btn-default_view')]")
    private WebElement defaultViewButton;

    @FindBy(xpath = "//div[contains(@class,'savedFilters')]/button[contains(@class,'s-btn-__unsaved_view')]")
    private WebElement unsavedViewButton;

    @FindBy(xpath = "//div[contains(@class,'savedFilters') and contains(@class,'is-menuOpen')]/div[contains(@class,'s-saveFilterView')]")
    private SavedViewPopupMenu savedViewPopupMenu;

    @FindBy(xpath = "//div[contains(@class,'saveActiveViewDialog')]")
    private DashboardSaveActiveViewDialog dashboardSaveActiveViewDialog;

    @FindBy(xpath = "//div[contains(@class,'s-savedViewDeleteConfirmDialog')]")
    private SavedViewDeleteConfirmDialog savedViewDeleteConfirmDialog;

    public SavedViewPopupMenu getSavedViewPopupMenu() {
        return savedViewPopupMenu;
    }

    public DashboardSaveActiveViewDialog getDashboardSaveActiveViewDialog() {
        return dashboardSaveActiveViewDialog;
    }

    public SavedViewDeleteConfirmDialog getSavedViewDeleteConfirmDialog() {
        return savedViewDeleteConfirmDialog;
    }

    public boolean isDefaultViewButtonPresent() {
        try {
            waitForElementVisible(defaultViewButton);
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    public boolean isUnsavedViewButtonPresent() {
        try {
            waitForElementVisible(unsavedViewButton);
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    public SavedViewWidget openSavedViewMenu() {
        waitForElementVisible(this.getRoot()).click();
        waitForElementVisible(savedViewPopupMenu.getRoot());
        return this;
    }

    /**
     * save current view
     * 
     * @param name
     * @param expectSuccessful true if we expect this action is successful
     * @param excludeFilters    filters that we don't want saved view remember their value 
     * @return
     */
    public void saveCurrentView(String name, boolean expectSuccessful, String... excludeFilters) {
        openSaveActiveViewDialog().saveCurrentView(name, expectSuccessful, excludeFilters);
    }

    public SavedViewWidget saveCurrentView(String name, String... excludeFilters) {
        saveCurrentView(name, true, excludeFilters);
        return this;
    }

    public String getCurrentSavedView() {
        return waitForElementVisible(this.getRoot()).getText();
    }

    public SavedViewWidget selectSavedView(String savedView) {
        getSavedViewPopupMenu().selectSavedView(savedView);
        return this;
    }

    public DashboardSaveActiveViewDialog openSaveActiveViewDialog() {
        getSavedViewPopupMenu().getSavedCurrentViewButton().click();
        return waitForFragmentVisible(dashboardSaveActiveViewDialog);
    }

    /**
     * Fragment represents 'Save active view' dialog.
     * That dialog is opened when save new saved view, rename a saved view
     *
     */
    public static class DashboardSaveActiveViewDialog extends AbstractFragment {

        @FindBy(xpath = "//div[contains(@class,'savedFilterName')]/input")
        private WebElement nameField;

        @FindBy(xpath = "//footer[@class='buttons']//button[contains(@class,'s-btn-save')]")
        private WebElement saveButton;

        @FindBy(xpath = "//footer[@class='buttons']//button[contains(@class,'s-btn-cancel')]")
        private WebElement cancelButton;

        @FindBy(xpath = "//div[@class='changed-filter-contents']/div[./label[@class='changed-filter-row']]")
        private List<WebElement> filters;

        public static final By NAME_ALREADY_IN_USE_ERROR = By.xpath("//div[contains(@class,'isActive') and .//*[text()='Name already in use. Choose a different name.']]"); 

        private static final String CSS_FILTER_CHECKBOX = "input[name='%s']";

        /**
         * save current view
         * 
         * @param name
         * @param expectSuccessful true if we expect this action is successful
         * @param excludeFilters    filters that we don't want saved view remember their value 
         * @return
         */
        public void saveCurrentView(String name, boolean expectSuccessful, String... excludeFilters) {
            waitForElementVisible(nameField).sendKeys(name);

            if (excludeFilters != null) {
                for (String excludeName : excludeFilters) {
                    for (WebElement filter : filters) {
                        if (!excludeName.equals(filter.getText())) continue;
                        filter.findElement(By.cssSelector(String.format(CSS_FILTER_CHECKBOX, excludeName))).click();
                        break;
                    }
                }
            }
            waitForElementVisible(saveButton).click();

            if (expectSuccessful) {
                waitForElementNotVisible(this.getRoot());
            } else {
                waitForElementVisible(this.getRoot());
            }
        }

        public WebElement getCancelButton() {
            return cancelButton;
        }

        public WebElement getNameField() {
            return nameField;
        }

        public List<WebElement> getFilters() {
            return filters;
        }

        /**
         * Check that all filters listed in dialog is checked by default
         * @return
         */
        public boolean isAllFiltersAreChecked() {
            for (WebElement ele : filters) {
                if (!ele.findElement(By.cssSelector(String.format(CSS_FILTER_CHECKBOX, ele.getText()))).isSelected()) {
                    return false;
                }
            }
            return true;
        }

        /**
         * rename a saved view
         * 
         * @param newName
         * @param expectSuccessful true if we expect this action is successful
         * @return
         */
        public void rename(String newName, boolean expectSuccessful) {
            waitForElementVisible(nameField).clear();
            nameField.sendKeys(newName);
            waitForElementVisible(saveButton).click();

            if (expectSuccessful) {
                waitForElementNotVisible(this.getRoot());
            } else {
                waitForElementVisible(this.getRoot());
            }
        }

        public void rename(String newName) {
            rename(newName, true);
        }
    }

    /**
     * Fragment represents a popup menu when click on saved view link in dashboard
     *
     */
    public static class SavedViewPopupMenu extends AbstractFragment {

        /**
         * Fragment represents a sub menu when click on arrow icon of a saved view.
         * This menu contains 'rename' and 'delete' command
         *
         */
        public static class SavedFiltersContextMenu extends AbstractFragment {

            @FindBy(css = ".s-rename")
            private WebElement renameButton;

            @FindBy(css = ".s-delete")
            private WebElement deleteButton;

            public void openRenameDialog() {
                waitForElementVisible(renameButton).click();
            }

            public void openDeleteDialog() {
                waitForElementVisible(deleteButton).click();
            }
        }

        private static final String CSS_ARROW_ICON = ".gd-list-view-item .s-triggerContextMenu";

        @FindBy(css = ".noSavedViews")
        private WebElement noSavedViews;

        @FindBy(css = ".s-openSaveFilterDialog")
        private WebElement savedCurrentViewButton;

        @FindBy(css = ".s-saveFilterView .gd-list-view-item")
        private List<WebElement> savedViews;

        @FindBy(xpath = "//div[contains(@class,'s-savedFiltersContextMenu')]")
        private SavedFiltersContextMenu savedFiltersContextMenu;

        public WebElement getSavedCurrentViewButton() {
            return savedCurrentViewButton;
        }

        public boolean isNoSavedViewPresent() {
            try {
                waitForElementVisible(noSavedViews);
                return true;
            } catch(Exception e) {
                return false;
            }
        }

        public void openContextMenuOfSavedView(String savedView) {
            for(WebElement view : savedViews) {
                if (!savedView.equals(view.getText())) continue;

                WebElement arrowButton = view.findElement(By.cssSelector(CSS_ARROW_ICON));
                sleepTightInSeconds(1);
                
                Actions actionBuilder = new Actions(browser);
                actionBuilder.moveToElement(view);
                actionBuilder.moveToElement(arrowButton).build().perform();
                arrowButton.sendKeys("something");
                arrowButton.click();
                sleepTightInSeconds(1);

                waitForElementVisible(savedFiltersContextMenu.getRoot());
                return;
            }
            throw new NoSuchElementException(String.format("Can not find '%s' saved view!", savedView));
        }

        public List<String> getAllSavedViewNames() {
            List<String> names = new ArrayList<String>();
            for (WebElement ele : savedViews) {
                names.add(ele.getText());
            }
            return names;
        }

        public SavedFiltersContextMenu getSavedFiltersContextMenu() {
            return savedFiltersContextMenu;
        }

        public void selectSavedView(String savedView) {
            for (WebElement ele : savedViews) {
                if (!savedView.equals(ele.getText())) continue;
                ele.click();
                return;
            }
            throw new NoSuchElementException(String.format("Can not find '%s' saved view!", savedView));
        }
    }

    /**
     * Fragment represents a delete confirm dialog when deleting a saved view
     *
     */
    public static class SavedViewDeleteConfirmDialog extends AbstractFragment {

        @FindBy(xpath = "//div[contains(@class,'s-savedViewDeleteConfirmDialog')]/section")
        private WebElement confirmMessage;

        @FindBy(css = ".s-btn-cancel")
        private WebElement cancelButton;

        @FindBy(css = ".s-btn-delete")
        private WebElement deleteButton;

        public String getConfirmMessage() {
            return waitForElementVisible(confirmMessage).getText().trim();
        }

        public WebElement getCancelButton() {
            return cancelButton;
        }

        public void deleteSavedView() {
            waitForElementVisible(deleteButton).click();
            waitForElementNotVisible(this.getRoot());
        }
    }
}
