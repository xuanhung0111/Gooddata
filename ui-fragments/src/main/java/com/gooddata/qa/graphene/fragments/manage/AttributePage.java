package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDataPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForObjectPageLoaded;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.AttributeLabelTypes;

public class AttributePage extends DataPage {

    @FindBy(id = "attributesTable")
    protected ObjectsTable attributesTable;

    @FindBy(css = ".s-attributesAddButton")
    protected WebElement createAttributeButton;

    public static AttributePage getInstance(SearchContext context) {
        return Graphene.createPageFragment(AttributePage.class, waitForElementVisible(ROOT_LOCATOR, context));
    }

    public void configureAttributeLabel(String attributeName, AttributeLabelTypes attributeLabel) {
        AttributeDetailPage attributeDetailPage = initAttribute(attributeName);
        assertEquals(attributeDetailPage.getName(), attributeName,
                "Invalid attribute name on detail page");
        attributeDetailPage.selectLabelType(attributeLabel.getlabel());
        sleepTightInSeconds(2);
        assertEquals(attributeDetailPage.getAttributeLabelType(), attributeLabel.getlabel(),
                "Label type not set properly");
    }

    public void verifyHyperLink(String attributeName) {
        assertTrue(initAttribute(attributeName).isHyperLink(),
                "Attribute is NOT hyperlink, probably failed in label configuration!");
    }

    public void configureDrillToExternalPage(String attributeName) {
        initAttribute(attributeName).setDrillToExternalPage();
    }

    public AttributeDetailPage initAttribute(String attributeName) {
        waitForElementVisible(attributesTable.getRoot());
        waitForDataPageLoaded(browser);
        assertTrue(attributesTable.selectObject(attributeName));
        waitForObjectPageLoaded(browser);
        String variableDetailsWindowHandle = browser.getWindowHandle();
        browser.switchTo().window(variableDetailsWindowHandle);
        return AttributeDetailPage.getInstance(browser);
    }

    public CreateAttributePage moveToCreateAttributePage() {
        waitForElementVisible(createAttributeButton).click();
        waitForObjectPageLoaded(browser);
        return CreateAttributePage.getInstance(browser);
    }

    public void renameAttribute(String attributeName, String newName) {
        if (attributeName.equals(newName))
            return;

        initAttribute(attributeName).changeName(newName);
    }

    public List<String> getAllAttributes() {
        return waitForFragmentVisible(attributesTable).getAllItems();
    }

    public boolean isAttributeVisible(String attribute) {
        return getAllAttributes().contains(attribute);
    }
}
