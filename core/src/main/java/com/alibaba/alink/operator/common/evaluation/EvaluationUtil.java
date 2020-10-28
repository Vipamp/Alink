package com.alibaba.alink.operator.common.evaluation;

import com.alibaba.alink.common.utils.JsonConverter;
import com.alibaba.alink.operator.common.dataproc.SortUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.type.TypeReference;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import org.apache.flink.util.Preconditions;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import static com.alibaba.alink.operator.common.evaluation.ClassificationEvaluationUtil.BINARY_LABEL_NUMBER;
import static com.alibaba.alink.operator.common.evaluation.ClassificationEvaluationUtil.buildLabelIndexLabelArray;

/**
 * Provide some static variables and common used functions for evaluation.
 */
public class EvaluationUtil implements Serializable {
    private static double LOG_LOSS_EPS = 1e-15;
    private static double PROB_SUM_EPS = 0.01;

    public static boolean labelCompare(Object label1, Object label2, TypeInformation type){
        return SortUtils.OBJECT_COMPARATOR.compare(castTo(label1, type), castTo(label2, type)) == 0;
    }

    public static int compare(Object o1, Object o2) {
        if(o1 == null || o2 == null){
            return (o1 == null) && (o2 == null) ? 0 : 1;
        }
        if(o1 instanceof Comparable && o2 instanceof Comparable && o1.getClass() == o2.getClass()){
            return ((Comparable)o1).compareTo((Comparable)o2);
        }else {
            throw new RuntimeException("Input Labels are not comparable!");
        }
    }

    public static Object castTo(Object x, TypeInformation t) {
        if (x == null) {
            return null;
        } else if (t.equals(Types.BOOLEAN)) {
            if (x instanceof Boolean) {
                return x;
            }
            return Boolean.valueOf(x.toString());
        } else if (t.equals(Types.BYTE)) {
            if (x instanceof Number) {
                return ((Number)x).byteValue();
            }
            return Byte.valueOf(x.toString());
        } else if (t.equals(Types.SHORT)) {
            if (x instanceof Number) {
                return ((Number)x).shortValue();
            }
            return Short.valueOf(x.toString());
        } else if (t.equals(Types.INT)) {
            if (x instanceof Number) {
                return ((Number)x).intValue();
            }
            return Integer.valueOf(x.toString());
        } else if (t.equals(Types.LONG)) {
            if (x instanceof Number) {
                return ((Number)x).longValue();
            }
            return Long.valueOf(x.toString());
        } else if (t.equals(Types.FLOAT)) {
            if (x instanceof Number) {
                return ((Number)x).floatValue();
            }
            return Float.valueOf(x.toString());
        } else if (t.equals(Types.DOUBLE)) {
            if (x instanceof Number) {
                return ((Number)x).doubleValue();
            }
            return Double.valueOf(x.toString());
        } else if (t.equals(Types.STRING)) {
            if (x instanceof String) {
                return x;
            }
            return x.toString();
        } else {
            throw new RuntimeException("unsupported type: " + t.getClass().getName());
        }
    }

    /**
     * Initialize the base classification evaluation metrics. There are two cases: BinaryClassMetrics and
     * MultiClassMetrics.
     *
     * @param rows          The first two fields of the input rows must be label and prediction detail. The prediction
     *                      detail column must be json string like: "{\"prefix1\": 0.9, \"prefix0\": 0.1}".
     * @param binary        If binary is true, it indicates binary classification, so the total number of labels must be
     *                      2.
     * @param positiveValue It only works when binary is true.
     * @return If rows is empty, return null. If binary is true, return BinaryClassMetrics. If binary is false, return
     * MultiClassMetrics.
     */
    public static BaseMetricsSummary getDetailStatistics(Iterable<Row> rows, String positiveValue, boolean binary, TypeInformation labelType) {
        return getDetailStatistics(rows, positiveValue, binary, null, labelType);
    }

    /**
     * Initialize the base classification evaluation metrics. There are two cases: BinaryClassMetrics and
     * MultiClassMetrics.
     *
     * @param rows   The first two fields of the input rows must be label and prediction detail. The prediction detail
     *               column must be json string like: "{\"prefix1\": 0.9, \"prefix0\": 0.1}".
     * @param binary If binary is true, it indicates binary classification, so the the size of labels must be 2.
     * @param labels The key is the label, and the value is the probability. The value of label column must be included
     *               in the labels. It could be null and be extracted from the detail column; in this case, it must be
     *               guaranteed that the keys of all json strings are the same.
     * @return If rows is empty, return null. If binary is true, return BinaryClassMetrics. If binary is false, return
     * MultiClassMetrics.
     */
    public static BaseMetricsSummary getDetailStatistics(Iterable<Row> rows,
                                                         boolean binary,
                                                         Tuple2<Map<Object, Integer>, Object[]> labels,
                                                         TypeInformation labelType) {
        return getDetailStatistics(rows, null, binary, labels, labelType);
    }

    /**
     * Initialize the base classification evaluation metrics. There are two cases: BinaryClassMetrics and
     * MultiClassMetrics.
     *
     * @param rows          The first two fields of the input rows must be label and prediction detail. The prediction
     *                      detail column must be json string like: "{\"prefix1\": 0.9, \"prefix0\": 0.1}".
     * @param binary        If binary is true, it indicates binary classification, so the the size of labels must be 2.
     * @param positiveValue It only works when binary is true and labels is null. When labels is null, it's used to
     *                      extract labels from the detail column.
     * @param tuple         The key is the label, and the value is the id of the label. the value of label column must
     *                      be included in the labels. It could be null and be extracted from the detail column; in this
     *                      case, it must be guaranteed that the keys of all json strings are the same.
     * @return If rows is empty, return null. If binary is true, return BinaryClassMetrics. If binary is false, return
     * MultiClassMetrics.
     */
    private static BaseMetricsSummary getDetailStatistics(Iterable<Row> rows,
                                                          String positiveValue,
                                                          boolean binary,
                                                          Tuple2<Map<Object, Integer>, Object[]> tuple,
                                                          TypeInformation labelType) {
        BinaryMetricsSummary binaryMetricsSummary = null;
        MultiMetricsSummary multiMetricsSummary = null;
        Tuple2<Map<Object, Integer>, Object[]> labelIndexLabelArray = tuple;

        Iterator<Row> iterator = rows.iterator();
        Row row = null;
        while (iterator.hasNext() && !checkRowFieldNotNull(row)) {
            row = iterator.next();
        }
        if (checkRowFieldNotNull(row)) {
            if (null == labelIndexLabelArray) {
                TreeMap<Object, Double> labelProbMap = extractLabelProbMap(row, labelType);
                labelIndexLabelArray = buildLabelIndexLabelArray(new HashSet<>(labelProbMap.keySet()), binary,
                    positiveValue, labelType);
            }
        } else {
            return null;
        }

        Map<Object, Integer> labelIndexMap = null;
        if (binary) {
            binaryMetricsSummary = new BinaryMetricsSummary(
                new long[ClassificationEvaluationUtil.DETAIL_BIN_NUMBER],
                new long[ClassificationEvaluationUtil.DETAIL_BIN_NUMBER],
                labelIndexLabelArray.f1, 0.0, 0L);
        } else {
            labelIndexMap = labelIndexLabelArray.f0;
            multiMetricsSummary = new MultiMetricsSummary(
                new long[labelIndexMap.size()][labelIndexMap.size()],
                labelIndexLabelArray.f1, 0.0, 0L);
        }

        while (null != row) {
            if (checkRowFieldNotNull(row)) {
                TreeMap<Object, Double> labelProbMap = extractLabelProbMap(row, labelType);
                Object label = row.getField(0);
                if (ArrayUtils.indexOf(labelIndexLabelArray.f1, label) >= 0) {
                    if (binary) {
                        updateBinaryMetricsSummary(labelProbMap, label, binaryMetricsSummary);
                    } else {
                        updateMultiMetricsSummary(labelProbMap, label, labelIndexMap, multiMetricsSummary);
                    }
                }
            }
            row = iterator.hasNext() ? iterator.next() : null;
        }

        return binary ? binaryMetricsSummary : multiMetricsSummary;
    }

    public static boolean checkRowFieldNotNull(Row row) {
        return row != null && row.getField(0) != null && row.getField(1) != null;
    }

    public static double extractLogloss(TreeMap<Object, Double> labelProbMap, Object label) {
        Double prob = labelProbMap.get(label);
        prob = null == prob ? 0. : prob;
        return -Math.log(Math.max(Math.min(prob, 1 - LOG_LOSS_EPS), LOG_LOSS_EPS));
    }

    /**
     * Extract the |label, probability| map
     *
     * @param row Input row, the second field is predDetail.
     * @return the  |label, probability| map.
     */
    public static TreeMap<Object, Double> extractLabelProbMap(Row row, TypeInformation labelType) {
        HashMap<String, Double> labelProbMap;
        final String detailStr = row.getField(1).toString();
        try {
            labelProbMap = JsonConverter.fromJson(detailStr,
                new TypeReference<HashMap<String, Double>>() {}.getType());
        } catch (Exception e) {
            throw new RuntimeException(
                String.format("Fail to deserialize detail column %s!", detailStr));
        }
        Collection<Double> probabilities = labelProbMap.values();
        probabilities.forEach(v ->
            Preconditions.checkArgument(v <= 1.0 && v >= 0,
                String.format("Probibality in %s not in range [0, 1]!", detailStr)));
        Preconditions.checkArgument(
            Math.abs(probabilities.stream().mapToDouble(Double::doubleValue).sum() - 1.0) < PROB_SUM_EPS,
            String.format("Probability sum in %s not equal to 1.0!", detailStr));
        TreeMap<Object, Double> castLabelProbMap = new TreeMap<>();
        for(Map.Entry<String, Double> entry : labelProbMap.entrySet()){
            castLabelProbMap.put(castTo(entry.getKey(), labelType), entry.getValue());
        }
        return castLabelProbMap;
    }

    public static void updateBinaryMetricsSummary(TreeMap<Object, Double> labelProbMap,
                                                  Object label,
                                                  BinaryMetricsSummary binaryMetricsSummary) {
        Preconditions.checkState(labelProbMap.size() == BINARY_LABEL_NUMBER,
            "The number of labels must be equal to 2!");
        binaryMetricsSummary.total++;
        binaryMetricsSummary.logLoss += extractLogloss(labelProbMap, label);

        double d = labelProbMap.get(binaryMetricsSummary.labels[0]);
        int idx = d == 1.0 ? ClassificationEvaluationUtil.DETAIL_BIN_NUMBER - 1 :
            (int)Math.floor(d * ClassificationEvaluationUtil.DETAIL_BIN_NUMBER);
        if (idx >= 0 && idx < ClassificationEvaluationUtil.DETAIL_BIN_NUMBER) {
            if (label.equals(binaryMetricsSummary.labels[0])) {
                binaryMetricsSummary.positiveBin[idx] += 1;
            } else if (label.equals(binaryMetricsSummary.labels[1])) {
                binaryMetricsSummary.negativeBin[idx] += 1;
            }
        }
    }

    public static void updateMultiMetricsSummary(TreeMap<Object, Double> labelProbMap,
                                                 Object label,
                                                 Map<Object, Integer> labels,
                                                 MultiMetricsSummary multiMetricsSummary) {
        multiMetricsSummary.total++;
        multiMetricsSummary.logLoss += extractLogloss(labelProbMap, label);
        Object predict = null;
        double score = Double.NEGATIVE_INFINITY;
        for (Map.Entry<Object, Double> entry : labelProbMap.entrySet()) {
            Object key = entry.getKey();
            Double v = entry.getValue();

            if (v > score) {
                score = v;
                predict = key;
            }
        }
        int predictionIdx = labels.get(predict);
        int labelIdx = labels.get(label);
        multiMetricsSummary.matrix.setValue(predictionIdx, labelIdx,
            multiMetricsSummary.matrix.getValue(predictionIdx, labelIdx) + 1);
    }

    /**
     * Calculate the RegressionMetrics from local data.
     *
     * @param rows Input rows, the first field is label value, the second field is prediction value.
     * @return RegressionMetricsSummary.
     */
    public static RegressionMetricsSummary getRegressionStatistics(Iterable<Row> rows) {
        RegressionMetricsSummary regressionSummary = new RegressionMetricsSummary();
        for (Row row : rows) {
            if (checkRowFieldNotNull(row)) {
                double yVal = ((Number)row.getField(0)).doubleValue();
                double predictVal = ((Number)row.getField(1)).doubleValue();
                double diff = Math.abs(yVal - predictVal);
                regressionSummary.ySumLocal += yVal;
                regressionSummary.ySum2Local += yVal * yVal;
                regressionSummary.predSumLocal += predictVal;
                regressionSummary.predSum2Local += predictVal * predictVal;
                regressionSummary.maeLocal += diff;
                regressionSummary.sseLocal += diff * diff;
                regressionSummary.mapeLocal += Math.abs(diff / yVal);
                regressionSummary.total++;
            }
        }
        return regressionSummary.total == 0 ? null : regressionSummary;
    }

    /**
     * Build the confusion matrix from label and prediction. Initialize the MultiClassMetrics. The inputs rows contains
     * two columns: label and prediction. They must be included in the set of labels. If prediction detail is not given,
     * it's impossible to calculate the AUC, K-S and other metrics of binary classification. So binary label case is
     * processed the same as multi label case.
     *
     * @param rows                 Input rows.
     * @param labelIndexLabelArray Label index map, label array.
     * @return MultiMetricsSummary.
     */
    public static MultiMetricsSummary getMultiClassMetrics(Iterable<Row> rows,
                                                           Tuple2<Map<Object, Integer>, Object[]>
                                                               labelIndexLabelArray) {
        final Object[] recordLabel = labelIndexLabelArray.f1;
        final Map<Object, Integer> labelIndexMap = labelIndexLabelArray.f0;
        final int n = recordLabel.length;
        MultiMetricsSummary multiMetricsSummary = new MultiMetricsSummary(new long[n][n], recordLabel, -1, 0L);
        for (Row r : rows) {
            if (checkRowFieldNotNull(r)) {
                multiMetricsSummary.total++;
                int label = labelIndexMap.get(r.getField(0));
                int prediction = labelIndexMap.get(r.getField(1));
                multiMetricsSummary.matrix.setValue(prediction, label,
                    multiMetricsSummary.matrix.getValue(prediction, label) + 1);
            }
        }
        return multiMetricsSummary.total == 0 ? null : multiMetricsSummary;
    }

    /**
     * Merge the BaseMetrics calculated locally.
     */
    public static class ReduceBaseMetrics implements ReduceFunction<BaseMetricsSummary> {
        @Override
        public BaseMetricsSummary reduce(BaseMetricsSummary t1, BaseMetricsSummary t2) throws Exception {
            return null == t1 ? t2 : t1.merge(t2);
        }
    }

    /**
     * After merging all the BaseMetrics, we get the total BaseMetrics. Calculate the indexes and save them into
     * params.
     */
    public static class SaveDataAsParams implements FlatMapFunction<BaseMetricsSummary, Row> {
        @Override
        public void flatMap(BaseMetricsSummary t, Collector<Row> collector) throws Exception {
            Preconditions.checkNotNull(t, "Please check the evaluation input! there is no effective row!");
            collector.collect(t.toMetrics().serialize());
        }
    }

    /**
     * Get all the distinct labels from label column and prediction column, and return the map of labels and their IDs.
     */
    public static class DistinctLabelIndexMap implements
        GroupReduceFunction<Object, Tuple2<Map<Object, Integer>, Object[]>> {
        private boolean binary;
        private String positiveValue;
        private TypeInformation labelType;

        public DistinctLabelIndexMap(boolean binary, String positiveValue, TypeInformation labelType) {
            this.binary = binary;
            this.positiveValue = positiveValue;
            this.labelType = labelType;
        }

        @Override
        public void reduce(Iterable<Object> rows, Collector<Tuple2<Map<Object, Integer>, Object[]>> collector)
            throws Exception {
            HashSet<Object> labels = new HashSet<>();
            rows.forEach(labels::add);
            collector.collect(buildLabelIndexLabelArray(labels, binary, positiveValue, labelType));
        }
    }

    /**
     * Merge data from different windows.
     */
    public static class AllDataMerge implements MapFunction<BaseMetricsSummary, BaseMetricsSummary> {
        private BaseMetricsSummary statistics;

        @Override
        public BaseMetricsSummary map(BaseMetricsSummary value) {
            this.statistics = (null == this.statistics ? value : this.statistics.merge(value));
            return this.statistics;
        }
    }

    /**
     * Serialize the data.
     */
    public static class SaveDataStream implements MapFunction<BaseMetricsSummary, Row> {
        private String funtionName;

        public SaveDataStream(String funtionName) {
            this.funtionName = funtionName;
        }

        @Override
        public Row map(BaseMetricsSummary baseMetricsSummary) throws Exception {
            BaseMetricsSummary metrics = baseMetricsSummary;
            BaseMetrics baseMetrics = metrics.toMetrics();
            Row row = baseMetrics.serialize();
            return Row.of(funtionName, row.getField(0));
        }
    }
}
