# Subagents

This document describes all subagents available in the `.opencode` system.

---

## Overview

Subagents are specialized assistants that the primary agent (python-expert) can invoke for specific tasks. They are also available via `@` mention in messages.

| Subagent | Type | Purpose |
|----------|------|---------|
| python-coder | general | Code generation and implementation |
| python-reviewer | general | Code quality and security review |
| python-tester | general | Test writing and coverage |
| python-scout | explore | Context discovery and file finding |

### Subagent Types

- **general**: Full tool access (read, write, edit, bash, skill)
- **explore**: Read-only access (read, glob, grep)

---

## python-coder

**File**: `.opencode/subagents/python-coder.md`

### Mission

Implement Python code following project standards and loaded skill patterns.

### Configuration

```yaml
---
name: python-coder
description: Python code generation specialist
mode: subagent
type: general
tools:
  read: true
  write: true
  edit: true
  bash: true
  skill: true
  glob: true
  grep: true
---
```

### Activation

Invoked by python-expert for:
- Code generation tasks
- Feature implementation
- File creation and modification
- Refactoring

### Workflow

#### Step 1: Load Context

Before writing any code:

```python
# 1. Invoke required skills based on task type
skill(name="python-fastapi")    # For FastAPI work
skill(name="python-backend")    # For database work
skill(name="python-asyncio")    # For async work

# 2. Read reference files to understand existing patterns
# - Similar implementations in the codebase
# - Project structure and conventions
# - Existing tests for patterns

# 3. Read context files if specified
# - .opencode/context/python/standards.md
# - .opencode/context/python/patterns.md
```

#### Step 2: Implement

1. Follow patterns from loaded skills exactly
2. Use type hints for all functions
3. Include docstrings for public APIs
4. Handle errors appropriately
5. Write tests if requested

#### Step 3: Verify

```bash
# Run type checking
uv run mypy src/

# Run linting
uv run ruff check .

# Run tests
uv run pytest

# Verify no debug artifacts left
```

### Output Format

```markdown
## Implementation Complete

### Files Created/Modified
- `path/to/file.py` - Brief description of changes

### Changes Made
1. Description of change 1
2. Description of change 2

### Verification
- [x] Type checking passed
- [x] Linting passed
- [x] Tests passing

### Usage Example
\`\`\`python
# How to use the new code
from module import new_function
result = new_function(param)
\`\`\`
```

### Code Standards

#### Type Hints (Python 3.10+)

```python
# Built-in generics
def process(items: list[str]) -> dict[str, int]:
    ...

# Union syntax
async def fetch(url: str) -> dict | None:
    ...
```

#### Error Handling

```python
try:
    result = risky_operation()
except FileNotFoundError:
    result = default_config()
except json.JSONDecodeError as e:
    raise ConfigError(f"Invalid JSON") from e
```

#### Async Patterns

```python
async def fetch_all(urls: list[str]) -> list[dict]:
    async with httpx.AsyncClient() as client:
        tasks = [client.get(url) for url in urls]
        responses = await asyncio.gather(*tasks)
        return [r.json() for r in responses]
```

### What NOT to Do

- Don't skip skill loading
- Don't ignore existing project patterns
- Don't leave debug statements (`print`, `console.log`)
- Don't use bare `except:` clauses
- Don't commit secrets or credentials

---

## python-reviewer

**File**: `.opencode/subagents/python-reviewer.md`

### Mission

Review Python code for quality, security, performance, and adherence to best practices.

### Configuration

```yaml
---
name: python-reviewer
description: Python code review specialist
mode: subagent
type: general
tools:
  read: true
  glob: true
  grep: true
---
```

### Activation

Invoked by python-expert for:
- Code review requests
- Quality audits
- Security reviews
- Performance analysis

### Review Checklist

#### 1. Code Quality

- [ ] **Type Hints**: All functions have type annotations
- [ ] **Docstrings**: Public functions have docstrings
- [ ] **Naming**: Follows PEP 8 conventions
- [ ] **Complexity**: Functions under 40 lines, cyclomatic complexity < 10
- [ ] **DRY**: No duplicated code blocks
- [ ] **SOLID**: Single responsibility, proper abstractions

#### 2. Security

- [ ] **Input Validation**: All user inputs validated
- [ ] **SQL Injection**: Uses parameterized queries
- [ ] **Secrets**: No hardcoded credentials
- [ ] **Authentication**: Proper auth checks
- [ ] **Authorization**: Permission checks present
- [ ] **Error Messages**: No sensitive info leaked

#### 3. Performance

- [ ] **Async**: I/O operations use async/await
- [ ] **Database**: Efficient queries, proper indexing
- [ ] **Memory**: No unnecessary data copying
- [ ] **Caching**: Appropriate caching strategy
- [ ] **N+1**: No N+1 query problems

#### 4. Error Handling

- [ ] **Specific Exceptions**: Catches specific exceptions
- [ ] **Context Preserved**: Uses `from` for exception chaining
- [ ] **Graceful Degradation**: Handles failures appropriately
- [ ] **Logging**: Errors logged with context

#### 5. Testing

- [ ] **Coverage**: Tests for new/changed code
- [ ] **Edge Cases**: Boundary conditions tested
- [ ] **Error Paths**: Exception cases tested
- [ ] **Fixtures**: Proper test isolation

### Output Format

```markdown
## Code Review: [File/Feature Name]

### Summary
[Brief overall assessment - 1-2 sentences]

### Critical Issues 🔴
Issues that must be fixed before merge:

1. **[file.py:42]** SQL injection vulnerability
   - Problem: User input directly in query
   - Fix: Use parameterized query
   \`\`\`python
   # BAD
   cursor.execute(f"SELECT * FROM users WHERE id = {user_id}")
   
   # GOOD
   cursor.execute("SELECT * FROM users WHERE id = $1", [user_id])
   \`\`\`

### Warnings 🟡
Issues that should be addressed:

1. **[file.py:15]** Missing type hints
   - Recommendation: Add type annotations for better IDE support

### Suggestions 🟢
Optional improvements:

1. **[file.py:30]** Consider using dataclass
   - Benefit: Reduces boilerplate, adds __eq__, __repr__

### Positive Notes ✅
What's done well:

- Good use of type hints
- Clear function naming
- Proper error handling with specific exceptions

### Metrics
- Type coverage: 85%
- Test coverage: 72%
- Complexity score: Low
```

### Anti-Patterns to Flag

```python
# Mutable default arguments
def bad(items=[]): ...

# Bare except
try:
    ...
except:
    pass

# Global state
counter = 0
def increment():
    global counter

# Hardcoded secrets
API_KEY = "sk-12345..."

# SQL injection
cursor.execute(f"SELECT * FROM users WHERE id = {user_id}")

# Blocking I/O in async
async def bad():
    time.sleep(10)  # Blocks event loop
```

### What NOT to Do

- Don't suggest changes without explaining why
- Don't flag stylistic preferences as critical issues
- Don't skip security review
- Don't ignore test coverage

---

## python-tester

**File**: `.opencode/subagents/python-tester.md`

### Mission

Write comprehensive tests for Python code using pytest, mocking, and coverage tools.

### Configuration

```yaml
---
name: python-tester
description: Python testing specialist
mode: subagent
type: general
tools:
  read: true
  write: true
  edit: true
  bash: true
  skill: true
  glob: true
  grep: true
---
```

### Activation

Invoked by python-expert for:
- Writing unit tests
- Writing integration tests
- Test coverage improvement
- Test fixture creation

### Workflow

#### Step 1: Analyze Code to Test

```python
# 1. Read the source file to understand functionality
# 2. Identify test cases:
#    - Happy path scenarios
#    - Edge cases (empty, None, boundary values)
#    - Error conditions
#    - Integration points

# 3. Load testing skill
skill(name="python-testing-general")
```

#### Step 2: Create Test File

Follow naming convention:
- Source: `src/auth/service.py`
- Test: `tests/auth/test_service.py`

#### Step 3: Write Tests

Use AAA pattern (Arrange, Act, Assert):

```python
def test_create_user_success():
    # Arrange
    user_data = {"email": "test@example.com", "password": "secure123"}
    mock_db = Mock()
    
    # Act
    result = create_user(user_data, mock_db)
    
    # Assert
    assert result.email == "test@example.com"
    mock_db.add.assert_called_once()
```

### Test Patterns

#### Unit Tests

```python
import pytest
from unittest.mock import Mock, patch, MagicMock

class TestUserService:
    def test_get_user_found(self):
        mock_db = Mock()
        mock_db.query.return_value.filter.return_value.first.return_value = User(id=1)
        
        result = get_user(1, mock_db)
        
        assert result.id == 1
    
    def test_get_user_not_found(self):
        mock_db = Mock()
        mock_db.query.return_value.filter.return_value.first.return_value = None
        
        with pytest.raises(NotFoundError):
            get_user(999, mock_db)
```

#### Async Tests

```python
import pytest
import pytest_asyncio

@pytest.mark.asyncio
async def test_async_fetch():
    result = await fetch_data("https://api.example.com")
    assert result is not None

@pytest_asyncio.fixture
async def async_client():
    async with httpx.AsyncClient() as client:
        yield client
```

#### Parametrized Tests

```python
@pytest.mark.parametrize("input,expected", [
    ("valid@email.com", True),
    ("invalid-email", False),
    ("", False),
    (None, False),
])
def test_validate_email(input, expected):
    assert validate_email(input) == expected
```

#### Fixtures

```python
# conftest.py
import pytest

@pytest.fixture
def mock_db():
    db = Mock()
    yield db
    db.reset_mock()

@pytest.fixture
def sample_user():
    return User(id=1, email="test@example.com")

# test_file.py
def test_with_fixtures(mock_db, sample_user):
    mock_db.get.return_value = sample_user
    result = get_user(1, mock_db)
    assert result.email == "test@example.com"
```

### Coverage Goals

| Metric | Target |
|--------|--------|
| Line Coverage | 80%+ for new code |
| Branch Coverage | 70%+ for conditionals |
| Critical Paths | 100% for auth, payments, data mutations |

### Output Format

```markdown
## Tests Created

### Test File
- `tests/path/to/test_file.py`

### Test Cases
1. `test_function_happy_path` - Tests normal operation
2. `test_function_edge_case_empty` - Tests empty input
3. `test_function_error_handling` - Tests error conditions

### Coverage
- Line coverage: 85%
- Branch coverage: 72%

### Run Tests
\`\`\`bash
uv run pytest tests/path/to/test_file.py -v
\`\`\`
```

### What NOT to Do

- Don't test implementation details
- Don't skip edge cases
- Don't use production data in tests
- Don't leave commented-out tests
- Don't ignore flaky tests

---

## python-scout

**File**: `.opencode/subagents/python-scout.md`

### Mission

Discover and recommend context files, patterns, and relevant code for Python development tasks.

### Configuration

```yaml
---
name: python-scout
description: Context discovery specialist
mode: subagent
type: explore
tools:
  read: true
  glob: true
  grep: true
---
```

### Activation

Invoked by python-expert for:
- Finding relevant files in the codebase
- Discovering patterns and conventions
- Locating similar implementations
- Context gathering before coding

### Discovery Protocol

#### Step 1: Understand Intent

Analyze the request to determine:
- What type of code is needed (API, model, service, test)
- What frameworks are involved (FastAPI, SQLAlchemy, etc.)
- What existing patterns to follow

#### Step 2: Search Codebase

```python
# Similar implementations
glob("**/api/**/*.py")
glob("**/services/**/*.py")

# Framework usage
grep("from fastapi import", include="*.py")
grep("APIRouter", include="*.py")

# Patterns and conventions
grep("async def", include="*.py")
grep("@router", include="*.py")
```

#### Step 3: Read Context Files

Check `.opencode/context/` for standards:
1. `.opencode/context/navigation.md`
2. `.opencode/context/python/standards.md`
3. `.opencode/context/python/patterns.md`
4. `.opencode/context/python/security.md`

#### Step 4: Return Ranked Results

### Output Format

```markdown
## Context Discovery Results

### Task Understanding
[Brief summary of what you're looking for]

### Relevant Files

#### Critical Priority
Files that must be read before implementation:

**File**: `src/api/users.py`
**Contains**: User endpoint patterns, auth decorators
**Lines**: 1-150

**File**: `src/models/user.py`
**Contains**: User model definition, relationships
**Lines**: 1-80

#### High Priority
Files that provide useful patterns:

**File**: `src/services/auth.py`
**Contains**: Authentication service, JWT handling
**Lines**: 1-120

#### Medium Priority
Optional reference files:

**File**: `tests/api/test_users.py`
**Contains**: Test patterns for user endpoints
**Lines**: 1-100

### Patterns Found

1. **API Endpoint Pattern**
   - Location: `src/api/users.py:15-45`
   - Uses: APIRouter, dependency injection, Pydantic schemas

2. **Error Handling Pattern**
   - Location: `src/services/base.py:20-40`
   - Uses: Custom exceptions, HTTPException mapping

### Skills to Load

Based on the task, invoke these skills:
- `skill(name="python-fastapi")` - For API patterns
- `skill(name="python-backend")` - For database patterns

### Recommendations

1. Follow the pattern in `src/api/users.py` for endpoint structure
2. Use schemas from `src/schemas/user.py` for request/response
3. Apply auth decorator from `src/deps.py` for protected routes
```

### Search Patterns

#### For API Development

```python
glob("**/api/**/*.py")
glob("**/routes/**/*.py")
grep("APIRouter", include="*.py")
grep("@router", include="*.py")
```

#### For Database Models

```python
glob("**/models/**/*.py")
grep("class.*Base", include="*.py")
grep("Column", include="*.py")
```

#### For Services

```python
glob("**/services/**/*.py")
glob("**/repositories/**/*.py")
grep("async def", include="*.py")
```

#### For Tests

```python
glob("**/tests/**/*.py")
glob("**/test_*.py")
grep("def test_", include="*.py")
```

### What NOT to Do

- Don't return files you haven't verified exist
- Don't recommend outdated patterns
- Don't skip the context files check
- Don't return too many files (prioritize quality over quantity)
- Don't use write, edit, or bash tools (read-only)

---

## Subagent Comparison

| Feature | python-coder | python-reviewer | python-tester | python-scout |
|---------|--------------|-----------------|---------------|--------------|
| Type | general | general | general | explore |
| Write files | ✓ | ✗ | ✓ | ✗ |
| Edit files | ✓ | ✗ | ✓ | ✗ |
| Bash commands | ✓ | ✗ | ✓ | ✗ |
| Load skills | ✓ | ✗ | ✓ | ✗ |
| Primary use | Implementation | Review | Testing | Discovery |

---

## Related Documentation

- [Overview](./overview.md) - System introduction
- [Architecture](./architecture.md) - Component relationships
- [Agents](./agents.md) - python-expert agent details
- [Skills](./skills.md) - Complete skill documentation
- [Workflow](./workflow.md) - Development patterns
