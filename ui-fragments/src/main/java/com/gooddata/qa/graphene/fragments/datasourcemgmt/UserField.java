package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class UserField extends AbstractFragment {
    @FindBy(className = "fixedDataTableCellGroupLayout_cellGroup")
    private List<WebElement> listCells;

    public static UserField getInstance(SearchContext context) {
        return Graphene.createPageFragment(UserField.class,
                waitForElementVisible(className("user-field"), context));
    }
}
