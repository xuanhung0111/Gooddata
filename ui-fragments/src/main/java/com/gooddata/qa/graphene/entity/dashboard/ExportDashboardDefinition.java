package com.gooddata.qa.graphene.entity.dashboard;

import org.json.JSONObject;

import java.util.Date;

public class ExportDashboardDefinition {
    private String projectName;
    private String dashboardName;
    private String tabName;
    private String reportName;
    private String copyright;
    private String pageOfTotal;
    private String remark;
    private Date createDate;

    public String getProjectName() {
        return projectName;
    }

    public ExportDashboardDefinition setProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public String getDashboardName() {
        return dashboardName;
    }

    public ExportDashboardDefinition setDashboardName(String dashboardName) {
        this.dashboardName = dashboardName;
        return this;
    }

    public String getTabName() {
        return tabName;
    }

    public ExportDashboardDefinition setTabName(String tabName) {
        this.tabName = tabName;
        return this;
    }

    public String getReportName() {
        return reportName;
    }

    public ExportDashboardDefinition setReportName(String reportName) {
        this.reportName = reportName;
        return this;
    }

    public String getCopyright() {
        return copyright;
    }

    public ExportDashboardDefinition setCopyright(String copyright) {
        this.copyright = copyright;
        return this;
    }

    public String getPageOfTotal() {
        return pageOfTotal;
    }

    public ExportDashboardDefinition setPageOfTotal(String pageOfTotal) {
        this.pageOfTotal = pageOfTotal;
        return this;
    }

    public String getRemark() {
        return remark;
    }

    public ExportDashboardDefinition setRemark(String remark) {
        this.remark = remark;
        return this;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public ExportDashboardDefinition setCreateDate(Date createDate) {
        this.createDate = createDate;
        return this;
    }

    public void ExportDashboardDefinition() {
        this.projectName = projectName;
        this.dashboardName = dashboardName;
        this.tabName = tabName;
        this.reportName = reportName;
        this.copyright = copyright;
        this.pageOfTotal = pageOfTotal;
        this.remark = remark;
        this.createDate = createDate;
    }

    public JSONObject getJsonConfigurationDashboardToExportPDF() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("pdfexportsettings", new JSONObject() {{
            put("dashboard", new JSONObject() {{
                put("allpages", new JSONObject() {{
                    put("footer", new JSONObject() {{
                        put("left", pageOfTotal);
                        put("right", "");
                    }});
                    put("header", new JSONObject() {{
                        put("left", copyright);
                        put("center", dashboardName);
                        put("right", projectName);
                    }});
                }});
                put("firstpageoverride", new JSONObject() {{
                    put("header", new JSONObject() {{
                        put("left", dashboardName);
                        put("right", remark);
                    }});
                }});
            }});
            put("report", new JSONObject() {{
                put("allpages", new JSONObject() {{
                    put("footer", new JSONObject() {{
                        put("left", pageOfTotal);
                        put("center", "");
                        put("right", reportName);
                    }});
                    put("header", new JSONObject() {{
                        put("left", copyright);
                        put("center", reportName);
                        put("right", projectName);
                    }});
                }});
                put("firstpageoverride", new JSONObject() {{
                    put("header", new JSONObject() {{
                        put("left", reportName);
                        put("right", remark);
                    }});
                }});
            }});
        }});
        return jsonObject;
    }
}
