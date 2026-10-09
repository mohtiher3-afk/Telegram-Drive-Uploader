# خطة تطوير Telegram Drive Uploader (للعمل داخل VS Code مع Cline)

> **الحالة: خطة عمل — المراحل 0 إلى 5 منجَزة.** هذه الخطة قادت إعادة بناء Mission Control،
> وهي محفوظة من أجل قواعدها الإجرائية وجدول حلّ المشاكل، **لا كقائمة مهام معلّقة**. منذ كتابتها
> على `main`: دُمج PR #57، وتبنّى `AppColors` لوحة Mission Control مع `AppColorsContrastTest`،
> وأُعيد بناء شاشات Home وQueue وHistory وSettings، ونُشرت أيقونة التطبيق، وأُضيف موديول
> `benchmark`. والسمة الآن طقم **Light + Dark كامل** يُختار من Settings ← Appearance، وليست
> "وضعاً داكناً فقط". تحقّق من كل بند أدناه مقابل `main` قبل التعامل معه كعمل قائم.

ضع هذا الملف في `docs/DEVELOPMENT_PLAN.md`، ومعه `docs/CLINE_TASKS.md`.
علّم كل خطوة بـ `[x]` عند إنجازها. **لا تنتقل لمرحلة قبل إنهاء التي قبلها.**

## قواعد ثابتة (من AGENTS.md ومن تجربة المشروع)
1. مهمة واحدة صغيرة في كل مرة، وفرع لكل مهمة، وcommit بعد كل خطوة ناجحة.
2. لا تعتبر المهمة منتهية إلا بعد نجاح `.\scripts\verify-project.ps1 -Mode QUICK` **وتثبيت التطبيق وتجربته** على جهاز.
3. لا استبدال جماعي (find/replace) على أسماء الرموز، وهو ما كسر `main` سابقاً (PR #53).
4. لا تدمج في `main` إلا بعد نجاح CI. كل مهمة تمس TDLib أو WorkManager أو الرفع أو المصادقة تحتاج تجربة فعلية على جهاز.
5. افتح **New Task** في Cline لكل مهمة، وابدأ بـ Plan mode، وأرفق **صوراً** للتصميم (روابط اللوحة الخاصة لا يقرؤها Cline).
6. الصق أي خطأ كاملاً (stack trace) عند طلب المساعدة، ولا تلخصه.

## إعداد VS Code (مرة واحدة)
- [ ] افتح **مجلد الجذر** (فيه `settings.gradle.kts`) وليس `app` فقط.
- [ ] ثبّت إضافتي Kotlin وGradle for Java.
- [ ] تأكد في طرفية VS Code: `./gradlew --version` يعمل، و`JAVA_HOME` (JDK 17 أو 21) و`ANDROID_HOME` معرّفان.
- [ ] على Windows استخدم PowerShell للسكربتات `.ps1`، ولا تعتمد على bash.
- [x] `.clinerules/` موجود في الجذر كـ **مجلد** (10 ملفات: `00-core` … `07-change-protocol`)
      — وليس ملفاً واحداً كما تقول النسخة القديمة من هذه الوثيقة.
- [x] `CLINE_IMPLEMENTATION_ORDER.md` موجود في الجذر.
- [ ] أنشئ `.clineignore` في الجذر إن لم يكن موجوداً:
```
build/
**/build/
.gradle/
*.so
*.apk
*.aab
releases/
docs/archive/
design/*.png
```
- [ ] راجع `.clinerules/` واعتبره المرجع الحالي؛ لا تُنشئ ملف `.clinerules` منفرداً (يتعارض مع المجلد).

## الحلقة اليومية (لكل مهمة)
1. `git switch main; git pull; git switch -c fix/اسم-المهمة`
2. Cline: **New Task** ← Plan mode ← الصق prompt المهمة ← راجع الخطة ووافق.
3. بدّل إلى Act mode، وراجع كل ملف في لوحة Source Control (Ctrl+Shift+G) قبل الحفظ.
4. `.\scripts\verify-project.ps1 -Mode QUICK` (يجب أن ترى `VERIFICATION PASSED`).
5. ثبّت التطبيق وجرّب الشاشة بعينك.
6. Commit برسالة واضحة (`fix(ui): ...` أو `feat(ui): ...`) ثم Publish Branch ثم افتح PR.
7. انتظر نجاح CI، ثم ادمج (Squash)، ثم نظّف: `git switch main; git pull; git branch -d fix/اسم-المهمة`

---

## المرحلة 0: تثبيت الأساس (يوم واحد)
- [x] تأكد أن `main` على GitHub أخضر (Actions).
- [ ] على جهازك: `git switch main; git pull; .\scripts\verify-project.ps1 -Mode QUICK` ينجح.
- [ ] راجع الـ PRs المفتوحة: ادمج أو أغلق كل واحد.
- [x] **PR #57** (`chore/cline-rules`) — فُحص وفُصل: دُمجت قواعد `.clinerules` وحدها، وأُسقطت
      تعديلات الشاشات القديمة لأن `main` سبقها. لا تعُد إليه.
- [ ] احذف الفروع المحلية القديمة (`git branch -D pr-53` وأي فرع منتهٍ).
- [ ] **لا ميزات جديدة** حتى تنتهي المرحلة 2.

## المرحلة 1: نظافة المستودع (يوم واحد)
- [ ] عطّل workflow `tdlib-release-check` إن لم يكن معطّلاً (Actions ← اسمه ← ⋯ ← Disable
      workflow)، فهو يولّد issues آلية عند كل فشل. راجع `tdlib-release-check-stable.yml` على
      `main` أولاً: قد يكون مضبوطاً بالفعل.
- [x] أداة واحدة (Cline) و`.clinerules/` هو المصدر. `skills-lock.json` على main للقراءة فقط.
- [ ] أبقِ `AGENTS.md` كما هو (قواعده جيدة) ولا تكرر محتواه.
- [x] `docs/archive` **مُزال بالفعل** من `main` (تاريخ git هو الأرشيف؛ انظر
      `docs/HISTORICAL_AUDITS.md`).
- [ ] **حماية فرع `main`:** Settings ← Branches ← Add rule: اشترط PR، واشترط نجاح فحوص
      `Android Multi-ABI CI`، وفعّل "Do not allow bypassing".
- [ ] قالب PR بسيط (ما تغيّر، كيف تحققت، لقطة شاشة) في `.github/pull_request_template.md`.

## المرحلة 2: استقرار التطبيق (الأهم)
المبدأ: **أعد إنتاج الخطأ ثم أصلحه ثم اكتب اختباراً**.
- [ ] شغّل التطبيق على جهاز حقيقي واجمع الأخطاء: `adb logcat *:E | findstr telegramdrive`
- [ ] لكل خطأ: انسخ stack trace كاملاً، وصنّفه (TDLib/native، مصادقة، رفع، واجهة).
- [ ] أصلح خطأً واحداً في مهمة، مع اختبار انحدار.
- [ ] تحقق من أن حالة المصادقة آلة حالات واحدة وليست أعلاماً متفرقة، وأن أخطاء TDLib تتحول إلى حالات معروفة تعرض رسالة واضحة بدل انهيار التطبيق.
- [ ] جرّب على جهازين مختلفي ABI إن أمكن.

Prompt لكل خطأ:
```
هذا stack trace لخطأ حدث على الجهاز. حلّل السبب الجذري أولاً ولا تعدّل
أي ملف حتى أوافق على الخطة. أريد اختباراً يفشل على الكود الحالي،
ثم أصغر إصلاح ممكن، ثم تشغيل verify-project.ps1.
```

## المرحلة 3: نظام التصميم (Mission Control) — **منجَزة**
- [x] دُمج الوضع الداكن: `AppColors` يحمل لوحة Mission Control، و`AppColorsContrastTest`
      يبوّب كل زوج تباين عند WCAG AA 4.5:1، و`MissionControlTokensTest` يحرس الثوابت.
- [x] قيم Mission Control أُضيفت إلى `AppColors` الحالي — **لا ملف ألوان ثانٍ ولا نظامان
      متوازيان**. لا تُنشئ كائن ألوان موازياً (قاعدة مكتوبة على `main`).
- [x] شاشات Home وQueue وHistory وSettings أُعيد بناؤها (PRs #69–#73).
- [x] قاعدة الانضباط: الليموني للأفعال فقط، البنفسجي والتيل للتوهج والعناوين الصغيرة، لا نص
      طويل بهما.
- [ ] ملاحظة محدَّثة: السمة الآن طقم **Light + Dark** يُختار من Settings ← Appearance، وليست
      "وضعاً داكناً فقط" كما تقول هذه الخطة.

## المرحلة 4: الشاشات (شاشة أو شاشتان في كل مهمة)
اتبع المهام 2 إلى 6 في `CLINE_TASKS.md` بهذا الترتيب:
1. Splash وWelcome وConnect Telegram
2. Home (وأصلح معها عيب `onPrimary` على `primaryContainer` القديم)
3. Choose destination وUpload preparation
4. Upload queue وTransfer detail
5. Destinations وSettings
6. مراجعة نهائية: ابحث عن أي `Color(0x...)` مكتوب مباشرة خارج ملف `AppColors`

لكل شاشة:
- [ ] أرفق لقطة التصميم للمهمة، وابنِ **بدون تغيير الحالة (UiState) ولا ViewModel**.
- [ ] تحقق بصرياً على الجهاز: التباين، الاتجاه RTL مع العربية، النصوص غير المقطوعة، أحجام اللمس 48dp.
- [ ] اختبر مع "إزالة الحركات" مفعّلة في النظام.

## المرحلة 5: الشعار والأيقونة — **منجَزة**
- [x] أيقونة تكيفية على `main`: `app/src/main/res/drawable/ic_launcher_{background,foreground,monochrome}.xml`
      مع `mipmap-*/ic_launcher*.webp`، والرمز داخل المنطقة الآمنة.
- [x] الأصل المعياري للشعار: `feature/src/main/res/drawable-nodpi/mission_control_logo.png`
      (يبقى PNG للمعاينة والواجهة).
- [x] معاينات الشاشات والأيقونة في `docs/previews/`.

## المرحلة 6: الجودة والإصدار
- [ ] `.\scripts\verify-project.ps1 -Mode FULL` ينجح.
- [ ] تجربة كاملة على جهاز: تسجيل الدخول، اختيار وجهة، رفع ملف كبير، إيقاف الشبكة أثناء الرفع واستئنافه، إعادة تشغيل الجهاز.
- [ ] لا تعتمد على أرقام تغطية قديمة (AGENTS.md: التغطية الحالية تقاس لـ `:feature` فقط).
- [ ] أنشئ tag واحداً (`v1.x.y`) بعد نجاح CI. إصدار موقّع جديد يحتاج موافقتك الصريحة (قاعدة STOP-AND-ASK).

## جدول سريع لحل المشاكل
| العَرَض | الإجراء |
|---|---|
| `Illegal Capacity` عند الاختبار | شغّل `verify-project.ps1` (يمسح نتائج الاختبار المخزنة قبل التشغيل) |
| `main` لا يُترجم بعد دمج | `git revert` للـ commit المسبب، ثم أصلح في فرع، ولا تصلح مباشرة على `main` |
| Cline يعدّل ملفات كثيرة | أوقفه، `git restore .` أو Restore من Checkpoints، وأعد المهمة بنطاق أصغر |
| Cline يعطي معلومات غريبة أو نصوصاً مختلطة | محادثة طويلة ومشوشة: افتح New Task |
| التطبيق يغلق عند الاتصال | تحقق من ABI المناسب للجهاز وقواعد R8 وlogcat (راجع جدول README) |
| اللون لا يطابق التصميم | لا لون مباشر في الشاشة: كل اللون من `AppColors` |
