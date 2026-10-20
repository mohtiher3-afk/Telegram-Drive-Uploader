# TDLib version decision analysis

Read-only investigation. **`TdApi.java` was not edited, no native library was
modified, and nothing was rebuilt.** The procedure in section 7 is written down but
**not executed**.

## 1. Verdict

**Pin all four artifacts to commit `022d60202e446ad1287b9fb68e687c8a0760788b`
(TDLib v1.8.66).**

That is the only commit the repository already pins, it is the version `TdApi.java`
actually matches, and it is the version the two live ABIs disagree with. Rebuilding
arm64-v8a and x86_64 from it costs nothing in application code, because the app
references none of the 41 schema classes that exist only in the newer build.

## 2. Git history: where each artifact came from

`git log --follow` for each file (abridged to the commits that changed bytes):

| Commit | Message | What it did |
|---|---|---|
| `0bc39f2` | build: initialize project structure and build config | added the original set |
| `80b62c2` | feat: publish TDLib 1.8.66 Android uploader | published 1.8.66 artifacts |
| `26d25a0` | refactor: extract data layer into :data Gradle module | moved `TdApi.java` and the `.so` files from `app/` to `data/` |
| `05c0f01` | chore: upgrade TDLib to v1.8.67 (native rebuild) | **rebuilt `app/.../arm64-v8a` (→ 20,640,912 B) and `armeabi-v7a` (→ 14,576,768 B) and rewrote `TdApi.java`** |
| `512f288` | chore: rebuild TDLib v1.8.66 with 16 KB ELF page alignment (Phase 03) | rebuilt all three `data/` ABIs for 16 KB alignment + regenerated `TdApi.java` |
| `eb5e4b9` | Merge tdlib/v1.8.67 into main: TDLib v1.8.67 native rebuild | **added the `app/arm64-v8a` set and overwrote `data/arm64-v8a` (→ 22,825,208 B), `data/x86_64` (→ 14,576,768 B)** |

Read that last row carefully: the merge `eb5e4b9` — titled "TDLib v1.8.67 native
rebuild" — is what left `data/arm64-v8a/libtdjni.so` at 22,825,208 bytes. That is the
file measured in Phase 1/2 as **x86-64 machine code** and later relocated into
`x86_64/`, where it belongs. It also overwrote `data/x86_64/libtdjni.so` with a
14,576,768-byte **ARM** binary — the stray that has now been moved out of the repo.

So the 1.8.67 merge wrote the right bytes into the wrong ABI directories, and a
genuine arm64 1.8.66 build (`2f58d26c…`, 20,640,912 B) survived untouched in
`app/`, which is why Phase 2 could repair the placement without rebuilding anything.

**Provenance of each shipped file today** (all verified by measurement, see
`TDLIB_VERSION_CHECK.md`):

| Path | Version | Built from |
|---|---|---|
| `data/src/main/jniLibs/arm64-v8a/libtdjni.so` | 1.8.67 | `td-master` |
| `data/src/main/jniLibs/armeabi-v7a/libtdjni.so` | 1.8.66 | `022d6020…` |
| `data/src/main/jniLibs/x86_64/libtdjni.so` | 1.8.67 | `td-master` |
| `data/src/main/java/org/drinkless/tdlib/TdApi.java` | **1.8.66** | `022d6020…` |

## 3. Which version `TdApi.java` is — schema comparison

`TdApi.java` carries no version header and no generation stamp, so its version was
established by comparing it against the upstream TL schema at both candidate
commits, fetched over HTTPS from `raw.githubusercontent.com`:

```
td_api_022d602.tl   (v1.8.66)  1,138,081 bytes
td_api_master.tl    (master)   1,191,756 bytes
```

Method: for each class, collect the field set from the `.tl` constructor line and
from the Java `public` fields of the matching `public static class`, normalise
`snake_case`/`camelCase`, and compare. 464 classes were comparable across all three.

Results:

| Test | Result |
|---|---|
| Classes where Java fields == 1.8.66 exactly (and != master) | **2** |
| Classes where Java fields == master exactly (and != 1.8.66) | **0** |
| Classes identical in both schemas | 224 |
| **master-only classes present in `TdApi.java`** | **0 of 41** |

The two discriminating classes prove it — master *added* fields, and Java does not
have them:

```
ChatAdministratorRights
  1.8.66 : ... canRestrictMembers, isAnonymous
  master : ... canRestrictMembers, canSendWelcomeMessages, isAnonymous   <-- added
  java   : ... canRestrictMembers, isAnonymous                           <-- matches 1.8.66

ThemeSettings
  1.8.66 : accentColor, ..., outgoingMessageFill
  master : accentColor, ..., hasOutgoingMessageAccentColor, outgoingMessageFill   <-- added
  java   : accentColor, ..., outgoingMessageFill                        <-- matches 1.8.66
```

The 41 master-only classes are all absent from `TdApi.java`:

```
CommunityChat, CommunityFullInfo, CommunityId, Currencies, CurrencyExchangeRate,
CurrencyExchangeRates, EphemeralMessageContent, InlineButton, OnRamp*(7),
TonCenter*(3), TonConnect*(11), TonNft*(2), TonWallet*(13), UserTonWalletAddress,
WalletBotBalance, WelcomeMessage
```

Conversely, zero classes were removed between 1.8.66 and master, so 1.8.66 is a clean
subset — nothing in `TdApi.java` is "too new" for either binary.

**Conclusion: `TdApi.java` is generated from v1.8.66.** It matches the armeabi-v7a
library and **does not** match the arm64-v8a / x86_64 libraries.

## 4. How the artifacts are meant to be produced

**`scripts/build-tdlib-android.sh`** is the only producer, and it generates *both*
the libraries and the Java bindings from one source checkout:

- Stage 0 (line 20): `TDLIB_REF="${TDLIB_REF:-master}"` — **the default is
  `master`**, an unpinned moving branch. The script's own header documents the
  alternative: `TDLIB_REF=<full-sha>` to pin an exact commit.
- Stage 0 (line 53): derives the version from upstream `CMakeLists.txt`, because
  TDLib publishes no per-patch tags, and **aborts if the requested
  `TDLIB_VERSION` disagrees with the source** (line 58) — so it cannot be mislabelled.
- Stage 2 (lines 79–101): runs the `tl_generate_java` CMake target and copies
  `TdApi.java` and `Client.java` out of the source tree. **`TdApi.java` is generated,
  never hand-edited.**
- Stage 3 (lines 104–139): cross-compiles `libtdjni.so` per ABI into
  `data/src/main/jniLibs/<abi>/`.
- Stage 6 (line 165+): regenerates `docs/TDLIB_SHA256SUMS.txt`.

**`.github/workflows/tdlib-upgrade.yml`** wraps exactly that script:

- inputs: `ref` (default `master`) and `expected_version` (optional assert).
- installs NDK `26.3.11579264` + CMake `3.22.1` — the same pins the manifest lists.
- runs `build-openssl-android.sh` with `FORCE_ALL_ABIS=1`, then
  `build-tdlib-android.sh`, then `check-tdlib-artifacts.sh` with
  `TDLIB_CHECK_ABI: all`.
- commits `data/src/main/jniLibs`, `TdApi.java`, `Client.java`,
  `docs/TDLIB_ARTIFACT_MANIFEST.md`, `docs/TDLIB_SHA256SUMS.txt` and opens a PR.

**`android-ci.yml` / `android-release.yml` do not rebuild TDLib.** They only run
`build-openssl-android.sh` (the OpenSSL dependencies) and consume the checked-in
binaries. So whatever the upgrade workflow commits is what ships, and CI cannot
detect a library/bindings mismatch — which is precisely why the current split
survived.

**Inputs pinned today:** NDK `26.3.11579264`, CMake `3.22.1`, OpenSSL `3.0.16`,
Zlib `1.3.1`, and the manifest's `Pinned source commit 022d6020…`. The *build
script's* default ref is **not** pinned — that is the single defect that produced the
1.8.67 binaries.

## 5. What happens at runtime if native and Java disagree

Based only on how this repo calls TDLib. The JNI boundary is
`org.drinkless.tdlib.TdApi` → `libtdjni.so`; every object is serialised by TDLib's TL
codec, and each class has a numeric `CONSTRUCTOR` id that must match on both sides.

The app calls TDLib through `data/src/main/java/com/telegramdrive/uploader/data/telegram/client/TelegramClientImpl.kt`
and `org/drinkless/tdlib/Client.java`. It uses the ordinary core surface —
`AuthorizationState*`, `SetTdlibParameters`, `CheckAuthenticationCode`,
`CheckAuthenticationPassword`, `GetMe`, `GetChats`, `GetChatHistory`, `GetChat`,
`GetSupergroup`, `SendMessage`, `InputMessageDocument`, `InputFileLocal`,
`InputFileId`, `File`, `Error`, `Close`.

Concretely, without speculating:

- **A constructor id present in `TdApi.java` but unknown to the library** →
  the native deserialiser rejects it. In TDLib that surfaces as an error object on
  the response, and on the Java side as a failed `receive()`/`execute()` rather than
  a hard crash at load time.
- **A constructor id present in the library but absent from `TdApi.java`** → TDLib
  emits a Java class `TdApi.java` cannot name; the generated decoder path cannot
  construct it. This is the dangerous direction and it is the one the repo is
  currently in: the 1.8.67 libraries emit ids for 41 classes `TdApi.java` has never
  heard of (`TonWalletState`, `CommunityChat`, `WelcomeMessage`, …). If any of those
  arrives in an update stream, the client cannot decode it.

Verified: **the app references none of those 41 classes** (`TdApi.TonWalletState`,
`TdApi.CommunityChat`, `TdApi.WelcomeMessage`, `TdApi.InlineButton`, … all have zero
call sites). So today the mismatch is latent — it bites only when a Telegram update
introduces one of the newer objects, and then it bites on arm64 and x86_64 while
armeabi-v7a keeps working. That is a nondeterministic, ABI-dependent production
fault, which is worse than a deterministic one.

## 6. Recommendation

**Pin everything to `022d60202e446ad1287b9fb68e687c8a0760788b` (v1.8.66).**

Evidence:

1. It is the commit `docs/TDLIB_ARTIFACT_MANIFEST.md` already pins, and 16 places in
   the docs (plus `TDLIB_VERSION` in the gate) say 1.8.66.
2. `TdApi.java` matches it exactly — 0 of 41 master-only classes present, and both
   discriminating classes (`ChatAdministratorRights`, `ThemeSettings`) carry the
   1.8.66 field set.
3. `armeabi-v7a/libtdjni.so` is already built from it, so that ABI needs no work.
4. Pinning to 1.8.67 instead would require **regenerating `TdApi.java`**, which is
   forbidden in this task and is a schema-visible change to the Java surface; 1.8.66
   requires only two binary rebuilds and no source change.
5. The app uses none of the 41 newer classes, so downgrading arm64/x86_64 to 1.8.66
   removes no functionality the app depends on.

Also fix the root cause regardless of which version is chosen: **pass an explicit
`TDLIB_REF` (a full SHA) in `tdlib-upgrade.yml`** instead of letting the workflow's
`master` default flow into `build-tdlib-android.sh`. The script header already
documents `TDLIB_REF=<full-sha>`; the workflow simply never uses it. Consider
defaulting `TDLIB_REF` in the script to the pinned manifest commit rather than
`master`, so a bare local run cannot reintroduce this.

## 7. Minimal procedure (NOT executed)

Using the repository's own workflow, with the NDK present (it is **not** installed on
this host — see section 8):

```bash
# 1. OpenSSL per ABI (already what CI does)
ANDROID_NDK_ROOT="$ANDROID_HOME/ndk/26.3.11579264" \
  FORCE_ALL_ABIS=1 ./scripts/build-openssl-android.sh

# 2. TDLib + Java bindings from the pinned commit — this regenerates all three
#    libtdjni.so AND TdApi.java/Client.java from one source tree, so they cannot
#    disagree afterwards.
TDLIB_REF=022d60202e446ad1287b9fb68e687c8a0760788b \
TDLIB_VERSION=1.8.66 \
ANDROID_NDK_ROOT="$ANDROID_HOME/ndk/26.3.11579264" \
  ./scripts/build-tdlib-android.sh

# 3. Gate — all three ABIs
TDLIB_CHECK_ABI=all ./scripts/check-tdlib-artifacts.sh
```

Then confirm, before committing:

- all three `data/src/main/jniLibs/<abi>/libtdjni.so` contain `1.8.66` and
  `022d6020…` (re-run the string/hash search from `TDLIB_VERSION_CHECK.md`);
- every PT_LOAD segment is `0x4000` (stage 512f288 already established the flags —
  keep them, or confirm NDK r26 defaults to 16 KB);
- `git diff` on `TdApi.java` shows the 41 master-only classes disappearing and
  `canSendWelcomeMessages` / `hasOutgoingMessageAccentColor` being removed — i.e. it
  moves *back* to what is already checked in. If `TdApi.java` does **not** change,
  the rebuild did not use the pinned ref.
- `docs/TDLIB_ARTIFACT_MANIFEST.md` and `docs/TDLIB_SHA256SUMS.txt` regenerate
  automatically (stage 6).

The whole thing is what `.github/workflows/tdlib-upgrade.yml` already automates — the
only change needed is to pass `ref: 022d60202e446ad1287b9fb68e687c8a0760788b` and
`expected_version: 1.8.66` instead of accepting its `master` default.

## 8. Limits of this investigation

- **No NDK on this host** (`%LOCALAPPDATA%\Android\Sdk\ndk` does not exist, no
  Android Studio), so nothing was rebuilt and no `llvm-readelf` was available;
  alignment/architecture figures come from the repo's pure-Python ELF reader,
  cross-checked against `file`.
- Schema comparison used the `.tl` files fetched over HTTPS; the `.tl` files carry no
  explicit constructor ids, so the comparison is on **class and field names**, which
  is sufficient here because master is a strict superset (0 removals, 41 additions)
  and both discriminating classes differ by exactly one added field.
- The exact `td-master` revision behind the arm64/x86_64 binaries is **unknowable**
  from the repo: those binaries embed no commit hash and `master` is not pinned.
  Nothing in this document depends on identifying it.

## 9. Provenance

- `git log --follow` on `TdApi.java`, `data/src/main/jniLibs/{arm64-v8a,armeabi-v7a,x86_64}/libtdjni.so`;
  `git show --stat` on `05c0f01`, `512f288`, `eb5e4b9`.
- Fetched `td/generate/scheme/td_api.tl` at `022d6020…` and at `master`; class/field
  set comparison against `TdApi.java`.
- Read: `scripts/build-tdlib-android.sh`, `.github/workflows/tdlib-upgrade.yml`,
  `android-ci.yml`, `android-release.yml`, `docs/TDLIB_ARTIFACT_MANIFEST.md`.
- Grepped all TdApi call sites in `data/`, `app/`, `feature/`, `core/` main sources.
- No file other than this document was created or modified.
