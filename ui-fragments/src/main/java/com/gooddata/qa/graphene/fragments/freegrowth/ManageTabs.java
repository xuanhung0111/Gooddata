package com.gooddata.qa.graphene.fragments.freegrowth;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.hamcrest.CoreMatchers;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openqa.selenium.By.cssSelector;

public class ManageTabs extends AbstractFragment {

    @FindBy(css = ".s-menu-dataPage")
    private WebElement dataTab;

    @FindBy(css = ".s-menu-projectPage")
    private WebElement projectUserTab;

    @FindBy(css = ".s-menu-schedulePage")
    private WebElement scheduleEmailTab;

    public static ManageTabs getInstance(SearchContext context) {
        return Graphene.createPageFragment(ManageTabs.class,
                waitForElementVisible(cssSelector(".yui3-c-collectionwidget.subMenu"), context));
    }

    public ManageTabs verifyManageTabs() {
        assertThat(dataTab, CoreMatchers.notNullValue());
        assertThat(projectUserTab, CoreMatchers.notNullValue());
        assertThat(scheduleEmailTab, CoreMatchers.notNullValue());
        return this;
    }

    public ManageTabs clickDataTab() {
        dataTab.click();
        return this;
    }

    public ManageTabs clickProjectUserTab() {
        projectUserTab.click();
        return this;
    }

    public ManageTabs clickScheduleEmailTab() {
        scheduleEmailTab.click();
        return this;
    }

    public boolean isProjectAndUserTabVisible() {
        return isElementVisible(By.className("s-menu-projectPage"), getRoot());
    }
}
