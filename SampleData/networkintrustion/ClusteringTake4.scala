// use weighted average entropy as the clustering score
def clusteringTake4 ( rawData: RDD[String]): Unit = {
	// parseFunction: String => (String, Vector) <- Label, Data
	val parseFunction = buildCategoricalAndLabelFunction(rawData)
	// LabelsAndData: RDD[(String,Vector)]
	val labelsAndData = rawData.map(parseFunction)
	val normalizedLabelsAndData = labelsAndData.mapValues(buildNormalizationFunction(labelsAndData.values)).cache()
	// search for best cluster
	(30 to 160 by 180).map(k => (k, clusteringScore3(normalizedLabelsAndData,k))).toList.foreach(println)

	normalizedLabelsAndData.unpersist()
}

clusteringTake4(rawData)
// OUTPUT: 15/04/14 12:08:51 INFO DAGScheduler: Job 48 finished: sum at <console>:96, took 20.433339 s
//(30,1.0734143316130873)
//15/04/14 12:08:51 INFO MappedValuesRDD: Removing RDD 55 from persistence list
//15/04/14 12:08:51 INFO BlockManager: Removing RDD 55
//15/04/14 12:08:51 INFO BlockManager: Removing block rdd_55_2
//15/04/14 12:08:51 INFO MemoryStore: Block rdd_55_2 of size 54669290 dropped from memory (free 277744416)
//clusteringTake4: (rawData: org.apache.spark.rdd.RDD[String])Unit