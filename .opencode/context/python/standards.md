# Python Code Standards

Code quality standards for python-backend project.

## Type Hints

### Required
- All functions must have type hints
- Use built-in generics (Python 3.10+)
- Use `|` for unions instead of `Union`

```python
def process(items: list[str]) -> dict[str, int]:
    ...

async def fetch(url: str) -> dict | None:
    ...
```

### Modern Syntax (3.12+)

```python
def first[T](items: list[T]) -> T:
    return items[0]

type Point = tuple[float, float]
```

## Naming Conventions

| Type | Style | Example |
|------|-------|---------|
| Module | `snake_case` | `user_service.py` |
| Class | `PascalCase` | `UserService` |
| Function | `snake_case` | `get_user_by_id()` |
| Variable | `snake_case` | `user_count` |
| Constant | `UPPER_SNAKE` | `MAX_RETRIES` |
| Exception | `PascalCase` + Error | `ValidationError` |

## Imports

Three groups, separated by blank lines, alphabetically sorted:

```python
# 1. Standard library
import json
from pathlib import Path

# 2. Third-party
import httpx
from fastapi import APIRouter

# 3. Local
from app.models import User
from app.utils import format_date
```

## Error Handling

### Do
```python
try:
    config = parse_config(path)
except FileNotFoundError:
    config = default_config()
except json.JSONDecodeError as e:
    raise ConfigError(f"Invalid JSON in {path}") from e
```

### Don't
```python
try:
    ...
except:  # Bare except
    pass
```

## Code Organization

### Function Length
- Maximum 40 lines per function
- Cyclomatic complexity < 10

### Class Design
- Prefer composition over inheritance
- Single responsibility principle
- Use dataclasses for data containers

### File Structure
```python
"""Module docstring."""

# Imports
import ...

# Constants
MAX_ITEMS = 100

# Classes
class UserService:
    ...

# Functions
def get_user():
    ...

# Private helpers
def _internal_helper():
    ...
```

## Documentation

### Docstrings (Google Style)

```python
def fetch_users(
    filters: dict[str, str],
    limit: int = 100,
) -> list[User]:
    """Fetch users matching the given filters.

    Args:
        filters: Key-value pairs for filtering.
        limit: Maximum number of users to return.

    Returns:
        List of User objects matching criteria.

    Raises:
        DatabaseError: If connection fails.
    """
```

## Testing Standards

### Coverage
- 80%+ line coverage for new code
- 100% coverage for critical paths (auth, payments)

### Test File Location
- Source: `src/auth/service.py`
- Test: `tests/auth/test_service.py`

### Test Patterns
```python
def test_function_happy_path():
    # Arrange
    data = {"key": "value"}
    
    # Act
    result = process(data)
    
    # Assert
    assert result.status == "success"
```

## Code Quality Tools

```bash
# Type checking
uv run mypy src/

# Linting
uv run ruff check .

# Formatting
uv run ruff format .

# Testing
uv run pytest --cov=src
```
