package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class JoinHandle extends AbstractFragment {
    private static final String JOINT_HANDLE = "handles";

    @FindBy(css = ".handle.link")
    private WebElement linkHandle;

    public static JoinHandle getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                JoinHandle.class, waitForElementVisible(className(JOINT_HANDLE), searchContext));
    }

    public WebElement getHandleLink() {
        return linkHandle;
    }

    public void addDatasetName(String name) {
        this.root.sendKeys(name);
        this.root.sendKeys(Keys.ENTER);
    }
}
