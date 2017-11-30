package com.gooddata.qa.graphene.fragments.common;

import com.gooddata.qa.graphene.entity.filter.TimeRange;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class SelectTimeRangePanel extends AbstractFragment {

    public static final SelectTimeRangePanel getInstance(By locator, SearchContext searchContext) {
        WebElement root = waitForElementVisible(locator, searchContext);
        return Graphene.createPageFragment(SelectTimeRangePanel.class, root);
    }

    public void setTimeRange(TimeRange from, TimeRange to) {
        setTimeRange(from);
        setTimeRange(to);
        //Tab to make it lost focus, So it is applied the input value
        getActions().sendKeys(Keys.TAB).perform();
    }

    private void setTimeRange(TimeRange timeRange) {
        new Select(getRoot().findElement(timeRange.getRange().getSelect())).selectByVisibleText(timeRange.getTime().getName());
        WebElement input = getRoot().findElement(timeRange.getRange().getInput());
        if (timeRange.getNumber() != 0) {
            input.clear();
            input.sendKeys(String.valueOf(timeRange.getNumber()));
        }
    }

    public enum Range {
        FROM(By.xpath("./descendant::input[1]"), By.xpath("./descendant::select[1]")),
        TO(By.xpath("./descendant::input[2]"), By.xpath("./descendant::select[2]"));

        private By input;
        private By select;

        Range(By input, By select) {
            this.input = input;
            this.select = select;
        }

        public By getInput() {
            return input;
        }

        public By getSelect() {
            return select;
        }
    }
}
