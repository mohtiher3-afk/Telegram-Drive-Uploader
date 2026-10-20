import io

path = "feature/src/test/java/com/telegramdrive/uploader/feature/screens/ScreenThemeProofTest.kt"
with open(path, "r", encoding="utf-8") as f:
    content = f.read()

# Find the test methods block (lines 78-198)
# The block starts with "    @Test\n    fun `queue renders dark`() {"
# and ends with "    }\n\n    private fun record(fileName: String) {"
old_test_block = """    @Test
    fun `queue renders dark`() {
        compose(true) {
            QueueScreen(
                viewModel = QueueViewModel(
                    fakeQueueRepository(tasks = emptyList()),
                    FakeUploadManager(),
                    OwnedStagedFileStore(ApplicationProvider.getApplicationContext())
                )
            )
        }
        record("theme-proof/queue_dark.png")
    }

    @Test
    fun `queue renders light`() {
        compose(false) {
            QueueScreen(
                viewModel = QueueViewModel(
                    fakeQueueRepository(tasks = emptyList()),
                    FakeUploadManager(),
                    OwnedStagedFileStore(ApplicationProvider.getApplicationContext())
                )
            )
        }
        record("theme-proof/queue_light.png")
    }

    @Test
    fun `history renders dark`() {
        compose(true) {
            HistoryScreen(
                viewModel = HistoryViewModel(fakeUploadRepository())
            )
        }
        record("theme-proof/history_dark.png")
    }

    @Test
    fun `history renders light`() {
        compose(false) {
            HistoryScreen(
                viewModel = HistoryViewModel(fakeUploadRepository())
            )
        }
        record("theme-proof/history_light.png")
    }

    @Test
    fun `settings renders dark`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        compose(true) {
            SettingsScreen(
                onConnectClick = {},
                viewModel = SettingsViewModel(
                    settingsDataStore = SettingsDataStore(context),
                    telegramRepository = FakeTelegramRepository(),
                    context = context
                )
            )
        }
        record("theme-proof/settings_dark.png")
    }

    @Test
    fun `settings renders light`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        compose(false) {
            SettingsScreen(
                onConnectClick = {},
                viewModel = SettingsViewModel(
                    settingsDataStore = SettingsDataStore(context),
                    telegramRepository = FakeTelegramRepository(),
                    context = context
                )
            )
        }
        record("theme-proof/settings_light.png")
    }

    @Test
    fun `upload renders dark`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        compose(true) {
            UploadScreen(
                onBackClick = {},
                onSelectDestination = {},
                onQueueAdded = {},
                viewModel = UploadViewModel(
                    uploadRepository = fakeUploadRepository(),
                    telegramRepository = FakeTelegramRepository(),
                    uploadManager = FakeUploadManager(),
                    settingsDataStore = SettingsDataStore(context),
                    ownedStagedFileStore = OwnedStagedFileStore(context),
                    context = context
                )
            )
        }
        record("theme-proof/upload_dark.png")
    }

    @Test
    fun `upload renders light`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        compose(false) {
            UploadScreen(
                onBackClick = {},
                onSelectDestination = {},
                onQueueAdded = {},
                viewModel = UploadViewModel(
                    uploadRepository = fakeUploadRepository(),
                    telegramRepository = FakeTelegramRepository(),
                    uploadManager = FakeUploadManager(),
                    settingsDataStore = SettingsDataStore(context),
                    ownedStagedFileStore = OwnedStagedFileStore(context),
                    context = context
                )
            )
        }
        record("theme-proof/upload_light.png")
    }

"""

new_test_block = """    @Test
    fun `queue renders dark vs light`() {
        compose(true) {
            QueueScreen(
                viewModel = QueueViewModel(
                    fakeQueueRepository(tasks = emptyList()),
                    FakeUploadManager(),
                    OwnedStagedFileStore(ApplicationProvider.getApplicationContext())
                )
            )
        }
        record("theme-proof/queue_dark.png")
        compose(false) {
            QueueScreen(
                viewModel = QueueViewModel(
                    fakeQueueRepository(tasks = emptyList()),
                    FakeUploadManager(),
                    OwnedStagedFileStore(ApplicationProvider.getApplicationContext())
                )
            )
        }
        record("theme-proof/queue_light.png")
        assertDifferences("queue", "queue_dark", "queue_light")
    }

    @Test
    fun `history renders dark vs light`() {
        compose(true) {
            HistoryScreen(
                viewModel = HistoryViewModel(fakeUploadRepository())
            )
        }
        record("theme-proof/history_dark.png")
        compose(false) {
            HistoryScreen(
                viewModel = HistoryViewModel(fakeUploadRepository())
            )
        }
        record("theme-proof/history_light.png")
        assertDifferences("history", "history_dark", "history_light")
    }

    @Test
    fun `settings renders dark vs light`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        compose(true) {
            SettingsScreen(
                onConnectClick = {},
                viewModel = SettingsViewModel(
                    settingsDataStore = SettingsDataStore(context),
                    telegramRepository = FakeTelegramRepository(),
                    context = context
                )
            )
        }
        record("theme-proof/settings_dark.png")
        compose(false) {
            SettingsScreen(
                onConnectClick = {},
                viewModel = SettingsViewModel(
                    settingsDataStore = SettingsDataStore(context),
                    telegramRepository = FakeTelegramRepository(),
                    context = context
                )
            )
        }
        record("theme-proof/settings_light.png")
        assertDifferences("settings", "settings_dark", "settings_light")
    }

    @Test
    fun `upload renders dark vs light`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        compose(true) {
            UploadScreen(
                onBackClick = {},
                onSelectDestination = {},
                onQueueAdded = {},
                viewModel = UploadViewModel(
                    uploadRepository = fakeUploadRepository(),
                    telegramRepository = FakeTelegramRepository(),
                    uploadManager = FakeUploadManager(),
                    settingsDataStore = SettingsDataStore(context),
                    ownedStagedFileStore = OwnedStagedFileStore(context),
                    context = context
                )
            )
        }
        record("theme-proof/upload_dark.png")
        compose(false) {
            UploadScreen(
                onBackClick = {},
                onSelectDestination = {},
                onQueueAdded = {},
                viewModel = UploadViewModel(
                    uploadRepository = fakeUploadRepository(),
                    telegramRepository = FakeTelegramRepository(),
                    uploadManager = FakeUploadManager(),
                    settingsDataStore = SettingsDataStore(context),
                    ownedStagedFileStore = OwnedStagedFileStore(context),
                    context = context
                )
            )
        }
        record("theme-proof/upload_light.png")
        assertDifferences("upload", "upload_dark", "upload_light")
    }

"""

if old_test_block in content:
    content = content.replace(old_test_block, new_test_block)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print("Replacement successful")
else:
    print("OLD TEST BLOCK NOT FOUND")
    # Show what's in the file around that area
    lines = content.split("\n")
    for i, line in enumerate(lines[77:198], start=78):
        print(f"{i}: {line[:100]}")
