package com.gooddata.qa.graphene.indigo.sdk.common;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static java.lang.String.format;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.utils.ProcessBuilderUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeClass;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class AbstractReactSdkTest extends GoodSalesAbstractTest {

    public String storePath = "/src/";
    public String fileUploadJson = "App.js";

    @BeforeClass
    public void initDirection() {
        if (testParams.useBoilerPlate()) {
            storePath = "/src/routes/";
            fileUploadJson = "Home.js";
            replaceContentAppRoute("<Redirect to=\"/welcome\" />", "");
        }
    }

    /**
     * Using create a catalog.json where contains necessary variables
     * to run java script
     * Example: Require value for visualizationName in file java script
     * and it is gotten from catalog.json
     * @param variables Pair.of("visualizationName", $titleOfVisualization)
     */
    public File createCatalogJSON(Pair<String, String>... variables) throws IOException {
        sleepTightInSeconds(3);
        createTestingVariable(variables);
        return createCatalogExportConfig(testParams.getProjectId(), "catalog.json");
    }

    public boolean isUIsdk8() {
        return testParams.getUIsdkVersion().startsWith("8");
    }

    public void replaceContentAppJSFrom(String fileName) throws IOException {
        Path appPath = Paths.get(testParams.getReactFolder() + testParams.getReactProjectTitle() + storePath  + fileUploadJson);
        File appFile = appPath.toFile();
        Files.copy(Paths.get(
                getFilePathFromResource(isUIsdk8() ? "/template-sdk-8/" + fileName : "/" + fileName)),
                appPath, REPLACE_EXISTING);
        log.info("Waiting for updating " + appFile.getAbsolutePath());
        Graphene.waitGui().withTimeout(3, TimeUnit.SECONDS).until(
                browser -> appFile.isFile() && appFile.exists() && appFile.length() > 0);
        log.info("File updated completely with size " + appFile.length());

        File pm2File = new File("/tmp/react/.npm-global/bin");
        List<String> commands = new LinkedList<>();
        commands.add("/tmp/react/.npm-global/bin/pm2");
        commands.add("reload");
        commands.add("APP");
        ProcessBuilderUtils.runCommandLine(commands, pm2File);
    }

    public void createTestingVariable(Pair<String, String>... variables) {
        try {
            JSONObject obj = new JSONObject();
            for (Pair<String, String> variable : variables) {
                obj.put(variable.getKey(), variable.getValue());
            }
            List<String> lines = Arrays.asList(obj.toString());
            Path path = Paths.get(testParams.getReactFolder() + testParams.getReactProjectTitle() + storePath + "testing-variable.json");
            Files.write(path, lines);
            log.info("Created a new file: " + path.toString());
            log.info("Content:" + new String(Files.readAllBytes(path)));
        } catch (IOException e) {
            throw new RuntimeException("there is an error while creating testing-variable.json", e);
        }
    }

    public File createCatalogExportConfig(String projectID, String fileName) throws JSONException, IOException {
        final File file = new File(testParams.getReactFolder() + testParams.getReactProjectTitle() + storePath + fileName);
        Files.deleteIfExists(file.toPath());
        Graphene.waitGui().until(browser -> !file.exists());
        try {
            Graphene.waitGui().withTimeout(5, TimeUnit.SECONDS)
                    .until(browser -> isElementPresent(By.tagName("iframe"), browser));
        } catch(TimeoutException e) {
            //do nothing
        }

        List<String> commands = new LinkedList<>();
        commands.add("gdc-catalog-export");
        commands.add("--project-id");
        commands.add(projectID);
        commands.add("--username");
        commands.add(testParams.getUser());
        commands.add("--password");
        commands.add(testParams.getPassword());
        commands.add("--hostname");
        commands.add(testParams.getHost());
        commands.add("--output");
        commands.add(storePath.substring(1) + fileName);
        try{
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            processBuilder.directory(new File(testParams.getReactFolder() + testParams.getReactProjectTitle()));
            Process process = processBuilder.start();
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String inputStream;
            while ((inputStream = reader.readLine()) != null) {
                System.out.println(inputStream);
            }
            Graphene.waitGui().until(browser -> file.isFile() && file.exists() && file.length() > 0);
            log.info(format("Created a new file: %s with size %s", file.getAbsolutePath(), file.length()));
            //Make sure applying new exported catalog
            if (browser.getCurrentUrl().contains(testParams.getLocalhostSDK())) {
                browser.navigate().refresh();
                waitForElementPresent(By.id("root"), browser);
            } else {
                initSDKAnalysisPage(); //To avoid some pages contain iframe tag
            }
            waitForElementNotPresent(By.tagName("iframe"), browser);
            return file;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("there is an error while creating catalog.json", e);
        }
    }

    private void replaceContentAppRoute(String search, String replace) {
        try {
            Path path = Paths.get(testParams.getReactFolder() + testParams.getReactProjectTitle() + storePath + "/AppRouter.js");
            Charset charset = StandardCharsets.UTF_8;

            String content = new String(Files.readAllBytes(path), charset);
            content = content.replaceAll(search, replace);
            Files.write(path, content.getBytes(charset));
        } catch (IOException e) {
            //Simple exception handling, replace with what's necessary for your use case!
            throw new RuntimeException("Generating file failed", e);
        }
    }
}
