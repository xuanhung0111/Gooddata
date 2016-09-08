package com.gooddata.qa.graphene.fragments.greypages.projects;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import com.gooddata.project.Environment;
import com.gooddata.project.ProjectDriver;
import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;

public class ProjectFragment extends AbstractGreyPagesFragment {

    @FindBy
    private WebElement title;

    @FindBy
    private WebElement summary;

    @FindBy
    private WebElement projectTemplate;

    @FindBy
    private WebElement authorizationToken;

    @FindBy (id="Vertica")
    private WebElement vertica;

    @FindBy (id="MYSQL")
    private WebElement mysql;

    @FindBy (id="PGSQL")
    private WebElement pg;

    @FindBy
    private WebElement submit;

    public String createProject(String title, String summary, String template, String authorizationToken,
            ProjectDriver projectDriver, Environment enviroment, int checkIterations) throws JSONException {
        waitForElementVisible(this.title).sendKeys(title);
        if (summary != null && summary.length() > 0) this.summary.sendKeys(summary);
        if (template != null && template.length() > 0) this.projectTemplate.sendKeys(template);

        switch (projectDriver) {
            case POSTGRES: this.pg.click(); break;
            case VERTICA: this.vertica.click(); break;
            default: this.pg.click();
        }

        selectEnviroment(enviroment);
        this.authorizationToken.sendKeys(authorizationToken);
        waitForElementVisible(submit).click();
        waitForElementNotVisible(this.title);
        waitForElementVisible(BY_GP_LINK, browser).click();
        Assert.assertEquals(loadJSON().getJSONObject("project").getJSONObject("meta").getString("title"), title,
                "Project creation wasn't redirected properly to project grey page");
        return waitForProjectStateEnabled(checkIterations);
    }

    public String getDwhDriverSelected() {
        return Stream.of(vertica, mysql, pg)
                .filter(e -> Objects.nonNull(e.getAttribute("checked")))
                .map(e -> e.getAttribute("value"))
                .findFirst()
                .get();
    }

    private String waitForProjectStateEnabled(int checkIterations) throws JSONException {
        String projectUrl = browser.getCurrentUrl();
        logProjectUrl(projectUrl);
        System.out.println("Waiting for project enabled: " + projectUrl);
        waitForPollState("ENABLED", checkIterations);
        return projectUrl.substring(projectUrl.lastIndexOf("/") + 1);
    }

    private void selectEnviroment(Environment enviroment) {
        waitForElementVisible(By.id(enviroment.toString()), getRoot()).click();
    }

    @Override
    protected String getPollState() throws JSONException {
        return loadJSON().getJSONObject("project").getJSONObject("content").getString("state");
    }

    private void logProjectUrl(String projectUrl) {
        File mavenProjectBuildDirectory = new File(System.getProperty("maven.project.build.directory", "./target/"));
        File outputFile = new File(mavenProjectBuildDirectory, "projectUrls.txt");
        try {
            FileUtils.writeStringToFile(outputFile, projectUrl + "\r\n", true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
