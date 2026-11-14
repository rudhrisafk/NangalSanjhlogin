package com.nangalsanjh.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import com.nangalsanjh.app.viewmodel.AppLanguage

/** Current app language, provided once in MainActivity. */
val LocalAppLanguage = compositionLocalOf { AppLanguage.ENGLISH }

/**
 * Looks up [en] (the English text used in code) for the current language.
 * Unknown strings fall back to the original text, so nothing ever breaks.
 * Bottom-bar labels are intentionally NOT passed through tr() and stay English.
 */
@Composable
fun tr(en: String): String = when (LocalAppLanguage.current) {
    AppLanguage.ENGLISH -> en
    AppLanguage.HINDI -> TrData.hi[en] ?: en
    AppLanguage.PUNJABI -> TrData.pa[en] ?: en
}

private data class E(val en: String, val hi: String, val pa: String)

object TrData {
    private val all = listOf(
        // ── App title sub-text (follows the selected language) ──
        E("ਨੰਗਲ ਸੇਵਾ", "नंगल सेवा", "ਨੰਗਲ ਸੇਵਾ"),

        E("Reported", "दर्ज", "ਦਰਜ"),
        E("In Review", "समीक्षा में", "ਸਮੀਖਿਆ ਵਿੱਚ"),
        E("Resolved", "हल हुई", "ਹੱਲ ਹੋਈ"),


        E("Text size", "टेक्स्ट का आकार", "ਟੈਕਸਟ ਦਾ ਆਕਾਰ"),
        E("Normal", "सामान्य", "ਆਮ"),
        E("Large", "बड़ा", "ਵੱਡਾ"),
        E("Extra large", "बहुत बड़ा", "ਬਹੁਤ ਵੱਡਾ"),

        // ── Worker side / leftovers ──
        E("Working hours: 9AM - 7PM", "काम के घंटे: सुबह 9 – शाम 7", "ਕੰਮ ਦੇ ਘੰਟੇ: ਸਵੇਰੇ 9 – ਸ਼ਾਮ 7"),
        E("Outside working hours", "काम के घंटों के बाहर", "ਕੰਮ ਦੇ ਘੰਟਿਆਂ ਤੋਂ ਬਾਹਰ"),
        E("⏰ Toggle only works 9AM-7PM. Turn ON tomorrow at 9AM.", "⏰ स्विच सिर्फ सुबह 9 से शाम 7 तक चलता है। कल सुबह 9 बजे ON करें।", "⏰ ਸਵਿੱਚ ਸਿਰਫ਼ ਸਵੇਰੇ 9 ਤੋਂ ਸ਼ਾਮ 7 ਤੱਕ ਚੱਲਦਾ ਹੈ। ਕੱਲ੍ਹ ਸਵੇਰੇ 9 ਵਜੇ ON ਕਰੋ।"),
        E("Describe your service", "अपनी सेवा बताएं", "ਆਪਣੀ ਸੇਵਾ ਦੱਸੋ"),
        E("Skills, experience...", "हुनर, अनुभव...", "ਹੁਨਰ, ਤਜਰਬਾ..."),
        E("Your Rate", "आपका रेट", "ਤੁਹਾਡਾ ਰੇਟ"),
        E("Department Note", "विभाग का नोट", "ਵਿਭਾਗ ਦਾ ਨੋਟ"),
        E("Custom Points", "कस्टम पॉइंट्स", "ਕਸਟਮ ਪੁਆਇੰਟ"),
        E("Reason", "कारण", "ਕਾਰਨ"),
        E("Specify Issue", "समस्या बताएं", "ਸਮੱਸਿਆ ਦੱਸੋ"),
        E("Describe the Issue", "समस्या का विवरण", "ਸਮੱਸਿਆ ਦਾ ਵੇਰਵਾ"),
        E("Location / Area", "जगह / इलाका", "ਜਗ੍ਹਾ / ਇਲਾਕਾ"),
        E("⏳ Please wait", "⏳ कृपया रुकें", "⏳ ਕਿਰਪਾ ਕਰਕੇ ਰੁਕੋ"),
        E("More details (optional)", "और जानकारी (वैकल्पिक)", "ਹੋਰ ਜਾਣਕਾਰੀ (ਵਿਕਲਪਿਕ)"),
        E("Thanks, admin will review this.", "धन्यवाद, एडमिन इसकी जांच करेगा।", "ਧੰਨਵਾਦ, ਐਡਮਿਨ ਇਸਦੀ ਜਾਂਚ ਕਰੇਗਾ।"),
        E("Jobs Done", "पूरे किए काम", "ਪੂਰੇ ਕੀਤੇ ਕੰਮ"),
        E("Available", "उपलब्ध", "ਉਪਲਬਧ"),
        E("Posts", "पोस्ट", "ਪੋਸਟਾਂ"),
        E("Jobs", "काम", "ਕੰਮ"),
        E("NEWS", "खबर", "ਖ਼ਬਰ"),
        E("You", "आप", "ਤੁਸੀਂ"),
        E("Nangal AI", "नंगल AI", "ਨੰਗਲ AI"),
        E("Online  ·  Hindi, Punjabi, English", "ऑनलाइन  ·  हिंदी, पंजाबी, English", "ਆਨਲਾਈਨ  ·  ਹਿੰਦੀ, ਪੰਜਾਬੀ, English"),
        E("Eg. Rajesh Kumar", "जैसे राजेश कुमार", "ਜਿਵੇਂ ਰਾਜੇਸ਼ ਕੁਮਾਰ"),
        E("Eg. Sector 4, Nangal", "जैसे सेक्टर 4, नंगल", "ਜਿਵੇਂ ਸੈਕਟਰ 4, ਨੰਗਲ"),
        E("Eg. 5", "जैसे 5", "ਜਿਵੇਂ 5"),
        E("Eg. ₹500/day or ₹200/hour", "जैसे ₹500/दिन या ₹200/घंटा", "ਜਿਵੇਂ ₹500/ਦਿਨ ਜਾਂ ₹200/ਘੰਟਾ"),
        E("Eg. AC Repair, DJ Services...", "जैसे AC रिपेयर, DJ सेवा...", "ਜਿਵੇਂ AC ਮੁਰੰਮਤ, DJ ਸੇਵਾ..."),
        E("Describe your skills and experience...", "अपने हुनर और अनुभव के बारे में लिखें...", "ਆਪਣੇ ਹੁਨਰ ਅਤੇ ਤਜਰਬੇ ਬਾਰੇ ਲਿਖੋ..."),
        E("Plumber", "प्लंबर", "ਪਲੰਬਰ"), E("Electrician", "इलेक्ट्रीशियन", "ਇਲੈਕਟ੍ਰੀਸ਼ੀਅਨ"),
        E("Carpenter", "बढ़ई", "ਤਰਖਾਣ"), E("Painter", "पेंटर", "ਪੇਂਟਰ"), E("Mason", "राजमिस्त्री", "ਰਾਜ ਮਿਸਤਰੀ"),
        E("Cleaner", "सफाईकर्मी", "ਸਫ਼ਾਈ ਕਰਮੀ"), E("Labour", "मज़दूर", "ਮਜ਼ਦੂਰ"), E("Automobile", "ऑटोमोबाइल", "ਆਟੋਮੋਬਾਈਲ"),
        E("Medical Tests", "मेडिकल टेस्ट", "ਮੈਡੀਕਲ ਟੈਸਟ"), E("Taxi Service", "टैक्सी सेवा", "ਟੈਕਸੀ ਸੇਵਾ"),
        E("Veterinary", "पशु चिकित्सा", "ਪਸ਼ੂ ਇਲਾਜ"), E("Other", "अन्य", "ਹੋਰ"),

        // ── Home / settings ──
        E("Complete Your Profile", "अपनी प्रोफ़ाइल पूरी करें", "ਆਪਣੀ ਪ੍ਰੋਫਾਈਲ ਪੂਰੀ ਕਰੋ"),
        E("Your profile isn't set up yet. Complete it to get the most out of Nangal Seva.", "आपकी प्रोफ़ाइल अभी सेट नहीं है। नंगल सेवा का पूरा लाभ लेने के लिए इसे पूरा करें।", "ਤੁਹਾਡੀ ਪ੍ਰੋਫਾਈਲ ਅਜੇ ਸੈੱਟ ਨਹੀਂ ਹੈ। ਨੰਗਲ ਸੇਵਾ ਦਾ ਪੂਰਾ ਲਾਭ ਲੈਣ ਲਈ ਇਸਨੂੰ ਪੂਰਾ ਕਰੋ।"),
        E("Set Up Now", "अभी सेट करें", "ਹੁਣੇ ਸੈੱਟ ਕਰੋ"),
        E("Later", "बाद में", "ਬਾਅਦ ਵਿੱਚ"),
        E("Profile", "प्रोफ़ाइल", "ਪ੍ਰੋਫਾਈਲ"),
        E("Settings", "सेटिंग्स", "ਸੈਟਿੰਗਾਂ"),
        E("Theme", "थीम", "ਥੀਮ"),
        E("Dark", "डार्क", "ਡਾਰਕ"),
        E("Light", "लाइट", "ਲਾਈਟ"),
        E("Done", "हो गया", "ਹੋ ਗਿਆ"),

        // ── Seeker home ──
        E("ਅੱਜ ਤੁਹਾਨੂੰ ਕਿਸ ਚੀਜ਼ ਦੀ ਲੋੜ ਹੈ?", "आज आपको किस चीज़ की ज़रूरत है?", "ਅੱਜ ਤੁਹਾਨੂੰ ਕਿਸ ਚੀਜ਼ ਦੀ ਲੋੜ ਹੈ?"),
        E("Post a job request", "काम की मांग डालें", "ਕੰਮ ਦੀ ਮੰਗ ਪਾਓ"),
        E("Emergency help", "आपातकालीन मदद", "ਐਮਰਜੈਂਸੀ ਮਦਦ"),
        E("QUICK ACTIONS", "त्वरित कार्रवाई", "ਤੁਰੰਤ ਕਾਰਵਾਈ"),
        E("ਤੁਰੰਤ ਕਾਰਵਾਈ", "Quick actions", "Quick actions"),
        E("HOW IT WORKS", "यह कैसे काम करता है", "ਇਹ ਕਿਵੇਂ ਕੰਮ ਕਰਦਾ ਹੈ"),
        E("ਇਹ ਕਿਵੇਂ ਕੰਮ ਕਰਦਾ ਹੈ", "How it works", "How it works"),
        E("LATEST FROM NANGAL", "नंगल की ताज़ा खबरें", "ਨੰਗਲ ਦੀਆਂ ਤਾਜ਼ਾ ਖ਼ਬਰਾਂ"),
        E("ਨੰਗਲ ਦੀਆਂ ਤਾਜ਼ਾ ਖ਼ਬਰਾਂ", "Latest from Nangal", "Latest from Nangal"),
        E("Services", "सेवाएं", "ਸੇਵਾਵਾਂ"),
        E("ਸੇਵਾਵਾਂ", "Services", "Services"),
        E("Complaints", "शिकायतें", "ਸ਼ਿਕਾਇਤਾਂ"),
        E("ਸ਼ਿਕਾਇਤ", "Complaints", "Complaints"),
        E("Live map", "लाइव नक्शा", "ਲਾਈਵ ਨਕਸ਼ਾ"),
        E("ਨਕਸ਼ਾ", "Map", "Map"),
        E("Emergency", "आपातकाल", "ਐਮਰਜੈਂਸੀ"),
        E("ਐਮਰਜੈਂਸੀ", "Emergency", "Emergency"),
        E("Describe the work", "काम बताएं", "ਕੰਮ ਦੱਸੋ"),
        E("Pick a category and drop a pin where you need help.", "श्रेणी चुनें और जहां मदद चाहिए वहां पिन लगाएं।", "ਸ਼੍ਰੇਣੀ ਚੁਣੋ ਅਤੇ ਜਿੱਥੇ ਮਦਦ ਚਾਹੀਦੀ ਹੈ ਉੱਥੇ ਪਿੰਨ ਲਗਾਓ।"),
        E("A worker accepts", "कारीगर स्वीकार करता है", "ਕਾਮਾ ਮਨਜ਼ੂਰ ਕਰਦਾ ਹੈ"),
        E("Nearby workers get your request and accept within 10 minutes.", "पास के कारीगरों को आपकी मांग मिलती है और वे 10 मिनट में स्वीकार करते हैं।", "ਨੇੜੇ ਦੇ ਕਾਮਿਆਂ ਨੂੰ ਤੁਹਾਡੀ ਮੰਗ ਮਿਲਦੀ ਹੈ ਅਤੇ ਉਹ 10 ਮਿੰਟ ਵਿੱਚ ਮਨਜ਼ੂਰ ਕਰਦੇ ਹਨ।"),
        E("Track and rate", "ट्रैक करें और रेट करें", "ਟ੍ਰੈਕ ਕਰੋ ਅਤੇ ਰੇਟ ਕਰੋ"),
        E("Follow the worker on the map, then leave a rating.", "नक्शे पर कारीगर को देखें, फिर रेटिंग दें।", "ਨਕਸ਼ੇ ਤੇ ਕਾਮੇ ਨੂੰ ਵੇਖੋ, ਫਿਰ ਰੇਟਿੰਗ ਦਿਓ।"),
        E("No ads", "कोई विज्ञापन नहीं", "ਕੋਈ ਇਸ਼ਤਿਹਾਰ ਨਹੀਂ"),
        E("Free to use", "उपयोग मुफ़्त", "ਵਰਤੋਂ ਮੁਫ਼ਤ"),
        E("Local workers", "स्थानीय कारीगर", "ਸਥਾਨਕ ਕਾਮੇ"),

        // ── Services ──
        E("SERVICES  ·  ਸੇਵਾਵਾਂ", "सेवाएं  ·  SERVICES", "ਸੇਵਾਵਾਂ  ·  SERVICES"),
        E("Find trusted workers in Nangal", "नंगल में भरोसेमंद कारीगर खोजें", "ਨੰਗਲ ਵਿੱਚ ਭਰੋਸੇਮੰਦ ਕਾਮੇ ਲੱਭੋ"),
        E("Browse job posts, save favourites or post your own request.", "काम की पोस्ट देखें, पसंदीदा सहेजें या अपनी मांग डालें।", "ਕੰਮ ਦੀਆਂ ਪੋਸਟਾਂ ਵੇਖੋ, ਪਸੰਦੀਦਾ ਸੰਭਾਲੋ ਜਾਂ ਆਪਣੀ ਮੰਗ ਪਾਓ।"),
        E("Search by name or category", "नाम या श्रेणी से खोजें", "ਨਾਮ ਜਾਂ ਸ਼੍ਰੇਣੀ ਨਾਲ ਲੱਭੋ"),
        E("Try another category, or post your own request.", "दूसरी श्रेणी आज़माएं, या अपनी मांग डालें।", "ਹੋਰ ਸ਼੍ਰੇਣੀ ਅਜ਼ਮਾਓ, ਜਾਂ ਆਪਣੀ ਮੰਗ ਪਾਓ।"),
        E("Post a request", "मांग डालें", "ਮੰਗ ਪਾਓ"),
        E("No saved workers yet", "अभी कोई सहेजा हुआ कारीगर नहीं", "ਹਾਲੇ ਕੋਈ ਸੰਭਾਲਿਆ ਕਾਮਾ ਨਹੀਂ"),
        E("Tap the heart on a job post to save it here.", "यहां सहेजने के लिए काम की पोस्ट पर दिल दबाएं।", "ਇੱਥੇ ਸੰਭਾਲਣ ਲਈ ਕੰਮ ਦੀ ਪੋਸਟ ਤੇ ਦਿਲ ਦਬਾਓ।"),
        E("Remove", "हटाएं", "ਹਟਾਓ"),
        E("Call", "कॉल करें", "ਕਾਲ ਕਰੋ"),
        E("NEW REQUEST  ·  ਨਵੀਂ ਮੰਗ", "नई मांग  ·  NEW REQUEST", "ਨਵੀਂ ਮੰਗ  ·  NEW REQUEST"),
        E("Not sure who to ask?", "पता नहीं किससे पूछें?", "ਪਤਾ ਨਹੀਂ ਕਿਸ ਤੋਂ ਪੁੱਛਣਾ ਹੈ?"),
        E("Broadcast this to every worker, regardless of category", "श्रेणी की परवाह किए बिना हर कारीगर को भेजें", "ਸ਼੍ਰੇਣੀ ਦੀ ਪਰਵਾਹ ਕੀਤੇ ਬਿਨਾਂ ਹਰ ਕਾਮੇ ਨੂੰ ਭੇਜੋ"),
        E("Category", "श्रेणी", "ਸ਼੍ਰੇਣੀ"),
        E("Post Request", "मांग डालें", "ਮੰਗ ਪਾਓ"),
        E("Cancel", "रद्द करें", "ਰੱਦ ਕਰੋ"),
        E("Job Description", "काम का विवरण", "ਕੰਮ ਦਾ ਵੇਰਵਾ"),
        E("Experience / About", "अनुभव / परिचय", "ਤਜਰਬਾ / ਜਾਣ-ਪਛਾਣ"),
        E("Call Now", "अभी कॉल करें", "ਹੁਣੇ ਕਾਲ ਕਰੋ"),
        E("Close", "बंद करें", "ਬੰਦ ਕਰੋ"),

        // ── Complaints ──
        E("No complaints yet", "अभी कोई शिकायत नहीं", "ਹਾਲੇ ਕੋਈ ਸ਼ਿਕਾਇਤ ਨਹੀਂ"),
        E("Be the first to report a civic issue", "नागरिक समस्या दर्ज करने वाले पहले व्यक्ति बनें", "ਸ਼ਹਿਰੀ ਸਮੱਸਿਆ ਦਰਜ ਕਰਨ ਵਾਲੇ ਪਹਿਲੇ ਬਣੋ"),
        E("COMPLAINTS  ·  ਸ਼ਿਕਾਇਤਾਂ", "शिकायतें  ·  COMPLAINTS", "ਸ਼ਿਕਾਇਤਾਂ  ·  COMPLAINTS"),
        E("Track reports from pending to resolved.", "लंबित से हल होने तक शिकायतों को ट्रैक करें।", "ਲੰਬਿਤ ਤੋਂ ਹੱਲ ਹੋਣ ਤੱਕ ਸ਼ਿਕਾਇਤਾਂ ਨੂੰ ਟ੍ਰੈਕ ਕਰੋ।"),
        E("Okay", "ठीक है", "ਠੀਕ ਹੈ"),
        E("Mark In Review", "समीक्षा में रखें", "ਸਮੀਖਿਆ ਵਿੱਚ ਰੱਖੋ"),
        E("Mark Resolved", "हल हुआ चिह्नित करें", "ਹੱਲ ਹੋਇਆ ਚਿੰਨ੍ਹਿਤ ਕਰੋ"),
        E("Add Points", "अंक जोड़ें", "ਅੰਕ ਜੋੜੋ"),
        E("Delete", "मिटाएं", "ਮਿਟਾਓ"),
        E("Update Status", "स्थिति अपडेट करें", "ਸਥਿਤੀ ਅੱਪਡੇਟ ਕਰੋ"),
        E("Add update for citizen...", "नागरिक के लिए अपडेट जोड़ें...", "ਨਾਗਰਿਕ ਲਈ ਅੱਪਡੇਟ ਜੋੜੋ..."),
        E("Update", "अपडेट करें", "ਅੱਪਡੇਟ ਕਰੋ"),
        E("Add Seva Points", "सेवा अंक जोड़ें", "ਸੇਵਾ ਅੰਕ ਜੋੜੋ"),
        E("Why giving points?", "अंक क्यों दे रहे हैं?", "ਅੰਕ ਕਿਉਂ ਦੇ ਰਹੇ ਹੋ?"),
        E("Points Levels", "अंक स्तर", "ਅੰਕ ਪੱਧਰ"),
        E("Delete Complaint", "शिकायत मिटाएं", "ਸ਼ਿਕਾਇਤ ਮਿਟਾਓ"),
        E("This action cannot be undone.", "यह क्रिया वापस नहीं की जा सकती।", "ਇਹ ਕਾਰਵਾਈ ਵਾਪਸ ਨਹੀਂ ਕੀਤੀ ਜਾ ਸਕਦੀ।"),
        E("Report Issue", "समस्या दर्ज करें", "ਸਮੱਸਿਆ ਦਰਜ ਕਰੋ"),
        E("Submit", "जमा करें", "ਜਮ੍ਹਾਂ ਕਰੋ"),
        E("No resolved complaints yet", "अभी कोई हल की गई शिकायत नहीं", "ਹਾਲੇ ਕੋਈ ਹੱਲ ਹੋਈ ਸ਼ਿਕਾਇਤ ਨਹੀਂ"),
        E("Reopen Complaint?", "शिकायत फिर से खोलें?", "ਸ਼ਿਕਾਇਤ ਮੁੜ ਖੋਲ੍ਹੋ?"),
        E("Reason (optional)", "कारण (वैकल्पिक)", "ਕਾਰਨ (ਵਿਕਲਪਿਕ)"),
        E("Confirm", "पुष्टि करें", "ਪੁਸ਼ਟੀ ਕਰੋ"),

        // ── Profile ──
        E("🏆 Seva Points Levels", "🏆 सेवा अंक स्तर", "🏆 ਸੇਵਾ ਅੰਕ ਪੱਧਰ"),
        E("Edit Profile", "प्रोफ़ाइल संपादित करें", "ਪ੍ਰੋਫਾਈਲ ਸੰਪਾਦਿਤ ਕਰੋ"),
        E("Change name or address", "नाम या पता बदलें", "ਨਾਮ ਜਾਂ ਪਤਾ ਬਦਲੋ"),
        E("Change Work Category", "काम की श्रेणी बदलें", "ਕੰਮ ਦੀ ਸ਼੍ਰੇਣੀ ਬਦਲੋ"),
        E("Blood Group", "रक्त समूह", "ਖੂਨ ਦਾ ਗਰੁੱਪ"),
        E("My Work", "मेरा काम", "ਮੇਰਾ ਕੰਮ"),
        E("My Jobs", "मेरे काम", "ਮੇਰੇ ਕੰਮ"),
        E("Jobs you've accepted", "जो काम आपने स्वीकार किए", "ਜੋ ਕੰਮ ਤੁਸੀਂ ਮਨਜ਼ੂਰ ਕੀਤੇ"),
        E("My Earnings", "मेरी कमाई", "ਮੇਰੀ ਕਮਾਈ"),
        E("Track your income & history", "अपनी आय और इतिहास देखें", "ਆਪਣੀ ਆਮਦਨ ਅਤੇ ਇਤਿਹਾਸ ਵੇਖੋ"),
        E("My Complaints", "मेरी शिकायतें", "ਮੇਰੀਆਂ ਸ਼ਿਕਾਇਤਾਂ"),
        E("My Job Requests", "मेरी काम की मांगें", "ਮੇਰੀਆਂ ਕੰਮ ਦੀਆਂ ਮੰਗਾਂ"),
        E("Auto-deletes after 48 hrs", "48 घंटे बाद अपने-आप हट जाता है", "48 ਘੰਟਿਆਂ ਬਾਅਦ ਆਪਣੇ-ਆਪ ਹਟ ਜਾਂਦਾ ਹੈ"),
        E("About Nangal Seva", "नंगल सेवा के बारे में", "ਨੰਗਲ ਸੇਵਾ ਬਾਰੇ"),
        E("Switch Profile", "प्रोफ़ाइल बदलें", "ਪ੍ਰੋਫਾਈਲ ਬਦਲੋ"),
        E("Logout", "लॉग आउट", "ਲੌਗ ਆਊਟ"),
        E("Sign out from app", "ऐप से साइन आउट करें", "ਐਪ ਤੋਂ ਸਾਈਨ ਆਊਟ ਕਰੋ"),
        E("Admin Passcode", "एडमिन पासकोड", "ਐਡਮਿਨ ਪਾਸਕੋਡ"),
        E("Enter department passcode to switch to Admin role", "एडमिन भूमिका में जाने के लिए विभाग का पासकोड डालें", "ਐਡਮਿਨ ਭੂਮਿਕਾ ਵਿੱਚ ਜਾਣ ਲਈ ਵਿਭਾਗ ਦਾ ਪਾਸਕੋਡ ਪਾਓ"),
        E("Passcode", "पासकोड", "ਪਾਸਕੋਡ"),
        E("Verify & Switch", "जांचें और बदलें", "ਜਾਂਚੋ ਅਤੇ ਬਦਲੋ"),
        E("Select your role. Admin requires a passcode.", "अपनी भूमिका चुनें। एडमिन के लिए पासकोड चाहिए।", "ਆਪਣੀ ਭੂਮਿਕਾ ਚੁਣੋ। ਐਡਮਿਨ ਲਈ ਪਾਸਕੋਡ ਚਾਹੀਦਾ ਹੈ।"),
        E("No work posted yet", "अभी कोई काम पोस्ट नहीं हुआ", "ਹਾਲੇ ਕੋਈ ਕੰਮ ਪੋਸਟ ਨਹੀਂ ਹੋਇਆ"),
        E("Tap + to post your services", "अपनी सेवाएं डालने के लिए + दबाएं", "ਆਪਣੀਆਂ ਸੇਵਾਵਾਂ ਪਾਉਣ ਲਈ + ਦਬਾਓ"),
        E("Delete this post?", "यह पोस्ट मिटाएं?", "ਇਹ ਪੋਸਟ ਮਿਟਾਓ?"),
        E("This can't be undone.", "इसे वापस नहीं किया जा सकता।", "ਇਸਨੂੰ ਵਾਪਸ ਨਹੀਂ ਕੀਤਾ ਜਾ ਸਕਦਾ।"),
        E("Edit Work Post", "काम की पोस्ट संपादित करें", "ਕੰਮ ਦੀ ਪੋਸਟ ਸੰਪਾਦਿਤ ਕਰੋ"),
        E("Description", "विवरण", "ਵੇਰਵਾ"),
        E("Rate", "दर", "ਦਰ"),
        E("Availability", "उपलब्धता", "ਉਪਲਬਧਤਾ"),
        E("Save Changes", "बदलाव सहेजें", "ਬਦਲਾਅ ਸੰਭਾਲੋ"),
        E("No active job requests", "कोई सक्रिय काम की मांग नहीं", "ਕੋਈ ਸਰਗਰਮ ਕੰਮ ਦੀ ਮੰਗ ਨਹੀਂ"),
        E("Requests auto-delete after 48 hours", "मांगें 48 घंटे बाद अपने-आप हट जाती हैं", "ਮੰਗਾਂ 48 ਘੰਟਿਆਂ ਬਾਅਦ ਆਪਣੇ-ਆਪ ਹਟ ਜਾਂਦੀਆਂ ਹਨ"),
        E("Send Feedback", "फ़ीडबैक भेजें", "ਫੀਡਬੈਕ ਭੇਜੋ"),
        E("Delete this request?", "यह मांग मिटाएं?", "ਇਹ ਮੰਗ ਮਿਟਾਓ?"),
        E("Report a civic issue to get started", "शुरू करने के लिए कोई नागरिक समस्या दर्ज करें", "ਸ਼ੁਰੂ ਕਰਨ ਲਈ ਕੋਈ ਸ਼ਹਿਰੀ ਸਮੱਸਿਆ ਦਰਜ ਕਰੋ"),
        E("Full Name", "पूरा नाम", "ਪੂਰਾ ਨਾਮ"),
        E("Area / Mohalla", "क्षेत्र / मोहल्ला", "ਇਲਾਕਾ / ਮੁਹੱਲਾ"),
        E("Save", "सहेजें", "ਸੰਭਾਲੋ"),
        E("Enter your work type", "अपना काम का प्रकार लिखें", "ਆਪਣੇ ਕੰਮ ਦੀ ਕਿਸਮ ਲਿਖੋ"),
        E("🩸 Blood Group", "🩸 रक्त समूह", "🩸 ਖੂਨ ਦਾ ਗਰੁੱਪ"),
        E("I am willing to donate blood if needed", "ज़रूरत पड़ने पर मैं रक्तदान के लिए तैयार हूं", "ਲੋੜ ਪੈਣ ਤੇ ਮੈਂ ਖੂਨਦਾਨ ਲਈ ਤਿਆਰ ਹਾਂ"),
        E("No jobs accepted yet", "अभी कोई काम स्वीकार नहीं किया", "ਹਾਲੇ ਕੋਈ ਕੰਮ ਮਨਜ਼ੂਰ ਨਹੀਂ ਕੀਤਾ"),
        E("Mark Complete", "पूरा हुआ चिह्नित करें", "ਪੂਰਾ ਹੋਇਆ ਚਿੰਨ੍ਹਿਤ ਕਰੋ"),
        E("Rate your experience", "अपना अनुभव रेट करें", "ਆਪਣੇ ਤਜਰਬੇ ਨੂੰ ਰੇਟ ਕਰੋ"),
        E("Tell us more (optional)", "हमें और बताएं (वैकल्पिक)", "ਸਾਨੂੰ ਹੋਰ ਦੱਸੋ (ਵਿਕਲਪਿਕ)"),
        E("Submit Feedback", "फ़ीडबैक जमा करें", "ਫੀਡਬੈਕ ਜਮ੍ਹਾਂ ਕਰੋ"),
        E("Skip", "छोड़ें", "ਛੱਡੋ"),

        // ── Emergency ──
        E("EMERGENCY · ਐਮਰਜੈਂਸੀ", "आपातकाल · EMERGENCY", "ਐਮਰਜੈਂਸੀ · EMERGENCY"),
        E("Help is one tap away", "मदद बस एक टैप दूर है", "ਮਦਦ ਬੱਸ ਇੱਕ ਟੈਪ ਦੂਰ ਹੈ"),
        E("SOS · Call 112", "SOS · 112 पर कॉल करें", "SOS · 112 ਤੇ ਕਾਲ ਕਰੋ"),
        E("National emergency number", "राष्ट्रीय आपातकालीन नंबर", "ਰਾਸ਼ਟਰੀ ਐਮਰਜੈਂਸੀ ਨੰਬਰ"),
        E("Blood donors", "रक्तदाता", "ਖੂਨਦਾਨੀ"),
        E("Find donors by blood group", "रक्त समूह से दाता खोजें", "ਖੂਨ ਦੇ ਗਰੁੱਪ ਨਾਲ ਦਾਨੀ ਲੱਭੋ"),
        E("HELPLINES · ਹੈਲਪਲਾਈਨ", "हेल्पलाइन · HELPLINES", "ਹੈਲਪਲਾਈਨ · HELPLINES"),

        // ── Feed / worker ──
        E("Nothing to show right now", "अभी दिखाने के लिए कुछ नहीं", "ਹੁਣ ਵਿਖਾਉਣ ਲਈ ਕੁਝ ਨਹੀਂ"),
        E("Community news, ads, and emergency alerts will appear here", "समुदाय की खबरें, विज्ञापन और आपातकालीन अलर्ट यहां दिखेंगे", "ਭਾਈਚਾਰੇ ਦੀਆਂ ਖ਼ਬਰਾਂ, ਇਸ਼ਤਿਹਾਰ ਅਤੇ ਐਮਰਜੈਂਸੀ ਅਲਰਟ ਇੱਥੇ ਦਿਖਣਗੇ"),
        E("Emergency Need", "आपातकालीन ज़रूरत", "ਐਮਰਜੈਂਸੀ ਲੋੜ"),
        E("WORKER  ·  ਕਾਮਾ", "कामगार  ·  WORKER", "ਕਾਮਾ  ·  WORKER"),
        E("Availability Status", "उपलब्धता की स्थिति", "ਉਪਲਬਧਤਾ ਦੀ ਸਥਿਤੀ"),
        E("No jobs in demand", "अभी कोई काम की मांग नहीं", "ਹਾਲੇ ਕੋਈ ਕੰਮ ਦੀ ਮੰਗ ਨਹੀਂ"),
        E("Seeker requests matching your category (or open to all) will show here", "आपकी श्रेणी (या सभी के लिए खुली) से मेल खाती मांगें यहां दिखेंगी", "ਤੁਹਾਡੀ ਸ਼੍ਰੇਣੀ (ਜਾਂ ਸਭ ਲਈ ਖੁੱਲ੍ਹੀਆਂ) ਨਾਲ ਮੇਲ ਖਾਂਦੀਆਂ ਮੰਗਾਂ ਇੱਥੇ ਦਿਖਣਗੀਆਂ"),
        E("Post Service", "सेवा डालें", "ਸੇਵਾ ਪਾਓ"),
        E("Broadcast", "सभी को भेजें", "ਸਭ ਨੂੰ ਭੇਜੋ"),
        E("Decline", "अस्वीकार करें", "ਇਨਕਾਰ ਕਰੋ"),
        E("Accept & Call", "स्वीकार करें और कॉल करें", "ਮਨਜ਼ੂਰ ਕਰੋ ਅਤੇ ਕਾਲ ਕਰੋ"),
        E("Job Requests", "काम की मांगें", "ਕੰਮ ਦੀਆਂ ਮੰਗਾਂ"),
        E("No job requests for your category", "आपकी श्रेणी के लिए कोई मांग नहीं", "ਤੁਹਾਡੀ ਸ਼੍ਰੇਣੀ ਲਈ ਕੋਈ ਮੰਗ ਨਹੀਂ"),
        E("New requests will show up here.", "नई मांगें यहां दिखेंगी।", "ਨਵੀਆਂ ਮੰਗਾਂ ਇੱਥੇ ਦਿਖਣਗੀਆਂ।"),
        E("Call Seeker", "ग्राहक को कॉल करें", "ਗਾਹਕ ਨੂੰ ਕਾਲ ਕਰੋ"),

        // ── Map ──
        E("Your requested location", "आपका मांगा हुआ स्थान", "ਤੁਹਾਡੀ ਮੰਗੀ ਥਾਂ"),
        E("On the way to customer", "ग्राहक के पास जा रहे हैं", "ਗਾਹਕ ਵੱਲ ਜਾ ਰਹੇ ਹਨ"),
        E("No active job right now", "अभी कोई सक्रिय काम नहीं", "ਹੁਣ ਕੋਈ ਸਰਗਰਮ ਕੰਮ ਨਹੀਂ"),
        E("Accept a job to see the customer's exact location here", "ग्राहक का सही स्थान यहां देखने के लिए कोई काम स्वीकार करें", "ਗਾਹਕ ਦੀ ਸਹੀ ਥਾਂ ਇੱਥੇ ਵੇਖਣ ਲਈ ਕੋਈ ਕੰਮ ਮਨਜ਼ੂਰ ਕਰੋ"),
        E("Worker on the way", "कारीगर रास्ते में है", "ਕਾਮਾ ਰਸਤੇ ਵਿੱਚ ਹੈ"),
        E("Call Worker", "कारीगर को कॉल करें", "ਕਾਮੇ ਨੂੰ ਕਾਲ ਕਰੋ"),
        E("Total", "कुल", "ਕੁੱਲ"),
        E("Submit a complaint with GPS location to see it here", "यहां देखने के लिए GPS लोकेशन के साथ शिकायत दर्ज करें", "ਇੱਥੇ ਵੇਖਣ ਲਈ GPS ਲੋਕੇਸ਼ਨ ਨਾਲ ਸ਼ਿਕਾਇਤ ਦਰਜ ਕਰੋ"),
        E("Mark Job Complete", "काम पूरा हुआ चिह्नित करें", "ਕੰਮ ਪੂਰਾ ਹੋਇਆ ਚਿੰਨ੍ਹਿਤ ਕਰੋ"),
        E("Add before/after photos as proof of work (optional)", "काम के सबूत के रूप में पहले/बाद की फ़ोटो जोड़ें (वैकल्पिक)", "ਕੰਮ ਦੇ ਸਬੂਤ ਵਜੋਂ ਪਹਿਲਾਂ/ਬਾਅਦ ਦੀਆਂ ਫੋਟੋਆਂ ਜੋੜੋ (ਵਿਕਲਪਿਕ)"),
        E("Before Photo URL", "पहले की फ़ोटो का URL", "ਪਹਿਲਾਂ ਦੀ ਫੋਟੋ ਦਾ URL"),
        E("After Photo URL", "बाद की फ़ोटो का URL", "ਬਾਅਦ ਦੀ ਫੋਟੋ ਦਾ URL"),

        // ── Onboarding ──
        E("STEP 2 OF 2 · ਪ੍ਰੋਫਾਈਲ", "चरण 2/2 · प्रोफ़ाइल", "ਪੜਾਅ 2/2 · ਪ੍ਰੋਫਾਈਲ"),
        E("Setup your profile", "अपनी प्रोफ़ाइल बनाएं", "ਆਪਣੀ ਪ੍ਰੋਫਾਈਲ ਬਣਾਓ"),
        E("Tell us about yourself so neighbours can trust you.", "अपने बारे में बताएं ताकि पड़ोसी आप पर भरोसा कर सकें।", "ਆਪਣੇ ਬਾਰੇ ਦੱਸੋ ਤਾਂ ਜੋ ਗੁਆਂਢੀ ਤੁਹਾਡੇ ਤੇ ਭਰੋਸਾ ਕਰ ਸਕਣ।"),
        E("Full name", "पूरा नाम", "ਪੂਰਾ ਨਾਮ"),
        E("Continue", "आगे बढ़ें", "ਅੱਗੇ ਵਧੋ"),
        E("WORKER SETUP · ਕੰਮ ਦੀ ਜਾਣਕਾਰੀ", "काम की जानकारी · WORKER SETUP", "ਕੰਮ ਦੀ ਜਾਣਕਾਰੀ · WORKER SETUP"),
        E("Your work details", "आपके काम का विवरण", "ਤੁਹਾਡੇ ਕੰਮ ਦਾ ਵੇਰਵਾ"),
        E("Pick what you do. Seekers will find you by this.", "अपना काम चुनें। ग्राहक आपको इसी से खोजेंगे।", "ਆਪਣਾ ਕੰਮ ਚੁਣੋ। ਗਾਹਕ ਤੁਹਾਨੂੰ ਇਸੇ ਨਾਲ ਲੱਭਣਗੇ।"),
        E("Years of Experience", "अनुभव के वर्ष", "ਤਜਰਬੇ ਦੇ ਸਾਲ"),
        E("Your Charges / Price", "आपका शुल्क / दाम", "ਤੁਹਾਡੀ ਫੀਸ / ਕੀਮਤ"),
        E("About Your Services", "आपकी सेवाओं के बारे में", "ਤੁਹਾਡੀਆਂ ਸੇਵਾਵਾਂ ਬਾਰੇ"),
        E("Describe your skills and experience...", "अपने हुनर और अनुभव का वर्णन करें...", "ਆਪਣੇ ਹੁਨਰ ਅਤੇ ਤਜਰਬੇ ਦਾ ਵਰਣਨ ਕਰੋ..."),
        E("Aadhar Card Number", "आधार कार्ड नंबर", "ਆਧਾਰ ਕਾਰਡ ਨੰਬਰ"),
        E("12-digit Aadhar number", "12 अंकों का आधार नंबर", "12 ਅੰਕਾਂ ਦਾ ਆਧਾਰ ਨੰਬਰ"),
        E("Aadhar details are stored securely for verification only", "आधार की जानकारी केवल सत्यापन के लिए सुरक्षित रखी जाती है", "ਆਧਾਰ ਦੀ ਜਾਣਕਾਰੀ ਸਿਰਫ਼ ਪੜਤਾਲ ਲਈ ਸੁਰੱਖਿਅਤ ਰੱਖੀ ਜਾਂਦੀ ਹੈ"),
        E("Save & Continue", "सहेजें और आगे बढ़ें", "ਸੰਭਾਲੋ ਅਤੇ ਅੱਗੇ ਵਧੋ"),
        E("NANGAL SEVA  ·  ਨੰਗਲ ਸੇਵਾ", "नंगल सेवा  ·  NANGAL SEVA", "ਨੰਗਲ ਸੇਵਾ  ·  NANGAL SEVA"),
        E("One door to every local service.", "हर स्थानीय सेवा के लिए एक दरवाज़ा।", "ਹਰ ਸਥਾਨਕ ਸੇਵਾ ਲਈ ਇੱਕ ਦਰਵਾਜ਼ਾ।"),
        E("ਹਰ ਸਥਾਨਕ ਸੇਵਾ ਲਈ ਇੱਕ ਦਰਵਾਜ਼ਾ", "हर स्थानीय सेवा के लिए एक दरवाज़ा", "ਹਰ ਸਥਾਨਕ ਸੇਵਾ ਲਈ ਇੱਕ ਦਰਵਾਜ਼ਾ"),
        E("Find trusted workers, report issues and get help in Nangal. No ads. No spam.", "नंगल में भरोसेमंद कारीगर खोजें, समस्याएं दर्ज करें और मदद पाएं। कोई विज्ञापन नहीं। कोई स्पैम नहीं।", "ਨੰਗਲ ਵਿੱਚ ਭਰੋਸੇਮੰਦ ਕਾਮੇ ਲੱਭੋ, ਸਮੱਸਿਆਵਾਂ ਦਰਜ ਕਰੋ ਅਤੇ ਮਦਦ ਪਾਓ। ਕੋਈ ਇਸ਼ਤਿਹਾਰ ਨਹੀਂ। ਕੋਈ ਸਪੈਮ ਨਹੀਂ।"),
        E("Phone Number", "फ़ोन नंबर", "ਫ਼ੋਨ ਨੰਬਰ"),
        E("By continuing, you agree to our Terms of Service", "आगे बढ़ने पर आप हमारी सेवा शर्तों से सहमत होते हैं", "ਅੱਗੇ ਵਧਣ ਤੇ ਤੁਸੀਂ ਸਾਡੀਆਂ ਸੇਵਾ ਸ਼ਰਤਾਂ ਨਾਲ ਸਹਿਮਤ ਹੁੰਦੇ ਹੋ"),
        E("STEP 1 OF 1  ·  ਆਪਣੀ ਭੂਮਿਕਾ ਚੁਣੋ", "चरण 1/1  ·  अपनी भूमिका चुनें", "ਪੜਾਅ 1/1  ·  ਆਪਣੀ ਭੂਮਿਕਾ ਚੁਣੋ"),
        E("How will you use Nangal Seva?", "आप नंगल सेवा का उपयोग कैसे करेंगे?", "ਤੁਸੀਂ ਨੰਗਲ ਸੇਵਾ ਕਿਵੇਂ ਵਰਤੋਗੇ?"),
        E("You can switch your role later from your profile.", "आप बाद में प्रोफ़ाइल से अपनी भूमिका बदल सकते हैं।", "ਤੁਸੀਂ ਬਾਅਦ ਵਿੱਚ ਪ੍ਰੋਫਾਈਲ ਤੋਂ ਆਪਣੀ ਭੂਮਿਕਾ ਬਦਲ ਸਕਦੇ ਹੋ।"),
        E("Seeker", "ग्राहक", "ਗਾਹਕ"),
        E("Find trusted workers, post a job request and report civic issues.", "भरोसेमंद कारीगर खोजें, काम की मांग डालें और नागरिक समस्याएं दर्ज करें।", "ਭਰੋਸੇਮੰਦ ਕਾਮੇ ਲੱਭੋ, ਕੰਮ ਦੀ ਮੰਗ ਪਾਓ ਅਤੇ ਸ਼ਹਿਰੀ ਸਮੱਸਿਆਵਾਂ ਦਰਜ ਕਰੋ।"),
        E("Worker", "कारीगर", "ਕਾਮਾ"),
        E("Offer your skills, accept jobs nearby and build your rating.", "अपना हुनर पेश करें, पास के काम स्वीकार करें और रेटिंग बनाएं।", "ਆਪਣਾ ਹੁਨਰ ਪੇਸ਼ ਕਰੋ, ਨੇੜੇ ਦੇ ਕੰਮ ਮਨਜ਼ੂਰ ਕਰੋ ਅਤੇ ਰੇਟਿੰਗ ਬਣਾਓ।"),
        E("Admin", "एडमिन", "ਐਡਮਿਨ"),
        E("Municipal or government official managing the city.", "शहर का प्रबंधन करने वाले नगरपालिका या सरकारी अधिकारी।", "ਸ਼ਹਿਰ ਦਾ ਪ੍ਰਬੰਧ ਕਰਨ ਵਾਲੇ ਨਗਰਪਾਲਿਕਾ ਜਾਂ ਸਰਕਾਰੀ ਅਧਿਕਾਰੀ।"),
        E("Active", "सक्रिय", "ਸਰਗਰਮ"),
        E("Verify OTP", "OTP जांचें", "OTP ਜਾਂਚੋ"),
        E("Verify", "जांचें", "ਜਾਂਚੋ"),
        E("Resend OTP", "OTP दोबारा भेजें", "OTP ਦੁਬਾਰਾ ਭੇਜੋ"),

        // ── Blood donors ──
        E("Register", "पंजीकरण करें", "ਰਜਿਸਟਰ ਕਰੋ"),
        E("Search by phone number...", "फ़ोन नंबर से खोजें...", "ਫ਼ੋਨ ਨੰਬਰ ਨਾਲ ਲੱਭੋ..."),
        E("Active filters:", "सक्रिय फ़िल्टर:", "ਸਰਗਰਮ ਫਿਲਟਰ:"),
        E("Clear all", "सब हटाएं", "ਸਭ ਹਟਾਓ"),
        E("Register as Donor", "दाता के रूप में पंजीकरण करें", "ਦਾਨੀ ਵਜੋਂ ਰਜਿਸਟਰ ਕਰੋ"),
        E("Register as donor", "दाता के रूप में पंजीकरण करें", "ਦਾਨੀ ਵਜੋਂ ਰਜਿਸਟਰ ਕਰੋ"),
        E("Delete Donor", "दाता मिटाएं", "ਦਾਨੀ ਮਿਟਾਓ"),
        E("Edit Details", "विवरण संपादित करें", "ਵੇਰਵੇ ਸੰਪਾਦਿਤ ਕਰੋ"),

        // ── Earnings / favourites ──
        E("WORKER · ਕਮਾਈ", "कमाई · WORKER", "ਕਮਾਈ · WORKER"),
        E("My earnings", "मेरी कमाई", "ਮੇਰੀ ਕਮਾਈ"),
        E("Total earned", "कुल कमाई", "ਕੁੱਲ ਕਮਾਈ"),
        E("Jobs done", "पूरे किए काम", "ਪੂਰੇ ਕੀਤੇ ਕੰਮ"),
        E("This month", "इस महीने", "ਇਸ ਮਹੀਨੇ"),
        E("Avg rating", "औसत रेटिंग", "ਔਸਤ ਰੇਟਿੰਗ"),
        E("No completed jobs yet", "अभी कोई पूरा काम नहीं", "ਹਾਲੇ ਕੋਈ ਪੂਰਾ ਕੰਮ ਨਹੀਂ"),
        E("Finished jobs and their fees will show up here.", "पूरे हुए काम और उनकी फ़ीस यहां दिखेगी।", "ਪੂਰੇ ਹੋਏ ਕੰਮ ਅਤੇ ਉਨ੍ਹਾਂ ਦੀ ਫੀਸ ਇੱਥੇ ਦਿਖੇਗੀ।"),
        E("SAVED · ਪਸੰਦੀਦਾ", "पसंदीदा · SAVED", "ਪਸੰਦੀਦਾ · SAVED"),
        E("My favorites", "मेरे पसंदीदा", "ਮੇਰੇ ਪਸੰਦੀਦਾ"),
        E("Tap the heart on a worker to save them here.", "यहां सहेजने के लिए कारीगर पर दिल दबाएं।", "ਇੱਥੇ ਸੰਭਾਲਣ ਲਈ ਕਾਮੇ ਤੇ ਦਿਲ ਦਬਾਓ।"),

        // ── AI chat ──
        E("TRY ASKING  ·  ਪੁੱਛ ਕੇ ਦੇਖੋ", "पूछकर देखें  ·  TRY ASKING", "ਪੁੱਛ ਕੇ ਦੇਖੋ  ·  TRY ASKING"),
        E("Ask anything about Nangal…", "नंगल के बारे में कुछ भी पूछें…", "ਨੰਗਲ ਬਾਰੇ ਕੁਝ ਵੀ ਪੁੱਛੋ…"),
    )

    val hi: Map<String, String> = all.associate { it.en to it.hi }
    val pa: Map<String, String> = all.associate { it.en to it.pa }
}

/** Per-language text for strings with dynamic parts (write the values with ${...} already filled in). */
@Composable
fun trl(en: String, hi: String, pa: String): String = when (LocalAppLanguage.current) {
    AppLanguage.ENGLISH -> en
    AppLanguage.HINDI -> hi
    AppLanguage.PUNJABI -> pa
}

/** Footer (bottom-nav) secondary label. Footer always shows English; this is the extra line for Hindi/Punjabi. */
@Composable
fun navSecondary(en: String): String? {
    val lang = LocalAppLanguage.current
    if (lang == AppLanguage.ENGLISH) return null
    val row = when (en) {
        "Feed" -> "फ़ीड" to "ਫੀਡ"
        "Services" -> "सेवाएं" to "ਸੇਵਾਵਾਂ"
        "SOS" -> "SOS" to "SOS"
        "Complaints" -> "शिकायतें" to "ਸ਼ਿਕਾਇਤਾਂ"
        "Map" -> "नक्शा" to "ਨਕਸ਼ਾ"
        "Dashboard" -> "डैशबोर्ड" to "ਡੈਸ਼ਬੋਰਡ"
        "Issues" -> "मुद्दे" to "ਮੁੱਦੇ"
        "Resolved" -> "हल हुईं" to "ਹੱਲ ਹੋਈਆਂ"
        "Reports" -> "रिपोर्ट" to "ਰਿਪੋਰਟਾਂ"
        "Notices" -> "सूचनाएं" to "ਸੂਚਨਾਵਾਂ"
        else -> return null
    }
    return if (lang == AppLanguage.HINDI) row.first else row.second
}

/** Translates a category name like "🔧 Plumber" (keeps the emoji prefix) or plain "Plumber". */
@Composable
fun trCat(raw: String): String {
    val sp = raw.indexOf(' ')
    return if (sp > 0 && !raw[0].isLetterOrDigit()) raw.substring(0, sp) + " " + tr(raw.substring(sp + 1))
    else tr(raw)
}
