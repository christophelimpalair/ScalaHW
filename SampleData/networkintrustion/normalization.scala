// Feature normalization
// This will generate function. Given the input vector,
// this function will produce the vector with each dimension normalized.
def buildNormaliationFunction( data: RDD[Vector] ): ( Vector => Vector ) = {
	// dataArray: RDD[Array[Double]] -> 2D Array
	val dataAsArray = data.map(_.toArray)
	// numCols: Int
	val numCols = dataAsArray.first().length
	// n: Long total number of samples, number of rows
	val n = dataAsArray.count()
	// sums: Array[Double] each element is total sum for one attribute or dimension
	val sums = dataAsArray.reduce(
		(a,b) => a.zip(b).map( t => t._1 + t._2 )
		)
	// sumSquares: Array[Double]
	// each element is the sum of squares for each attributed
	val sumSquares = dataAsArray.fold(
		new Array[Double](numCols)
		)(
			(a,b) => a.zip(b).map(t => t._1 + t._2 * t._2 )
		)
		// stdevs: Array[Double]
		// each element is standard deviation for each attribute
		val stdevs = sumSquares.zip(sums).map {
			case( sumSq, sum ) => math.sqrt( n * sumSq - sum * sum ) / n
		}

	// means: Array[Double]
	// means for each attribute
	val means = sums.map(_ / n)
	// define the function
	(datum: Vector) => {
		val normalizedArray = (datum.toArray, means, stdevs).zipped.map(
			(value, mean, stdev) => if (stdev <= 0)(value - mean) else (value - mean) / stdev
			)
		Vectors.dense(normalizedArray)
	}
}
// OUTPUT: buildNormaliationFunction: (data: org.apache.spark.rdd.RDD[org.apache.spark.mllib.linalg.Vector])org.apache.spark.mllib.linalg.Vector => org.apache.spark.mllib.linalg.Vector
// 
// each attribute for this data is normalized
// normalizedData: RDD[Vector]
val normalizedData = data.map(buildNormaliationFunction(data)).cache()

(60 to 120 by 80).map(k => (k, clusteringScore2(normalizedData, k))).toList.foreach(println)
// OUTPUT: 15/04/07 12:00:39 INFO DAGScheduler: Job 141 finished: mean at <console>:45, took 72.670212 s
// (60,0.003991396051602379)

// Earlier, three categorical features are excluded, since non-numeric features aren't used with
// the Euclidean distance function that k-means uses in MLLib.
// The categorical features can translate into several binary indicator features using one-hot encoding, which can be viewed
// as numeric dimensions.
// The second column contains the protocol type: tcp, udp or lcmp
// 
// The single feature value tcp might become 1,0,0 udp might be 0,1,0
// This function will include categorical variable
// This function will return the function. The returned function takes string of each line and returns label and 
// unnormalized data vector including categorical feature for each line.
def buildCategoricalAndLabelFunction(rawData: RDD[String]: (String => (String, Vector))) = {
	// split Data: RDD[Array[String]]
	val splitData = rawData.map(_.split(','))
	// produce the mapping for each protocols
	// protocols: Map[String, Int] = Map(tcp -> 0, icmp -> 1, udp -> 2)
	// zipWithIndex method return a list of pairs where the second component is the index of each element
	val protocols = splitData.map(_(1)).distinc().collect().zipWithIndex.toMap // collect returns value back to driver

	val services = splitData.map(_(2)).distinct().zipWithIndex.toMap

	val tcpStates = splitData.map(_(3)).distinct().collect().zipWithIndex.toMap

	// produce function
	(line: String) => {
		// buffer: mutable.Buffer[String]
		val buffer = line.split(',').toBuffer
		val protocol = buffer.remove(1)
		val service = buffer.remove(1)
		val tcpState = buffer.remove(1)
		val label = buffer.remove(buffer.length - 1)
		// vector: mutable.buffer[Double]
		val vector = buffer.map(_.toDouble)
		val newProtocolFeatures = new Array[Double](protocols.size)
		newProtocolFeatures(protocols(protocol)) = 1.0

		val newServiceFeatures = new Array[Double](services.size)
		// encode service
		new ServiceFeatures(service(protocol)) = 1.0

		val newTopStateFeatures = new Array[Double](topStates.size)

		// encode TCP state
		newTCPStateFeatures(tcpStates(tcpState)) = 1.0

		// add categorical features into vector
		vector.insertAll(1, newTopStateFeatures)
		vector.insertAll(1, newServiceFeatures)
		vector.insertAll(1, newProtocolFeatures)

		(label, Vector.dense(vector.toArray))
	}
}