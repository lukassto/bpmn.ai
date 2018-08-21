package de.viadee.ki.sparkimporter.processing.steps.dataprocessing;

import de.viadee.ki.sparkimporter.processing.interfaces.PreprocessingStepInterface;
import de.viadee.ki.sparkimporter.util.SparkImporterUtils;
import de.viadee.ki.sparkimporter.util.SparkImporterVariables;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import scala.collection.Seq;

import java.util.Arrays;

public class ReduceColumnsDatasetStep implements PreprocessingStepInterface {

    @Override
    public Dataset<Row> runPreprocessingStep(Dataset<Row> dataset, boolean writeStepResultIntoFile) {

        //TODO: refactor
        Seq<Column> selectionColumns = null;

        if(Arrays.asList(dataset.columns()).contains("timestamp_")) {
            dataset = dataset.select(SparkImporterVariables.VAR_PROCESS_INSTANCE_ID,
                    SparkImporterVariables.VAR_PROCESS_INSTANCE_VARIABLE_NAME,
                    SparkImporterVariables.VAR_PROCESS_INSTANCE_VARIABLE_TYPE,
                    SparkImporterVariables.VAR_PROCESS_INSTANCE_VARIABLE_REVISION,
                    "timestamp_",
                    SparkImporterVariables.VAR_STATE,
                    SparkImporterVariables.VAR_LONG,
                    SparkImporterVariables.VAR_DOUBLE,
                    SparkImporterVariables.VAR_TEXT,
                    SparkImporterVariables.VAR_TEXT2)
                    .filter(SparkImporterVariables.VAR_PROCESS_INSTANCE_ID + " <> 'null'");
        } else {
            dataset = dataset.select(SparkImporterVariables.VAR_PROCESS_INSTANCE_ID,
                    SparkImporterVariables.VAR_PROCESS_INSTANCE_VARIABLE_NAME,
                    SparkImporterVariables.VAR_PROCESS_INSTANCE_VARIABLE_TYPE,
                    SparkImporterVariables.VAR_PROCESS_INSTANCE_VARIABLE_REVISION,
                    SparkImporterVariables.VAR_STATE,
                    SparkImporterVariables.VAR_LONG,
                    SparkImporterVariables.VAR_DOUBLE,
                    SparkImporterVariables.VAR_TEXT,
                    SparkImporterVariables.VAR_TEXT2)
                    .filter(SparkImporterVariables.VAR_PROCESS_INSTANCE_ID + " <> 'null'");
        }

        if(writeStepResultIntoFile) {
            SparkImporterUtils.getInstance().writeDatasetToCSV(dataset, "reduced_columns");
        }

        //return preprocessed data
        return dataset;
    }
}
