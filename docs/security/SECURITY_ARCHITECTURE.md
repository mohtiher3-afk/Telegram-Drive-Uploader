# Security Architecture

## Threat model

The application minimizes attack surface through a small manifest, app-private storage, explicit backup exclusions, platform TLS defaults, and fail-closed TDLib initialization. It does not claim to prevent compromise of a rooted/unlocked device, reverse engineering of public APK configuration, or credentials entered into a compromised environment.

## Authentication and Telegram data

Telegram API configuration is injected through build/CI configuration rather than literal source values. User authentication is delegated to the official TDLib boundary. Codes, passwords, and session material are not intended for diagnostics. TDLib source, JNI, and ABI artifacts are protected from this phase.

## Local data and files

Room and DataStore remain app-private and are excluded from backup rules. File selection uses Android content URIs and the streaming reader rather than exposing raw filesystem paths through an exported provider. File existence, MIME, readability, and stream behavior remain application validation responsibilities.

## Network and native code

No trust-all TLS implementation, custom hostname verifier, WebView, or cleartext network policy was found in the audited source. Native loading remains the existing packaged official TDLib path; arbitrary external native paths are not introduced.

## Release security

Release signing uses environment-provided secrets and a CI-generated keystore path; signing files are not committed. Debug signing values are confined to debug configuration. R8 and release configuration were not weakened.
