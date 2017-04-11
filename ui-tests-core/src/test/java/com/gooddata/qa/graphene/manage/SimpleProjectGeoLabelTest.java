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
        GEO_PUSHPIN(
                "Geo Pushpin",
                "Sum of Amount",
                10225f,
                102303f,
                asList(0, 1, 2, 3, 4, 5, 6, 7, 8),
                asList("rgb(230, 230, 230)", "rgb(43, 107, 174)", "rgb(230, 230, 230)", "rgb(43, 107, 174)",
                        "rgb(43, 107, 174)", "rgb(43, 107, 174)", "rgb(230, 230, 230)", "rgb(230, 230, 230)",
                        "rgb(230, 230, 230)"),
                asList("M419,245A4,4,0,1,1,418.9,245 z", "M326,138A4,4,0,1,1,325.9,138 z",
                        "M356,129A4,4,0,1,1,355.9,129 z", "M347,129A4,4,0,1,1,346.9,129 z",
                        "M356,120A4,4,0,1,1,355.9,120 z", "M363,114A4,4,0,1,1,362.9,114 z",
                        "M96,59A4,4,0,1,1,95.9,59 z", "M365,52A4,4,0,1,1,364.9,52 z",
                        "M100,33A4,4,0,1,1,99.9,33 z"),
                asList("10,230.00", "102,303.00", "10,237.00", "102,300.00", "102,302.00", "102,301.00",
                        "10,239.00", "10,236.00", "10,225.00"),
                asList("31.190020;-85.399150", "33.179071;-87.447521", "33.334175;-86.784966",
                        "33.335554;-86.994991", "33.507590;-86.798513", "33.607361;-86.630355",
                        "34.616719;-92.498309", "34.743001;-86.601108", "35.091612;-92.427588")),
        AUS_STATE_NAME(
                "Aus State Name",
                "Sum of Amount",
                102300f,
                112526f,
                asList(50, 65, 69),
                asList("rgb(43, 107, 174)", "rgb(230, 230, 230)", "rgb(230, 230, 230)"),
                asList("M40 -13L40 -2L41 -3L43 -1L43 2L36 3L35 6L32 7L31 9L31 7L28 5L21 5L18 6L17 8L-28 8L-28 -12L-45 -12L-45 -70L-35 -63L-31 -63L-28 -65L-24 -79L-25 -90L-23 -92L-23 -96L-22 -95L-22 -101L-19 -104L-13 -88L-13 -84L-11 -82L-8 -84L-7 -81L-4 -79L-4 -75L-2 -72L-3 -71L0 -67L3 -54L9 -52L8 -53L12 -49L17 -47L16 -44L19 -43L21 -35L24 -33L24 -36L24 -34L28 -33L28 -28L38 -18L40 -14z",
                        "M10 105L7 105L8 109L6 107L7 109L5 111L2 111L0 109L2 109L0 109L-4 102L-4 100L-2 103L-3 102L-1 102L-7 94L-7 88L3 93L6 92L7 95L5 92L6 91L12 89L14 91L14 100L12 100L12 104L10 106z",
                        "M-28 69L-28 41L-21 42L-19 46L-15 46L-15 48L-6 56L-2 53L0 55L3 54L6 56L9 54L12 55L13 59L23 65L21 67L9 70L6 74L2 74L3 77L1 75L0 76L-3 72L-2 71L-6 73L-7 71L-6 72L-4 70L-6 68L-13 75L-19 72L-26 71zM5 74L6 73z"),
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
                asList("M83 49L80 54L79 65L69 60L66 54L62 56L59 54L56 55L54 53L49 56L41 48L41 46L38 46L34 42L30 42L28 8L73 8L77 5L83 5L87 7L87 9L91 7L90 4L99 2L100 7L98 9L96 19L97 20L94 30L90 32L92 32L89 35L88 34L89 35L88 34L89 35L87 38L86 37L87 40L85 40L87 40L83 48zM85 41L85 41zM87 37L87 37zM89 33L89 33zM73 53L74 54L75 50L74 49L72 51z",
                        "M-40 -79L-39 -78L-39 -80L-36 -78L-38 -82L-33 -88L-34 -91L-30 -92L-30 -94L-29 -92L-30 -93L-27 -96L-23 -94L-19 -96L-19 -98L-24 -101L-19 -99L-18 -100L-15 -97L-7 -97L-7 -95L-1 -97L-2 -95L0 -96L-1 -95L1 -93L1 -96L3 -97L2 -96L5 -95L2 -91L3 -90L2 -91L3 -90L-1 -88L0 -85L-4 -80L3 -74L4 -75L4 -73L5 -74L11 -70L11 -12L-40 -12L-40 -64z",
                        "M96 -13L96 -2L97 -3L99 -1L99 2L92 3L91 6L87 9L87 7L84 5L77 5L73 8L28 8L28 -12L11 -12L11 -70L21 -63L25 -63L28 -65L32 -79L31 -90L33 -92L33 -96L34 -95L34 -101L37 -104L43 -88L43 -84L45 -82L48 -84L49 -81L52 -79L52 -75L54 -72L53 -71L56 -67L59 -54L65 -52L64 -53L68 -49L73 -47L72 -44L75 -43L77 -35L80 -33L80 -36L80 -34L84 -33L84 -28L94 -18L96 -14z",
                        "M-40 25L-40 -12L28 -12L28 69L26 69L22 65L22 61L19 54L15 51L12 52L14 50L14 46L11 42L10 48L5 49L5 47L7 47L8 45L11 35L10 30L10 34L8 35L7 39L2 41L-1 46L0 48L-5 44L-4 45L-7 36L-11 34L-11 30L-13 31L-12 30L-14 28L-17 29L-19 27L-22 28L-24 25L-34 24z",
                        "M66 105L65 106L64 104L64 109L62 107L63 109L61 111L58 111L56 109L58 109L56 109L52 102L52 100L54 103L53 102L55 102L49 94L49 88L59 93L62 92L63 95L61 92L62 91L68 89L70 91L70 100L68 100L68 104L66 106z",
                        "M28 69L28 41L35 42L37 46L41 46L41 48L50 56L54 53L56 55L59 54L62 56L65 54L68 55L69 59L79 65L77 67L65 70L62 74L58 74L59 77L57 75L56 76L53 72L54 71L50 73L49 71L50 72L52 70L50 68L43 75L37 72L30 71zM61 74L62 73z",
                        "M-40 -79L-40 25L-63 32L-68 35L-69 38L-73 41L-91 40L-94 42L-94 44L-95 43L-96 45L-97 44L-102 48L-103 47L-103 49L-111 48L-116 44L-120 43L-120 38L-116 36L-115 27L-116 28L-120 16L-120 9L-125 0L-124 -1L-130 -10L-129 -12L-127 -7L-126 -9L-129 -13L-128 -15L-127 -11L-126 -12L-126 -10L-124 -10L-129 -21L-126 -31L-126 -37L-125 -38L-124 -33L-120 -39L-118 -39L-109 -46L-110 -45L-107 -44L-103 -45L-102 -47L-90 -49L-85 -52L-82 -55L-82 -57L-78 -60L-79 -65L-74 -71L-75 -69L-73 -69L-74 -68L-71 -64L-71 -66L-69 -66L-69 -68L-70 -67L-72 -70L-70 -70L-70 -72L-64 -70L-66 -72L-64 -74L-66 -73L-66 -75L-64 -77L-60 -75L-64 -78L-60 -78L-62 -80L-60 -82L-59 -81L-59 -83L-59 -81L-57 -81L-57 -85L-55 -83L-56 -84L-54 -85L-54 -83L-52 -85L-53 -86L-51 -86L-45 -80L-45 -76L-43 -80L-41 -79z"),
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
                asList("M-121 -32L-121 -40L-122 -39L-121 -40L-123 -42L-122 -45L-123 -44L-124 -46L-123 -47L-91 -47L-90 -21L-106 -21L-114 -18L-122 -18L-125 -23L-131 -24L-130 -29L-132 -31L-135 -41L-130 -39L-125 -40L-126 -39L-124 -39L-123 -36L-122 -37L-123 -38L-121 -32z",
                        "M128 32L123 37L121 37L118 44L110 46L109 43L107 43L105 40L105 38L108 35L107 34L109 34L110 31L115 28L116 21L117 26L122 27L123 31L127 28L131 28L133 30L132 32L129 29L128 32z",
                        "M76 -13L74 -11L75 -10L80 -15L76 -5L76 5L59 6L56 3L57 0L55 -5L50 -10L47 -11L47 -20L50 -23L51 -28L54 -27L58 -29L58 -27L63 -24L73 -21L76 -18L76 -13z",
                        "M-17 -5L-17 18L-57 18L-57 -13L-17 -13L-17 -5z"),
                asList("102,300.00", "102,301.00", "102,302.00", "102,303.00"),
                asList("Washington", "West Virginia", "Wisconsin", "Wyoming")),
        US_STATE_CODE(
                "Statecode",
                "Sum of Amount",
                102301f,
                255769f,
                asList(1, 12),
                asList("rgb(230, 230, 230)", "rgb(43, 107, 174)"),
                asList("M205 76L218 76L222 101L226 113L223 118L225 129L195 130L195 132L198 135L197 138L195 140L191 140L194 139L191 134L189 138L187 138L186 136L185 120L190 77L188 75L205 76z",
                        "M-201 -112L-201 -120L-199 -124L-200 -127L-200 -125L-202 -125L-201 -128L-202 -131L-204 -131L-202 -131L-201 -134L-203 -137L-203 -135L-204 -136L-203 -137L-205 -139L-140 -140L-140 -95L-138 -90L-162 -90L-167 -88L-168 -89L-176 -85L-194 -85L-199 -82L-202 -83L-205 -85L-206 -91L-209 -93L-211 -92L-213 -94L-220 -95L-220 -99L-219 -100L-219 -98L-217 -102L-219 -102L-218 -101L-220 -105L-219 -104L-217 -106L-221 -106L-224 -120L-227 -123L-227 -129L-226 -130L-220 -126L-209 -126L-207 -124L-205 -126L-203 -122L-205 -120L-204 -119L-203 -122L-201 -119L-201 -112z"),
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
                asList("M-52 85L-56 79L-57 63L-52 63L-45 70L-45 85z",
                        "M27 45L28 41L37 42L39 39L54 39L54 44L50 44L49 51L45 52L45 55L30 55L30 58L23 58L23 49L27 49z",
                        "M-19 31L-23 34L-25 32L-27 36L-30 38L-28 43L-32 49L-48 49L-48 52L-50 52L-50 28L-48 20L-45 18L-16 19L-17 29z",
                        "M59 -74L59 -79L72 -79L72 -82L77 -82L77 -85L94 -85L94 -82L97 -82L97 -79L101 -79L101 -65L95 -65L94 -59L87 -55L76 -55L67 -61L69 -64L59 -64z",
                        "M-73 47L-79 47L-79 44L-82 43L-91 43L-96 49L-102 49L-102 46L-100 45L-98 38L-100 36L-103 36L-103 34L-98 33L-97 26L-94 23L-90 25L-88 23L-86 25L-81 25L-81 22L-63 22L-65 24L-67 32L-70 34L-73 41z"),
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
                asList("M7 -37L3 -37L0 -41L3 -43L10 -39L7 -38z",
                        "M-152 -39L-151 -37L-147 -36L-143 -37L-136 -31L-134 -31L-135 -23L-129 -26L-130 -27L-126 -27L-123 -30L-117 -31L-115 -35L-113 -34L-112 -30L-118 -26L-118 -23L-121 -22L-119 -22L-123 -21L-122 -20L-125 -15L-126 -19L-125 -12L-133 -5L-131 2L-132 5L-136 -3L-143 -3L-145 -1L-152 -2L-156 4L-161 -2L-164 -1L-168 -6L-175 -5L-184 -7L-186 -10L-189 -11L-193 -18L-194 -37L-191 -34L-192 -38L-152 -39z",
                        "M-238 -104L-237 -102L-233 -103L-234 -102L-230 -102L-223 -99L-221 -100L-218 -98L-218 -66L-216 -66L-212 -62L-210 -65L-202 -54L-203 -52L-205 -53L-205 -56L-208 -60L-213 -61L-217 -65L-222 -65L-226 -68L-228 -67L-228 -65L-233 -63L-231 -68L-236 -63L-236 -60L-244 -53L-245 -54L-251 -51L-245 -55L-240 -62L-245 -61L-245 -63L-246 -62L-248 -65L-252 -67L-252 -73L-251 -75L-249 -74L-246 -77L-246 -80L-252 -79L-256 -83L-251 -86L-250 -84L-247 -85L-254 -93L-249 -97L-247 -101L-240 -105z",
                        "M137 11L133 14L138 20L138 25L133 30L131 27L136 24L136 20L128 9L133 8L135 10z"),
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
                asList("M7 -37L3 -37L0 -41L3 -43L10 -39L7 -38z",
                        "M-152 -39L-151 -37L-147 -36L-143 -37L-136 -31L-134 -31L-135 -23L-129 -26L-130 -27L-126 -27L-123 -30L-117 -31L-115 -35L-113 -34L-112 -30L-118 -26L-118 -23L-121 -22L-119 -22L-123 -21L-122 -20L-125 -15L-126 -19L-125 -12L-133 -5L-131 2L-132 5L-136 -3L-143 -3L-145 -1L-152 -2L-156 4L-161 -2L-164 -1L-168 -6L-175 -5L-184 -7L-186 -10L-189 -11L-193 -18L-194 -37L-191 -34L-192 -38L-152 -39z",
                        "M-238 -104L-237 -102L-233 -103L-234 -102L-230 -102L-223 -99L-221 -100L-218 -98L-218 -66L-216 -66L-212 -62L-210 -65L-202 -54L-203 -52L-205 -53L-205 -56L-208 -60L-213 -61L-217 -65L-222 -65L-226 -68L-228 -67L-228 -65L-233 -63L-231 -68L-236 -63L-236 -60L-244 -53L-245 -54L-251 -51L-245 -55L-240 -62L-245 -61L-245 -63L-246 -62L-248 -65L-252 -67L-252 -73L-251 -75L-249 -74L-246 -77L-246 -80L-252 -79L-256 -83L-251 -86L-250 -84L-247 -85L-254 -93L-249 -97L-247 -101L-240 -105z",
                        "M137 11L133 14L138 20L138 25L133 30L131 27L136 24L136 20L128 9L133 8L135 10z"),
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
                asList("M7 -37L3 -37L0 -41L3 -43L10 -39L7 -38z",
                        "M-152 -39L-151 -37L-147 -36L-143 -37L-136 -31L-134 -31L-135 -23L-129 -26L-130 -27L-126 -27L-123 -30L-117 -31L-115 -35L-113 -34L-112 -30L-118 -26L-118 -23L-121 -22L-119 -22L-123 -21L-122 -20L-125 -15L-126 -19L-125 -12L-133 -5L-131 2L-132 5L-136 -3L-143 -3L-145 -1L-152 -2L-156 4L-161 -2L-164 -1L-168 -6L-175 -5L-184 -7L-186 -10L-189 -11L-193 -18L-194 -37L-191 -34L-192 -38L-152 -39z",
                        "M-238 -104L-237 -102L-233 -103L-234 -102L-230 -102L-223 -99L-221 -100L-218 -98L-218 -66L-216 -66L-212 -62L-210 -65L-202 -54L-203 -52L-205 -53L-205 -56L-208 -60L-213 -61L-217 -65L-222 -65L-226 -68L-228 -67L-228 -65L-233 -63L-231 -68L-236 -63L-236 -60L-244 -53L-245 -54L-251 -51L-245 -55L-240 -62L-245 -61L-245 -63L-246 -62L-248 -65L-252 -67L-252 -73L-251 -75L-249 -74L-246 -77L-246 -80L-252 -79L-256 -83L-251 -86L-250 -84L-247 -85L-254 -93L-249 -97L-247 -101L-240 -105z",
                        "M137 11L133 14L138 20L138 25L133 30L131 27L136 24L136 20L128 9L133 8L135 10z"),
                asList("214,826.00", "214,835.00", "214,835.00", "214,835.00"),
                asList("Czech Republic", "United States", "United States", "United States")),
        CZ_DISTRICT_NAME(
                "Cz District Name",
                "Sum of Amount",
                30712f,
                214835f,
                asList(0, 1, 2),
                asList("rgb(43, 107, 174)", "rgb(43, 107, 174)", "rgb(230, 230, 230)"),
                asList("M141 162L140 152L160 153L160 147L166 139L164 136L172 139L174 132L181 126L198 126L203 135L210 136L208 140L216 139L216 144L222 147L219 152L224 158L217 161L210 179L203 179L185 204L182 199L178 201L171 190L168 195L163 193L153 198L154 206L142 210L136 209L141 194L139 185L141 178L146 179L143 167L145 162z",
                        "M320 108L319 104L327 105L316 83L318 72L325 71L327 65L355 72L357 55L366 54L370 44L376 46L381 42L392 45L400 60L406 57L413 61L422 56L427 63L425 77L431 82L423 94L426 97L421 100L424 100L422 106L410 105L402 113L391 110L383 122L373 114L372 107L359 108L354 119L351 114L344 115L348 110L346 106L325 115L319 112L322 110z",
                        "M84 217L86 210L92 206L86 202L84 194L90 192L87 189L88 173L94 167L90 166L96 161L103 165L101 157L110 161L114 154L130 151L129 158L145 162L143 167L146 179L141 178L139 185L141 194L136 209L142 210L133 221L129 217L121 221L116 230L126 232L119 235L124 241L116 241L110 239L109 244L107 240L99 240L102 229L92 228L90 223L95 221z"),
                asList("214,826.00", "214,835.00", "30,712.00"),
                asList("Beroun", "Nymburk", "Rokycany")),
        CZ_DISTRICT_NAME_WO_DIAC(
                "Cz District No Diacritics",
                "Sum of Amount",
                30701f,
                306903f,
                asList(0, 1, 2),
                asList("rgb(43, 107, 174)", "rgb(168, 189, 211)", "rgb(230, 230, 230)"),
                asList("M304 69L305 65L312 62L312 60L316 60L315 56L318 54L329 56L333 54L343 57L344 59L339 71L343 73L337 79L335 89L332 90L331 95L319 96L316 99L314 96L310 96L311 90L315 86L311 83L310 79L313 77L307 69L305 69L307 68z",
                        "M294 187L297 183L295 180L295 176L293 176L295 172L300 170L302 172L304 170L313 173L314 176L318 178L317 181L314 182L317 186L315 187L318 189L316 190L318 191L316 191L315 198L319 202L326 216L332 216L331 218L334 218L334 221L341 221L337 226L338 227L335 230L324 231L321 230L318 224L315 226L313 223L313 227L304 228L303 224L295 225L293 222L288 221L288 218L291 216L288 212L292 208L290 207L289 204L292 204L291 200L295 198L297 194z",
                        "M184 116L188 118L193 115L196 118L196 116L199 116L198 114L208 112L212 114L216 112L219 114L222 112L229 120L227 126L232 127L234 131L231 136L236 139L230 143L233 145L226 155L224 153L216 153L215 158L214 156L203 158L202 156L202 158L197 158L192 162L191 159L190 161L182 157L182 147L175 145L171 138L177 133L177 127L180 126z"),
                asList("306,903.00", "122,769.00", "30,701.00"),
                asList("Kladno", "Strakonice", "Tachov")),
        CZ_DISTRICT_NUTS4(
                "Cz District Nuts4",
                "Sum of Amount",
                30712f,
                214835f,
                asList(0, 1, 2),
                asList("rgb(43, 107, 174)", "rgb(43, 107, 174)", "rgb(230, 230, 230)"),
                asList("M141 162L140 152L160 153L160 147L166 139L164 136L172 139L174 132L181 126L198 126L203 135L210 136L208 140L216 139L216 144L222 147L219 152L224 158L217 161L210 179L203 179L185 204L182 199L178 201L171 190L168 195L163 193L153 198L154 206L142 210L136 209L141 194L139 185L141 178L146 179L143 167L145 162z",
                        "M320 108L319 104L327 105L316 83L318 72L325 71L327 65L355 72L357 55L366 54L370 44L376 46L381 42L392 45L400 60L406 57L413 61L422 56L427 63L425 77L431 82L423 94L426 97L421 100L424 100L422 106L410 105L402 113L391 110L383 122L373 114L372 107L359 108L354 119L351 114L344 115L348 110L346 106L325 115L319 112L322 110z",
                        "M84 217L86 210L92 206L86 202L84 194L90 192L87 189L88 173L94 167L90 166L96 161L103 165L101 157L110 161L114 154L130 151L129 158L145 162L143 167L146 179L141 178L139 185L141 194L136 209L142 210L133 221L129 217L121 221L116 230L126 232L119 235L124 241L116 241L110 239L109 244L107 240L99 240L102 229L92 228L90 223L95 221z"),
                asList("214,826.00", "214,835.00", "30,712.00"),
                asList("Beroun", "Nymburk", "Rokycany")),
        CZ_DISTRICT_KNOK(
                "Cz District Knok",
                "Sum of Amount",
                122764f,
                214834f,
                asList(0, 1, 2),
                asList("rgb(230, 230, 230)", "rgb(43, 107, 174)", "rgb(230, 230, 230)"),
                asList("M260 120L241 130L229 130L229 139L219 125L211 122L214 116L202 106L209 101L203 93L198 93L212 85L215 90L223 89L223 80L230 80L254 72L254 76L266 78L266 85L277 87L276 92L284 102L273 106L273 124L263 118z",
                        "M231 182L235 180L234 172L239 169L235 161L252 163L255 155L262 155L264 163L277 160L277 147L285 150L284 157L290 155L295 158L306 154L314 158L321 153L324 157L323 166L327 167L326 172L330 174L331 183L339 182L338 186L356 203L371 208L373 216L381 217L383 228L370 231L369 238L360 232L349 237L334 234L337 240L328 241L326 249L321 251L316 239L311 242L296 229L289 237L291 249L281 252L275 262L272 258L261 257L264 250L259 254L243 250L238 242L250 235L246 232L253 227L248 220L254 212L249 202L241 206L236 197L231 197L230 192L234 191z",
                        "M132 53L134 45L149 39L148 35L155 36L153 28L160 23L183 28L190 24L202 29L210 29L212 35L202 58L211 62L199 75L195 93L188 95L186 106L162 107L155 113L151 108L144 107L145 96L155 88L146 81L144 74L150 71L138 54L134 54L138 52z"),
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
