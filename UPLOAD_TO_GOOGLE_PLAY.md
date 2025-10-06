# 📤 دليل رفع التطبيق على Google Play Console

## ✅ التحضيرات المكتملة

### 1️⃣ **App Bundle المحسّن**
- ✅ **الملف:** `PDFCreator-v1.2.0-release-optimized.aab` (14 MB)
- ✅ **R8/ProGuard:** مفعّل ✓
- ✅ **Resource Shrinking:** مفعّل ✓
- ✅ **Signed:** موقّع بـ Keystore ✓
- ✅ **Native Debug Symbols:** مفعّل ✓

### 2️⃣ **ملف Mapping (Deobfuscation)**
- ✅ **الموقع:** `app/build/outputs/mapping/release/mapping.txt` (50 MB)
- ✅ **الغرض:** لفك تشفير crash reports وتحليل الأخطاء
- ✅ **مطلوب:** يجب رفعه مع كل App Bundle

### 3️⃣ **ملفات إضافية**
- ✅ **configuration.txt** - إعدادات ProGuard المستخدمة
- ✅ **resources.txt** - الموارد المحذوفة
- ✅ **seeds.txt** - الكلاسات المحفوظة
- ✅ **usage.txt** - تحليل استخدام الكود

---

## 🚀 خطوات الرفع على Google Play Console

### **الخطوة 1: تسجيل الدخول**
1. اذهب إلى: https://play.google.com/console
2. اختر تطبيقك أو أنشئ تطبيقاً جديداً

---

### **الخطوة 2: رفع App Bundle**

#### **A. اذهب إلى Release Management**
```
Production → Releases → Create Release
```
أو
```
Internal Testing / Closed Testing → Create Release
```

#### **B. رفع App Bundle**
1. اضغط **"Upload"**
2. اختر الملف: **`PDFCreator-v1.2.0-release-optimized.aab`**
3. انتظر حتى يكتمل الرفع

#### **C. إدخال معلومات الإصدار**
- **Release name:** `v1.2.0 - Page Numbering & Watermark`
- **Release notes:** (انسخ من `GOOGLE_PLAY_RELEASE_NOTES.md`)

---

### **الخطوة 3: رفع ملف Mapping (مهم جداً!)**

#### **طريقة 1: من واجهة الويب**
1. بعد رفع AAB، ستجد قسم **"App artifacts"**
2. ابحث عن **"Deobfuscation files"**
3. اضغط **"Upload"**
4. اختر الملف: **`app/build/outputs/mapping/release/mapping.txt`**
5. اضغط **"Save"**

#### **طريقة 2: من Release Dashboard**
1. اذهب إلى **"Release" → "App bundle explorer"**
2. اختر الإصدار المرفوع
3. ابحث عن **"Download and re-upload"** في قسم **"Deobfuscation file"**
4. ارفع ملف `mapping.txt`

#### **ملاحظة مهمة:**
⚠️ **احتفظ بنسخة من ملف mapping.txt في مكان آمن!**
- لن تستطيع قراءة crash reports بدونه
- يجب رفعه مع كل إصدار جديد
- لا يمكن استرجاعه بعد فقدانه

---

### **الخطوة 4: Native Debug Symbols (اختياري)**

#### **لماذا التحذير ظاهر؟**
- التطبيق يستخدم مكتبات native من طرف ثالث (androidx.graphics.path.so)
- هذه المكتبات تأتي من Jetpack Compose

#### **الحل:**
يمكنك تجاهل هذا التحذير بأمان لأن:
1. المكتبات من Google (Jetpack)
2. لديها debug symbols خاصة بها
3. تطبيقك لا يحتوي على native code مخصص

#### **إذا أردت إزالة التحذير:**
```bash
# ستحتاج إلى استخراج debug symbols من المكتبات
# ولكن هذا غير ضروري للتطبيق الحالي
```

---

## 📋 قائمة التحقق قبل النشر

### **معلومات التطبيق:**
- [ ] **App name:** H PDF Creator
- [ ] **Package name:** com.tsavvy.pdfcreator
- [ ] **Version name:** 1.2.0
- [ ] **Version code:** 1 (يُفضل تغييره إلى 12000)

### **الملفات المطلوبة:**
- [x] App Bundle (AAB) ✓
- [x] Mapping file (mapping.txt) ✓
- [ ] Release notes (عربي + إنجليزي)
- [ ] Screenshots (8 صور على الأقل)
- [ ] Feature graphic (1024x500)
- [ ] App icon (512x512)

### **إعدادات التطبيق:**
- [ ] **Category:** Productivity / Tools
- [ ] **Content rating:** Everyone
- [ ] **Target audience:** General
- [ ] **Pricing:** Free
- [ ] **In-app purchases:** No
- [ ] **Ads:** No

### **Descriptions:**
- [ ] Short description (80 chars max)
- [ ] Full description (4000 chars max) - استخدم من `GOOGLE_PLAY_RELEASE_NOTES.md`
- [ ] App icon
- [ ] Feature graphic
- [ ] Screenshots (Phone + Tablet)

---

## 🔍 كيفية التحقق من رفع Mapping بنجاح

### **بعد رفع الملف:**
1. اذهب إلى **"Release" → "App bundle explorer"**
2. اختر الإصدار
3. ابحث عن **"Deobfuscation file"**
4. يجب أن ترى: ✅ **"ProGuard mapping (mapping.txt)"**

### **إذا لم يظهر:**
- جرب إعادة رفع الملف
- تأكد من أن الملف بصيغة `.txt`
- تأكد من أن الملف من نفس build

---

## 📊 مقارنة بين النسخ

| العنصر | النسخة القديمة | النسخة الجديدة (Optimized) |
|--------|----------------|---------------------------|
| **حجم AAB** | 13 MB | 14 MB |
| **R8/ProGuard** | ❌ معطّل | ✅ مفعّل |
| **Mapping file** | ❌ لا يوجد | ✅ موجود (50 MB) |
| **Resource shrinking** | ❌ معطّل | ✅ مفعّل |
| **Debug symbols** | ❌ لا يوجد | ✅ مفعّل |
| **Crash analysis** | ⚠️ صعب | ✅ سهل |
| **Code obfuscation** | ❌ لا | ✅ نعم |

**ملاحظة:** الحجم زاد قليلاً بسبب debug symbols، لكن حجم APK الفعلي الذي سيُحمّل للمستخدمين سيكون أصغر بفضل App Bundle optimization.

---

## 🎯 حجم التنزيل المتوقع للمستخدمين

عند رفع AAB على Google Play:
- **Base APK:** ~8-10 MB (يعتمد على architecture)
- **Config APKs:** ~1-2 MB (للموارد حسب الجهاز)
- **Total download:** ~9-12 MB (أصغر من AAB بكثير!)

هذا هو السبب في استخدام AAB بدلاً من APK الواحد.

---

## 🔒 أمان الملفات

### **ملفات يجب حفظها (مهمة جداً!):**
1. **app/pdf-creator-release-key.jks** - Keystore (لا تفقده أبداً!)
2. **app/build/outputs/mapping/release/mapping.txt** - لكل إصدار
3. **local.properties** - يحتوي على كلمات مرور Keystore

### **ملفات يمكن مشاركتها:**
1. **PDFCreator-v1.2.0-release-optimized.aab** - للرفع على Google Play
2. **PDFCreator-v1.2.0-release.apk** - للتوزيع المباشر
3. **GOOGLE_PLAY_RELEASE_NOTES.md** - ملاحظات الإصدار

---

## 📞 الدعم

إذا واجهت أي مشاكل:
1. تحقق من [Google Play Console Help](https://support.google.com/googleplay/android-developer)
2. راجع الملف `RELEASE_NOTES_v1.2.0.md` للتفاصيل الفنية
3. تأكد من أن جميع الملفات موقعة بنفس Keystore

---

## ✅ ملخص سريع

```bash
# 1. الملف الرئيسي للرفع
PDFCreator-v1.2.0-release-optimized.aab (14 MB)

# 2. ملف Mapping (مهم!)
app/build/outputs/mapping/release/mapping.txt (50 MB)

# 3. اتبع الخطوات في Google Play Console
# 4. ارفع AAB أولاً
# 5. ثم ارفع mapping.txt
# 6. أكمل معلومات الإصدار
# 7. اضغط Review & Roll out
```

---

**تاريخ الإنشاء:** October 6, 2025  
**الإصدار:** v1.2.0  
**الحزمة:** com.tsavvy.pdfcreator  
**الحالة:** ✅ جاهز للنشر

