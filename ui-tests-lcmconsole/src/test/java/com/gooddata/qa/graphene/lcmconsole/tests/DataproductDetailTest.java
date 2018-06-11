package com.gooddata.qa.graphene.lcmconsole.tests;

import com.gooddata.qa.graphene.fragments.lcmconsole.DataproductDetailPage;
import com.gooddata.qa.graphene.fragments.lcmconsole.DomainProjectsDialog;
import com.gooddata.qa.graphene.fragments.lcmconsole.DomainSegmentFragment;
import com.gooddata.qa.graphene.lcmconsole.AbstractLcmConsoleTest;
import org.json.JSONException;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.lcmconsole.NamingConstants.DATAPRODUCT_ID;
import static com.gooddata.qa.graphene.lcmconsole.NamingConstants.DOMAIN_ID_1;
import static com.gooddata.qa.graphene.lcmconsole.NamingConstants.DOMAIN_ID_2;
import static com.gooddata.qa.graphene.lcmconsole.NamingConstants.DOMAIN_ID_3;
import static com.gooddata.qa.graphene.lcmconsole.NamingConstants.PROJECT_NAME1;
import static com.gooddata.qa.graphene.lcmconsole.NamingConstants.PROJECT_NAME2;
import static com.gooddata.qa.graphene.lcmconsole.NamingConstants.PROJECT_NAME3;
import static com.gooddata.qa.graphene.lcmconsole.NamingConstants.SEGMENT_ID1;
import static com.gooddata.qa.graphene.lcmconsole.NamingConstants.SEGMENT_ID2;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;


public class DataproductDetailTest extends AbstractLcmConsoleTest {

    @Test(dependsOnMethods = {"signIn"})
    public void initTest() throws JSONException {
        initDataproductPage().openDataproductDetailPage(DATAPRODUCT_ID);
    }

    @Test(dependsOnMethods = {"initTest"})
    public void testDomainSegment() throws JSONException {
        final DataproductDetailPage dataproductDetailPage = DataproductDetailPage.getInstance(browser);
        final DomainSegmentFragment domainSegmentFragment = dataproductDetailPage.getDomainSegment(DOMAIN_ID_2, SEGMENT_ID2);

        assertTrue("Numbers of clients should be greater than 0", domainSegmentFragment.getClientsCount() > 0);
        assertEquals(domainSegmentFragment.getMasterProjectName(), "master");
    }

    @Test(dependsOnMethods = {"initTest"})
    public void testProjectsListDialog() throws JSONException {
        final DataproductDetailPage dataproductDetailPage = DataproductDetailPage.getInstance(browser);

        assertTrue("Number of projects should be greater or equal than 3",
                dataproductDetailPage.getNumberOfProjectsInDomain(DOMAIN_ID_1) >= 3);

        final DomainProjectsDialog domainProjectsDialog = dataproductDetailPage.openProjectList(DOMAIN_ID_1);

        assertTrue("Number of projects should be greater or equal than 3",
                domainProjectsDialog.getNumberOfProjects() >= 3);

        assertTrue(domainProjectsDialog.isProjectPresent(PROJECT_NAME1));
        assertTrue(domainProjectsDialog.isProjectPresent(PROJECT_NAME2));
        assertTrue(domainProjectsDialog.isProjectPresent(PROJECT_NAME3));

        domainProjectsDialog.filterProject(PROJECT_NAME1);
        domainProjectsDialog.waitForProjectIsNotPresent(PROJECT_NAME2);
        domainProjectsDialog.waitForProjectIsNotPresent(PROJECT_NAME3);

        assertTrue(domainProjectsDialog.isProjectPresent(PROJECT_NAME1));

        domainProjectsDialog.closeDialog();
    }

    @Test(dependsOnMethods = {"initTest"})
    public void testDomainsPresent() throws JSONException {
        final DataproductDetailPage dataproductDetailPage = DataproductDetailPage.getInstance(browser);
        assertDomainPresent(dataproductDetailPage, DATAPRODUCT_ID, DOMAIN_ID_2);
        assertDomainPresent(dataproductDetailPage, DATAPRODUCT_ID, DOMAIN_ID_3);
    }

    @Test(dependsOnMethods = {"initTest"})
    public void testDomainSegmentsPresent() throws JSONException {
        final DataproductDetailPage dataproductDetailPage = DataproductDetailPage.getInstance(browser);
        assertDomainSegmentPresent(dataproductDetailPage, DOMAIN_ID_3, SEGMENT_ID1);
        assertDomainSegmentPresent(dataproductDetailPage, DOMAIN_ID_3, SEGMENT_ID2);
        assertDomainSegmentPresent(dataproductDetailPage, DOMAIN_ID_2, SEGMENT_ID1);
        assertDomainSegmentPresent(dataproductDetailPage, DOMAIN_ID_2, SEGMENT_ID2);
    }

    private void assertDomainSegmentPresent(DataproductDetailPage dataproductDetailPage, String domainId, String segmentId) {
        assertTrue(format("Segment %s should be present in domain %s", segmentId, domainId),
                dataproductDetailPage.isDomainSegmentsPresent(domainId, segmentId));
    }

    private void assertDomainPresent(DataproductDetailPage dataproductDetailPage, String dataproductId, String domainId) {
        assertTrue(format("Domain %s should be present in dataproduct %s", domainId, dataproductId),
                dataproductDetailPage.isDomainPresent(domainId));
    }
}
