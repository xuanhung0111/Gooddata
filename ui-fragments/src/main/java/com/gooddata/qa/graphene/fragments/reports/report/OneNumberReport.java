package com.gooddata.qa.graphene.fragments.reports.report;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class OneNumberReport extends AbstractReport {

    @FindBy(className = "number")
    private WebElement number;

    @FindBy(className = "description")
    private WebElement description;

    public String getValue() {
        return number.getText();
    }

    public String getDescription() {
        return description.getText();
    }
}
