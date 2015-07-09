import org.apache.spark.mllib.clustering._
import org.apache.spark.mllib.linalg._
import org.apache.spark.rdd._
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.SparkContext._
// RDD [String]
val rawData = sc.textFile("kddcup.data_10_percent")
// what labels are present in the data, how many are there of each?
// count by label into label_count tuples, sort them descending by count
// countByValue: scala.collection.Map[String,Long] (Label, count)
rawData.map(_.split(',').last).countByValue().toSeq.sortBy(_._2).reverse.foreach(println)

/**
15/03/31 11:50:28 INFO DAGScheduler: Job 2 finished: countByValue at <console>:35, took 2.830504 s
(smurf.,280790)
(neptune.,107201)
(normal.,97278)
(back.,2203)
(satan.,1589)
(ipsweep.,1247)
(portsweep.,1040)
(warezclient.,1020)
(teardrop.,979)
(pod.,264)
(nmap.,231)
(guess_passwd.,53)
(buffer_overflow.,30)
(land.,21)
(warezmaster.,20)
(imap.,12)
(rootkit.,10)
(loadmodule.,9)
(ftp_write.,8)
(multihop.,7)
(phf.,4)
(perl.,3)
(spy.,2)
 */

// Note the data contains non-numeric features for example:
// The second column may be tcp, udp, or icmp. But k-means 
// requires numeric features. The final label column is also non-numeric
// RDD[(String,Vetor)]
val labelsAndData = rawData.map { line =>
	// toBuffer creates Buffer, a mutable list
    val buffer = line.split(',').toBuffer

	// remove 3 categorical value columns
    buffer.remove(1, 3)

    val label = buffer.remove(buffer.length - 1)
	//get data vector
    val vector = Vectors.dense(buffer.map(_.toDouble).toArray)
    (label, vector)
}

// get rdd of data vector only
val data = labelsAndData.values.cache()

val kmeans = new KMeans()
// cluster the data to create model
val model = kmeans.run(data)

//prints centroid k = 2
model.clusterCenters.foreach(println)

/*
it is good opportunity to use the given labels to get an
intuitive sense of what went into these two clusters by counting labels
within each cluster.
 */