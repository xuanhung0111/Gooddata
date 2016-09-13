package com.gooddata.qa.graphene.manage;

import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.getVariableUri;
import static com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils.getUserProfileUri;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertEquals;
import static com.gooddata.md.Restriction.title;
import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Metric;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.Filter;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.md.report.Report;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.entity.variable.NumericVariable;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;
import com.gooddata.qa.graphene.fragments.manage.VariableDetailPage;
import com.gooddata.qa.graphene.fragments.profile.UserProfilePage;
import com.gooddata.qa.graphene.fragments.manage.VariablesPage;
import com.gooddata.qa.utils.http.RestApiClient;

public class GoodSalesVariableTest extends GoodSalesAbstractTest {

    private static final String ATTRIBUTE_VARIABLE = "A-var";
    private static final String NUMERIC_VARIABLE = "N-var";

    private static final Collection<String> ATTRIBUTE_VALUES = asList("Interest", "Discovery");
    private static final Collection<String> EDITED_ATTRIBUTE_VALUES = asList("Interest", "Discovery", "Short List");
    private static final Collection<String> ALL_VALUES = asList("Interest", "Discovery", "Short List",
            "Risk Assessment", "Conviction", "Negotiation", "Closed Won", "Closed Lost");

    private static final int NUMERIC_VALUE = 1234;
    private static final int EDITED_NUMERIC_VALUE = 5678;

    private static final String RED_BAR_MESSAGE = "\"%s\" name already in use. Please change the name and try again.";

    @BeforeClass(alwaysRun = true)
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-variable";
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"basic"})
    public void createNumericVariable() {
        final String variable = generateVariableName();

        initVariablePage()
                .createVariable(new NumericVariable(variable).withDefaultNumber(NUMERIC_VALUE));

        assertTrue(initVariablePage().hasVariable(variable));
        assertEquals(VariablesPage.getInstance(browser).openVariableFromList(variable).getDefaultNumericValue(),
                NUMERIC_VALUE);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"basic"})
    public void createNumericVariableWithSpecificUser() throws ParseException, JSONException, IOException {
        RestApiClient restApiClient = testParams.getDomainUser() != null ? getDomainUserRestApiClient() : getRestApiClient();
        String userProfileUri = getUserProfileUri(restApiClient, testParams.getUserDomain(), testParams.getUser());

        initVariablePage().createVariable(new NumericVariable(NUMERIC_VARIABLE)
                .withDefaultNumber(NUMERIC_VALUE)
                .withUserSpecificNumber(userProfileUri, NUMERIC_VALUE));

        assertTrue(initVariablePage().hasVariable(NUMERIC_VARIABLE));
        assertEquals(VariablesPage.getInstance(browser).openVariableFromList(NUMERIC_VARIABLE)
                .getUserSpecificNumericValue(userProfileUri), NUMERIC_VALUE);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"basic"})
    public void createAttributeVariable() {
        final String variable = generateVariableName();

        initVariablePage().createVariable(new AttributeVariable(variable)
                .withAttribute(ATTR_STAGE_NAME)
                .withAttributeValues(ATTRIBUTE_VALUES));

        assertTrue(initVariablePage().hasVariable(variable));
        assertEquals(VariablesPage.getInstance(browser).openVariableFromList(variable).getDefaultAttributeValues(),
                ATTRIBUTE_VALUES);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"basic"})
    public void createAttributeVariableWithSpecificUser() throws ParseException, JSONException, IOException {
        RestApiClient restApiClient = testParams.getDomainUser() != null ? getDomainUserRestApiClient() : getRestApiClient();
        String userProfileUri = getUserProfileUri(restApiClient, testParams.getUserDomain(), testParams.getUser());

        initVariablePage().createVariable(new AttributeVariable(ATTRIBUTE_VARIABLE)
                .withAttribute(ATTR_STAGE_NAME)
                .withAttributeValues(ATTRIBUTE_VALUES)
                .withUserSpecificValues(userProfileUri, ATTRIBUTE_VALUES));

        assertTrue(initVariablePage().hasVariable(ATTRIBUTE_VARIABLE));
        assertEquals(VariablesPage.getInstance(browser).openVariableFromList(ATTRIBUTE_VARIABLE)
                .getUserSpecificAttributeValues(userProfileUri), ATTRIBUTE_VALUES);
    }

    @DataProvider(name = "variableProvider")
    public Object[][] getVariableProvider() {
        return new Object[][] {
            {ATTRIBUTE_VARIABLE},
            {NUMERIC_VARIABLE}
        };
    }

    @Test(dependsOnGroups = {"basic"}, dataProvider = "variableProvider")
    public void editVariableBasicInfo(String variable) {
        final String editedName = variable + "-edited";
        final String description = "New-description";
        final String tag = "new-tag";
        final String comment = "New-comment";

        try {
            initVariablePage()
                    .openVariableFromList(variable)
                    .changeName(editedName)
                    .changeDescription(description)
                    .addTag(tag)
                    .addComment(comment);

            VariableDetailPage variableDetailPage = initVariablePage().openVariableFromList(editedName);
            takeScreenshot(browser, "Basic-info-update-for-" + variable, getClass());
            assertEquals(variableDetailPage.getDescription(), description);
            assertThat(variableDetailPage.getComments(), hasItem(comment));
            assertThat(variableDetailPage.getTags(), hasItem(tag));
            assertFalse(variableDetailPage.canSelectAttributeVariableType(), "Can edit variable type in edit mode!");
            assertFalse(variableDetailPage.canSelectNumericVariableType(), "Can edit variable type in edit mode!");

        } finally {
            initVariablePage().openVariableFromList(editedName).changeName(variable);
        }
    }

    @Test(dependsOnGroups = {"basic"})
    public void editAttributeVariableDefaultAndUserSpecificValue() throws ParseException, JSONException, IOException {
        RestApiClient restApiClient = testParams.getDomainUser() != null ? getDomainUserRestApiClient() : getRestApiClient();
        String userProfileUri = getUserProfileUri(restApiClient, testParams.getUserDomain(), testParams.getUser());

        initVariablePage()
                .openVariableFromList(ATTRIBUTE_VARIABLE)
                .selectDefaultAttributeValues(EDITED_ATTRIBUTE_VALUES)
                .selectUserSpecificAttributeValues(userProfileUri, EDITED_ATTRIBUTE_VALUES);

        VariableDetailPage variableDetailPage = initVariablePage().openVariableFromList(ATTRIBUTE_VARIABLE);
        takeScreenshot(browser, "Default-and-user-specific-attribute-values-not-updated", getClass());
        assertEquals(variableDetailPage.getDefaultAttributeValues(), ATTRIBUTE_VALUES);
        assertEquals(variableDetailPage.getUserSpecificAttributeValues(userProfileUri), ATTRIBUTE_VALUES);

        variableDetailPage
                .selectDefaultAttributeValues(EDITED_ATTRIBUTE_VALUES)
                .selectUserSpecificAttributeValues(userProfileUri, EDITED_ATTRIBUTE_VALUES)
                .saveChange();

        variableDetailPage = initVariablePage().openVariableFromList(ATTRIBUTE_VARIABLE);
        takeScreenshot(browser, "Default-and-user-specific-attribute-values-updated", getClass());
        assertEquals(variableDetailPage.getDefaultAttributeValues(), EDITED_ATTRIBUTE_VALUES);
        assertEquals(variableDetailPage.getUserSpecificAttributeValues(userProfileUri), EDITED_ATTRIBUTE_VALUES);
    }

    @Test(dependsOnGroups = {"basic"})
    public void editNumericVariableDefaultAndUserSpecificValue() throws ParseException, JSONException, IOException {
        RestApiClient restApiClient = testParams.getDomainUser() != null ? getDomainUserRestApiClient() : getRestApiClient();
        String userProfileUri = getUserProfileUri(restApiClient, testParams.getUserDomain(), testParams.getUser());

        initVariablePage()
                .openVariableFromList(NUMERIC_VARIABLE)
                .setDefaultNumericValue(EDITED_NUMERIC_VALUE)
                .setUserSpecificNumericValue(userProfileUri, EDITED_NUMERIC_VALUE);

        VariableDetailPage variableDetailPage = initVariablePage().openVariableFromList(NUMERIC_VARIABLE);
        takeScreenshot(browser, "Default-and-user-specific-numeric-value-not-updated", getClass());
        assertEquals(variableDetailPage.getDefaultNumericValue(), NUMERIC_VALUE);
        assertEquals(variableDetailPage.getUserSpecificNumericValue(userProfileUri), NUMERIC_VALUE);

        variableDetailPage
                .setDefaultNumericValue(EDITED_NUMERIC_VALUE)
                .setUserSpecificNumericValue(userProfileUri, EDITED_NUMERIC_VALUE)
                .saveChange();

        variableDetailPage = initVariablePage().openVariableFromList(NUMERIC_VARIABLE);
        takeScreenshot(browser, "Default-and-user-specific-numeric-value-updated", getClass());
        assertEquals(variableDetailPage.getDefaultNumericValue(), EDITED_NUMERIC_VALUE);
        assertEquals(variableDetailPage.getUserSpecificNumericValue(userProfileUri), EDITED_NUMERIC_VALUE);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deleteVariable() throws JSONException, IOException {
        final String variable = generateVariableName();

        initVariablePage().createVariable(new AttributeVariable(variable)
                .withAttribute(ATTR_STAGE_NAME)
                .withAttributeValues(ATTRIBUTE_VALUES));

        Metric amountMetric = getMdService().getObj(getProject(), Metric.class, title(METRIC_AMOUNT));
        String promptFilterUri = getVariableUri(getRestApiClient(), testParams.getProjectId(), variable);
        Attribute stageNameAttribute = getMdService().getObj(getProject(), Attribute.class, title(ATTR_STAGE_NAME));

        Report report = createReportViaRest(GridReportDefinitionContent.create("Report",
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(stageNameAttribute.getDefaultDisplayForm().getUri(), stageNameAttribute.getTitle())),
                singletonList(new MetricElement(amountMetric)),
                singletonList(new Filter(format("[%s]", promptFilterUri)))));

        initVariablePage()
                .openVariableFromList(variable)
                .deleteObjectButCancel();

        initVariablePage()
                .openVariableFromList(variable)
                .deleteObject();
        assertFalse(VariablesPage.getInstance(browser).hasVariable(variable), "Variable is not deleted!");

        initReportsPage().openReport(report.getTitle());
        waitForAnalysisPageLoaded(browser);

        takeScreenshot(browser, "Variable-filter-is-kept-in-report", getClass());
        assertEquals(reportPage.getTableReport().getAttributeElements(), ATTRIBUTE_VALUES);
        assertThat(reportPage.getFilters(), hasItem(variable));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void updateAttributeVariableInProfilePage() throws ParseException, JSONException, IOException {
        final String variable = generateVariableName();

        RestApiClient restApiClient = testParams.getDomainUser() != null ? getDomainUserRestApiClient() : getRestApiClient();
        String userProfileUri = getUserProfileUri(restApiClient, testParams.getUserDomain(), testParams.getUser());

        initVariablePage().createVariable(new AttributeVariable(variable)
                .withAttribute(ATTR_STAGE_NAME));

        UserProfilePage userProfilePage = initProjectsAndUsersPage()
                .openUserProfile(testParams.getUser())
                .selectAttributeValuesFor(variable, ATTRIBUTE_VALUES)
                .saveChanges();

        takeScreenshot(browser, "Variable-attribute-values-updated-in-profile-page", getClass());
        assertEquals(userProfilePage.getAttributeValuesOf(variable), ATTRIBUTE_VALUES);
        assertEquals(initVariablePage().openVariableFromList(variable).getUserSpecificAttributeValues(userProfileUri),
                ATTRIBUTE_VALUES);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void updateNumericVariableInProfilePage() throws ParseException, JSONException, IOException {
        final String variable = generateVariableName();

        RestApiClient restApiClient = testParams.getDomainUser() != null ? getDomainUserRestApiClient() : getRestApiClient();
        String userProfileUri = getUserProfileUri(restApiClient, testParams.getUserDomain(), testParams.getUser());

        initVariablePage().createVariable(new NumericVariable(variable)
                .withDefaultNumber(NUMERIC_VALUE));

        UserProfilePage userProfilePage = initProjectsAndUsersPage()
                .openUserProfile(testParams.getUser())
                .setNumericValueFor(variable, NUMERIC_VALUE)
                .saveChanges();

        takeScreenshot(browser, "Variable-numeric-value-updated-in-profile-page", getClass());
        assertEquals(userProfilePage.getNumericValueOf(variable), NUMERIC_VALUE);
        assertEquals(initVariablePage().openVariableFromList(variable).getUserSpecificNumericValue(userProfileUri),
                NUMERIC_VALUE);
    }

    @Test(dependsOnGroups = {"basic"}, dataProvider = "variableProvider")
    public void editAttributeVaribaleAsEditor(String variable) throws JSONException {
        logoutAndLoginAs(true, UserRoles.EDITOR);

        try {
            VariableDetailPage variableDetailPage = initVariablePage().openVariableFromList(variable);

            takeScreenshot(browser, "Editor-cannot-edit-variable-infos-" + variable, getClass());
            assertTrue(variableDetailPage.isNameFieldDisabled(), "Editor can edit variable name");
            assertTrue(variableDetailPage.isDescriptionFieldDisabled(), "Editor can edit variable description");
            assertFalse(variableDetailPage.canAddTag(), "Editor can add new tag for variable");
            assertFalse(variableDetailPage.isUserSpecificTableDisplayed(), "Editor can view the user specific table");

            if (variable.equals(ATTRIBUTE_VARIABLE)) {
                assertFalse(variableDetailPage.canEditDefaultAttributeValues(),
                        "Editor can edit default attribute values for variable");
            } else {
                assertFalse(variableDetailPage.canEditDefaultNumericValue(),
                        "Editor can edit default attribute values for variable");
            }

            final String comment = "Editor add comment";
            variableDetailPage.addComment(comment);
            takeScreenshot(browser, "Editor-can-add-comment-for-" + variable, getClass());
            assertThat(variableDetailPage.getComments(), hasItem(comment));

        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"basic"})
    public void checkVariableNamingUniqueness() {
        initVariablePage()
                .clickCreateVariableButton()
                .getVariableDetailPage()
                .changeName(ATTRIBUTE_VARIABLE);

        takeScreenshot(browser, "Red-bar-shows-when-create-variable-with-a-duplicated-name", getClass());
        assertEquals(getRedBarMessage(), format(RED_BAR_MESSAGE, ATTRIBUTE_VARIABLE));

        initVariablePage()
                .openVariableFromList(ATTRIBUTE_VARIABLE)
                .changeName(NUMERIC_VARIABLE);

        takeScreenshot(browser, "Red-bar-shows-when-edit-variable-name-with-a-duplicated-one", getClass());
        assertEquals(getRedBarMessage(), format(RED_BAR_MESSAGE, NUMERIC_VARIABLE));
        assertEquals(VariableDetailPage.getInstance(browser).getName(), ATTRIBUTE_VARIABLE);
    }

    @Test(dependsOnGroups = {"basic"})
    public void customAttributeVariableValues() {
        SelectItemPopupPanel selectItemPopupPanel = initVariablePage()
                .openVariableFromList(ATTRIBUTE_VARIABLE)
                .clickEditAttributeValuesButton()
                .clearAllItems();

        takeScreenshot(browser, "All-attribute-values-are-deselected", getClass());
        assertTrue(selectItemPopupPanel.areAllItemsDeselected(), "All attribute values are not deselected");

        selectItemPopupPanel.selectAllItems();
        takeScreenshot(browser, "All-attribute-values-are-selected", getClass());
        assertTrue(selectItemPopupPanel.areAllItemsSelected(), "All attribute values are not selected");

        selectItemPopupPanel.submitPanel();
        assertEquals(VariableDetailPage.getInstance(browser).getDefaultAttributeValues(), ALL_VALUES);

        changeAttributeFilterOperatorTo("isn't");
        takeScreenshot(browser, "Attribute-filter-operator-changes-to-isn't", getClass());
        assertEquals(getAttributeFilterOperator(), "isn't");
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    private String generateVariableName() {
        return "Variable-" + UUID.randomUUID().toString().substring(0, 6);
    }

    private String getRedBarMessage() {
        return waitForElementVisible(By.cssSelector(".box-warning .leftContainer"), browser).getText();
    }

    private void changeAttributeFilterOperatorTo(String operator) {
        SelectItemPopupPanel popup = VariableDetailPage.getInstance(browser).clickEditAttributeValuesButton();
        new Select(popup.getRoot().findElement(By.cssSelector(".notin select"))).selectByVisibleText(operator);
        popup.submitPanel();
    }

    private String getAttributeFilterOperator() {
        return VariableDetailPage.getInstance(browser)
                .getRoot().findElement(By.cssSelector(".filterAnswer .answer b")).getText();
    }
}