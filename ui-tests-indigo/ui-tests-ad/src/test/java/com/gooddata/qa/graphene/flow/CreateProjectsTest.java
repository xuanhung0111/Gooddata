package com.gooddata.qa.graphene.flow;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;
import com.google.common.collect.Lists;

public class CreateProjectsTest extends AbstractProjectTest {

    private static final String MAQL_PATH = "/customer/customer.maql";
    private static final String UPLOADINFO_PATH = "/customer/upload_info.json";
    private static final String CSV_PATH = "/customer/customer.csv";

    private List<String> pids = Lists.newArrayList();

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "[E2E] No Date dimension project";
        validateAfterClass = false;
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"setupProjects"})
    public void setupNoDateProject() throws JSONException, IOException, URISyntaxException {
        setupMaql(MAQL_PATH);
        setupData(CSV_PATH, UPLOADINFO_PATH);
        pids.add(testParams.getProjectId());
    }

    @Test(dependsOnMethods = {"setupNoDateProject"}, groups = {"setupProjects"})
    public void setupBlankProject() throws ParseException, JSONException, IOException {
        String pid = ProjectRestUtils.createBlankProject(getGoodDataClient(), "[E2E] Blank project",
                testParams.getAuthorizationToken(), testParams.getProjectDriver(), testParams.getProjectEnvironment());
        pids.add(pid);
    }

    @Test(dependsOnMethods = {"setupBlankProject"}, groups = {"setupProjects"})
    public void setupGoodSalesProject() throws ParseException, JSONException, IOException {
        String pid = ProjectRestUtils.createProject(getGoodDataClient(), "[E2E] GoodSales project", "/projectTemplates/GoodSalesDemo/2",
                testParams.getAuthorizationToken(), testParams.getProjectDriver(), testParams.getProjectEnvironment());
        pids.add(pid);

        testParams.setProjectId(pid);
        String activitiesUri = getMdService().getObjUri(getProject(), Metric.class, title("# of Activities"));
        createMetric("__EMPTY__", "SELECT [" + activitiesUri + "] WHERE 1 = 0", "#,##0");
    }

    @Test(dependsOnGroups = {"setupProjects"})
    public void exportProjectIds() throws IOException {
        File exportPidsFile = getPidsFile();

        try (FileWriter writer = new FileWriter(exportPidsFile)) {
            writer.append("PIDS=")
                 // No Date - Blank - GoodSales
                .append(pids.stream().collect(joining(",")));
        }

        log.info("PIDs file path: " + exportPidsFile.getAbsolutePath());
        log.info("Content: ");
        FileUtils.readLines(exportPidsFile).stream().forEach(log::info);
    }

    @Test(groups = {"teardownProjects", PROJECT_INIT_GROUP})
    public void teardownProjects() throws JSONException {
        for (String pid : testParams.getProjectId().split(",")) {
            ProjectRestUtils.deleteProject(getGoodDataClient(), pid);
        }
        signIn(true, UserRoles.ADMIN);
        initProjectsPage();
        takeScreenshot(browser, "delete-all-[E2E]-projects", getClass());
    }

    private File getPidsFile() {
        return new File(System.getProperty("user.dir"), "pids.txt");
    }
}
