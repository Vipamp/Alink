package com.alibaba.alink.pipeline.clustering;

import com.alibaba.alink.common.MLEnvironmentFactory;
import com.alibaba.alink.common.utils.DataStreamConversionUtil;
import com.alibaba.alink.common.utils.httpsrc.Iris;
import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.clustering.BisectingKMeansTrainBatchOp;
import com.alibaba.alink.operator.batch.clustering.KMeansTrainBatchOp;
import com.alibaba.alink.operator.batch.evaluation.EvalClusterBatchOp;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import com.alibaba.alink.operator.common.clustering.BisectingKMeansModelInfoBatchOp;
import com.alibaba.alink.operator.common.clustering.kmeans.KMeansModelInfoBatchOp;
import com.alibaba.alink.operator.common.evaluation.ClusterMetrics;
import com.alibaba.alink.pipeline.Pipeline;
import com.alibaba.alink.pipeline.PipelineModel;
import com.alibaba.alink.pipeline.dataproc.vector.VectorAssembler;
import org.apache.flink.table.api.Table;
import org.apache.flink.types.Row;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class BisectingKMeansTest {
	private static Row[] rows = new Row[] {
		Row.of("0  0  0") ,
		Row.of("0.1  0.1  0.1") ,
		Row.of("0.2  0.2  0.2") ,
		Row.of("9  9  9") ,
		Row.of("9.1  9.1  9.1") ,
		Row.of("9.2  9.2  9.2") ,
	};

	@Test
	public void test() throws Exception {
		Table data = MLEnvironmentFactory.getDefault().createBatchTable(rows, new String[] {"vector"});
		Table dataStream = MLEnvironmentFactory.getDefault().createStreamTable(rows, new String[] {"vector"});

		BisectingKMeans bisectingKMeans = new BisectingKMeans()
			.setVectorCol("vector")
			.setPredictionCol("pred")
			.setK(3)
			.setMaxIter(10)
			.enableLazyPrintModelInfo();

		PipelineModel model = new Pipeline().add(bisectingKMeans).fit(data);

		Table res = model.transform(data);

		List<Long> list = MLEnvironmentFactory.getDefault().getBatchTableEnvironment().toDataSet(
			res.select("pred"), Long.class).collect();

		Assert.assertArrayEquals(list.toArray(new Long[0]), new Long[]{0L, 0L, 0L, 1L, 2L, 2L});
		res = model.transform(dataStream);

		DataStreamConversionUtil.fromTable(MLEnvironmentFactory.DEFAULT_ML_ENVIRONMENT_ID, res).print();

		MLEnvironmentFactory.getDefault().getStreamExecutionEnvironment().execute();
	}

	@Test
	public void iris() {
		VectorAssembler va = new VectorAssembler().setSelectedCols(Iris.getFeatureColNames())
			.setReservedCols(Iris.getLabelColName()).setOutputCol("features");

		BisectingKMeans bikmeans = new BisectingKMeans()
			.setK(3)
			.setMaxIter(100)
			.setVectorCol("features")
			.setReservedCols(Iris.getLabelColName())
			.setPredictionCol("pred");

		Pipeline pipeline = new Pipeline().add(va).add(bikmeans);
		PipelineModel model = pipeline.fit(Iris.getBatchData());

		ClusterMetrics metrics = new EvalClusterBatchOp()
			.setPredictionCol("pred")
			.setLabelCol(Iris.getLabelColName())
			.linkFrom(model.transform(Iris.getBatchData()))
			.collectMetrics();

		Assert.assertEquals(metrics.getAri(), 0.68, 0.01);
		Assert.assertEquals(metrics.getNmi(), 0.69, 0.01);
		Assert.assertEquals(metrics.getRi(), 0.85, 0.01);
	}

	@Test
	public void testLazy() throws Exception{
		MemSourceBatchOp source = new MemSourceBatchOp(Arrays.asList(rows), new String[]{"vec"});

		BisectingKMeansTrainBatchOp bisectingKMeans = new BisectingKMeansTrainBatchOp()
			.setVectorCol("vec")
			.setK(3)
			.setMaxIter(10)
			.linkFrom(source);

		bisectingKMeans.lazyCollectModelInfo(
			new Consumer<BisectingKMeansModelInfoBatchOp.BisectingKMeansModelInfo>() {
				@Override
				public void accept(BisectingKMeansModelInfoBatchOp.BisectingKMeansModelInfo bisectingKMeansModelInfo) {
					Assert.assertEquals(bisectingKMeansModelInfo.getClusterNumber(), 3);
				}
			});

		bisectingKMeans.lazyPrintModelInfo();

		BatchOperator.execute();
	}
}
