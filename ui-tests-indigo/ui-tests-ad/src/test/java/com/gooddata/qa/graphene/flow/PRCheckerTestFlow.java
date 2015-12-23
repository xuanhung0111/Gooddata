package com.gooddata.qa.graphene.flow;

import java.io.IOException;

import com.gooddata.qa.graphene.indigo.analyze.e2e.AttributeBasedMetricsTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.AttributeFiltersTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.BucketsTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.CsvUploaderTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.DropZonesTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.EmptyCatalogueTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.ErrorStatesTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.FactBasedMetricsTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.RecommendationsWithoutDateDimensionTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.ResetButtonTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.StackedChartsTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.TableTest;
import com.gooddata.qa.graphene.indigo.analyze.e2e.UndoTest;
import com.gooddata.qa.utils.flow.TestsRegistry;

public class PRCheckerTestFlow {

    public static void main(String[] args) throws IOException {
        TestsRegistry.getInstance()
            .register(EmptyCatalogueTest.class)
            .register(CsvUploaderTest.class)
            .register(RecommendationsWithoutDateDimensionTest.class)
            .register(AttributeFiltersTest.class)
            .register(BucketsTest.class)
            .register(DropZonesTest.class)
            .register(ErrorStatesTest.class)
            .register(TableTest.class)
            .register(UndoTest.class)
            .register(AttributeBasedMetricsTest.class)
            .register(FactBasedMetricsTest.class)
            .register(StackedChartsTest.class)
            .register(ResetButtonTest.class)
            .register("testng-ad-e2e-metrics-test.xml")
            .register("testng-ad-e2e-visualization-test.xml")
            .register("testng-ad-e2e-recommendation-test.xml")
            .register("testng-ad-e2e-catalogue-test.xml")
            .register("testng-ad-e2e-date-test.xml")
            .toTextFile();
    }
}
