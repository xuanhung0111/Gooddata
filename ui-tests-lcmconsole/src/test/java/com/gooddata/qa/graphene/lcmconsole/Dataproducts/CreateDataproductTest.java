package com.gooddata.qa.graphene.lcmconsole.Dataproducts;

import com.gooddata.qa.graphene.fragments.lcmconsole.CreateDataproductDialog;
import com.gooddata.qa.graphene.fragments.lcmconsole.DataproductsPage;
import com.gooddata.qa.graphene.lcmconsole.AbstractLcmConsoleTest;
import org.json.JSONException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Random;

import static org.testng.AssertJUnit.assertTrue;


public class CreateDataproductTest extends AbstractLcmConsoleTest {

    private DataproductsPage dataproductsPage;
    private CreateDataproductDialog dataproductDialog;

    @BeforeMethod
    public void initTest() throws JSONException {
        signIn();
        dataproductsPage = initDataproductPage();
        dataproductsPage.clickCreateDataproductButton();
        dataproductDialog = CreateDataproductDialog.getInstance(browser);
    }

    @Test
    public void testCreateDataproduct() throws JSONException {
        final String dataproductName = createRandomDataproductName();
        createDataproductWithName(dataproductName);

        assertTrue(dataproductsPage.isDataproductPresent(dataproductName));
    }

    private void createDataproductWithName(String dataproductName) {
        dataproductDialog.setName(dataproductName);
        dataproductDialog.checkDataAdminTest1Checkbox();
        dataproductDialog.submitForm();
    }

    private String createRandomDataproductName() {
        return "GrapheneDataproduct" + new Random().nextLong();
    }

}
