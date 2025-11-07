from surprise import Dataset, SVD
from surprise.model_selection import cross_validate


# Load the movielens-100k dataset (download it if needed),
data = Dataset.load_builtin("ml-100k")

# We'll use the famous SVD algorithm.
"""
Similarity measure configuration: Used to estimate a rating.
1. Pass a 'sim_options' argument at the creation of an algorithm. 
2. (Argument = Dictionary) With following (all optional) keys:
  sim_options = {
    "name": "cosine", # name of the similarity to use, as defined in the 'similaritie' module.
    "user_based": False,  # compute  similarities between items
}
algo = KNNBasic(sim_options=sim_options)
"""
algo = SVD()


# Run 5-fold cross-validation and print results
"""
The cross_validate() function runs a cross-validation procedure according to the cv argument, 
and computes some accuracy measures. We are here using a classical 5-fold cross-validation, 
but fancier iterators can be used.
"""
cross_validate(algo, data, measures=["RMSE", "MAE"], cv=5, verbose=True)

"""
* The Evaluation: cross_validate() *
The cross_validate() function is what evaluates the SVD algorithm:

1. Data Split (cv=5): Divide the entire dataset of 100,000 ratings into 5 subsets (or folds) of equal size.

2. Training and Testing Cycle: Run the process 5 times. In each iteration:

- Training: Use 4 of the 5 subsets to train the SVD algorithm. The algorithm learns to predict based on 80% of the data.
- Test: Use the remaining subset (20%) to test the algorithm. The SVD predicts the rating for each (user, movie) pair in this set.
- Comparison: The algorithm compares the predicted score with the actual score (the one in the dataset) to see how well it did.

3. Metrics (measures=["RMSE", "MAE"]): The difference between the predicted and actual score is measured with:
- RMSE (Root Mean Square Error): Measures the average error in predictions. A lower value is better.
- MAE (Mean Absolute Error): Measures the average magnitude of the errors in a set of predictions. A lower value is also better.

4. Result: 
The final results printed are the average of the 5 runs for the RMSE and MAE metrics, giving you a reliable idea of ​​the accuracy of the SVD algorithm on the MovieLens 100k dataset.
"""