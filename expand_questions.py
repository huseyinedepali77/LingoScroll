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
print(f"Current maximum ID: {max_id}")

new_questions = [
    # ==================== CRISIS ====================
    # SKELETON
    {"category": "CRISIS", "mechanicType": "SKELETON", "scenarioTr": "Biri beni takip ediyor de.", "targetEn": "Someone is following me.", "options": [], "difficulty": 2},
    {"category": "CRISIS", "mechanicType": "SKELETON", "scenarioTr": "Polisi arayın, acil durum var de.", "targetEn": "Call the police, it is an emergency.", "options": [], "difficulty": 2},
    {"category": "CRISIS", "mechanicType": "SKELETON", "scenarioTr": "Çantamı arabada unuttum de.", "targetEn": "I left my bag in the car.", "options": [], "difficulty": 1},
    {"category": "CRISIS", "mechanicType": "SKELETON", "scenarioTr": "Bu çocuk kaybolmuş görünüyor de.", "targetEn": "This child seems to be lost.", "options": [], "difficulty": 2},
    {"category": "CRISIS", "mechanicType": "SKELETON", "scenarioTr": "Astım krizim var, ilacım nerede de.", "targetEn": "I have an asthma attack, where is my medicine?", "options": [], "difficulty": 3},
    {"category": "CRISIS", "mechanicType": "SKELETON", "scenarioTr": "Buranın adresi ne de.", "targetEn": "What is the address of this place?", "options": [], "difficulty": 1},
    {"category": "CRISIS", "mechanicType": "SKELETON", "scenarioTr": "Telefonumu şarj etmem gerek, acil durum de.", "targetEn": "I need to charge my phone, it is an emergency.", "options": [], "difficulty": 2},
    {"category": "CRISIS", "mechanicType": "SKELETON", "scenarioTr": "Beni yalnız bırakın lütfen de.", "targetEn": "Please leave me alone.", "options": [], "difficulty": 1},
    {"category": "CRISIS", "mechanicType": "SKELETON", "scenarioTr": "Pasaportumu otelde unuttum de.", "targetEn": "I forgot my passport at the hotel.", "options": [], "difficulty": 2},
    {"category": "CRISIS", "mechanicType": "SKELETON", "scenarioTr": "Lütfen sakin olun de.", "targetEn": "Please stay calm.", "options": [], "difficulty": 1},

    # SWIPE
    {"category": "CRISIS", "mechanicType": "SWIPE", "scenarioTr": "Biri seni rahatsız ediyor ve uzaklaşmasını istiyorsun.", "targetEn": "Go away or I will call for help.", "options": ["Go away or I will call for help.", "Can you show me the way?"], "difficulty": 2},
    {"category": "CRISIS", "mechanicType": "SWIPE", "scenarioTr": "Hırsızlığı bildirmek için karakola gitmek istiyorsun.", "targetEn": "I need to report a theft.", "options": ["I need to report a theft.", "I want to buy a ticket."], "difficulty": 2},
    {"category": "CRISIS", "mechanicType": "SWIPE", "scenarioTr": "Vücudunda ciddi bir alerjik reaksiyon başladı.", "targetEn": "I am having a severe allergic reaction.", "options": ["I am having a severe allergic reaction.", "I have a slight headache."], "difficulty": 3},
    {"category": "CRISIS", "mechanicType": "SWIPE", "scenarioTr": "Yemek yerken arkadaşın boğuluyor, acil yardım çağır.", "targetEn": "My friend is choking, help us.", "options": ["My friend is choking, help us.", "My friend wants to drink water."], "difficulty": 3},
    {"category": "CRISIS", "mechanicType": "SWIPE", "scenarioTr": "Otelin anahtarını kaybettiğini resepsiyona söyle.", "targetEn": "I lost my room key.", "options": ["I lost my room key.", "I want to check out."], "difficulty": 1},
    {"category": "CRISIS", "mechanicType": "SWIPE", "scenarioTr": "Dışarıda yangın olduğunu görüyorsun, insanları uyar.", "targetEn": "Fire! Get out of the building.", "options": ["Fire! Get out of the building.", "It is very hot outside."], "difficulty": 1},
    {"category": "CRISIS", "mechanicType": "SWIPE", "scenarioTr": "Hattın çekmiyor ve acil arama yapman gerek.", "targetEn": "I have no signal, is there a landline?", "options": ["I have no signal, is there a landline?", "Can I have your phone number?"], "difficulty": 2},

    # CHUNK
    {"category": "CRISIS", "mechanicType": "CHUNK", "scenarioTr": "Bana yardım edebilecek Türkçe bilen biri var mı?", "targetEn": "Is there anyone here who speaks Turkish?", "options": ["Is", "there", "anyone", "here", "who", "speaks", "Turkish?"], "difficulty": 2},
    {"category": "CRISIS", "mechanicType": "CHUNK", "scenarioTr": "En yakın hastaneye nasıl gidebilirim?", "targetEn": "How can I get to the nearest hospital?", "options": ["How", "can", "I", "get", "to", "the", "nearest", "hospital?"], "difficulty": 1},
    {"category": "CRISIS", "mechanicType": "CHUNK", "scenarioTr": "Lütfen ambulans çağırın, çok kan kaybediyor.", "targetEn": "Please call an ambulance, he is bleeding heavily.", "options": ["Please", "call", "an", "ambulance,", "he", "is", "bleeding", "heavily."], "difficulty": 3},
    {"category": "CRISIS", "mechanicType": "CHUNK", "scenarioTr": "Konsolosluk ile iletişime geçmek istiyorum.", "targetEn": "I want to contact the consulate.", "options": ["I", "want", "to", "contact", "the", "consulate."], "difficulty": 2},

    # ERROR_FIND
    {"category": "CRISIS", "mechanicType": "ERROR_FIND", "scenarioTr": "Kayboldum de. (losts/lost hatasını düzelt)", "targetEn": "I am completely lost.", "options": ["I am completely losts.", "losts", "lost"], "difficulty": 1},
    {"category": "CRISIS", "mechanicType": "ERROR_FIND", "scenarioTr": "Bana yardım et de. (helps/help hatasını düzelt)", "targetEn": "Please help me.", "options": ["Please helps me.", "helps", "help"], "difficulty": 1},
    {"category": "CRISIS", "mechanicType": "ERROR_FIND", "scenarioTr": "Doktora ihtiyacım var de. (doctors/doctor hatasını düzelt)", "targetEn": "I need a doctor.", "options": ["I need a doctors.", "doctors", "doctor"], "difficulty": 1},

    # ==================== NAVIGATION ====================
    # SKELETON
    {"category": "NAVIGATION", "mechanicType": "SKELETON", "scenarioTr": "En yakın tuvalet nerede de.", "targetEn": "Where is the nearest restroom?", "options": [], "difficulty": 1},
    {"category": "NAVIGATION", "mechanicType": "SKELETON", "scenarioTr": "Bilet gişesi nerede de.", "targetEn": "Where is the ticket office?", "options": [], "difficulty": 1},
    {"category": "NAVIGATION", "mechanicType": "SKELETON", "scenarioTr": "Bu tren hangi perondan kalkıyor de.", "targetEn": "Which platform does this train leave from?", "options": [], "difficulty": 2},
    {"category": "NAVIGATION", "mechanicType": "SKELETON", "scenarioTr": "Şehir merkezine giden otobüs hangisi de.", "targetEn": "Which bus goes to the city center?", "options": [], "difficulty": 2},
    {"category": "NAVIGATION", "mechanicType": "SKELETON", "scenarioTr": "Bunu haritada gösterebilir misiniz de.", "targetEn": "Can you show this on the map?", "options": [], "difficulty": 1},
    {"category": "NAVIGATION", "mechanicType": "SKELETON", "scenarioTr": "Giriş ücreti ne kadar de.", "targetEn": "How much is the entrance fee?", "options": [], "difficulty": 1},
    {"category": "NAVIGATION", "mechanicType": "SKELETON", "scenarioTr": "Doğru yolda mıyım de.", "targetEn": "Am I on the right way?", "options": [], "difficulty": 2},

    # SWIPE
    {"category": "NAVIGATION", "mechanicType": "SWIPE", "scenarioTr": "Otobüs durağının yerini sorman gerek.", "targetEn": "Where is the bus stop?", "options": ["Where is the bus stop?", "Do you sell tickets?"], "difficulty": 1},
    {"category": "NAVIGATION", "mechanicType": "SWIPE", "scenarioTr": "Uçağını kaçırmak üzeresin, hızlı geçiş talep et.", "targetEn": "I am going to miss my flight.", "options": ["I am going to miss my flight.", "Where is my seat?"], "difficulty": 3},
    {"category": "NAVIGATION", "mechanicType": "SWIPE", "scenarioTr": "Taksi çağırmak istediğini resepsiyona belirt.", "targetEn": "Could you call a taxi for me?", "options": ["Could you call a taxi for me?", "I want to walk to the airport."], "difficulty": 1},
    {"category": "NAVIGATION", "mechanicType": "SWIPE", "scenarioTr": "Gideceğin yerin ne kadar uzak olduğunu sor.", "targetEn": "How far is it from here?", "options": ["How far is it from here?", "How much does it cost?"], "difficulty": 2},

    # CHUNK
    {"category": "NAVIGATION", "mechanicType": "CHUNK", "scenarioTr": "Buraya yürüyerek gidebilir miyim?", "targetEn": "Can I walk there from here?", "options": ["Can", "I", "walk", "there", "from", "here?"], "difficulty": 1},
    {"category": "NAVIGATION", "mechanicType": "CHUNK", "scenarioTr": "Havaalanına gitmenin en hızlı yolu nedir?", "targetEn": "What is the fastest way to the airport?", "options": ["What", "is", "the", "fastest", "way", "to", "the", "airport?"], "difficulty": 2},
    {"category": "NAVIGATION", "mechanicType": "CHUNK", "scenarioTr": "Bir sonraki durakta inmem gerekiyor.", "targetEn": "I need to get off at the next stop.", "options": ["I", "need", "to", "get", "off", "at", "the", "next", "stop."], "difficulty": 2},

    # ERROR_FIND
    {"category": "NAVIGATION", "mechanicType": "ERROR_FIND", "scenarioTr": "Metro nerede de. (subways/subway hatasını düzelt)", "targetEn": "Where is the nearest subway station?", "options": ["Where is the nearest subways station?", "subways", "subway"], "difficulty": 1},
    {"category": "NAVIGATION", "mechanicType": "ERROR_FIND", "scenarioTr": "Bilet almak istiyorum de. (buys/buy hatasını düzelt)", "targetEn": "I want to buy a ticket.", "options": ["I want to buys a ticket.", "buys", "buy"], "difficulty": 1},

    # ==================== FINANCE ====================
    # SKELETON
    {"category": "FINANCE", "mechanicType": "SKELETON", "scenarioTr": "Hesabı alabilir miyim de.", "targetEn": "Can I have the bill, please?", "options": [], "difficulty": 1},
    {"category": "FINANCE", "mechanicType": "SKELETON", "scenarioTr": "Kredi kartı kabul ediyor musunuz de.", "targetEn": "Do you accept credit cards?", "options": [], "difficulty": 1},
    {"category": "FINANCE", "mechanicType": "SKELETON", "scenarioTr": "Döviz bürosu ne zaman açılıyor de.", "targetEn": "What time does the exchange office open?", "options": [], "difficulty": 2},
    {"category": "FINANCE", "mechanicType": "SKELETON", "scenarioTr": "Burada komisyon ücreti var mı de.", "targetEn": "Is there a commission fee here?", "options": [], "difficulty": 2},
    {"category": "FINANCE", "mechanicType": "SKELETON", "scenarioTr": "Makbuz alabilir miyim de.", "targetEn": "Can I get a receipt, please?", "options": [], "difficulty": 1},
    {"category": "FINANCE", "mechanicType": "SKELETON", "scenarioTr": "Bahşiş hesaba dahil mi de.", "targetEn": "Is the tip included in the bill?", "options": [], "difficulty": 2},

    # SWIPE
    {"category": "FINANCE", "mechanicType": "SWIPE", "scenarioTr": "Nakit ödemek istediğini söyle.", "targetEn": "I would like to pay in cash.", "options": ["I would like to pay in cash.", "Can I pay by credit card?"], "difficulty": 1},
    {"category": "FINANCE", "mechanicType": "SWIPE", "scenarioTr": "Ürünün fiyatının çok yüksek olduğunu belirt.", "targetEn": "This is too expensive for me.", "options": ["This is too expensive for me.", "It is very cheap."], "difficulty": 1},
    {"category": "FINANCE", "mechanicType": "SWIPE", "scenarioTr": "Kartının bloke olduğunu bankaya söyle.", "targetEn": "My credit card is blocked.", "options": ["My credit card is blocked.", "I forgot my wallet."], "difficulty": 2},
    {"category": "FINANCE", "mechanicType": "SWIPE", "scenarioTr": "Para iadesi almak istediğini mağazaya belirt.", "targetEn": "I want to get a refund.", "options": ["I want to get a refund.", "Keep the change."], "difficulty": 2},

    # CHUNK
    {"category": "FINANCE", "mechanicType": "CHUNK", "scenarioTr": "Hesabı ikiye bölebilir miyiz?", "targetEn": "Can we split the bill?", "options": ["Can", "we", "split", "the", "bill?"], "difficulty": 2},
    {"category": "FINANCE", "mechanicType": "CHUNK", "scenarioTr": "İndirim yapabilir misiniz?", "targetEn": "Can you give me a discount?", "options": ["Can", "you", "give", "me", "a", "discount?"], "difficulty": 1},
    {"category": "FINANCE", "mechanicType": "CHUNK", "scenarioTr": "En yakın ATM nerede acaba?", "targetEn": "Where is the nearest ATM?", "options": ["Where", "is", "the", "nearest", "ATM?"], "difficulty": 1},

    # ERROR_FIND
    {"category": "FINANCE", "mechanicType": "ERROR_FIND", "scenarioTr": "Hesap yanlış de. (wrongs/wrong hatasını düzelt)", "targetEn": "This bill is wrong.", "options": ["This bill is wrongs.", "wrongs", "wrong"], "difficulty": 1},
    {"category": "FINANCE", "mechanicType": "ERROR_FIND", "scenarioTr": "Kartımı yuttu de. (swallows/swallowed hatasını düzelt)", "targetEn": "The ATM swallowed my card.", "options": ["The ATM swallows my card.", "swallows", "swallowed"], "difficulty": 2},

    # ==================== BASIC_NEEDS ====================
    # SKELETON
    {"category": "BASIC_NEEDS", "mechanicType": "SKELETON", "scenarioTr": "Çok açım, restoran nerede de.", "targetEn": "I am very hungry, where is a restaurant?", "options": [], "difficulty": 1},
    {"category": "BASIC_NEEDS", "mechanicType": "SKELETON", "scenarioTr": "Wi-Fi şifresi nedir de.", "targetEn": "What is the Wi-Fi password?", "options": [], "difficulty": 1},
    {"category": "BASIC_NEEDS", "mechanicType": "SKELETON", "scenarioTr": "Bir bardak su alabilir miyim de.", "targetEn": "Can I have a glass of water?", "options": [], "difficulty": 1},
    {"category": "BASIC_NEEDS", "mechanicType": "SKELETON", "scenarioTr": "Şarj aleti kiralayabilir miyim de.", "targetEn": "Can I rent a charger?", "options": [], "difficulty": 2},
    {"category": "BASIC_NEEDS", "mechanicType": "SKELETON", "scenarioTr": "Bu oda çok soğuk, klimayı kapatın de.", "targetEn": "This room is too cold, turn off the AC.", "options": [], "difficulty": 2},
    {"category": "BASIC_NEEDS", "mechanicType": "SKELETON", "scenarioTr": "Vejetaryen yemekleriniz var mı de.", "targetEn": "Do you have vegetarian options?", "options": [], "difficulty": 2},

    # SWIPE
    {"category": "BASIC_NEEDS", "mechanicType": "SWIPE", "scenarioTr": "Sıcak bir çay içmek istiyorsun.", "targetEn": "I want a cup of hot tea.", "options": ["I want a cup of hot tea.", "I want to buy some shoes."], "difficulty": 1},
    {"category": "BASIC_NEEDS", "mechanicType": "SWIPE", "scenarioTr": "Oda temizliği rica etmek istiyorsun.", "targetEn": "Please clean my room.", "options": ["Please clean my room.", "Please clean the street."], "difficulty": 1},
    {"category": "BASIC_NEEDS", "mechanicType": "SWIPE", "scenarioTr": "Ağrı kesici almak istediğini eczacıya söyle.", "targetEn": "I need some painkillers.", "options": ["I need some painkillers.", "I need a new laptop."], "difficulty": 2},
    {"category": "BASIC_NEEDS", "mechanicType": "SWIPE", "scenarioTr": "Otelde kahvaltının ne zaman olduğunu sor.", "targetEn": "What time is breakfast?", "options": ["What time is breakfast?", "Is there a free table?"], "difficulty": 1},

    # CHUNK
    {"category": "BASIC_NEEDS", "mechanicType": "CHUNK", "scenarioTr": "Telefonum için priz dönüştürücü lazım.", "targetEn": "I need a plug adapter for my phone.", "options": ["I", "need", "a", "plug", "adapter", "for", "my", "phone."], "difficulty": 2},
    {"category": "BASIC_NEEDS", "mechanicType": "CHUNK", "scenarioTr": "Hesap lütfen.", "targetEn": "Could I have the bill, please?", "options": ["Could", "I", "have", "the", "bill,", "please?"], "difficulty": 1},
    {"category": "BASIC_NEEDS", "mechanicType": "CHUNK", "scenarioTr": "İnternet bağlantısı çalışmıyor.", "targetEn": "The internet connection is not working.", "options": ["The", "internet", "connection", "is", "not", "working."], "difficulty": 2},

    # ERROR_FIND
    {"category": "BASIC_NEEDS", "mechanicType": "ERROR_FIND", "scenarioTr": "Oda kirli de. (cleans/clean hatasını düzelt)", "targetEn": "My room is not clean.", "options": ["My room is not cleans.", "cleans", "clean"], "difficulty": 1},
    {"category": "BASIC_NEEDS", "mechanicType": "ERROR_FIND", "scenarioTr": "Rezervasyonum var de. (reservations/reservation hatasını düzelt)", "targetEn": "I have a booking reservation.", "options": ["I have a booking reservations.", "reservations", "reservation"], "difficulty": 1}
]

# Her soruya benzersiz bir ID ata
start_id = max_id + 1
added_count = 0
for q in new_questions:
    # Aynı targetEn olan bir soru zaten var mı kontrol et (çiftlemeyi önlemek için)
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
