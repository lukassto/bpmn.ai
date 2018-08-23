package de.viadee.ki.sparkimporter.runner;

import de.viadee.ki.sparkimporter.processing.PreprocessingRunner;
import de.viadee.ki.sparkimporter.processing.steps.dataprocessing.*;
import de.viadee.ki.sparkimporter.processing.steps.importing.InitialCleanupStep;
import de.viadee.ki.sparkimporter.processing.steps.output.WriteToCSVStep;
import de.viadee.ki.sparkimporter.processing.steps.userconfig.DataFilterStep;
import de.viadee.ki.sparkimporter.processing.steps.userconfig.VariableFilterStep;
import de.viadee.ki.sparkimporter.processing.steps.userconfig.VariableNameMappingStep;
import de.viadee.ki.sparkimporter.runner.interfaces.ImportRunnerInterface;
import de.viadee.ki.sparkimporter.util.SparkImporterArguments;
import de.viadee.ki.sparkimporter.util.SparkImporterLogger;
import de.viadee.ki.sparkimporter.util.SparkImporterUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import static de.viadee.ki.sparkimporter.CSVImportAndProcessingApplication.ARGS;

public class CSVImportAndProcessingRunner implements ImportRunnerInterface {

    @Override
    public void run(SparkSession sparkSession) {

        final long startMillis = System.currentTimeMillis();

        SparkImporterLogger.getInstance().writeInfo("Starting CSV import and processing");
        SparkImporterLogger.getInstance().writeInfo("Importing CSV file: " + ARGS.getFileSource());

        //Load source CSV file
        Dataset<Row> dataset = sparkSession.read()
                .option("inferSchema", "true")
                .option("delimiter", ARGS.getDelimiter())
                .option("header", "true")
                .option("ignoreLeadingWhiteSpace", "false")
                .option("ignoreTrailingWhiteSpace", "false")
                .csv(ARGS.getFileSource());

        // write imported CSV structure to file for debugging
        if (SparkImporterArguments.getInstance().isWriteStepResultsToCSV()) {
            SparkImporterUtils.getInstance().writeDatasetToCSV(dataset, "import_result");
        }

        SparkImporterLogger.getInstance().writeInfo("Starting data processing");

        InitialCleanupStep initialCleanupStep = new InitialCleanupStep();
        dataset = initialCleanupStep.runPreprocessingStep(dataset, false);


        // Define processing steps to run
        final PreprocessingRunner preprocessingRunner = new PreprocessingRunner();
        PreprocessingRunner.writeStepResultsIntoFile = ARGS.isWriteStepResultsToCSV();

        //add steps
        // spark shows exception with correct result when this step is placed after data filter step, which would be better. therefore it stays here
        preprocessingRunner.addPreprocessorStep(new ReduceColumnsDatasetStep());

        // user configuration step
        preprocessingRunner.addPreprocessorStep(new DataFilterStep());

        // user configuration step
        preprocessingRunner.addPreprocessorStep(new VariableFilterStep());

        // user configuration step
        preprocessingRunner.addPreprocessorStep(new VariableNameMappingStep());

        preprocessingRunner.addPreprocessorStep(new GetVariablesTypesOccurenceStep());
        preprocessingRunner.addPreprocessorStep(new VariablesTypeEscalationStep());
        preprocessingRunner.addPreprocessorStep(new VariablesTypeEscalationStep());
        preprocessingRunner.addPreprocessorStep(new AggregateVariableUpdatesStep());
        preprocessingRunner.addPreprocessorStep(new AddVariablesColumnsStep());
        preprocessingRunner.addPreprocessorStep(new AggregateProcessInstancesStep());
        preprocessingRunner.addPreprocessorStep(new AddRemovedColumnsToDatasetStep());
        preprocessingRunner.addPreprocessorStep(new WriteToCSVStep());

        // Run processing runner
        preprocessingRunner.run(dataset);

        // Cleanup
        sparkSession.close();

        final long endMillis = System.currentTimeMillis();

        SparkImporterLogger.getInstance().writeInfo("CSV import and processing finished (took " + ((endMillis - startMillis) / 1000) + " seconds in total)");
    }
}
