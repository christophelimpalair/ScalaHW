/** Now we need to know how good this recommendation is. i.e.: how accurate **/
// Evaluate Recommendation Quality
// To make this meaningful, some of the artist play data can be set aside and hidden from the ALS model building process. Then this held-out data can be interpreted as a collection of “good” recommendations for each user, but one that the recommender has not already been given.
// The recommender’s performance can then be computed by comparing all held-out artists’ ranks to the rest. The fraction of pairs where the held-out artist is ranked higher is its score.

// AUC may be viewed as the probability that randomly-chosen “good” artist ranks above a randomly-chosen “bad” artist
import scala.collection.Map
import scala.collection.mutable.ArrayBuffer
import scala.util.Random.
import org.apache.spark.{SparkConf, SparkContext}

import org.apache.spark.SparkContext._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.mllib.recommendation._
import org.apache.spark.rdd.RDD

def areaUnderCurve(
      positiveData: RDD[Rating],
      bAllItemIDs: Broadcast[Array[Int]], //bAllItemIDs is the array of all unique artist ID (product ID)
      predictFunction: ( RDD[(Int,Int)] => RDD[Rating] )//function used for prediction
) = {
      // What this actually computes is AUC per user. 
      // The result is called "mean AUC" for all users
      // Take held-out data as the "positive", and map to tuples
      // positiveUserProducts: RDD[(Int,Int)]
      val positiveUserProducts = positiveData.map( r=> (r.user, r.product))

      // Make predictions for each of them, including a numeric score and gather by user
      // positivePredictions: RDD[(Int,Iterable[mllib.recommendation.Rating])]
      //                   ^userId  ^list of rating for each user
      val positivePredictions = predictFunction( positiveUserProducts ).groupBy( _.user )

      // Create a set of "negative" products for each user. These are randomly chosen from 
      // among all of the other items, excluding those that are "positive" for the user.
      // mapPartitions
      // This is a specialized map that is called only once for each partition.
      // The entire content of the respective partitions is available as sequential
      // streams of values via the input argument (Iterator[T]). The function
      // must return another Iterator[U]. The combined iterators are automatically
      // converted into a new RDD
      // negativeUserProduct: RDD[(Int,Int)] each element is userId and productID pair.
      // This is the pair is least likely to listen to.
      // positiveUserProducts.groupByKey(): RDD[(Int,Iterable[Int])]
      val negativeUserProducts = positiveUserProducts.groupByKey().mapPartitions {
         // mapPartition operates on many (user,positive-items) pairs at once
         // userIDAndPosItemIDs: Iterator[(Int,Iterable[Int])]
       userIDAndPosItemIDs => {
            // Init on RNG and the item IDs set once for partition
            val random = new Random()
            // allItemIDs: Array[Int] all unique product ID
            val allItemIDs = bAllItemIDs.value
            // userID: Int posItemIDs: Iterable[Int]
            userIDAndPosItemIDs.map { case(userID, posItemIDs) => 
                  // posItemIDSet: scala.collection.immutable.Set[Int]
                  val posItemIDSet = posItemIDs.toSet
                  // negative will hold array buffer of negative  product id
                  val negative = new ArrayBufer[Int]()
                  var i = 0
                  // keep about as many negative examples per user as possible
                  // duplicates are OK
                  while ( i < allItemIDs.size && negative.size < posItemIDSet.size) {
                        // get random ID from all item set
                        val itemID = allItemIDs(random.nextInt(allItemIDs.size))
                        if ( ! posItemIDSet.contains(itemID) ) {
                              negative += itemID
                        }
                        i += 1
                  }
                  // Result is collection of (user, negative-item) tuples
                  // scala.collection.mutable.ArrayBuffer[(Int,Int)]
                  negative.map(itemID => (userID, itemID))
            } // close of mapping function
      }
}.flatMap(t => t)
// Before running flatMap: RDD[scala.collection.mutable.ArrayBuffer[(Int,Int)]
// flatMap breaks the collection above down into a big set of tuples RDD[(Int,Int)]

// Make a prediction on the rest
// negative Predictions: RDD[(Int, Iterable[mllib.recommendations.Rating])]
val negativePredictions = produceFunction(negativeUserProducts).groupBy(_.user)

// result of join positive and negative prediction
// res3: RDD[(Int,(Iterable[Rating],Iterable[Rating]))]
positivePredictions.join(negativePrediction).values.map {
	case( positiveRatings, negativeRatings ) =>
		// calculate the proportion of all positive predictions rank higher than neg predictions
		var correct = 0L
		var total 	= 0L

		// For each pair
		for ( positive <- positiveRatings;
			  negative <- negativeRatings ) 
		{
			if ( positive.rating > negative.rating )
			{
				correct += 1
			}
			total += 1
		}
		// return AUC: fraction of pairs ranked correctly
		correct.toDouble / total
}.mean() // return mean of AUC over all users