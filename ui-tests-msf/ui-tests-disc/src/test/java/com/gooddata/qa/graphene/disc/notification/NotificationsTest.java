package com.gooddata.qa.graphene.disc.notification;

import static com.gooddata.qa.utils.http.process.ProcessRestUtils.deteleProcess;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import static com.gooddata.qa.graphene.utils.ElementUtils.getBubbleMessage;

import org.testng.annotations.Test;

import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.qa.graphene.disc.common.AbstractDiscTest;
import com.gooddata.qa.graphene.entity.disc.NotificationRule;
import com.gooddata.qa.graphene.enums.disc.notification.VariableList;
import com.gooddata.qa.graphene.fragments.disc.notification.NotificationRuleItem;
import com.gooddata.qa.graphene.fragments.disc.notification.NotificationRuleItem.NotificationEvent;
import com.gooddata.qa.graphene.fragments.disc.notification.NotificationRulesDialog;
import com.gooddata.qa.graphene.fragments.disc.process.ProcessDetail;

public class NotificationsTest extends AbstractDiscTest {

    private static final String INVALID_EMAIL_MESSAGE = "Email address is invalid. Please insert an email address "
            + "in this format: email@domain.com. You may insert only one recipient per notification rule.";

    @Test(dependsOnGroups = {"createProject"})
    public void checkEmptyStateNotificationList() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            NotificationRulesDialog dialog = initDiscProjectDetailPage()
                    .getProcess(process.getName())
                    .openNotificationRuleDialog();

            takeScreenshot(browser, "Notification-rules-dialog-on-empty-state", getClass());
            assertEquals(dialog.getEmptyStateMessage(),
                    "No event (eg. schedule start, finish, fail, etc.) will trigger a notification email.");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createNotificationRuleWithInvalidEmailFormat() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            NotificationRuleItem notifyItem = initDiscProjectDetailPage()
                    .getProcess(process.getName())
                    .openNotificationRuleDialog()
                    .clickAddNotificationRule()
                    .clickSaveButton();

            assertTrue(notifyItem.isEmailInputError(), "Email input not show error");
            assertEquals(getBubbleMessage(browser), INVALID_EMAIL_MESSAGE);

            notifyItem.enterEmail("abc@gmail.com,xyz@gmail.com").clickSaveButton();
            assertTrue(notifyItem.isEmailInputError(), "Email input not show error");
            assertEquals(getBubbleMessage(browser), INVALID_EMAIL_MESSAGE);

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createNotificationRuleWithEmptyFields() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            NotificationRuleItem notifyItem = initDiscProjectDetailPage()
                    .getProcess(process.getName())
                    .openNotificationRuleDialog()
                    .clickAddNotificationRule()
                    .enterEmail(testParams.getUser())
                    .clickSaveButton();

            assertTrue(notifyItem.isSubjectInputError(), "Subject input not show error");
            assertEquals(getBubbleMessage(browser), "Subject cannot be empty.");

            notifyItem.enterSubject("new subject").clickSaveButton();
            assertTrue(notifyItem.isMessageInputError(), "Message input not show error");
            assertEquals(getBubbleMessage(browser), "Message cannot be empty.");

            notifyItem.enterMessage("new message").selectEvent(NotificationEvent.CUSTOM_EVENT).clickSaveButton();
            assertTrue(notifyItem.isCustomEventNameInputError(), "Custom event name input not show error");
            assertEquals(getBubbleMessage(browser),
                    "The event name is invalid.\n\nPlease insert alphanumeric characters only. No spaces.");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkAvailableParams() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            NotificationRuleItem notifyItem = initDiscProjectDetailPage()
                    .getProcess(process.getName())
                    .openNotificationRuleDialog()
                    .clickAddNotificationRule();
            assertEquals(notifyItem.getVariables(), VariableList.SUCCESS.getVariables());

            notifyItem.selectEvent(NotificationEvent.FAILURE);
            assertEquals(notifyItem.getVariables(), VariableList.FAILURE.getVariables());

            notifyItem.selectEvent(NotificationEvent.PROCESS_SCHEDULED);
            assertEquals(notifyItem.getVariables(), VariableList.PROCESS_SCHEDULED.getVariables());

            notifyItem.selectEvent(NotificationEvent.PROCESS_STARTED);
            assertEquals(notifyItem.getVariables(), VariableList.PROCESS_STARTED.getVariables());

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createNotificationRule() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            ProcessDetail processDetail = initDiscProjectDetailPage().getProcess(process.getName());

            final NotificationRule notificationRule = getNewRule();
            processDetail.openNotificationRuleDialog()
                    .createNotificationRule(notificationRule)
                    .closeDialog();

            NotificationRuleItem notifyItem = processDetail.openNotificationRuleDialog()
                    .getLastNotificationRuleItem()
                    .expand();

            assertEquals(notifyItem.getEmail(), notificationRule.getEmail());
            assertEquals(notifyItem.getEvent(), notificationRule.getEvent());
            assertEquals(notifyItem.getSubject(), notificationRule.getSubject());
            assertEquals(notifyItem.getMessage(), notificationRule.getMessage());

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void cancelCreateNotificationRule() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            NotificationRulesDialog dialog = initDiscProjectDetailPage()
                    .getProcess(process.getName())
                    .openNotificationRuleDialog();
            
            dialog.clickAddNotificationRule().cancel();
            assertEquals(dialog.getNotificationRuleNumber(), 0);

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void editNotificationRule() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            ProcessDetail processDetail = initDiscProjectDetailPage().getProcess(process.getName());

            final NotificationRule notificationRule = getNewRule();
            final String editedSubject = "Edited subject " + generateHashString();

            NotificationRuleItem notifyItem = processDetail.openNotificationRuleDialog()
                    .createNotificationRule(notificationRule)
                    .getLastNotificationRuleItem()
                    .enterSubject(editedSubject);

            notifyItem.cancel();
            assertEquals(notifyItem.getSubject(), notificationRule.getSubject());

            notifyItem.enterSubject(editedSubject).save();
            assertEquals(notifyItem.getSubject(), editedSubject);

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deleteNotificationRule() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            NotificationRulesDialog notificationRuleDialog = initDiscProjectDetailPage()
                    .getProcess(process.getName())
                    .openNotificationRuleDialog();

            NotificationRuleItem notifyItem = notificationRuleDialog
                    .createNotificationRule(getNewRule())
                    .getLastNotificationRuleItem()
                    .tryDeleteButCancel();
            assertEquals(notificationRuleDialog.getNotificationRuleNumber(), 1);

            notifyItem.deleteRule();
            takeScreenshot(browser, "Notification-rule-delete-successfully", getClass());
            assertEquals(notificationRuleDialog.getNotificationRuleNumber(), 0);

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkNotificationNumber() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            ProcessDetail processDetail = initDiscProjectDetailPage().getProcess(process.getName());
            assertEquals(processDetail.getNotificationRuleDescription(), "No notification rules");

            processDetail.openNotificationRuleDialog()
                    .createNotificationRule(getNewRule())
                    .closeDialog();
            assertEquals(processDetail.getNotificationRuleDescription(), "1 notification rule");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    private NotificationRule getNewRule() {
        return new NotificationRule()
                .withEmail(testParams.getUser())
                .withEvent(NotificationEvent.SUCCESS)
                .withSubject("Subject " + generateHashString())
                .withMessage("Message " + generateHashString());
    }
}
