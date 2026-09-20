# Repository image replacement audit

The legacy launcher foreground image at `app/src/main/res/drawable-nodpi/ic_launcher_foreground_image.png` is the older red/coral upload-tray icon and is no longer referenced by the current launcher foreground or onboarding after the Mission Control branding update.

The legacy JPG at `app/src/main/res/drawable/ic_tg_drive_uploader_1786926729865.jpg` is a blue Telegram Drive Uploader promotional-style image containing a rendered wordmark. It is a stale repository asset and is not referenced by the current Android source paths inspected.

The current replacement is `app/src/main/res/drawable-nodpi/mission_control_logo.png`, the updated Mission Control orbital upload logo. The cleanup was completed: the stale `ic_launcher_foreground_image.png` and `ic_tg_drive_uploader_1786926729865.jpg` assets were removed, the launcher density files were regenerated from the Mission Control logo, and repository references were verified.
