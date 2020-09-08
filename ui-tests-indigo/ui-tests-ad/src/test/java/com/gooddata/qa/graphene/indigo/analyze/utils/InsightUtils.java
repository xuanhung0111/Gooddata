package com.gooddata.qa.graphene.indigo.analyze.utils;

import com.gooddata.qa.graphene.entity.visualization.CategoryBucket.Type;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.sdk.model.md.Attribute;
import com.gooddata.sdk.model.md.Metric;
import com.gooddata.sdk.model.project.Project;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gooddata.qa.graphene.enums.indigo.ReportType.*;
import static com.gooddata.sdk.model.md.Restriction.title;
import static java.util.Collections.singletonList;

public class InsightUtils {
    private IndigoRestRequest indigoRestRequest ;
    private RestClient restClient;
    private Project project;

    public InsightUtils(RestClient client, String projectId) {
        indigoRestRequest = new IndigoRestRequest(client, projectId);
        restClient = client;
        project = restClient.getProjectService().getProjectById(projectId);
    }

    public String createSimpleComboInsight(String title,
                                           List<String> metrics, List<String> secondMetrics, String attribute) {
        return createComboInsight(title, COMBO_CHART, metrics, secondMetrics, attribute);
    }

    public String createSimpleColumnInsight(String title,
                                            List<String> metrics, List<String> attributes, String stack) {
        return createInsight(title, COLUMN_CHART, metrics, attributes, stack, Type.VIEW, Type.STACK);
    }

    public String createSimpleHeatMapInsight(String title,
                                             List<String> metrics, List<String> attributes, String stack) {
        return createInsight(title, HEAT_MAP, metrics, attributes, stack, Type.VIEW, Type.STACK);
    }

    public String createSimplePivotTableInsight(String title,
                                                List<String> metrics, List<String> attributes, String stack) {
        return createInsight(title, TABLE, metrics, attributes, stack, Type.ATTRIBUTE, Type.COLUMNS);
    }

    public String createSimpleTreeMapInsight(String title,
                                             List<String> metrics, List<String> attributes, String stack) {
        return createInsight(title, TREE_MAP, metrics, attributes, stack, Type.VIEW, Type.SEGMENT);
    }

    private String createComboInsight(String title, ReportType reportType,
                                        List<String> metrics, List<String> secondaryMetrics, String attribute) {
        InsightMDConfiguration insightMDConfiguration = new InsightMDConfiguration(title, reportType);
        List<MeasureBucket> measureBuckets = metrics.stream()
                .map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))
                .collect(Collectors.toList());

        List<MeasureBucket> secondaryMeasureBuckets = secondaryMetrics.stream()
                .map(metric -> MeasureBucket.createMeasureBucket(
                        getMetricByTitle(metric), MeasureBucket.Type.SECONDARY_MEASURES))
                .collect(Collectors.toList());
        insightMDConfiguration.setMeasureBucket(Stream.concat(measureBuckets.stream(), secondaryMeasureBuckets.stream())
                .collect(Collectors.toList()));

        CategoryBucket categoryBucket = CategoryBucket.createCategoryBucket(
                getAttributeByTitle(attribute), Type.VIEW);
        insightMDConfiguration.setCategoryBucket(singletonList(categoryBucket));

        return indigoRestRequest.createInsight(insightMDConfiguration);
    }

    private String createInsight(String title, ReportType reportType,
                                   List<String> metrics, List<String> attributes, String stack, Type attributeType, Type stackType) {
        InsightMDConfiguration insightMDConfiguration = new InsightMDConfiguration(title, reportType);
        List<CategoryBucket> categoryBuckets;
        List<MeasureBucket> measureBuckets = metrics.stream()
                .map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))
                .collect(Collectors.toList());
        insightMDConfiguration.setMeasureBucket(measureBuckets);

        categoryBuckets = attributes.stream()
                .map(attribute -> CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute), attributeType))
                .collect(Collectors.toList());

        if (StringUtils.isNotEmpty(stack)) {
            categoryBuckets.add(CategoryBucket.createCategoryBucket(getAttributeByTitle(stack), stackType));
        }
        insightMDConfiguration.setCategoryBucket(categoryBuckets);
        return indigoRestRequest.createInsight(insightMDConfiguration);
    }

    private Metric getMetricByTitle(String title) {
        return restClient.getMetadataService().getObj(project, Metric.class, title(title));
    }

    private Attribute getAttributeByTitle(String title) {
        return restClient.getMetadataService().getObj(project, Attribute.class, title(title));
    }
}
