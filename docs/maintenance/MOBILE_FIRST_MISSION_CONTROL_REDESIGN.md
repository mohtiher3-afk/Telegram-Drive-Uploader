# Mobile-first Mission Control Redesign

## Evidence and finding

The supplied phone screenshots show a shared composition issue rather than an upload-state defect. Home contains multiple competing glass surfaces; Queue and History render an oversized empty-state slab; the compact NavigationBar appears as a second large floating card; and TelegramAuth vertically centers an error state, creating an extended dead zone above it. The functional data in each screenshot remains truthful: disconnected Telegram, no active uploads, empty queue/history, and unconfigured API credentials.

## Scope and protected behavior

This redesign is constrained to Compose presentation, localized copy only if required, shared layout tokens, semantic tests, and visual documentation. It preserves `AppNavigation` routes/back stack, all existing test tags, Home callbacks, TelegramAuth retry/auth callbacks, ViewModels, TDLib client ownership, API credential truth, Room/DataStore, WorkManager, upload identity, true progress/terminal state, JNI artifacts, ABIs, signing, and release gates.

## Functional translation

The compact phone layout will use one dominant upload surface, a compact connection row, reduced nested card weight, compact empty/error surfaces, and a lower-noise NavigationBar. Decorative Aurora stays behind content. The navigation indicator, upload action, actual connection state, and semantic error roles remain the sole highlighted signals. Empty data does not become a fictional dashboard state.

## Risk, validation, and reversal

The risk is accidental loss of usable touch area, truncation, semantics, routes, or real callbacks when tightening layout. Validation will cover focused tests, Kotlin compile, unit tests, Arabic/English parity, diff and secret checks, plus available visual review. Device, TalkBack, large-font, RTL, reduced-motion, real authorization, and real upload outcomes remain **NOT VERIFIED** until observed. Reversal is limited to the presentation commits touching the files listed in the plan.

## Implemented mobile changes

The compact NavigationBar now has a smaller visual footprint and no longer adds a glow rim around every selected destination. `EmptyState` and `ErrorState` use a medium surface, a 40dp icon, and compact spacing. Home now uses a tighter page rhythm, one simplified Aurora hero, a compact connection row, neutral statistic surfaces, and a lightweight no-active-uploads row. Queue and History share phone-edge spacing, smaller list gaps, quiet filter chips, and compact empty-state placement. TelegramAuth starts its content at the top, reduces the login mark, and groups a terminal error with its truthful explanation while keeping the existing retry and QR-recovery callbacks.

The only implementation correction required by validation was replacing `WindowInsets.safeDrawing`, which is unavailable in this Compose version, with the project-supported inset construction. Kotlin compile and all debug unit tests then passed. `git diff --check` passed and Gradle was stopped. No new strings were added, so Arabic/English resource parity is unchanged.

No phone/emulator screenshot, actual TalkBack traversal, large-font/RTL observation, reduced-motion observation, Telegram authorization, or real upload was available for this iteration. The screenshots provided by the user remain the only device evidence and showed the pre-redesign UI.

## File-level record

| الملف | التعديل المحدد | ما لم يتغير |
|---|---|---|
| `feature/telegram/TelegramAuthScreen.kt` | المحتوى أصبح يبدأ من أعلى الشاشة عبر `Alignment.TopCenter`؛ أضيفت حواف `phoneEdge`/`phoneSection`؛ تقلص `TelegramLogo` من 100dp إلى 72dp؛ وأصبحت حالة `ERROR` بطاقة `errorContainer` مدمجة فيها `ErrorOutline` ونص الخطأ ووسم `error_text`. | `TelegramAuthViewModel`، حالة TDLib، رسالة الخطأ الحقيقية، `connect()`، استرداد QR، وزر `retry_connect_button` بقيت كما هي. |
| `core/ui/components/EmptyState.kt` | السطح تحوّل من `large` إلى `medium`، الأيقونة من 64dp إلى 40dp، والمسافات والعنوان أصبحت compact. | العنوان والوصف والفعل الاختياري والدلالات نفسها. |
| `core/ui/components/ErrorState.kt` | نفس تخفيض كثافة EmptyState: surface متوسط، أيقونة 40dp، عنوان `titleMedium` ومسافات compact. | رسالة الخطأ وزر المحاولة ووصف الأيقونة كما هي. |
| `core/navigation/AppNavigation.kt` | شريط الهاتف أصبح بارتفاع 72dp مع inset عمودي 4dp؛ أزيل rim المتكرر من عناصر التنقل، وبقي مؤشر Material3 المختار وحده. | routes، back stack، `nav_tab_*` test tags، الأيقونات والنصوص وأفعال التنقل. |
| `feature/home/HomeScreen.kt` | العنوان والمسافات أصغر؛ بطل الرفع صار هالة Aurora واحدة خلف المحتوى، وأيقونته 48dp؛ الاتصال والإحصاءات وحالة «لا رفع نشط» أصبحت أسطحًا مدمجة. | `onSelectVideos` و`onConnectClick` وحالات الاتصال والعدادات وبيانات الرفع الفعلية. |
| `feature/queue/QueueScreen.kt` و`feature/history/HistoryScreen.kt` | استعملت حواف الهاتف ومسافات قائمة أصغر؛ أزيلت rims شرائح الفلترة؛ الحالة الفارغة compact. | الفلاتر، ترتيب المهام والسجل، المصدر البياناتي وtest tags. |
| `core/ui/theme/DesignTokens.kt` | أضيفت `phoneEdge = 16.dp` و`phoneSection = 20.dp` و`phoneNavInset = 4.dp`. | `touchTarget = 48.dp` وعقود المسافة القائمة. |
| `values/strings.xml` و`values-ar/strings.xml` | **لا تغيير** في هذه الجولة؛ لم تضف نصوص ثابتة أو رسائل اعتماد جديدة، لذلك لا يوجد أثر جديد على الترجمة. | تكافؤ العربية والإنجليزية القائم. |
