package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class DatasetEdit extends AbstractFragment {
    private static String DATASET_EDIT = "dataset-edit";

    @FindBy(css = ".dataset-column-wrapper")
    List<WebElement> listColumns;

    @FindBy(css = ".dataset-column-wrapper.is-disabled")
    List<WebElement> listDisabledColumns;

    public static DatasetEdit getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                DatasetEdit.class, waitForElementVisible(className(DATASET_EDIT), searchContext));
    }

    public List<String> getListColumns () {
        return listColumns.stream().map(WebElement::getText).collect(Collectors.toList());
    }

    public int getNumberOfDisabledColumns() {
        return listDisabledColumns.size();
    }
}
