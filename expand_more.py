import json
import os

questions_file = "app/src/main/assets/survival_questions.json"

if not os.path.exists(questions_file):
    print("Questions file not found!")
    exit(1)

with open(questions_file, "r", encoding="utf-8") as f:
    existing_questions = json.load(f)

print(f"Existing questions count: {len(existing_questions)}")
max_id = max(q["id"] for q in existing_questions)

new_questions = [
    # CRISIS
    {"category": "CRISIS", "mechanicType": "SKELETON", "scenarioTr": "Yardım edin, boğuluyorum de.", "targetEn": "Help me, I am drowning.", "options": [], "difficulty": 3},
    {"category": "CRISIS", "mechanicType": "SKELETON", "scenarioTr": "Bagajımı kaybettim, nerede bulabilirim de.", "targetEn": "I lost my luggage, where can I find it?", "options": [], "difficulty": 2},
    {"category": "CRISIS", "mechanicType": "SKELETON", "scenarioTr": "Bu ilaç reçetesiz satılıyor mu de.", "targetEn": "Is this medicine available without a prescription?", "options": [], "difficulty": 3},
    {"category": "CRISIS", "mechanicType": "SKELETON", "scenarioTr": "Buranın deprem toplanma alanı neresi de.", "targetEn": "Where is the earthquake assembly area here?", "options": [], "difficulty": 3},
    {"category": "CRISIS", "mechanicType": "SKELETON", "scenarioTr": "Lütfen polisi aramayın de.", "targetEn": "Please do not call the police.", "options": [], "difficulty": 2},
    {"category": "CRISIS", "mechanicType": "SKELETON", "scenarioTr": "Beni kurtarın de.", "targetEn": "Save me.", "options": [], "difficulty": 1},
    {"category": "CRISIS", "mechanicType": "SKELETON", "scenarioTr": "Telefonumu çaldılar, polisi arayın de.", "targetEn": "They stole my phone, call the police.", "options": [], "difficulty": 2},
    
    # NAVIGATION
    {"category": "NAVIGATION", "mechanicType": "SKELETON", "scenarioTr": "Buradan tren istasyonuna ne kadar sürede gidilir de.", "targetEn": "How long does it take to get to the train station from here?", "options": [], "difficulty": 3},
    {"category": "NAVIGATION", "mechanicType": "SKELETON", "scenarioTr": "Bana en yakın benzin istasyonunu gösterebilir misiniz de.", "targetEn": "Can you show me the nearest gas station?", "options": [], "difficulty": 2},
    {"category": "NAVIGATION", "mechanicType": "SKELETON", "scenarioTr": "Bu bilet tek yön mü yoksa gidiş-dönüş mü de.", "targetEn": "Is this ticket one-way or round-trip?", "options": [], "difficulty": 2},
    {"category": "NAVIGATION", "mechanicType": "SKELETON", "scenarioTr": "Şehir haritası alabilir miyim de.", "targetEn": "Can I have a city map?", "options": [], "difficulty": 1},
    {"category": "NAVIGATION", "mechanicType": "SKELETON", "scenarioTr": "İlk sağdan dönün de.", "targetEn": "Turn at the first right.", "options": [], "difficulty": 1},
    {"category": "NAVIGATION", "mechanicType": "SKELETON", "scenarioTr": "Burada taksi sırası nerede de.", "targetEn": "Where is the taxi stand here?", "options": [], "difficulty": 2},

    # FINANCE
    {"category": "FINANCE", "mechanicType": "SKELETON", "scenarioTr": "Fatura için ödeme vadesi nedir de.", "targetEn": "What is the payment deadline for the bill?", "options": [], "difficulty": 3},
    {"category": "FINANCE", "mechanicType": "SKELETON", "scenarioTr": "Bunu taksitle ödeyebilir miyim de.", "targetEn": "Can I pay this in installments?", "options": [], "difficulty": 2},
    {"category": "FINANCE", "mechanicType": "SKELETON", "scenarioTr": "Para üstü kalsın de.", "targetEn": "Keep the change.", "options": [], "difficulty": 1},
    {"category": "FINANCE", "mechanicType": "SKELETON", "scenarioTr": "Fiş alabilir miyim de.", "targetEn": "Can I have a receipt?", "options": [], "difficulty": 1},
    {"category": "FINANCE", "mechanicType": "SKELETON", "scenarioTr": "Bu fiyata vergi dahil mi de.", "targetEn": "Is tax included in this price?", "options": [], "difficulty": 2},
    {"category": "FINANCE", "mechanicType": "SKELETON", "scenarioTr": "Döviz kuru bugün kaç de.", "targetEn": "What is the exchange rate today?", "options": [], "difficulty": 2},

    # BASIC_NEEDS
    {"category": "BASIC_NEEDS", "mechanicType": "SKELETON", "scenarioTr": "Bana en yakın eczaneyi tarif edebilir misiniz de.", "targetEn": "Can you describe the way to the nearest pharmacy?", "options": [], "difficulty": 2},
    {"category": "BASIC_NEEDS", "mechanicType": "SKELETON", "scenarioTr": "Burada şarj aleti ödünç alabilir miyim de.", "targetEn": "Can I borrow a charger here?", "options": [], "difficulty": 2},
    {"category": "BASIC_NEEDS", "mechanicType": "SKELETON", "scenarioTr": "Lütfen temiz çarşaf getirin de.", "targetEn": "Please bring clean sheets.", "options": [], "difficulty": 1},
    {"category": "BASIC_NEEDS", "mechanicType": "SKELETON", "scenarioTr": "Ütüye ihtiyacım var, nerede bulabilirim de.", "targetEn": "I need an iron, where can I find one?", "options": [], "difficulty": 2},
    {"category": "BASIC_NEEDS", "mechanicType": "SKELETON", "scenarioTr": "Bu yemek çok acı mı de.", "targetEn": "Is this food very spicy?", "options": [], "difficulty": 1},
    {"category": "BASIC_NEEDS", "mechanicType": "SKELETON", "scenarioTr": "Burada Wi-Fi ücretsiz mi de.", "targetEn": "Is Wi-Fi free here?", "options": [], "difficulty": 1},

    # SWIPE CRISIS
    {"category": "CRISIS", "mechanicType": "SWIPE", "scenarioTr": "Kaza yaptın ve acil servisi aramak istiyorsun.", "targetEn": "I had a car accident, help me.", "options": ["I had a car accident, help me.", "Where can I rent a car?"], "difficulty": 2},
    {"category": "CRISIS", "mechanicType": "SWIPE", "scenarioTr": "Pasaportunun kayıp olduğunu polise bildirmek istiyorsun.", "targetEn": "My passport is missing.", "options": ["My passport is missing.", "Do you want to see my passport?"], "difficulty": 2},
    
    # SWIPE NAVIGATION
    {"category": "NAVIGATION", "mechanicType": "SWIPE", "scenarioTr": "Trenin saat kaçta kalktığını sorman gerek.", "targetEn": "What time does the train leave?", "options": ["What time does the train leave?", "Where is the train?"], "difficulty": 1},
    {"category": "NAVIGATION", "mechanicType": "SWIPE", "scenarioTr": "Müzenin açık olup olmadığını sorman gerek.", "targetEn": "Is the museum open today?", "options": ["Is the museum open today?", "Where is the museum?"], "difficulty": 1},

    # SWIPE FINANCE
    {"category": "FINANCE", "mechanicType": "SWIPE", "scenarioTr": "Fiyatı yazılı olarak istemek istiyorsun.", "targetEn": "Can you write down the price?", "options": ["Can you write down the price?", "I will pay with cash."], "difficulty": 1},
    {"category": "FINANCE", "mechanicType": "SWIPE", "scenarioTr": "Hesapta hata olduğunu garsona belirtmek istiyorsun.", "targetEn": "There is a mistake in the bill.", "options": ["There is a mistake in the bill.", "Can we get the bill?"], "difficulty": 2},

    # SWIPE BASIC_NEEDS
    {"category": "BASIC_NEEDS", "mechanicType": "SWIPE", "scenarioTr": "Bir taksi durağı sormak istiyorsun.", "targetEn": "Where is the nearest taxi stand?", "options": ["Where is the nearest taxi stand?", "How much is the taxi fare?"], "difficulty": 1},
    {"category": "BASIC_NEEDS", "mechanicType": "SWIPE", "scenarioTr": "Otel odasında sıcak su akmadığını resepsiyona söyle.", "targetEn": "There is no hot water in my room.", "options": ["There is no hot water in my room.", "I want to take a cold shower."], "difficulty": 2},

    # CHUNK CRISIS
    {"category": "CRISIS", "mechanicType": "CHUNK", "scenarioTr": "Acil durum çıkışı nerede bulunuyor?", "targetEn": "Where is the emergency exit located?", "options": ["Where", "is", "the", "emergency", "exit", "located?"], "difficulty": 2},
    {"category": "CRISIS", "mechanicType": "CHUNK", "scenarioTr": "Burada güvende miyiz?", "targetEn": "Are we safe here?", "options": ["Are", "we", "safe", "here?"], "difficulty": 1},

    # CHUNK NAVIGATION
    {"category": "NAVIGATION", "mechanicType": "CHUNK", "scenarioTr": "Taksiyle şehir merkezine gitmek ne kadar tutar?", "targetEn": "How much does it cost to get to the city center by taxi?", "options": ["How", "much", "does", "it", "cost", "to", "get", "to", "the", "city", "center", "by", "taxi?"], "difficulty": 3},
    {"category": "NAVIGATION", "mechanicType": "CHUNK", "scenarioTr": "Bu otobüs havaalanına gidiyor mu?", "targetEn": "Does this bus go to the airport?", "options": ["Does", "this", "bus", "go", "to", "the", "airport?"], "difficulty": 1},

    # CHUNK FINANCE
    {"category": "FINANCE", "mechanicType": "CHUNK", "scenarioTr": "Bana ödeme için bir makbuz verebilir misiniz?", "targetEn": "Can you give me a receipt for the payment?", "options": ["Can", "you", "give", "me", "a", "receipt", "for", "the", "payment?"], "difficulty": 2},
    {"category": "FINANCE", "mechanicType": "CHUNK", "scenarioTr": "Kredi kartıyla ödeyebilir miyim?", "targetEn": "Can I pay by credit card?", "options": ["Can", "I", "pay", "by", "credit", "card?"], "difficulty": 1},

    # CHUNK BASIC_NEEDS
    {"category": "BASIC_NEEDS", "mechanicType": "CHUNK", "scenarioTr": "Wi-Fi şifresini alabilir miyim?", "targetEn": "Could I have the Wi-Fi password?", "options": ["Could", "I", "have", "the", "Wi-Fi", "password?"], "difficulty": 1},
    {"category": "BASIC_NEEDS", "mechanicType": "CHUNK", "scenarioTr": "Sıcak su ile ilgili bir sorun var.", "targetEn": "There is a problem with the hot water.", "options": ["There", "is", "a", "problem", "with", "the", "hot", "water."], "difficulty": 2},

    # ERROR_FIND CRISIS
    {"category": "CRISIS", "mechanicType": "ERROR_FIND", "scenarioTr": "Beni yalnız bırakın de. (leaves/leave hatasını düzelt)", "targetEn": "Please leave me alone.", "options": ["Please leaves me alone.", "leaves", "leave"], "difficulty": 1},
    {"category": "CRISIS", "mechanicType": "ERROR_FIND", "scenarioTr": "Güvende değiliz de. (safes/safe hatasını düzelt)", "targetEn": "We are not safe.", "options": ["We are not safes.", "safes", "safe"], "difficulty": 1},

    # ERROR_FIND NAVIGATION
    {"category": "NAVIGATION", "mechanicType": "ERROR_FIND", "scenarioTr": "Harita alabilir miyim de. (maps/map hatasını düzelt)", "targetEn": "Can I get a map?", "options": ["Can I get a maps.", "maps", "map"], "difficulty": 1},
    {"category": "NAVIGATION", "mechanicType": "ERROR_FIND", "scenarioTr": "Taksi nerede de. (taxis/taxi hatasını düzelt)", "targetEn": "Where is the taxi?", "options": ["Where is the taxis?", "taxis", "taxi"], "difficulty": 1},

    # ERROR_FIND FINANCE
    {"category": "FINANCE", "mechanicType": "ERROR_FIND", "scenarioTr": "Fiyat ne kadar de. (costs/cost hatasını düzelt)", "targetEn": "How much does it cost?", "options": ["How much does it costs?", "costs", "cost"], "difficulty": 1},
    {"category": "FINANCE", "mechanicType": "ERROR_FIND", "scenarioTr": "Kartımı kabul edin de. (accepts/accept hatasını düzelt)", "targetEn": "Do you accept cards?", "options": ["Do you accepts cards?", "accepts", "accept"], "difficulty": 1},

    # ERROR_FIND BASIC_NEEDS
    {"category": "BASIC_NEEDS", "mechanicType": "ERROR_FIND", "scenarioTr": "Su istiyorum de. (waters/water hatasını düzelt)", "targetEn": "I need some water.", "options": ["I need some waters.", "waters", "water"], "difficulty": 1},
    {"category": "BASIC_NEEDS", "mechanicType": "ERROR_FIND", "scenarioTr": "Şifre nedir de. (passwords/password hatasını düzelt)", "targetEn": "What is the password?", "options": ["What is the passwords?", "passwords", "password"], "difficulty": 1}
]

start_id = max_id + 1
added_count = 0
for q in new_questions:
    exists = any(eq["targetEn"].lower() == q["targetEn"].lower() for eq in existing_questions)
    if not exists:
        q["id"] = start_id
        existing_questions.append(q)
        start_id += 1
        added_count += 1

print(f"Added {added_count} brand-new unique questions.")
print(f"New total questions count: {len(existing_questions)}")

with open(questions_file, "w", encoding="utf-8") as f:
    json.dump(existing_questions, f, ensure_ascii=False, indent=2)

print("Questions successfully updated!")
