# تطبيق تصميم Mission Control في التطبيق عبر Cline

## قبل البدء
1. **أرفق صوراً لا روابط.** رابط اللوحة خاص ولا يقرؤه Cline. التقط لقطة لكل شاشة من اللوحة (أو استخدم صورتك المرجعية) وأرفقها بكل مهمة.
2. **لا طبقة ألوان ثانية.** القيم الجديدة تُضاف إلى `AppColors` الحالي (المرتبط بـ `DarkColorScheme`) وليس في ملف أو كائن منفصل.
3. فرع جديد لكل مهمة، و**New Task** لكل مهمة، وابدأ بـ Plan mode.
4. بعد كل مهمة: `.\scripts\verify-project.ps1 -Mode QUICK`، ثم ثبّت التطبيق وقارن مع اللقطة، ثم commit.

## إضافة إلى `.clinerules`
```
- التصميم المرجعي: Mission Control، وضع داكن فقط.
- كل الألوان من AppColors فقط، ولا تضع لوناً مباشراً في أي شاشة.
- الليموني (AppColors.Lime) للأزرار والتقدم والتبويب النشط فقط.
- لا تستخدم Modifier.blur. التوهج بـ Brush.radialGradient داخل drawBehind.
- كل نص يُرسم بأحد الزوجين: OnSurface/OnSurfaceMuted على الخلفية والزجاج، OnLime فوق الليموني.
- كل عنصر قابل للضغط له contentDescription أو نص، وحجمه 48dp على الأقل.
- لا تضف مكتبات دون سؤالي (بما فيها خطوط Google Fonts).
```

## المهمة 1: إضافة القيم والمكونات
```
أضف إلى AppColors الحالي (لا ملف ولا كائن جديد للألوان) هذه القيم:
Background=#0E110A، GridLine=أبيض بشفافية 3%، GlassFill=أبيض 5.5%،
GlassBorder=أبيض 10%، OnSurface=#F2F5EC، OnSurfaceMuted=#AEB6A4،
Lime=#A3E636، LimeLight=#D9F79A، OnLime=#11160A، Teal=#39D8C2،
Purple=#863FFD، OnPurple=أبيض، Error=#FF7B7B.
اعتمد على أسماء الأدوار الموجودة حيث تتطابق، ولا تكرر لوناً موجوداً.
أضف أزواجها إلى AppColorsContrastTest القائم بتباين WCAG 4.5:1:
OnSurface/Background، OnSurfaceMuted/Background، OnLime/Lime،
OnPurple/Purple، Teal/Background، Error/Background.
ثم أضف في وحدة core مكونات: MissionBackground (شبكة خافتة وتوهج بنفسجي
متحرك بـ Brush.radialGradient داخل drawBehind)، GlassCard، Eyebrow، LimeButton،
ShimmerProgressBar، PulseFab، SettingToggle، باستخدام AppColors فقط ودون blur.
(مرجع إن توفر: MissionComponents.kt المرفق، وإلا ابنها من الوصف.)
شغّل ./gradlew :core:compileDebugKotlin وأخبرني بأي أخطاء قبل إصلاحها.
اعرض الخطة والملفات المتأثرة أولاً ولا تنفذ قبل موافقتي.
```

## المهمة 2: Splash ثم Welcome ثم Connect Telegram
أرفق لقطات الشاشات الثلاث، ثم:
```
ابنِ الشاشات الثلاث لتطابق اللقطات، داخل MissionBackground، باستخدام
GlassCard وLimeButton وEyebrow من مكونات Mission في core. لا تلمس شاشات أخرى
ولا منطق المصادقة. Splash: حلقة ليمونية تدور (rememberInfiniteTransition)
حول أيقونة Telegram على مربع بنفسجي. اعرض الخطة قبل التنفيذ.
```

## المهمة 3: Home
```
أعد بناء HomeScreen لتطابق اللقطة. استخدم PulseFab للزر العائم،
وShimmerProgressBar لبطاقة الرفع النشط. شريط التنقل السفلي بسطح زجاجي
والتبويب النشط ليموني. لا تغيّر ViewModel ولا مصدر البيانات.
اعرض الخطة قبل التنفيذ.
```

## المهمة 4: Choose destination + Upload preparation
```
ابنِ الشاشتين لتطابق اللقطتين. الصف المحدد بحد ليموني وعلامة صح.
مفاتيح التحضير باستخدام SettingToggle. حافظ على منطق الاختيار الحالي.
```

## المهمة 5: Upload queue + Transfer detail
```
ابنِ الشاشتين. حلقة النسبة في Transfer detail بـ Canvas (drawArc) مع نبض خفيف.
حالة الفشل: بطاقة بحد AppColors.Error وزر Retry ثانوي. لا تغيّر WorkManager.
```

## المهمة 6: Destinations + Settings
```
ابنِ الشاشتين، وSettings بمفاتيح SettingToggle. اربط المفاتيح بالإعدادات
الموجودة فقط ولا تضف إعدادات جديدة.
```

## المهمة 7: المراجعة النهائية
```
ابحث في المشروع عن أي لون مكتوب مباشرة (Color(0x...)) خارج ملف AppColors،
وعن أي استخدام قديم لـ AppColors في الشاشات المحدّثة. اعرض القائمة فقط.
```

## ملاحظات
- كود المكونات **لم يُترجم ولم يُشغَّل** عند كتابته. توقع أخطاء استيراد أو إصدار في أول ترجمة، وهذا سبب المهمة 1 المستقلة.
- التوهج الملون للزر (`shadow` بلون) يعمل على Android 9 وما فوق، وفي الأقدم يظهر ظل عادي.
- إن أردت خط Space Grotesk للعناوين، فهو يحتاج مكتبة `ui-text-google-fonts` أو ملفات خط في المشروع، وهذا قرارك. يعمل التطبيق بالخط الافتراضي بدونه.
- الحركات تحترم إعداد النظام "إزالة الحركات" في أغلب الأجهزة، وتحقق من ذلك على جهازك.
