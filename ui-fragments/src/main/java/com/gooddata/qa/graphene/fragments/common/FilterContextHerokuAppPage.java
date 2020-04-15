package com.gooddata.qa.graphene.fragments.common;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.fragments.dashboards.widget.EmbeddedWidget;
import com.gooddata.qa.graphene.utils.ElementUtils;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;

public class FilterContextHerokuAppPage extends EmbeddedWidget {

    @FindBy(tagName = "input")
    private WebElement inputEmbedded;

    @FindBy(xpath = "//button[contains(text(),'Send Message')]")
    private WebElement sendMessageButton;

    @FindBy(css = "[id$=json-message-input-content-box]")
    private WebElement inputFilterContent;

    private static final By BY_IFRAME = By.tagName("iframe");
    private static final String ROOT_CLASS = "test-message-container";
    private static final By BUSY_MASK = By.className("gdc-busy-mask-visible");
    private static final By NO_DATA = By.className("noData");

    public static FilterContextHerokuAppPage getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(FilterContextHerokuAppPage.class,
            waitForElementVisible(By.className(ROOT_CLASS), searchContext));
    }

    public boolean isNoData() {
        browser.switchTo().frame(waitForElementVisible(BY_IFRAME, browser));
        Boolean isNoData = isElementPresent(NO_DATA, browser);
        BrowserUtils.switchToMainWindow(browser);
        return isNoData;
    }

    public FilterContextHerokuAppPage inputEmbeddedDashboardUrl(String embeddedUri) {
        waitForElementVisible(inputEmbedded).clear();
        waitForElementVisible(inputEmbedded).sendKeys(embeddedUri, Keys.ENTER);

        return waitForLoaded();
    }

    public FilterContextHerokuAppPage setFilterContext(List<Pair<String, String>> values) {
        JSONObject jsonObject = new JSONObject() {{
            put("setFilterContext", new JSONArray() {{
                values.forEach((value) -> {
                    put(new JSONObject() {{
                        put("label", value.getKey());
                        put("value", value.getValue());
                    }});
                });
            }});
        }};

        waitForElementVisible(inputFilterContent).clear();
        waitForElementVisible(inputFilterContent).sendKeys(jsonObject.toString());
        log.info("Message Send : " + convertPrettyJson(jsonObject.toString()));

        waitForElementVisible(sendMessageButton).click();
        return waitForLoaded();
    }

    public FilterContextHerokuAppPage setFilterContext(String label, List<String> values) {
        JSONObject jsonObject = new JSONObject() {{
            put("setFilterContext", new JSONArray() {{
                values.forEach((value) -> {
                    put(new JSONObject() {{
                        put("label", label);
                        put("value", value);
                    }});
                });
            }});
        }};

        waitForElementVisible(inputFilterContent).clear();
        waitForElementVisible(inputFilterContent).sendKeys(jsonObject.toString());
        log.info("Message Send : " + convertPrettyJson(jsonObject.toString()));

        waitForElementVisible(sendMessageButton).click();
        return waitForLoaded();
    }

    public FilterContextHerokuAppPage setDateFilterContext(List<Pair<String, Pair<String, String>>> values) {
        JSONObject jsonObject = new JSONObject() {{
            put("setFilterContext", new JSONArray() {{
                values.forEach((value) -> {
                    put(new JSONObject() {{
                        put("label", value.getKey());
                        put("type", "date");
                        put("from", value.getValue().getKey());
                        put("to", value.getValue().getValue());
                    }});
                });
            }});
        }};

        waitForElementVisible(inputFilterContent).clear();
        waitForElementVisible(inputFilterContent).sendKeys(jsonObject.toString());
        log.info("Message Send : " + convertPrettyJson(jsonObject.toString()));

        waitForElementVisible(sendMessageButton).click();
        return waitForLoaded();
    }

    public List<String> getLatestIncomingMessage() {
        List<String> messages = ElementUtils.getElementTexts(waitForCollectionIsNotEmpty(
            browser.findElements(BY_LATEST_INCOMING_MESSAGE)));

        //wait for the latest message text is updated
        messages = waitToChange(messages.get(0));
        log.info("Incoming Message :" + messages.toString() + "\n");
        return messages;
    }

    private String convertPrettyJson(String uglyJSONString) {
        return new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(uglyJSONString));
    }

    private FilterContextHerokuAppPage waitForLoaded() {
        browser.switchTo().frame(waitForElementVisible(BY_IFRAME, browser));
        try {
            sleepTightInSeconds(2);
            if (isPageLoaded()) {
                WebElement computingElement = browser.findElement(BUSY_MASK);
                waitForElementNotVisible(computingElement);
            }
        } catch (Exception e) {
            // Variable detail already loaded so WebDriver unable to catch the loading indicator
        }

        BrowserUtils.switchToMainWindow(browser);
        return this;
    }

    private boolean isPageLoaded() {
        return isElementPresent(BUSY_MASK, browser);
    }
}
