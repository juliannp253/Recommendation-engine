import json
import time
from data_tools import fetch_all_user_profiles, gather_candidate_buckets, fetch_user_data
from llm_recommender import generate_final_recommendations

def print_step(step_name):
    print(f"\n{'='*60}")
    print(f" ESTADO: {step_name}")
    print(f"{'='*60}")

def run_single_user_test():
    print_step("1. USER SELECTION")
    
    # A) We try to obtain real IDs from your database
    all_ids_or_users = fetch_all_user_profiles()
    
    if not all_ids_or_users or (isinstance(all_ids_or_users[0], dict) and all_ids_or_users[0].get("error")):
        print("ERROR: Users could not be retrieved from the database.")
        return

    first_item = all_ids_or_users[0]
    
    if isinstance(first_item, dict):
        # If we receive the complete object, we extract the ID
        target_user_id = str(first_item.get("_id"))
    else:
        # If it's already a string, we use it directly
        target_user_id = str(first_item)
    # --------------------------------------------------
    
    print(f"User selected (ID): {target_user_id}")
    
    # Display basic data to validate that we read the database correctly.
    user_data = fetch_user_data(target_user_id)
    
    # Extra security check
    if "error" in user_data:
        print(f"Error retrieving user data: {user_data['error']}")
        return

    genres = user_data.get("favoriteGenres", [])
    print(f"Profile: Genres {genres} | Ratings Count: {len(user_data.get('movieRatings', []))}")

    # ---------------------------------------------------------
    
    print_step("2. PHASE 1: DATA MINING (Pure Python)")
    print("   Running algorithms (Collaborative + Content + Trending)...")
    
    start_time = time.time()
    data_package = gather_candidate_buckets(target_user_id)
    mining_time = time.time() - start_time
    
    if "error" in data_package:
        print(f"ERROR in Mining: {data_package['error']}")
        return

    # Bucket Inspection
    candidates = data_package.get("candidates", {})
    print(f"Mining completed in {mining_time:.2f}s")
    print("Summary of Candidates Found:")
    for bucket, items in candidates.items():
        print(f"   - {bucket}: {len(items)} Movies")

    # Validate if there are enough candidates
    total = sum(len(l) for l in candidates.values())
    if total == 0:
        print("ALERT: No candidates were found. Please review your job functions in data_tools..")
    
    # ---------------------------------------------------------

    print_step("3. PHASE 2: INTELLIGENT HEALING (LLM Agent)")
    print("   Submitting candidates to GPT-5-nano for reranking and justification...")
    
    start_time = time.time()
    final_output = generate_final_recommendations(data_package)
    llm_time = time.time() - start_time
    
    if final_output.get("error"):
        print(f"ERROR from LLM: {final_output['error']}")
        if "raw" in final_output:
            print(f"Raw Response: {final_output['raw']}")
        return

    print(f"Healing completed in {llm_time:.2f}s")

    # ---------------------------------------------------------

    print_step("4. FINAL RESULT (JSON FOR FRONTEND)")
    
    print(json.dumps(final_output, indent=2, ensure_ascii=False))
    
    sections = final_output.get("sections", [])
    print("\nSELF-DIAGNOSIS:")
    if len(sections) == 3:
        print("Correct structure (3 Carousels generated).")
    else:
        print(f"Unusual structure: They were generated {len(sections)} sections (3 were expected).")
        
    missing_reasons = False
    for sec in sections:
        for mov in sec["movies"]:
            if not mov.get("ai_reason"):
                missing_reasons = True
    
    if not missing_reasons:
        print("All films have an 'ai_reason' (AI justification).")
    else:
        print("Some films lack justification..")

if __name__ == "__main__":
    try:
        run_single_user_test()
    except KeyboardInterrupt:
        print("\nTest interrupted by user.")
    except Exception as e:
        print(f"\nUNCONTROLLED FATAL ERROR: {e}")