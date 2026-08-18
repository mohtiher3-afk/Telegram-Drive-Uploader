# TDLib External Build Environment Specification

## Minimum System Requirements

The build environment responsible for compiling TDLib native libraries must meet these specifications:

### Operating System Support
1. **Linux**: Ubuntu 22.04 LTS / Debian 12 (Recommended)
2. **Windows**: Windows 11 with WSL2 (Ubuntu 22.04 LTS)
3. **macOS**: macOS Sonoma 14+ with Homebrew

### Required Software Packages (Ubuntu / Debian / WSL2)
```bash
sudo apt update && sudo apt install -y \
    build-essential \
    cmake \
    ninja-build \
    gperf \
    php-cli \
    git \
    openjdk-17-jdk \
    zlib1g-dev \
    libssl-dev
```

### Required Android Tooling
- **Android SDK Command-Line Tools**: 11.0+
- **Android NDK**: Version `26.1.10909125` (r26b) or `25.2.9519653` (r25c)
- **CMake (Android SDK Component)**: `3.22.1`

### Recommended Docker Environment
Using the official multi-stage Docker build avoids host package discrepancies:
```dockerfile
# Official TDLib Android Dockerfile (example/android/Dockerfile)
# Runs isolated Android NDK compilation targeting Java JNI
```
