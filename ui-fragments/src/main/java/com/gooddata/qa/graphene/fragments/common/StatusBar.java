package com.gooddata.qa.graphene.fragments.common;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class StatusBar extends AbstractFragment {

    @FindBy(className = "s-btn-dismiss")
    private WebElement dismissButton;

    public static final StatusBar getInstance(SearchContext context) {
        return Graphene.createPageFragment(StatusBar.class,
                waitForElementVisible(By.className("c-status"), context));
    }

    public Status getStatus() {
        for (Status status : Status.values()) {
            if (getRoot().getAttribute("class").contains(status.toString().toLowerCase())) {
                return status;
            }
        }
        throw new RuntimeException("Error occurs on getting status or it is not supported by current status list");
    }

    public String getMessage() {
        return waitForElementVisible(By.className("leftContainer"), getRoot()).getText();
    }

    public void dismiss() {
        waitForElementVisible(dismissButton).click();
        waitForFragmentNotVisible(this);
    }

    public enum Status {
        SUCCESS, ERROR, INFO
    }
}
