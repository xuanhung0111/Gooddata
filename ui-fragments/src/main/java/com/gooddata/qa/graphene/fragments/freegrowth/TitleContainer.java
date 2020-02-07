package com.gooddata.qa.graphene.fragments.freegrowth;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class TitleContainer extends AbstractFragment {

    @FindBy(css = ".good-data")
    private WebElement title;

    @FindBy(css = ".platform-edition")
    private WebElement edition;

    public String getTitle() {
        return title.getText();
    }

    public String getEdition() {
        return edition.getText();
    }
}
