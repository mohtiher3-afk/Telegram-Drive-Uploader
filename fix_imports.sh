#!/bin/bash
BASE="app/src/main/java/com/telegramdrive/uploader"

# Fix package names in all files
find $BASE -type f -name "*.kt" -exec sed -i 's/package com.telegramdrive.uploader.core.database/package com.telegramdrive.uploader.data.local/g' {} +
find $BASE -type f -name "*.kt" -exec sed -i 's/package com.telegramdrive.uploader.core.model/package com.telegramdrive.uploader.domain.model/g' {} +
find $BASE -type f -name "*.kt" -exec sed -i 's/package com.telegramdrive.uploader.core.repository/package com.telegramdrive.uploader.data.repository/g' {} +
find $BASE -type f -name "*.kt" -exec sed -i 's/package com.telegramdrive.uploader.ui.theme/package com.telegramdrive.uploader.core.ui.theme/g' {} +
find $BASE -type f -name "*.kt" -exec sed -i 's/package com.telegramdrive.uploader.ui.components/package com.telegramdrive.uploader.core.ui.components/g' {} +
find $BASE -type f -name "*.kt" -exec sed -i 's/package com.telegramdrive.uploader.ui.navigation/package com.telegramdrive.uploader.core.navigation/g' {} +
find $BASE/feature/home -type f -name "*.kt" -exec sed -i 's/package com.telegramdrive.uploader.ui.screens/package com.telegramdrive.uploader.feature.home/g' {} +
find $BASE/feature/queue -type f -name "*.kt" -exec sed -i 's/package com.telegramdrive.uploader.ui.screens/package com.telegramdrive.uploader.feature.queue/g' {} +
find $BASE/feature/history -type f -name "*.kt" -exec sed -i 's/package com.telegramdrive.uploader.ui.screens/package com.telegramdrive.uploader.feature.history/g' {} +
find $BASE/feature/settings -type f -name "*.kt" -exec sed -i 's/package com.telegramdrive.uploader.ui.screens/package com.telegramdrive.uploader.feature.settings/g' {} +
find $BASE/feature/upload -type f -name "*.kt" -exec sed -i 's/package com.telegramdrive.uploader.ui.screens/package com.telegramdrive.uploader.feature.upload/g' {} +

# Fix imports in all files
find $BASE -type f -name "*.kt" -exec sed -i 's/import com.telegramdrive.uploader.core.database/import com.telegramdrive.uploader.data.local/g' {} +
find $BASE -type f -name "*.kt" -exec sed -i 's/import com.telegramdrive.uploader.core.model/import com.telegramdrive.uploader.domain.model/g' {} +
find $BASE -type f -name "*.kt" -exec sed -i 's/import com.telegramdrive.uploader.core.repository/import com.telegramdrive.uploader.data.repository/g' {} +
find $BASE -type f -name "*.kt" -exec sed -i 's/import com.telegramdrive.uploader.ui.theme/import com.telegramdrive.uploader.core.ui.theme/g' {} +
find $BASE -type f -name "*.kt" -exec sed -i 's/import com.telegramdrive.uploader.ui.components/import com.telegramdrive.uploader.core.ui.components/g' {} +
find $BASE -type f -name "*.kt" -exec sed -i 's/import com.telegramdrive.uploader.ui.navigation/import com.telegramdrive.uploader.core.navigation/g' {} +
find $BASE -type f -name "*.kt" -exec sed -i 's/import com.telegramdrive.uploader.ui.screens.HomeScreen/import com.telegramdrive.uploader.feature.home.HomeScreen/g' {} +
find $BASE -type f -name "*.kt" -exec sed -i 's/import com.telegramdrive.uploader.ui.screens.QueueScreen/import com.telegramdrive.uploader.feature.queue.QueueScreen/g' {} +
find $BASE -type f -name "*.kt" -exec sed -i 's/import com.telegramdrive.uploader.ui.screens.HistoryScreen/import com.telegramdrive.uploader.feature.history.HistoryScreen/g' {} +
find $BASE -type f -name "*.kt" -exec sed -i 's/import com.telegramdrive.uploader.ui.screens.SettingsScreen/import com.telegramdrive.uploader.feature.settings.SettingsScreen/g' {} +
find $BASE -type f -name "*.kt" -exec sed -i 's/import com.telegramdrive.uploader.ui.screens.DestinationScreen/import com.telegramdrive.uploader.feature.settings.DestinationScreen/g' {} +
find $BASE -type f -name "*.kt" -exec sed -i 's/import com.telegramdrive.uploader.ui.screens.UploadDetailsScreen/import com.telegramdrive.uploader.feature.upload.UploadDetailsScreen/g' {} +

# Remove any imports of HighSpeedUploadEngine
find $BASE -type f -name "*.kt" -exec sed -i '/import com.telegramdrive.uploader.core.engine/d' {} +
