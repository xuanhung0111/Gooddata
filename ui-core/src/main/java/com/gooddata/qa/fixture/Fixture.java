package com.gooddata.qa.fixture;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.gooddata.AbstractPollHandler;
import com.gooddata.GoodDataRestException;
import com.gooddata.PollResult;
import com.gooddata.fixture.ResourceManagement;
import com.gooddata.fixture.ResourceManagement.ResourceTemplate;
import com.gooddata.gdc.GdcError;
import com.gooddata.gdc.TaskStatus;
import com.gooddata.project.Environment;
import com.gooddata.project.Project;
import com.gooddata.project.ProjectDriver;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestRequest;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.isNull;

public class Fixture {

    private ResourceTemplate fixture;
    private int version;

    private String projectId;
    private RestClient restClient;

    private static final String PULL_DATA_LINK = "/gdc/md/%s/etl/pull2";
    private static final int FIXTURE_DEFAULT_VERSION = 1;

    private final Logger log = Logger.getLogger(this.getClass().getName());
    private final ResourceManagement resourceManagement = new ResourceManagement();

    public Fixture(ResourceTemplate fixture) {
        this.fixture = fixture;
        version = FIXTURE_DEFAULT_VERSION;
    }

    public Fixture setRestClient(RestClient restClient) {
        this.restClient = restClient;
        return this;
    }

    public String deploy(String title, String authToken, ProjectDriver driver, Environment environment) {
        log.info("Deploying fixture: " + fixture.getPath().toUpperCase());

        if (Objects.isNull(restClient)) {
            throw new RuntimeException("Rest api or Goodata client has not been initialized !");
        }

        projectId = createBlankProject(restClient, title, authToken, driver, environment);
        log.info("a blank project has been created " + projectId);

        Project workingProject = restClient.getProjectService().getProjectById(projectId);

        try {
            log.info("updating LDM");
            try (InputStream stream = resourceManagement.getModelFileContent(fixture, version)) {
                restClient.getModelService()
                        .updateProjectModel(workingProject, getInputStreamAsString(stream)).get();
            }

            String uploadDir = "fixture_" + RandomStringUtils.randomAlphabetic(6);
            log.info("uploading data files to staging area named " + uploadDir);
            try (InputStream stream = resourceManagement.getUploadInfoFileContent(fixture, version)) {
                restClient.getDataStoreService().upload(uploadDir + "/upload_info.json", stream);
            }

            for (String entry : resourceManagement.getCsvEntryNames(fixture, version)) {
                try (InputStream stream = resourceManagement.getFileContent(entry)) {
                    restClient.getDataStoreService().upload(uploadDir + "/" + getFileName(entry), stream);
                }
            }

            log.info("pulling data to working project");
            pullDataToProject(uploadDir);

            Map<String, String> map = restClient.getMetadataService()
                    .identifiersToUris(workingProject, Stream.of(getUsedIdentifiers(), getImportIdentifiers())
                            .flatMap(List::stream)
                            .collect(Collectors.toList()))
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            entry -> entry.getKey().replace(".", "_"),
                            Map.Entry::getValue));

            JSONArray rawMdObjects = new JSONObject(getInputStreamAsString(
                    resourceManagement.getMetadataFileContent(fixture, version))).getJSONArray("objects");

            for (int i = 0; i < rawMdObjects.length(); i++) {
                JSONObject obj = rawMdObjects.getJSONObject(i);
                // there are some cases which refer to created obj uri
                // updating map object after iteration is necessary
                map.put(obj.getString("name"), createMdObject(
                        constructMdObject(obj.getJSONObject("content").toString(), map).toString()));
            }
        } catch (IOException | JSONException e) {
            throw new FixtureException("There is an error during deploying fixture process", e);
        }

        return projectId;
    }

    private void pullDataToProject(String dirPath) throws JSONException, IOException {
        String pollingUri = getPollingUri(getJsonObject(
                RestRequest.initPostRequest(
                        format(PULL_DATA_LINK, projectId),
                        new JSONObject().put("pullIntegration", dirPath).toString()),
                HttpStatus.CREATED));

        doPolling(pollingUri, dirPath);
    }

    private String getPollingUri(JSONObject obj) throws JSONException {
        return obj.getJSONObject("pull2Task").getJSONObject("links").getString("poll");
    }

    private void doPolling(String pollingUri, String dirPath) {
        new PollResult<>(restClient.getDatasetService(), new AbstractPollHandler<TaskStatus, Void>(pollingUri,
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
                restClient.getDataStoreService().delete(dirPath);
            }
        }).get();
    }

    private String createMdObject(String content) throws IOException, JSONException {
        return getUriFromJSONObjectWithoutSearchKey(getJsonObject(
                RestRequest.initPostRequest(format("/gdc/md/%s/obj?createAndGet=true", projectId), content)));
    }

    private String getUriFromJSONObjectWithoutSearchKey(JSONObject obj) throws JSONException {
        String firstKey = (String) obj.keys().next();
        return obj.getJSONObject(firstKey).getJSONObject("meta").getString("uri");
    }

    private List<String> getUsedIdentifiers() throws IOException {
        List<String> identifiers = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\{([\\w.]*)\\}").matcher(
                getInputStreamAsString(resourceManagement.getModelFileContent(fixture, version)));

        while (matcher.find()) {
            identifiers.add(matcher.group(1));
        }

        if (identifiers.isEmpty()) log.info("There is no identifier found in maql statements");

        return identifiers;
    }

    private List<String> getImportIdentifiers() throws JSONException, IOException {
        List<String> identifiers = new ArrayList<>();
        JSONObject obj = new JSONObject(
                getInputStreamAsString(resourceManagement.getMetadataFileContent(fixture, version)));

        if (obj.has("import_identifiers")) {
            JSONArray array = obj.getJSONArray("import_identifiers");
            for (int i = 0; i < array.length(); i++) {
                identifiers.add(array.getString(i));
            }
        }

        if (identifiers.isEmpty()) {
            log.info("There is no import identifier found in metadata file");
        }

        return identifiers;
    }

    private JSONObject constructMdObject(String content, Map<String, String> identifierMap) throws
            IOException, JSONException {
        MustacheFactory factory = new DefaultMustacheFactory();
        Mustache mustache = factory.compile(new StringReader(content), "tempt_file");

        return new JSONObject(mustache.execute(new StringWriter(), identifierMap).toString());
    }

    private String getFileName(String entryName) {
        return Stream.of(entryName.split(File.separator))
                .reduce((s1, s2) -> s2).orElseThrow(() -> new RuntimeException("Can't parse entry name"));

    }

    private String getInputStreamAsString(InputStream stream) throws IOException {
        try (InputStream workingStream = stream) {
            return IOUtils.readLines(workingStream, "UTF-8").stream().collect(Collectors.joining());
        }
    }

    /**
     * Create project with specific template
     *
     * @param restClient
     * @param title
     * @param authorizationToken
     * @param projectDriver
     * @param environment
     * @return project id
     */
    private String createBlankProject(final RestClient restClient, final String title, final String authorizationToken,
                                      final ProjectDriver projectDriver, final Environment environment) {
        final Project project = new Project(title, authorizationToken);
        project.setDriver(projectDriver);
        project.setEnvironment(environment);

        return restClient.getProjectService().createProject(project).get().getId();
    }

    /**
     * Get json object from request with expected status code
     *
     * @param request
     * @param expectedStatusCode
     * @return
     */
    private JSONObject getJsonObject(final HttpRequestBase request, final HttpStatus expectedStatusCode)
            throws ParseException, JSONException, IOException {
        return new JSONObject(getResource(request, expectedStatusCode));
    }

    /**
     * Get json object from request, expected status code is OK (200)
     *
     * @param request
     * @return
     */
    private JSONObject getJsonObject(final HttpRequestBase request)
            throws ParseException, JSONException, IOException {
        return new JSONObject(getResource(request, HttpStatus.OK));
    }

    /**
     * Get resource from request with expected status code
     *
     * @param request
     * @param setupRequest        setup request before executing like configure header, ...
     * @param expectedStatusCode
     * @return entity from response in String form
     */
    private String getResource(final HttpRequestBase request, final Consumer<HttpRequestBase> setupRequest,
                               final HttpStatus expectedStatusCode)
            throws ParseException, IOException {
        setupRequest.accept(request);

        try {
            final HttpResponse response = restClient.execute(request, expectedStatusCode);
            final HttpEntity entity = response.getEntity();

            final String ret = isNull(entity) ? "" : EntityUtils.toString(entity);
            EntityUtils.consumeQuietly(entity);
            return ret;

        } finally {
            request.releaseConnection();
        }
    }

    /**
     * Get resource from request with expected status code
     *
     * @param request
     * @param expectedStatusCode
     * @return entity from response in json form
     */
    private String getResource(final HttpRequestBase request,
                               final HttpStatus expectedStatusCode) throws ParseException, IOException {
        return getResource(
                request,
                req -> req.setHeader("Accept", ContentType.APPLICATION_JSON.getMimeType()),
                expectedStatusCode);
    }
}
