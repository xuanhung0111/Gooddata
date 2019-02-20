package com.gooddata.qa.graphene.indigo.sdk.common;

import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AbstractReactSdkTest extends GoodSalesAbstractTest {

    /**
     * Using create a catalog.json where contains necessary variables
     * to run java script
     * Example: Require value for visualizationName in file java script
     * and it is gotten from catalog.json
     * @param variables Pair.of("visualizationName", $titleOfVisualization)
     */
    public void createCatalogJSON(Pair<String, String>... variables) throws IOException {
        createTestingVariable(variables);
        createCatalogExportConfig();
    }

    public void replaceContentAppJSFrom(String fileName) throws IOException {
        Path appPath = Paths.get(testParams.getReactFolder() + testParams.getReactProjectTitle() + "/src/App.js");
        File appFile = appPath.toFile();
        Files.copy(Paths.get(getFilePathFromResource("/" + fileName)), appPath, REPLACE_EXISTING);
        log.info("Waiting for updating " + appFile.getAbsolutePath());
        Graphene.waitGui().withTimeout(3, TimeUnit.SECONDS).until(browser -> appFile.exists() && appFile.length() > 0);
        log.info("Update completely");
    }

    public void createTestingVariable(Pair<String, String>... variables) {
        try {
            JSONObject obj = new JSONObject();
            for (Pair<String, String> variable : variables) {
                obj.put(variable.getKey(), variable.getValue());
            }
            List<String> lines = Arrays.asList(obj.toString());
            Path path = Paths.get(testParams.getReactFolder() + testParams.getReactProjectTitle() + "/src/testing-variable.json");
            Files.write(path, lines);
            log.info("Created a new file: " + path.toString());
            log.info("Content:" + new String(Files.readAllBytes(path)));
        } catch (IOException e) {
            throw new RuntimeException("there is an error while creating testing-variable.json", e);
        }
    }

    private void createCatalogExportConfig() throws JSONException, IOException {
        final File file = new File(testParams.getReactFolder() + testParams.getReactProjectTitle() + "/src/catalog.json");
        Files.deleteIfExists(file.toPath());
        List<String> commands = new LinkedList<>();
        commands.add("gdc-catalog-export");
        commands.add("--project-id");
        commands.add(testParams.getProjectId());
        commands.add("--username");
        commands.add(testParams.getUser());
        commands.add("--password");
        commands.add(testParams.getPassword());
        commands.add("--hostname");
        commands.add(testParams.getHost());
        commands.add("--output");
        commands.add("src/catalog.json");
        try{
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            processBuilder.directory(new File(testParams.getReactFolder() + testParams.getReactProjectTitle()));
            Process process = processBuilder.start();
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String inputStream;
            while ((inputStream = reader.readLine()) != null) {
                System.out.print(inputStream);
            }
            System.out.print("Created a new file: " + file.getAbsolutePath());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("there is an error while creating catalog.json", e);
        }
    }
}
