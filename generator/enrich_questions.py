#!/usr/bin/env python3
import os
import sys
import json
import re
import urllib.request
import urllib.parse
import time

def parse_learning_content(file_path):
    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()
    
    items = []
    idx = 0
    while True:
        idx = content.find("LearningItem(", idx)
        if idx == -1:
            break
        start = idx + len("LearningItem(")
        paren_count = 1
        end = start
        inside_string = False
        escaped = False
        while end < len(content):
            char = content[end]
            if escaped:
                escaped = False
            elif char == '\\':
                escaped = True
            elif char == '"':
                inside_string = not inside_string
            
            if not inside_string:
                if char == '(':
                    paren_count += 1
                elif char == ')':
                    paren_count -= 1
                    if paren_count == 0:
                        end += 1
                        break
            end += 1
        
        block_text = content[start:end-1]
        
        if "val id:" in block_text:
            idx = end
            continue
            
        item = {}
        
        id_match = re.search(r"id\s*=\s*(\d+)", block_text)
        if id_match:
            item["id"] = int(id_match.group(1))
        else:
            idx = end
            continue
            
        type_match = re.search(r"type\s*=\s*ItemType\.(\w+)", block_text)
        if type_match:
            item["type"] = type_match.group(1)
            
        level_match = re.search(r"level\s*=\s*Level\.(\w+)", block_text)
        if level_match:
            item["level"] = level_match.group(1)
            
        phrase_match = re.search(r'phrase\s*=\s*"(.*?)"', block_text, re.DOTALL)
        if phrase_match:
            item["phrase"] = phrase_match.group(1).replace('\\"', '"')
            
        translation_match = re.search(r'translation\s*=\s*"(.*?)"', block_text, re.DOTALL)
        if translation_match:
            item["translation"] = translation_match.group(1).replace('\\"', '"')
            
        context_match = re.search(r'context\s*=\s*"(.*?)"', block_text, re.DOTALL)
        if context_match:
            item["context"] = context_match.group(1).replace('\\"', '"')
            
        options_match = re.search(r'options\s*=\s*listOf\((.*?)\)', block_text, re.DOTALL)
        if options_match:
            opts_text = options_match.group(1)
            opts = re.findall(r'"(.*?)"', opts_text)
            item["options"] = [o.replace('\\"', '"') for o in opts]
        else:
            item["options"] = []
            
        correct_match = re.search(r'correctAnswer\s*=\s*"(.*?)"', block_text, re.DOTALL)
        if correct_match:
            item["correctAnswer"] = correct_match.group(1).replace('\\"', '"')
        else:
            item["correctAnswer"] = ""
            
        cat_match = re.search(r'category\s*=\s*(\w+|\".*?\")', block_text)
        if cat_match:
            val = cat_match.group(1).replace('"', '')
            if val.startswith("category."):
                val = val.split(".")[-1]
            item["category"] = val
        else:
            item["category"] = "CASUAL"
            
        vars_match = re.search(r'variations\s*=\s*listOf\((.*?)\)', block_text, re.DOTALL)
        if vars_match:
            vars_text = vars_match.group(1)
            vars_list = re.findall(r'"(.*?)"', vars_text)
            item["variations"] = [v.replace('\\"', '"') for v in vars_list]
        else:
            item["variations"] = []
            
        items.append(item)
        idx = end
        
    return items

def generate_enrichment(api_key, level, category, count):
    if count <= 0:
        return []

    print(f"Generating {count} questions for {level} + {category} using Gemini...")
    
    prompt = f"""LingoScroll uygulaması için {level} seviyesinde ve {category} kategorisinde tam {count} adet benzersiz, pratik, günlük hayatta / sokakta doğrudan kullanılabilir (survival) İngilizce kelime/cümle/deyim pratik sorusu üret.

Sorular aşağıdaki JSON şemasına tam uymalıdır (JSON Array olarak):
[
  {{
    "id": 0,
    "type": "QUIZ_COMPLETION", // veya "QUIZ_MULTIPLE_CHOICE"
    "level": "{level}",
    "phrase": "İngilizce soru kalıbı (Boşluk doldurma ise '_____', çoktan seçmeli ise Türkçe soru cümlesi)",
    "translation": "İngilizce kelimenin/cümlenin Türkçe karşılığı veya doğru seçenek açıklaması",
    "context": "Kelimenin/deyiminin günlük hayatta/sokakta nerede ve nasıl kullanıldığına dair samimi, eğlenceli ve akılda kalıcı Türkçe açıklama.",
    "options": ["seçenek1", "seçenek2", "seçenek3", "seçenek4"], // 4 seçenek (doğru cevap dahil)
    "correctAnswer": "doğru_cevap",
    "category": "{category}",
    "variations": ["alternatif_ingilizce_cumle1", "alternatif_ingilizce_cumle2"] // Boşluk doldurma ise 2 alternatif cümle, çoktan seçmeli ise boş liste []
  }}
]

Kurallar:
1. Kesinlikle akademik gramer kuralları veya soyut kültürel bilgiler sorma! Tamamen seyahat kurtaran (taksi, havaalanı, otel, yemek siparişi, yol sorma vb.) veya iş yaşamında / günlük hayatta doğrudan konuşmayı kurtaracak pratik ifadeler olsun.
2. Soru cümleleri kısa, net ve anlaşılır olmalı. Her İngilizce cümle en fazla 12-15 kelime olmalıdır.
3. Sadece geçerli bir JSON dizisi döndür. Başlangıcına veya sonuna markdown (```json, ```) veya açıklama ekleme.
4. Metinlerin içerisinde kesinlikle kaçışlandırılmamış çift tırnak (") karakteri kullanma. Tırnak yerine tek tırnak (') kullan.
"""

    url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key={api_key}"
    payload = {
        "contents": [
            {
                "parts": [
                    {
                        "text": prompt
                    }
                ]
            }
        ],
        "generationConfig": {
            "responseMimeType": "application/json",
            "maxOutputTokens": 8192
        }
    }

    try:
        data = json.dumps(payload).encode('utf-8')
        req = urllib.request.Request(
            url,
            data=data,
            headers={'Content-Type': 'application/json'}
        )
        with urllib.request.urlopen(req, timeout=90) as response:
            res_data = json.loads(response.read().decode('utf-8'))
            content = res_data['candidates'][0]['content']['parts'][0]['text']
            parsed_json = json.loads(content)
            return parsed_json
    except urllib.error.HTTPError as e:
        print(f"HTTP Error generating for {level} + {category}: {e.code} {e.reason}")
        try:
            body = e.read().decode('utf-8')
            print(f"Response body: {body}")
        except Exception:
            pass
        return []
    except Exception as e:
        print(f"Error generating for {level} + {category}: {e}")
        return []

def main():
    api_key = os.environ.get("GEMINI_API_KEY")
    if not api_key:
        print("Error: GEMINI_API_KEY environment variable not found.")
        sys.exit(1)

    kt_file = "app/src/main/java/com/example/lingoscroll/data/LearningContent.kt"
    existing_items = parse_learning_content(kt_file)
    print(f"Loaded {len(existing_items)} existing questions from LearningContent.kt")

    # Group counts
    counts = {}
    for item in existing_items:
        key = (item["level"], item["category"])
        counts[key] = counts.get(key, 0) + 1

    # Enrich to at least 25 per combination
    enriched_items = list(existing_items)
    new_id_counter = 3001

    levels = ["BEGINNER", "INTERMEDIATE", "ADVANCED"]
    categories = ["TRAVEL", "BUSINESS", "CASUAL"]

    for lvl in levels:
        for cat in categories:
            current = counts.get((lvl, cat), 0)
            needed = 25 - current
            if needed > 0:
                # Retry loop up to 3 times in case of API glitch
                new_questions = []
                for attempt in range(3):
                    new_questions = generate_enrichment(api_key, lvl, cat, needed)
                    if new_questions:
                        break
                    print(f"Retry attempt {attempt+1} for {lvl} + {cat}...")
                    time.sleep(2)
                
                for q in new_questions:
                    q["id"] = new_id_counter
                    new_id_counter += 1
                    # Ensure defaults match schema
                    if "type" not in q:
                        q["type"] = "QUIZ_COMPLETION"
                    if "variations" not in q:
                        q["variations"] = []
                    enriched_items.append(q)
                
                # Sleep briefly to be nice to API limits
                time.sleep(1)

    print(f"Total question pool enriched to {len(enriched_items)} questions!")

    # Write to assets
    assets_dir = "app/src/main/assets"
    os.makedirs(assets_dir, exist_ok=True)
    output_file = os.path.join(assets_dir, "offline_questions.json")
    
    with open(output_file, "w", encoding="utf-8") as f:
        json.dump(enriched_items, f, ensure_ascii=False, indent=2)
        
    print(f"Successfully wrote {len(enriched_items)} questions to {output_file}")

if __name__ == "__main__":
    main()
