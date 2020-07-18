from pyalink.alink import *

if __name__ == '__main__':
    useLocalEnv(1, flinkHome=None, config=None)
    source = CsvSourceBatchOp() \
        .setSchemaStr(
        "sepal_length double, sepal_width double, petal_length double, petal_width double, category string") \
        .setFilePath("https://alink-release.oss-cn-beijing.aliyuncs.com/data-files/iris.csv")
    res = source.select(["sepal_length", "sepal_width"])
    df = res.collectToDataframe()
    print(df)
    print(type(df))
