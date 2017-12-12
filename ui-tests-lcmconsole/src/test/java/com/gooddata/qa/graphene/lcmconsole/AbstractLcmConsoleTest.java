package com.gooddata.qa.graphene.lcmconsole;

import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.lcmconsole.DataproductsPage;
import com.gooddata.qa.graphene.fragments.lcmconsole.SegmentDetailPage;
import org.json.JSONException;
import org.testng.annotations.Test;

public class AbstractLcmConsoleTest extends AbstractUITest {

    @Test
    protected void signIn() throws JSONException {
        signIn(true, UserRoles.ADMIN);
    }

    protected DataproductsPage initDataproductPage() {
        openUrl(DataproductsPage.URI);
        return DataproductsPage.getInstance(browser);
    }

    protected SegmentDetailPage initSegmentDetailPage(String dataProductId, String segmentId, String domainId) {
        openUrl(SegmentDetailPage.getUri(dataProductId, segmentId, domainId));
        return SegmentDetailPage.getInstance(browser);
    }
}
