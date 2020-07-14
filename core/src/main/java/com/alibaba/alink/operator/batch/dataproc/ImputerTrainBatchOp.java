package com.alibaba.alink.operator.batch.dataproc;

import com.alibaba.alink.common.MLEnvironmentFactory;
import com.alibaba.alink.common.utils.RowCollector;
import com.alibaba.alink.common.utils.TableUtil;
import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.common.dataproc.ImputerModelDataConverter;
import com.alibaba.alink.operator.common.statistics.StatisticsHelper;
import com.alibaba.alink.operator.common.statistics.basicstatistic.TableSummary;
import com.alibaba.alink.params.dataproc.ImputerTrainParams;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.ml.api.misc.param.Params;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;

/**
 * Imputer completes missing values in a dataSet, but only same type of columns can be selected at the same time.
 * Imputer Train will train a model for predict.
 * Strategy support min, max, mean or value.
 * If min, will replace missing value with min of the column.
 * If max, will replace missing value with max of the column.
 * If mean, will replace missing value with mean of the column.
 * If value, will replace missing value with the value.
 */
public class ImputerTrainBatchOp extends BatchOperator<ImputerTrainBatchOp>
        implements ImputerTrainParams<ImputerTrainBatchOp> {

    public ImputerTrainBatchOp() {
        super(null);
    }

    public ImputerTrainBatchOp(Params params) {
        super(params);
    }

    @Override
    public ImputerTrainBatchOp linkFrom(BatchOperator<?>... inputs) {
        BatchOperator<?> in = checkAndGetFirst(inputs);
        String[] selectedColNames = getSelectedCols();
        Strategy strategy = getStrategy();

        //result is statistic model with strategy.
        ImputerModelDataConverter converter = new ImputerModelDataConverter();
        converter.selectedColNames = selectedColNames;
        converter.selectedColTypes = TableUtil.findColTypesWithAssertAndHint(in.getSchema(), selectedColNames);

        //if strategy is not min, max, mean
        DataSet<Row> rows;
        if (isNeedStatModel()) {
            rows = StatisticsHelper.summary(in, selectedColNames)
                .flatMap(new BuildImputerModel(selectedColNames,
                        TableUtil.findColTypesWithAssertAndHint(in.getSchema(), selectedColNames), strategy));

        } else {
            if (!getParams().contains(ImputerTrainParams.FILL_VALUE)) {
                throw new RuntimeException("In VALUE strategy, the filling value is necessary.");
            }
            String fillValue = getFillValue();
            RowCollector collector = new RowCollector();
            converter.save(Tuple3.of(Strategy.VALUE, null, fillValue), collector);
            rows = MLEnvironmentFactory.get(getMLEnvironmentId()).getExecutionEnvironment().fromCollection(collector.getRows());
        }

        this.setOutput(rows, converter.getModelSchema());
        return this;
    }

    private boolean isNeedStatModel() {
        ImputerTrainParams.Strategy strategy = getStrategy();
        if (Strategy.MIN.equals(strategy) || Strategy.MAX.equals(strategy) || Strategy.MEAN.equals(strategy)) {
            return true;
        } else if (Strategy.VALUE.equals(strategy)){
            return false;
        } else {
            throw new IllegalArgumentException("Only support \"MAX\", \"MEAN\", \"MIN\" and \"VALUE\" strategy.");
        }
    }


    /**
     * table summary build model.
     */
    public static class BuildImputerModel implements FlatMapFunction<TableSummary, Row> {
        private String[] selectedColNames;
        private TypeInformation[] selectedColTypes;
        private Strategy strategy;

        public BuildImputerModel(String[] selectedColNames, TypeInformation[] selectedColTypes, Strategy strategy) {
            this.selectedColNames = selectedColNames;
            this.selectedColTypes = selectedColTypes;
            this.strategy = strategy;
        }

        @Override
        public void flatMap(TableSummary srt, Collector<Row> collector) throws Exception {
            if (null != srt) {
                ImputerModelDataConverter converter = new ImputerModelDataConverter();
                converter.selectedColNames = selectedColNames;
                converter.selectedColTypes = selectedColTypes;

                converter.save(new Tuple3<>(strategy, srt, ""), collector);
            }
        }
    }

}