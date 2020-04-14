package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class InteractiveAction extends AbstractFragment {
    private static final String JOINT_HALO = "joint-halo";

    @FindBy(css = ".handles > .handle.link")
    private WebElement linkHandle;

    @FindBy(className = "handles")
    private JoinHandle handle;

    public static InteractiveAction getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                InteractiveAction.class, waitForElementVisible(className(JOINT_HALO), searchContext));
    }

    public JoinHandle getHandle() {
        waitForElementVisible(handle.getRoot());
        return handle;
    }

    public WebElement getLink() {
        return linkHandle;
    }
}
