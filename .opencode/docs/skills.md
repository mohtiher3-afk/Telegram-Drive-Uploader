# Skills

This document describes all skills available in the `.opencode` system.

---

## Overview

Skills are reusable knowledge modules loaded on-demand via the `skill()` tool. Each skill contains domain-specific patterns, examples, and best practices.

### Skill Loading Protocol

**CRITICAL**: Skills must be explicitly invoked. There is no auto-loading.

```python
skill(name="python-fastapi")
```

### Skill Structure

Each skill is located in `.opencode/skills/<name>/SKILL.md`:

```
skills/
└── python-fastapi/
    ├── SKILL.md              # Skill definition
    └── references/           # Optional additional content
        └── dependency-injection-patterns.md
```

### SKILL.md Format

```yaml
---
name: python-fastapi              # Required: must match folder name
description: "Description here"   # Required: 1-1024 characters
license: MIT                      # Optional
compatibility: opencode           # Optional
metadata:                         # Optional: string-to-string map
  audience: developers
  workflow: api
---

# Skill Content
...
```

---

## Skills Reference

### Quick Reference Table

| Skill | Triggers | When to Use |
|-------|----------|-------------|
| python-fundamentals | `*.py`, `python`, `dataclass`, `async` | Core Python patterns |
| python-fastapi | `fastapi`, `pydantic`, `endpoint`, `api` | API development |
| python-backend | `sqlalchemy`, `database`, `orm`, `migration` | Database work |
| python-testing-general | `pytest`, `test`, `mock`, `fixture` | Testing patterns |
| python-testing-deep | `hypothesis`, `property-based`, `snapshot` | Advanced testing |
| python-asyncio | `async`, `await`, `asyncio`, `concurrent` | Async programming |
| python-type-hints | `typing`, `mypy`, `pyright`, `Protocol` | Type system |
| python-package-management | `uv`, `pip`, `package`, `pyproject` | Dependencies |
| python-tooling | `docker`, `ci`, `cd`, `github`, `profile` | DevOps tooling |
| python-fundamentals-313 | `3.13`, `jit`, `free-threading` | Python 3.13+ features |

---

## python-fundamentals

**Location**: `.opencode/skills/python-fundamentals/SKILL.md`

### Triggers

- `*.py` files
- `python`, `dataclass`, `async`
- `type hint`

### When to Use

- Writing new Python code
- Applying type annotations
- Working with dataclasses, enums, or Pydantic
- Implementing error handling patterns
- Using async/await
- Handling file I/O with pathlib
- Following naming conventions

### Key Content Areas

#### Type Annotations (Python 3.10+)

```python
# Built-in generics
items: list[str]
mapping: dict[str, int]
optional: str | None

# Union syntax
def fetch(url: str) -> dict | None:
    ...

# Type parameter syntax (3.12+)
def first[T](items: list[T]) -> T:
    return items[0]
```

#### Dataclasses

```python
from dataclasses import dataclass, field

@dataclass(slots=True)
class User:
    name: str
    email: str
    tags: list[str] = field(default_factory=list)
```

#### Error Handling

```python
try:
    config = parse_config(path)
except FileNotFoundError:
    config = default_config()
except json.JSONDecodeError as e:
    raise ConfigError(f"Invalid JSON in {path}") from e
```

#### Async Programming

```python
# Concurrent execution
results = await asyncio.gather(
    fetch("url1"),
    fetch("url2"),
)

# Task groups (3.11+)
async with asyncio.TaskGroup() as tg:
    for item in items:
        tg.create_task(process_item(item))
```

#### Pattern Matching (3.10+)

```python
def process_command(command: dict) -> str:
    match command:
        case {"action": "create", "name": str(name)}:
            return f"Creating {name}"
        case {"action": "delete", "id": int(id_)}:
            return f"Deleting item {id_}"
        case _:
            return "Unknown command"
```

---

## python-fastapi

**Location**: `.opencode/skills/python-fastapi/SKILL.md`

### Triggers

- `fastapi`, `pydantic`, `uvicorn`
- `endpoint`, `api`, `router`

### When to Use

- Setting up FastAPI project structure
- Creating Pydantic schemas with validation
- Implementing dependency injection
- JWT authentication setup
- Docker deployment configuration
- Production deployment

### Key Content Areas

#### Project Structure

```
src/
├── app/
│   ├── main.py              # Application factory
│   ├── config.py            # Settings management
│   ├── api/v1/
│   │   ├── router.py        # API router
│   │   └── endpoints/       # Endpoint handlers
│   ├── models/              # SQLAlchemy models
│   ├── schemas/             # Pydantic schemas
│   └── services/            # Business logic
```

#### Pydantic Schemas

```python
# Base schema with shared fields
class UserBase(BaseModel):
    email: EmailStr
    full_name: str | None = None

# Input schema for creation
class UserCreate(UserBase):
    password: str = Field(min_length=8)

# Input schema for updates
class UserUpdate(BaseModel):
    email: EmailStr | None = None
    full_name: str | None = None

# Output schema (what API returns)
class UserResponse(UserBase):
    id: int
    is_active: bool
    created_at: datetime

    model_config = {"from_attributes": True}
```

#### Dependency Injection

```python
from typing import Annotated
from fastapi import Depends

# Type aliases for cleaner signatures
CurrentUser = Annotated[User, Depends(get_current_active_user)]
DbSession = Annotated[AsyncSession, Depends(get_db)]

@router.get("/me", response_model=UserResponse)
async def read_current_user(current_user: CurrentUser):
    return current_user
```

#### Configuration

```python
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env")
    
    app_name: str = "My API"
    database_url: str
    secret_key: str
```

#### Production Deployment

```bash
# Gunicorn with Uvicorn workers
gunicorn app.main:app -w 4 -k uvicorn.workers.UvicornWorker
```

### Additional References

- `references/dependency-injection-patterns.md` - Advanced DI patterns

---

## python-backend

**Location**: `.opencode/skills/python-backend/SKILL.md`

### Triggers

- `sqlalchemy`, `database`, `orm`
- `migration`, `alembic`, `model`

### When to Use

- Creating SQLAlchemy models
- Working with async sessions
- Database migrations with Alembic
- Setting up database connections

### Key Content Areas

#### Async Engine Setup

```python
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession

engine = create_async_engine(
    "postgresql+asyncpg://user:pass@localhost/db",
    echo=False,
    pool_size=5,
    max_overflow=10,
)

async_session = sessionmaker(
    engine, class_=AsyncSession, expire_on_commit=False
)
```

#### Model Definition

```python
from sqlalchemy import Column, Integer, String, DateTime
from sqlalchemy.orm import DeclarativeBase

class Base(DeclarativeBase):
    pass

class User(Base):
    __tablename__ = "users"
    
    id = Column(Integer, primary_key=True)
    email = Column(String, unique=True, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow)
```

#### Async Queries

```python
from sqlalchemy import select

async def get_user(db: AsyncSession, user_id: int) -> User | None:
    result = await db.execute(
        select(User).where(User.id == user_id)
    )
    return result.scalar_one_or_none()
```

#### Migrations

```bash
# Create migration
alembic revision --autogenerate -m "Add user table"

# Apply migrations
alembic upgrade head
```

---

## python-testing-general

**Location**: `.opencode/skills/python-testing-general/SKILL.md`

### Triggers

- `pytest`, `test`, `mock`
- `fixture`, `coverage`

### When to Use

- Writing unit tests
- Creating test fixtures
- Mocking dependencies
- Setting up test coverage

### Key Content Areas

#### Test Structure

```python
# tests/auth/test_service.py
import pytest
from unittest.mock import Mock

class TestUserService:
    def test_create_user_success(self):
        # Arrange
        user_data = {"email": "test@example.com"}
        mock_db = Mock()
        
        # Act
        result = create_user(user_data, mock_db)
        
        # Assert
        assert result.email == "test@example.com"
```

#### Fixtures

```python
# conftest.py
@pytest.fixture
def mock_db():
    db = Mock()
    yield db
    db.reset_mock()

@pytest.fixture
def sample_user():
    return User(id=1, email="test@example.com")
```

#### Parametrization

```python
@pytest.mark.parametrize("input,expected", [
    ("valid@email.com", True),
    ("invalid", False),
    ("", False),
])
def test_validate_email(input, expected):
    assert validate_email(input) == expected
```

#### Async Tests

```python
@pytest.mark.asyncio
async def test_async_fetch():
    result = await fetch_data("https://api.example.com")
    assert result is not None
```

---

## python-testing-deep

**Location**: `.opencode/skills/python-testing-deep/SKILL.md`

### Triggers

- `hypothesis`, `property-based`
- `snapshot`, `mutation`

### When to Use

- Property-based testing
- Snapshot testing
- Advanced test scenarios
- Comprehensive coverage

### Key Content Areas

#### Property-Based Testing

```python
from hypothesis import given, strategies as st

@given(st.text(), st.integers())
def test_process_never_crashes(text, number):
    result = process(text, number)
    assert result is not None
```

#### Snapshot Testing

```python
def test_api_response(snapshot):
    response = client.get("/api/users")
    snapshot.assert_match(response.json())
```

---

## python-asyncio

**Location**: `.opencode/skills/python-asyncio/SKILL.md`

### Triggers

- `async`, `await`, `asyncio`
- `concurrent`, `gather`, `TaskGroup`

### When to Use

- Writing async code
- Concurrent execution
- Rate limiting with semaphores
- Timeout handling

### Key Content Areas

#### Concurrent Execution

```python
# Sequential (slow)
result1 = await fetch("url1")
result2 = await fetch("url2")

# Concurrent (fast)
results = await asyncio.gather(
    fetch("url1"),
    fetch("url2"),
)
```

#### Task Groups (3.11+)

```python
async def process_all(items: list[Item]):
    async with asyncio.TaskGroup() as tg:
        for item in items:
            tg.create_task(process_item(item))
```

#### Rate Limiting

```python
async def fetch_all(urls: list[str], max_concurrent: int = 10):
    semaphore = asyncio.Semaphore(max_concurrent)
    
    async def fetch_one(url: str):
        async with semaphore:
            return await fetch(url)
    
    return await asyncio.gather(*[fetch_one(url) for url in urls])
```

#### Timeouts

```python
async def fetch_with_timeout(url: str, timeout: float = 5.0):
    async with asyncio.timeout(timeout):
        return await fetch(url)
```

---

## python-type-hints

**Location**: `.opencode/skills/python-type-hints/SKILL.md`

### Triggers

- `typing`, `mypy`, `pyright`
- `Protocol`, `TypedDict`

### When to Use

- Adding type hints
- Using Protocol for structural typing
- Creating TypedDict schemas
- Configuring mypy/pyright

### Key Content Areas

#### Protocols

```python
from typing import Protocol

class Readable(Protocol):
    def read(self) -> str: ...

def process(source: Readable) -> None:
    content = source.read()
```

#### TypedDict

```python
from typing import TypedDict, NotRequired

class UserData(TypedDict):
    name: str
    email: str
    age: NotRequired[int]
```

#### Type Parameter Syntax (3.12+)

```python
type Point = tuple[float, float]
type Vector[T] = list[T]

class Stack[T]:
    def __init__(self) -> None:
        self._items: list[T] = []
```

### Additional References

- `references/advanced-typing-patterns.md` - Advanced patterns

---

## python-package-management

**Location**: `.opencode/skills/python-package-management/SKILL.md`

### Triggers

- `uv`, `pip`, `package`
- `pyproject`, `dependency`, `venv`

### When to Use

- Managing dependencies with UV
- Configuring pyproject.toml
- Setting up virtual environments
- Publishing packages

### Key Content Areas

#### UV Commands

```bash
uv sync                    # Install dependencies
uv add fastapi             # Add dependency
uv add --dev pytest        # Add dev dependency
uv remove package          # Remove dependency
uv run command             # Run in virtual environment
```

#### pyproject.toml

```toml
[project]
name = "my-project"
version = "0.1.0"
requires-python = ">=3.11"
dependencies = [
    "fastapi>=0.115.0",
    "pydantic>=2.7.0",
]

[project.optional-dependencies]
dev = ["pytest", "mypy", "ruff"]
```

---

## python-tooling

**Location**: `.opencode/skills/python-tooling/SKILL.md`

### Triggers

- `docker`, `ci`, `cd`
- `github`, `profile`, `optimize`

### When to Use

- Docker configuration
- CI/CD pipeline setup
- Performance profiling
- Build optimization

### Key Content Areas

#### Dockerfile

```dockerfile
FROM python:3.12-slim
WORKDIR /app

COPY --from=ghcr.io/astral-sh/uv:latest /uv /bin/uv
COPY pyproject.toml uv.lock ./
RUN uv sync --frozen --no-dev

COPY src/ ./src/
CMD ["uv", "run", "uvicorn", "app.main:app", "--host", "0.0.0.0"]
```

#### CI/CD

```yaml
# .github/workflows/test.yml
name: Test
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: uv sync
      - run: uv run pytest
      - run: uv run mypy src/
```

---

## python-fundamentals-313

**Location**: `.opencode/skills/python-fundamentals-313/SKILL.md`

### Triggers

- `3.13`, `free-threading`, `jit`
- `pattern matching`, `no-gil`

### When to Use

- Using Python 3.13+ specific features
- Free-threaded mode (no-GIL)
- JIT compiler
- New pattern matching features

### Key Content Areas

#### Free-Threaded Mode

```python
# python3.13t - Free-threaded build
import threading

def cpu_bound_task(n):
    total = 0
    for i in range(n):
        total += i * i
    return total

# True parallelism with free-threading
threads = []
for _ in range(4):
    t = threading.Thread(target=cpu_bound_task, args=(10_000_000,))
    threads.append(t)
    t.start()
```

#### JIT Compiler

```bash
# Enable JIT
PYTHON_JIT=1 python3.13 script.py

# Or use -X flag
python3.13 -X jit script.py
```

---

## Skill Permissions

Control which skills agents can access in `opencode.json`:

```json
{
  "permission": {
    "skill": {
      "*": "allow",
      "internal-*": "deny",
      "experimental-*": "ask"
    }
  }
}
```

| Permission | Behavior |
|------------|----------|
| `allow` | Skill loads immediately |
| `deny` | Skill hidden from agent |
| `ask` | User prompted before loading |

---

## Troubleshooting Skills

If a skill doesn't appear:

1. Verify `SKILL.md` is spelled in all caps
2. Check frontmatter includes `name` and `description`
3. Ensure skill names are unique
4. Check permissions - skills with `deny` are hidden

---

## Related Documentation

- [Overview](./overview.md) - System introduction
- [Architecture](./architecture.md) - Component relationships
- [Agents](./agents.md) - python-expert agent details
- [Subagents](./subagents.md) - All subagent workflows
- [Workflow](./workflow.md) - Development patterns
- [Configuration](./configuration.md) - Customization guide
