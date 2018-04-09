package com.gooddata.qa.graphene.lcmconsole.tests;

import com.gooddata.qa.graphene.fragments.lcmconsole.ClientDetailDialog;
import com.gooddata.qa.graphene.fragments.lcmconsole.SegmentDetailPage;
import com.gooddata.qa.graphene.lcmconsole.AbstractLcmConsoleTest;
import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.Test;

import java.util.List;

import static com.gooddata.qa.graphene.lcmconsole.NamingConstants.DATA_PRODUCT_ID;
import static com.gooddata.qa.graphene.lcmconsole.NamingConstants.DOMAIN_ID_2;
import static com.gooddata.qa.graphene.lcmconsole.NamingConstants.SEGMENT_1_CLIENT_ID_1;
import static com.gooddata.qa.graphene.lcmconsole.NamingConstants.SEGMENT_1_CLIENT_ID_2;
import static com.gooddata.qa.graphene.lcmconsole.NamingConstants.SEGMENT_1_CLIENT_ID_3;
import static com.gooddata.qa.graphene.lcmconsole.NamingConstants.SEGMENT_ID_1;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SegmentDetailTest extends AbstractLcmConsoleTest {

    @Test(dependsOnMethods = {"signIn"})
    public void testClientSearch() {
        final SegmentDetailPage segmentDetail = initSegmentDetailPage(DATA_PRODUCT_ID, SEGMENT_ID_1, DOMAIN_ID_2);

        assertEquals(segmentDetail.getClientCount(), 3);
        assertTrue(segmentDetail.isClientPresent(SEGMENT_1_CLIENT_ID_1));
        assertTrue(segmentDetail.isClientPresent(SEGMENT_1_CLIENT_ID_2));
        assertTrue(segmentDetail.isClientPresent(SEGMENT_1_CLIENT_ID_3));

        segmentDetail.filterClients(SEGMENT_1_CLIENT_ID_1);
        segmentDetail.waitForClientIsNotPresent(SEGMENT_1_CLIENT_ID_2);
        segmentDetail.waitForClientIsNotPresent(SEGMENT_1_CLIENT_ID_3);

        assertTrue(segmentDetail.isClientPresent(SEGMENT_1_CLIENT_ID_1));
    }

    @Test(dependsOnMethods = {"signIn"})
    public void testUserSearch() {
        final SegmentDetailPage segmentDetail = initSegmentDetailPage(DATA_PRODUCT_ID, SEGMENT_ID_1, DOMAIN_ID_2);
        final ClientDetailDialog clientDetailDialog = segmentDetail.openClientDetailDialog(SEGMENT_1_CLIENT_ID_1);
        final String adminUser = testParams.getUser();
        final List<String> extraUsers = clientDetailDialog.getAllUserEmails().stream()
                .filter(user -> !user.equals(adminUser))
                .collect(toList());

        assertEquals(clientDetailDialog.getUserCount(), 3);
        assertTrue(clientDetailDialog.isUserPresent(adminUser));

        clientDetailDialog.filterUsers(adminUser);
        extraUsers.forEach(clientDetailDialog::waitForUserIsNotPresent);

        assertTrue(clientDetailDialog.isUserPresent(adminUser));
        clientDetailDialog.close();
    }

    @Test(dependsOnMethods = {"signIn"})
    public void testMasterProjectChange() {
        final SegmentDetailPage segmentDetail = initSegmentDetailPage(DATA_PRODUCT_ID, SEGMENT_ID_1, DOMAIN_ID_2);
        final String oldProjectId = segmentDetail.getMasterProjectId();
        final String newProjectId = segmentDetail.getClientProjectId(SEGMENT_1_CLIENT_ID_1);

        segmentDetail.openMasterProjectDialog().changeMasterProject(newProjectId);
        Graphene.waitGui().until(browser -> segmentDetail.getMasterProjectId().equals(newProjectId));

        segmentDetail.openMasterProjectDialog().changeMasterProject(oldProjectId);
        Graphene.waitGui().until(browser -> segmentDetail.getMasterProjectId().equals(oldProjectId));
    }
}
