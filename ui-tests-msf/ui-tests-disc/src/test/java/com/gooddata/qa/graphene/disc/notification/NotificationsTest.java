package com.gooddata.qa.graphene.disc.notification;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import static com.gooddata.qa.graphene.utils.ElementUtils.getBubbleMessage;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.qa.graphene.common.AbstractProcessTest;
import com.gooddata.qa.graphene.entity.disc.NotificationRule;
import com.gooddata.qa.graphene.enums.disc.notification.VariableList;
import com.gooddata.qa.graphene.fragments.disc.notification.NotificationRuleItem;
import com.gooddata.qa.graphene.fragments.disc.notification.NotificationRuleItem.NotificationEvent;
import com.gooddata.qa.graphene.fragments.disc.notification.NotificationRulesDialog;
import com.gooddata.qa.graphene.fragments.disc.process.ProcessDetail;

public class NotificationsTest extends AbstractProcessTest {

    private static final String INVALID_EMAIL_MESSAGE = "Email address is invalid. Please insert an email address "
            + "in this format: email@domain.com. You may insert only one recipient per notification rule.";
    private static final Integer CONSECUTIVE_FAILURES_VALUE = 2;

    @DataProvider(name = "consecutiveFailuresProvider")
    public Object[][] getConsecutiveFailues() {
        return new Object[][] {
                {true},
                {false}
        };
    }

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
            getProcessService().removeProcess(process);
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
            getProcessService().removeProcess(process);
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
            getProcessService().removeProcess(process);
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
            getProcessService().removeProcess(process);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "consecutiveFailuresProvider")
    public void createNotificationRule(boolean hasConsecutiveFailures) {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            ProcessDetail processDetail = initDiscProjectDetailPage().getProcess(process.getName());

            final NotificationRule notificationRule = getNewRule(hasConsecutiveFailures);
            processDetail.openNotificationRuleDialog()
                    .createNotificationRule(notificationRule)
                    .closeDialog();

            NotificationRuleItem notifyItem = processDetail.openNotificationRuleDialog()
                    .getLastNotificationRuleItem()
                    .expand();

            assertEquals(notifyItem.getEmail(), notificationRule.getEmail());
            assertEquals(notifyItem.getSelectedEvent(), notificationRule.getEvent());
            assertEquals(notifyItem.getSubject(), notificationRule.getSubject());
            assertEquals(notifyItem.getMessage(), notificationRule.getMessage());
            if (hasConsecutiveFailures) {
                assertEquals(notifyItem.getConsecutiveFailures(), notificationRule.getConsecutiveFailures());
            }
        } finally {
            getProcessService().removeProcess(process);
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
            getProcessService().removeProcess(process);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "consecutiveFailuresProvider")
    public void editNotificationRule(boolean hasConsecutiveFailures) {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            ProcessDetail processDetail = initDiscProjectDetailPage().getProcess(process.getName());

            final NotificationRule notificationRule = getNewRule(hasConsecutiveFailures);
            final String editedSubject = "Edited subject " + generateHashString();
            final Integer editedConsecutiveFailures = CONSECUTIVE_FAILURES_VALUE + 1;

            NotificationRuleItem notifyItem = processDetail.openNotificationRuleDialog()
                    .createNotificationRule(notificationRule)
                    .getLastNotificationRuleItem()
                    .enterSubject(editedSubject);

            notifyItem.cancel();
            assertEquals(notifyItem.getSubject(), notificationRule.getSubject());

            if (hasConsecutiveFailures) {
                assertEquals(notifyItem.getConsecutiveFailures(), CONSECUTIVE_FAILURES_VALUE);
                notifyItem.enterConsecutiveFailures(editedConsecutiveFailures);
            }
            notifyItem.enterSubject(editedSubject).save();
            assertEquals(notifyItem.getSubject(), editedSubject);
            if (hasConsecutiveFailures) {
                assertEquals(notifyItem.getConsecutiveFailures(), editedConsecutiveFailures);
            }
        } finally {
            getProcessService().removeProcess(process);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "consecutiveFailuresProvider")
    public void deleteNotificationRule(boolean hasConsecutiveFailures) {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            NotificationRulesDialog notificationRuleDialog = initDiscProjectDetailPage()
                    .getProcess(process.getName())
                    .openNotificationRuleDialog();

            NotificationRuleItem notifyItem = notificationRuleDialog
                    .createNotificationRule(getNewRule(hasConsecutiveFailures))
                    .getLastNotificationRuleItem()
                    .tryDeleteButCancel();
            assertEquals(notificationRuleDialog.getNotificationRuleNumber(), 1);

            notifyItem.deleteRule();
            takeScreenshot(browser, "Notification-rule-delete-successfully", getClass());
            assertEquals(notificationRuleDialog.getNotificationRuleNumber(), 0);

        } finally {
            getProcessService().removeProcess(process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkNotificationNumber() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            ProcessDetail processDetail = initDiscProjectDetailPage().getProcess(process.getName());
            assertEquals(processDetail.getNotificationRuleDescription(), "No notification rules");

            processDetail.openNotificationRuleDialog()
                    .createNotificationRule(getNewRule(false))
                    .closeDialog();
            assertEquals(processDetail.getNotificationRuleDescription(), "1 notification rule");

        } finally {
            getProcessService().removeProcess(process);
        }
    }

    private NotificationRule getNewRule(boolean configConsecutiveFailures) {
        NotificationRule notificationRule = new NotificationRule()
                .withEmail(testParams.getUser())
                .withEvent(configConsecutiveFailures ? NotificationEvent.FAILURE : NotificationEvent.SUCCESS)
                .withSubject("Subject " + generateHashString())
                .withMessage("Message " + generateHashString());
        if (configConsecutiveFailures) {
            notificationRule.withConsecutiveFailures(CONSECUTIVE_FAILURES_VALUE);
        }
        return notificationRule;
    }
}
