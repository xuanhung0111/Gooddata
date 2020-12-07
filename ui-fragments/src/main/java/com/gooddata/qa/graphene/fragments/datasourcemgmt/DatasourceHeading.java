package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class DatasourceHeading extends AbstractFragment {
    public static final String DATASOURCE_HEADING_CLASS = "heading";

    @FindBy(className = "datasource-heading-name")
    private WebElement datasourceName;

    @FindBy(className = "s-more-button")
    private WebElement moreButton;

    public static final DatasourceHeading getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(DatasourceHeading.class, waitForElementVisible(className(DATASOURCE_HEADING_CLASS), searchContext));
    }

    public String getName() {
        waitForElementVisible(datasourceName);
        return datasourceName.getText();
    }

    public void clickEditButton() {
        waitForElementVisible(moreButton).click();
        MoreContentDialog.getInstance(browser).clickEditButton();
    }

    public DeleteDatasourceDialog clickDeleteButton() {
        waitForElementVisible(moreButton).click();
        return MoreContentDialog.getInstance(browser).clickDeleteButton();
    }

    public PublishWorkspaceDialog openPublishIntoWorkSpaceDialog() {
        waitForElementVisible(moreButton).click();
        return MoreContentDialog.getInstance(browser).openPublishIntoWorkSpaceDialog();
    }

    public GenerateOutputStageDialog getGenerateDialog () {
        waitForElementVisible(moreButton).click();
        return MoreContentDialog.getInstance(browser).getGenerateDialog();
    }

    public DatasourceMessageBar getErrorMessageDialog () {
        waitForElementVisible(moreButton).click();
        return MoreContentDialog.getInstance(browser).getErrorMessageDialog();
    }
}
