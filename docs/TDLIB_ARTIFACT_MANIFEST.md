# TDLib Artifact Manifest & Gap Analysis

## 1. Upstream Source Information
- **Repository**: https://github.com/tdlib/td
- **Tag**: `v1.8.0`
- **Pinned Commit**: `b3ab664a18f8611f4dfcd3054717504271eeaa7a`
- **Target Java Package**: `org.drinkless.tdlib`
- **Native JNI Library**: `libtdjni.so`
- **Build Toolchain**: Android NDK `26.3.11579264`, Android API `21`, CMake `3.22.1`, OpenSSL `1.1.1w`

## 2. Java Binding Status
The Java bindings in `app/src/main/java/org/drinkless/tdlib/` are present, complete, and matched to TDLib v1.8.0.

| Java Source File | Relative Path | Size | Status | SHA-256 |
|---|---|---:|---|---|
| Client binding | `app/src/main/java/org/drinkless/tdlib/Client.java` | 8,725 bytes | ✅ PRESENT | `7bcd825fb59b0438478446f4624513078943d73147b5375f215dc10948a87f2e` |
| Log binding | `app/src/main/java/org/drinkless/tdlib/Log.java` | 3,401 bytes | ✅ PRESENT | `e162d82cd9b88f89668ba83451d600f578de205ceaf90625f062aad757173a36` |
| TdApi binding | `app/src/main/java/org/drinkless/tdlib/TdApi.java` | 744,333 bytes | ✅ PRESENT | `bafe9c04ae3ce46f65ec160da4d07d955aeae28ac7122c78175ee351561b42fd` |

### JNI Native Method Signatures in `Client.java`
- `createNativeClient() -> int`
- `nativeClientSend(int nativeClientId, long eventId, TdApi.Function function) -> void`
- `nativeClientReceive(int[] clientIds, long[] eventIds, TdApi.Object[] events, double timeout) -> int`
- `nativeClientExecute(TdApi.Function function) -> TdApi.Object`

## 3. Native JNI Runtime Status (`libtdjni.so`)
The native C++ TDLib library (`libtdjni.so`) compiled for target Android ABIs is currently **MISSING** from the workspace.

| Target ABI | Expected Relative Path | Library Name | Status | Expected SHA-256 (Reference) |
|---|---|---|---|---|
| `arm64-v8a` | `app/src/main/jniLibs/arm64-v8a/libtdjni.so` | `libtdjni.so` | ❌ MISSING | `4de55947ab2d204d5c1bfebf857457f81d8115acd8cef5c82ec23eda2a29aea8` |
| `armeabi-v7a` | `app/src/main/jniLibs/armeabi-v7a/libtdjni.so` | `libtdjni.so` | ❌ MISSING | `372bfd30b1bdb2ad47632336235d7f8fa23516de066716847be92f768eb65e36` |
| `x86_64` | `app/src/main/jniLibs/x86_64/libtdjni.so` | `libtdjni.so` | ❌ MISSING | `3ea8b9459afb18af3df174d305640afed7d4bbc695d2d440d36b3db10e79dde0` |

## 4. Current Gap Analysis Summary
- **JAVA_BINDINGS_PRESENT**: `true`
- **NATIVE_TDLIB_PRESENT**: `false`
- **RUNTIME_STATUS**: `UNAVAILABLE` (Production UI gracefully displays runtime unavailable status without fallback fakes).
