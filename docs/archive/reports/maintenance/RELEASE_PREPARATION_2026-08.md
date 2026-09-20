# سجل تجهيز إصدار Android للرفع — 2026-08

## النطاق

هذا السجل يجهز تطبيق Telegram Drive Uploader للرفع عبر Workflow الإصدار الموجود في `.github/workflows/android-release.yml`. لم ينفذ هذا السجل commit أو push أو GitHub Release أو نشرًا خارجيًا.

## إعداد الإصدار الموجود

| البند | القيمة المتحققة من المصدر |
|---|---|
| `applicationId` | `com.telegramdrive.uploader` |
| `versionName` | `1.0.18` |
| `versionCode` | `18` |
| `compileSdk` / `targetSdk` | `36` / `36` |
| `minSdk` | `24` |
| ABI | `arm64-v8a`, `armeabi-v7a`, `x86_64` |
| R8 | مفعّل في Release عبر `isMinifyEnabled = true` |
| Resource shrinking | مفعّل عبر `isShrinkResources = true` |
| Release signing | مشروط بمتغيرات بيئة/أسرار CI، ولا يوجد مفتاح إنتاج محليًا |

## بوابات التحقق

نجحت محليًا بوابات الأمن، الكود الإنتاجي، سلامة الموارد، Manifest الخاص بـWorkManager، وسلامة ملفات TDLib v1.8.66. تحقق فحص TDLib من وجود المكتبات الرسمية لكل ABI، مع تحقق ELF للمعمارية والاعتماديات وJava bindings. كما نجح `:app:lintVitalRelease`، ونجح `git diff --check` بعد إزالة سطر زائد من دفتر المهام.

لم يُبنَ Release APK موقّع محليًا؛ متطلبات التوقيع الإنتاجي الأربعة غير موجودة في البيئة الحالية: `RELEASE_KEYSTORE_BASE64`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, و`RELEASE_KEY_PASSWORD`. هذا مقصود أمنيًا. لا ينبغي إنشاء أو مشاركة keystore إنتاجي داخل المستودع أو المحادثة.

## مسار الرفع الآمن

Workflow الإصدار يتحقق من tag بصيغة `vMAJOR.MINOR.PATCH`، ويطابقه مع `versionName`. للنسخة الحالية يجب أن يكون tag هو `v1.0.18` أو suffix prerelease متوافقًا. بعد ذلك يبني مصفوفة ABI منفصلة، يشغل بوابات الأمن والاختبارات و`lintVitalRelease`، يبني Release موقّعًا، يتحقق من `libtdjni.so` الخاص بكل ABI، يشغل `apksigner verify`، ينشئ SHA-256، ثم ينشر الأصول فقط بعد نجاح المصفوفة كاملة.

قبل تشغيل workflow يجب وضع الأسرار الأربعة الخاصة بالتوقيع، إضافة إلى `TELEGRAM_API_ID` و`TELEGRAM_API_HASH`، في GitHub Actions Secrets باسمها الدقيق. لا تُكتب القيم في `build.gradle.kts` أو `.env` المتعقب أو سجل CI. يقوم workflow بإنشاء الملفات المؤقتة أثناء التشغيل ويحذفها في خطوة `always()`.

## حالة الجاهزية

حالة المصدر وWorkflow: **READY FOR AUTHORIZED CI RELEASE**.

حالة APK إنتاجي موقّع محليًا: **NO-GO / BLOCKED** حتى تتوفر أسرار التوقيع الإنتاجي داخل GitHub Secrets وتُنفّذ بوابات CI. لا تكفي نسخة Debug أو نجاح compile أو وجود TDLib لإثبات صلاحية إصدار الإنتاج أو مصادقة Telegram أو رفع ملف حقيقي.

لم تُجرَ اختبارات جهاز أو محاكي لتسجيل Telegram أو الوجهات أو رفع ملف حقيقي، لذلك تظل هذه السيناريوهات **NOT VERIFIED**.
