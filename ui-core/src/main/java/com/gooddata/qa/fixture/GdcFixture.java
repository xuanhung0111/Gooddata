package com.gooddata.qa.fixture;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.gooddata.AbstractPollHandler;
import com.gooddata.GoodData;
import com.gooddata.GoodDataRestException;
import com.gooddata.PollResult;
import com.gooddata.gdc.GdcError;
import com.gooddata.gdc.TaskStatus;
import com.gooddata.project.Environment;
import com.gooddata.project.ProjectDriver;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;
import com.gooddata.qa.utils.io.ResourceUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.gooddata.qa.utils.http.RestUtils.getJsonObject;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static java.lang.String.format;

public class GdcFixture {

    private Fixture fixture;
    private String model;
    private String metadata;
    private String uploadInfo;
    private List<String> csvFiles;

    private String projectId;
    private RestApiClient restApiClient;
    private GoodData goodDataClient;

    private static final String PULL_DATA_LINK = "/gdc/md/%s/etl/pull2";
    private final Logger log = Logger.getLogger(this.getClass().getName());

    public GdcFixture(Fixture fixture) throws IOException {
        this.fixture = fixture;
        loadFiles();
    }

    public void setRestApiClient(RestApiClient restApiClient) {
        this.restApiClient = restApiClient;
    }

    public void setGoodDataClient(GoodData goodDataClient) {
        this.goodDataClient = goodDataClient;
    }

    public String deploy(String title, String authToken, ProjectDriver driver,
                         Environment environment) throws IOException, JSONException {
        log.info("Deploying fixture: ...");

        if (Objects.isNull(restApiClient) || Objects.isNull(goodDataClient))
            throw new RuntimeException("Rest api or Goodata client has not been initialized !");

        projectId = ProjectRestUtils.createBlankProject(goodDataClient, title, authToken, driver, environment);
        log.info("a blank project has been created " + projectId);

        goodDataClient.getModelService().updateProjectModel(goodDataClient.getProjectService()
                .getProjectById(projectId), model).get();
        log.info("LDM has been updated");

        String uploadDir = "fixture_" + RandomStringUtils.randomAlphabetic(6);
        try (InputStream stream = new ByteArrayInputStream(uploadInfo.getBytes())) {
            goodDataClient.getDataStoreService().upload(uploadDir + "/upload_info.json", stream);
        }

        for (int i = 0; i < csvFiles.size(); i++) {
            try (InputStream stream = new ByteArrayInputStream(csvFiles.get(i).getBytes())) {
                String fileName = getCsvFilePaths().get(i).getFileName().toString();
                log.info("Uploading " + fileName + " to user specific storage");
                goodDataClient.getDataStoreService().upload(uploadDir + "/" + fileName, stream);
            }
        }
        log.info("Data has been uploaded to directory named " + uploadDir);

        pullDataToProject(uploadDir);
        log.info("uploaded data has been added to working project");

        List<String> identifiers = getUsedIdentifiers();
        identifiers.addAll(getImportIdentifiers());

        HashMap<String, String> hashMap = new HashMap<>();
        for (String identifier : identifiers) {
            hashMap.put(identifier.replace(".", "_"), getMappedUri(identifier));
        }

        JSONArray jsonArray = new JSONObject(metadata).getJSONArray("objects");

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            hashMap.put(obj.getString("name"),
                    createMdObject(constructMdObject(obj.getJSONObject("content").toString(), hashMap).toString()));
        }
        log.info("MD objects has been created without error");
        log.info("Successfully deploy fixture to project " + projectId);

        return projectId;
    }

    private void pullDataToProject(String dirPath) throws JSONException, IOException {
        String pollingUri = getPollingUri(getJsonObject(restApiClient,
                restApiClient.newPostMethod(
                        format(PULL_DATA_LINK, projectId),
                        new JSONObject().put("pullIntegration", dirPath).toString()),
                HttpStatus.CREATED));

        doPolling(pollingUri, dirPath);
    }

    private String getPollingUri(JSONObject obj) throws JSONException {
        return obj.getJSONObject("pull2Task").getJSONObject("links").getString("poll");
    }

    private void doPolling(String pollingUri, String dirPath) {
        new PollResult<>(goodDataClient.getDatasetService(), new AbstractPollHandler<TaskStatus, Void>(pollingUri,
                TaskStatus.class, Void.class) {
            @Override
            public void handlePollResult(TaskStatus pollResult) {
                if (!pollResult.isSuccess()) {
                    GdcError error = pollResult.getMessages().iterator().next();
                    throw new FixtureException("Unable to pull data to the project"
                            + " [requestId=" + error.getRequestId() + "]"
                            + " [errorMessage=" + error.getFormattedMessage() + "]");
                }

                setResult(null);
            }

            @Override
            public void handlePollException(GoodDataRestException e) {
                throw new FixtureException("Unable to pull data to project", e);
            }

            @Override
            protected void onFinish() {
                goodDataClient.getDataStoreService().delete(dirPath);
            }
        }).get();
    }

    private String createMdObject(String content) throws IOException, JSONException {
        return getUriFromJSONObjectWithoutSearchKey(getJsonObject(restApiClient,
                restApiClient.newPostMethod(format("/gdc/md/%s/obj?createAndGet=true", projectId), content)));
    }

    private String getUriFromJSONObjectWithoutSearchKey(JSONObject obj) throws JSONException {
        String firstKey = (String) obj.keys().next();
        return obj.getJSONObject(firstKey).getJSONObject("meta").getString("uri");
    }

    private List<String> getUsedIdentifiers() throws IOException {
        List<String> identifiers = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\{([\\w.]*)\\}").matcher(model);

        while (matcher.find()) {
            identifiers.add(matcher.group(1));
        }

        if (identifiers.isEmpty()) log.info("There is no identifier found in maql statements");

        return identifiers;
    }

    private List<String> getImportIdentifiers() throws JSONException, IOException {
        List<String> identifiers = new ArrayList<>();
        JSONObject obj = new JSONObject(metadata);

        if (obj.has("import_identifiers")) {
            JSONArray array = obj.getJSONArray("import_identifiers");
            for (int i = 0; i < array.length(); i++) {
                identifiers.add(array.getString(i));
            }
        }

        if (identifiers.isEmpty()) log.info("There is no import identifier found in metadata file");

        return identifiers;
    }

    private String getMappedUri(String identifier) throws JSONException, IOException {
        return getJsonObject(restApiClient,
                restApiClient.newPostMethod(
                        format("/gdc/md/%s/identifiers", projectId),
                        new JSONObject().put("identifierToUri", Collections.singleton(identifier)).toString()))
                .getJSONArray("identifiers").getJSONObject(0).getString("uri");
    }

    private JSONObject constructMdObject(String content, HashMap<String, String> identifierHashMap) throws
            IOException, JSONException {
        MustacheFactory factory = new DefaultMustacheFactory();
        Mustache mustache = factory.compile(new StringReader(content), "tempt_file");

        return new JSONObject(mustache.execute(new StringWriter(), identifierHashMap).toString());
    }

    private String getModelFileContent() {
        return getResourceAsString(fixture.getPath().resolve("model.maql").toString());
    }

    private String getMetadataFileContent() throws IOException {
        return getResourceAsString(fixture.getPath().resolve("metadata.json").toString());
    }

    private String getUploadInfoFileContent() throws IOException {
        return getResourceAsString(fixture.getPath().resolve("upload_info.json").toString());
    }

    private List<String> getCsvFilesContent() throws IOException {
        List<Path> paths = getCsvFilePaths();

        if (!paths.isEmpty()) {
            return paths.stream()
                    .map(file -> ResourceUtils.getResourceAsString(File.separator + file))
                    .collect(Collectors.toList());
        }

        throw new RuntimeException("There are no any csv files");
    }

    private List<Path> getCsvFilePaths() throws IOException {
        List<Path> filePaths = new ArrayList<>();

        try (ZipInputStream stream = new ZipInputStream(getClass()
                .getProtectionDomain().getCodeSource().getLocation().openStream())) {

            ZipEntry entry;

            while ((entry = stream.getNextEntry()) != null) {
                String entryName = entry.getName();
                if (entryName.contains(fixture.getName()) && entryName.endsWith(".csv")) {
                    filePaths.add(Paths.get(entryName));
                }
            }
        }

        if (filePaths.isEmpty()) log.info("There is no csv file");

        return filePaths;
    }

    private void loadFiles() throws IOException {
        this.model = getModelFileContent();
        this.metadata = getMetadataFileContent();
        this.csvFiles = getCsvFilesContent();
        this.uploadInfo = getUploadInfoFileContent();
    }
}
