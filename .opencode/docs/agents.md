# Agents

This document describes the primary agent configuration for this project.

---

## Primary Agent: python-expert

The `python-expert` agent is the main agent for Python development in this project.

### Configuration

**File**: `.opencode/agent/python-expert.md`

```yaml
---
name: python-expert
description: "Python expert agent for Python projects"
mode: primary
---
```

| Field | Value | Description |
|-------|-------|-------------|
| name | `python-expert` | Agent identifier |
| description | Python expert agent | Brief description for agent selection |
| mode | `primary` | Main interactive agent |

### Activation Protocol

The agent activates when:
1. Python files (`.py`) exist in the project
2. Python-related keywords are detected in requests
3. Selected via `config.json`: `{ "agent": "python-expert" }`

---

## Skill Loading Protocol

**CRITICAL**: Skills must be explicitly invoked via the `skill()` tool. There is no auto-loading.

### Keyword Detection Table

When these keywords/patterns are detected, invoke the corresponding skill:

| Keyword/Pattern | Skill to Invoke |
|-----------------|-----------------|
| `*.py` files, `python`, `dataclass` | `skill(name="python-fundamentals")` |
| `fastapi`, `pydantic`, `endpoint`, `api` | `skill(name="python-fastapi")` |
| `sqlalchemy`, `database`, `orm`, `migration` | `skill(name="python-backend")` |
| `pytest`, `test`, `mock`, `fixture` | `skill(name="python-testing-general")` |
| `async`, `await`, `asyncio`, `concurrent` | `skill(name="python-asyncio")` |
| `type hint`, `typing`, `mypy`, `pyright` | `skill(name="python-type-hints")` |
| `uv`, `pip`, `package`, `pyproject` | `skill(name="python-package-management")` |
| `docker`, `ci`, `cd`, `github actions` | `skill(name="python-tooling")` |

### Session Initialization

On first interaction in a Python project:

```
1. Invoke skill(name="python-fundamentals") to load core patterns
2. Scan project for frameworks (FastAPI, SQLAlchemy, etc.)
3. Load additional skills based on detected frameworks
```

### Skill Invocation Example

```python
# Correct way to load a skill
skill(name="python-fastapi")

# Multiple skills can be loaded for complex tasks
skill(name="python-fastapi")
skill(name="python-backend")
skill(name="python-asyncio")
```

---

## Task Complexity Detection

### Simple Tasks (Answer Directly)

**Indicators**:
- Keywords: `what is`, `how to`, `explain`, `show me`, `example`
- Single concept explanations
- Quick references
- Documentation questions

**Action**: Answer directly using loaded skill context.

**Example**:
```
User: "How do I create a Pydantic model with validation?"
Agent: [Loads python-fastapi skill] → Provides direct answer with code example
```

### Complex Tasks (Delegate to Subagent)

**Indicators**:
- Keywords: `create`, `build`, `implement`, `design`, `refactor`, `convert`, `migrate`
- Multi-file changes
- Architecture decisions
- System design
- Full feature implementation

**Action**: Delegate to appropriate subagent via Task tool.

**Example**:
```
User: "Create a user authentication system with JWT tokens"
Agent: [Loads skills] → Delegates to python-coder subagent
```

---

## Delegation Rules

### Delegation Table

| Task Type | Subagent | Task Tool Usage |
|-----------|----------|-----------------|
| Code generation | python-coder | `task(subagent_type="general", ...)` |
| Code review | python-reviewer | `task(subagent_type="general", ...)` |
| Test writing | python-tester | `task(subagent_type="general", ...)` |
| Codebase exploration | python-scout | `task(subagent_type="explore", ...)` |

### Delegation Format

```python
task(
    subagent_type="general",  # Use "explore" for python-scout
    description="Brief task description",
    prompt="""Detailed instructions including:
    - Context from loaded skills
    - Specific files to work with
    - Acceptance criteria
    - Expected output format
    
    Additional context: [relevant skill patterns]
    Files to modify: [specific paths]
    Requirements: [detailed list]
    """
)
```

### Delegation Best Practices

1. **Include skill context**: Summarize relevant patterns from loaded skills
2. **Specify files**: List exact file paths to work with
3. **Define acceptance criteria**: Clear success conditions
4. **Request output format**: How results should be presented

---

## Response Format

### For Simple Queries

```
1. Provide direct answer with code examples if applicable
2. Reference relevant skill patterns
3. Keep responses concise
4. Include links to related context files
```

**Example Response**:
```markdown
To create a Pydantic model with validation:

\`\`\`python
from pydantic import BaseModel, Field, EmailStr

class UserCreate(BaseModel):
    email: EmailStr
    name: str = Field(min_length=1, max_length=100)
    age: int | None = Field(None, ge=0, le=150)
\`\`\`

See `.opencode/skills/python-fastapi/SKILL.md` for more patterns.
```

### For Complex Tasks (After Delegation)

```
1. Confirm delegation to subagent
2. Summarize what was accomplished
3. List files created/modified
4. Provide verification steps
5. Note any follow-up actions
```

**Example Response**:
```markdown
## Task Completed

Delegated to python-coder for implementation.

### Files Created
- `src/api/v1/endpoints/users.py` - User CRUD endpoints
- `src/schemas/user.py` - Pydantic schemas
- `src/services/user.py` - Business logic

### Verification Steps
1. Run: `uv run pytest tests/api/test_users.py`
2. Check: `uv run mypy src/`

### Next Steps
- Add integration tests
- Update API documentation
```

---

## Core Capabilities

### Python 3.13+ Features

| Feature | Description |
|---------|-------------|
| Free-threaded mode | No-GIL for true parallelism (experimental) |
| JIT compiler | 5-30% speedups (experimental) |
| Pattern matching | Match statements with guards |
| Type parameter syntax | Generic classes without TypeVar |
| Improved error messages | Better debugging experience |

### FastAPI

| Capability | Patterns |
|------------|----------|
| Async request handling | `async def` endpoints |
| Pydantic v2 | `BaseModel`, `Field`, validators |
| Dependency injection | `Annotated[T, Depends(...)]` |
| JWT authentication | `python-jose`, `passlib` |
| Production deployment | Gunicorn + Uvicorn workers |

### SQLAlchemy 2.0

| Capability | Patterns |
|------------|----------|
| Async sessions | `AsyncSession`, `create_async_engine` |
| Modern select() | `select(Model).where(...)` |
| Relationship patterns | `relationship()`, `back_populates` |
| Migrations | Alembic integration |

### Testing

| Capability | Tools |
|------------|-------|
| Fixtures | `@pytest.fixture`, `conftest.py` |
| Parametrization | `@pytest.mark.parametrize` |
| Async testing | `pytest-asyncio`, `@pytest.mark.asyncio` |
| Mocking | `pytest-mock`, `unittest.mock` |
| Coverage | `pytest-cov` |

---

## Guidelines

### Code Quality

1. **Type Everything**: Use type hints for all functions, prefer built-in generics
   ```python
   def process(items: list[str]) -> dict[str, int]: ...
   ```

2. **Async by Default**: Use async for I/O-bound operations
   ```python
   async def fetch_data(url: str) -> dict: ...
   ```

3. **Modern Python**: Target Python 3.11+ minimum
   ```python
   # Use modern union syntax
   def get_user(id: int) -> User | None: ...
   ```

### Security

1. **Validate inputs**: Always validate user input with Pydantic
2. **Parameterized queries**: Never use f-strings in SQL
3. **Hash passwords**: Use `passlib` with bcrypt
4. **No secrets in code**: Use environment variables

### Testing

1. **Write tests for new code**: Aim for 80%+ coverage
2. **Use fixtures**: Isolate test data
3. **Test edge cases**: Empty inputs, None values, boundaries
4. **Async tests**: Use `@pytest.mark.asyncio`

---

## Context Navigation

The agent can reference project context files:

| Need | Path |
|------|------|
| Code standards | `.opencode/context/python/standards.md` |
| Common patterns | `.opencode/context/python/patterns.md` |
| Security patterns | `.opencode/context/python/security.md` |
| Quick reference | `.opencode/context/navigation.md` |

Use `python-scout` subagent to discover relevant context for specific tasks.

---

## Agent Frontmatter Reference

```yaml
---
name: python-expert              # Required: Agent identifier
description: "Description"       # Required: For agent selection UI
mode: primary                    # Required: primary | subagent
temperature: 0.1                 # Optional: Response randomness (0-1)
model: "provider/model-id"       # Optional: Override default model
tools:                           # Optional: Tool access control
  read: true
  write: true
  edit: true
  bash: true
  skill: true
permission:                      # Optional: Permission overrides
  edit: ask
  bash:
    "*": ask
    "git status": allow
---
```

---

## Related Documentation

- [Overview](./overview.md) - System introduction
- [Architecture](./architecture.md) - Component relationships
- [Subagents](./subagents.md) - All subagent workflows
- [Skills](./skills.md) - Complete skill documentation
- [Configuration](./configuration.md) - Customization guide
