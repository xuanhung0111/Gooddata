package com.gooddata.qa.graphene.lcmconsole;

import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.lcmconsole.DataproductsPage;
import org.json.JSONException;
import org.openqa.selenium.support.FindBy;

public class AbstractLcmConsoleTest extends AbstractUITest {

    @FindBy(id = "app-admin")
    DataproductsPage dataproductsPage;

    protected void signIn() throws JSONException {
        signIn(true, UserRoles.ADMIN);
    }

    protected DataproductsPage initDataproductPage() {
        openUrl(DataproductsPage.URI);
        return dataproductsPage;
    }
}
