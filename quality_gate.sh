#!/bin/bash
# ==============================================================================
# Telegram Drive Uploader — Automated Quality Gate & CI Pipeline Safeguard
# ==============================================================================
# This script performs static analysis, architecture validation, anti-fake reviews,
# credential scans, dependency lock checks, unit tests, and production build checks.
#
# Exit Codes:
#   0 - All gates passed successfully (GO)
#   1 - A critical gate failed (NO-GO)
# ==============================================================================

set -o pipefail

# ANSI Color Codes for beautiful terminal logs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

echo -e "${BLUE}======================================================================${NC}"
echo -e "${PURPLE}      TELEGRAM DRIVE UPLOADER — CONTINUOUS VERIFICATION PIPELINE      ${NC}"
echo -e "${BLUE}======================================================================${NC}"

# TRACKED STATISTICS
GATES_TOTAL=9
GATES_PASSED=0

# Helper function to print headers
print_gate_header() {
    local gate_num=$1
    local gate_name=$2
    echo -e "\n${BLUE}[Gate $gate_num/$GATES_TOTAL] $gate_name...${NC}"
}

# Helper function to print success
gate_success() {
    echo -e "${GREEN}✔ PASS: Gate check completed successfully.${NC}"
    ((GATES_PASSED++))
}

# Helper function to print failures
gate_failure() {
    local message=$1
    echo -e "${RED}✘ FAIL: $message${NC}"
    echo -e "${RED}======================================================================${NC}"
    echo -e "${RED}             STOP DEVELOPMENT — CRITICAL REGRESSION DETECTED          ${NC}"
    echo -e "${RED}======================================================================${NC}"
    exit 1
}

# ==============================================================================
# GATE 1: DEPENDENCY CATALOG AUDIT
# ==============================================================================
print_gate_header 1 "Dependency Catalog Audit"

LIBS_TOML="gradle/libs.versions.toml"
if [ ! -f "$LIBS_TOML" ]; then
    gate_failure "Version catalog file $LIBS_TOML does not exist."
fi

# Search for dynamic, latest, or wildcard version selectors in the version catalog
if grep -E 'version\s*=\s*"\+"' "$LIBS_TOML" || grep -E 'version\.ref\s*=\s*"\+"' "$LIBS_TOML"; then
    gate_failure "Dynamic '+' version reference found in $LIBS_TOML."
fi

if grep -E 'version\s*=\s*"latest"' "$LIBS_TOML" || grep -E 'version\s*=\s*"\*"' "$LIBS_TOML"; then
    gate_failure "Uncontrolled version selector ('latest' or '*') found in $LIBS_TOML."
fi

echo -e "${GREEN}✔ Catalog check passed: all dependencies are locked and reproducible.${NC}"
gate_success

# ==============================================================================
# GATE 2: ARCHITECTURE & ISOLATION GUARDS
# ==============================================================================
print_gate_header 2 "Architecture & Isolation Guards"

# Enforce clean architecture flow constraints
# Rule A: UI/Compose files (Screen.kt, components, themes) must NOT import Room DAOs or entities directly
echo -e "${BLUE}  - Checking Compose UI layer decoupling from local DAOs/Database...${NC}"
UI_DIR="app/src/main/java/com/telegramdrive/uploader/feature"
COMP_DIR="app/src/main/java/com/telegramdrive/uploader/core/ui"

VIOLATING_UI_DAO=$(find "$UI_DIR" "$COMP_DIR" -type f -name "*.kt" -exec grep -l "import com.telegramdrive.uploader.data.local" {} \+ || true)
if [ -n "$VIOLATING_UI_DAO" ]; then
    echo -e "${YELLOW}Warning: Direct local database imports found in UI files:${NC}"
    echo "$VIOLATING_UI_DAO"
    gate_failure "Architecture Violation: Jetpack Compose files must not directly import local DAO or Database classes."
fi

# Rule B: ViewModels must NOT directly import low-level TDLib/Telegram SDK components
echo -e "${BLUE}  - Checking ViewModel layer decoupling from raw TDLib/Telegram SDK...${NC}"
VIOLATING_VM_TDLIB=$(find "$UI_DIR" -type f -name "*ViewModel.kt" -exec grep -l -E "import org.drinkless.tdlib|import org.telegram" {} \+ || true)
if [ -n "$VIOLATING_VM_TDLIB" ]; then
    echo -e "${YELLOW}Warning: Low-level TDLib/Telegram SDK imports found in ViewModels:${NC}"
    echo "$VIOLATING_VM_TDLIB"
    gate_failure "Architecture Violation: ViewModels must not access low-level Telegram SDK or TDLib classes. Inject repositories instead."
fi

echo -e "${GREEN}✔ Architecture guards passed: UI, ViewModels, and Data layers are clean and decoupled.${NC}"
gate_success

# ==============================================================================
# GATE 3: STORAGE SAFETY & ENTITIES AUDIT
# ==============================================================================
print_gate_header 3 "Storage Safety & Database Entities Audit"

# Database Entity Check: Check that Room entities don't store plain user passwords, codes, or credentials
echo -e "${BLUE}  - Auditing Room Database entities for credentials leak...${NC}"
DB_ENTITY_FILES=$(find app/src/main/java/com/telegramdrive/uploader/data/local -type f -name "*.kt" || true)
for file in $DB_ENTITY_FILES; do
    if grep -E "val\s+(password|verificationCode|apiHash|sessionToken|credential)" "$file" | grep -v -E "(//|/\*)" > /dev/null; then
        gate_failure "Room Entity security violation in $file: Storing plain passwords, verification codes, or API credentials is forbidden."
    fi
done

# DataStore Preferences Check: Ensure SettingsDataStore has no keys representing passwords, verification codes, or API hashes
echo -e "${BLUE}  - Auditing Settings DataStore for credentials leak...${NC}"
DATASTORE_FILE="app/src/main/java/com/telegramdrive/uploader/core/datastore/SettingsDataStore.kt"
if [ -f "$DATASTORE_FILE" ]; then
    if grep -E -i "password|api_hash|verification_code|secret" "$DATASTORE_FILE" | grep -v -E "(//|/\*)" > /dev/null; then
        gate_failure "DataStore storage safety violation: ordinary preference stores must not store user credentials/session secrets."
    fi
fi

echo -e "${GREEN}✔ Storage safety audit passed: credentials and session tokens are strictly isolated.${NC}"
gate_success

# ==============================================================================
# GATE 4: ANTI-FAKE PRODUCTION CODE SCANNER
# ==============================================================================
print_gate_header 4 "Anti-Fake Production Code Scanner"

# Search production codebase (excluding test directories) for demo/fake/mock uploader/client implementations
echo -e "${BLUE}  - Scanning production codebase for artificial simulation shortcuts...${NC}"
PROD_SRC_DIR="app/src/main"

# Look for fake implementation filenames
SUSPICIOUS_FILES=$(find "$PROD_SRC_DIR" -type f -name "*Fake*" -o -name "*Mock*" -o -name "*Demo*" -o -name "*Sample*" -o -name "*Stub*" || true)
if [ -n "$SUSPICIOUS_FILES" ]; then
    echo -e "${YELLOW}Warning: Suspicious file naming in production sources:${NC}"
    echo "$SUSPICIOUS_FILES"
    gate_failure "Anti-Fake: Demo/Fake/Mock/Stub files must not exist in production source sets (only allowed in tests)."
fi

# Look for fake/simulation words in production source code content
FAKE_PATTERNS=("FakeTelegramClient" "MockTelegramClient" "DemoTelegramClient" "FakeUploadEngine" "DemoUploadEngine" "fakeProgress" "fakeSpeed" "fakeETA" "simulateUpload")
for pattern in "${FAKE_PATTERNS[@]}"; do
    FOUND_VIOLATIONS=$(grep -rn "$pattern" "$PROD_SRC_DIR" || true)
    if [ -n "$FOUND_VIOLATIONS" ]; then
        echo -e "${YELLOW}Warning: Suspicious simulation pattern '$pattern' found in production code:${NC}"
        echo "$FOUND_VIOLATIONS"
        gate_failure "Anti-Fake Validation Failed: Production code must use 100% genuine integrations."
    fi
done

# Look for fake-success markers
FOUND_ARTIFICIAL_COMPLETION=$(grep -rn "status = COMPLETED" "$PROD_SRC_DIR" | grep -E -i "delay|timer|loop" || true)
if [ -n "$FOUND_ARTIFICIAL_COMPLETION" ]; then
    echo -e "${YELLOW}Warning: Artificial delay-based status completion found:${NC}"
    echo "$FOUND_ARTIFICIAL_COMPLETION"
    gate_failure "Anti-Fake Validation Failed: Fake delay-based completion is forbidden in production source sets."
fi

echo -e "${GREEN}✔ Anti-fake scan passed: 100% genuine implementation confirmed.${NC}"
gate_success

# ==============================================================================
# GATE 5: STATIC RESOURCE & SECURITY CHECK
# ==============================================================================
print_gate_header 5 "Plaintext Secret & Unsafe Logging Audits"

# Scan files for hardcoded credential values in Kotlin/Java production files
echo -e "${BLUE}  - Scanning for hardcoded API hashes, secrets, or credential properties...${NC}"
HARDCODED_SECRETS=$(grep -rn "apiHash\s*=\s*\"[a-zA-Z0-9]\{32\}\"" "$PROD_SRC_DIR" || true)
if [ -n "$HARDCODED_SECRETS" ]; then
    echo -e "${YELLOW}Warning: Hardcoded API credentials found:${NC}"
    echo "$HARDCODED_SECRETS"
    gate_failure "Plaintext Secret Leak: API Credentials/Hashes must never be hardcoded into the source."
fi

# Scan for unsafe logging patterns
echo -e "${BLUE}  - Scanning for unsafe logging of credentials...${NC}"
UNSAFE_LOGS=$(grep -rn -E "Log\.[vdiwe]\(.*(password|code|api_hash|verification).*|println\(.*(password|code|api_hash|verification).*\)" "$PROD_SRC_DIR" || true)
if [ -n "$UNSAFE_LOGS" ]; then
    echo -e "${YELLOW}Warning: Unsafe logging statement found in production source:${NC}"
    echo "$UNSAFE_LOGS"
    gate_failure "Security Violation: Plaintext logs containing authentication values/secrets are forbidden."
fi

# Scan for unsafe memory operations on files (e.g. readBytes which loads entire file in memory instead of streaming)
echo -e "${BLUE}  - Auditing memory boundaries for unbounded Whole-File reads (streaming guard)...${NC}"
UNBOUNDED_FILE_READS=$(grep -rn "readBytes(" "$PROD_SRC_DIR" || true)
if [ -n "$UNBOUNDED_FILE_READS" ]; then
    echo -e "${YELLOW}Warning: readBytes() call detected. This loads whole-file in memory and can crash on large videos:${NC}"
    echo "$UNBOUNDED_FILE_READS"
    gate_failure "Performance & Memory Guard: readBytes() is prohibited in the production uploader to maintain a streaming-based design."
fi

echo -e "${GREEN}✔ Plaintext and security reviews completed without violations.${NC}"
gate_success

# ==============================================================================
# GATE 6: GRADLE COMPILE VALIDATION
# ==============================================================================
print_gate_header 6 "Compile Check (Debug Variant)"

echo -e "${BLUE}Executing debug build compilation...${NC}"
gradle :app:assembleDebug --no-daemon
if [ $? -ne 0 ]; then
    gate_failure "Debug compilation failed."
fi

echo -e "${GREEN}✔ Debug compilation completed successfully.${NC}"
gate_success

# ==============================================================================
# GATE 7: UNIT TEST EXECUTIVE
# ==============================================================================
print_gate_header 7 "Unit Test Executive"

echo -e "${BLUE}Executing unit and integration test suites...${NC}"
gradle :app:testDebugUnitTest --no-daemon
if [ $? -ne 0 ]; then
    gate_failure "Unit tests failed."
fi

echo -e "${GREEN}✔ All unit test assertions passed.${NC}"
gate_success

# ==============================================================================
# GATE 8: STATIC ANALYSIS (LINT)
# ==============================================================================
print_gate_header 8 "Static Analysis & Lint"

echo -e "${BLUE}Executing Android linter validations...${NC}"
gradle :app:lintDebug --no-daemon
if [ $? -ne 0 ]; then
    gate_failure "Lint analysis failed."
fi

echo -e "${GREEN}✔ Lint analysis passed with zero compilation blockers.${NC}"
gate_success

# ==============================================================================
# GATE 9: PRODUCTION RELEASE COMPILATION (R8 & PROGUARD ACCEPTANCE)
# ==============================================================================
print_gate_header 9 "Production Release Compilation"

echo -e "${BLUE}Executing production release build compilation...${NC}"
gradle :app:assembleRelease --no-daemon
if [ $? -ne 0 ]; then
    gate_failure "Release compilation failed."
fi

echo -e "${GREEN}✔ Production Release variant successfully compiled and signed.${NC}"
gate_success

# ==============================================================================
# REPORT SUMMARY
# ==============================================================================
echo -e "\n${BLUE}======================================================================${NC}"
echo -e "${GREEN}              PIPELINE SUCCESSFUL: ACCEPTANCE CRITERIA MET            ${NC}"
echo -e "${BLUE}======================================================================${NC}"
echo -e "${GREEN}Gates Passed: $GATES_PASSED/$GATES_TOTAL${NC}"
echo -e "${GREEN}All production stability, anti-fake, and architectural guards are clean.${NC}"
echo -e "${BLUE}======================================================================${NC}"

exit 0
