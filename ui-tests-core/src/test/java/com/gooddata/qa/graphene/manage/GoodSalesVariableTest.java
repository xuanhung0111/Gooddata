package com.gooddata.qa.graphene.manage;

import static com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils.getUserProfileUri;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.entity.variable.NumericVariable;
import com.gooddata.qa.utils.http.RestApiClient;

public class GoodSalesVariableTest extends ObjectAbstractTest {

    private static final Collection<String> ATTRIBUTE_VALUES = asList("Interest", "Discovery");
    private static final int NUMERIC_VALUE = 1234;

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-variable";
    }

    @Test(dependsOnGroups = {"createProject"}, groups = { "object-tests" })
    public void createNumericVariable() {
        final String variable = generateVariableName();

        initVariablePage()
                .createVariable(new NumericVariable(variable).withDefaultNumber(NUMERIC_VALUE));

        assertTrue(initVariablePage().hasVariable(variable));
        assertEquals(variablePage.openVariableFromList(variable).getDefaultNumericValue(), NUMERIC_VALUE);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = { "object-tests" })
    public void createNumericVariableWithSpecificUser() throws ParseException, JSONException, IOException {
        final String variable = generateVariableName();

        RestApiClient restApiClient = testParams.getDomainUser() != null ? getDomainUserRestApiClient() : getRestApiClient();
        String userProfileUri = getUserProfileUri(restApiClient, testParams.getUserDomain(), testParams.getUser());

        initVariablePage().createVariable(new NumericVariable(variable)
                .withDefaultNumber(5678)
                .withUserSpecificNumber(userProfileUri, NUMERIC_VALUE));

        assertTrue(initVariablePage().hasVariable(variable));
        assertEquals(variablePage.openVariableFromList(variable).getUserSpecificNumericValue(userProfileUri), NUMERIC_VALUE);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = { "object-tests" })
    public void createAttributeVariable() {
        final String variable = generateVariableName();

        initVariablePage().createVariable(new AttributeVariable(variable)
                .withAttribute(ATTR_STAGE_NAME)
                .withAttributeValues(ATTRIBUTE_VALUES));

        assertTrue(initVariablePage().hasVariable(variable));
        assertEquals(variablePage.openVariableFromList(variable).getDefaultAttributeValues(), ATTRIBUTE_VALUES);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = { "object-tests" })
    public void createAttributeVariableWithSpecificUser() throws ParseException, JSONException, IOException {
        name = generateVariableName();

        RestApiClient restApiClient = testParams.getDomainUser() != null ? getDomainUserRestApiClient() : getRestApiClient();
        String userProfileUri = getUserProfileUri(restApiClient, testParams.getUserDomain(), testParams.getUser());

        initVariablePage().createVariable(new AttributeVariable(name)
                .withAttribute(ATTR_STAGE_NAME)
                .withUserSpecificValues(userProfileUri, ATTRIBUTE_VALUES));

        assertTrue(initVariablePage().hasVariable(name));
        assertEquals(variablePage.openVariableFromList(name).getUserSpecificAttributeValues(userProfileUri), ATTRIBUTE_VALUES);
    }

    @Override
    public void initObject(String variableName) {
        description = "Test description";
        tagName = "var";
        initVariablePage();
        variablePage.openVariableFromList(variableName);
    }

    private String generateVariableName() {
        return "Variable-" + UUID.randomUUID().toString().substring(0, 6);
    }
}