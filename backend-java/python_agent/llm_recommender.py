# llm_recommender.py
# PHASE 2: (STRATEGY 1+2)

import os
import json
from typing import Dict, Any, List
from openai import AzureOpenAI
from data_tools import json_serial

# --- CONFIGURATION ---
AZURE_ENDPOINT = os.getenv("AZURE_OPENAI_ENDPOINT")
AZURE_API_KEY = os.getenv("AZURE_OPENAI_API_KEY")
DEPLOYMENT_NAME = os.getenv("AZURE_DEPLOYMENT_NAME", "gpt-5-nano")

try:
    LLM = AzureOpenAI(
        api_version="2025-01-01-preview",
        azure_endpoint=AZURE_ENDPOINT,
        api_key=AZURE_API_KEY
    )
except Exception:
    LLM = None

def generate_final_recommendations(data_package: Dict[str, Any]) -> Dict[str, Any]:
    """
    Receive the 'data_package' containing the candidate buckets (Collaborative, Content, Trending).
    Use the LLM to select and sort the items into 3 Carousels (Sections).
    """
    
    if not LLM:
        return {"error": "LLM Client not initialized"}

    candidates_map = {}
    all_candidates_list = []
    
    raw_candidates = data_package.get("candidates", {})
    
    # We iterated over all the lists (collaborative, content_based, trending...)
    # Break down those categories and put all the films into one giant bag (candidates_map)
    for category, items in raw_candidates.items():
        for item in items:
            c_id = str(item["id"]) 
            if c_id not in candidates_map:
                candidates_map[c_id] = item
                all_candidates_list.append(item)

    # If there are no candidates, abort
    if not candidates_map:
        return {"error": "No candidates provided to rank."}

    # 2. BUILDING THE PROMPT
    user_summary = data_package.get("user_profile_summary", {})
    genres = user_summary.get("favorite_genres", ["General"])
    
    genre_1 = genres[0] if len(genres) > 0 else "Cinema"
    genre_2 = genres[1] if len(genres) > 1 else "Popular"

    system_prompt = (
        "You are the Head Curator of a premium streaming platform.\n"
        "Your goal is to organize the user's Home Page into 3 SECTIONS (Carousels) "
        "selecting the best movies from the provided candidates.\n\n"

        "INPUT:\n"
        f"User Profile: {json.dumps(user_summary)}\n"
        "Candidates: A list of movies pre-selected by algorithms (Collaborative, Content-Based, Trending).\n\n"

        "MANDATORY TASK (STRATEGY 1+2):\n"
        "You must generate a JSON with exactly 3 sections:\n"
        "1. 'Top Picks for You': Hybrid selection (Collaborative + Content). The best 8-10 movies for this specific user.\n"
        f"2. 'Best in {genre_1}': The best 8-10 options for {genre_1}.\n"
        f"3. 'Best in {genre_2}': The best 8-10 options for {genre_2}.\n\n"

        "CURATION RULES:\n"
        "- LANGUAGE: All output (titles, reasons) must be in ENGLISH.\n"
        "- You can ONLY recommend IDs that exist in the candidates list.\n"
        "- DO NOT invent titles.\n"
        "- Generate a short, persuasive 'ai_reason' for each movie (e.g., 'Because you enjoyed Inception...').\n"
        "- Prioritize movies with 'origin': 'collaborative' or 'content_based' for section 1.\n\n"

        "OUTPUT JSON FORMAT:\n"
        "{\n"
        "  \"meta_justification\": \"Brief summary of the strategy used\",\n"
        "  \"sections\": [\n"
        "    {\n"
        "      \"title\": \"Top Picks for You\",\n"
        "      \"type\": \"mixed\",\n"
        "      \"items\": [ {\"id\": \"12345\", \"ai_reason\": \"...\"}, ... ]\n"
        "    },\n"
        "    { ... section 2 ... },\n"
        "    { ... section 3 ... }\n"
        "  ]\n"
        "}"
    )

    # We minimize the payload to avoid spending tokens (we only send ID, Title, Genres, Origin)
    minified_candidates = [
        {"id": str(m["id"]), "title": m["title"], "origin": m["origin"], "info": m.get("reason", "")}
        for m in all_candidates_list
    ]

    user_message = json.dumps({
        "user_genres": genres,
        "available_candidates": minified_candidates
    })

    # 3. CALL TO THE LLM
    try:
        resp = LLM.chat.completions.create(
            model=DEPLOYMENT_NAME,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_message}
            ],
            response_format={"type": "json_object"}
            # temperature=0.3  <--- DELETED
        )
        raw_response = resp.choices[0].message.content
        parsed_response = json.loads(raw_response)

    except Exception as e:
        return {"error": f"LLM ranking failed: {e}", "raw": None}

    # 4. HYDRATION AND VALIDATION (Python Logic)
    
    final_sections = []
    
    if "sections" not in parsed_response:
        return {"error": "LLM structure invalid (missing sections)", "raw": raw_response}

    for section in parsed_response["sections"]:
        hydrated_items = []
        raw_items = section.get("items", []) # Get Movie IDs from each of the three sections
        
        for item in raw_items:
            rec_id = str(item.get("id"))
            
            if rec_id in candidates_map:
                original_data = candidates_map[rec_id]
                
                hydrated_items.append({
                    "id": original_data["id"],
                    "title": original_data["title"],
                    "poster_path": original_data.get("poster_path"),
                    "vote_average": original_data.get("vote_average"),
                    "origin_type": original_data.get("origin"), 
                    "ai_reason": item.get("ai_reason", "Recomendada para ti.")
                })
        
        if hydrated_items:
            final_sections.append({
                "title": section.get("title", "Recommendations"),
                "type": section.get("type", "general"),
                "movies": hydrated_items 
            })

    return {
        "status": "success",
        "meta_justification": parsed_response.get("meta_justification", ""),
        "sections": final_sections
    }