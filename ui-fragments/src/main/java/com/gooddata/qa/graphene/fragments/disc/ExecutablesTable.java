package com.gooddata.qa.graphene.fragments.disc;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import com.gooddata.qa.graphene.enums.DISCProcessTypes;
import com.gooddata.qa.graphene.fragments.AbstractTable;

import static org.testng.Assert.*;

public class ExecutablesTable extends AbstractTable {

    private final static By BY_EXECUTABLE_PATH = By.xpath("//span[@class='executable-path']");
    private final static By BY_EXECUTABLE = By.cssSelector(".executable-title-cell .executable");
    private final static By BY_EXECUTABLE_SCHEDULE_NUMBER = By
            .cssSelector(".ait-process-executable-list-item-schedules-label");
    private final static By BY_EXECUTABLE_NEW_SCHEDULE_LINK = By
            .cssSelector(".ait-process-executable-list-item-new-schedule-btn");

    public void assertExecutablesList(DISCProcessTypes processType, List<String> executables) {
        String executablePath = String.format("/%s/", processType.getProcessTypeExecutable());
        for (int i = 0; i < this.getNumberOfRows(); i++) {
            assertEquals(getRow(i).findElement(BY_EXECUTABLE_PATH).getText(), executablePath);
            assertEquals(executables.get(i), getRow(i).findElement(BY_EXECUTABLE).getText());
        }
    }

    public WebElement getExecutableListItem(String executableName) {
        for (int i = 0; i < this.getNumberOfRows(); i++) {
            if (executableName.equals(getRow(i).findElement(BY_EXECUTABLE).getText()))
                return getRow(i);
        }
        return null;
    }

    public WebElement getExecutableScheduleLink(String executableName) {
        return getExecutableListItem(executableName).findElement(BY_EXECUTABLE_NEW_SCHEDULE_LINK);
    }

    public String getExecutableScheduleNumber(String executableName) {
        return getExecutableListItem(executableName).findElement(BY_EXECUTABLE_SCHEDULE_NUMBER)
                .getText();
    }
}
