package com.gooddata.qa.graphene.dlui;

import org.json.JSONException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.user.UserRoles;

public class AnnieDialogReportTest extends AbstractAnnieDialogTest {

    @BeforeClass
    public void initProperties() {
        projectTitle = "Dlui-annie-dialog-report-test";
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = {"basicTest"})
    public void prepareMetricsToCheckReport() throws JSONException {
        prepareMetricToCheckNewAddedFields("age", "price", "totalprice");
    }

    @DataProvider(name = "basicFieldData")
    public Object[][] basicFieldData() {
        return new Object[][] {
            {AddedFields.POSITION, ReportWithAddedFields.POSITION},
            {AddedFields.TOTALPRICE2, ReportWithAddedFields.TOTALPRICE2}};
    }

    @DataProvider(name = "newFieldData")
    public Object[][] newFieldData() {
        return new Object[][] {
            {AddedFields.LABEL, ReportWithAddedFields.LABEL},
            {AddedFields.DATE, ReportWithAddedFields.DATE},
            {AddedFields.POSITION_CONNECTION_POINT, ReportWithAddedFields.POSITION_CONNECTION_POINT},
            {AddedFields.LABEL_OF_NEW_FIELD, ReportWithAddedFields.LABEL_OF_NEW_FIELD}};
    }

    @Test(dataProvider = "basicFieldData", dependsOnMethods = {"prepareMetricsToCheckReport"},
            groups = {"addOneField", "basicTest"})
    public void adminCheckReportWithBasicSingleField(AddedFields addedField,
            ReportWithAddedFields reportWithAddedFields) throws JSONException {
        checkNewAddedDataReportAndCleanAddedData(UserRoles.ADMIN, addedField, reportWithAddedFields);
    }

    @Test(dataProvider = "newFieldData", dependsOnMethods = {"prepareMetricsToCheckReport"},
            groups = {"addOneField"})
    public void adminCheckReportWithSingleField(AddedFields addedField, ReportWithAddedFields reportWithAddedFields)
            throws JSONException {
        checkNewAddedDataReportAndCleanAddedData(UserRoles.ADMIN, addedField, reportWithAddedFields);
    }

    @Test(dependsOnMethods = {"prepareMetricsToCheckReport"}, dependsOnGroups = {"addOneField"},
            groups = "annieDialogTest")
    public void adminCheckReportWithMultiAddedFields() throws JSONException {
        addMultiFieldsAndCheckReport(UserRoles.ADMIN);
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = {"basicTest"})
    public void addEditorUser() {
        try {
            addUserToProject(testParams.getEditorUser(), UserRoles.EDITOR);
        } catch (Exception e) {
            throw new IllegalStateException("There is exeception when adding user to project!", e);
        }
    }

    @Test(dataProvider = "basicFieldData", dependsOnMethods = {"addEditorUser", "prepareMetricsToCheckReport"},
            groups = {"editorAddOneField", "basicTest"})
    public void editorCheckReportWithBasicSingleField(AddedFields addedField,
            ReportWithAddedFields reportWithAddedFields) throws JSONException {
        checkNewAddedDataReportAndCleanAddedData(UserRoles.EDITOR, addedField, reportWithAddedFields);
    }

    @Test(dataProvider = "newFieldData", dependsOnMethods = {"addEditorUser", "prepareMetricsToCheckReport"},
            groups = {"editorAddOneField"})
    public void editorCheckReportWithSingleField(AddedFields addedField,
            ReportWithAddedFields reportWithAddedFields) throws JSONException {
        checkNewAddedDataReportAndCleanAddedData(UserRoles.EDITOR, addedField, reportWithAddedFields);
    }

    @Test(dependsOnMethods = {"addEditorUser", "prepareMetricsToCheckReport"},
            dependsOnGroups = {"editorAddOneField"})
    public void editorCheckReportWithMultiAddedFields() throws JSONException {
        addMultiFieldsAndCheckReport(UserRoles.EDITOR);
    }

    @AfterClass
    public void cleanUp() {
        deleteADSInstance(ads);
    }
}
