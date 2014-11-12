package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForDataPageLoaded;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForObjectPageLoaded;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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

    public void configureAttributeLabel(String attributeName, AttributeLabelTypes attributeLabel)
            throws InterruptedException {
        initAttribute(attributeName);
        assertEquals(attributeDetailPage.getAttributeName(), attributeName,
                "Invalid attribute name on detail page");
        attributeDetailPage.selectLabelType(attributeLabel.getlabel());
        Thread.sleep(2000);
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

    public void initAttribute(String attributeName) {
        waitForElementVisible(attributesTable.getRoot());
        waitForDataPageLoaded(browser);
        attributesTable.selectObject(attributeName);
        waitForObjectPageLoaded(browser);
        String variableDetailsWindowHandle = browser.getWindowHandle();
        browser.switchTo().window(variableDetailsWindowHandle);
        waitForElementVisible(attributeDetailPage.getRoot());
    }

    public void createAttribute() {
        waitForElementVisible(createAttributeButton).click();
        waitForObjectPageLoaded(browser);
        String computedAttributeWindowHandle = browser.getWindowHandle();
        browser.switchTo().window(computedAttributeWindowHandle);
        waitForElementVisible(createAttributePage.getRoot());
    }

    public void renameAttribute(String attributeName, String newName) {
        if (attributeName.equals(newName))
            return;

        initAttribute(attributeName);
        attributeDetailPage.renameAttribute(newName);
    }

}
