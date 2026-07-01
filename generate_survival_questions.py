# -*- coding: utf-8 -*-
import json
import os

questions = []

# --- CRISIS ---
# SKELETON
crisis_skeleton = [
    ("Taksici aşırı yüksek fiyat istedi, polisi arayacağını söyle.", "I will call the police.", 1),
    ("Cüzdanını çaldırdın, birinden yardım iste.", "Someone stole my wallet.", 1),
    ("Acil tıbbi yardıma ihtiyacın var.", "I need an ambulance.", 2),
    ("Pasaportunu kaybettiğini söyle.", "I lost my passport.", 2),
    ("Biri seni takip ediyor, yardım iste.", "This person is following me.", 2),
    ("Yangın var, milleti uyar.", "There is a fire here.", 1),
    ("Birinin canı acıyor, doktor çağırın de.", "Please call a doctor.", 1),
    ("Saldırıya uğradın, imdat diye bağır.", "Help me, I am attacked.", 2),
    ("En yakın karakolun yerini sor.", "Where is the police station?", 2),
    ("Kayıp olduğunu ve yardıma ihtiyacın olduğunu belirt.", "I am lost, help me.", 1),
    ("İlaçlarını otelde unuttun.", "I left my medicine at the hotel.", 3),
    ("Astım krizi geçiriyorsun, ilacını iste.", "I need my inhaler.", 3),
    ("Alerjin olduğunu ve nefes alamadığını söyle.", "I cannot breathe.", 3)
]
for i, (scen, target, diff) in enumerate(crisis_skeleton):
    questions.append({
        "id": 1000 + i,
        "category": "CRISIS",
        "mechanicType": "SKELETON",
        "scenarioTr": scen,
        "targetEn": target,
        "options": [],
        "difficulty": diff
    })

# SWIPE
crisis_swipe = [
    ("Tehditkar bir satıcı peşini bırakmıyor. Ne dersin?", "Leave me alone!", ["Leave me alone!", "Here is my money."], 1),
    ("Taksici taksimetreyi açmayı reddediyor. Tepkin ne olur?", "Turn on the meter, please.", ["Turn on the meter, please.", "Okay, I will pay whatever."], 1),
    ("Biri çantanı kapıp kaçıyor. Hangisini bağırırsın?", "Stop that thief!", ["Stop that thief!", "Goodbye bag!"], 1),
    ("Yabancı biri otel odana girmeye çalışıyor.", "Get away from my door!", ["Get away from my door!", "Come in, please."], 2),
    ("Hesapta içmediğin içkiler var. İtiraz et.", "I did not order this.", ["I did not order this.", "I will pay for everyone."], 2),
    ("Biri seni zorla bir yere götürmeye çalışıyor.", "I will not go with you.", ["I will not go with you.", "Okay, lead the way."], 2),
    ("Sana uyuşturucu satmaya çalışıyorlar.", "No, get away from me.", ["No, get away from me.", "How much is it?"], 1),
    ("Otobüste biri cüzdanına dokunuyor.", "Hands off my bag!", ["Hands off my bag!", "Take whatever you want."], 2),
    ("Acil çıkış kapısını sorman gerek.", "Where is the emergency exit?", ["Where is the emergency exit?", "Where is the lobby?"], 1),
    ("Konsolosluğu aramak istediğini belirt.", "I must call my embassy.", ["I must call my embassy.", "I want to go to a museum."], 3),
    ("Araban yolda kaldı, çekici çağır.", "I need a tow truck.", ["I need a tow truck.", "I want to rent a bike."], 3),
    ("Şüpheli bir paket gördün, görevliyi uyar.", "There is a suspicious bag here.", ["There is a suspicious bag here.", "Whose bag is this?"], 3),
    ("Deprem oluyor, insanları uyar.", "Get under the table!", ["Get under the table!", "Run out of the window!"], 2)
]
for i, (scen, target, opts, diff) in enumerate(crisis_swipe):
    questions.append({
        "id": 1100 + i,
        "category": "CRISIS",
        "mechanicType": "SWIPE",
        "scenarioTr": scen,
        "targetEn": target,
        "options": opts,
        "difficulty": diff
    })

# CHUNK
crisis_chunk = [
    ("Polisi aramakla tehdit et.", "I will call the police now.", ["I will", "call the", "police now."], 1),
    ("Cüzdanının çalındığını polise bildir.", "My wallet was stolen from my pocket.", ["My wallet", "was stolen", "from my pocket."], 2),
    ("Acil servis numarasını sor.", "What is the emergency phone number?", ["What is", "the emergency", "phone number?"], 1),
    ("Şeker hastası olduğunu ve insüline ihtiyacın olduğunu söyle.", "I am diabetic and I need insulin.", ["I am diabetic", "and I need", "insulin."], 3),
    ("Yaralı biri için ilk yardım kiti iste.", "We need a first aid kit immediately.", ["We need a", "first aid kit", "immediately."], 2),
    ("En yakın hastaneye acil gitmen gerek.", "Take me to the nearest hospital please.", ["Take me to", "the nearest hospital", "please."], 2),
    ("Biri tarafından takip edildiğini polise söyle.", "A man is following me down the street.", ["A man is", "following me", "down the street."], 3),
    ("Pasaportunun çalındığını konsolosluğa bildir.", "My passport was stolen from my room.", ["My passport", "was stolen", "from my room."], 3),
    ("Kaza yerinde ambulans çağrılmasını iste.", "Please call an ambulance for the driver.", ["Please call", "an ambulance", "for the driver."], 2),
    ("Çocuğunun kaybolduğunu söyle.", "My child is lost in the mall.", ["My child", "is lost in", "the mall."], 2),
    ("Astım krizi geçiren biri için yardım çağır.", "He is having an asthma attack now.", ["He is having", "an asthma attack", "now."], 3),
    ("Gıda zehirlenmesi geçirdiğini belirt.", "I think I have food poisoning.", ["I think", "I have", "food poisoning."], 3),
    ("Otobüs terminalinde çantanı unuttuğunu söyle.", "I left my backpack on the bus.", ["I left my", "backpack on", "the bus."], 1),
    ("Kilitli kaldığını ve yardım istediğini söyle.", "I am locked inside the restroom.", ["I am locked", "inside the", "restroom."], 2)
]
for i, (scen, target, chunks, diff) in enumerate(crisis_chunk):
    questions.append({
        "id": 1200 + i,
        "category": "CRISIS",
        "mechanicType": "CHUNK",
        "scenarioTr": scen,
        "targetEn": target,
        "options": chunks,
        "difficulty": diff
    })

# ERROR_FIND
crisis_error = [
    ("Acil ambulans çağır de. (an/a hatasını düzelt)", "I need an ambulance.", ["I need a ambulance.", "a", "an"], 2),
    ("Cüzdanım çalındı de. (stolen/stealed hatasını düzelt)", "My wallet was stolen.", ["My wallet was stealed.", "stealed", "stolen"], 1),
    ("Doktor çağır de. (call/calling hatasını düzelt)", "Please call a doctor.", ["Please calling a doctor.", "calling", "call"], 1),
    ("Polisi arayacağım de. (call/will call hatasını düzelt)", "I will call the police.", ["I calling the police.", "calling", "will call"], 2),
    ("Burada güvende değilim de. (safe/safely hatasını düzelt)", "I am not safe here.", ["I am not safely here.", "safely", "safe"], 2),
    ("Biri beni takip ediyor de. (following/followed hatasını düzelt)", "Someone is following me.", ["Someone is followed me.", "followed", "following"], 2),
    ("Pasaportumu kaybettim de. (lost/lose hatasını düzelt)", "I lost my passport.", ["I lose my passport.", "lose", "lost"], 1),
    ("Anahtarımı içeride unuttum de. (left/leave hatasını düzelt)", "I left my key inside.", ["I leave my key inside.", "leave", "left"], 2),
    ("Nefes alamıyorum de. (breathe/breath hatasını düzelt)", "I cannot breathe.", ["I cannot breath.", "breath", "breathe"], 3),
    ("Yardıma ihtiyacım var de. (need/needing hatasını düzelt)", "I need help.", ["I am needing help.", "am needing", "need"], 1),
    ("Acil durum çıkışı nerede de. (Where is/Where are hatasını düzelt)", "Where is the emergency exit?", ["Where are the emergency exit?", "are", "is"], 2),
    ("Alerjim var de. (to/for hatasını düzelt)", "I am allergic to peanuts.", ["I am allergic for peanuts.", "for", "to"], 3),
    ("Beni yalnız bırak de. (alone/lonely hatasını düzelt)", "Leave me alone.", ["Leave me lonely.", "lonely", "alone"], 2),
    ("Yolumu kaybettim de. (lost/lose hatasını düzelt)", "I am lost.", ["I am lose.", "lose", "lost"], 1)
]
for i, (scen, target, opts, diff) in enumerate(crisis_error):
    questions.append({
        "id": 1300 + i,
        "category": "CRISIS",
        "mechanicType": "ERROR_FIND",
        "scenarioTr": scen,
        "targetEn": target,
        "options": opts,
        "difficulty": diff
    })


# --- NAVIGATION ---
# SKELETON
nav_skeleton = [
    ("En yakın metro istasyonu nerede diye sor.", "Where is the nearest subway station?", 1),
    ("Adresi haritada gösterebilir misiniz diye sor.", "Can you show me on the map?", 1),
    ("Bu tren Londra'ya gidiyor mu diye sor.", "Does this train go to London?", 2),
    ("Yürüyerek ne kadar sürer diye sor.", "How long does it take to walk?", 2),
    ("Bilet gişesi nerede diye sor.", "Where is the ticket office?", 1),
    ("Kayboldum, tren istasyonunu arıyorum.", "I am looking for the train station.", 1),
    ("Buradan otobüs geçiyor mu diye sor.", "Does a bus stop here?", 2),
    ("Hangi perondan kalkıyor diye sor.", "Which platform does it leave from?", 2),
    ("Bir sonraki otobüs saat kaçta diye sor.", "What time is the next bus?", 1),
    ("Taksi durağı nerede diye sor.", "Where is the taxi stand?", 1),
    ("Gidiş-dönüş bilet almak istediğini söyle.", "I want a round-trip ticket.", 3),
    ("En hızlı yol hangisi diye sor.", "Which way is the fastest?", 3),
    ("Bu adrese nasıl gidebilirim diye sor.", "How do I get to this address?", 2)
]
for i, (scen, target, diff) in enumerate(nav_skeleton):
    questions.append({
        "id": 2000 + i,
        "category": "NAVIGATION",
        "mechanicType": "SKELETON",
        "scenarioTr": scen,
        "targetEn": target,
        "options": [],
        "difficulty": diff
    })

# SWIPE
nav_swipe = [
    ("Havaalanına gitmek için hangisini söylersin?", "To the airport, please.", ["To the airport, please.", "I want to buy shoes."], 1),
    ("Metro kartı almak için ne dersin?", "Where can I buy a metro card?", ["Where can I buy a metro card?", "I like this station."], 1),
    ("Otobüs şoförüne doğru yöne gidip gitmediğini sor.", "Is this bus going downtown?", ["Is this bus going downtown?", "Do you have coffee?"], 2),
    ("Taksi çağırılmasını istiyorsun.", "Can you call a taxi for me?", ["Can you call a taxi for me?", "Where is my bicycle?"], 2),
    ("Harita satın almak istiyorsun.", "I need a tourist map.", ["I need a tourist map.", "I want to swim."], 1),
    ("Otobüsün ücretini soracaksın.", "How much is the bus fare?", ["How much is the bus fare?", "What is your name?"], 2),
    ("Trenin rötar yapıp yapmadığını sor.", "Is the train delayed?", ["Is the train delayed?", "Is the train fast?"], 2),
    ("Yürüyerek gidip gidilemeyeceğini sor.", "Can I walk there?", ["Can I walk there?", "Can I fly there?"], 1),
    ("Hangi otobüse bineceğini sor.", "Which bus should I take?", ["Which bus should I take?", "Where is the beach?"], 2),
    ("Biletini iptal etmek istiyorsun.", "I want to cancel my ticket.", ["I want to cancel my ticket.", "I want to buy a ticket."], 3),
    ("Koltuk numarasını soracaksın.", "Where is my seat?", ["Where is my seat?", "Where is the driver?"], 1),
    ("İneceğin durağı kaçırdın.", "I missed my stop.", ["I missed my stop.", "I love this stop."], 2),
    ("Müzenin giriş ücretini sor.", "How much is the entrance fee?", ["How much is the entrance fee?", "Where is the museum?"], 2)
]
for i, (scen, target, opts, diff) in enumerate(nav_swipe):
    questions.append({
        "id": 2100 + i,
        "category": "NAVIGATION",
        "mechanicType": "SWIPE",
        "scenarioTr": scen,
        "targetEn": target,
        "options": opts,
        "difficulty": diff
    })

# CHUNK
nav_chunk = [
    ("En yakın otobüs durağını sor.", "Where is the nearest bus stop?", ["Where is", "the nearest", "bus stop?"], 1),
    ("Merkeze nasıl gideceğini sor.", "How can I go to the city center?", ["How can I", "go to the", "city center?"], 2),
    ("Bunun doğru yol olup olmadığını sor.", "Is this the correct way to the museum?", ["Is this", "the correct way", "to the museum?"], 2),
    ("Tek yön bilet istediğini belirt.", "I would like to buy a one-way ticket.", ["I would like", "to buy a", "one-way ticket."], 3),
    ("Sola dönüp düz gitmesini söyle.", "Turn left and go straight ahead.", ["Turn left", "and go", "straight ahead."], 1),
    ("Uçuş kapısını sor.", "Which gate does the flight leave from?", ["Which gate", "does the flight", "leave from?"], 3),
    ("Bagaj alım alanını sor.", "Where is the baggage claim area?", ["Where is", "the baggage", "claim area?"], 2),
    ("Pasaport kontrolünün nerede olduğunu sor.", "Where is the passport control?", ["Where is", "the passport", "control?"], 2),
    ("Otobüsün kalkış saatini öğren.", "When does the next bus leave?", ["When does", "the next bus", "leave?"], 1),
    ("Karşı tarafa geçmesini söyle.", "Cross the street and walk two blocks.", ["Cross the street", "and walk", "two blocks."], 2),
    ("Otobüs kartı yükleme yerini sor.", "Where can I top up my card?", ["Where can I", "top up", "my card?"], 3),
    ("Taksiyle ne kadar süreceğini sor.", "How long will it take by taxi?", ["How long", "will it take", "by taxi?"], 2),
    ("Bu trene binmek zorunda mıyım diye sor.", "Do I need to change trains?", ["Do I need", "to change", "trains?"], 3),
    ("Danışma masasını aradığını söyle.", "I am looking for the information desk.", ["I am looking", "for the information", "desk."], 1)
]
for i, (scen, target, chunks, diff) in enumerate(nav_chunk):
    questions.append({
        "id": 2200 + i,
        "category": "NAVIGATION",
        "mechanicType": "CHUNK",
        "scenarioTr": scen,
        "targetEn": target,
        "options": chunks,
        "difficulty": diff
    })

# ERROR_FIND
nav_error = [
    ("Buraya yakın mı de. (near/nearly hatasını düzelt)", "Is it near here?", ["Is it nearly here?", "nearly", "near"], 1),
    ("Metro istasyonu nerede de. (Where is/Where are hatasını düzelt)", "Where is the subway station?", ["Where are the subway station?", "are", "is"], 1),
    ("Haritada göster de. (on/at hatasını düzelt)", "Show me on the map.", ["Show me at the map.", "at", "on"], 2),
    ("Yürüyerek gidemezsin de. (by foot/on foot hatasını düzelt)", "You cannot go on foot.", ["You cannot go by foot.", "by foot", "on foot"], 2),
    ("Sola dön de. (turn/turning hatasını düzelt)", "Turn to the left.", ["Turning to the left.", "Turning", "Turn"], 1),
    ("Metro kartı almak istiyorum de. (buy/buying hatasını düzelt)", "I want to buy a card.", ["I want to buying a card.", "buying", "buy"], 1),
    ("Taksi çağır de. (call/calling hatasını düzelt)", "Can you call a taxi?", ["Can you calling a taxi?", "calling", "call"], 2),
    ("İstasyonu arıyorum de. (looking for/looking to hatasını düzelt)", "I am looking for the station.", ["I am looking to the station.", "looking to", "looking for"], 2),
    ("Tren ne zaman kalkıyor de. (does/is hatasını düzelt)", "When does the train leave?", ["When is the train leave?", "is", "does"], 2),
    ("İki bilet istiyorum de. (tickets/ticket hatasını düzelt)", "I want two tickets.", ["I want two ticket.", "ticket", "tickets"], 1),
    ("Trafik çok sıkışık de. (heavy/heavily hatasını düzelt)", "The traffic is heavy.", ["The traffic is heavily.", "heavily", "heavy"], 3),
    ("Buradan uzak mı de. (far from/far to hatasını düzelt)", "Is it far from here?", ["Is it far to here?", "far to", "far from"], 2),
    ("Otobüse bin de. (get on/get in hatasını düzelt)", "Get on the bus.", ["Get in the bus.", "Get in", "Get on"], 3),
    ("Bilet gişesi nerede de. (ticket/tickets hatasını düzelt)", "Where is the ticket office?", ["Where is the tickets office?", "tickets", "ticket"], 2)
]
for i, (scen, target, opts, diff) in enumerate(nav_error):
    questions.append({
        "id": 2300 + i,
        "category": "NAVIGATION",
        "mechanicType": "ERROR_FIND",
        "scenarioTr": scen,
        "targetEn": target,
        "options": opts,
        "difficulty": diff
    })


# --- FINANCE ---
# SKELETON
fin_skeleton = [
    ("Hesabı kredi kartıyla ödemek istediğini söyle.", "Can I pay by credit card?", 1),
    ("Nakit ödemek istediğini söyle.", "I would like to pay in cash.", 1),
    ("Bahşiş hesaba dahil mi diye sor.", "Is the tip included?", 2),
    ("Faturada bir hata olduğunu belirt.", "There is a mistake on the bill.", 2),
    ("Döviz bürosu nerede diye sor.", "Where is the currency exchange?", 2),
    ("Bunu bozdurabilir miyim diye sor.", "Can you break this bill?", 3),
    ("Faturayı bölebilir miyiz diye sor.", "Can we split the bill?", 1),
    ("Banka kartımın ATM'de sıkıştığını söyle.", "My card is stuck in the ATM.", 3),
    ("Komisyon ücreti ne kadar diye sor.", "How much is the commission fee?", 3),
    ("En yakın ATM nerede diye sor.", "Where is the nearest ATM?", 1),
    ("Bunun için makbuz alabilir miyim diye sor.", "Can I get a receipt for this?", 2),
    ("Hesabı istemek için ne dersin?", "Could we have the bill, please?", 1),
    ("Daha ucuz bir oda var mı diye sor.", "Do you have a cheaper room?", 2)
]
for i, (scen, target, diff) in enumerate(fin_skeleton):
    questions.append({
        "id": 3000 + i,
        "category": "FINANCE",
        "mechanicType": "SKELETON",
        "scenarioTr": scen,
        "targetEn": target,
        "options": [],
        "difficulty": diff
    })

# SWIPE
fin_swipe = [
    ("Ödeme yaparken 'Üstü kalsın' demek istiyorsun.", "Keep the change.", ["Keep the change.", "Give me more money."], 1),
    ("ATM'nin çalışmadığını görevliye bildireceksin.", "This ATM is not working.", ["This ATM is not working.", "I love this bank."], 1),
    ("Hesabı ödemek istediğini garsona söyle.", "Can I have the check, please?", ["Can I have the check, please?", "I want more food."], 1),
    ("Kartının reddedildiğini söyleyen satıcıya cevap ver.", "I will try another card.", ["I will try another card.", "Keep my card anyway."], 2),
    ("İndirim istemek için ne dersin?", "Can you give me a discount?", ["Can you give me a discount?", "I will pay double."], 1),
    ("Fiyatın çok pahalı olduğunu belirteceksin.", "It is too expensive.", ["It is too expensive.", "It is very cheap."], 1),
    ("Doları Euro'ya çevirmek istediğini söyle.", "I want to exchange dollars to euros.", ["I want to exchange dollars to euros.", "I want to buy food."], 2),
    ("Kart şifreni unuttuğunu söyle.", "I forgot my PIN code.", ["I forgot my PIN code.", "My card is beautiful."], 2),
    ("Gizli bir ücret olup olmadığını sor.", "Are there any hidden fees?", ["Are there any hidden fees?", "Is it free?"], 3),
    ("Bahşiş bırakmak istiyorsun.", "This tip is for you.", ["This tip is for you.", "Give me some cash."], 2),
    ("Kayıp kartını bildireceksin.", "I need to freeze my card.", ["I need to freeze my card.", "I want a new bank account."], 3),
    ("İade almak istediğini belirt.", "I want to get a refund.", ["I want to get a refund.", "Keep the money."], 3),
    ("Girişin ücretsiz olup olmadığını sor.", "Is the admission free?", ["Is the admission free?", "How much is the bill?"], 2)
]
for i, (scen, target, opts, diff) in enumerate(fin_swipe):
    questions.append({
        "id": 3100 + i,
        "category": "FINANCE",
        "mechanicType": "SWIPE",
        "scenarioTr": scen,
        "targetEn": target,
        "options": opts,
        "difficulty": diff
    })

# CHUNK
fin_chunk = [
    ("Kredi kartı kabul edip etmediklerini sor.", "Do you accept credit cards here?", ["Do you accept", "credit cards", "here?"], 1),
    ("Faturayı ikiye bölmeyi teklif et.", "Let us split the bill in half.", ["Let us split", "the bill", "in half."], 2),
    ("Döviz bürosunun komisyonunu öğren.", "What is the exchange rate today?", ["What is the", "exchange rate", "today?"], 2),
    ("Hesaptaki hatayı garsona kibarca göster.", "I think there is a mistake here.", ["I think", "there is a", "mistake here."], 2),
    ("ATM'nin nerede olduğunu sor.", "Where can I find an ATM machine?", ["Where can I", "find an", "ATM machine?"], 1),
    ("Makbuzu almak istediğini söyle.", "Please give me a printed receipt.", ["Please give me", "a printed", "receipt."], 1),
    ("Bu fiyatın her şeyi kapsayıp kapsamadığını sor.", "Does this price include tax?", ["Does this price", "include", "tax?"], 3),
    ("Daha küçük paralar istediğini söyle.", "I would like some smaller bills.", ["I would like", "some smaller", "bills."], 3),
    ("Kartının ATM'de kaldığını bankaya söyle.", "My debit card was swallowed by the machine.", ["My debit card", "was swallowed", "by the machine."], 3),
    ("Hesabı ödemek için ne kadar vermen gerektiğini sor.", "How much do I owe you?", ["How much", "do I", "owe you?"], 2),
    ("Fiyatın çok yüksek olduğunu belirt.", "That is a bit too expensive.", ["That is a", "bit too", "expensive."], 1),
    ("İndirim yapılıp yapılamayacağını sor.", "Is it possible to get a discount?", ["Is it possible", "to get a", "discount?"], 2),
    ("Temassız ödeme yapmak istediğini belirt.", "I want to pay with contactless.", ["I want to", "pay with", "contactless."], 2),
    ("Kartının çalındığını ve iptal etmek istediğini söyle.", "I need to cancel my stolen card.", ["I need to", "cancel my", "stolen card."], 3)
]
for i, (scen, target, chunks, diff) in enumerate(fin_chunk):
    questions.append({
        "id": 3200 + i,
        "category": "FINANCE",
        "mechanicType": "CHUNK",
        "scenarioTr": scen,
        "targetEn": target,
        "options": chunks,
        "difficulty": diff
    })

# ERROR_FIND
fin_error = [
    ("Kredi kartıyla ödeyebilir miyim de. (by/with hatasını düzelt)", "Can I pay by credit card?", ["Can I pay with credit card?", "with", "by"], 1),
    ("Nakit ödeyeceğim de. (in cash/with cash hatasını düzelt)", "I will pay in cash.", ["I will pay with cash.", "with cash", "in cash"], 1),
    ("Faturada hata var de. (on/at hatasını düzelt)", "There is a mistake on the bill.", ["There is a mistake at the bill.", "at", "on"], 2),
    ("ATM çalışmıyor de. (working/works hatasını düzelt)", "This ATM is not working.", ["This ATM is not works.", "works", "working"], 1),
    ("Bahşiş dahil mi de. (included/includes hatasını düzelt)", "Is the tip included?", ["Is the tip includes?", "includes", "included"], 2),
    ("Faturayı bölelim de. (split/splitting hatasını düzelt)", "Let us split the bill.", ["Let us splitting the bill.", "splitting", "split"], 1),
    ("Çok pahalı de. (too/to hatasını düzelt)", "It is too expensive.", ["It is to expensive.", "to", "too"], 1),
    ("Hesabı alabilir miyim de. (bill/bills hatasını düzelt)", "Can I have the bill?", ["Can I have the bills?", "bills", "bill"], 1),
    ("Döviz bürosu nerede de. (exchange/exchanging hatasını düzelt)", "Where is the currency exchange?", ["Where is the currency exchanging?", "exchanging", "exchange"], 3),
    ("Param kalmadı de. (out of/out from hatasını düzelt)", "I am out of cash.", ["I am out from cash.", "out from", "out of"], 2),
    ("İade istiyorum de. (a refund/refunds hatasını düzelt)", "I want to get a refund.", ["I want to get refunds.", "refunds", "a refund"], 3),
    ("Makbuz istiyorum de. (a receipt/receipts hatasını düzelt)", "Can I get a receipt?", ["Can I get receipts?", "receipts", "a receipt"], 2),
    ("Kartım ATM'de sıkıştı de. (stuck/sticked hatasını düzelt)", "My card is stuck.", ["My card is sticked.", "sticked", "stuck"], 3),
    ("Üstü kalsın de. (change/changes hatasını düzelt)", "Keep the change.", ["Keep the changes.", "changes", "change"], 2)
]
for i, (scen, target, opts, diff) in enumerate(fin_error):
    questions.append({
        "id": 3300 + i,
        "category": "FINANCE",
        "mechanicType": "ERROR_FIND",
        "scenarioTr": scen,
        "targetEn": target,
        "options": opts,
        "difficulty": diff
    })


# --- BASIC_NEEDS ---
# SKELETON
basic_skeleton = [
    ("Eczane nerede diye sor.", "Where is the nearest pharmacy?", 1),
    ("İngilizce menü iste.", "Can I have an English menu?", 1),
    ("Priz dönüştürücüye ihtiyacın olduğunu söyle.", "I need an adapter for my charger.", 3),
    ("Wi-Fi şifresini sor.", "What is the Wi-Fi password?", 1),
    ("Vejetaryen yemek seçeneği olup olmadığını sor.", "Do you have vegetarian options?", 2),
    ("Boş oda var mı diye otelde sor.", "Do you have any vacancies?", 2),
    ("Odamın temizlenmesini istiyorum de.", "Please clean my room.", 1),
    ("Su almak istediğini söyle.", "I want to buy a bottle of water.", 1),
    ("En yakın süpermarketin yerini sor.", "Where is the nearest supermarket?", 1),
    ("Hesapta kahvaltı dahil mi diye sor.", "Is breakfast included in the price?", 2),
    ("Ağrı kesici almak istediğini eczaneye söyle.", "I need some painkillers.", 2),
    ("Bu yemek acı mı diye sor.", "Is this food spicy?", 1),
    ("Otelden çıkış saatini sor.", "What time is checkout?", 2)
]
for i, (scen, target, diff) in enumerate(basic_skeleton):
    questions.append({
        "id": 4000 + i,
        "category": "BASIC_NEEDS",
        "mechanicType": "SKELETON",
        "scenarioTr": scen,
        "targetEn": target,
        "options": [],
        "difficulty": diff
    })

# SWIPE
basic_swipe = [
    ("Garsona su siparişi verirken hangisini söylersin?", "A bottle of water, please.", ["A bottle of water, please.", "I need a passport."], 1),
    ("Otel resepsiyonunda giriş (check-in) yapmak istiyorsun.", "I want to check in.", ["I want to check in.", "Give me a taxi."], 1),
    ("Eczacıdan boğaz ağrısı için ilaç isteyeceksin.", "I need something for a sore throat.", ["I need something for a sore throat.", "I want to eat pizza."], 2),
    ("Otel odasında havlu eksik, resepsiyonu ara.", "I need clean towels in my room.", ["I need clean towels in my room.", "The room is very big."], 2),
    ("Yemekte hesap ödemek için ne dersin?", "The check, please.", ["The check, please.", "Hello chef!"], 1),
    ("Kullanıcı Wi-Fi şifresi arıyor.", "Is the Wi-Fi free here?", ["Is the Wi-Fi free here?", "Where is the elevator?"], 1),
    ("Priz dönüştürücü sormak için ne dersin?", "Do you have a plug adapter?", ["Do you have a plug adapter?", "I need an ambulance."], 3),
    ("Yemeğin içinde et olup olmadığını sor.", "Does this dish contain meat?", ["Does this dish contain meat?", "Is this food delicious?"], 2),
    ("Oda anahtarını kaybettiğini söyle.", "I lost my room key.", ["I lost my room key.", "I am checking out."], 2),
    ("Siparişini iptal etmek istiyorsun.", "I want to cancel my order.", ["I want to cancel my order.", "I want to pay."], 2),
    ("Tuvaletin nerede olduğunu soracaksın.", "Where is the restroom?", ["Where is the restroom?", "Where is London?"], 1),
    ("Bagajını otele emanet etmek istiyorsun.", "Can I leave my bags here?", ["Can I leave my bags here?", "I am leaving now."], 3),
    ("Eczanenin ne zaman kapandığını sor.", "When does the pharmacy close?", ["When does the pharmacy close?", "Where is the shop?"], 2)
]
for i, (scen, target, opts, diff) in enumerate(basic_swipe):
    questions.append({
        "id": 4100 + i,
        "category": "BASIC_NEEDS",
        "mechanicType": "SWIPE",
        "scenarioTr": scen,
        "targetEn": target,
        "options": opts,
        "difficulty": diff
    })

# CHUNK
basic_chunk = [
    ("Eczanenin nerede olduğunu sor.", "Where is the nearest pharmacy around here?", ["Where is", "the nearest pharmacy", "around here?"], 1),
    ("Otel odası rezervasyonu yaptığını söyle.", "I have a reservation under my name.", ["I have a", "reservation under", "my name."], 2),
    ("Wi-Fi şifresini yazmasını rica et.", "Could you write down the Wi-Fi password?", ["Could you", "write down", "the Wi-Fi password?"], 2),
    ("Eczacıdan ağrı kesici iste.", "I need a pack of painkillers please.", ["I need a", "pack of painkillers", "please."], 2),
    ("Priz dönüştürücüye ihtiyacın olduğunu söyle.", "I am looking for a travel adapter.", ["I am looking", "for a travel", "adapter."], 3),
    ("Yemekte alerjin olduğunu garsona söyle.", "I am allergic to dairy products.", ["I am allergic", "to dairy", "products."], 3),
    ("Menüde ne önerdiklerini sor.", "What do you recommend on the menu?", ["What do you", "recommend on", "the menu?"], 2),
    ("Faturayı getirmesini rica et.", "Could you bring us the bill please?", ["Could you", "bring us the bill", "please?"], 1),
    ("Odanın çok soğuk olduğunu resepsiyona bildir.", "The heating in my room is not working.", ["The heating", "in my room", "is not working."], 3),
    ("Sıcak su akmadığını söyle.", "There is no hot water in my shower.", ["There is no", "hot water in", "my shower."], 2),
    ("Otelden taksi çağırmasını rica et.", "Can you order a taxi for me?", ["Can you order", "a taxi", "for me?"], 1),
    ("Hesaba kahvaltının dahil olup olmadığını sor.", "Is the breakfast included in the rate?", ["Is the breakfast", "included in", "the rate?"], 2),
    ("Otel odasının anahtarını iste.", "Can I have my room key please?", ["Can I have", "my room key", "please?"], 1),
    ("Vejetaryen seçeneği olup olmadığını sor.", "Do you have any vegetarian dishes?", ["Do you have", "any vegetarian", "dishes?"], 2)
]
for i, (scen, target, chunks, diff) in enumerate(basic_chunk):
    questions.append({
        "id": 4200 + i,
        "category": "BASIC_NEEDS",
        "mechanicType": "CHUNK",
        "scenarioTr": scen,
        "targetEn": target,
        "options": chunks,
        "difficulty": diff
    })

# ERROR_FIND
basic_error = [
    ("Eczane nerede de. (Where is/Where are hatasını düzelt)", "Where is the pharmacy?", ["Where are the pharmacy?", "are", "is"], 1),
    ("Rezervasyonum var de. (under/at hatasını düzelt)", "I have a reservation under my name.", ["I have a reservation at my name.", "at my", "under my"], 2),
    ("Wi-Fi şifresi nedir de. (password/passwords hatasını düzelt)", "What is the Wi-Fi password?", ["What is the Wi-Fi passwords?", "passwords", "password"], 1),
    ("Su istiyorum de. (bottle/bottles hatasını düzelt)", "I want a bottle of water.", ["I want a bottles of water.", "bottles", "bottle"], 1),
    ("Priz dönüştürücüye ihtiyacım var de. (an/a hatasını düzelt)", "I need an adapter.", ["I need a adapter.", "a", "an"], 2),
    ("Ağrı kesici var mı de. (any/some hatasını düzelt)", "Do you have any painkillers?", ["Do you have some painkillers?", "some", "any"], 2),
    ("Menüyü alabilir miyim de. (menu/menus hatasını düzelt)", "Can I see the menu?", ["Can I see the menus?", "menus", "menu"], 1),
    ("Kahvaltı dahil mi de. (included/include hatasını düzelt)", "Is breakfast included?", ["Is breakfast include?", "include", "included"], 2),
    ("Oda anahtarımı kaybettim de. (lost/lose hatasını düzelt)", "I lost my room key.", ["I lose my room key.", "lose", "lost"], 1),
    ("Sıcak su yok de. (There is/There are hatasını düzelt)", "There is no hot water.", ["There are no hot water.", "are", "is"], 2),
    ("Taksi çağır de. (call/calling hatasını düzelt)", "Can you call a taxi?", ["Can you calling a taxi?", "calling", "call"], 1),
    ("Eczane ne zaman kapanıyor de. (does/is hatasını düzelt)", "When does the pharmacy close?", ["When is the pharmacy close?", "is", "does"], 2),
    ("Vejetaryen yemeğiniz var mı de. (options/option hatasını düzelt)", "Do you have vegetarian options?", ["Do you have vegetarian option?", "option", "options"], 2),
    ("Hesap lütfen de. (bill/bills hatasını düzelt)", "Could I have the bill?", ["Could I have the bills?", "bills", "bill"], 1)
]
for i, (scen, target, opts, diff) in enumerate(basic_error):
    questions.append({
        "id": 4300 + i,
        "category": "BASIC_NEEDS",
        "mechanicType": "ERROR_FIND",
        "scenarioTr": scen,
        "targetEn": target,
        "options": opts,
        "difficulty": diff
    })

# Write to file
output_path = os.path.join("app", "src", "main", "assets", "survival_questions.json")
os.makedirs(os.path.dirname(output_path), exist_ok=True)

with open(output_path, "w", encoding="utf-8") as f:
    json.dump(questions, f, ensure_ascii=False, indent=2)

print(f"Generated {len(questions)} survival questions in {output_path}.")
