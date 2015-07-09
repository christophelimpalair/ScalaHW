/**
 * This file needs ALS-1 and ALS-2 
 */

// The ALS model will be trained on the train data only
// and the CV (cross-validation) set is used to evaluate the model
// Training : 90%
// CV       : 10% <- data your model has not been provided
// 
// process dataset so that it can be used by ALS model.
// return: RDD[Rating]
def buildRatings (
	rawUserArtistData: RDD[String],
	bArtistAlias: Broadcast[Map[Int,Int]]) = { // Map[ Int <- bad artist ID, Int <- good artist ID]
		rawUserArtistData.map {
			line => val Array(userID, artistID, count) = line.split(' ').map(_.toInt)
			// replace badID with goodID
			val finalArtistID = bArtistAlias.value.getOrElse(artistID, artistID)
			Rating(userID, finalArtistID, count)
		}
	}
)

// allDate: RDD[Rating]
val allData = buildRatings(rawUserArtistData, bArtistAlias)

// Randomly select 90% data as training, 10% as CV to avoid bias.
// trainData: RDD[Rating]	cvData: RDD[Rating]
val Array(trainData, cvData) = allData.randomSplit(Array(0.9, 0.1)) // 0.9 goes to trainData, 0.1 goes to cvData


trainData.cache()
cvData.cache()

// Remove duplicates product(artist) id, collect to driver
// allItemIDs: Array[Int]
val allItemIDs = allData.map(_.product).distinct().collect()

// Broadcast this dataset to each partition so that each partition only has one copy
// instead of one copy per task
val bAllItemIDs = sc.broadcast(allItemIDs)

// model: mllib.recommendation.MatrixFactorizationModel
val model = ALS.trainImplicit(trainData, 10, 5, 0.01, 1.0)

val auc = areaUnderCurve(cvData, bAllItemIDs, model.predict)
// OUTPUT:
/*
15/03/26 11:30:55 INFO DAGScheduler: Job 40 finished: mean at <console>:80, took 14.616847 s
auc: Double = 0.9363228929060406
*/

// It is helpful to benchmark this against a simpler approach
// For example, consider recommending the globally most-played artist to every user.
// This is not personalized but is simple and may be effective.

// This is another interesting demonstration of Scala syntax, where the function
// appears to be defined to take two lists of arguments, calling the function
// and supplying the first two arguments creates a partially applied function,
// which itself takes an argument (allData) in order to return predictions.
def predictMostListened (sc: SparkContext, train: RDD[Rating])(allData: RDD[(Int, Int)]) = { // currying
	// bListenCount: Broadcast[scala.collection.Map[Int,Double]]  here Int is artistID, Double is total play count
	// r.rating is play count in the training set
	val bListenCount = sc.broadcast( train.map( r => (r.product, r.rating ) ).reduceByKey(_+_).collectAsMap() ) // all ratings added to product
	
	allData.map { 
		case (user, product) => Rating( user, product, bListenCount.value.getOrElse(product, 0.0) ) 
	}
}
// predictMostListened(sc, trainData) is a function: RDD[(Int,Int)] => RDD[Rating]
val auc = areaUnderCurve(cvData, bAllItemIDs, predictMostListened(sc, trainData))
// OUTPUT: 
// 15/03/26 11:47:55 INFO DAGScheduler: Stage 395 (mean at <console>:80) finished in 2.860 s
// 15/03/26 11:47:55 INFO DAGScheduler: Job 43 finished: mean at <console>:80, took 4.369515 s
// auc: Double = 0.8399968824893063

// take 20 users to the driver
val someUsers = allData.map(_.user).distinct().take(20)

// someUsers: Array[Int] = Array(1000482, 1000764, 1004720, 1000536, 1006728, 1002416, 1001884, 1007214, 1005104, 
	// 1003224, 1006612, 1002494, 1006118, 1006198, 1005372, 1003248, 1005446, 1006860, 1005982, 1005708)
	
// someRecommendations: Array[Array[Rating]] <- each element is 5 recommendations for one user
val someRecommendations = someUsers.map(userID => model.recommendProducts(userID, 5))
someRecommendations.map(
	// mkString joins a collection to a string with a delimiter
	recs => recs.head.user + "->" + recs.map(_.product).mkString(",")
).foreach(println)