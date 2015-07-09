def clusteringTake3(rawData: RDD[String]): Unit = {
  // parseFunction: String => (String, Vector) -- label, data
  val parseFunction = buildCategoricalAndLabelFunction(rawData)
  // data: RDD[Vector] data only has valid vectors without labels
  val data = rawData.map(parseFunction).values
  val normalizedData = data.map(buildNormalizationFunction(data)).cache()
  (30 to 160 by 180).map(k => (k,clusteringScore2(normalizedData, k))).toList.foreach(println)
  normalizedData.unpersist()
}

clusteringTake3(rawData)
// OUTPUT: 15/04/09 11:44:55 INFO DAGScheduler: Job 43 finished: mean at <console>:45, took 27.891707 s
// (30,0.13609445034041417)

// ClusteringScore2 ( evaluate how compact your cluster is. Smaller value, all points are closer to centroid) 
// Second metric based on entropy
// A good clustering would create clusters that contain one or a few types of the known attack, and little of anything else
// A good clustering would have clusters whose collection of labels are homogeneous and have low entropy
// 
// Entropy for one cluster:
// label 	attack1 	normal  	attack2
// 			3			1			1
// probab.  3/5			1/5			1/5
// 
// -3/5 * Log ( 3/5 ) - 1/5 * Log(1/5) - 1/5 * Log ( 1/5 )
// 
// Entropy: (counts: Iterable[Int]) Double
def entropy( counts: Iterable[Int]) = {
	val values = counts.filter(_ > 0)
	val n:Double = values.sum
	values.map { v =>
		val p = v/n
		-p * math.log(p)
	}.sum
}

// weighted average of entropy for all cluster can be used as cluster score
// weight is the size o cluster
// input of this function will be RDD. Each element of RDD is label and normalized data
def clusteringScore3( normalizedLabelsAndData: RDD[(String, Vector)], k:Int) = {
	val kmeans = new KMeans()
	kmeans.setK(k)
	kmeans.setRuns(10)
	val model = kmeans.run(normalizedLabelsAndData.values)
	// predict cluster for each datum
	// rdd.RDD[(String, Int)] each element is the label and cluster number it belongs to
	val labelsAndClusters = normalizedLabelsAndData.mapValues(model.predict)
	// swap keys/values
	// rdd.RDD[(Int, String)]
	val clustersAndLabels = labels.AndClusters.map(_.swap)
}

// Extract collections of labels per cluster
// rdd.RDD[Iterable[String]] each element is all labels for one cluser
val labelsInCluter = clustersAndLabels.groupByKey().values

// count labels in collections
// RDD[Iterable[Int]] each element is total count of each label for each cluster
val labelCounts = labelsInCluter.map(_.groupBy(l => l).map(_._2.size))

// total sample size
