package com.gooddata.qa.graphene.fragments.modeler.datasource;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class DropDownDSContent extends AbstractFragment {
    private static final String DATASOURCE_DROP_DOWN_CONTENT = "dropdown-body";

    @FindBy(className = "gd-list-item")
    private List<WebElement> listDSItem ;

    public void selectDatasource(String dsName) {
        listDSItem.stream()
                .filter(el -> el.findElement(By.className("type-name")).getText().equals(dsName))
                .findFirst()
                .get()
                .click();
    }
}
