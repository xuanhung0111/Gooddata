package com.gooddata.qa.graphene.enums.dashboard;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

public enum DashboardWidgetDirection {

    NONE,
    LEFT("left: 0px", Keys.LEFT),
    RIGHT("left: 630px", Keys.RIGHT),
    UP("top: 0px", Keys.UP),
    MIDDLE("top: 60px", Keys.DOWN),
    DOWN("top: 120px", Keys.DOWN);

    private String direction;
    private Keys key;

    private DashboardWidgetDirection(String direction, Keys key) {
        this.direction = direction;
        this.key = key;
    }

    private DashboardWidgetDirection() {}

    public void moveElementToRightPlace(WebElement element) {
        if (direction == null) {
            return;
        }

        if (this != UP) {
            UP.moveElementToRightPlace(element);
        }

        while (!element.getAttribute("style").contains(direction)) {
            element.sendKeys(Keys.SHIFT, key);
        }
    }
}
