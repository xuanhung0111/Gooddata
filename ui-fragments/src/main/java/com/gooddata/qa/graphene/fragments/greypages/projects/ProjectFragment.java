package com.gooddata.qa.graphene.fragments.greypages.projects;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

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

    @FindBy
    private WebElement submit;

    public String createProject(String title, String summary, String template, String authorizationToken, int checkIterations) throws JSONException, InterruptedException {
        waitForElementVisible(this.title).sendKeys(title);
        if (summary != null && summary.length() > 0) this.summary.sendKeys(summary);
        if (template != null && template.length() > 0) this.projectTemplate.sendKeys(template);
        this.authorizationToken.sendKeys(authorizationToken);
        Graphene.guardHttp(submit).click();
        waitForElementNotVisible(this.title);
        Graphene.guardHttp(waitForElementVisible(BY_GP_LINK)).click();
        return waitForProjectStateEnabled(checkIterations);
    }

    private String waitForProjectStateEnabled(int checkIterations) throws JSONException, InterruptedException {
        String projectUrl = browser.getCurrentUrl();
        logProjectUrl(projectUrl);
        System.out.println("Waiting for project enabled: " + projectUrl);
        waitForPollState("ENABLED",checkIterations);
        return projectUrl.substring(projectUrl.lastIndexOf("/") + 1);
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