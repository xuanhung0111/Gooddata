package com.gooddata.qa.graphene.lcmconsole.tests;

import com.gooddata.qa.graphene.fragments.lcmconsole.CreateDataproductDialog;
import com.gooddata.qa.graphene.fragments.lcmconsole.DataproductsPage;
import com.gooddata.qa.graphene.lcmconsole.AbstractLcmConsoleTest;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.lcmconsole.NamingConstants.DOMAIN_ID_1;
import static java.util.Arrays.asList;
import static org.testng.AssertJUnit.assertTrue;


public class DataproductsTest extends AbstractLcmConsoleTest {

    private DataproductsPage dataproductsPage;

    @Test(dependsOnMethods = {"signIn"})
    public void initTest() throws JSONException {
        dataproductsPage = initDataproductPage();
    }

    @Test(dependsOnMethods = {"initTest"})
    public void testCreateDataproduct() throws JSONException {
        final String dataproductId = generateRandomDataproductId();

        final CreateDataproductDialog dataproductDialog = dataproductsPage.openCreateDataProductDialog();
        dataproductDialog.submitDialog(dataproductId, DOMAIN_ID_1);

        assertTrue(dataproductsPage.isDataproductPresent(dataproductId));
    }

    @Test(dependsOnMethods = {"initTest"})
    public void testDataProductPresent() throws JSONException {
        assertTrue("Dataproduct BranchConnect should be present", dataproductsPage.isDataproductPresent("BranchConnect"));
        assertTrue("Dataproduct BranchConnect should have segments Region, State",
                dataproductsPage.isSegmentsPresent("BranchConnect", asList("Region", "State")));
    }

    private String generateRandomDataproductId() {
        return "dataProduct-" + RandomStringUtils.randomAlphanumeric(16);
    }

}
