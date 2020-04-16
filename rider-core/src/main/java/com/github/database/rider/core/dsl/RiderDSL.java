package com.github.database.rider.core.dsl;

import com.github.database.rider.core.configuration.DBUnitConfig;
import com.github.database.rider.core.configuration.DataSetConfig;
import com.github.database.rider.core.connection.ConnectionHolderImpl;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;

import java.sql.Connection;

/**
 * @author rmpestano
 * @since 1.13.0
 * DSL for populating database which is an alternative to `@DataSet` annotation
 */
public class RiderDSL {

    private static RiderDSL INSTANCE;
    private Connection connection;
    private DataSetConfig dataSetConfig;
    private DBUnitConfig dbUnitConfig;


    /**
     * Creates a dataset in database using the provided connection ({@link Connection}), dataset configuration ({@link DataSetConfig}
     * and dbunit configuration ({@link DBUnitConfig})
     */
    public void createDataSet() {
        validateConnection(connection);
        validateDataSetConfig(dataSetConfig);
        final DataSetExecutorImpl dataSetExecutor = DataSetExecutorImpl.instance(dataSetConfig.getExecutorId(), new ConnectionHolderImpl(connection));
        if (dbUnitConfig != null) {
            dataSetExecutor.setDBUnitConfig(dbUnitConfig);
        }
        dataSetExecutor.createDataSet(dataSetConfig);
    }

    private static void createInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RiderDSL();
        }
    }

    /**
     * Configures the DSL with provided JDBC connection
     *
     * @param connection jdbc connection to be used when populating the database
     * @return
     */
    public static DataSetConfigDSL withConnection(Connection connection) {
        createInstance();
        validateConnection(connection);
        INSTANCE.connection = connection;
        return new DataSetConfigDSL();
    }

    /**
     * Reuses current connection configured in the DSL
     *
     * @return DataSet config DSL
     */
    public static DataSetConfigDSL withConnection() {
        return withConnection(INSTANCE.connection);
    }

    public static class DataSetConfigDSL {

        /**
         * Configures the DSL with provided DataSet configuration
         *
         * @param dataSetConfig {@link DataSetConfig}
         * @return A DBUnitConfigDSL to create DBUnit configuration ({@link DBUnitConfig}
         */
        public static DBUnitConfigDSL withDataSet(DataSetConfig dataSetConfig) {
            validateDataSetConfig(dataSetConfig);
            INSTANCE.dataSetConfig = dataSetConfig;
            return new DBUnitConfigDSL();
        }

        /**
         * Reuses dataset configuration already provided to the DSL
         *
         * @return A DBUnitConfigDSL to create DBUnit configuration ({@link DBUnitConfig}
         */
        public static DBUnitConfigDSL withDataSet() {
            return withDataSet(INSTANCE.dataSetConfig);
        }

    }

    public static class DBUnitConfigDSL {

        /**
         * Configures the DSL with provided DBUnit configuration
         *
         * @param dbUnitConfig {@link DBUnitConfig}
         * @return
         */
        public static RiderDSL withDBUnitConfig(DBUnitConfig dbUnitConfig) {
            INSTANCE.dbUnitConfig = dbUnitConfig;
            return INSTANCE;
        }

        /**
         * Creates a dataset in database using the provided connection ({@link Connection}), dataset configuration ({@link DataSetConfig}
         * and dbunit configuration ({@link DBUnitConfig})
         */
        public static void createDataSet() {
            INSTANCE.createDataSet();
        }
    }

    private static void validateConnection(Connection connection) {
        if (connection == null) {
            throw new RuntimeException("Invalid jdbc connection.");
        }
    }

    private static void validateDataSetConfig(DataSetConfig dataSetConfig) {
        if (dataSetConfig == null || (!dataSetConfig.hasDataSets() && !dataSetConfig.hasDataSetProvider())) {
            throw new RuntimeException("Invalid dataset configuration. You must provide at least one dataset or dataset provider.");
        }
    }

}
