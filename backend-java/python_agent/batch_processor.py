# batch_processor.py

import json
import time
import argparse  
from typing import List, Dict, Any

from data_tools import (
    fetch_all_user_profiles,
    gather_candidate_buckets,    
    save_final_recommendations
)

from llm_recommender import generate_final_recommendations


# ============================================================
#  BATCH PROCESSOR — "1+2" (HYBRID) STRATEGY
# ============================================================

def process_user_list(users_to_process: List[Any]) -> str:
    """
    Core function that iterates over a list of users (or IDs) 
    and runs the recommendation pipeline for each.
    """
    total_users = len(users_to_process)
    print(f"\n--- INITIATING PROCESS FOR {total_users} USER(S) ---\n")

    processed_count = 0

    # ============================================================
    # USER-BY-USER PROCESSING LOOP
    # ============================================================

    for user_input in users_to_process:
        # data_tools handles both full profile dicts or simple ID strings
        # If user_input is a dict, we extract ID for logging; if string, we use it directly.
        user_id_log = user_input.get("_id") if isinstance(user_input, dict) else str(user_input)
        
        print(f"-> Processing user: {user_id_log}")

        # ============================================================
        # A) PHASE 1 — EVIDENCE COLLECTION (Hard Criterion)
        # ============================================================
        
        try:
            # gather_candidate_buckets handles the ID lookup internally
            data_package = gather_candidate_buckets(user_input)
        except Exception as e:
            print(f"ERROR DATA MINING: {e}")
            continue

        if data_package.get("error"):
            print(f"ERROR GETTING CANDIDATES: {data_package['error']}")
            continue

        # Validate candidates quantity (Just for logging/debugging)
        candidates = data_package.get("candidates", {})
        total_candidates = (
            len(candidates.get("collaborative", [])) + 
            len(candidates.get("content_based", [])) + 
            len(candidates.get("trending_primary", [])) 
        )
        
        # ============================================================
        # B) PHASE 2 — INTELLIGENT CURATION (The LLM Agent)
        # ============================================================

        try:
            final_output = generate_final_recommendations(data_package)
        except Exception as e:
            print(f"CRITICAL ERROR IN LLM RECOMMENDER: {e}")
            continue

        if final_output.get("error"):
            print(f"ERROR LLM RESPONSE: {final_output['error']}")
            print("RAW:", final_output.get("raw"))
            continue

        # ============================================================
        # C) PERSISTENCE
        # ============================================================

        # We use the ID found inside data_package to ensure it's the correct string ID
        real_user_id = data_package.get("user_profile_summary", {}).get("user_id_debug", user_id_log)
        
        # Note: data_tools usually expects the ID to save
        save_result = save_final_recommendations(user_id_log, final_output)

        try:
            parsed = json.loads(save_result)
            if parsed.get("status") == "success":
                print(f"   [OK] Recommendations saved for user {user_id_log}")
                processed_count += 1
            else:
                print(f"   [ERROR DB] {save_result}")
        except:
            print(f"   [ERROR PARSING] {save_result}")
            
        # Short pause only if processing multiple users to avoid rate limits
        if total_users > 1:
            time.sleep(0.5)

    # ============================================================
    # END
    # ============================================================

    print(f"\n--- BATCH ENDED: Processed {processed_count} FROM {total_users} ---\n")

    return json.dumps({
        "status": "completed",
        "processed": processed_count,
        "total": total_users
    })


if __name__ == "__main__":
    # 1. SETUP ARGUMENT PARSER
    parser = argparse.ArgumentParser(description="Run Recommendation Agent")
    
    # We add an optional argument --user_id
    parser.add_argument("--user_id", type=str, help="Target specific user ID (Single Mode)", default=None)
    
    args = parser.parse_args()

    # 2. DECIDE MODE
    if args.user_id:
        # --- SINGLE USER MODE (Real-time Trigger) ---
        print(f"SINGLE MODE DETECTED: Target {args.user_id}")
        targets = [args.user_id]
        
    else:
        # --- BATCH MODE (Scheduled Cron Job) ---
        print("BATCH MODE DETECTED: Fetching all users...")
        targets = fetch_all_user_profiles()
        
        # Validation if fetch failed
        if not targets or (isinstance(targets, list) and len(targets) > 0 and isinstance(targets[0], dict) and targets[0].get("error")):
            print(f"CRITICAL ERROR: Could not fetch users. {targets}")
            targets = []

    # 3. RUN PROCESS
    if targets:
        process_user_list(targets)
    else:
        print("No users to process.")