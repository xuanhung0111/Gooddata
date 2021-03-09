package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoEmbedDashboardDialogs;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.graphene.utils.Sleeper;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.openqa.selenium.Keys;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.util.List;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Collections.singletonList;
import static org.testng.Assert.*;

public class EmbeddingDashboardDialogTest extends AbstractDashboardTest {
    private static final String DASHBOARD_ACTIVITIES = "Dashboard Activites";

    @Override
    protected void customizeProject() throws Throwable {
        IndigoRestRequest indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)),
                testParams.getProjectId());
        indigoRestRequest.createAnalyticalDashboard(singletonList(createNumOfActivitiesKpi()), DASHBOARD_ACTIVITIES);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void getEmbedCodeWithNavigationConfiguration(){
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        final List<String> embedCode = indigoDashboardsPage.expectedEmbedCodes(true);
        final String embedIframeCode = embedCode.get(0);
        final String embedUrlCode = embedCode.get(1);

        IndigoEmbedDashboardDialogs indigoEmbedDashboardDialogs = indigoDashboardsPage.openEmbedDialog();
        assertTrue(indigoEmbedDashboardDialogs.isPreviewAll(), "Navigation bar is on by default");

        indigoEmbedDashboardDialogs.switchToIframeTab();
        assertTrue(indigoEmbedDashboardDialogs.isIframeTabVisible(), "Switch to Iframe tab successfully");
        assertTrue(indigoEmbedDashboardDialogs.getIframeContent().contains(embedIframeCode),
                "Expected is" + embedIframeCode + "but" + indigoEmbedDashboardDialogs.getIframeContent());

        indigoEmbedDashboardDialogs.switchToUrlTab();
        assertTrue(indigoEmbedDashboardDialogs.isUrlTabVisible(), "Switch to URL tab successfully");
        assertTrue(indigoEmbedDashboardDialogs.getUrlContent().contains(embedUrlCode),"Embed code is matching");

        //Preview tab apply correct layout when hovering
        indigoEmbedDashboardDialogs.switchToPreviewTab();
        assertTrue(indigoEmbedDashboardDialogs.hoveringOnNavigationToggle(),"On Preview tab, navigation bar is highlighted when hovering");
        assertTrue(indigoEmbedDashboardDialogs.hoveringOnCustomHeightToggle(),
                "On Preview tab, navigation bar is not highlighted when hovering on other toggles - Custom Height");
        assertTrue(indigoEmbedDashboardDialogs.isPreviewAll(), "Preview as all and display");

        //Switch toggle to hide navigation bar => the parameter showNavigation=false
        indigoEmbedDashboardDialogs.switchNavigationToggle();
        Sleeper.sleepTightInSeconds(5);
        assertTrue(indigoEmbedDashboardDialogs.isConfiguredNoNavigationBar(),"Preview tab hides Navigation bar on layout");
        assertTrue(indigoEmbedDashboardDialogs.hoveringOnNavigationToggle(),
                "Navigation bar is highlighted when hovering although it was set as hidden");

        //get embed code when hidden navigation bar
        final List<String> embedCodeHiddenNav = indigoDashboardsPage.expectedEmbedCodes(false);
        final String embedIframeCodeHiddenNav = embedCodeHiddenNav.get(0);
        final String embedUrlCodeHiddenNav = embedCodeHiddenNav.get(1);

        indigoEmbedDashboardDialogs.switchToIframeTab();
        assertTrue(indigoEmbedDashboardDialogs.isIframeTabVisible(), "Switch to Iframe tab successfully");
        assertTrue(indigoEmbedDashboardDialogs.getIframeContent().contains(embedIframeCodeHiddenNav),
                "Expected is" + embedIframeCodeHiddenNav + "but" + indigoEmbedDashboardDialogs.getIframeContent());

        indigoEmbedDashboardDialogs.switchToUrlTab();
        assertTrue(indigoEmbedDashboardDialogs.isUrlTabVisible(), "Switch to URL tab successfully");
        assertTrue(indigoEmbedDashboardDialogs.getUrlContent().contains(embedUrlCodeHiddenNav),"Embed code is matching");
        indigoEmbedDashboardDialogs.closeEmbeddedDialog();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void getEmbedCodeWithCustomHeightConfiguration(){
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        final String customHeight = "1111.0";
        final List<String> embedCode = indigoDashboardsPage.expectedEmbedCodes(true,customHeight);
        final String embedIframeCode = embedCode.get(0);
        final String embedUrlCode = embedCode.get(1);

        IndigoEmbedDashboardDialogs indigoEmbedDashboardDialogs = indigoDashboardsPage.openEmbedDialog();
        assertTrue(indigoEmbedDashboardDialogs.isOffCustomHeight(), "Custom height is off by default");
        indigoEmbedDashboardDialogs.switchCustomHeightToggle();
        assertFalse(indigoEmbedDashboardDialogs.isOffCustomHeight(), "Custom height is not off");

        indigoEmbedDashboardDialogs.setHeight(customHeight);
        indigoEmbedDashboardDialogs.switchToIframeTab();
        assertTrue(indigoEmbedDashboardDialogs.getIframeContent().contains(embedIframeCode),
                "Expected is" + embedIframeCode + "but" + indigoEmbedDashboardDialogs.getIframeContent());

        indigoEmbedDashboardDialogs.switchToUrlTab();
        assertTrue(indigoEmbedDashboardDialogs.getUrlContent().contains(embedUrlCode),
                "Expected is" + embedUrlCode + "but" + indigoEmbedDashboardDialogs.getUrlContent());

        //Remove height -> remove parameter
        indigoEmbedDashboardDialogs.removeHeight();

        final List<String> embedCodeNoHeight = indigoDashboardsPage.expectedEmbedCodes(true);
        final String embedIframeCodeNoHeight = embedCodeNoHeight.get(0);
        final String embedUrlCodeNoHeight = embedCodeNoHeight.get(1);

        assertTrue(indigoEmbedDashboardDialogs.getUrlContent().contains(embedUrlCodeNoHeight),
                "Expected is" + embedUrlCodeNoHeight + "but" + indigoEmbedDashboardDialogs.getUrlContent());
        indigoEmbedDashboardDialogs.switchToIframeTab();
        assertTrue(indigoEmbedDashboardDialogs.getIframeContent().contains(embedIframeCodeNoHeight),
                "Expected is" + embedIframeCodeNoHeight + "but" + indigoEmbedDashboardDialogs.getIframeContent());
        indigoEmbedDashboardDialogs.closeEmbeddedDialog();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void validateInvalidCustomHeight(){
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        IndigoEmbedDashboardDialogs indigoEmbedDashboardDialogs = indigoDashboardsPage.openEmbedDialog();
        indigoEmbedDashboardDialogs.switchCustomHeightToggle();

        // Validate invalid custom height
        assertTrue(indigoEmbedDashboardDialogs.isInvalidHeightMessageVisible("0"),"The height must be a number greater than 0");
        indigoEmbedDashboardDialogs.removeHeight();

        assertTrue(indigoEmbedDashboardDialogs.isInvalidHeightMessageVisible("0.0"),"The height must be a number greater than 0");
        indigoEmbedDashboardDialogs.removeHeight();

        assertTrue(indigoEmbedDashboardDialogs.isInvalidHeightMessageVisible("-"),"This input accepts only digits greater than 0");
        assertTrue(indigoEmbedDashboardDialogs.isInvalidHeightMessageVisible("abcd"),"This input accepts only digits greater than 0");
        indigoEmbedDashboardDialogs.closeEmbeddedDialog();
    }

    @DataProvider(name = "customHeight")
    public Object[][] getCustomHeight() {
        return new Object[][]{
                {"0"},
                {""},
                {"0.0"},
                {"0.9"},
                {"680"},
                {"680.90"}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "customHeight")
    public void getEmbedCodeCorrespondingWithCustomHeight(String customHeight){
        final IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        List<String> embeddedCode = indigoDashboardsPage.expectedEmbedCodes(true, customHeight);
        String embedIframeCode = embeddedCode.get(0);
        String embedUrlCode = embeddedCode.get(1);

        IndigoEmbedDashboardDialogs indigoEmbedDashboardDialogs = indigoDashboardsPage.openEmbedDialog();
        indigoEmbedDashboardDialogs.switchToIframeTab();

        indigoEmbedDashboardDialogs.switchCustomHeightToggle().setHeight(customHeight);
        assertTrue(indigoEmbedDashboardDialogs.getIframeContent().contains(embedIframeCode),
                "Expected is" + embedIframeCode + "but" + indigoEmbedDashboardDialogs.getIframeContent());

        indigoEmbedDashboardDialogs.switchToUrlTab();
        assertTrue(indigoEmbedDashboardDialogs.getUrlContent().contains(embedUrlCode),
                "Expected is" + embedUrlCode + "but" + indigoEmbedDashboardDialogs.getUrlContent());
    }

    @DataProvider(name = "tags")
    public Object[][] getTags() {
        return new Object[][]{
                {"", Keys.ENTER},
                {"123", Keys.TAB},
                {"abcd", Keys.SPACE}
        };
    }

    @Test(dependsOnGroups = {"createProject"},dataProvider = "tags")
    public void getEmbedCodeWithFilterByTags(String tags, Keys keys){
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        List<String> embeddedCode = indigoDashboardsPage.expectedEmbedCodes(true,"","include", tags);
        String embedIframeCode = embeddedCode.get(0);
        String embedUrlCode = embeddedCode.get(1);

        IndigoEmbedDashboardDialogs indigoEmbedDashboardDialogs = indigoDashboardsPage.openEmbedDialog();
        assertTrue(indigoEmbedDashboardDialogs.isOffFilterByTags(), "Filter by tags is off by default");

        indigoEmbedDashboardDialogs.switchFilterByTagsToggle().setTags(tags,keys);
        assertFalse(indigoEmbedDashboardDialogs.isOffFilterByTags(), "Filter by tags is turned on");
        assertTrue(indigoEmbedDashboardDialogs.isIncludeTabVisible(), "Include tab is selected by default");

        indigoEmbedDashboardDialogs.switchToIframeTab();
        assertTrue(indigoEmbedDashboardDialogs.getIframeContent().contains(embedIframeCode),
                "Expected is" + embedIframeCode + "but" + indigoEmbedDashboardDialogs.getIframeContent());

        indigoEmbedDashboardDialogs.switchToUrlTab();
        assertTrue(indigoEmbedDashboardDialogs.getUrlContent().contains(embedUrlCode),
                "Expected is" + embedUrlCode + "but" + indigoEmbedDashboardDialogs.getUrlContent());

        List<String> embeddedCodeExcludedTags = indigoDashboardsPage.expectedEmbedCodes(true,"","exclude", tags);
        String embedIframeCodeExcludedTags = embeddedCodeExcludedTags.get(0);
        String embedUrlCodeExcludedTags = embeddedCodeExcludedTags.get(1);

        indigoEmbedDashboardDialogs.switchToExcludeTab();
        assertTrue(indigoEmbedDashboardDialogs.isExcludeTabVisible(), "Switch to Exclude tab successfully");
        assertTrue(indigoEmbedDashboardDialogs.getUrlContent().contains(embedUrlCodeExcludedTags),
                "Expected is" + embedUrlCodeExcludedTags + "but" + indigoEmbedDashboardDialogs.getUrlContent());
        indigoEmbedDashboardDialogs.switchToIframeTab();
        assertTrue(indigoEmbedDashboardDialogs.getIframeContent().contains(embedIframeCodeExcludedTags),
                "Expected is" + embedIframeCodeExcludedTags + "but" + indigoEmbedDashboardDialogs.getIframeContent());

        indigoEmbedDashboardDialogs.switchToIncludeTab();
        assertTrue(indigoEmbedDashboardDialogs.getIframeContent().contains(embedIframeCode),
                "Expected is" + embedIframeCode + "but" + indigoEmbedDashboardDialogs.getIframeContent());
        indigoEmbedDashboardDialogs.switchToUrlTab();
        assertTrue(indigoEmbedDashboardDialogs.getUrlContent().contains(embedUrlCode),
                "Expected is" + embedUrlCode + "but" + indigoEmbedDashboardDialogs.getUrlContent());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void copyEmbeddedCodeFromEmbedDialog(){
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        IndigoEmbedDashboardDialogs indigoEmbedDashboardDialogs = indigoDashboardsPage.openEmbedDialog();
        assertEquals(indigoEmbedDashboardDialogs.getCopiedMessage(),
                "The embedding code was copied to your clipboard. You can paste it to your application.");

        indigoEmbedDashboardDialogs.switchToIframeTab();
        assertEquals(indigoEmbedDashboardDialogs.getCopiedMessage(),
                "The embedding code was copied to your clipboard. You can paste it to your application.");

        indigoEmbedDashboardDialogs.switchToUrlTab();
        assertEquals(indigoEmbedDashboardDialogs.getCopiedMessage(),"The URL was copied to your clipboard.");
        indigoEmbedDashboardDialogs.closeEmbeddedDialog();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchToggleToOff(){
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        final String customHeight = "1111.0";
        final String tags = "<button>a!@#$%^&*().</button>";
        final List<String> embedCode = indigoDashboardsPage.expectedEmbedCodes(true,customHeight,"include", tags);
        final String embedIframeCode = embedCode.get(0);
        final String embedUrlCode = embedCode.get(1);

        IndigoEmbedDashboardDialogs indigoEmbedDashboardDialogs = indigoDashboardsPage.openEmbedDialog();
        indigoEmbedDashboardDialogs.switchCustomHeightToggle().setHeight(customHeight);
        indigoEmbedDashboardDialogs.switchFilterByTagsToggle().setTags(tags,Keys.ENTER);
        indigoEmbedDashboardDialogs.switchToIframeTab();
        assertTrue(indigoEmbedDashboardDialogs.getIframeContent().contains(embedIframeCode),
                "Expected is" + embedIframeCode + "but" + indigoEmbedDashboardDialogs.getIframeContent());

        indigoEmbedDashboardDialogs.switchToUrlTab();
        assertTrue(indigoEmbedDashboardDialogs.getUrlContent().contains(embedUrlCode),
                "Expected is" + embedUrlCode + "but" + indigoEmbedDashboardDialogs.getUrlContent());

        //Switch to remove height parameter, tags parameter
        indigoEmbedDashboardDialogs.switchCustomHeightToggle();
        Sleeper.sleepTightInSeconds(6);
        takeScreenshot(browser, "TurnOFFHeight", getClass());
        assertTrue(indigoEmbedDashboardDialogs.isOffCustomHeight(), "Custom height is turned off successfully");

        indigoEmbedDashboardDialogs.switchFilterByTagsToggle();
        takeScreenshot(browser, "TurnOFFFilters", getClass());
        assertTrue(indigoEmbedDashboardDialogs.isOffFilterByTags(), "Filter by tags is turned off successfully");

        final List<String> embedCodeTurnedOffOptions = indigoDashboardsPage.expectedEmbedCodes(true);
        final String embedIframeCodeTurnedOffOptions = embedCodeTurnedOffOptions.get(0);
        final String embedUrlCodeTurnedOffOptions = embedCodeTurnedOffOptions.get(1);

        assertTrue(indigoEmbedDashboardDialogs.getUrlContent().contains(embedUrlCodeTurnedOffOptions),
                "Expected is" + embedUrlCodeTurnedOffOptions + "but" + indigoEmbedDashboardDialogs.getUrlContent());
        indigoEmbedDashboardDialogs.switchToIframeTab();
        assertTrue(indigoEmbedDashboardDialogs.getIframeContent().contains(embedIframeCodeTurnedOffOptions),
                "Expected is" + embedIframeCodeTurnedOffOptions + "but" + indigoEmbedDashboardDialogs.getIframeContent());
        indigoEmbedDashboardDialogs.closeEmbeddedDialog();
    }
}
