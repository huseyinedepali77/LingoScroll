package com.example.lingoscroll.data

enum class Level {
    BEGINNER,     // A1-A2: Temel sokak İngilizcesi ve basit ihtiyaçlar
    INTERMEDIATE,   // B1-B2: Günlük diyaloglar, yaygın deyimler ve pratik kalıplar
    ADVANCED      // C1: Doğal sokak argosu, iş hayatı deyimleri ve ileri kalıplar
}

enum class ItemType {
    CARD,                   // Kelime/Deyim Kartı (Hoparlör butonu ve açıklama ile)
    QUIZ_MULTIPLE_CHOICE,   // Çoktan seçmeli kelime/deyim sorusu
    QUIZ_COMPLETION         // Boşluk doldurmalı cümle sorusu
}

data class LearningItem(
    val id: Int,
    val type: ItemType,
    val level: Level,
    val phrase: String,          // İngilizce kelime/cümle veya soru kalıbı (Soru ise: "I'm feeling under the _____.")
    val translation: String,     // Türkçe karşılığı veya doğru cevap açıklaması
    val context: String,         // Sokakta nerede ve nasıl kullanıldığına dair açıklama/not
    val options: List<String> = emptyList(), // Çoktan seçmeli ise seçenekler (doğru cevap dahil)
    val correctAnswer: String = "", // Doğru cevap
    val category: String = "CASUAL", // "TRAVEL", "BUSINESS", "CASUAL", "MIXED"
    val variations: List<String> = emptyList() // Alternatif cümleler/sorular
)

object LearningContent {
    // Seviye tespit sınavı için kullanılacak 9 soruluk test havuzu
    val diagnosticQuestions = listOf(
        // Beginner (A1-A2)
        LearningItem(
            id = 101,
            type = ItemType.QUIZ_MULTIPLE_CHOICE,
            level = Level.BEGINNER,
            phrase = "Sokakta birine 'Kendine iyi bak / İyi günler!' anlamında ne diyebilirsiniz?",
            translation = "Have a good one!",
            context = "ABD ve İngiltere'de vedalaşırken en çok kullanılan samimi ifadedir.",
            options = listOf("Have a good one!", "Good morning!", "Nice to meet you.", "Pardon me."),
            correctAnswer = "Have a good one!",
            category = "CASUAL"
        ),
        LearningItem(
            id = 102,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.BEGINNER,
            phrase = "Cafede kahve siparişi verirken: 'Could I _____ a latte, please?'",
            translation = "get",
            context = "'Could I get ...?' günlük hayatta sipariş verirken en kibar ve yaygın kalıptır.",
            options = listOf("give", "get", "take", "bring"),
            correctAnswer = "get",
            category = "TRAVEL",
            variations = listOf(
                "In the cafe, I ordered: 'Could I _____ a tea, please?'",
                "At the restaurant: 'Could I _____ the bill, please?'"
            )
        ),
        LearningItem(
            id = 103,
            type = ItemType.QUIZ_MULTIPLE_CHOICE,
            level = Level.BEGINNER,
            phrase = "'Naber, ne var ne yok?' anlamına gelen en yaygın sokak ifadesi hangisidir?",
            translation = "What's up?",
            context = "Arkadaşlar arasında selamlaşırken kullanılır.",
            options = listOf("How do you do?", "What's up?", "Pleased to see you.", "Are you okay?"),
            correctAnswer = "What's up?",
            category = "CASUAL"
        ),
        // Intermediate (B1-B2)
        LearningItem(
            id = 104,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.INTERMEDIATE,
            phrase = "Birini can kulağıyla dinlediğinizi belirtmek için: 'I am all _____.'",
            translation = "ears",
            context = "'I'm all ears' (Kulak kesildim / Seni tüm dikkatimle dinliyorum) anlamına gelen yaygın bir deyimdir.",
            options = listOf("eyes", "ears", "mouth", "hands"),
            correctAnswer = "ears",
            category = "CASUAL",
            variations = listOf(
                "Tell me your secret, I am all _____.",
                "Speak up, I am all _____ now."
            )
        ),
        LearningItem(
            id = 105,
            type = ItemType.QUIZ_MULTIPLE_CHOICE,
            level = Level.INTERMEDIATE,
            phrase = "Hesabı sizin ödeyeceğinizi belirtmek için hangisini söylersiniz?",
            translation = "It's on me.",
            context = "Kafede veya restoranda hesabı üstlenirken söylenir.",
            options = listOf("I pay this.", "It's my value.", "It's on me.", "Give me the ticket."),
            correctAnswer = "It's on me.",
            category = "TRAVEL"
        ),
        LearningItem(
            id = 106,
            type = ItemType.QUIZ_MULTIPLE_CHOICE,
            level = Level.INTERMEDIATE,
            phrase = "'Bana uyar / Ben de varım' anlamında hangi kalıp kullanılır?",
            translation = "I'm down for that.",
            context = "Bir teklifi kabul ederken veya plana katılacağınızı belirtirken kullanılır.",
            options = listOf("I'm up of that.", "I'm down for that.", "I'm low on this.", "I'm under that."),
            correctAnswer = "I'm down for that.",
            category = "CASUAL"
        ),
        // Advanced (C1)
        LearningItem(
            id = 107,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.ADVANCED,
            phrase = "İş yerinde 'Bugünlük bu kadar yeter, paydos edelim' derken: 'Let's call it a _____.'",
            translation = "day",
            context = "'Call it a day' bir işi veya çalışmayı o günlük sonlandırmak anlamına gelir.",
            options = listOf("night", "time", "job", "day"),
            correctAnswer = "day",
            category = "BUSINESS",
            variations = listOf(
                "We have been working for hours, let's call it a _____.",
                "I am so exhausted, let's call it a _____."
            )
        ),
        LearningItem(
            id = 108,
            type = ItemType.QUIZ_MULTIPLE_CHOICE,
            level = Level.ADVANCED,
            phrase = "Kendini hafif hasta veya keyifsiz hisseden biri hangisini kullanır?",
            translation = "Under the weather",
            context = "Hafif kırgınlık veya soğuk algınlığı durumlarında kullanılır.",
            options = listOf("Over the moon", "Under the weather", "In the dark", "Out of blue"),
            correctAnswer = "Under the weather",
            category = "CASUAL"
        ),
        LearningItem(
            id = 109,
            type = ItemType.QUIZ_MULTIPLE_CHOICE,
            level = Level.ADVANCED,
            phrase = "'Sırrı ifşa etmek / Baklayı ağzından çıkarmak' anlamına gelen deyim hangisidir?",
            translation = "Spill the beans",
            context = "Gizli kalması gereken bir şeyi yanlışlıkla veya isteyerek söylemek anlamındadır.",
            options = listOf("Spill the beans", "Break the ice", "Bite the bullet", "Hit the road"),
            correctAnswer = "Spill the beans",
            category = "CASUAL"
        ),
        LearningItem(
            id = 110,
            type = ItemType.QUIZ_MULTIPLE_CHOICE,
            level = Level.BEGINNER,
            phrase = "İş yerinde 'Bana yardım edebilir misiniz?' anlamındaki en kibar kalıp hangisidir?",
            translation = "Could you give me a hand, please?",
            context = "İş yerinde birinden yardım isterken kullanılan en yaygın deyimsel ifadedir.",
            options = listOf("Could you give me a hand, please?", "Give me hands.", "Are you working?", "Help me now."),
            correctAnswer = "Could you give me a hand, please?",
            category = "BUSINESS"
        ),
        LearningItem(
            id = 111,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.INTERMEDIATE,
            phrase = "İş yerinde teslim tarihini yetiştirmeyi anlatırken: 'We must meet the _____.'",
            translation = "deadline",
            context = "Projelerin son teslim tarihini (deadline) ifade etmek için kullanılır.",
            options = listOf("deadline", "meeting", "work", "office"),
            correctAnswer = "deadline",
            category = "BUSINESS",
            variations = listOf(
                "Don't miss the _____.",
                "We are working hard to meet the _____."
            )
        ),
        LearningItem(
            id = 112,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.ADVANCED,
            phrase = "İş yerinde 'Paydos edelim / Bugünlük bu kadar yeter' derken: 'Let's call it a _____.'",
            translation = "day",
            context = "Çalışmayı o günlük bitirmeyi ifade eden popüler deyimdir.",
            options = listOf("night", "time", "day", "job"),
            correctAnswer = "day",
            category = "BUSINESS",
            variations = listOf(
                "We have done enough, let's call it a _____.",
                "I'm tired, let's call it a _____."
            )
        ),
        LearningItem(
            id = 113,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.BEGINNER,
            phrase = "Havalimanında pasaport kontrolü sırasında: 'Please show me your _____.'",
            translation = "passport",
            context = "Pasaportunuzu ibraz etmenizi isteyen klasik seyahat kalıbıdır.",
            options = listOf("car", "passport", "key", "bill"),
            correctAnswer = "passport",
            category = "TRAVEL"
        ),
        LearningItem(
            id = 114,
            type = ItemType.QUIZ_MULTIPLE_CHOICE,
            level = Level.INTERMEDIATE,
            phrase = "Bir yere giderken 'Yoldayım / Geliyorum' anlamında hangi kalıp kullanılır?",
            translation = "I'm on my way.",
            context = "Hedefinize doğru yola çıktığınızı veya hareket halinde olduğunuzu bildirmek için kullanılan en yaygın kalıptır.",
            options = listOf("I'm on my way.", "I'm in my path.", "I'm coming now.", "I'm run way."),
            correctAnswer = "I'm on my way.",
            category = "TRAVEL"
        ),
        LearningItem(
            id = 115,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.ADVANCED,
            phrase = "Havalimanında vergi iadesi (Tax Free) ofisini ararken: 'Where is the tax _____ desk?'",
            translation = "refund",
            context = "Vergi iadesini (tax refund) sormak için kullanılır.",
            options = listOf("refund", "back", "return", "free"),
            correctAnswer = "refund",
            category = "TRAVEL",
            variations = listOf(
                "Can I get a tax _____ for these items?",
                "You need to show receipts at the tax _____ desk."
            )
        )
    )

    // Genel Eğitim Kartları ve Pratik Egzersizleri
    val practiceItems = listOf(
        // --- TRAVEL CATEGORY (Turistik / Tatil İngilizcesi) ---
        LearningItem(
            id = 1,
            type = ItemType.CARD,
            level = Level.BEGINNER,
            phrase = "Where is the baggage claim?",
            translation = "Bagaj alım yeri nerede?",
            context = "Havalimanına indikten sonra bavullarınızı teslim alacağınız yeri sormak için kullanılır.",
            category = "TRAVEL"
        ),
        LearningItem(
            id = 2,
            type = ItemType.CARD,
            level = Level.BEGINNER,
            phrase = "Could I have the menu, please?",
            translation = "Menüyü alabilir miyim lütfen?",
            context = "Restoranda sipariş vermeden önce menüyü istemek için en kibar kalıptır.",
            category = "TRAVEL"
        ),
        LearningItem(
            id = 3,
            type = ItemType.QUIZ_MULTIPLE_CHOICE,
            level = Level.BEGINNER,
            phrase = "Otelde resepsiyona 'Giriş yapmak istiyorum' demek için hangisi kullanılır?",
            translation = "I'd like to check in.",
            context = "Check-in işlemi otellere giriş yaparken kullanılan standart ifadedir.",
            options = listOf("I want register.", "I'd like to check in.", "I am entering.", "Give me my room."),
            correctAnswer = "I'd like to check in.",
            category = "TRAVEL"
        ),
        LearningItem(
            id = 4,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.BEGINNER,
            phrase = "Yol tarifi alırken metroya nasıl gideceğinizi sormak için: 'How do I get to the _____?'",
            translation = "subway",
            context = "'How do I get to...?' bir yere nasıl ulaşacağınızı sormanın en kolay ve doğal yoludur.",
            options = listOf("subway", "flight", "check", "room"),
            correctAnswer = "subway",
            category = "TRAVEL",
            variations = listOf(
                "Excuse me, how do I get to the _____?",
                "Could you tell me how do I get to the _____?"
            )
        ),
        LearningItem(
            id = 5,
            type = ItemType.CARD,
            level = Level.INTERMEDIATE,
            phrase = "I'd like to make a reservation.",
            translation = "Rezervasyon yaptırmak istiyorum.",
            context = "Restoranlarda veya otellerde önceden yer ayırtmak için bu kalıbı kullanabilirsiniz.",
            category = "TRAVEL"
        ),
        LearningItem(
            id = 6,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.INTERMEDIATE,
            phrase = "Havalimanında 'Uçuşum ertelendi mi?' diye sormak için: 'Is my flight _____?'",
            translation = "delayed",
            context = "'Delayed', uçuşların veya trenlerin rötarlı olduğunu belirtmek için kullanılır.",
            options = listOf("postponed", "delayed", "late", "stopped"),
            correctAnswer = "delayed",
            category = "TRAVEL",
            variations = listOf(
                "Oh no, the train is _____ by two hours.",
                "Is the direct flight to Paris _____?"
            )
        ),

        // --- BUSINESS CATEGORY (İş / Ofis İngilizcesi) ---
        LearningItem(
            id = 11,
            type = ItemType.CARD,
            level = Level.BEGINNER,
            phrase = "Let's schedule a meeting.",
            translation = "Bir toplantı ayarlayalım.",
            context = "İş ortaklarıyla veya ekip arkadaşlarıyla toplantı tarihi belirlemek için kullanılır.",
            category = "BUSINESS"
        ),
        LearningItem(
            id = 12,
            type = ItemType.QUIZ_MULTIPLE_CHOICE,
            level = Level.BEGINNER,
            phrase = "Ofiste iş arkadaşınıza 'Bana yardım edebilir misin?' demenin en doğal yolu hangisidir?",
            translation = "Could you give me a hand?",
            context = "'Give a hand' deyimi fiziksel veya zihinsel bir yardım talep ederken kullanılır.",
            options = listOf("Can you take my hand?", "Could you give me a hand?", "Will you pay me?", "Are you working?"),
            correctAnswer = "Could you give me a hand?",
            category = "BUSINESS"
        ),
        LearningItem(
            id = 13,
            type = ItemType.CARD,
            level = Level.INTERMEDIATE,
            phrase = "We need to meet the deadline.",
            translation = "Son teslim tarihine uymamız gerekiyor.",
            context = "Projelerin veya görevlerin teslim edilmesi gereken son güne (deadline) yetişilmesini anlatır.",
            category = "BUSINESS"
        ),
        LearningItem(
            id = 14,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.INTERMEDIATE,
            phrase = "Bir işi ertelemek veya daha sonraya bırakmak anlamında: 'Let's put it on the back _____.'",
            translation = "burner",
            context = "'Put on the back burner' (Arka ocağa koymak), bir işi önemsiz olduğu için ertelemek demektir.",
            options = listOf("table", "burner", "shelf", "office"),
            correctAnswer = "burner",
            category = "BUSINESS",
            variations = listOf(
                "This project is not urgent, let's put it on the back _____.",
                "We must put that feature on the back _____ for now."
            )
        ),
        LearningItem(
            id = 15,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.ADVANCED,
            phrase = "Bir şeyi çok iyi ve detaylıca anlamak / kavramak anlamında: 'I need to get a grip on _____.'",
            translation = "this",
            context = "'Get a grip on something' kontrolü ele almak veya bir konuyu tamamen kavramak anlamına gelir.",
            options = listOf("this", "that", "there", "them"),
            correctAnswer = "this",
            category = "BUSINESS",
            variations = listOf(
                "The new software is hard, I need to get a grip on _____.",
                "Before presenting, make sure you get a grip on _____."
            )
        ),

        // --- CASUAL CATEGORY (Günlük Konuşma / Deyimler) ---
        LearningItem(
            id = 21,
            type = ItemType.CARD,
            level = Level.BEGINNER,
            phrase = "Never mind.",
            translation = "Boşver. / Önemi yok.",
            context = "Bir konunun üzerinde durmaya gerek olmadığını belirtmek için kullanılır.",
            category = "CASUAL"
        ),
        LearningItem(
            id = 22,
            type = ItemType.QUIZ_MULTIPLE_CHOICE,
            level = Level.BEGINNER,
            phrase = "'Haklısın / Sana sonuna kadar katılıyorum' anlamında sokakta ne söylenir?",
            translation = "You can say that again!",
            context = "Karşı tarafın söylediği bir şeyi çok güçlü bir şekilde onaylarken söylenir.",
            options = listOf("Repeat it please.", "You can say that again!", "Speak louder.", "I hear you again."),
            correctAnswer = "You can say that again!",
            category = "CASUAL"
        ),
        LearningItem(
            id = 23,
            type = ItemType.CARD,
            level = Level.INTERMEDIATE,
            phrase = "I'll grab a bite.",
            translation = "Hızlıca bir şeyler atıştıracağım.",
            context = "Özellikle kısıtlı vakitlerde hızlıca bir şeyler yemek anlamında çok sık kullanılır.",
            category = "CASUAL"
        ),
        LearningItem(
            id = 24,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.INTERMEDIATE,
            phrase = "Bir sırrı saklayamayıp ağzından kaçıran birine: 'Don't spill the _____!'",
            translation = "beans",
            context = "'Spill the beans' sırları ifşa etmek veya gizli kalması gereken şeyi açıklamak demektir.",
            options = listOf("milk", "beans", "tea", "secret"),
            correctAnswer = "beans",
            category = "CASUAL",
            variations = listOf(
                "We are planning a surprise, so don't spill the _____!",
                "Who spilled the _____ about the party?"
            )
        ),
        LearningItem(
            id = 25,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.ADVANCED,
            phrase = "Gergin ve yabancı bir ortamdaki havayı yumuşatmak için: 'Let's break the _____.'",
            translation = "ice",
            context = "'Break the ice' (Buzu kırmak), yeni tanışılan insanlarla konuşma başlatıp ortamı rahatlatmaktır.",
            options = listOf("glass", "ice", "wood", "line"),
            correctAnswer = "ice",
            category = "CASUAL",
            variations = listOf(
                "A simple game can help to break the _____.",
                "I told a joke to break the _____ at the meeting."
            )
        ),
        // --- ADVANCED CARD ITEMS (İleri Düzey Kelime Kartları) ---
        LearningItem(
            id = 30,
            type = ItemType.CARD,
            level = Level.ADVANCED,
            phrase = "I'm looking for a layover lounge.",
            translation = "Aktarma salonunu arıyorum.",
            context = "Uzun aktarmalı uçuşlarda dinlenme salonunu sormak için kullanılır.",
            category = "TRAVEL"
        ),
        LearningItem(
            id = 31,
            type = ItemType.CARD,
            level = Level.ADVANCED,
            phrase = "We need to pivot our business strategy.",
            translation = "İş stratejimizi yönlendirmeli / değiştirmeliyiz.",
            context = "Gidişata göre hızlıca strateji yönünü değiştirmek anlamına gelen profesyonel bir iş terimidir.",
            category = "BUSINESS"
        ),
        LearningItem(
            id = 32,
            type = ItemType.CARD,
            level = Level.ADVANCED,
            phrase = "I'm feeling under the weather.",
            translation = "Kendimi keyifsiz / hafif hasta hissediyorum.",
            context = "Hafif keyifsizlik veya yorgunluk durumlarını belirtmek için kullanılan popüler bir günlük deyimdir.",
            category = "CASUAL"
        ),
        // --- ADDED DEV TEST QUIZ ITEMS (Kategori Çeşitliliği İçin Yeni Test Soruları) ---
        LearningItem(
            id = 41,
            type = ItemType.QUIZ_MULTIPLE_CHOICE,
            level = Level.BEGINNER,
            phrase = "Otel resepsiyonundan oda anahtarı isterken hangisini söylersiniz?",
            translation = "Could I have my room key, please?",
            context = "Resepsiyondan oda anahtarını talep ederken kullanılan kibar ifadedir.",
            options = listOf("Could I have my room key, please?", "Where is the airport?", "Is this key gold?", "I want to checkout."),
            correctAnswer = "Could I have my room key, please?",
            category = "TRAVEL"
        ),
        LearningItem(
            id = 42,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.BEGINNER,
            phrase = "Restoranda hesabı isterken: 'Could we have the _____, please?'",
            translation = "bill",
            context = "'Bill' (fatura/hesap), yemek sonunda ödeme talep ederken söylenir.",
            options = listOf("ticket", "card", "bill", "menu"),
            correctAnswer = "bill",
            category = "TRAVEL",
            variations = listOf(
                "Excuse me, could we have the _____ please?",
                "We are ready to pay, could we get the _____?"
            )
        ),
        LearningItem(
            id = 43,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.INTERMEDIATE,
            phrase = "Uçağa biniş kartını sorarken: 'Where can I get my boarding ____?'",
            translation = "pass",
            context = "'Boarding pass' uçağa binmek için gerekli olan barkodlu geçiş kartıdır.",
            options = listOf("card", "pass", "ticket", "paper"),
            correctAnswer = "pass",
            category = "TRAVEL",
            variations = listOf(
                "Please present your boarding _____ at the gate.",
                "I lost my boarding _____, can you print it?"
            )
        ),
        LearningItem(
            id = 44,
            type = ItemType.QUIZ_MULTIPLE_CHOICE,
            level = Level.INTERMEDIATE,
            phrase = "Gümrükten geçerken 'Bildirecek bir şeyim yok' anlamındaki kalıp hangisidir?",
            translation = "I have nothing to declare.",
            context = "Gümrük memuruna gümrüğe tabi mal taşımadığınızı belirtirken söylenir.",
            options = listOf("I have nothing to declare.", "I have no bags.", "I am travel free.", "Nothing in my pocket."),
            correctAnswer = "I have nothing to declare.",
            category = "TRAVEL"
        ),
        LearningItem(
            id = 45,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.ADVANCED,
            phrase = "Uçuş iptalinden sonra tazminat talep ederken: 'I am entitled to a full _____.'",
            translation = "refund",
            context = "'Refund', para iadesi anlamına gelen resmi bir terimdir.",
            options = listOf("refund", "ticket", "flight", "payment"),
            correctAnswer = "refund",
            category = "TRAVEL",
            variations = listOf(
                "Because of the delay, I demand a full _____.",
                "The airline offered a voucher instead of a _____."
            )
        ),
        LearningItem(
            id = 46,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.BEGINNER,
            phrase = "İş arkadaşına kartvizitini verirken: 'Here is my business _____.'",
            translation = "card",
            context = "'Business card' iş dünyasında iletişim bilgilerini paylaşmak için kullanılan kartvizittir.",
            options = listOf("paper", "card", "note", "letter"),
            correctAnswer = "card",
            category = "BUSINESS"
        ),
        LearningItem(
            id = 47,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.INTERMEDIATE,
            phrase = "Bir toplantıyı başka saate ertelemek anlamında: 'Let's push back the _____.'",
            translation = "meeting",
            context = "'Push back the meeting' toplantı vaktini daha ileriye ötelemek demektir.",
            options = listOf("work", "meeting", "call", "office"),
            correctAnswer = "meeting",
            category = "BUSINESS",
            variations = listOf(
                "Can we push back the _____ to 3 PM?",
                "We need to push back the project _____."
            )
        ),
        LearningItem(
            id = 48,
            type = ItemType.QUIZ_MULTIPLE_CHOICE,
            level = Level.INTERMEDIATE,
            phrase = "İş yerinde 'Aynı fikirdeyiz / Mutabıkız' anlamındaki yaygın kalıp hangisidir?",
            translation = "We are on the same page.",
            context = "Ekipteki herkesin ortak bir konuda hemfikir olduğunu anlatır.",
            options = listOf("We are on the same page.", "We read the same book.", "We write same notes.", "We are in same office."),
            correctAnswer = "We are on the same page.",
            category = "BUSINESS"
        ),
        LearningItem(
            id = 49,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.ADVANCED,
            phrase = "Bir projeyi başlatmak veya onaylamak anlamında: 'We got the green _____.'",
            translation = "light",
            context = "'Get the green light' (Yeşil ışık almak), bir işin yapılması için onay almaktır.",
            options = listOf("light", "card", "sign", "go"),
            correctAnswer = "light",
            category = "BUSINESS",
            variations = listOf(
                "Management gave us the green _____.",
                "We are waiting to get the green _____."
            )
        ),
        LearningItem(
            id = 50,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.BEGINNER,
            phrase = "Biri teşekkür ettiğinde 'Rica ederim' anlamında: 'You are _____.'",
            translation = "welcome",
            context = "'You're welcome' teşekkürlere karşılık verilen en yaygın kibar ifadedir.",
            options = listOf("welcome", "good", "nice", "ok"),
            correctAnswer = "welcome",
            category = "CASUAL"
        ),
        LearningItem(
            id = 51,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.INTERMEDIATE,
            phrase = "Biriyle şakalaşırken veya dalga geçerken: 'Are you pulling my _____?'",
            translation = "leg",
            context = "'Pull someone's leg' biriyle şaka yollu dalga geçmek veya kandırmak anlamında bir deyimdir.",
            options = listOf("arm", "leg", "hair", "hand"),
            correctAnswer = "leg",
            category = "CASUAL",
            variations = listOf(
                "Don't believe him, he is just pulling your _____.",
                "You must be kidding, are you pulling my _____?"
            )
        ),
        LearningItem(
            id = 52,
            type = ItemType.QUIZ_MULTIPLE_CHOICE,
            level = Level.ADVANCED,
            phrase = "Bir işi çok kolay veya çocuk oyuncağı olarak tanımlarken hangisini kullanırsınız?",
            translation = "A piece of cake.",
            context = "Bir görevin son derece zahmetsiz ve kolay olduğunu belirtir.",
            options = listOf("A piece of cake.", "An easy card.", "A sweet job.", "A walk in park."),
            correctAnswer = "A piece of cake.",
            category = "CASUAL"
        ),
        LearningItem(
            id = 60,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.BEGINNER,
            phrase = "Seyahatte 'Bavulum kayboldu' derken: 'My luggage is _____.'",
            translation = "lost",
            context = "Havalimanında kaybolan eşyaları bildirmek için kullanılır.",
            options = listOf("lost", "check", "flight", "room"),
            correctAnswer = "lost",
            category = "TRAVEL"
        ),
        LearningItem(
            id = 61,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.BEGINNER,
            phrase = "Alışverişte bir şeyin fiyatını sorarken: 'How _____ is this?'",
            translation = "much",
            context = "Fiyat sormanın en temel İngilizce kalıbıdır.",
            options = listOf("much", "many", "cost", "price"),
            correctAnswer = "much",
            category = "TRAVEL"
        ),
        LearningItem(
            id = 62,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.BEGINNER,
            phrase = "Otelde 'Sessiz bir oda istiyorum' derken: 'I'd like a _____ room.'",
            translation = "quiet",
            context = "Konaklama esnasında gürültüden uzak bir oda talep etmek için söylenir.",
            options = listOf("quiet", "loud", "big", "hot"),
            correctAnswer = "quiet",
            category = "TRAVEL"
        ),
        LearningItem(
            id = 63,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.BEGINNER,
            phrase = "Arkadaşımıza ayrılırken 'Kendine iyi bak' demek için: 'Take _____.'",
            translation = "care",
            context = "'Take care' (Kendine dikkat et) vedalaşırken sıkça kullanılır.",
            options = listOf("care", "look", "see", "go"),
            correctAnswer = "care",
            category = "CASUAL"
        ),
        LearningItem(
            id = 64,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.BEGINNER,
            phrase = "Saati sorarken: 'What _____ is it?'",
            translation = "time",
            context = "Zamanı öğrenmek için en yaygın soru biçimidir.",
            options = listOf("time", "day", "hour", "clock"),
            correctAnswer = "time",
            category = "CASUAL"
        ),
        LearningItem(
            id = 65,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.BEGINNER,
            phrase = "Hesabı isterken: 'Can I have the _____, please?'",
            translation = "check",
            context = "Restoranda yemeğin sonunda hesabı istemek için kullanılır.",
            options = listOf("check", "menu", "card", "book"),
            correctAnswer = "check",
            category = "TRAVEL"
        ),
        LearningItem(
            id = 66,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.BEGINNER,
            phrase = "Bir teklifi onaylarken: 'That sounds _____.'",
            translation = "great",
            context = "Karşı tarafın fikrini beğendiğimizi kibarca belirtir.",
            options = listOf("great", "bad", "slow", "short"),
            correctAnswer = "great",
            category = "CASUAL"
        ),
        LearningItem(
            id = 67,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.BEGINNER,
            phrase = "Adres sorarken: 'Where is the bus _____?'",
            translation = "station",
            context = "Otobüs durağının yerini sormak için kullanılır.",
            options = listOf("station", "car", "flight", "road"),
            correctAnswer = "station",
            category = "TRAVEL"
        ),
        LearningItem(
            id = 68,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.INTERMEDIATE,
            phrase = "İş yerinde 'Fikir fırtınası yapalım' derken: 'Let's _____ some ideas.'",
            translation = "brainstorm",
            context = "Yeni fikirler üretmek için ortak çalışma toplantılarını anlatır.",
            options = listOf("brainstorm", "build", "make", "think"),
            correctAnswer = "brainstorm",
            category = "BUSINESS"
        ),
        LearningItem(
            id = 69,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.INTERMEDIATE,
            phrase = "Gecikme durumunda özür dilerken: 'Sorry for the _____.'",
            translation = "delay",
            context = "Uçuş veya toplantı gecikmelerinde nezaketen söylenir.",
            options = listOf("delay", "check", "flight", "call"),
            correctAnswer = "delay",
            category = "TRAVEL"
        ),
        LearningItem(
            id = 70,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.INTERMEDIATE,
            phrase = "Sahneye çıkacak birine şans dilerken: 'Break a _____!'",
            translation = "leg",
            context = "'Break a leg' (Şeytanın bacağını kır), başarılar dilemenin deyim halidir.",
            options = listOf("leg", "arm", "hand", "head"),
            correctAnswer = "leg",
            category = "CASUAL"
        ),
        LearningItem(
            id = 71,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.INTERMEDIATE,
            phrase = "Toplantıyı ertelemek anlamında: 'We need to postpone the _____.'",
            translation = "meeting",
            context = "'Postpone', resmi erteleme durumlarında kullanılır.",
            options = listOf("meeting", "deadlines", "office", "workers"),
            correctAnswer = "meeting",
            category = "BUSINESS"
        ),
        LearningItem(
            id = 72,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.INTERMEDIATE,
            phrase = "Bir konuyu kısaca özetlerken: 'To make a long story _____.'",
            translation = "short",
            context = "'To make a long story short' (Uzun lafın kısası) anlamına gelen bir deyimdir.",
            options = listOf("short", "big", "tall", "brief"),
            correctAnswer = "short",
            category = "CASUAL"
        ),
        LearningItem(
            id = 73,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.INTERMEDIATE,
            phrase = "İş yerinde bir projenin sorumluluğunu alırken: 'I will take _____ of this.'",
            translation = "charge",
            context = "'Take charge of something' bir işin kontrolünü ve yönetimini ele almaktır.",
            options = listOf("charge", "work", "money", "deadlines"),
            correctAnswer = "charge",
            category = "BUSINESS"
        ),
        LearningItem(
            id = 74,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.INTERMEDIATE,
            phrase = "Bir konuda kararsız kalındığında: 'I am on the _____ about it.'",
            translation = "fence",
            context = "'On the fence' (Çitin üstünde), kararsızlığı simgeleyen popüler bir deyimdir.",
            options = listOf("fence", "wall", "chair", "page"),
            correctAnswer = "fence",
            category = "CASUAL"
        ),
        LearningItem(
            id = 75,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.INTERMEDIATE,
            phrase = "Hafif hasta veya keyifsiz hissederken: 'I'm feeling under the _____.'",
            translation = "weather",
            context = "'Under the weather' halsizlik ve keyifsizlik durumlarını anlatır.",
            options = listOf("weather", "sun", "sky", "cloud"),
            correctAnswer = "weather",
            category = "CASUAL"
        ),
        LearningItem(
            id = 76,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.INTERMEDIATE,
            phrase = "Bir sırrı kimseye söylememek anlamında: 'Keep it under your _____.'",
            translation = "hat",
            context = "'Keep it under your hat' sırrı gizli tutmak anlamına gelir.",
            options = listOf("hat", "head", "desk", "card"),
            correctAnswer = "hat",
            category = "CASUAL"
        ),
        LearningItem(
            id = 77,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.ADVANCED,
            phrase = "İçgüdüsel hisleri ifade ederken: 'I have a gut _____.'",
            translation = "feeling",
            context = "'Gut feeling' (Karın/iç hissi) mantıktan ziyade hislere dayanmayı anlatır.",
            options = listOf("feeling", "thought", "reaction", "view"),
            correctAnswer = "feeling",
            category = "CASUAL"
        ),
        LearningItem(
            id = 78,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.ADVANCED,
            phrase = "Çaba harcamadan kazanılan çok kolay iş: 'It's a walk in the _____.'",
            translation = "park",
            context = "'A walk in the park' işin zahmetsizliğini ifade eder.",
            options = listOf("park", "garden", "street", "road"),
            correctAnswer = "park",
            category = "CASUAL"
        ),
        LearningItem(
            id = 79,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.ADVANCED,
            phrase = "Bir projeyi veya işi başlatırken: 'Let's get the ball _____.'",
            translation = "rolling",
            context = "'Get the ball rolling' işin ilk adımını atıp harekete geçirmektir.",
            options = listOf("rolling", "turning", "spin", "run"),
            correctAnswer = "rolling",
            category = "BUSINESS"
        ),
        LearningItem(
            id = 80,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.ADVANCED,
            phrase = "Çok heyecanlı ve meraklı bekleyiş: 'I am on the edge of my _____.'",
            translation = "seat",
            context = "'On the edge of my seat' (Koltuğumun ucundayım) heyecandan yerinde duramamaktır.",
            options = listOf("seat", "bed", "chair", "desk"),
            correctAnswer = "seat",
            category = "CASUAL"
        ),
        LearningItem(
            id = 81,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.ADVANCED,
            phrase = "Beklenmedik sürpriz bir gelişme: 'Out of the _____.'",
            translation = "blue",
            context = "'Out of the blue' aniden, beklenmedik şekilde ortaya çıkan durumları anlatır.",
            options = listOf("blue", "red", "green", "white"),
            correctAnswer = "blue",
            category = "CASUAL"
        ),
        LearningItem(
            id = 82,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.ADVANCED,
            phrase = "Zor gerçeklerle yüzleşip dürüst olmak: 'Let's face the _____.'",
            translation = "music",
            context = "'Face the music' yaptığı hataların sonuçlarına katlanmak demektir.",
            options = listOf("music", "song", "noise", "sound"),
            correctAnswer = "music",
            category = "BUSINESS"
        ),
        LearningItem(
            id = 83,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.ADVANCED,
            phrase = "Zor bir kararı cesaretle uygulamak: 'You have to bite the _____.'",
            translation = "bullet",
            context = "'Bite the bullet' (Kurşunu ısırmak) acı veren ama kaçınılmaz bir durumu kabullenmektir.",
            options = listOf("bullet", "food", "stone", "glass"),
            correctAnswer = "bullet",
            category = "BUSINESS"
        ),
        LearningItem(
            id = 84,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.ADVANCED,
            phrase = "Yanlış anlaşılmaları giderip konuyu netleştirmek: 'Let's clear the _____.'",
            translation = "air",
            context = "'Clear the air' gergin havayı dağıtıp ilişkileri yumuşatmaktır.",
            options = listOf("air", "sky", "wind", "room"),
            correctAnswer = "air",
            category = "CASUAL"
        ),
        LearningItem(
            id = 85,
            type = ItemType.QUIZ_COMPLETION,
            level = Level.ADVANCED,
            phrase = "Çok mutlu ve neşeli olmak: 'I'm on cloud _____.'",
            translation = "nine",
            context = "'On cloud nine' (Dokuzuncu bulutun üstünde) son derece mutlu ve keyifli olmaktır.",
            options = listOf("nine", "ten", "eight", "seven"),
            correctAnswer = "nine",
            category = "CASUAL"
        )
    )
}
