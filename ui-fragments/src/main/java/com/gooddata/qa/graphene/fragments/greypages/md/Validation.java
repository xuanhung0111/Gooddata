package com.gooddata.qa.graphene.fragments.greypages.md;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * User: peter.lesko@gooddata.com
 * Date: 11/18/13
 * Time: 5:02 PM
 */
public enum Validation {

    INVALID_OBJECTS(0),
    LDM(1),
    METRIC_FILTER(2),
    PMD__ELEM_VALIDATION(3),
    PMD__PDM_VS_DWH(4),
    PMD__PK_FK_CONSISTENCY(5),
    PMD__TRANSITIVITY(6);



    private final int validationID;

    Validation(int validationID) {
        this.validationID = validationID;
    }

    public int getValidatioID() { return validationID;}
}
