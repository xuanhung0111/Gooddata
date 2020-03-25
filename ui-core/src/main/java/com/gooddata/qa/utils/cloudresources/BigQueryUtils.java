package com.gooddata.qa.utils.cloudresources;

import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.*;
import com.google.cloud.bigquery.JobStatistics.LoadStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.stream.Collectors.joining;

public class BigQueryUtils {
    private static final Logger log = LoggerFactory.getLogger(BigQueryUtils.class);
    protected static BigQuery bigQueryClient;
    private static ConnectionInfo bigqueryConnectionInfo;

    public BigQueryUtils(ConnectionInfo connectionInfo) {
        log.info("Creating BigQuery Client .......");
        bigqueryConnectionInfo = connectionInfo;
        bigQueryClient = createBigQueryClient(bigqueryConnectionInfo);
    }

    public static BigQuery createBigQueryClient(ConnectionInfo connectionInfo) {
        log.info("Creating BigQuery Client .......");
        BigQuery bigQuery = null;
        try {
            GoogleCredentials googleCredentials = ServiceAccountCredentials.fromPkcs8(null, connectionInfo.getClientEmail(), StringEscapeUtils.unescapeJson(connectionInfo.getPrivateKey()), null, null);
            bigQuery = BigQueryOptions.newBuilder().setProjectId(connectionInfo.getProject()).setCredentials(googleCredentials).build().getService();
            log.info("BigQuery Client create successfully ");
        } catch (Exception e) {
            log.error("Cannot create BigQuery Client " + e.getMessage());
            throw new RuntimeException();
        }
        return bigQuery;
    }

    public static Dataset createDataset(String datasetName) {
        Dataset dataset;
        try {
            DatasetInfo datasetInfo = DatasetInfo.newBuilder(datasetName).build();
            dataset = bigQueryClient.create(datasetInfo);
        } catch (Exception e) {
            log.error("Create dataset failed " + e.getMessage());
            throw new RuntimeException();
        }
        log.info("Dataset create successfully : " + dataset);
        return dataset;
    }

    /**
     * Creating a table.
     */
    public Table createTable(String datasetName, String tableName, String fieldName) {
        TableId tableId = TableId.of(datasetName, tableName);
        // Table field definition
        Field field = Field.of(fieldName, LegacySQLTypeName.STRING);
        // Table schema definition
        Schema schema = Schema.of(field);
        TableDefinition tableDefinition = StandardTableDefinition.of(schema);
        TableInfo tableInfo = TableInfo.newBuilder(tableId, tableDefinition).build();
        Table table = null;
        try {
            table = bigQueryClient.create(tableInfo);
        } catch (Exception e) {
            log.error("Cannot create table " + e.getMessage());
            throw new RuntimeException();
        }
        return table;
    }

    public static Table createTable(String datasetName, String tableName) {
        TableId tableId = TableId.of(datasetName, tableName);
        // create table
        Schema schema = Schema.of(Collections.EMPTY_LIST);
        TableDefinition tableDefinition = StandardTableDefinition.of(schema);
        TableInfo tableInfo = TableInfo.newBuilder(tableId, tableDefinition).build();
        Table table = null;
        try {
            table = bigQueryClient.create(tableInfo);
        } catch (Exception e) {
            log.error("Cannot create table " + e.getMessage());
            throw new RuntimeException();
        }
        return table;
    }

    public static void deleteDataset(String datasetName) {
        try {
            bigQueryClient.delete(datasetName, BigQuery.DatasetDeleteOption.deleteContents());
        } catch (Exception e) {
            log.error("Cannot delete dataset " + e.getMessage());
            throw new RuntimeException();
        }
    }

    public static Table createTable(String datasetName, String tableName, List<Field> fields) {
        Table table = null;
        try {
            TableId tableId = TableId.of(datasetName, tableName);

            // delete table first
            bigQueryClient.delete(tableId);   // ignore exception because table is not exist yet

            // create table
            Schema schema = Schema.of(fields);
            TableDefinition tableDefinition = StandardTableDefinition.of(schema);
            TableInfo tableInfo = TableInfo.newBuilder(tableId, tableDefinition).build();
            table = bigQueryClient.create(tableInfo);
        } catch (Exception e) {
            log.error("Cannot create table " + e.getMessage());
            throw new RuntimeException();
        }
        log.info("table create successfully " + table);
        return table;
    }

    public static void deleteTable(String datasetName, String tableName) {
        TableId tableId = TableId.of(datasetName, tableName);
        try {
            bigQueryClient.delete(tableId);
            log.info("Dropped table " + tableId);
        } catch (Exception e) {
            log.error("Cannot delete table  " + e.getMessage());
            throw new RuntimeException();
        }
    }

    public static void executeSql(String strSql) {
        try {
            Job job = bigQueryClient.create(JobInfo.of(QueryJobConfiguration.newBuilder(strSql).setDefaultDataset(bigqueryConnectionInfo.schema).build()));
            job.waitFor();
        } catch (Exception e) {
            log.error("Cannot execute SQL " + e.getMessage());
            throw new RuntimeException();
        }
    }

    public static long writeFileToTable(String datasetName, String tableName, List<Field> fields, Path csvPath)
            throws IOException {
        TableId tableId = TableId.of(datasetName, tableName);
        Schema schema = Schema.of(fields);
        WriteChannelConfiguration writeChannelConfiguration = WriteChannelConfiguration.newBuilder(tableId)
                .setFormatOptions(CsvOptions.newBuilder().setSkipLeadingRows(1).build())
                .setSchema(schema)
                .setWriteDisposition(JobInfo.WriteDisposition.WRITE_APPEND)
                .build();
        // The location must be specified; other fields can be auto-detected.
        JobId jobId = JobId.newBuilder().setLocation("us").build();
        TableDataWriteChannel writer = bigQueryClient.writer(jobId, writeChannelConfiguration);
        // Write data to writer
        OutputStream stream = null;
        try {
            stream = Channels.newOutputStream(writer);
            log.info("stream : " + stream);
            Files.copy(csvPath, stream);
        } catch (Exception e) {
            throw new IllegalStateException("Fail to write file to BigQuery table", e);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        // Get load job
        Job job = getJob(writer);
        if (job.getStatus().getError() != null) {
            throw new IllegalStateException("Fail to write file to BigQuery table. Reason: " + job.getStatus().getExecutionErrors().get(1).getMessage());
        }
        LoadStatistics stats = job.getStatistics();
        return stats.getOutputRows();
    }

    private static Job getJob(TableDataWriteChannel writeChannel) {
        Job job = null;
        try {
            job = writeChannel.getJob().waitFor();
        } catch (Exception e) {
            log.error("Cannot create job " + e.getMessage());
            throw new RuntimeException();
        }
        return job;
    }

    /**
     * Get result by Redshift condition .
     *
     * @param //tableName     redshift Table name.
     * @param //column        redshift column name.
     * @param //andConditions condition after where clause. (example: column = "Column 1" , condition = " = 1 ")
     * @param //limit         : limit records returns
     * @return ResultSet apply querry
     */
    public ArrayList<String> getRecordsByCondition(String dataset, String tableName, String column, List<Pair<String, String>> andConditions,
                                                   List<Pair<String, String>> orConditions, int limit) {
        String andConditionString = andConditions != null
                ? " ( " + andConditions.stream().map(this::getCondition).collect(joining(" AND ")) + " ) "
                : "";
        String orConditionString = orConditions != null
                ? " ( " + orConditions.stream().map(this::getCondition).collect(joining(" OR ")) + " ) "
                : "";
        StringBuilder builder = new StringBuilder();

        String sqlStr = builder.append("SELECT ${column} FROM ${dataset}.${tableName} WHERE ")
                .append(andConditionString)
                .append((andConditions != null && orConditions != null) ? " AND " : "")
                .append(orConditionString)
                .append(" LIMIT " + limit)
                .toString()
                .replace("${dataset}", dataset)
                .replace("${tableName}", tableName)
                .replace("${column}", column);
        return getArrayResult(sqlStr, column);
    }

    private String getCondition(Pair<String, String> condition) {
        return String.format("(%s %s)", condition.getLeft(), condition.getRight());
    }

    /**
     * Execute SQL statement then return SQL result.
     *
     * @param sqlStr SQL command or a script (multiple colon-separated commands) to execute.
     * @return SQL result
     */
    public ArrayList<String> getArrayResult(String sqlStr, String column) {
        ArrayList<String> resultArray = new ArrayList<String>();
        try {
            QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(sqlStr).build();
            for (FieldValueList row : bigQueryClient.query(queryConfig).iterateAll()) {
                resultArray.add(row.get(column).getValue().toString());
            }
        } catch (Exception e) {
            e.getMessage();
            throw new RuntimeException();
        }
        return resultArray;
    }

    public List<Dataset> getOldBigQuerySchemas(int retentionDays) throws ParseException {
        List<Dataset> listSchema = new ArrayList<>();
        Page<Dataset> schemas;

        try {
            schemas = bigQueryClient.listDatasets(BigQuery.DatasetListOption.pageSize(50));
        } catch (Exception e) {
            throw new RuntimeException("Embed menu is not visible - " + e.getMessage());
        }

        for (Dataset dataset : schemas.getValues()) {
            // filter schemas are created by script test
            if (dataset.getGeneratedId().contains("customersmappingprojectid")) {
                log.info("dataset.getGeneratedId() : " + dataset.getGeneratedId());
                String date = dataset.getGeneratedId().replace("gdc-us-dev:customersmappingprojectid2_", "");
                // list old schemas
                log.info("Retention Date : " + getRetentionDate(retentionDays));
                log.info("Schema Created Date : " + convertStringToDate(date));
                if (compareTwoDates(convertStringToDate(date), getRetentionDate(retentionDays))) {
                    listSchema.add(dataset);
                }
            }
        }
        return listSchema;
    }

    public void deleteTablesInSelectedSchema(Dataset dataset) {
        Page<Table> tables = bigQueryClient.listTables(dataset.getDatasetId());
        for (Table table : tables.getValues()) {
            String tableName = table.getGeneratedId().replace(dataset.getGeneratedId() + ".", "");
            log.info("tableName : " + tableName);
            deleteTable(dataset.getGeneratedId().replace("gdc-us-dev:", ""), tableName);
        }
    }

    public void dropSchemaIfExists(String datasetName) {
        String schemaName = datasetName.replace("gdc-us-dev:", "");
        log.info("datasetName : " + schemaName);
        deleteDataset(schemaName);
    }

    private Date convertStringToDate(String value) throws ParseException {
        return new SimpleDateFormat("YYYY_MM_dd_HH_mm_ss").parse(value);
    }

    private Date getRetentionDate(int number) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.add(Calendar.DATE, -number);
        return cal.getTime();
    }

    private boolean compareTwoDates(Date firstDate, Date secondDate) {
        return firstDate.compareTo(secondDate) > 0;
    }
}
