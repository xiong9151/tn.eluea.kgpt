import os
import re

translations = {
    'ar': {
        'error': r'لم يتم استلام أي رد. أعاد الذكاء الاصطناعي محتوى فارغًا.\\nتلميح: ربما تستخدم تريجر/أمر غير مسجل.',
        'log': r'تم إصلاح مشكلة اختفاء النص عند استخدام تريجر غير مسجل أو عند عدم تلقي رد من الذكاء الاصطناعي'
    },
    'fr': {
        'error': r"Aucune réponse reçue. L'IA a renvoyé un contenu vide.\\nIndice : Vous utilisez peut-être un déclencheur/commande non enregistré.",
        'log': r"Correction du problème de disparition du texte lors de l'utilisation d'un déclencheur non enregistré ou d'une absence de réponse de l'IA"
    },
    'es': {
        'error': r"No se recibió respuesta. La IA devolvió contenido vacío.\\nPista: Es posible que esté utilizando un activador/comando no registrado.",
        'log': r"Solucionado el problema de desaparición del texto al usar un activador no registrado o al no recibir respuesta de la IA"
    },
    'de': {
        'error': r"Keine Antwort erhalten. Die KI hat leeren Inhalt zurückgegeben.\\nHinweis: Sie verwenden möglicherweise einen nicht registrierten Auslöser/Befehl.",
        'log': r"Problem behoben, bei dem Text verschwindet, wenn ein nicht registrierter Auslöser verwendet wird oder keine Antwort von der KI kommt"
    },
    'it': {
        'error': r"Nessuna risposta ricevuta. L'IA ha restituito un contenuto vuoto.\\nSuggerimento: potresti utilizzare un trigger/comando non registrato.",
        'log': r"Risolto il problema della scomparsa del testo quando si utilizza un trigger non registrato o non si riceve risposta dall'IA"
    },
    'pt': {
        'error': r"Nenhuma resposta recebida. A IA retornou conteúdo vazio.\\nDica: Você pode estar usando um gatilho/comando não registrado.",
        'log': r"Corrigido o problema de desaparecimento do texto ao usar um gatilho não registrado ou não receber resposta da IA"
    },
    'ru': {
        'error': r"Ответ не получен. ИИ вернул пустой контент.\\nПодсказка: возможно, вы используете незарегистрированный триггер/команду.",
        'log': r"Исправлена проблема с исчезновением текста при использовании незарегистрированного триггера или отсутствии ответа от ИИ"
    },
    'tr': {
        'error': r"Yanıt alınamadı. Yapay zeka boş içerik döndürdü.\\nİpucu: Kayıtsız bir tetikleyici/komut kullanıyor olabilirsiniz.",
        'log': r"Kayıtsız bir tetikleyici kullanıldığında veya yapay zekadan yanıt alınamadığında metnin kaybolması sorunu düzeltildi"
    },
    'zh': {
        'error': r"未收到响应。AI 返回了空内容。\\n提示：您可能使用的是未注册的触发器/命令。",
        'log': r"修复了使用未注册的触发器或 AI 无响应时文本消失的问题"
    },
    'hi': {
        'error': r"कोई प्रतिक्रिया प्राप्त नहीं हुई। AI ने खाली सामग्री लौटाई।\\nसंकेत: आप एक अनрегистриड ट्रिगर/कमांड का उपयोग कर रहे होंगे।",
        'log': r"अनрегистриड ट्रिगर का उपयोग करने या AI से कोई प्रतिक्रिया न मिलने पर टेक्स्ट गायब होने की समस्या को ठीक किया गया"
    },
    'id': {
        'error': r"Tidak ada respons diterima. AI mengembalikan konten kosong.\\nPetunjuk: Anda mungkin menggunakan pemicu/perintah yang tidak terdaftar.",
        'log': r"Memperbaiki masalah teks menghilang saat menggunakan pemicu yang tidak terdaftar atau tidak menerima respons dari AI"
    },
    'ja': {
        'error': r"応答がありません。AIは空のコンテンツを返しました。\\nヒント：未登録のトリガー/コマンドを使用している可能性があります。",
        'log': r"未登録のトリガーを使用した場合やAIからの応答がない場合にテキストが消える問題を修正しました"
    },
    'ko': {
        'error': r"응답을 받지 못했습니다. AI가 빈 콘텐츠를 반환했습니다。\\n힌트: 등록되지 않은 트리거/명령을 사용하고 있을 수 있습니다.",
        'log': r"등록되지 않은 트리거를 사용하거나 AI 응답이 없을 때 텍스트가 사라지는 문제를 수정했습니다"
    },
    'bn': {
        'error': r"কোনো সাড়া পাওয়া যায়নি। AI খালি কন্টেন্ট ফেরত দিয়েছে।\\nইঙ্গিত: আপনি সম্ভবত একটি অনিবন্ধিত ট্রিগার/কमांड ব্যবহার করছেন।",
        'log': r"অনিবন্ধিত ট্রিগার ব্যবহার করলে বা AI থেকে কোনো সাড়া না পেলে টেক্সট অদৃশ্য হয়ে যাওয়ার সমস্যাটি সমাধান করা হয়েছে"
    },
    'nl': {
        'error': r"Geen reactie ontvangen. De AI gaf lege inhoud terug.\\nTip: U gebruikt mogelijk een niet-geregistreerde trigger/opdracht.",
        'log': r"Probleem opgelost waarbij tekst verdwijnt bij gebruik van een niet-geregistreerde trigger of geen reactie van AI"
    },
    'pl': {
        'error': r"Nie otrzymano odpowiedzi. AI zwróciło pustą zawartość.\\nWskazówka: Być może używasz niezarejestrowanego wyzwalacza/polecenia.",
        'log': r"Naprawiono problem znikania tekstu przy użyciu niezarejestrowanego wyzwalacza lub braku odpowiedzi od AI"
    },
    'sw': {
        'error': r"Hakuna jibu lililopokelewa. AI ilirudisha maudhui tupu.\\nDokezo: Huenda unatumia kichochezi/amri isiyosajiliwa.",
        'log': r"Imerekebisha tatizo la maandishi kupotea unapotumia kichochezi kisichosajiliwa au kutopokea majibu kutoka kwa AI"
    },
    'th': {
        'error': r"ไม่ได้รับการตอบกลับ AI ส่งคืนเนื้อหาว่างเปล่า\\nคำแนะนำ: คุณอาจกำลังใช้ทริกเกอร์/คำสั่งที่ไม่ได้ลงทะเบียน",
        'log': r"แก้ไขปัญหาข้อความหายไปเมื่อใช้ทริกเกอร์ที่ไม่ได้ลงทะเบียนหรือไม่ได้รับการตอบกลับจาก AI"
    },
    'ur': {
        'error': r"کوئی جواب موصول نہیں ہوا۔ AI نے خالی مواد واپس کیا۔\\nاشارہ: ہو سکتا ہے آپ غیر رجسٹرڈ ٹرگر/کمانڈ استعمال کر رہے ہوں۔",
        'log': r"غیر رجسٹرڈ ٹرگر استعمال کرنے یا AI سے کوئی جواب موصول نہ ہونے پر متن غائب ہونے کا مسئلہ حل کر دیا گیا"
    },
    'vi': {
        'error': r"Không nhận được phản hồi. AI trả về nội dung trống.\\nGợi ý: Bạn có thể đang sử dụng trình kích hoạt/lệnh chưa đăng ký.",
        'log': r"Đã khắc phục sự cố văn bản biến mất khi sử dụng trình kích hoạt chưa đăng ký hoặc không nhận được phản hồi từ AI"
    }
}

base_path = r'c:\Users\Classic\Downloads\KGPT-main (2)\KGPT-main\app\src\main\res'

def update_file(lang, data):
    dir_name = f'values-{lang}'
    file_path = os.path.join(base_path, dir_name, 'strings.xml')
    
    if not os.path.exists(file_path):
        print(f"Skipping {lang}: File not found")
        return

    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()

        # Add Changelog Item 5
        changelog_anchor = 'changelog_4_0_8_item4'
        if changelog_anchor in content:
            new_line = f'    <string name="changelog_4_0_8_item5">{data["log"]}</string>'
            if 'changelog_4_0_8_item5' not in content:
                # Insert after the anchor (regex to find the closing tag of anchor)
                content = re.sub(f'(<string name="{changelog_anchor}">.*?</string>)', f'\\1\n{new_line}', content)
            else:
                print(f"{lang}: Changelog already exists")
        else:
            print(f"{lang}: Anchor not found, appending to resources end")
            # fallback
        
        # Add Error Msg
        error_msg = f'    <string name="msg_ai_no_response_error">{data["error"]}</string>'
        if 'msg_ai_no_response_error' not in content:
             content = re.sub(r'(</resources>)', f'{error_msg}\n\\1', content)
        else:
            print(f"{lang}: Error msg already exists")

        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Updated {lang}")

    except Exception as e:
        print(f"Error updating {lang}: {e}")

for lang, data in translations.items():
    update_file(lang, data)
