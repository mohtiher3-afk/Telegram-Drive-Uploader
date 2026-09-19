# CURRENT_TASK — تتبع المرحلة (آخر تحديث: 2026-09-19)

## الوضع الحالي
- **Phase 07 قيد الكتابة**: 3 اختبارات انحدار مجهزة (Fail-then-Pass) على الجهاز الحقيقي.
- الجهاز **مرتبط ومصرَّح**: `BUFYHQZXR4BQK7WK` (23090RA98G / Redmi Note 13 Pro+ 5G).

## المنجز (موثَّق بالبايت — `PHASE07_EVIDENCE.md`)
- `ChannelSearchSeparatorRegressionTest.kt` (3914B) — Phase 04/06: سيم البحث `TelegramDestinationPolicy.matchesSearch` نقي على الجهاز.
- `UploadChainRegressionTest.kt` (8049B) — Phase 02/05: سلسلة الإرسال تصل العضو الحقيقي `UploadStatus.COMPLETED` (لا `SUCCEEDED` — لا وجود له).
- `Phase07RegressionTest.kt` (8970B) — Fail-then-Pass مركّب.

## العائق الحقيقي (لا يُتجاهل)
- **ترجمة androidTest لم تُكتمل**: انهيار Gradle daemon بذاكرة (`mmap … paging file is too small`؛ `hs_err_pid8700.log`).
- دون ترجمة ناجحة لا يُنفَّذ fail-then-pass على الجهاز. **لا يُدَّعى نجاح تشغيلي.**

## الخطوة التالية (تتطلب قرارك)
1. رفع page file أو خفض ذاكرة daemon (`-Xmx512m/--max-workers=1`) ثم إعادة الترجمة.
2. بعد نجاحها: تشغيل `adb shell am instrument -w -e class …` على `BUFYHQZXR4BQK7WK` + توثيق fail-then-pass.
3. **لا commit/دفع قبل "نعم" الصريحة.**
