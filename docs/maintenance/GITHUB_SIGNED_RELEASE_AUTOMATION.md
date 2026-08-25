# أتمتة إصدار APK موقّع عبر GitHub Actions

## الغرض وحدود الثقة

ينشئ `.github/workflows/android-release.yml` ملفات APK موقّعة للإنتاج لكل من `arm64-v8a` و`armeabi-v7a` و`x86_64`، ثم ينشر GitHub Release فقط بعد نجاح كل مهام ABI والتحقق من مكتبة TDLib والتوقيع والتجزئة. لا يغير السريان الحقيقي للمصادقة أو الوجهة أو Room أو WorkManager أو TDLib؛ إنه مسار بناء وإصدار فقط.

> نجاح CI يثبت التغليف والتوقيع والتحقق الساكن، **ولا يثبت** تسجيل الدخول إلى Telegram أو صلاحيات الوجهة أو الرفع الحقيقي على هاتف.

## متى يعمل المسار

يبدأ سير العمل تلقائيًا عند دفع وسم بالشكل `vMAJOR.MINOR.PATCH` أو وسم مسبق بإضافة لاحقة، مثل `v1.0.19` أو `v1.0.19-rc.1`. ويرفض الوسم إن لم يطابق `versionName` في `app/build.gradle.kts` أو إن كان GitHub Release موجودًا للوسم نفسه. يمكن أيضًا تشغيله يدويًا من صفحة Actions، لكن يجب أن يكون الوسم موجودًا مسبقًا في المستودع.

## إعداد الأسرار

أضف القيم الآتية من صفحة المستودع: **Settings → Secrets and variables → Actions → New repository secret**. لا تضفها إلى `.env` المتعقب أو ملف YAML أو سجل الإصدار.

| السر | المحتوى |
|---|---|
| `RELEASE_KEYSTORE_BASE64` | ملف `release.keystore` كامل مرمّز Base64 كسطر واحد. |
| `RELEASE_STORE_PASSWORD` | كلمة مرور الـkeystore. |
| `RELEASE_KEY_ALIAS` | اسم alias لمفتاح الإنتاج. |
| `RELEASE_KEY_PASSWORD` | كلمة مرور المفتاح. |
| `TELEGRAM_API_ID` | معرّف Telegram API المخصص للتطبيق. |
| `TELEGRAM_API_HASH` | قيمة Telegram API Hash المطابقة. |

لتحويل المفتاح إلى Base64 دون طباعته إلى سجل CI:

```bash
base64 -w 0 release.keystore
```

انسخ الناتج مباشرة إلى `RELEASE_KEYSTORE_BASE64`، ثم امسح الحافظة عند الحاجة. احتفظ بنسخة احتياطية مشفرة من المفتاح وكلمات المرور؛ فقدان مفتاح الإنتاج يمنع تقديم تحديثات تثبّت فوق النسخ الموقعة بالمفتاح نفسه.

## إصدار الإصدار الأول

1. حدّث `versionCode` و`versionName` في `app/build.gradle.kts` والتزم بالتغيير بعد نجاح التحقق المحلي.
2. بعد إعداد الأسرار، أنشئ وادفع وسمًا جديدًا غير مستخدم يطابق `versionName`:

```bash
git tag v1.0.19
git push origin v1.0.19
```

3. راقب سير عمل **Android Signed Multi-ABI Release**. يجب أن تنجح ABI الثلاثة قبل أن تنشأ صفحة Release.
4. افحص ملفات `.sha256` ونتيجة `apksigner verify` في سجلات CI، ثم نزّل الأصول من Release للتحقق المستقل قبل التوزيع.

## حواجز النشر

يعطل السير العمل النشر عندما تكون أسرار الإنتاج أو Telegram فارغة، أو يفشل فتح keystore أو اختبار الوحدات أو `lintVitalRelease` أو فحوص الأمان/الموارد/WorkManager/TDLib، أو يغيب `libtdjni.so` للـABI المطلوب، أو يفشل `apksigner verify`، أو لا تطابق التجزئة المرفوعة الأصل. ملفات المفاتيح و`.env` تمسح من مساحة العمل دائمًا، وملفات APK تُرفع إلى صفحة GitHub Release بدل الالتزام إلى فرع المصدر.

## أدلة ما بعد الإصدار

تظل اختبارات الهاتف الفعلي مطلوبة: تشغيل التطبيق بتثبيت جديد، JNI smoke لكل ABI متاح، RTL وإمكانية الوصول، تسجيل الدخول، الوجهة، والرفع الحقيقي إلى قناة اختبار. لا تنشر نتائج غير مشاهدة أو تدّعي اعتماد وصول رسمي بالاعتماد على CI فقط.

## المراجع

[1] [GitHub Docs — Using secrets in GitHub Actions](https://docs.github.com/actions/security-for-github-actions/security-guides/using-secrets-in-github-actions)

[2] [GitHub Docs — Creating releases](https://docs.github.com/repositories/releasing-projects-on-github/managing-releases-in-a-repository)
