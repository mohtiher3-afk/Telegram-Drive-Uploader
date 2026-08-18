# TDLib for Android Build Instructions

This document outlines the authoritative, reproducible build process for compiling the official Telegram Database Library (**TDLib v1.8.0**) for Android using an external NDK-enabled Linux, macOS, or Windows (WSL2) environment.

---

## 1. Prerequisites & Toolchain Requirements

To compile TDLib natively for Android, your host machine requires:

| Tool | Version | Purpose |
| :--- | :--- | :--- |
| **Java Development Kit (JDK)** | 17+ | Java/Kotlin source generation |
| **Android SDK** | API Level 34+ | Target Android framework |
| **Android NDK** | r26b+ (recommended) | Native cross-compilation toolchain |
| **CMake** | 3.22.1+ | Build configuration & Makefiles |
| **Ninja** | 1.10+ | Fast build execution |
| **gperf** | 3.1+ | Perfect hash generator for TDLib schema parsing |
| **PHP** | 8.0+ | TL-parser generation script support |
| **Git** | 2.30+ | Repository version management |

---

## 2. Step-by-Step Compilation Workflow

### Step 1: Clone Official TDLib Repository
```bash
git clone https://github.com/tdlib/td.git
cd td
git checkout v1.8.0
```

### Step 2: Configure Environment Variables
```bash
export ANDROID_HOME=$HOME/Android/Sdk
export ANDROID_NDK_HOME=$ANDROID_HOME/ndk/26.1.10909125
export PATH=$ANDROID_HOME/cmake/3.22.1/bin:$PATH
```

### Step 3: Run the Official Android Build Script
Navigate to the official Android build example:
```bash
cd example/android
./build-openssl.sh
./build-tdlib.sh
```

Alternatively, use Docker for an isolated, reproducible container build:
```bash
cd example/android
docker build --build-arg TDLIB_INTERFACE=java --output tdlib .
```

---

## 3. Generated Artifacts & Integration Destinations

Upon successful build completion, the following artifacts will be produced:

### A. Java JNI Bindings
Generated source files containing `org.drinkless.tdlib.Client` and `org.drinkless.tdlib.TdApi`:
- **Source location**: `tdlib/java/`
- **Destination in Project**: `app/src/main/java/org/drinkless/tdlib/`

### B. Native Shared Libraries (`libtdjni.so`)
Compiled ELF shared objects for each targeted Android ABI:
- **`arm64-v8a`**: `tdlib/libs/arm64-v8a/libtdjni.so` -> `app/src/main/jniLibs/arm64-v8a/libtdjni.so`
- **`armeabi-v7a`**: `tdlib/libs/armeabi-v7a/libtdjni.so` -> `app/src/main/jniLibs/armeabi-v7a/libtdjni.so`
- **`x86_64`**: `tdlib/libs/x86_64/libtdjni.so` -> `app/src/main/jniLibs/x86_64/libtdjni.so`

---

## 4. Verification and Validation

After copying the generated artifacts, verify their presence and integrity using:
```bash
chmod +x scripts/check-tdlib-artifacts.sh
./scripts/check-tdlib-artifacts.sh
```
