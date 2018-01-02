package com.gooddata.qa.graphene.fragments.disc.process;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.util.Collection;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.disc.schedule.Executable;
import com.gooddata.qa.graphene.fragments.disc.ConfirmationDialog;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;
import com.gooddata.qa.graphene.fragments.disc.schedule.CreateScheduleForm;
import com.gooddata.qa.graphene.fragments.disc.schedule.ScheduleDetail;

public class ProcessDetail extends AbstractProcessDetail {

    @FindBy(className = "ait-process-executable-list-item")
    private Collection<WebElement> executables;

    @FindBy(className = "ait-process-delete-btn")
    private WebElement deleteButton;

    @FindBy(className = "ait-process-download-btn")
    private WebElement downloadButton;

    @FindBy(className = "ait-process-redeploy-btn")
    private WebElement redeployButton;

    public Collection<String> getExecutables() {
        return executables.stream().map(this::getExecutableTitle).collect(toList());
    }

    public String getScheduleInfoFrom(Executable executable) {
        return getExecutableElement(executable).findElement(By.className("executable-schedules-cell")).getText();
    }

    public CreateScheduleForm clickScheduleLinkFrom(Executable executable) {
        getExecutableElement(executable).findElement(By.cssSelector("a[class*='new-schedule-btn']")).click();
        return CreateScheduleForm.getInstance(browser);
    }

    public void downloadProcess() {
        waitForElementVisible(downloadButton).click();
    }

    public ProcessDetail redeployWithZipFile(String processName, ProcessType processType, File packageFile) {
        clickRedeployButton().selectZipAndDeploy(processName, processType, packageFile);
        return this;
    }

    public ProcessDetail redeploySqlExecutorProcess(String newProcessName) {
        clickRedeployButton().enterEtlProcessNameAndDeploy(newProcessName);
        return this;
    }

    public ConfirmationDialog clickDeleteButton() {
        waitForElementVisible(deleteButton).click();
        return ConfirmationDialog.getInstance(browser);
    }

    public DeployProcessForm clickRedeployButton() {
        waitForElementVisible(redeployButton).click();
        return DeployProcessForm.getInstance(browser);
    }

    public void deleteProcess() {
        clickDeleteButton().confirm();
    }

    public ScheduleDetail openSchedule(String scheduleName) {
        findSchedule(scheduleName).get().findElement(By.cssSelector(".schedule-title-cell a")).click();
        return ScheduleDetail.getInstance(browser);
    }

    private WebElement getExecutableElement(Executable executable) {
        return executables.stream()
                .filter(e -> executable.getPath().equals(getExecutableTitle(e)))
                .findFirst()
                .get();
    }

    private String getExecutableTitle(WebElement executable) {
        return waitForElementVisible(executable).findElement(By.className("executable-title-cell"))
                .getText().replace(" ", "");
    }
}
