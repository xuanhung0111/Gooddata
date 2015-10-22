package com.gooddata.qa.graphene.fragments.disc;

import java.util.List;
import static java.util.stream.Collectors.toList;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.gooddata.qa.graphene.fragments.AbstractTable;

public class ExecutablesTable extends AbstractTable {

    private final static By BY_EXECUTABLE_PATH = By.xpath("//span[@class='executable-path']");
    private final static By BY_EXECUTABLE = By.cssSelector(".executable-title-cell .executable");
    private final static By BY_EXECUTABLE_SCHEDULE_NUMBER = By
            .cssSelector(".ait-process-executable-list-item-schedules-label");
    private final static By BY_EXECUTABLE_NEW_SCHEDULE_LINK = By
            .cssSelector(".ait-process-executable-list-item-new-schedule-btn");

    public boolean isCorrectExecutableList(List<Executables> executables) {
        List<String> expectedExecutablePaths =
                executables.stream().map(executable -> executable.getExecutablePath()).collect(toList());
        return expectedExecutablePaths.containsAll(getExecutablePaths());
    }

    public List<String> getExecutablePaths() {
        return getRows()
                .stream()
                .map(row -> row.findElement(BY_EXECUTABLE_PATH).getText()
                        + row.findElement(BY_EXECUTABLE).getText()).collect(toList());
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
        return getExecutableListItem(executableName).findElement(BY_EXECUTABLE_SCHEDULE_NUMBER).getText();
    }
}
