import pandas as pd
from surprise import Reader, Dataset, SVD
from surprise.model_selection import cross_validate

# ----------------------------------------------------
# A. DATA LOAD
# ----------------------------------------------------

# 1. Load fake data from CSV file to a Pandas DataFrame 
df_ratings = pd.read_csv(
    'fake_ratings.csv', 
    header=None,  # No headers
    names=['user_id', 'movie_id', 'rating']
)

# 2. Define Reader: Specify the format and rating scale we will be using
reader = Reader(line_format='user item rating', rating_scale=(1, 5))

# 3. Load the Dataset of Surprise from the DataFrame
# Important: For cross_validate, we need the ccomplete object data, not just trainset.
data = Dataset.load_from_df(
    df_ratings[['user_id', 'movie_id', 'rating']], 
    reader=reader
)

# ----------------------------------------------------
# B. EVALUATING ALGORITHM 
# ----------------------------------------------------

print(">>> Starting Evaluation of SVD Algorithm (Croos Validation 5-Fold)...")

# Initializa algorithm 
algo_eval = SVD() 

# 4. Run 5-Fold Cross-Validation:
# - Algorithm gets trained and tested 5 times.
# - RMSE y MAE are being calculated.
cv_results = cross_validate(
    algo_eval, 
    data, 
    measures=["RMSE", "MAE"], 
    cv=5, 
    verbose=True
)

# Show means from results (most important values)
mean_rmse = cv_results['test_rmse'].mean()
mean_mae = cv_results['test_mae'].mean()

print("\n--- RESULTS FROM EVALUATION (MEAN) ---")
print(f"RMSE (Mean Error): {mean_rmse:.4f} (Deviation of {mean_rmse:.2f} is expected in predictions)")
print(f"MAE (Absolut Error): {mean_mae:.4f}")
print("--------------------------------------------\n")


# ----------------------------------------------------
# C. FINAL TRAINING AND PREDICTION (USING ALL DATA)
# ----------------------------------------------------

# 5. Create the full training dataset (100% of data)
trainset = data.build_full_trainset()
algo_final = SVD()
print(">>> Re-training SVD model with 100% of data for the final prediction...")
algo_final.fit(trainset)
print(">>> Final training finished.\n")


# ----------------------------------------------------
# D. GENERATING RECOMMENDATIONS (User_ID: 1)
# ----------------------------------------------------

TARGET_USER_ID = 1
TOP_N = 3 

# 6. Identify all unique movies and movies user have watched
all_movies = df_ratings['movie_id'].unique()
movies_seen_by_user = df_ratings.loc[
    df_ratings['user_id'] == TARGET_USER_ID, 
    'movie_id'
].unique()

# 7. Make pair-list (user, item) for which we will make prediction
# We only include movies user have not watched.
movies_to_predict = [
    (TARGET_USER_ID, movie_id, 0)
    for movie_id in all_movies 
    if movie_id not in movies_seen_by_user
]

# 8. Get predictions using the trained model.
print(f">>> Generating predictions for User ID: {TARGET_USER_ID}...")
predictions = algo_final.test(movies_to_predict)

# Function to get Top-N (3)
def get_top_n(predictions, n=3):
    pred_items = [(pred.iid, pred.est) for pred in predictions]
    top_n = sorted(pred_items, key=lambda x: x[1], reverse=True)
    return top_n[:n]

# Get and Show Top 3
top_recommendations = get_top_n(predictions, n=TOP_N)

print(f"\n--- TOP {TOP_N} RECOMMENDATIONS for the User {TARGET_USER_ID} ---")
for movie_id, score in top_recommendations:
    print(f"Movie ID: {movie_id} | Predicted Rating: {score:.4f}")