package com.gooddata.qa.graphene.fragments.freegrowth;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.id;

public class FreeLandingPage extends AbstractFragment {

    @FindBy(css = ".header-panel")
    protected LandingPageHeader header;

    @FindBy(css = ".title-container")
    protected TitleContainer titleContainer;

    @FindBy(xpath = "//div[contains(@class, 'workspace-separator')]/preceding::div[contains(@class, 'workspace')]")
    protected DemoContainer demoContainer;

    @FindBy(xpath = "//div[contains(@class, 'workspace-separator')]/following::div[contains(@class, 'workspace')]")
    protected WorkspaceContainer workspaceContainer;

    public static final FreeLandingPage getInstance(SearchContext context) {
        waitForElementNotVisible(id("appLoading"), context);
        return Graphene.createPageFragment(FreeLandingPage.class, waitForElementVisible(id("root"), context));
    }

    public TitleContainer getTitleContainer() {
        return titleContainer;
    }

    public WorkspaceContainer getWorkspaceContainer() {
        return workspaceContainer;
    }
}
