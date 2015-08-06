package com.gooddata.qa.graphene.fragments.greypages.projects;

import com.gooddata.qa.graphene.enums.DWHDriver;
import com.gooddata.qa.graphene.enums.ProjectEnvironment;
import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.io.File;
import java.io.IOException;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;

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
            DWHDriver dwhDriver, ProjectEnvironment enviroment, int checkIterations) throws JSONException, InterruptedException {
        waitForElementVisible(this.title).sendKeys(title);
        if (summary != null && summary.length() > 0) this.summary.sendKeys(summary);
        if (template != null && template.length() > 0) this.projectTemplate.sendKeys(template);

        switch (dwhDriver) {
            case PG: this.pg.click(); break;
            case VERTICA: this.vertica.click(); break;
            default: this.pg.click();
        }

        selectEnviroment(enviroment);
        this.authorizationToken.sendKeys(authorizationToken);
        Graphene.guardHttp(submit).click();
        waitForElementNotVisible(this.title);
        Graphene.guardHttp(waitForElementVisible(BY_GP_LINK, browser)).click();
        return waitForProjectStateEnabled(checkIterations);
    }

    private String waitForProjectStateEnabled(int checkIterations) throws JSONException, InterruptedException {
        String projectUrl = browser.getCurrentUrl();
        logProjectUrl(projectUrl);
        System.out.println("Waiting for project enabled: " + projectUrl);
        waitForPollState("ENABLED", checkIterations);
        return projectUrl.substring(projectUrl.lastIndexOf("/") + 1);
    }

    private void selectEnviroment(ProjectEnvironment enviroment) {
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
