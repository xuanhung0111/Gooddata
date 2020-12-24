package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class Modeler extends AbstractFragment {
    private static final String MODELER = "gdc-modeler";

    @FindBy(className = "gdc-ldm-sidebar")
    private Sidebar sidebar;

    @FindBy(className = "gdc-ldm-component-container")
    private ComponentContainer componentContainer;

    @FindBy(className = "gdc-ldm-layout")
    private Layout layout;

    @FindBy(css = ".gdc-modeler #json-file")
    private WebElement chooseJsonfile;

    @FindBy(className = "gdc-ldm-toolbar")
    private ToolBar toolBar;

    public static Modeler getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                Modeler.class, waitForElementVisible(className(MODELER), searchContext));
    }

    public Sidebar getSidebar() {
        return sidebar;
    }

    public ComponentContainer getComponentContainer() {
        return componentContainer;
    }

    public Layout getLayout() {
        return layout;
    }

    public ToolBar getToolbar() {
        this.getLayout().waitForLoading();
        return toolBar;
    }

    public Modeler pickJsonFile(String csvFilePath) {
        log.finest("Json file path: " + csvFilePath);
        log.finest("Is file exists? " + new File(csvFilePath).exists());

        waitForElementVisible(chooseJsonfile).sendKeys(csvFilePath);
        return this;
    }
}
