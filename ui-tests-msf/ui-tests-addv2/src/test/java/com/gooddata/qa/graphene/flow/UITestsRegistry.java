package com.gooddata.qa.graphene.flow;

import com.gooddata.qa.graphene.bigquery.CleanUpBigQueryTest;
import com.gooddata.qa.graphene.postgre.PostGreCurrentLoadTest;
import com.gooddata.qa.graphene.postgre.PostGreSegmentLoadTest;
import com.gooddata.qa.graphene.redshift.CleanUpADSInstanceTest;
import com.gooddata.qa.graphene.redshift.CleanUpDatasourceTest;
import com.gooddata.qa.graphene.redshift.CleanUpRedShiftTest;
import com.gooddata.qa.graphene.snowflake.DeployProcessTest;
import com.gooddata.qa.graphene.snowflake.SegmentDeleteByLabelTest;
import com.gooddata.qa.graphene.snowflake.SegmentDeleteColumnFactTableGrainTest;
import com.gooddata.qa.graphene.snowflake.SegmentDeleteColumnForceLoadTest;
import com.gooddata.qa.graphene.snowflake.SegmentDeleteColumnLoadTest;
import com.gooddata.qa.graphene.snowflake.SegmentDeleteTableForceLoadTest;
import com.gooddata.qa.graphene.snowflake.SegmentDeleteTableLoadTest;
import com.gooddata.qa.graphene.snowflake.SegmentForceLoadTest;
import com.gooddata.qa.graphene.snowflake.SegmentLoadTest;
import com.gooddata.qa.graphene.snowflake.CustomCurrentLoadTest;
import com.gooddata.qa.graphene.snowflake.CleanUpSnowflakeTest;
import com.gooddata.qa.graphene.redshift.RedShiftCurrentLoadTest;
import com.gooddata.qa.graphene.redshift.RedShiftSegmentLoadTest;
import com.gooddata.qa.graphene.bigquery.BigQueryCurrentLoadTest;
import com.gooddata.qa.graphene.bigquery.BigQuerySegmentLoadTest;
import com.gooddata.qa.graphene.snowflake.CustomCurrentForceLoadTest;
import com.gooddata.qa.graphene.snowflake.CustomSegmentMappingBothIdTest;
import com.gooddata.qa.graphene.snowflake.CustomSegmentMappingClientIdTest;
import com.gooddata.qa.graphene.snowflake.CustomSegmentMappingProjectIdTest;
import com.gooddata.qa.graphene.snowflake.ValidateDatasourceSnowflakeTest;
import com.gooddata.qa.graphene.redshift.ValidateDatasourceRedshiftTest;
import com.gooddata.qa.graphene.bigquery.ValidateDatasourceBigQueryTest;

import com.gooddata.qa.utils.flow.TestsRegistry;

import java.util.HashMap;
import java.util.Map;

public class UITestsRegistry {

    public static void main(String[] args) throws Throwable {
        Map<String, Object> suites = new HashMap<>();

        suites.put("all", new Object[] {
                SegmentForceLoadTest.class,
                SegmentLoadTest.class,
                SegmentDeleteByLabelTest.class,
                SegmentDeleteColumnFactTableGrainTest.class,
                SegmentDeleteColumnForceLoadTest.class,
                SegmentDeleteColumnLoadTest.class,
                SegmentDeleteTableForceLoadTest.class,
                SegmentDeleteTableLoadTest.class,
                CustomCurrentLoadTest.class,
                CustomCurrentForceLoadTest.class,
                CustomSegmentMappingProjectIdTest.class,
                CustomSegmentMappingClientIdTest.class,
                CustomSegmentMappingBothIdTest.class,
                RedShiftCurrentLoadTest.class,
                RedShiftSegmentLoadTest.class,
                BigQueryCurrentLoadTest.class,
                BigQuerySegmentLoadTest.class,
//                PostGreCurrentLoadTest.class,
//                PostGreSegmentLoadTest.class,
                CleanUpSnowflakeTest.class,
                CleanUpBigQueryTest.class,
                CleanUpRedShiftTest.class,
                CleanUpDatasourceTest.class,
                CleanUpADSInstanceTest.class
        });

        TestsRegistry.getInstance()
            .register(args, suites)
            .toTextFile();
    }
}
