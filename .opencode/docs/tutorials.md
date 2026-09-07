# Tutorials

This document provides step-by-step tutorials for common tasks using the `.opencode` system.

---

## Tutorial 1: Creating a FastAPI Endpoint

### Goal

Create a complete user registration endpoint with validation, password hashing, and JWT token generation.

### Prerequisites

- Python project with FastAPI installed
- `.opencode` system configured

### Steps

#### Step 1: Request with Specific Requirements

Start with a clear, specific request:

```
Create a POST /api/v1/auth/register endpoint with:
- Email validation (valid format, unique check)
- Password validation (min 8 chars, requires uppercase)
- Password hashing with bcrypt
- Returns JWT token on success
- Returns 409 for duplicate email
- Returns 422 for validation errors
```

#### Step 2: Agent Loads Skills

The agent will automatically load relevant skills:

```python
skill(name="python-fastapi")
skill(name="python-backend")
```

#### Step 3: Context Discovery

The agent uses python-scout to find existing patterns:

```python
task(
    subagent_type="explore",
    description="Find existing auth patterns",
    prompt="Find existing authentication endpoints, user models, and schema patterns"
)
```

Expected findings:
- `src/api/v1/endpoints/` - Existing endpoints
- `src/models/user.py` - User model
- `src/schemas/` - Pydantic schemas
- `src/core/security.py` - Existing security utilities

#### Step 4: Implementation

The agent delegates to python-coder:

```python
task(
    subagent_type="general",
    description="Create registration endpoint",
    prompt="""Create user registration endpoint:

Files to create/modify:
1. src/schemas/auth.py - Add RegisterRequest, RegisterResponse schemas
2. src/api/v1/endpoints/auth.py - Add POST /register endpoint
3. src/services/auth.py - Add register_user function

Requirements:
- Email validation with EmailStr
- Password min 8 chars, must contain uppercase
- Use bcrypt for password hashing (passlib)
- Generate JWT token on success
- Return 409 Conflict for duplicate email

Follow patterns from:
- src/api/v1/endpoints/users.py for endpoint structure
- src/core/security.py for hashing and JWT
"""
)
```

#### Step 5: Verify Output

Expected files created:

**src/schemas/auth.py**
```python
from pydantic import BaseModel, EmailStr, Field

class RegisterRequest(BaseModel):
    email: EmailStr
    password: str = Field(min_length=8)

class RegisterResponse(BaseModel):
    id: int
    email: str
    access_token: str
    token_type: str = "bearer"
```

**src/api/v1/endpoints/auth.py**
```python
from fastapi import APIRouter, HTTPException, status
from app.schemas.auth import RegisterRequest, RegisterResponse
from app.services.auth import register_user
from app.api.deps import DbSession

router = APIRouter()

@router.post("/register", response_model=RegisterResponse)
async def register(data: RegisterRequest, db: DbSession):
    result = await register_user(data, db)
    if result is None:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Email already registered"
        )
    return result
```

#### Step 6: Run Verification

```bash
# Type checking
uv run mypy src/

# Linting
uv run ruff check .

# Run tests (if created)
uv run pytest tests/api/test_auth.py -v
```

---

## Tutorial 2: Writing Tests for New Code

### Goal

Write comprehensive tests for the registration endpoint created in Tutorial 1.

### Steps

#### Step 1: Request Tests

```
Write tests for the registration endpoint in src/api/v1/endpoints/auth.py:
- Test successful registration
- Test duplicate email rejection
- Test invalid email format
- Test weak password rejection
- Use pytest-asyncio
- Mock the database
```

#### Step 2: Agent Loads Testing Skill

```python
skill(name="python-testing-general")
```

#### Step 3: Test Implementation

The agent delegates to python-tester:

```python
task(
    subagent_type="general",
    description="Write registration tests",
    prompt="""Write tests for the registration endpoint:

Test file: tests/api/v1/test_auth.py

Test cases needed:
1. test_register_success - Valid data returns 200 with token
2. test_register_duplicate_email - Returns 409 for duplicate
3. test_register_invalid_email - Returns 422 for bad email
4. test_register_weak_password - Returns 422 for weak password

Use:
- pytest-asyncio for async tests
- httpx AsyncClient for API testing
- Mock database session

Follow patterns from:
- tests/api/test_users.py
"""
)
```

#### Step 4: Verify Tests

Expected test file:

```python
# tests/api/v1/test_auth.py
import pytest
from httpx import AsyncClient, ASGITransport
from app.main import app

@pytest.fixture
async def client():
    async with AsyncClient(
        transport=ASGITransport(app=app),
        base_url="http://test"
    ) as client:
        yield client

@pytest.mark.asyncio
async def test_register_success(client: AsyncClient):
    response = await client.post("/api/v1/auth/register", json={
        "email": "test@example.com",
        "password": "SecurePass123"
    })
    assert response.status_code == 200
    data = response.json()
    assert "access_token" in data
    assert data["email"] == "test@example.com"

@pytest.mark.asyncio
async def test_register_duplicate_email(client: AsyncClient):
    # First registration
    await client.post("/api/v1/auth/register", json={
        "email": "dupe@example.com",
        "password": "SecurePass123"
    })
    
    # Second registration with same email
    response = await client.post("/api/v1/auth/register", json={
        "email": "dupe@example.com",
        "password": "SecurePass123"
    })
    assert response.status_code == 409

@pytest.mark.asyncio
async def test_register_invalid_email(client: AsyncClient):
    response = await client.post("/api/v1/auth/register", json={
        "email": "not-an-email",
        "password": "SecurePass123"
    })
    assert response.status_code == 422

@pytest.mark.asyncio
async def test_register_weak_password(client: AsyncClient):
    response = await client.post("/api/v1/auth/register", json={
        "email": "test2@example.com",
        "password": "weak"
    })
    assert response.status_code == 422
```

#### Step 5: Run Tests

```bash
uv run pytest tests/api/v1/test_auth.py -v

# With coverage
uv run pytest tests/api/v1/test_auth.py --cov=src/api/v1/endpoints/auth.py
```

---

## Tutorial 3: Code Review Workflow

### Goal

Perform a security review of authentication code.

### Steps

#### Step 1: Request Review

```
Review src/core/security.py and src/api/v1/endpoints/auth.py for security issues:
- Check for hardcoded secrets
- Verify password hashing strength
- Check JWT implementation
- Look for SQL injection
- Verify input validation
```

#### Step 2: Review Execution

The agent delegates to python-reviewer:

```python
task(
    subagent_type="general",
    description="Security review of auth code",
    prompt="""Review these files for security issues:

Files:
- src/core/security.py
- src/api/v1/endpoints/auth.py

Focus on:
1. Hardcoded secrets or credentials
2. Password hashing implementation
3. JWT token generation and validation
4. SQL injection vulnerabilities
5. Input validation completeness
6. Error message information leakage

Provide detailed report with:
- Critical issues (must fix)
- Warnings (should fix)
- Suggestions (consider fixing)
- Positive notes (what's done well)
"""
)
```

#### Step 3: Review Report

Expected output:

```markdown
## Code Review: Authentication Security

### Summary
Generally well-implemented authentication with some areas needing attention.

### Critical Issues 🔴
1. **[security.py:15]** JWT secret key should be from environment
   - Problem: Using hardcoded secret key
   - Fix: Load from environment variable
   \`\`\`python
   # BAD
   SECRET_KEY = "my-secret-key"
   
   # GOOD
   import os
   SECRET_KEY = os.environ["SECRET_KEY"]
   \`\`\`

2. **[auth.py:42]** No rate limiting on login endpoint
   - Problem: Vulnerable to brute force attacks
   - Fix: Add SlowAPI rate limiter

### Warnings 🟡
1. **[security.py:30]** Consider increasing bcrypt rounds
   - Current: 12 rounds
   - Recommendation: 14 rounds for better security

### Suggestions 🟢
1. **[auth.py:25]** Add password strength validation
   - Benefit: Prevents weak passwords

### Positive Notes ✅
- Good use of passlib with bcrypt
- Proper exception handling
- Type hints throughout
- Input validation with Pydantic
```

---

## Tutorial 4: Adding a New Skill

### Goal

Create a custom skill for API documentation generation.

### Steps

#### Step 1: Create Skill Directory

```bash
mkdir -p .opencode/skills/api-documentation
```

#### Step 2: Create SKILL.md

Create `.opencode/skills/api-documentation/SKILL.md`:

```markdown
---
name: api-documentation
description: "Generate OpenAPI documentation and markdown docs for FastAPI endpoints"
license: MIT
metadata:
  audience: developers
  workflow: documentation
---

# API Documentation

Generate comprehensive documentation for FastAPI endpoints.

## When to Use This Skill

- Documenting new endpoints
- Creating API reference docs
- Generating OpenAPI examples
- Writing endpoint usage guides

## Documentation Patterns

### Endpoint Documentation

\`\`\`python
@router.get(
    "/users/{user_id}",
    response_model=UserResponse,
    summary="Get user by ID",
    description="Retrieve a single user by their unique identifier.",
    responses={
        200: {"description": "User found"},
        404: {"description": "User not found"},
    }
)
async def get_user(user_id: int, db: DbSession):
    ...
\`\`\`

### Markdown Documentation

\`\`\`markdown
## GET /api/v1/users/{user_id}

Retrieve a single user by ID.

### Parameters
| Name | Type | Required | Description |
|------|------|----------|-------------|
| user_id | integer | Yes | User's unique identifier |

### Responses
| Code | Description |
|------|-------------|
| 200 | User object |
| 404 | User not found |

### Example
\`\`\`bash
curl -X GET "http://api.example.com/api/v1/users/123"
\`\`\`
\`\`\`

## Best Practices

1. Include example requests and responses
2. Document all error codes
3. Use clear, concise descriptions
4. Keep examples up-to-date with code
```

#### Step 3: Register in agent-metadata.json

Edit `.opencode/config/agent-metadata.json`:

```json
{
  "skills": {
    "api-documentation": {
      "id": "api-documentation",
      "name": "API Documentation",
      "version": "1.0.0",
      "triggers": ["documentation", "openapi", "docs", "swagger"]
    }
  }
}
```

#### Step 4: Test the Skill

```
Generate documentation for the registration endpoint
```

The agent should now:
1. Detect "documentation" keyword
2. Load `skill(name="api-documentation")`
3. Follow the documentation patterns

---

## Tutorial 5: Refactoring with Tests

### Goal

Safely refactor a service to use the repository pattern while maintaining test coverage.

### Steps

#### Step 1: Request Refactoring

```
Refactor src/services/user.py to use the repository pattern:
- Create src/repositories/user.py
- Move database operations to repository
- Keep business logic in service
- Ensure existing tests still pass
```

#### Step 2: Context Discovery

```python
task(
    subagent_type="explore",
    description="Find current user service implementation",
    prompt="Find src/services/user.py and related test files"
)
```

#### Step 3: Implementation

```python
task(
    subagent_type="general",
    description="Refactor to repository pattern",
    prompt="""Refactor user service to repository pattern:

Current: src/services/user.py (contains both DB and business logic)
Target:
1. src/repositories/user.py - Database operations only
2. src/services/user.py - Business logic, uses repository

Steps:
1. Create src/repositories/user.py with:
   - UserRepository class
   - get_by_id, get_by_email, create, update methods
   
2. Modify src/services/user.py:
   - Remove direct DB operations
   - Inject UserRepository
   - Keep business logic methods

3. Update tests if needed
4. Ensure all existing tests pass

Follow patterns from:
- src/repositories/item.py (if exists)
"""
)
```

#### Step 4: Verification

```bash
# Run all user-related tests
uv run pytest tests/services/test_user.py tests/repositories/test_user.py -v

# Check type safety
uv run mypy src/services/user.py src/repositories/user.py

# Verify coverage maintained
uv run pytest --cov=src/services/user.py --cov=src/repositories/user.py
```

---

## Related Documentation

- [Overview](./overview.md) - System introduction
- [Workflow](./workflow.md) - Development patterns
- [Agents](./agents.md) - Agent details
- [Subagents](./subagents.md) - Subagent workflows
- [Skills](./skills.md) - Skill documentation
