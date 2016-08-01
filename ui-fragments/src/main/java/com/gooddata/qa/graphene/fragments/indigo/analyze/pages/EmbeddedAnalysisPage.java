package com.gooddata.qa.graphene.fragments.indigo.analyze.pages;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;

import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisPageHeader;

public class EmbeddedAnalysisPage extends AnalysisPage {

    public static EmbeddedAnalysisPage getInstance(final SearchContext context) {
        return Graphene.createPageFragment(EmbeddedAnalysisPage.class,
                waitForElementVisible(className("adi-editor"), context));
    }

    /**
     * get error message when opening embedded AD page.
     * The action also works in case EmbeddedAnalysisPage's root is not visible
     * 
     * @param context search context
     * @return error message content
     */
    public static String getErrorMessage(final SearchContext context) {
        return waitForElementVisible(cssSelector(".main-error .adi-canvas-message h2"), context).getText();
    }

    @Override
    public AnalysisPageHeader getPageHeader() {
        if(isEmbeddedPage())
            return super.getPageHeader();

        throw new RuntimeException("Embedded AD page header does not exist !");
    }

    public boolean isEmbeddedPage() {
        return super.getPageHeader().getRoot().getAttribute("class")
                .contains("without-dataset-picker");
    }

    public boolean isAddDataButtonPresent() {
        return isElementPresent(className("s-btn-add_data"), getRoot());
    }
}
