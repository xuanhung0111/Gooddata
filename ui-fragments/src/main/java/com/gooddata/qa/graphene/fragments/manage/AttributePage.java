package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDataPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForObjectPageLoaded;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.AttributeLabelTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class AttributePage extends AbstractFragment {

    @FindBy(id = "attributesTable")
    protected ObjectsTable attributesTable;

    @FindBy(xpath = "//div[@id='p-objectPage' and contains(@class,'s-displayed')]")
    protected AttributeDetailPage attributeDetailPage;

    @FindBy(xpath = "//div[@id='p-objectPage' and contains(@class,'s-displayed')]")
    protected CreateAttributePage createAttributePage;

    @FindBy(css = ".s-attributesAddButton")
    protected WebElement createAttributeButton;

    public void configureAttributeLabel(String attributeName, AttributeLabelTypes attributeLabel) {
        initAttribute(attributeName);
        assertEquals(attributeDetailPage.getAttributeName(), attributeName,
                "Invalid attribute name on detail page");
        attributeDetailPage.selectLabelType(attributeLabel.getlabel());
        sleepTightInSeconds(2);
        assertEquals(attributeDetailPage.getAttributeLabelType(), attributeLabel.getlabel(),
                "Label type not set properly");
    }

    public void verifyHyperLink(String attributeName) {
        initAttribute(attributeName);
        assertTrue(attributeDetailPage.isHyperLink(),
                "Attribute is NOT hyperlink, probably failed in label configuration!");
    }

    public void configureDrillToExternalPage(String attributeName) {
        initAttribute(attributeName);
        attributeDetailPage.setDrillToExternalPage();
    }

    public AttributeDetailPage initAttribute(String attributeName) {
        waitForElementVisible(attributesTable.getRoot());
        waitForDataPageLoaded(browser);
        assertTrue(attributesTable.selectObject(attributeName));
        waitForObjectPageLoaded(browser);
        String variableDetailsWindowHandle = browser.getWindowHandle();
        browser.switchTo().window(variableDetailsWindowHandle);
        return waitForFragmentVisible(attributeDetailPage);
    }

    public CreateAttributePage createAttribute() {
        waitForElementVisible(createAttributeButton).click();
        waitForObjectPageLoaded(browser);
        return CreateAttributePage.getInstance(browser);
    }

    public void renameAttribute(String attributeName, String newName) {
        if (attributeName.equals(newName))
            return;

        initAttribute(attributeName);
        attributeDetailPage.renameAttribute(newName);
    }

    public List<String> getAllAttributes() {
        return waitForFragmentVisible(attributesTable).getAllItems();
    }

    public boolean isAttributeVisible(String attribute) {
        return getAllAttributes().contains(attribute);
    }
}
