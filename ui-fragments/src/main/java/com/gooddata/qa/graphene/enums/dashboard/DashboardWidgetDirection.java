package com.gooddata.qa.graphene.enums.dashboard;

import org.jboss.arquillian.drone.api.annotation.Default;
import org.jboss.arquillian.graphene.context.GrapheneContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum DashboardWidgetDirection {

    LEFT("(left): (\\d+)px", 0),
    RIGHT("(left): (\\d+)px", 630),
    UP("(top): (\\d+)px", 0),
    MIDDLE("(top): (\\d+)px", 60),
    DOWN("(top): (\\d+)px", 120);

    private String pattern;
    private int expectedCoordinates;

    DashboardWidgetDirection(String pattern, int expectedCoordinates) {
        this.pattern = pattern;
        this.expectedCoordinates = expectedCoordinates;
    }

    public void moveElementToRightPlace(WebElement element) {
        WebDriver driver = GrapheneContext.getContextFor(Default.class).getWebDriver(WebDriver.class);

        Map<String, Integer> distance = getDistance(element);
        new Actions(driver).clickAndHold(element)
                .moveByOffset(distance.get("x"), distance.get("y"))
                .release()
                .perform();
    }

    private Map<String, Integer> getDistance(WebElement element) {
        Map<String, Integer> distance = new HashMap<>();

        Pattern p = Pattern.compile(this.pattern);
        Matcher m = p.matcher(element.getAttribute("style"));

        if (!m.find()) {
            throw new RuntimeException("Not found pattern: " + this.pattern + " in element HTML");
        }

        int x = 0;
        int y = 0;

        if (m.group(1).equals("left")) {
            x = this.expectedCoordinates - Integer.parseInt(m.group(2));
        } else {
            y = this.expectedCoordinates - Integer.parseInt(m.group(2));
        }

        distance.put("x", x);
        distance.put("y", y);
        return distance;
    }
}
