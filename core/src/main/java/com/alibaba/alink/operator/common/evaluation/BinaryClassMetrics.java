package com.alibaba.alink.operator.common.evaluation;

import org.apache.commons.lang.ArrayUtils;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.ml.api.misc.param.ParamInfo;
import org.apache.flink.ml.api.misc.param.ParamInfoFactory;
import org.apache.flink.ml.api.misc.param.Params;
import org.apache.flink.types.Row;

import java.io.IOException;

/**
 * Binary classification evaluation metrics.
 */
public final class BinaryClassMetrics extends BaseSimpleClassifierMetrics<BinaryClassMetrics> {
    public BinaryClassMetrics(Row row) {
        super(row);
    }

    public BinaryClassMetrics(Params params) {
        super(params);
    }

    public static final ParamInfo<double[][]> ROC_CURVE = ParamInfoFactory
        .createParamInfo("RocCurve", double[][].class)
        .setDescription("auc")
        .setRequired()
        .build();
    public static final ParamInfo<Double> AUC = ParamInfoFactory
        .createParamInfo("AUC", Double.class)
        .setDescription("auc")
        .setRequired()
        .build();

    public static final ParamInfo<Double> GINI = ParamInfoFactory
        .createParamInfo("GINI", Double.class)
        .setDescription("GINI")
        .setRequired()
        .build();

    public static final ParamInfo<Double> KS = ParamInfoFactory
        .createParamInfo("K-S", Double.class)
        .setDescription("ks")
        .setRequired()
        .build();
    public static final ParamInfo<Double> PRC = ParamInfoFactory
        .createParamInfo("PRC", Double.class)
        .setDescription("ks")
        .setRequired()
        .build();
    public static final ParamInfo<double[][]> RECALL_PRECISION_CURVE = ParamInfoFactory
        .createParamInfo("RecallPrecisionCurve", double[][].class)
        .setDescription("recall precision curve")
        .setRequired()
        .build();
    public static final ParamInfo<double[][]> LIFT_CHART = ParamInfoFactory
        .createParamInfo("LiftChart", double[][].class)
        .setDescription("liftchart")
        .setRequired()
        .build();
    public static final ParamInfo<double[][]> LORENZ_CURVE = ParamInfoFactory
        .createParamInfo("LorenzCurve", double[][].class)
        .setDescription("lorenzCurve")
        .setRequired()
        .build();
    public static final ParamInfo<double[]> THRESHOLD_ARRAY = ParamInfoFactory
        .createParamInfo("ThresholdArray", double[].class)
        .setDescription("threshold list")
        .setRequired()
        .build();

    public static final ParamInfo<Double> PRECISION = ParamInfoFactory
        .createParamInfo("Precision", Double.class)
        .setDescription("precision")
        .setRequired()
        .build();

    public static final ParamInfo<Double> RECALL = ParamInfoFactory
        .createParamInfo("Recall", Double.class)
        .setDescription("recall")
        .setRequired()
        .build();

    public static final ParamInfo<Double> F1 = ParamInfoFactory
        .createParamInfo("F1", Double.class)
        .setDescription("f1")
        .setRequired()
        .build();

    public Tuple2<double[], double[]> getRocCurve() {
        double[][] curve = getParams().get(ROC_CURVE);
        return Tuple2.of(curve[0], curve[1]);
    }
    public Tuple2<double[], double[]> getLorenzeCurve() {
        double[][] curve = getParams().get(LORENZ_CURVE);
        return Tuple2.of(curve[0], curve[1]);
    }

    public Double getPrecision() {
        return get(PRECISION);
    }

    public Double getRecall() {
        return get(RECALL);
    }

    public Double getF1() {
        return get(F1);
    }

    public Double getAuc() {return get(AUC);}

    public Double getGini() {
        return get(GINI);
    }

    public Double getKs() {return get(KS);}

    public Double getPrc() {return get(PRC);}

    public Tuple2<double[], double[]> getRecallPrecisionCurve() {
        double[][] curve = getParams().get(RECALL_PRECISION_CURVE);
        return Tuple2.of(curve[0], curve[1]);
    }

    public Tuple2<double[], double[]> getLiftChart() {
        double[][] curve = getParams().get(LIFT_CHART);
        return Tuple2.of(curve[0], curve[1]);
    }

    public double[] getThresholdArray() {return get(THRESHOLD_ARRAY);}

    public Tuple2<double[], double[]> getPrecisionByThreshold() {
        return Tuple2.of(getParams().get(THRESHOLD_ARRAY), getParams().get(PRECISION_ARRAY));
    }

    public Tuple2<double[], double[]> getSpecificityByThreshold() {
        return Tuple2.of(getParams().get(THRESHOLD_ARRAY), getParams().get(SPECIFICITY_ARRAY));
    }

    public Tuple2<double[], double[]> getSensitivityByThreshold() {
        return Tuple2.of(getParams().get(THRESHOLD_ARRAY), getParams().get(SENSITIVITY_ARRAY));
    }

    public Tuple2<double[], double[]> getRecallByThreshold() {
        return Tuple2.of(getParams().get(THRESHOLD_ARRAY), getParams().get(RECALL_ARRAY));
    }

    public Tuple2<double[], double[]> getF1ByThreshold() {
        return Tuple2.of(getParams().get(THRESHOLD_ARRAY), getParams().get(F1_ARRAY));
    }

    public Tuple2<double[], double[]> getAccuracyByThreshold() {
        return Tuple2.of(getParams().get(THRESHOLD_ARRAY), getParams().get(ACCURACY_ARRAY));
    }

    public Tuple2<double[], double[]> getKappaByThreshold() {
        return Tuple2.of(getParams().get(THRESHOLD_ARRAY), getParams().get(KAPPA_ARRAY));
    }

    public void saveRocCurveAsImage(String path, boolean isOverwrite) throws IOException {
        VisualizationUtil.saveAsImage(path,
            isOverwrite,
            "ROC Curve",
            "FPR",
            "TPR",
            new String[]{"ROC"},
            Tuple2.of("AUC", getAuc()),
            getRocCurve());
    }

    public void saveKSAsImage(String path, boolean isOverwrite) throws IOException {
        double[] thresholdArray =getThresholdArray();
        double[] tprArray = getRocCurve().f1;
        double[] fprArray = getRocCurve().f0;
        ArrayUtils.reverse(thresholdArray);
        ArrayUtils.reverse(tprArray);
        ArrayUtils.reverse(fprArray);
        VisualizationUtil.saveAsImage(path,
            isOverwrite,
            "K-S Curve",
            "Thresholds",
            "Rate",
            new String[]{"TPR", "FPR"},
            Tuple2.of("KS", getKs()),
            Tuple2.of(thresholdArray, tprArray),
            Tuple2.of(thresholdArray, fprArray));
    }

    public void saveLiftChartAsImage(String path, boolean isOverwrite) throws IOException {
        VisualizationUtil.saveAsImage(path,
            isOverwrite,
            "LiftChart",
            "Positive Rate",
            "True Positive",
            new String[]{"LiftChart"},
            null,
            getLiftChart());
    }

    public void saveRecallPrecisionCurveAsImage(String path, boolean isOverwrite) throws IOException {
        VisualizationUtil.saveAsImage(path,
            isOverwrite,
            "RecallPrecisionCurve",
            "Precision",
            "Recall",
            new String[]{"RecallPrecision"},
            Tuple2.of("PRC", getPrc()),
            getRecallPrecisionCurve());
    }

    public void saveLorenzCurveAsImage(String path, boolean isOverwrite) throws IOException {
        VisualizationUtil.saveAsImage(path,
            isOverwrite,
            "LorenzCurve",
            "Positive Rate",
            "TPR",
            new String[]{"LorenzCurve"},
            Tuple2.of("GINI", getGini()),
            getLorenzeCurve());
    }
}
