// use the given labels to get an intuitive sense
// of what went into these two clusters by
// counting the labels within each cluster
// clusterLabelCount: Map[(Int,String), Long] <- (Cluster num, label, count
val clusterLabelCount = labelsAndData.map { case(label, datum) =>
	// assign the sample to a particular cluster
	val cluster = model.predict(datum)
	(cluster, label)
}.countByValue()
/*
15/04/02 11:09:49 INFO DAGScheduler: Job 17 finished: countByValue at <console>:52, took 5.996167 s
clusterLabelCount: scala.collection.Map[(Int, String),Long] = Map((0,portsweep.) -> 1039, (0,rootkit.) -> 10, (0,buffer_overflow.) -> 30, (0,phf.) -> 4, (0,pod.) -> 264, (0,perl.) -> 3, (0,spy.) -> 2, (0,ftp_write.) -> 8, (0,nmap.) -> 231, (0,ipsweep.) -> 1247, (0,imap.) -> 12, (0,warezmaster.) -> 20, (0,satan.) -> 1589, (0,teardrop.) -> 979, (0,smurf.) -> 280790, (0,neptune.) -> 107201, (0,loadmodule.) -> 9, (0,guess_passwd.) -> 53, (0,normal.) -> 97278, (0,land.) -> 21, (0,multihop.) -> 7, (1,portsweep.) -> 1, (0,warezclient.) -> 1020, (0,back.) -> 2203)
 */

clusterLabelCount.toSeq.sorted.foreach { case((cluster, label), count) =>
	println(cluster + " " + label + " " + count)
}
/*
0 back. 2203
0 buffer_overflow. 30
0 ftp_write. 8
0 guess_passwd. 53
0 imap. 12
0 ipsweep. 1247
0 land. 21
0 loadmodule. 9
0 multihop. 7
0 neptune. 107201
0 nmap. 231
0 normal. 97278
0 perl. 3
0 phf. 4
0 pod. 264
0 portsweep. 1039
0 rootkit. 10
0 satan. 1589
0 smurf. 280790
0 spy. 2
0 teardrop. 979
0 warezclient. 1020
0 warezmaster. 20
1 portsweep. 1
 */

// choosing k. How many clusters are appropriate for this dataset
// A clustering could be considered good if each data point were near to its closest centroid
// diameter: is longest distance of sample and centroid
// Smaller diameter is better because cluster is more compact
// 
// Define Euclidean distance between two samples (collections)
// p = (p1,p2,..,pn)
// q = (q1,q2,..,qn)
// d(p,q) = sqrt(summation(qi-pi)^2)
def distance (a:Vector, b:Vector) = math.sqrt(a.toArray.zip(b.toArray).
	map(p => p._1 - p._2).map(d => d * d).sum)
// return distance between the data point and its nearest cluster's centroid
def disToCentroid(datum: Vector, model: KMeansModel) = {
	val cluster = model.predict(datum)
	val centroid = model.clusterCenters(cluster)
	distance(centroid, datum)
}
// disToCentroid: (datum: org.apache.spark.mllib.linalg.Vector, model: org.apache.spark.mllib.clustering.KMeansModel)Double


// define a function that measures average distance to centroid for a model built with a given k.
// smaller the score, the cluster is more compact
// k is the number of clusters
def clusteringScore(data: RDD[Vector], k: Int): Double = {
	val kmeans = new KMeans()
	// number of clusters you wants
	kmeans.setK(k)
	val model = kmeans.run(data)
	data.map(datum => disToCentroid(datum,model)).mean()
}

// this can be used to evaluate values of k from 10 to 30
(10 to 30 by 10).map( k => (k, clusteringScore(data,k))).foreach(println)

/*
15/04/02 11:33:09 INFO DAGScheduler: Job 106 finished: mean at <console>:57, took 3.207656 s
(10,1556.1256732910322)
(20,504.3472279939467)
(30,377.64311507043897)
 */

// set number of iteration and minimum convergence condition
// The algorithm exposes setRun() to set number of times
// the clustering is run for one k
// The algorithm has a threshold via setEpsilon() which controls the minimum amount of cluster centroid movement
// that is considered significant
def clusteringScore2(data: RDD[Vector], k: Int): Double = {
	val kmeans = new KMeans()
	// number of clusters you wants
	kmeans.setK(k)
	kmeans.setRuns(10)
	kmeans.setEpsilon(1.0e - 6)
	val model = kmeans.run(data)
	data.map(datum => disToCentroid(datum,model)).mean()
}

(30 to 100 by 90).map( k => (k, clusteringScore2(data,k))).foreach(println)

// Feature Normalization
// dataset has two features that are on a much larger scale than the others.
// Whereas most features have values between 0 and 1
// the bytes-sent and bytes received vary from 0 to tens of thousands.
// 
// The Euclidean distance bt points is completely determined by these two features.
// It is almost as if the other feature doesn't exist.
// So it's important to normalize away these differences in scale in order to put
// features on near-equal footing.
