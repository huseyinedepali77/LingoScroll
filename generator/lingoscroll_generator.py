#!/usr/bin/env python3
import os
import sys
import json
import urllib.request
import urllib.parse
import xml.etree.ElementTree as ET

# RSS Feed URLs
FEEDS = {
    "BBC World News": "http://feeds.bbci.co.uk/news/world/rss.xml",
    "TechCrunch": "https://techcrunch.com/feed/",
    "Science & Space": "http://feeds.bbci.co.uk/news/science_and_environment/rss.xml",
    "Sports News": "https://www.skysports.com/rss/12040",
    "Weird & Viral": "https://news.yahoo.com/rss/weird"
}

def fetch_rss_headlines():
    headlines = []
    print("Canlı RSS haberleri çekiliyor...")
    for name, url in FEEDS.items():
        try:
            req = urllib.request.Request(
                url, 
                headers={'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'}
            )
            with urllib.request.urlopen(req, timeout=8) as response:
                xml_data = response.read()
                root = ET.fromstring(xml_data)
                count = 0
                for item in root.findall('.//item'):
                    title = item.find('title')
                    if title is not None and title.text:
                        headlines.append(f"[{name}] {title.text}")
                        count += 1
                        if count >= 4: # Her kaynaktan en fazla 4 başlık al (Toplam ~20 başlık)
                            break
            print(f"-> {name} kaynağından {count} başlık başarıyla çekildi.")
        except Exception as e:
            print(f"-> {name} çekilirken hata oluştu: {e}")
    return headlines

def generate_feed_json(api_key, headlines):
    if not headlines:
        print("Hata: Çekilmiş haber başlığı bulunamadı.")
        return None

    print("\nGemini API üzerinden LingoScroll günlük soruları üretiliyor...")
    
    # Prompt hazırlığı
    headlines_text = "\n".join(headlines)
    prompt = f"""Günün şu güncel haber başlıklarını (dünya gündemi, teknoloji, bilim/uzay, spor haberleri ve tuhaf/viral/komik haber olayları) incele:
{headlines_text}

Bu haberlerde geçen anahtar kelimelerden, teknolojik gelişmelerden, komik/viral durumlardan, spor terimlerinden veya seyahat durumlarından yola çıkarak LingoScroll uygulaması için tam 6 adet güncel pratik test sorusu üret.

Sorular aşağıdaki şemaya tam uymalıdır (JSON Array olarak):
[
  {{
    "id": 2001,
    "type": "QUIZ_COMPLETION", // veya "QUIZ_MULTIPLE_CHOICE"
    "level": "INTERMEDIATE", // "BEGINNER", "INTERMEDIATE" veya "ADVANCED"
    "phrase": "İngilizce soru kalıbı (Boşluk doldurma ise '_____', çoktan seçmeli ise Türkçe soru cümlesi)",
    "translation": "İngilizce kelimenin/cümlenin Türkçe karşılığı veya doğru seçenek açıklaması",
    "context": "Haberde veya günlük hayatta geçen kelimenin/deyiminin sokakta nerede ve nasıl kullanıldığına dair detaylı Türkçe açıklama. Eğlenceli, akılda kalıcı ve samimi bir ton kullan.",
    "options": ["seçenek1", "seçenek2", "seçenek3", "seçenek4"], // 4 seçenek (doğru cevap dahil)
    "correctAnswer": "doğru_cevap",
    "category": "BUSINESS", // "TRAVEL", "BUSINESS" veya "CASUAL"
    "variations": ["alternatif_ingilizce_cumle1", "alternatif_ingilizce_cumle2"] // Boşluk doldurma ise 2 alternatif cümle, çoktan seçmeli ise boş liste []
  }}
]

Kurallar:
1. Ürettiğin 6 sorunun dağılımı: 2 adet BEGINNER, 2 adet INTERMEDIATE, 2 adet ADVANCED olmalıdır.
2. İlgi çeken tuhaf/viral haberleri ve spor/uzay haberlerini de mutlaka sorulara yansıt. Kelime açıklamaları eğlenceli ve merak uyandırıcı olsun.
3. Sadece geçerli bir JSON dizisi çıktısı ver. Çıktının başına veya sonuna ```json, ``` veya herhangi bir markdown/metin açıklaması ekleme, doğrudan raw JSON döndür.
4. "options" dizisini kesinlikle boş bırakma. "type" değeri QUIZ_COMPLETION (boşluk doldurma) olsa dahi, kullanıcının boşluğa yerleştirebilmesi için 1 adet doğru cevap ve 3 adet çeldirici olmak üzere tam 4 seçeneği mutlaka listele!
"""

    # Gemini REST Endpoint
    url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key={api_key}"
    
    payload = {
        "contents": [
            {
                "parts": [
                    {"text": prompt}
                ]
            }
        ],
        "generationConfig": {
            "responseMimeType": "application/json"
        }
    }

    try:
        data = json.dumps(payload).encode('utf-8')
        req = urllib.request.Request(
            url,
            data=data,
            headers={'Content-Type': 'application/json'}
        )
        with urllib.request.urlopen(req, timeout=30) as response:
            res_data = json.loads(response.read().decode('utf-8'))
            
            # Gemini JSON yanıtını çözümle
            content = res_data['candidates'][0]['content']['parts'][0]['text']
            
            # JSON'ı parse edip doğrula
            parsed_json = json.loads(content)
            return parsed_json
    except Exception as e:
        print(f"Gemini API çağrısı sırasında hata oluştu: {e}")
        return None

def main():
    api_key = os.environ.get("GEMINI_API_KEY")
    
    # Argüman kontrolü
    if len(sys.argv) > 2 and sys.argv[1] in ["--key", "-k"]:
        api_key = sys.argv[2]
        
    if not api_key:
        print("Hata: GEMINI_API_KEY ortam değişkeni veya --key parametresi bulunamadı.")
        print("Kullanım: python lingoscroll_generator.py --key YOUR_GEMINI_API_KEY")
        sys.exit(1)

    headlines = fetch_rss_headlines()
    if not headlines:
        print("Haber başlıkları çekilemediği için işlem sonlandırıldı.")
        sys.exit(1)

    feed_cards = generate_feed_json(api_key, headlines)
    if feed_cards:
        output_file = "feed.json"
        try:
            with open(output_file, "w", encoding="utf-8") as f:
                json.dump(feed_cards, f, ensure_ascii=False, indent=2)
            print(f"\n[BAŞARILI] {len(feed_cards)} adet gündem sorusu üretildi ve '{output_file}' dosyasına yazıldı!")
            print("Bu dosyayı GitHub'a yükleyerek veya yerel sunucunuzda yayınlayarak LingoScroll'a bağlayabilirsiniz.")
        except Exception as e:
            print(f"Dosya yazma hatası: {e}")
    else:
        print("\n[HATA] Yapay zeka gündem soruları üretilemedi.")
        sys.exit(1)

if __name__ == "__main__":
    main()
