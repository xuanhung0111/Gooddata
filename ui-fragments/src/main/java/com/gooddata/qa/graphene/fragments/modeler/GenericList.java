package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static java.util.Arrays.asList;
import static java.lang.String.format;

public class GenericList extends AbstractFragment {
    private static final String GENERIC_LIST = ".dropdown-overlay .generic-list";
    private static final List<String> DROP_DOWN_APPLY_ATTRIBUTE = asList(DATA_TYPE_PICKER.ATTRIBUTE.getClassName(),
            DATA_TYPE_PICKER.PRIMARY_KEY.getClassName(), DATA_TYPE_PICKER.REFERENCE.getClassName());
    private static final List<String> DROP_DOWN_APPLY_MEASURE = asList(DATA_TYPE_PICKER.ATTRIBUTE.getClassName(),
            DATA_TYPE_PICKER.PRIMARY_KEY.getClassName(), DATA_TYPE_PICKER.REFERENCE.getClassName() , DATA_TYPE_PICKER.MEASURE.getClassName());
    private static final List<String> DROP_DOWN_APPLY_DATE = asList(DATA_TYPE_PICKER.ATTRIBUTE.getClassName(),
            DATA_TYPE_PICKER.PRIMARY_KEY.getClassName(), DATA_TYPE_PICKER.REFERENCE.getClassName(), DATA_TYPE_PICKER.DATE.getClassName());
    private static final List<String> DROP_DOWN_APPLY_DATE_FORMAT = asList(DATA_TYPE_PICKER.MM_DD_YYYY.getClassName(),
            DATA_TYPE_PICKER.DD_MM_YYYY.getClassName() );
    public static GenericList getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                GenericList.class, waitForElementVisible(cssSelector(GENERIC_LIST), searchContext));
    }

    public boolean isContainItem(String item) {
        List<WebElement> listItems =  this.getRoot().findElements(className(item));
        return listItems.size() != 0 ? true : false;
    }

    public DatasetEdit selectBasicItem(String itemName) {
        WebElement item =  this.getRoot().findElement(className(itemName));
        item.click();
        return DatasetEdit.getInstance(browser);
    }

    public DatasetEdit selectDateFormatItem(String itemName, String dateFormat) {
        WebElement item =  this.getRoot().findElement(className(format(itemName,dateFormat)));
        item.click();
        return DatasetEdit.getInstance(browser);
    }

    public DatasetEdit selectReferenceItem() {
        WebElement item =  this.getRoot().findElement(className(DATA_TYPE_PICKER.REFERENCE.getClassName()));
        item.click();
        return DatasetEdit.getInstance(browser);
    }

    public boolean isDropdownApplyForAttribute() {
        for(String item: DROP_DOWN_APPLY_ATTRIBUTE) {
            if(!isContainItem(item)) return false;
        }
        return true;
    }

    public boolean isDropdownApplyForMeasure() {
        for(String item: DROP_DOWN_APPLY_MEASURE) {
            if(!isContainItem(item)) return false;
        }
        return true;
    }

    public boolean isDropdownApplyForDate() {
        for(String item: DROP_DOWN_APPLY_DATE) {
            if(!isContainItem(item)) return false;
        }
        return true;
    }

    public boolean isDropdownApplyForFormat() {
        for(String item: DROP_DOWN_APPLY_DATE_FORMAT) {
            if(!isContainItem(item)) return false;
        }
        return true;
    }

    public enum DATA_TYPE_PICKER {
        ATTRIBUTE("s-attribute"),
        MEASURE("s-measure"),
        DATE("s-date"),
        REFERENCE("s-reference"),
        PRIMARY_KEY("s-primary"),
        DD_MM_YYYY("s-dddashMMdashyyyy"),
        MM_DD_YYYY("s-MMdashdddashyyyy");

        private final String className;

        private DATA_TYPE_PICKER(String className) {
            this.className = className;
        }

        public String getClassName() {
            return className;
        }
    }
}
