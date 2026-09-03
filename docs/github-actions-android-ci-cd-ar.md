# إعداد CI/CD لتطبيق Telegram Drive Uploader عبر GitHub Actions

## الفكرة العامة

يُفضّل فصل المسار إلى مستويين. يعمل مسار **التحقق** عند فتح Pull Request أو الدفع إلى `main`، ويشغّل الفحوصات السريعة دون استخدام مفتاح التوقيع. أما مسار **الإصدار** فيعمل عند إنشاء وسم مثل `v1.0.19`، ويبني APK موقّعًا لكل ABI ثم ينشئ GitHub Release ويرفق ملفات APK وملفات SHA-256.

يحتوي مستودع Telegram Drive Uploader حاليًا على مسار إصدار جاهز في `.github/workflows/android-release.yml`. هذا المسار يستخدم JDK 17، وAndroid SDK API 36، وNDK `26.3.11579264`، وGradle 8.9، ويبني `arm64-v8a` و`armeabi-v7a` و`x86_64` في مصفوفة مستقلة.

## 1. إنشاء مسار التحقق عند Pull Request

أنشئ الملف `.github/workflows/android-ci.yml`:

```yaml
name: Android CI

on:
  pull_request:
  push:
    branches: [main]

permissions:
  contents: read

concurrency:
  group: android-ci-${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true

jobs:
  verify:
    name: Verify Android project
    runs-on: ubuntu-24.04
    timeout-minutes: 30

    steps:
      - name: Checkout
        uses: actions/checkout@v7

      - name: Set up JDK 17
        uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: '17'

      - name: Set up Android SDK
        uses: android-actions/setup-android@v4

      - name: Install Android SDK packages
        run: |
          sdkmanager "platform-tools" "platforms;android-36" "build-tools;36.0.0" "ndk;26.3.11579264"

      - name: Set up Gradle 8.9
        uses: gradle/actions/setup-gradle@v6
        with:
          gradle-version: '8.9'

      - name: Prepare Telegram configuration
        env:
          TELEGRAM_API_ID: ${{ secrets.TELEGRAM_API_ID }}
          TELEGRAM_API_HASH: ${{ secrets.TELEGRAM_API_HASH }}
        run: |
          set -euo pipefail
          test -n "$TELEGRAM_API_ID"
          test -n "$TELEGRAM_API_HASH"
          printf 'TELEGRAM_API_ID=%s\n' "$TELEGRAM_API_ID" > .env
          printf 'TELEGRAM_API_HASH=%s\n' "$TELEGRAM_API_HASH" >> .env
          chmod 600 .env

      - name: Run repository gates
        run: |
          set -euo pipefail
          ./scripts/check-repository-security.sh
          ./scripts/check-resource-integrity.sh
          ./scripts/check-workmanager-manifest.sh
          ./scripts/check-tdlib-artifacts.sh

      - name: Run unit tests and lint
        run: |
          gradle --no-daemon --max-workers=2 \
            :app:testDebugUnitTest \
            :app:lintDebug

      - name: Assemble debug APK
        run: gradle --no-daemon --max-workers=2 :app:assembleDebug

      - name: Upload debug APK
        uses: actions/upload-artifact@v7
        with:
          name: telegram-drive-uploader-debug
          path: app/build/outputs/apk/debug/*.apk
          if-no-files-found: error
          retention-days: 14

      - name: Remove sensitive build files
        if: always()
        run: rm -f .env local.properties
```

لا تضع `RELEASE_KEYSTORE_BASE64` في مسار Pull Request من Fork. أسرار GitHub لا تُمرّر عادةً إلى تشغيل Pull Request القادم من مستودع خارجي، وهذا سلوك أمني مطلوب.

## 2. إنشاء مفتاح توقيع Release بأمان

أنشئ keystore محليًا مرة واحدة فقط، ثم احتفظ بنسخة احتياطية مشفّرة خارج GitHub:

```bash
keytool -genkeypair \
  -v \
  -keystore release.keystore \
  -alias telegram-drive-release \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000
```

حوّل الملف إلى Base64 محليًا، ثم أضف القيمة إلى GitHub Actions Secrets. لا ترفع `release.keystore` أو كلمة المرور إلى المستودع:

```bash
base64 -w 0 release.keystore > release.keystore.b64
```

أنشئ الأسرار التالية من صفحة **Settings → Secrets and variables → Actions**:

| السر | الاستخدام |
|---|---|
| `RELEASE_KEYSTORE_BASE64` | محتوى keystore بعد Base64 |
| `RELEASE_STORE_PASSWORD` | كلمة مرور keystore |
| `RELEASE_KEY_ALIAS` | اسم alias داخل keystore |
| `RELEASE_KEY_PASSWORD` | كلمة مرور المفتاح |
| `TELEGRAM_API_ID` | إعداد Telegram المطلوب للبناء |
| `TELEGRAM_API_HASH` | إعداد Telegram المطلوب للبناء |

يجب أن يختبر المسار وجود الأسرار (`test -n`) وأن يتحقق من alias عبر `keytool -list` قبل بدء البناء.

## 3. ربط التوقيع بملف Gradle

يجب أن يقرأ `app/build.gradle.kts` قيم التوقيع من خصائص Gradle أو متغيرات البيئة، وألا يحتوي على كلمات مرور ثابتة:

```kotlin
val releaseKeystorePath = providers.gradleProperty("RELEASE_KEYSTORE_PATH")
    .orElse(providers.environmentVariable("RELEASE_KEYSTORE_PATH"))

android {
    signingConfigs {
        create("release") {
            storeFile = releaseKeystorePath.map(::file).orNull
            storePassword = providers.gradleProperty("RELEASE_STORE_PASSWORD")
                .orElse(providers.environmentVariable("RELEASE_STORE_PASSWORD"))
                .orNull
            keyAlias = providers.gradleProperty("RELEASE_KEY_ALIAS")
                .orElse(providers.environmentVariable("RELEASE_KEY_ALIAS"))
                .orNull
            keyPassword = providers.gradleProperty("RELEASE_KEY_PASSWORD")
                .orElse(providers.environmentVariable("RELEASE_KEY_PASSWORD"))
                .orNull
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }
}
```

يفترض المثال أن إعدادات Gradle الحالية في المشروع تدعم `RELEASE_KEYSTORE_PATH`. يجب اختبار ذلك محليًا باستخدام keystore غير إنتاجي قبل تفعيل المسار الإنتاجي.

## 4. مسار الإصدار الموقّع

في المشروع الحالي، يعمل الإصدار عند دفع وسم يبدأ بـ `v`:

```yaml
on:
  push:
    tags:
      - 'v*'
  workflow_dispatch:
```

يجب أن يتحقق المسار من أن الوسم يطابق `versionName`. إذا كان `versionName = "1.0.19"`، فاستخدم أحد الشكلين:

```text
v1.0.19
v1.0.19-rc.1
```

مسار الإصدار الحالي ينفّذ الترتيب التالي:

1. يتحقق من صيغة الوسم ومن عدم وجود Release منشور بالوسم نفسه.
2. يثبت JDK 17 وAndroid SDK وNDK والإصدار المحدد من Gradle.
3. ينشئ `.env` مؤقتًا من أسرار Telegram.
4. يبني الاعتماديات الأصلية الرسمية، بما في ذلك OpenSSL وTDLib.
5. يفك Base64 الخاص بالـ keystore داخل مجلد بيئة التشغيل ويجري `keytool -list`.
6. يشغّل فحوصات أمان الموارد وWorkManager وTDLib والاختبارات وlint.
7. يبني APK لكل ABI عبر `-PtargetAbi`.
8. يتحقق من توقيع APK ووجود `libtdjni.so` للمعمارية المطلوبة فقط.
9. ينشئ SHA-256 ويرفع كل APK كـ artifact مؤقت.
10. يجمع artifacts، يتحقق من checksums، ثم ينشئ GitHub Release.

## 5. تشغيل الإصدار

بعد دفع التغييرات إلى `main`، أنشئ الوسم وادفعه:

```bash
git checkout main
git pull --ff-only origin main
git tag -a v1.0.19 -m "Release v1.0.19"
git push origin v1.0.19
```

يمكن أيضًا تشغيل workflow يدويًا من تبويب **Actions** عبر `workflow_dispatch` مع إدخال وسم موجود. لا تعِد استخدام وسم سبق أن أنشأ Release؛ المسار الحالي يرفض الكتابة فوق Release موجود لحماية الأصول المنشورة.

راقب التشغيل باستخدام GitHub CLI:

```bash
gh run list --repo mohtiher3-afk/Telegram-Drive-Uploader --workflow android-release.yml

gh run watch RUN_ID --repo mohtiher3-afk/Telegram-Drive-Uploader
```

بعد النجاح، تحقق من الأصول:

```bash
gh release view v1.0.19 \
  --repo mohtiher3-afk/Telegram-Drive-Uploader
```

## 6. فحص APK بعد التنزيل

نزّل APK المناسب لمعمارية الجهاز، ثم تحقق من checksum والتوقيع:

```bash
sha256sum --check app-arm64-v8a-release.apk.sha256
$ANDROID_HOME/build-tools/36.0.0/apksigner verify \
  --verbose \
  --print-certs \
  app-arm64-v8a-release.apk
```

وللتأكد من المكتبة الأصلية:

```bash
unzip -l app-arm64-v8a-release.apk | grep 'lib/arm64-v8a/libtdjni.so'
```

## 7. تحسينات الأمان والاعتمادية

استخدم `permissions: contents: read` في CI، ولا تمنح `contents: write` إلا لوظيفة `publish` التي تحتاج إنشاء Release. ثبّت إصدارات Actions وJDK وSDK وNDK بدل استخدام `latest`. استخدم `set -euo pipefail` في أوامر Bash، ونظّف `.env` وkeystore في خطوة `if: always()`.

فعّل حماية الفرع `main` مع اشتراط نجاح Android CI قبل الدمج. اجعل إصدار Release يدويًا عبر tag محمي أو بيئة GitHub Environment تتطلب موافقة عند الحاجة. احتفظ بنسخة احتياطية من keystore؛ فقدانه يمنع تحديث التطبيق الموقع بالمفتاح نفسه.

لا تسجل الأسرار أو محتوى `.env` في مخرجات Actions. ولا تستخدم `pull_request_target` لتشغيل كود غير موثوق مع أسرار إنتاجية، لأن ذلك قد يمنح كود Pull Request صلاحية الوصول إلى الأسرار.

## 8. تشخيص الإخفاقات الشائعة

| العرض | السبب المرجح | المعالجة |
|---|---|---|
| `SDK location not found` | غياب Android SDK محليًا | ضبط `ANDROID_HOME` أو استخدام `sdk.dir` محليًا؛ GitHub runner يثبته عبر setup-android |
| `Process completed with exit code 126` | فقدان صلاحية التنفيذ لسكريبت | تنفيذ `chmod +x scripts/*.sh` ثم commit للصلاحيات |
| فشل `keytool -list` | keystore أو alias أو كلمة المرور غير صحيحة | إعادة ترميز keystore والتحقق محليًا بالقيم نفسها |
| عدم وجود APK | اختلاف مسار Gradle outputs أو ABI | طباعة `find app/build/outputs -type f` وتثبيت مسار التحقق |
| checksum لا يطابق | الملف تغيّر بعد إنشاء checksum أو مسار خاطئ | أعد حساب checksum بعد تسوية أسماء الملفات وقبل النشر |
| فشل إنشاء Release بسبب tag | الوسم غير موجود أو يوجد Release سابق | تحقق من `git ls-remote --tags` و`gh release view` واستخدم وسمًا جديدًا |

## المراجع

[1]: https://docs.github.com/en/actions/security-for-github-actions/security-guides/using-secrets-in-github-actions "Using secrets in GitHub Actions — GitHub Docs"

[2]: https://docs.github.com/en/actions/writing-workflows/workflow-syntax-for-github-actions "Workflow syntax for GitHub Actions — GitHub Docs"

[3]: https://developer.android.com/build/building-cmdline "Build your app from the command line — Android Developers"

[4]: https://developer.android.com/studio/publish/app-signing "Sign your app — Android Developers"

[5]: https://github.com/android-actions/setup-android "setup-android — GitHub"

[6]: https://github.com/gradle/actions "Gradle Build Action — GitHub"
