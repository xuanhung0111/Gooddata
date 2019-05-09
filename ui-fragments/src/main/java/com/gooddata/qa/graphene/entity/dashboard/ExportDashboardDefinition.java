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

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDashboardName() {
        return dashboardName;
    }

    public void setDashboardName(String dashboardName) {
        this.dashboardName = dashboardName;
    }

    public String getTabName() {
        return tabName;
    }

    public void setTabName(String tabName) {
        this.tabName = tabName;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getPageOfTotal() {
        return pageOfTotal;
    }

    public void setPageOfTotal(String pageOfTotal) {
        this.pageOfTotal = pageOfTotal;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
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
