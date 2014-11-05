package com.gooddata.qa.graphene.fragments.reports;

import org.openqa.selenium.By;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public abstract class AbstractReport extends AbstractFragment {
    public static final By DRILL_REPORT_LOCATOR = By.cssSelector(".c-drillDialog-report");
}
