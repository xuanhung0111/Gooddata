package com.gooddata.qa.graphene.dlui;

import java.util.Collection;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.UserRoles;

public class AnnieDialogReportTest extends AbstractAnnieDialogTest {

    @BeforeClass
    public void initProperties() {
        projectTitle = "Dlui-annie-dialog-report-test";
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = {"annieDialogTest"})
    public void prepareMetricsToCheckReport() throws JSONException {
        prepareMetricToCheckNewAddedFields("age", "price", "totalprice");
    }

    @Test(dataProvider = "newFieldData", dependsOnMethods = {"prepareMetricsToCheckReport"},
            groups = {"annieDialogTest", "addOneField"})
    public void adminCheckReportWithSingleField(AddedFields addedField,
            Collection<String> attributeValues, Collection<String> metricValues)
            throws JSONException, InterruptedException {
        addNewDataAndCheckReport(UserRoles.ADMIN, addedField, attributeValues, metricValues);
    }

    @Test(dependsOnMethods = {"prepareMetricsToCheckReport"}, dependsOnGroups = {"addOneField"},
            groups = "annieDialogTest")
    public void adminCheckReportWithMultiAddedFields() throws JSONException {
        addMultiFieldsAndCheckReport(UserRoles.ADMIN);
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = "annieDialogTest")
    public void addEditorUser() {
        try {
            addUserToProject(testParams.getEditorProfileUri(), UserRoles.EDITOR);
        } catch (Exception e) {
            throw new IllegalStateException("There is exeception when adding user to project!", e);
        }
    }

    @Test(dataProvider = "newFieldData", dependsOnMethods = {"addEditorUser",
        "prepareMetricsToCheckReport"}, groups = {"annieDialogTest", "editorAddOneField"})
    public void editorCheckReportWithSingleField(AddedFields addedField,
            Collection<String> attributeValues, Collection<String> metricValues)
            throws JSONException, InterruptedException {
        addNewDataAndCheckReport(UserRoles.EDITOR, addedField, attributeValues, metricValues);
    }

    @Test(dependsOnMethods = {"addEditorUser", "prepareMetricsToCheckReport"},
            dependsOnGroups = {"editorAddOneField"}, groups = "annieDialogTest")
    public void editorCheckReportWithMultiAddedFields() throws JSONException {
        addMultiFieldsAndCheckReport(UserRoles.EDITOR);
    }

    @Test(dependsOnGroups = "annieDialogTest", alwaysRun = true)
    public void cleanUp() {
        deleteADSInstance(adsInstance);
    }
}
