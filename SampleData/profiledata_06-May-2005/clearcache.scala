/*
hyperparamater selection

rank = 10: the number of latent factors or the number of columns k in the user-feature and product feature matrices.

iteration = 5: the number of iterations that the factorization runs
lamda = 0.01: a standard overfitting parameter. Higher values resist overfitting
alpha = 1.0: control the relative weight of observed vs unobserved user-product interaction

rank, lamba and alpha can be considered hyperparameters.
The most basic way to choose values is to simply try combinations of values and evaluate a metric for each of them.
*/
// Remove model from the cache
def unpersist(model: MatrixFactorizationModel): Unit = {
	// when done with it in order to make sure they are promptly uncached
	model.userFeatures.unpersist()
	model.productFeadures.unpersist()
}

val evaluations = for (	rank <- Array(10,50);
						lambda <- Array(1.0,0.0001);
						alpha <- Array(1.0,40.0)
					  )
yield {
	// Train model
	val model = ALS.trainImplicit(trainData, rank, 10, lambda, alpha)
	// evaluate performance
	val auc = areaUnderCurve(cvData, bAllItemIDs, model.predict)
	unpersist(model)

	println(rank + " " + lambda + " " + alpha + " " + auc)
	((rank, lambda, alpha), auc)
}
// sort by second value (AUC) descending
evaluations.sortBy(_._2).reverse.foreach(println)
