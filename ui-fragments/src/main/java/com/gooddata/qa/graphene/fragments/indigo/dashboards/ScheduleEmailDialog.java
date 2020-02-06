package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import java.util.List;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

public class ScheduleEmailDialog extends AbstractFragment {

    private static final String DIALOG_ROOT = "s-gd-schedule-email-dialog";
    private static final By BY_MESSAGE_TEXT = className("gd-message-text");
    private static final String RECIPIENTS_INPUT_CLASS_NAME = "gd-recipients__input";

    @FindBy(className = "s-dialog-submit-button")
    private WebElement submitSchedule;

    @FindBy(className = "s-gd-recipients-value")
    private WebElement recipientsToField;

    @FindBy(className = "s-gd-recipient-remove")
    private WebElement recipientsRemove;

    @FindBy(className = RECIPIENTS_INPUT_CLASS_NAME)
    private WebElement recipientsInput;

    @FindBy(css = ".gd-datepicker-input input")
    private WebElement datepickerInput;

    @FindBy(className = "gd-recipients__option")
    private List<WebElement> recipientsSuggestion;

    @FindBy(css = ".s-gd-schedule-email-dialog-subject input")
    private WebElement subjectInput;

    public static ScheduleEmailDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(ScheduleEmailDialog.class,
                waitForElementVisible(className(DIALOG_ROOT), context));
    }

    public void submit() {
        waitForElementVisible(submitSchedule).click();
        Assert.assertEquals(waitForElementVisible(ElementUtils.BY_SUCCESS_MESSAGE_BAR, browser).getText(),
            "Success! Your dashboard is scheduled for emailing.");
    }

    public List<String> getRecipientValues() {
        return waitForElementVisible(recipientsToField)
            .findElements(className("s-gd-recipient-value-item"))
            .stream().map(WebElement::getText).collect(toList());
    }

    public boolean isRecipientValueWrongFormat(String email) {
        return getRecipientValue(email).getAttribute("class").contains("not-valid");
    }

    private WebElement getRecipientValue(String email) {
        return waitForElementVisible(recipientsToField)
            .findElements(className("s-gd-recipient-value-item"))
            .stream()
            .filter(e -> e.getText().equals(email))
            .findFirst()
            .get();
    }

    public List<String> getRecipientsSuggestion() {
        return recipientsSuggestion.stream().map(WebElement::getText).collect(toList());
    }

    public ScheduleEmailDialog addRecipientToField(String email) {
        clickToField();
        getActions().sendKeys(email).sendKeys(Keys.ENTER).perform();
        return this;
    }

    public void clickToField() {
        waitForElementVisible(recipientsInput).click();
    }

    public boolean isRecipientsInputPresent() {
        return isElementPresent(className(RECIPIENTS_INPUT_CLASS_NAME), browser);
    }

    public void removeRecipients() {
        waitForElementVisible(recipientsRemove).click();
    }

    public String getMessageText() {
        return waitForElementVisible(BY_MESSAGE_TEXT, getRoot()).getText();
    }

    public ScheduleEmailDialog setDate(String date) {
        ElementUtils.clear(waitForElementVisible(datepickerInput));
        getActions().sendKeys(date, Keys.ENTER).perform();
        return this;
    }

    public ScheduleEmailDialog setSubject(String date) {
        ElementUtils.clear(waitForElementVisible(subjectInput));
        getActions().sendKeys(date, Keys.ENTER).perform();
        return this;
    }

    public ScheduleEmailDialog chooseTime(String time) {
        DropDown.getInstance(cssSelector(".gd-schedule-email-dialog-datetime-time div[class*='dropdown']"), getRoot())
            .selectByName(time);
        return this;
    }

    public ScheduleEmailDialog chooseRepeats(String option) {
        DropDown.getInstance(cssSelector(".s-gd-schedule-email-dialog-repeat-type div[class*='dropdown']"), getRoot())
            .selectByName(option);
        return this;
    }

    public ScheduleEmailDialog chooseRepeatsFrequency(String option) {
        DropDown.getInstance(cssSelector(".s-gd-schedule-email-dialog-repeat-frequency div[class*='dropdown']"), getRoot())
            .selectByName(option);
        return this;
    }

    public static class DropDown extends AbstractReactDropDown {

        @Override
        protected String getDropdownCssSelector() {
            return ".overlay.dropdown-body";
        }

        public static ScheduleEmailDialog.DropDown getInstance(By locator, SearchContext searchContext) {
            WebElement root = waitForElementVisible(locator, searchContext);

            return Graphene.createPageFragment(ScheduleEmailDialog.DropDown.class, root);
        }
    }
}
