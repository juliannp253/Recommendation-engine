# data_tools.py

import requests
import json
import os
import datetime
from typing import List, Dict, Any, Set
from pymongo import MongoClient
from dotenv import load_dotenv
from bson.objectid import ObjectId
from collections import Counter
from functools import lru_cache 

load_dotenv()

# --- CONFIGURATION ---
MONGO_URI = os.getenv("MONGO_URI")
DB_NAME = os.getenv("DB_NAME")
COLLECTION_NAME_USERS = os.getenv("COLLECTION_NAME_USERS", "users")
RECOMMENDATION_COLLECTION = os.getenv("RECOMMENDATION_COLLECTION", "recommendation_cache")

TMDB_API_KEY = os.getenv("TMDB_API_KEY")
TMDB_BASE_URL = "https://api.themoviedb.org/3"

# ------------------------------
# PERSISTENT MONGO CLIENT
# ------------------------------
try:
    if not MONGO_URI:
        raise ValueError("MONGO_URI is not configured.")

    GLOBAL_MONGO_CLIENT = MongoClient(MONGO_URI)
    GLOBAL_DB = GLOBAL_MONGO_CLIENT[DB_NAME]
    GLOBAL_USERS_COLLECTION = GLOBAL_DB[COLLECTION_NAME_USERS]
    CONNECTION_ERROR = None
    GLOBAL_MONGO_CLIENT.admin.command('ping')
except Exception as e:
    GLOBAL_MONGO_CLIENT = None
    CONNECTION_ERROR = str(e)


def json_serial(obj):
    if isinstance(obj, (datetime.datetime, datetime.date)):
        return obj.isoformat()
    return str(obj)

# ----------------------------------------------------
# 1. BASIC DATA ACCESS FUNCTIONS (FETCHERS)
# ----------------------------------------------------

def fetch_user_data(user_id: str) -> Dict[str, Any]:
    if CONNECTION_ERROR: return {"error": CONNECTION_ERROR}
    try:
        # If we receive a dictionary by mistake, we extract the ID
        if isinstance(user_id, dict):
            user_id = user_id.get("_id", str(user_id))

        oid = ObjectId(user_id) if ObjectId.is_valid(user_id) else user_id # Mongo needs an object ID 24 hex.
        user = GLOBAL_USERS_COLLECTION.find_one({"_id": oid})
        
        if user: 
            user["_id"] = str(user["_id"]) # Convert into a string to send it as JSON
            return user
        else:
            return {"error": f"User {user_id} not found"} 

    except Exception as e: return {"error": str(e)}

def fetch_all_user_profiles() -> List[Dict[str, Any]]:
    """
    Bring the necessary dataset for Collaborative Filtering.
    """
    if CONNECTION_ERROR: return []
    try:
        profiles = []
        projection = {"movieRatings": 1, "favoriteGenres": 1}
        for doc in GLOBAL_USERS_COLLECTION.find({}, projection):
            doc["_id"] = str(doc["_id"])
            profiles.append(doc)
        return profiles
    except Exception: return []

# ----------------------------------------------------
# 2. HELPERS TMDB WITH CACHE
# ----------------------------------------------------

@lru_cache(maxsize=1000)
def fetch_tmdb_movie_details(movie_id: str) -> Dict[str, Any]:
    """
    It retrieves basic details (Title, Poster) from TMDB.
    It uses in-memory caching: If it has already looked up the ID '123', it doesn't call the API again.
    """
    # Convert to string to ensure consistent cache key
    movie_id = str(movie_id) 
    
    url = f"{TMDB_BASE_URL}/movie/{movie_id}"
    params = {"api_key": TMDB_API_KEY, "language": "en-US"}
    
    try:
        res = requests.get(url, params=params, timeout=3)
        if res.status_code == 200:
            data = res.json() # Make a Dict with only relevant info from the whole JSON response    
            return {
                "id": data["id"],
                "title": data["title"], 
                "poster_path": data.get("poster_path"),
                "vote_average": data.get("vote_average"),
                "overview": data.get("overview", "")[:100]
            }
    except Exception:
        pass
    return None

# ----------------------------------------------------
# 3. ALGORITHMIC LOGIC = HARD CRITERIA
# ----------------------------------------------------

def get_user_top_genres(user_profile: Dict) -> List[str]:
    genres = user_profile.get("favoriteGenres", [])
    if not genres:
        return ["ACTION", "COMEDY"] 
    return genres[:2] 

def get_collaborative_candidates(target_user: Dict, all_users: List[Dict]) -> List[Dict]:
    """
    Collaborative filtering algorithm (User-User).
    Now 'hydrate' the found IDs using fetch_tmdb_movie_details.
    """
    candidates = [] # Empty list to store all recommended movies
    target_ratings = target_user.get("movieRatings", [])  # Get rated movies from user, if not, return an empty
    
    # Iterate over each rating r in target_ratings, get a set of movies with score >= 4
    target_liked_ids = {
        r.get("movieId") for r in target_ratings 
        if r.get("score", 0) >= 4
    } 

    if not target_liked_ids:
        return [] 

    found_ids = set() # To avoid duplicates in the final list

    for other in all_users:
        if other["_id"] == target_user["_id"]: continue # Avoid the user itself

        other_ratings = other.get("movieRatings", [])
        other_liked_ids = {
            r.get("movieId") for r in other_ratings 
            if r.get("score", 0) >= 4
        } # Movies score >= 4

        # Which IDs are present in BOTH sets
        intersection = target_liked_ids.intersection(other_liked_ids)
        
        # If we have at least one movie in common that we both liked, 
        # we consider this user to be a "Neighbor".
        if len(intersection) >= 1: 
            # what he saw MINUS what I saw. POTENTIAL CANDIDATE(S)
            diff = other_liked_ids - target_liked_ids 
            for movie_id in diff:
                # Safety limit to avoid overloading, LIMIT of 15
                if movie_id not in found_ids and len(found_ids) < 15:
                    
                    # We searched for the title in TMDB (or Cache)
                    details = fetch_tmdb_movie_details(movie_id)
                    
                    if details:
                        candidates.append({
                            "id": details["id"],
                            "title": details["title"], 
                            "poster_path": details["poster_path"],
                            "vote_average": details["vote_average"],
                            "origin": "collaborative",
                            "reason": "Liked by users with similar taste"
                        })
                        found_ids.add(movie_id)
    
    return candidates

def get_content_based_candidates(target_user: Dict) -> List[Dict]:
    ratings = target_user.get("movieRatings", [])
    ratings.sort(key=lambda x: x.get("score", 0), reverse=True) # Sort movies from user by decreasing rating

    if not ratings: return []

    top_movie_id_str = ratings[0].get("movieId") 
    if not top_movie_id_str: return []

    url = f"{TMDB_BASE_URL}/movie/{top_movie_id_str}/recommendations"
    params = {"api_key": TMDB_API_KEY, "language": "en-US", "page": 1}
    
    try:
        res = requests.get(url, params=params, timeout=5)
        if res.status_code == 200:
            data = res.json()
            results = []
            for item in data.get("results", [])[:10]:
                results.append({
                    "id": item["id"],
                    "title": item["title"],
                    "poster_path": item.get("poster_path"),
                    "vote_average": item.get("vote_average"),
                    "origin": "content_based",
                    "reason": f"Similar to a movie you rated highly (TMDB ID: {top_movie_id_str})"
                })
            return results
    except Exception:
        pass
    return []

def get_trending_candidates(genre_id_or_name: str = None) -> List[Dict]:
    endpoint = f"{TMDB_BASE_URL}/discover/movie"
    
    genre_map = {
        "ACTION": 28, "ADVENTURE": 12, "ANIMATION": 16, "COMEDY": 35,
        "CRIME": 80, "DOCUMENTARY": 99, "DRAMA": 18, "FAMILY": 10751,
        "FANTASY": 14, "HISTORY": 36, "HORROR": 27, "MUSIC": 10402,
        "MYSTERY": 9648, "ROMANCE": 10749, "SCI-FI": 878, "TV MOVIE": 10770,
        "THRILLER": 53, "WAR": 10752, "WESTERN": 37
    }
    
    params = {
        "api_key": TMDB_API_KEY,
        "sort_by": "popularity.desc",
        "vote_count.gte": 300, 
        "vote_average.gte": 6.0,
        "page": 1
    }

    # "Sci-Fi" in parameters, convert into "SCI-FI" to match a genre in genre_map
    genre_key = str(genre_id_or_name).upper() if genre_id_or_name else ""

    if genre_key in genre_map:
        params["with_genres"] = genre_map[genre_key] # Get the genre ID
    elif genre_key.isdigit():
        params["with_genres"] = genre_key

    try:
        res = requests.get(endpoint, params=params, timeout=5)
        if res.status_code == 200:
            data = res.json()
            results = []
            for item in data.get("results", [])[:10]:
                results.append({
                    "id": item["id"],
                    "title": item["title"],
                    "poster_path": item.get("poster_path"),
                    "vote_average": item.get("vote_average"),
                    "origin": "trending",
                    "reason": f"Trending now in {genre_key.title()}"
                })
            return results
    except Exception:
        pass
    return []

# ----------------------------------------------------
# 4. MASTER GATHERER (PHASE 1)
# ----------------------------------------------------

def gather_candidate_buckets(user_id: str) -> Dict[str, Any]:
    user = fetch_user_data(user_id)
    if "error" in user: return user
    if not user: return {"error": "User not found"}

    all_profiles = fetch_all_user_profiles()

    top_genres = get_user_top_genres(user) # First two genres in collection
    genre_1 = top_genres[0] if len(top_genres) > 0 else "ACTION" 
    genre_2 = top_genres[1] if len(top_genres) > 1 else None 

    print(f"[DataMiner] Mining candidates for user {user_id} (Genres: {top_genres})...")

    collab_list = get_collaborative_candidates(user, all_profiles)
    content_list = get_content_based_candidates(user)
    trend_g1_list = get_trending_candidates(genre_1)
    trend_g2_list = get_trending_candidates(genre_2) if genre_2 else []

    genres_display = [g.title() for g in top_genres]

    package = {
        "user_profile_summary": {
            "favorite_genres": genres_display,
            "history_count": len(user.get("movieRatings", []))
        },
        "candidates": {
            "collaborative": collab_list,       
            "content_based": content_list,      
            f"trending_{genre_1.lower()}": trend_g1_list, 
            f"trending_{genre_2.lower()}" if genre_2 else "trending_secondary": trend_g2_list  
        }
    }
    
    return package

# ----------------------------------------------------
# 5. PERSISTENCE
# ----------------------------------------------------
def save_final_recommendations(user_id: str, recommendations_data: Dict[str, Any]) -> str:
    if CONNECTION_ERROR or not GLOBAL_MONGO_CLIENT:
        return json.dumps({"error": "DB Error"})
    try:
        cache_collection = GLOBAL_DB[RECOMMENDATION_COLLECTION]
        document = {
            "user_id": user_id,
            "generated_at": datetime.datetime.utcnow(),
            "sections": recommendations_data.get("sections", []),
            "meta_justification": recommendations_data.get("meta_justification", "")
        }
        cache_collection.update_one(
            {"user_id": user_id}, {"$set": document}, upsert=True
        )
        return json.dumps({"status": "success"})
    except Exception as e:
        return json.dumps({"error": str(e)})

# TESTING
if __name__ == "__main__":
    pass