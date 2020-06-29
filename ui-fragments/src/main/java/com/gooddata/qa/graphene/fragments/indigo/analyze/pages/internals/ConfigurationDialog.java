package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import com.gooddata.qa.graphene.enums.GeoPointSize;
import com.gooddata.qa.graphene.enums.LegendPosition;
import com.gooddata.qa.graphene.enums.MapArea;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.stream.Collectors;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static org.openqa.selenium.By.className;

public class ConfigurationDialog extends AbstractFragment {

    @FindBy(className = "public_fixedDataTable_main")
    public WebElement listItems;

    public static ConfigurationDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(ConfigurationDialog.class,
                waitForElementVisible(className("dropdown-body"), context));
    }

    public ConfigurationDialog selectItems(String items) {
        waitForElementVisible(listItems).findElement(By.cssSelector(items)).click();
        return this;
    }

    public ConfigurationDialog selectArea(MapArea area) {
        return selectItems(".s-" + simplifyText(area.getCountryName()));
    }

    public ConfigurationDialog selectLegendPosition(LegendPosition position) {
        return selectItems(position.getCssPosition());
    }

    public ConfigurationDialog selectPointSize(GeoPointSize pointSize) {
        return selectItems(".s-" + simplifyText(pointSize.getSize()));
    }

    public List<String> getListItems() {
        return waitForElementVisible(listItems).findElements(By.className("gd-list-item"))
                .stream().map(el -> el.getText()).collect(Collectors.toList());
    }
}
