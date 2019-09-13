package com.gooddata.qa.graphene.csvuploader;

import com.gooddata.project.Environment;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.utils.ProcessBuilderUtils;
import org.testng.annotations.Test;

import java.io.*;
import java.nio.file.*;
import java.util.LinkedList;
import java.util.List;

import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;

public class CSVUploaderTest extends AbstractCsvUploaderTest {

    private static String newProjectID;
    private final File file = new File(System.getProperty(
        "maven.project.build.directory",
        "/graphene-tests/ui-tests-trial/ui-tests-trial-csv-uploader/src/test/resources"));

    @Override
    protected void initProperties() {
        testParams.setDomainUser(testParams.getUser());
        validateAfterClass = false;
    }

    @Test
    public void checkInstallingGoodDataRubySDKAndDownloadTheRepository() {
        List<String> commands = new LinkedList<>();
        commands.add("gooddata");
        commands.add("--version");

        String outputStream = ProcessBuilderUtils.runCommandLine(commands, file);
        assertThat(outputStream, containsString("gooddata version 2.1.3"));
    }

    @Test(dependsOnMethods = "checkInstallingGoodDataRubySDKAndDownloadTheRepository")
    public void checkStoreUserCredentials() {
        final org.json.JSONObject obj = new org.json.JSONObject() {{
            put("username", testParams.getUser());
            put("password", testParams.getPassword());
            put("server", testParams.getHost());
            put("environment", Environment.PRODUCTION);
            put("auth_token", testParams.getAuthorizationToken());
        }};

        new File(System.getProperty(
            "maven.project.build.directory", "/graphene-tests/.gooddata"));

        try (FileWriter file = new FileWriter(Paths.get("/graphene-tests/.gooddata").toString())) {
            file.write(obj.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test(dependsOnMethods = "checkStoreUserCredentials")
    public void createNewWorkspaceOnCommandLine() {
        List<String> commands = new LinkedList<>();
        commands.add("gooddata");
        commands.add("project");
        commands.add("create");

        String outputStream = ProcessBuilderUtils.runCommandLine(commands, file);
        assertThat(outputStream, containsString("state\":\"ENABLED"));

        int a = outputStream.indexOf("projectFeatureFlags");
        int b = outputStream.indexOf("/projectFeatureFlags");
        newProjectID = outputStream.substring(a, b).split("/projects/")[1];
        log.info("New Project ID: " + newProjectID);
        testParams.setProjectId(newProjectID);
    }

    @Test(dependsOnMethods = "createNewWorkspaceOnCommandLine")
    public void refactorTheDataModelAndUploadNewData() throws IOException {
        try {
            final File file = new File(System.getProperty("maven.project.build.directory", "/tmp"));

            String fileUpdated = generateHashString();
            Files.copy(Paths.get(getFilePathFromResource(("/02_update.rb"))),
                Paths.get("/tmp/" + fileUpdated + ".rb"), StandardCopyOption.REPLACE_EXISTING);

            List<String> commands = new LinkedList<>();
            commands.add("chmod");
            commands.add("+x");
            commands.add(fileUpdated + ".rb");
            ProcessBuilderUtils.runCommandLine(commands, file);

            commands = new LinkedList<>();
            commands.add("./"+ fileUpdated + ".rb");
            commands.add(newProjectID);
            String outputStream = ProcessBuilderUtils.runCommandLine(commands, file);
            assertThat(outputStream, containsString("Connection successful"));

            initAnalysePage();
            assertEquals(AnalysisPage.getInstance(browser).getCatalogPanel()
                .getFieldNamesInViewPort(), asList("Date", "Budget", "Campaign Category", "Campaign Channel ID",
                "Campaign ID", "Campaign Name", "Campaign Type", "Customer ID", "Customer Name", "Customer State",
                "Order ID", "Order Line ID", "Order Status", "Price", "Product", "Product Category", "Product ID",
                "Quantity", "Spend"));
        } finally {
            testParams.setProjectId(newProjectID);
            deleteProject(testParams.getProjectId());
        }
    }
}
