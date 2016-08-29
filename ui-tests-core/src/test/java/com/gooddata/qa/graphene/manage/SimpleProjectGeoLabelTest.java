package com.gooddata.qa.graphene.manage;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.PAYROLL_CSV;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.md.Fact;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.AttributeLabelTypes;
import com.gooddata.qa.graphene.fragments.csvuploader.DataTypeSelect.ColumnType;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;

public class SimpleProjectGeoLabelTest extends AbstractProjectTest {

    private List<String> attributesList;

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "SimpleProject-test-geos-labels";
    }

    @Test(dependsOnGroups = {"createProject"})
    public void initialize() {
        attributesList =
                asList("Geo Pushpin", "Aus State Name", "Aus State Iso", "Statename", "Stateid",
                        "Statecode", "Countyid", "Countryname", "Country Iso2", "Country Iso3",
                        "Cz District Name", "Cz District No Diacritics", "Cz District Nuts4",
                        "Cz District Knok");
        Map<String, ColumnType> columnIndexAndType = new HashMap<String, ColumnType>();
        columnIndexAndType.put("Cz District Knok", ColumnType.ATTRIBUTE);
        uploadCSV(getFilePathFromResource("/" + PAYROLL_CSV + "/attribute_geo_labels.csv"), columnIndexAndType);
        createMetric("Sum of Amount", 
                format("SELECT SUM([%s])", getMdService().getObjUri(getProject(), Fact.class, title("Amount"))),
                "#,##0.00");
    }

    @Test(dependsOnMethods = {"initialize"})
    public void showInfoForNoAvailableLayer() {
        initDashboardsPage();
        DashboardEditBar dashboardEditBar = dashboardsPage.editDashboard();
        dashboardsPage.addNewTab("no-layer");
        dashboardEditBar.verifyGeoLayersList("Sum of Amount", emptyList());
        dashboardEditBar.saveDashboard();
        dashboardsPage.deleteDashboardTab(1);
    }

    @Test(dependsOnMethods = {"initialize"}, priority = 1)
    public void changeAttributeToGeoStateTest() {
        int i = 0;
        for (AttributeLabelTypes type : getGeoLabels()) {
            initAttributePage().configureAttributeLabel(attributesList.get(i), type);
            i++;
        }
    }

    @Test(dependsOnMethods = {"changeAttributeToGeoStateTest"})
    public void verifyGeoLayersTest() {
        initDashboardsPage();
        DashboardEditBar dashboardEditBar = dashboardsPage.editDashboard();
        dashboardsPage.addNewTab("tab");
        dashboardEditBar.verifyGeoLayersList("Sum of Amount", attributesList);
        dashboardEditBar.saveDashboard();
        dashboardsPage.deleteDashboardTab(1);
    }

    @Test(dependsOnMethods = {"changeAttributeToGeoStateTest"})
    public void verifyGeoChartTest() {
        for (GeoAttributeLabels attributeLayer : GeoAttributeLabels.values()) {
            System.out.println("Verifying attribute " + attributeLayer + " ...");
            initDashboardsPage();
            DashboardEditBar dashboardEditBar = dashboardsPage.editDashboard();
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
        GEO_PUSHPIN("Geo Pushpin", "Sum of Amount", 10225f, 102303f, asList(0, 1, 2, 3, 4, 5, 6, 7,
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
                "Aus State Name",
                "Sum of Amount",
                102300f,
                112526f,
                asList(50, 65, 69),
                asList("rgb(43, 107, 174)", "rgb(230, 230, 230)", "rgb(230, 230, 230)"),
                asList("M241 117L241 128L242 127L244 129L244 132L237 133L236 136L233 137L232 139L232 137L229 135L222 135L219 136L218 138L173 138L173 118L156 118L156 60L166 67L170 67L173 65L177 51L176 40L178 38L178 34L179 35L179 29L182 26L188 42L188 46L190 48L193 46L194 49L197 51L197 55L199 58L198 59L201 63L204 76L210 78L209 77L213 81L218 83L217 86L220 87L222 95L225 97L225 94L225 96L229 97L229 102L239 112L241 116z",
                        "M211 235L210 236L209 234L209 239L207 237L208 239L206 241L203 241L201 239L203 239L201 239L197 232L197 230L199 233L198 232L200 232L194 224L194 218L204 223L207 222L208 225L206 222L207 221L213 219L215 221L215 230L213 230L213 234L211 236z",
                        "M173 199L173 171L180 172L182 176L186 176L186 178L195 186L199 183L201 185L204 184L207 186L210 184L213 185L214 189L224 195L222 197L210 200L207 204L203 204L204 207L202 205L201 206L198 202L199 201L195 203L194 201L195 202L197 200L195 198L188 205L182 202L175 201zM206 204L207 203z"),
                asList("112,526.00", "102,303.00", "102,300.00"),
                asList("Queensland", "Tasmania", "Victoria")),
        AUS_STATE_ISO(
                "Aus State Iso",
                "Sum of Amount",
                10230f,
                112526f,
                asList(2, 39, 90, 97, 112, 116, 156),
                asList("rgb(230, 230, 230)", "rgb(230, 230, 230)", "rgb(43, 107, 174)",
                        "rgb(230, 230, 230)", "rgb(62, 119, 180)", "rgb(62, 119, 180)",
                        "rgb(62, 119, 180)"),
                asList("M283 179L280 184L279 195L269 190L266 184L262 186L259 184L256 185L254 183L249 186L241 178L241 176L238 176L234 172L230 172L228 138L273 138L277 135L283 135L287 137L287 139L291 137L290 134L299 132L300 137L298 139L296 149L297 150L294 160L290 162L292 162L289 165L288 164L289 165L288 164L289 165L287 168L286 167L287 170L285 170L287 170L283 178zM285 171L285 171zM287 167L287 167zM289 163L289 163zM273 183L274 184L275 180L274 179L272 181z",
                        "M160 51L161 52L161 50L164 52L162 48L167 42L166 39L170 38L170 36L171 38L170 37L173 34L177 36L181 34L181 32L176 29L181 31L182 30L185 33L193 33L193 35L199 33L198 35L200 34L199 35L201 37L201 34L203 33L202 34L205 35L202 39L203 40L202 39L203 40L199 42L200 45L196 50L203 56L204 55L204 57L205 56L211 60L211 118L160 118L160 66z",
                        "M296 117L296 128L297 127L299 129L299 132L292 133L291 136L288 137L287 139L287 137L284 135L277 135L274 136L273 138L228 138L228 118L211 118L211 60L221 67L225 67L228 65L232 51L231 40L233 38L233 34L234 35L234 29L237 26L243 42L243 46L245 48L248 46L249 49L252 51L252 55L254 58L253 59L256 63L259 76L265 78L264 77L268 81L273 83L272 86L275 87L277 95L280 97L280 94L280 96L284 97L284 102L294 112L296 116z",
                        "M160 155L160 118L228 118L228 199L226 199L222 195L222 191L219 184L215 181L212 182L214 180L214 176L211 172L210 178L205 179L205 177L207 177L208 175L211 165L210 160L210 164L208 165L207 169L202 171L199 176L200 178L195 174L196 175L193 166L189 164L189 160L187 161L188 160L186 158L183 159L181 157L178 158L176 155L166 154z",
                        "M266 235L265 236L264 234L264 239L262 237L263 239L261 241L258 241L256 239L258 239L256 239L252 232L252 230L254 233L253 232L255 232L249 224L249 218L259 223L262 222L263 225L261 222L262 221L268 219L270 221L270 230L268 230L268 234L266 236z",
                        "M228 199L228 171L235 172L237 176L241 176L241 178L250 186L254 183L256 185L259 184L262 186L265 184L268 185L269 189L279 195L277 197L265 200L262 204L258 204L259 207L257 205L256 206L253 202L254 201L250 203L249 201L250 202L252 200L250 198L243 205L237 202L230 201zM261 204L262 203z",
                        "M160 51L160 155L137 162L132 165L131 168L127 171L109 170L106 172L106 174L105 173L104 175L103 174L98 178L97 177L97 179L89 178L84 174L80 173L80 168L84 166L85 157L84 158L80 146L80 139L75 130L76 129L70 120L71 118L73 123L74 121L71 117L72 115L73 119L74 118L74 120L76 120L71 109L74 99L74 93L75 92L76 97L80 91L82 91L91 84L90 85L93 86L97 85L98 83L110 81L115 78L118 75L118 73L122 70L121 65L126 59L125 61L127 61L126 62L129 66L129 64L131 64L131 62L130 63L128 60L130 60L130 58L136 60L134 58L136 56L134 57L134 55L136 53L140 55L136 52L140 52L138 50L140 48L141 49L141 47L141 49L143 49L143 45L145 47L144 46L146 45L146 47L148 45L147 44L149 44L155 50L155 54L157 50L159 51z"),
                asList("10,236.00", "10,239.00", "112,526.00", "10,237.00", "102,303.00",
                        "102,300.00", "102,302.00"),
                asList("New South Wales", "Northern Territory", "Queensland", "South Australia",
                        "Tasmania", "Victoria", "Western Australia")),
        US_STATE_NAME(
                "Statename",
                "Sum of Amount",
                102301f,
                255769f,
                asList(56),
                asList("rgb(43, 107, 174)"),
                asList("M64 64L63 63L64 64z"),
                asList("255,769.00"),
                asList("Washington")),
        US_STATE_CENSUS_ID(
                "Stateid",
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
                "Statecode",
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
                "Countyid",
                "Sum of Amount",
                10225f,
                225081f,
                asList(0, 1, 2, 3, 4),
                asList("rgb(43, 107, 174)", "rgb(230, 230, 230)", "rgb(230, 230, 230)",
                        "rgb(61, 119, 179)", "rgb(230, 230, 230)"),
                asList("M-51 85L-55 79L-56 63L-51 63L-44 70L-44 85z",
                        "M28 45L29 41L38 42L40 39L55 39L55 44L51 44L50 51L46 52L46 55L31 55L31 58L24 58L24 49L28 49z",
                        "M-18 31L-22 34L-24 32L-26 36L-29 38L-27 43L-31 49L-47 49L-47 52L-49 52L-49 28L-47 20L-44 18L-15 19L-16 29z",
                        "M60 -74L60 -79L73 -79L73 -82L78 -82L78 -85L95 -85L95 -82L98 -82L98 -79L102 -79L102 -65L96 -65L95 -59L88 -55L77 -55L68 -61L70 -64L60 -64z",
                        "M-72 47L-78 47L-78 44L-81 43L-90 43L-95 49L-101 49L-101 46L-99 45L-97 38L-99 36L-102 36L-102 34L-97 33L-96 26L-93 23L-89 25L-87 23L-85 25L-80 25L-80 22L-62 22L-64 24L-66 32L-69 34L-72 41z"),
                asList("225,081.00", "10,225.00", "10,236.00", "204,601.00", "10,230.00"),
                asList("Ada County", "Clark County", "Valley County", "Chouteau County", "Baker County")),
        WORLD_COUNTRIES_NAME(
                "Countryname",
                "Sum of Amount",
                30712f,
                214835f,
                asList(0, 6, 10, 11),
                asList("rgb(43, 107, 174)", "rgb(43, 107, 174)", "rgb(43, 107, 174)",
                        "rgb(230, 230, 230)"),
                asList("M68 1L64 1L61 -3L64 -5L71 -1L68 0z",
                        "M-91 -1L-90 1L-86 2L-82 1L-75 7L-73 7L-74 15L-68 12L-69 11L-65 11L-62 8L-56 7L-54 3L-52 4L-51 8L-57 12L-57 15L-60 16L-58 16L-62 17L-61 18L-64 23L-65 19L-64 26L-72 33L-70 40L-71 43L-75 35L-82 35L-84 37L-91 36L-95 42L-100 36L-103 37L-107 32L-114 33L-123 31L-125 28L-128 27L-132 20L-133 1L-130 4L-131 0L-91 -1z",
                        "M-177 -66L-176 -64L-172 -65L-173 -64L-169 -64L-162 -61L-160 -62L-157 -60L-157 -28L-155 -28L-151 -24L-149 -27L-141 -16L-142 -14L-144 -15L-144 -18L-147 -22L-152 -23L-156 -27L-161 -27L-165 -30L-167 -29L-167 -27L-172 -25L-170 -30L-175 -25L-175 -22L-183 -15L-184 -16L-190 -13L-184 -17L-179 -24L-184 -23L-184 -25L-185 -24L-187 -27L-191 -29L-191 -35L-190 -37L-188 -36L-185 -39L-185 -42L-191 -41L-195 -45L-190 -48L-189 -46L-186 -47L-193 -55L-188 -59L-186 -63L-179 -67z",
                        "M198 49L194 52L199 58L199 63L194 68L192 65L197 62L197 58L189 47L194 46L196 48z"),
                asList("214,826.00", "214,835.00", "214,835.00", "214,835.00"),
                asList("Czech Republic", "United States of America", "United States of America",
                        "United States of America")),
        WORLD_COUNTRIES_ISO2(
                "Country Iso2",
                "Sum of Amount",
                30712f,
                214835f,
                asList(0, 6, 10, 11),
                asList("rgb(43, 107, 174)", "rgb(43, 107, 174)", "rgb(43, 107, 174)",
                        "rgb(230, 230, 230)"),
                asList("M68 1L64 1L61 -3L64 -5L71 -1L68 0z",
                        "M-91 -1L-90 1L-86 2L-82 1L-75 7L-73 7L-74 15L-68 12L-69 11L-65 11L-62 8L-56 7L-54 3L-52 4L-51 8L-57 12L-57 15L-60 16L-58 16L-62 17L-61 18L-64 23L-65 19L-64 26L-72 33L-70 40L-71 43L-75 35L-82 35L-84 37L-91 36L-95 42L-100 36L-103 37L-107 32L-114 33L-123 31L-125 28L-128 27L-132 20L-133 1L-130 4L-131 0L-91 -1z",
                        "M-177 -66L-176 -64L-172 -65L-173 -64L-169 -64L-162 -61L-160 -62L-157 -60L-157 -28L-155 -28L-151 -24L-149 -27L-141 -16L-142 -14L-144 -15L-144 -18L-147 -22L-152 -23L-156 -27L-161 -27L-165 -30L-167 -29L-167 -27L-172 -25L-170 -30L-175 -25L-175 -22L-183 -15L-184 -16L-190 -13L-184 -17L-179 -24L-184 -23L-184 -25L-185 -24L-187 -27L-191 -29L-191 -35L-190 -37L-188 -36L-185 -39L-185 -42L-191 -41L-195 -45L-190 -48L-189 -46L-186 -47L-193 -55L-188 -59L-186 -63L-179 -67z",
                        "M198 49L194 52L199 58L199 63L194 68L192 65L197 62L197 58L189 47L194 46L196 48z"),
                asList("214,826.00", "214,835.00", "214,835.00", "214,835.00"),
                asList("Czech Republic", "United States", "United States", "United States")),
        WORLD_COUNTRIES_ISO3(
                "Country Iso3",
                "Sum of Amount",
                30712f,
                214835f,
                asList(0, 6, 10, 11),
                asList("rgb(43, 107, 174)", "rgb(43, 107, 174)", "rgb(43, 107, 174)",
                        "rgb(230, 230, 230)"),
                asList("M68 1L64 1L61 -3L64 -5L71 -1L68 0z",
                        "M-91 -1L-90 1L-86 2L-82 1L-75 7L-73 7L-74 15L-68 12L-69 11L-65 11L-62 8L-56 7L-54 3L-52 4L-51 8L-57 12L-57 15L-60 16L-58 16L-62 17L-61 18L-64 23L-65 19L-64 26L-72 33L-70 40L-71 43L-75 35L-82 35L-84 37L-91 36L-95 42L-100 36L-103 37L-107 32L-114 33L-123 31L-125 28L-128 27L-132 20L-133 1L-130 4L-131 0L-91 -1z",
                        "M-177 -66L-176 -64L-172 -65L-173 -64L-169 -64L-162 -61L-160 -62L-157 -60L-157 -28L-155 -28L-151 -24L-149 -27L-141 -16L-142 -14L-144 -15L-144 -18L-147 -22L-152 -23L-156 -27L-161 -27L-165 -30L-167 -29L-167 -27L-172 -25L-170 -30L-175 -25L-175 -22L-183 -15L-184 -16L-190 -13L-184 -17L-179 -24L-184 -23L-184 -25L-185 -24L-187 -27L-191 -29L-191 -35L-190 -37L-188 -36L-185 -39L-185 -42L-191 -41L-195 -45L-190 -48L-189 -46L-186 -47L-193 -55L-188 -59L-186 -63L-179 -67z",
                        "M198 49L194 52L199 58L199 63L194 68L192 65L197 62L197 58L189 47L194 46L196 48z"),
                asList("214,826.00", "214,835.00", "214,835.00", "214,835.00"),
                asList("Czech Republic", "United States", "United States", "United States")),
        CZ_DISTRICT_NAME(
                "Cz District Name",
                "Sum of Amount",
                30712f,
                214835f,
                asList(0, 1, 2),
                asList("rgb(43, 107, 174)", "rgb(43, 107, 174)", "rgb(230, 230, 230)"),
                asList("M83 149L82 139L102 140L102 134L108 126L106 123L114 126L116 119L123 113L140 113L145 122L152 123L150 127L158 126L158 131L164 134L161 139L166 145L159 148L152 166L145 166L127 191L124 186L120 188L113 177L110 182L105 180L95 185L96 193L84 197L78 196L83 181L81 172L83 165L88 166L85 154L87 149z",
                        "M262 95L261 91L269 92L258 70L260 59L267 58L269 52L297 59L299 42L308 41L312 31L318 33L323 29L334 32L342 47L348 44L355 48L364 43L369 50L367 64L373 69L365 81L368 84L363 87L366 87L364 93L352 92L344 100L333 97L325 109L315 101L314 94L301 95L296 106L293 101L286 102L290 97L288 93L267 102L261 99L264 97z",
                        "M26 204L28 197L34 193L28 189L26 181L32 179L29 176L30 160L36 154L32 153L38 148L45 152L43 144L52 148L56 141L72 138L71 145L87 149L85 154L88 166L83 165L81 172L83 181L78 196L84 197L75 208L71 204L63 208L58 217L68 219L61 222L66 228L58 228L52 226L51 231L49 227L41 227L44 216L34 215L32 210L37 208z"),
                asList("214,826.00", "214,835.00", "30,712.00"),
                asList("Beroun", "Nymburk", "Rokycany")),
        CZ_DISTRICT_NAME_WO_DIAC(
                "Cz District No Diacritics",
                "Sum of Amount",
                30701f,
                306903f,
                asList(0, 1, 2),
                asList("rgb(43, 107, 174)", "rgb(168, 189, 211)", "rgb(230, 230, 230)"),
                asList("M247 57L248 53L255 50L255 48L259 48L258 44L261 42L272 44L276 42L286 45L287 47L282 59L286 61L280 67L278 77L275 78L274 83L262 84L259 87L257 84L253 84L254 78L258 74L254 71L253 67L256 65L250 57L248 57L250 56z",
                        "M237 175L240 171L238 168L238 164L236 164L238 160L243 158L245 160L247 158L256 161L257 164L261 166L260 169L257 170L260 174L258 175L261 177L259 178L261 179L259 179L258 186L262 190L269 204L275 204L274 206L277 206L277 209L284 209L280 214L281 215L278 218L267 219L264 218L261 212L258 214L256 211L256 215L247 216L246 212L238 213L236 210L231 209L231 206L234 204L231 200L235 196L233 195L232 192L235 192L234 188L238 186L240 182z",
                        "M127 104L131 106L136 103L139 106L139 104L142 104L141 102L151 100L155 102L159 100L162 102L165 100L172 108L170 114L175 115L177 119L174 124L179 127L173 131L176 133L169 143L167 141L159 141L158 146L157 144L146 146L145 144L145 146L140 146L135 150L134 147L133 149L125 145L125 135L118 133L114 126L120 121L120 115L123 114z"),
                asList("306,903.00", "122,769.00", "30,701.00"),
                asList("Kladno", "Strakonice", "Tachov")),
        CZ_DISTRICT_NUTS4(
                "Cz District Nuts4",
                "Sum of Amount",
                30712f,
                214835f,
                asList(0, 1, 2),
                asList("rgb(43, 107, 174)", "rgb(43, 107, 174)", "rgb(230, 230, 230)"),
                asList("M83 149L82 139L102 140L102 134L108 126L106 123L114 126L116 119L123 113L140 113L145 122L152 123L150 127L158 126L158 131L164 134L161 139L166 145L159 148L152 166L145 166L127 191L124 186L120 188L113 177L110 182L105 180L95 185L96 193L84 197L78 196L83 181L81 172L83 165L88 166L85 154L87 149z",
                        "M262 95L261 91L269 92L258 70L260 59L267 58L269 52L297 59L299 42L308 41L312 31L318 33L323 29L334 32L342 47L348 44L355 48L364 43L369 50L367 64L373 69L365 81L368 84L363 87L366 87L364 93L352 92L344 100L333 97L325 109L315 101L314 94L301 95L296 106L293 101L286 102L290 97L288 93L267 102L261 99L264 97z",
                        "M26 204L28 197L34 193L28 189L26 181L32 179L29 176L30 160L36 154L32 153L38 148L45 152L43 144L52 148L56 141L72 138L71 145L87 149L85 154L88 166L83 165L81 172L83 181L78 196L84 197L75 208L71 204L63 208L58 217L68 219L61 222L66 228L58 228L52 226L51 231L49 227L41 227L44 216L34 215L32 210L37 208z"),
                asList("214,826.00", "214,835.00", "30,712.00"),
                asList("Beroun", "Nymburk", "Rokycany")),
        CZ_DISTRICT_KNOK(
                "Cz District Knok",
                "Sum of Amount",
                122764f,
                214834f,
                asList(0, 1, 2),
                asList("rgb(230, 230, 230)", "rgb(43, 107, 174)", "rgb(230, 230, 230)"),
                asList("M201 119L191 124L186 124L185 128L180 121L176 120L178 117L172 112L176 109L172 105L170 105L177 102L178 104L182 104L183 99L198 95L198 97L204 98L204 102L209 103L213 110L208 112L208 121L202 118z",
                        "M187 150L189 149L188 145L191 144L188 140L197 140L199 137L202 137L203 141L209 139L209 132L213 134L213 137L216 136L218 138L224 136L228 138L232 135L232 142L235 143L234 145L236 146L236 150L241 150L240 152L249 160L256 163L257 167L262 167L263 173L256 175L255 178L251 175L245 177L238 176L240 179L235 179L234 183L232 184L229 178L226 180L219 174L215 178L216 184L211 185L208 190L207 188L202 187L203 184L200 186L192 184L190 180L196 177L194 175L198 172L195 169L198 165L195 160L192 162L189 157L187 158L186 155L188 154z",
                        "M137 86L138 82L145 79L145 77L149 77L148 73L151 71L162 73L166 71L176 74L177 76L172 88L176 90L170 96L168 106L165 107L164 112L152 113L149 116L147 113L143 113L144 107L148 103L144 100L143 96L146 94L140 86L138 86L140 85z"),
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
