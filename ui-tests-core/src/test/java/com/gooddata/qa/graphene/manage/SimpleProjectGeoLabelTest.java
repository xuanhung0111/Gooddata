package com.gooddata.qa.graphene.manage;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForDashboardPageLoaded;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.Arrays.asList;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.AttributeLabelTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.upload.UploadColumns.OptionDataType;

public class SimpleProjectGeoLabelTest extends AbstractProjectTest {

    private List<String> attributesList;
    private String csvFilePath;

    @BeforeClass
    public void setProjectTitle() {
        csvFilePath = testParams.loadProperty("csvFilePath") + testParams.getFolderSeparator();
        projectTitle = "SimpleProject-test-geos-labels";
    }

    @Test(dependsOnMethods = {"createProject"})
    public void initialize() throws InterruptedException, JSONException {
        attributesList =
                asList("Geo_pushpin", "AUS_State_Name", "AUS_State_ISO", "StateName", "StateID",
                        "StateCode", "CountyID", "CountryName", "Country_ISO2", "Country_ISO3",
                        "CZ_District_Name", "CZ_District_NO_Diacritics", "CZ_District_NUTS4",
                        "CZ_District_KNOK");
        Map<Integer, OptionDataType> columnIndexAndType = new HashMap<Integer, OptionDataType>();
        columnIndexAndType.put(9, OptionDataType.TEXT);
        uploadCSV(csvFilePath + "attribute_geo_labels.csv", columnIndexAndType,
                "attribute_geo_labels");
    }

    @Test(dependsOnMethods = {"initialize"})
    public void changeAttributeToGeoStateTest() throws InterruptedException {
        int i = 0;
        for (AttributeLabelTypes type : getGeoLabels()) {
            openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|attributes");
            attributePage.configureAttributeLabel(attributesList.get(i), type);
            i++;
        }
    }

    @Test(dependsOnMethods = {"initialize"})
    public void verifyGeoLayersTest() throws InterruptedException {
        initDashboardsPage();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardsPage.editDashboard();
        dashboardsPage.addNewTab("tab");
        dashboardEditBar.verifyGeoLayersList("Sum of Amount", attributesList);
        dashboardEditBar.saveDashboard();
        dashboardsPage.deleteDashboardTab(1);
    }

    @Test(dependsOnMethods = {"initialize"})
    public void verifyGeoChartTest() throws InterruptedException {
        for (GeoAttributeLabels attributeLayer : GeoAttributeLabels.values()) {
            System.out.println("Verifying attribute " + attributeLayer + " ...");
            initDashboardsPage();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.editDashboard();
            dashboardsPage.addNewTab("tab");
            dashboardEditBar.addGeoChart(attributeLayer.metricName, attributeLayer.name);
            dashboardEditBar.saveDashboard();
            waitForDashboardPageLoaded(browser);
            dashboardsPage
                    .getContent()
                    .getGeoChart(0)
                    .verifyGeoChart(attributeLayer.name, attributeLayer.metricName,
                            attributeLayer.startMetricValue, attributeLayer.stopMetricValue,
                            attributeLayer.indexList, attributeLayer.colorList,
                            attributeLayer.svgDataList, attributeLayer.metricValues,
                            attributeLayer.attrValues);
            dashboardsPage.deleteDashboardTab(1);
        }
    }

    private List<AttributeLabelTypes> getGeoLabels() {
        List<AttributeLabelTypes> list = new ArrayList<AttributeLabelTypes>();
        for (AttributeLabelTypes label : AttributeLabelTypes.values()) {
            if (label.isGeoLabel()) {
                list.add(label);
            }
        }
        return list;
    }

    private enum GeoAttributeLabels {
        GEO_PUSHPIN("Geo_pushpin", "Sum of Amount", 10225f, 102303f, asList(0, 1, 2, 3, 4, 5, 6, 7,
                8), asList("rgb(230, 230, 230)", "rgb(43, 107, 174)", "rgb(230, 230, 230)",
                "rgb(43, 107, 174)", "rgb(43, 107, 174)", "rgb(43, 107, 174)",
                "rgb(230, 230, 230)", "rgb(230, 230, 230)", "rgb(230, 230, 230)"), asList(
                "M161,102A4,4,0,1,1,160.9,102 z", "M68,-5A4,4,0,1,1,67.9,-5 z",
                "M98,-14A4,4,0,1,1,97.9,-14 z", "M89,-14A4,4,0,1,1,88.9,-14 z",
                "M98,-23A4,4,0,1,1,97.9,-23 z", "M105,-29A4,4,0,1,1,104.9,-29 z",
                "M-162,-84A4,4,0,1,1,-162.1,-84 z", "M107,-91A4,4,0,1,1,106.9,-91 z",
                "M-158,-110A4,4,0,1,1,-158.1,-110 z"), asList("10,230.00", "102,303.00",
                "10,237.00", "102,300.00", "102,302.00", "102,301.00", "10,239.00", "10,236.00",
                "10,225.00"), asList("31.190020;-85.399150", "33.179071;-87.447521",
                "33.334175;-86.784966", "33.335554;-86.994991", "33.507590;-86.798513",
                "33.607361;-86.630355", "34.616719;-92.498309", "34.743001;-86.601108",
                "35.091612;-92.427588")),
        AUS_STATE_NAME(
                "AUS_State_Name",
                "Sum of Amount",
                102300f,
                112526f,
                asList(50, 65, 69),
                asList("rgb(43, 107, 174)", "rgb(230, 230, 230)", "rgb(230, 230, 230)"),
                asList("M241 126L241 137L242 136L244 138L244 141L237 142L236 145L233 146L232 148L232 146L229 144L222 144L219 145L218 147L173 147L173 127L156 127L156 69L166 76L170 76L173 74L177 60L176 49L178 47L178 43L179 44L179 38L182 35L188 51L188 55L190 57L193 55L194 58L197 60L197 64L199 67L198 68L201 72L204 85L210 87L209 86L213 90L218 92L217 95L220 96L222 104L225 106L225 103L225 105L229 106L229 111L239 121L241 126z",
                        "M211 244L210 245L209 243L209 248L207 246L208 248L206 250L203 250L201 248L203 248L201 248L197 241L197 239L199 242L198 241L200 241L194 233L194 227L204 232L207 231L208 234L206 231L213 228L215 230L215 239L213 239L213 243L211 244z",
                        "M173 208L173 180L180 181L182 185L186 185L186 187L195 195L199 192L201 194L204 193L207 195L210 193L213 194L214 198L224 204L222 206L210 209L207 213L203 213L204 216L202 214L201 215L198 211L199 210L195 212L194 210L195 211L197 209L195 207L188 214L178 210L175 211L173 208zM206 213L207 212L206 213z"),
                asList("112,526.00", "102,303.00", "102,300.00"),
                asList("Queensland", "Tasmania", "Victoria")),
        AUS_STATE_ISO(
                "AUS_State_ISO",
                "Sum of Amount",
                10230f,
                112526f,
                asList(2, 39, 90, 97, 112, 116, 156),
                asList("rgb(230, 230, 230)", "rgb(230, 230, 230)", "rgb(43, 107, 174)",
                        "rgb(230, 230, 230)", "rgb(62, 119, 180)", "rgb(62, 119, 180)",
                        "rgb(62, 119, 180)"),
                asList("M283 188L280 193L279 204L269 199L266 193L262 195L259 193L256 194L254 192L249 195L241 187L241 185L238 185L234 181L230 181L228 147L273 147L277 144L283 144L287 146L287 148L291 146L290 143L299 141L300 146L298 148L296 158L297 159L294 169L290 171L292 171L289 174L288 173L289 174L288 173L289 174L287 177L286 176L287 179L285 179L287 179L284 183L283 188zM285 180L285 180zM287 176L287 176zM289 172L289 172zM273 192L274 193L274 188L272 190L273 192z",
                        "M160 60L161 61L161 59L164 61L162 57L167 51L166 48L170 47L170 45L171 47L170 46L173 43L177 45L181 43L181 41L176 38L181 40L182 39L185 42L193 42L193 44L199 42L198 44L200 43L199 44L201 46L201 43L203 42L202 43L205 44L202 48L203 49L202 48L203 49L199 51L200 54L196 59L203 65L204 64L204 66L205 65L211 69L211 127L160 127L160 60z",
                        "M296 126L296 137L297 136L299 138L299 141L292 142L291 145L288 146L287 148L287 146L284 144L277 144L274 145L273 147L228 147L228 127L211 127L211 69L221 76L225 76L228 74L232 60L231 49L233 47L233 43L234 44L234 38L237 35L243 51L243 55L245 57L248 55L249 58L252 60L252 64L254 67L253 68L256 72L259 85L265 87L264 86L268 90L273 92L272 95L275 96L277 104L280 106L280 103L280 105L284 106L284 111L294 121L296 126z",
                        "M160 164L160 127L228 127L228 208L226 208L222 204L222 200L219 193L215 190L212 191L214 189L214 185L211 181L210 187L205 188L205 186L207 186L208 184L211 174L210 169L210 173L208 174L207 178L202 180L199 185L200 187L195 183L196 184L193 175L189 173L190 170L187 170L188 169L186 167L178 167L176 164L172 163L160 164z",
                        "M266 244L265 245L264 243L264 248L262 246L263 248L261 250L258 250L256 248L258 248L256 248L252 241L252 239L254 242L253 241L255 241L249 233L249 227L259 232L262 231L263 234L261 231L268 228L270 230L270 239L268 239L268 243L266 244z",
                        "M228 208L228 180L235 181L237 185L241 185L241 187L250 195L254 192L256 194L259 193L262 195L265 193L268 194L269 198L279 204L277 206L265 209L262 213L258 213L259 216L257 214L256 215L253 211L254 210L250 212L249 210L250 211L252 209L250 207L243 214L233 210L230 211L228 208zM261 213L262 212L261 213z",
                        "M160 60L160 164L137 171L132 174L131 177L127 180L109 179L106 181L106 183L105 182L104 184L103 183L98 187L97 186L97 188L89 187L84 183L80 182L80 177L84 175L85 166L84 167L80 155L80 148L75 139L76 138L70 129L71 127L73 132L74 130L71 126L72 124L73 128L74 127L74 129L76 129L71 118L74 108L74 102L75 101L76 106L80 100L82 100L91 93L90 94L93 95L97 94L98 92L110 90L115 87L118 84L118 82L122 79L121 74L126 68L125 70L127 70L126 71L129 75L129 73L131 73L131 71L130 72L128 69L130 69L130 67L136 69L134 67L136 65L134 66L134 64L136 62L140 64L136 61L140 61L138 59L140 57L141 58L141 56L141 58L143 58L143 54L145 56L144 55L146 54L146 56L148 54L147 53L149 53L155 59L155 63L157 59L160 60z"),
                asList("10,236.00", "10,239.00", "112,526.00", "10,237.00", "102,303.00",
                        "102,300.00", "102,302.00"),
                asList("New South Wales", "Northern Territory", "Queensland", "South Australia",
                        "Tasmania", "Victoria", "Western Australia")),
        US_STATE_NAME(
                "StateName",
                "Sum of Amount",
                102301f,
                255769f,
                asList(56),
                asList("rgb(43, 107, 174)"),
                asList("M64 51L63 50L64 51z"),
                asList("255,769.00"),
                asList("Washington")),
        US_STATE_CENSUS_ID(
                "StateID",
                "Sum of Amount",
                102300f,
                102303f,
                asList(10, 11, 17, 18),
                asList("rgb(230, 230, 230)", "rgb(168, 189, 211)", "rgb(105, 148, 193)",
                        "rgb(43, 107, 174)"),
                asList("M-125 -59L-125 -67L-126 -66L-125 -67L-127 -69L-126 -72L-127 -71L-128 -73L-127 -74L-95 -74L-94 -48L-110 -48L-118 -45L-126 -45L-129 -50L-135 -51L-134 -56L-136 -58L-139 -68L-134 -66L-129 -67L-130 -66L-128 -66L-127 -63L-126 -64L-127 -65L-125 -59z",
                        "M124 5L119 10L117 10L114 17L106 19L105 16L103 16L101 13L101 11L104 8L103 7L105 7L106 4L111 1L112 -6L113 -1L118 0L119 4L123 1L127 1L129 3L128 5L125 2L124 5z",
                        "M72 -40L70 -38L71 -37L76 -42L73 -37L71 -27L72 -22L55 -21L52 -24L53 -27L51 -32L43 -38L43 -47L46 -50L47 -55L50 -54L54 -56L54 -54L59 -51L69 -48L72 -45L72 -40z",
                        "M-21 -32L-21 -9L-61 -9L-61 -40L-21 -40L-21 -32z"),
                asList("102,300.00", "102,301.00", "102,302.00", "102,303.00"),
                asList("Washington", "West Virginia", "Wisconsin", "Wyoming")),
        US_STATE_CODE(
                "StateCode",
                "Sum of Amount",
                102301f,
                255769f,
                asList(1, 12),
                asList("rgb(230, 230, 230)", "rgb(43, 107, 174)"),
                asList("M77 34L84 35L86 49L88 53L87 60L74 61L74 65L71 66L70 63L69 66L69 34L77 34z",
                        "M-125 -59L-125 -67L-126 -66L-125 -67L-127 -69L-126 -72L-127 -71L-128 -73L-127 -74L-95 -74L-94 -48L-110 -48L-118 -45L-126 -45L-129 -50L-135 -51L-134 -56L-136 -58L-139 -68L-134 -66L-129 -67L-130 -66L-128 -66L-127 -63L-126 -64L-127 -65L-125 -59z"),
                asList("102,301.00", "255,769.00"),
                asList("Alabama", "Washington")),
        US_COUNTY_CENSUS_ID(
                "CountyID",
                "Sum of Amount",
                10225f,
                225081f,
                asList(0, 1, 2, 3, 4),
                asList("rgb(43, 107, 174)", "rgb(230, 230, 230)", "rgb(230, 230, 230)",
                        "rgb(61, 119, 179)", "rgb(230, 230, 230)"),
                asList("M-51 84L-55 78L-56 62L-51 62L-44 69L-44 84L-51 84z",
                        "M28 44L29 40L38 41L40 38L55 38L55 43L51 43L50 50L46 51L46 54L31 54L31 57L24 57L24 48L28 48L28 44z",
                        "M-18 30L-22 33L-24 31L-26 35L-29 37L-27 42L-31 48L-47 48L-47 51L-49 51L-49 27L-47 19L-44 17L-15 18L-16 28L-18 30z",
                        "M60 -75L60 -80L73 -80L73 -83L78 -83L78 -86L95 -86L95 -83L98 -83L98 -80L102 -80L102 -66L96 -66L95 -60L88 -56L77 -56L68 -62L70 -65L60 -65L60 -75z",
                        "M-72 46L-78 46L-78 43L-81 42L-90 42L-95 48L-101 48L-101 45L-99 44L-97 37L-99 35L-102 35L-102 33L-97 32L-96 25L-93 22L-89 24L-87 22L-85 24L-80 24L-80 21L-62 21L-64 23L-66 31L-69 33L-72 40L-72 46z"),
                asList("225,081.00", "10,225.00", "10,236.00", "204,601.00", "10,230.00"),
                asList("Ada", "Clark", "Valley", "Chouteau", "Baker")),
        WORLD_COUNTRIES_NAME(
                "CountryName",
                "Sum of Amount",
                30712f,
                214835f,
                asList(0, 6, 10, 11),
                asList("rgb(43, 107, 174)", "rgb(43, 107, 174)", "rgb(43, 107, 174)",
                        "rgb(230, 230, 230)"),
                asList("M68 -16L64 -16L61 -20L64 -22L69 -20L71 -18L68 -16z",
                        "M-91 -18L-90 -16L-86 -15L-82 -16L-75 -10L-73 -10L-74 -2L-68 -5L-69 -6L-65 -6L-62 -9L-56 -10L-54 -14L-52 -13L-51 -9L-57 -5L-57 -2L-60 -1L-58 -1L-62 0L-61 1L-64 6L-65 2L-64 9L-72 16L-70 23L-71 26L-75 18L-82 18L-84 20L-91 19L-95 25L-100 19L-103 20L-107 15L-114 16L-123 14L-125 11L-128 10L-132 3L-133 -16L-130 -13L-131 -17L-91 -18z",
                        "M-177 -83L-176 -81L-172 -82L-173 -81L-169 -81L-162 -78L-160 -79L-157 -77L-157 -45L-155 -45L-151 -41L-149 -44L-141 -33L-142 -31L-144 -32L-144 -35L-147 -39L-152 -40L-156 -44L-161 -44L-165 -47L-167 -46L-167 -44L-172 -42L-170 -47L-175 -42L-175 -39L-183 -32L-184 -33L-190 -30L-184 -34L-179 -41L-184 -40L-184 -42L-185 -41L-187 -44L-191 -46L-191 -52L-190 -54L-188 -53L-185 -56L-185 -59L-191 -58L-195 -62L-190 -65L-189 -63L-186 -64L-193 -72L-188 -76L-186 -80L-179 -84L-177 -83z",
                        "M198 32L194 35L199 41L199 46L194 51L192 48L197 45L197 41L189 30L194 29L198 32z"),
                asList("214,826.00", "214,835.00", "214,835.00", "214,835.00"),
                asList("Czech Republic", "United States of America", "United States of America",
                        "United States of America")),
        WORLD_COUNTRIES_ISO2(
                "Country_ISO2",
                "Sum of Amount",
                30712f,
                214835f,
                asList(0, 6, 10, 11),
                asList("rgb(43, 107, 174)", "rgb(43, 107, 174)", "rgb(43, 107, 174)",
                        "rgb(230, 230, 230)"),
                asList("M68 -16L64 -16L61 -20L64 -22L69 -20L71 -18L68 -16z",
                        "M-91 -18L-90 -16L-86 -15L-82 -16L-75 -10L-73 -10L-74 -2L-68 -5L-69 -6L-65 -6L-62 -9L-56 -10L-54 -14L-52 -13L-51 -9L-57 -5L-57 -2L-60 -1L-58 -1L-62 0L-61 1L-64 6L-65 2L-64 9L-72 16L-70 23L-71 26L-75 18L-82 18L-84 20L-91 19L-95 25L-100 19L-103 20L-107 15L-114 16L-123 14L-125 11L-128 10L-132 3L-133 -16L-130 -13L-131 -17L-91 -18z",
                        "M-177 -83L-176 -81L-172 -82L-173 -81L-169 -81L-162 -78L-160 -79L-157 -77L-157 -45L-155 -45L-151 -41L-149 -44L-141 -33L-142 -31L-144 -32L-144 -35L-147 -39L-152 -40L-156 -44L-161 -44L-165 -47L-167 -46L-167 -44L-172 -42L-170 -47L-175 -42L-175 -39L-183 -32L-184 -33L-190 -30L-184 -34L-179 -41L-184 -40L-184 -42L-185 -41L-187 -44L-191 -46L-191 -52L-190 -54L-188 -53L-185 -56L-185 -59L-191 -58L-195 -62L-190 -65L-189 -63L-186 -64L-193 -72L-188 -76L-186 -80L-179 -84L-177 -83z",
                        "M198 32L194 35L199 41L199 46L194 51L192 48L197 45L197 41L189 30L194 29L198 32z"),
                asList("214,826.00", "214,835.00", "214,835.00", "214,835.00"),
                asList("Czech Republic", "United States", "United States", "United States")),
        WORLD_COUNTRIES_ISO3(
                "Country_ISO2",
                "Sum of Amount",
                30712f,
                214835f,
                asList(0, 6, 10, 11),
                asList("rgb(43, 107, 174)", "rgb(43, 107, 174)", "rgb(43, 107, 174)",
                        "rgb(230, 230, 230)"),
                asList("M68 -16L64 -16L61 -20L64 -22L69 -20L71 -18L68 -16z",
                        "M-91 -18L-90 -16L-86 -15L-82 -16L-75 -10L-73 -10L-74 -2L-68 -5L-69 -6L-65 -6L-62 -9L-56 -10L-54 -14L-52 -13L-51 -9L-57 -5L-57 -2L-60 -1L-58 -1L-62 0L-61 1L-64 6L-65 2L-64 9L-72 16L-70 23L-71 26L-75 18L-82 18L-84 20L-91 19L-95 25L-100 19L-103 20L-107 15L-114 16L-123 14L-125 11L-128 10L-132 3L-133 -16L-130 -13L-131 -17L-91 -18z",
                        "M-177 -83L-176 -81L-172 -82L-173 -81L-169 -81L-162 -78L-160 -79L-157 -77L-157 -45L-155 -45L-151 -41L-149 -44L-141 -33L-142 -31L-144 -32L-144 -35L-147 -39L-152 -40L-156 -44L-161 -44L-165 -47L-167 -46L-167 -44L-172 -42L-170 -47L-175 -42L-175 -39L-183 -32L-184 -33L-190 -30L-184 -34L-179 -41L-184 -40L-184 -42L-185 -41L-187 -44L-191 -46L-191 -52L-190 -54L-188 -53L-185 -56L-185 -59L-191 -58L-195 -62L-190 -65L-189 -63L-186 -64L-193 -72L-188 -76L-186 -80L-179 -84L-177 -83z",
                        "M198 32L194 35L199 41L199 46L194 51L192 48L197 45L197 41L189 30L194 29L198 32z"),
                asList("214,826.00", "214,835.00", "214,835.00", "214,835.00"),
                asList("Czech Republic", "United States", "United States", "United States")),
        CZ_DISTRICT_NAME(
                "CZ_District_Name",
                "Sum of Amount",
                30712f,
                214835f,
                asList(0, 1, 2),
                asList("rgb(43, 107, 174)", "rgb(43, 107, 174)", "rgb(230, 230, 230)"),
                asList("M83 149L82 139L102 140L102 134L108 126L106 123L114 126L116 119L123 113L140 113L145 122L152 123L150 127L158 126L158 131L164 134L161 139L166 145L159 148L152 166L145 166L127 191L124 186L120 188L113 177L110 182L105 180L95 185L96 193L84 197L78 196L83 181L81 172L83 165L88 166L85 154L87 149L83 149z",
                        "M262 95L261 91L269 92L258 70L260 59L267 58L269 52L297 59L299 42L308 41L312 31L318 33L323 29L334 32L342 47L348 44L355 48L364 43L369 50L367 64L373 69L365 81L368 84L363 87L366 87L364 93L352 92L344 100L333 97L325 109L315 101L314 94L301 95L296 106L293 101L286 102L290 97L288 93L267 102L261 99L264 97L262 95z",
                        "M26 204L28 197L34 193L28 189L26 181L32 179L29 176L30 160L36 154L32 153L38 148L45 152L43 144L52 148L56 141L72 138L71 145L87 149L85 154L88 166L83 165L81 172L83 181L78 196L84 197L75 208L71 204L63 208L58 217L68 219L61 222L66 228L58 228L52 226L51 231L49 227L41 227L44 216L34 215L32 210L37 208L26 204z"),
                asList("214,826.00", "214,835.00", "30,712.00"),
                asList("Beroun", "Nymburk", "Rokycany")),
        CZ_DISTRICT_NAME_WO_DIAC(
                "CZ_District_NO_Diacritics",
                "Sum of Amount",
                30701f,
                306903f,
                asList(0, 1, 2),
                asList("rgb(43, 107, 174)", "rgb(168, 189, 211)", "rgb(230, 230, 230)"),
                asList("M247 56L248 52L255 49L255 47L259 47L258 43L261 41L272 43L276 41L286 44L287 46L282 58L286 60L280 66L278 76L275 77L274 82L271 83L262 83L259 86L257 83L253 83L254 77L258 73L254 70L253 66L256 64L255 62L250 56L248 56L250 55L247 56z",
                        "M237 174L240 170L238 167L238 163L236 163L238 159L243 157L245 159L247 157L256 160L257 163L261 165L260 168L257 169L260 173L258 174L261 176L259 177L261 178L259 178L258 185L262 189L269 203L275 203L274 205L277 205L277 208L284 208L280 213L281 214L278 217L267 218L264 217L261 211L258 213L256 210L256 214L247 215L246 211L238 212L236 209L231 208L231 205L234 203L231 199L235 195L233 194L232 191L235 191L234 187L238 185L240 181L237 174z",
                        "M127 103L131 105L136 102L139 105L139 103L142 103L141 101L151 99L155 101L159 99L162 101L165 99L172 107L170 113L175 114L177 118L174 123L179 126L173 130L176 132L169 142L167 140L159 140L158 145L157 143L146 145L145 143L145 145L140 145L135 149L134 146L133 148L125 144L125 134L118 132L114 125L120 120L120 114L123 113L127 103z"),
                asList("306,903.00", "122,769.00", "30,701.00"),
                asList("Kladno", "Strakonice", "Tachov")),
        CZ_DISTRICT_NUTS4(
                "CZ_District_NUTS4",
                "Sum of Amount",
                30712f,
                214835f,
                asList(0, 1, 2),
                asList("rgb(43, 107, 174)", "rgb(43, 107, 174)", "rgb(230, 230, 230)"),
                asList("M83 149L82 139L102 140L102 134L108 126L106 123L114 126L116 119L123 113L140 113L145 122L152 123L150 127L158 126L158 131L164 134L161 139L166 145L159 148L152 166L145 166L127 191L124 186L120 188L113 177L110 182L105 180L95 185L96 193L84 197L78 196L83 181L81 172L83 165L88 166L85 154L87 149L83 149z",
                        "M262 95L261 91L269 92L258 70L260 59L267 58L269 52L297 59L299 42L308 41L312 31L318 33L323 29L334 32L342 47L348 44L355 48L364 43L369 50L367 64L373 69L365 81L368 84L363 87L366 87L364 93L352 92L344 100L333 97L325 109L315 101L314 94L301 95L296 106L293 101L286 102L290 97L288 93L267 102L261 99L264 97L262 95z",
                        "M26 204L28 197L34 193L28 189L26 181L32 179L29 176L30 160L36 154L32 153L38 148L45 152L43 144L52 148L56 141L72 138L71 145L87 149L85 154L88 166L83 165L81 172L83 181L78 196L84 197L75 208L71 204L63 208L58 217L68 219L61 222L66 228L58 228L52 226L51 231L49 227L41 227L44 216L34 215L32 210L37 208L26 204z"),
                asList("214,826.00", "214,835.00", "30,712.00"),
                asList("Beroun", "Nymburk", "Rokycany")),
        CZ_DISTRICT_KNOK(
                "CZ_District_KNOK",
                "Sum of Amount",
                122764f,
                214834f,
                asList(0, 1, 2),
                asList("rgb(230, 230, 230)", "rgb(43, 107, 174)", "rgb(230, 230, 230)"),
                asList("M201 119L191 124L186 124L185 128L180 121L176 120L178 117L172 112L176 109L172 105L170 105L177 102L178 104L182 104L183 99L198 95L198 97L204 98L204 102L209 103L213 110L208 112L208 121L202 118L201 119z",
                        "M187 150L189 149L188 145L191 144L188 140L197 140L199 137L202 137L203 141L209 139L209 132L213 134L213 137L216 136L218 138L224 136L228 138L232 135L232 142L235 143L234 145L236 146L236 150L241 150L240 152L249 160L256 163L257 167L262 167L263 173L256 175L255 178L251 175L245 177L238 176L240 179L235 179L234 183L232 184L229 178L226 180L219 174L215 178L216 184L211 185L208 190L207 188L202 187L203 184L200 186L192 184L190 180L196 177L194 175L198 172L195 169L198 165L195 160L192 162L189 157L187 158L187 150z",
                        "M137 86L138 82L145 79L145 77L149 77L148 73L151 71L162 73L166 71L176 74L177 76L172 88L176 90L170 96L168 106L165 107L164 112L161 113L152 113L149 116L147 113L143 113L144 107L148 103L144 100L143 96L146 94L145 92L140 86L138 86L140 85L137 86z"),
                asList("122,764.00", "214,834.00", "122,775.00"),
                asList("Hlavní město Praha", "Benešov", "Kladno"));

        private final String name;
        private final String metricName;
        private final float startMetricValue;
        private final float stopMetricValue;
        private final List<Integer> indexList;
        private final List<String> colorList;
        private final List<String> svgDataList;
        private final List<String> metricValues;
        private final List<String> attrValues;

        private GeoAttributeLabels(String name, String metricName, float startMetricValue,
                float stopMetricValue, List<Integer> indexList, List<String> colorList,
                List<String> svgDataList, List<String> metricValues, List<String> attrValues) {
            this.name = name;
            this.metricName = metricName;
            this.startMetricValue = startMetricValue;
            this.stopMetricValue = stopMetricValue;
            this.indexList = indexList;
            this.colorList = colorList;
            this.svgDataList = svgDataList;
            this.metricValues = metricValues;
            this.attrValues = attrValues;
        }
    }
}
