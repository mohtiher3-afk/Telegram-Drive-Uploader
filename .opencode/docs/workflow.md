# Workflow

This document describes typical development workflows using the `.opencode` system.

---

## Overview

The `.opencode` system follows a structured approach to Python development:

```
1. Session Start → Load foundational skills
2. Request Analysis → Detect keywords, determine complexity
3. Skill Loading → Invoke relevant skills via skill() tool
4. Task Execution → Direct answer or delegate to subagent
5. Verification → Run tests, linting, type checking
```

---

## Session Initialization

### Default Flow

```
User opens project
       ↓
OpenCode reads config.json
       ↓
Loads python-expert agent
       ↓
Agent detects .py files
       ↓
Invokes skill(name="python-fundamentals")
       ↓
Scans for frameworks (FastAPI, SQLAlchemy)
       ↓
Ready for requests
```

### First Interaction

On first user message:

```
1. Parse request for keywords
2. Determine task type
3. Load additional skills based on keywords
4. Begin processing
```

---

## Request Handling Flow

### Keyword Detection

The agent scans user requests for keywords to determine which skills to load:

| Keywords Detected | Skills to Load |
|-------------------|----------------|
| `fastapi`, `endpoint`, `api`, `router` | `python-fastapi` |
| `sqlalchemy`, `database`, `model`, `migration` | `python-backend` |
| `pytest`, `test`, `mock`, `fixture` | `python-testing-general` |
| `async`, `await`, `asyncio`, `concurrent` | `python-asyncio` |
| `type`, `mypy`, `pyright`, `typing` | `python-type-hints` |
| Multiple keywords | Multiple skills |

### Complexity Decision

```
                    ┌─────────────────┐
                    │ User Request    │
                    └────────┬────────┘
                             │
                             ▼
              ┌──────────────────────────────┐
              │ Contains create/build/       │
              │ implement/design/refactor?   │
              └──────────────┬───────────────┘
                             │
              ┌──────────────┴──────────────┐
              │                             │
              NO                           YES
              │                             │
              ▼                             ▼
    ┌─────────────────┐          ┌─────────────────┐
    │ Simple Query    │          │ Complex Task    │
    │                 │          │                 │
    │ Answer directly │          │ Delegate to     │
    │ with skill      │          │ subagent        │
    │ context         │          │                 │
    └─────────────────┘          └─────────────────┘
```

---

## Task Type Patterns

### Feature Development

**Full workflow for implementing a new feature:**

```
1. User Request
   "Create a user registration endpoint with email validation"
       ↓
2. Skill Loading
   skill(name="python-fastapi")
   skill(name="python-backend")
       ↓
3. Context Discovery
   task(subagent_type="explore", description="Find user patterns")
       ↓
4. Implementation
   task(subagent_type="general", description="Create endpoint",
        prompt="Create user registration endpoint...")
       ↓
5. Verification
   uv run pytest tests/api/test_users.py
   uv run mypy src/
   uv run ruff check .
```

**Example Request:**
```
Create a user registration endpoint with:
- Email validation
- Password strength check
- Duplicate email detection
- JWT token generation on success
```

**Expected Flow:**
1. Agent loads `python-fastapi` skill
2. Agent loads `python-backend` skill
3. Delegates to `python-coder` for implementation
4. Creates: endpoint, schema, service, tests

### Bug Fixes

**Investigation-focused workflow:**

```
1. User Request
   "Fix the memory leak in the user service"
       ↓
2. Context Discovery
   task(subagent_type="explore", description="Find user service code")
       ↓
3. Analysis
   skill(name="python-asyncio")  # If async-related
   Read and analyze code
       ↓
4. Fix Implementation
   task(subagent_type="general", description="Fix memory leak")
       ↓
5. Verification
   uv run pytest
   Manual testing
```

**Example Request:**
```
Fix the memory leak in src/services/user.py:
- Connection pool not closing
- Occurs after prolonged use
- Affects /api/users endpoint
```

**Expected Flow:**
1. Agent uses `python-scout` to find relevant code
2. Loads `python-backend` skill for database patterns
3. Analyzes the issue
4. Delegates fix to `python-coder`

### Code Review

**Review-focused workflow:**

```
1. User Request
   "Review the auth.py file for security issues"
       ↓
2. Review Execution
   task(subagent_type="general", description="Security review")
       ↓
3. Report
   Returns structured review with:
   - Critical issues
   - Warnings
   - Suggestions
   - Positive notes
```

**Example Request:**
```
Review src/core/auth.py for:
- Security vulnerabilities
- Best practices
- Performance issues
```

**Expected Output:**
```markdown
## Code Review: src/core/auth.py

### Critical Issues 🔴
1. **[auth.py:42]** Hardcoded secret key
   - Fix: Use environment variable

### Warnings 🟡
1. **[auth.py:15]** No rate limiting on login
   - Recommendation: Add SlowAPI rate limiter

### Suggestions 🟢
1. **[auth.py:30]** Consider using passlib with bcrypt

### Positive Notes ✅
- Good use of type hints
- Proper exception handling
```

### Testing

**Test-writing workflow:**

```
1. User Request
   "Write tests for the user service"
       ↓
2. Skill Loading
   skill(name="python-testing-general")
       ↓
3. Test Creation
   task(subagent_type="general", description="Write tests")
       ↓
4. Verification
   uv run pytest --cov=src/services/user.py
```

**Example Request:**
```
Write tests for src/services/user.py:
- Unit tests for create_user
- Unit tests for get_user
- Edge case testing
- Mock database
```

**Expected Output:**
```markdown
## Tests Created

### Test File
- `tests/services/test_user.py`

### Test Cases
1. `test_create_user_success`
2. `test_create_user_duplicate_email`
3. `test_get_user_found`
4. `test_get_user_not_found`

### Coverage: 92%
```

### Refactoring

**Safe refactoring workflow:**

```
1. User Request
   "Refactor user service to use repository pattern"
       ↓
2. Context Discovery
   task(subagent_type="explore", description="Find current patterns")
       ↓
3. Planning
   skill(name="python-backend")
   Analyze current implementation
       ↓
4. Implementation
   task(subagent_type="general", description="Refactor to repository")
       ↓
5. Verification
   uv run pytest  # Ensure nothing broke
   uv run mypy src/
```

---

## Best Practices

### For Users

1. **Be Specific**
   ```
   # Bad
   "Create an endpoint"
   
   # Good
   "Create a POST /api/users endpoint that:
    - Accepts email and password
    - Validates email format
    - Hashes password with bcrypt
    - Returns 201 with user ID on success"
   ```

2. **Mention Files**
   ```
   "Update src/services/auth.py to add rate limiting"
   ```

3. **Specify Constraints**
   ```
   "Must work with PostgreSQL 16"
   "Should be backwards compatible"
   "Keep under 50 lines"
   ```

4. **Request Review**
   ```
   "After implementation, review for security issues"
   ```

### For Agent Behavior

1. **Always Load Skills First**
   ```python
   skill(name="python-fastapi")
   # Then proceed with task
   ```

2. **Use Scout for Context**
   ```python
   # Before implementing
   task(subagent_type="explore", description="Find patterns")
   ```

3. **Verify After Implementation**
   ```bash
   uv run pytest
   uv run mypy src/
   uv run ruff check .
   ```

4. **Document Changes**
   ```markdown
   ### Files Modified
   - `src/api/users.py` - Added registration endpoint
   ```

---

## Context Management

### Fresh Sessions

For complex tasks, consider starting fresh:

1. **Maximizes inference quality** - Full context window available
2. **Reduces token usage** - No accumulated context
3. **Avoids context pollution** - Clean slate

### Context Compression

The system automatically compresses context by:
- Extracting essential information from skills
- Summarizing findings from subagents
- Focusing on actionable details
- Preserving file references

### When to Start Fresh

- Starting a new feature implementation
- After completing a major task
- When context seems confused
- For complex multi-step workflows

---

## Common Patterns

### Pattern 1: End-to-End Feature

```
Request → Scout → Load Skills → Coder → Test → Review
```

### Pattern 2: Quick Fix

```
Request → Load Skill → Direct Answer → Verify
```

### Pattern 3: Investigation

```
Request → Scout → Analyze → Report
```

### Pattern 4: Code Quality

```
Request → Reviewer → Report → (Optional) Coder to Fix
```

---

## Development Commands

Reference commands used throughout workflows:

```bash
# Package management
uv sync                    # Install dependencies
uv add fastapi             # Add dependency
uv add --dev pytest        # Add dev dependency

# Running
uv run uvicorn app.main:app --reload

# Testing
uv run pytest
uv run pytest tests/path/to/test_file.py -v
uv run pytest --cov=src

# Linting & Type Checking
uv run ruff check .
uv run ruff format .
uv run mypy src/

# Database
uv run alembic revision --autogenerate -m "description"
uv run alembic upgrade head
```

---

## Related Documentation

- [Overview](./overview.md) - System introduction
- [Architecture](./architecture.md) - Component relationships
- [Agents](./agents.md) - python-expert agent details
- [Subagents](./subagents.md) - All subagent workflows
- [Skills](./skills.md) - Complete skill documentation
- [Tutorials](./tutorials.md) - Step-by-step guides
