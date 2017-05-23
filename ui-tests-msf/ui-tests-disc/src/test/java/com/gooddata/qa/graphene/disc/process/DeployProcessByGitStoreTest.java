package com.gooddata.qa.graphene.disc.process;

import static com.gooddata.qa.graphene.utils.ElementUtils.getBubbleMessage;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.common.AbstractDiscTest;
import com.gooddata.qa.graphene.fragments.disc.ConfirmationDialog;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm;

public class DeployProcessByGitStoreTest extends AbstractDiscTest {

    @DataProvider(name = "invalidGitPathFormatProvider")
    public Object[][] getInvalidGitPathFormatProvider() {
        return new Object[][] {
            {""},
            {"           "},
            {"!@#$%^&*()'\"~"},
            {"C:\\Testdata\\abc"},
            {"${PUBLIC_APPSTORE}:branch"}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "invalidGitPathFormatProvider")
    public void deployProcessWithInvalidFormatGitPath(String gitPath) {
        DeployProcessForm deployForm = initDiscProjectDetailPage().clickDeployButton();

        deployForm.selectGitOption()
                .enterGitPath(gitPath)
                .enterProcessName(generateProcessName())
                .submit();
        assertTrue(deployForm.isGitPathInputError());
        assertEquals(getBubbleMessage(browser), "Path doesn't have proper format");
    }

    @DataProvider(name = "invalidGitPathProvider")
    public Object[][] getInvalidGitPathProvider() {

        String missingInfoJsonPath = "${PUBLIC_APPSTORE}:branch/prodigy-testing:/abc/doesnotexist";
        //TODO: this failed message will be updated after the bug MSF-11118 is fixed
        String missingInfoJsonErrorMessage = "Failed to deploy .*\\. Reasons?: Deployment failed"
                + " on internal error, error_messsage: Info.json is missing in root application directory .";

        String relativeGitPath = "${PUBLIC_APPSTORE}:branch/prodigy-testing:/../ReadFile";
        String relativeGitPathErrorMessage = "Failed to deploy .*\\. Reasons?: Invalid path. "
                + "Deployment path %s cannot contain relative references..";

        String anotherDomainGitPath = "${PRIVATE_APPSTORE}:branch/prodigy-testing:/vietnam/giraffes/ReadFile";
        //TODO: this failed message will be updated after the bug MSF-11120 is fixed
        String anotherDomainErrorMessage = "Failed to deploy .*\\. Reasons?: Invalid path. "
                + "Deployment path /vietnam/giraffes/ReadFile/ not allowed for this domain.";

        return new Object[][] {
            {missingInfoJsonPath, missingInfoJsonErrorMessage},
            {relativeGitPath, relativeGitPathErrorMessage},
            {anotherDomainGitPath, anotherDomainErrorMessage}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "invalidGitPathProvider")
    public void deployProcessWithInvalidGitPathOnProjectDetailPage(String gitPath, String errorMessage) {
        initDiscProjectDetailPage().deployProcessWithGitStorePath(generateProcessName(), gitPath);

        ConfirmationDialog confirmDialog = ConfirmationDialog.getInstance(browser);
        assertEquals(confirmDialog.getTitle(), "Process failed to deploy");

        String completeErrorMessage = format(errorMessage, gitPath.replace("$", "\\$").replace("{", "\\{").replace("}", "\\}"));
        assertTrue(confirmDialog.getMessage().matches(completeErrorMessage), "Error message not show correctly");
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "invalidGitPathProvider")
    public void deployProcessWithInvalidGitPathOnProjectsPage(String gitPath, String errorMessage) {
        initDiscProjectsPage().markProjectCheckbox(projectTitle)
                .deployProcessWithGitStorePath(generateProcessName(), gitPath);

        String completeErrorMessage = format(errorMessage, gitPath.replace("$", "\\$").replace("{", "\\{").replace("}", "\\}"));
        assertTrue(projectsPage.getErrorBarMessage().matches(completeErrorMessage), "Error message not show correctly");
    }
}
