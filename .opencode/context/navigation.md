# Context Navigation

Quick reference for finding relevant context and skills.

## Structure

```
.opencode/
├── context/
│   ├── navigation.md          # This file
│   └── python/
│       ├── standards.md       # Code quality standards
│       ├── patterns.md        # Common patterns
│       └── security.md        # Security patterns
├── skills/
│   ├── python-fundamentals/   # Core Python patterns
│   ├── python-fastapi/        # FastAPI patterns
│   ├── python-backend/        # Backend/DB patterns
│   ├── python-testing-general/# Testing patterns
│   ├── python-testing-deep/   # Advanced testing
│   ├── python-asyncio/        # Async patterns
│   ├── python-type-hints/     # Type system
│   ├── python-package-management/ # UV/pip/venv
│   ├── python-tooling/        # Docker/CI/CD
│   └── python-fundamentals-313/ # Python 3.13+ features
├── agent/
│   └── python-expert.md       # Main agent
└── subagents/
    ├── python-coder.md        # Code generation
    ├── python-reviewer.md     # Code review
    ├── python-tester.md       # Testing
    └── python-scout.md        # Context discovery
```

## Quick Routes

| Task | Context | Skill |
|------|---------|-------|
| Write FastAPI endpoint | `python/standards.md` | `python-fastapi` |
| Write database model | `python/patterns.md` | `python-backend` |
| Write tests | `python/standards.md` | `python-testing-general` |
| Add async code | `python/patterns.md` | `python-asyncio` |
| Add type hints | `python/standards.md` | `python-type-hints` |
| Security review | `python/security.md` | - |
| Set up CI/CD | - | `python-tooling` |
| Package setup | - | `python-package-management` |

## Skill Activation Map

Invoke skills based on keywords in your request:

| Trigger Keywords | Skill to Invoke |
|------------------|-----------------|
| `fastapi`, `pydantic`, `endpoint`, `api`, `router` | `skill(name="python-fastapi")` |
| `sqlalchemy`, `database`, `orm`, `migration`, `model` | `skill(name="python-backend")` |
| `pytest`, `test`, `mock`, `fixture`, `coverage` | `skill(name="python-testing-general")` |
| `hypothesis`, `snapshot`, `property-based` | `skill(name="python-testing-deep")` |
| `async`, `await`, `asyncio`, `concurrent`, `gather` | `skill(name="python-asyncio")` |
| `type hint`, `typing`, `Protocol`, `mypy`, `pyright` | `skill(name="python-type-hints")` |
| `uv`, `pip`, `package`, `dependency`, `pyproject` | `skill(name="python-package-management")` |
| `docker`, `ci`, `cd`, `github`, `profile`, `optimize` | `skill(name="python-tooling")` |
| `3.13`, `free-threading`, `jit`, `pattern matching` | `skill(name="python-fundamentals-313")` |

## Subagent Delegation

| Task Type | Subagent | When to Use |
|-----------|----------|-------------|
| Code generation | `python-coder` | Creating new files, implementing features |
| Code review | `python-reviewer` | Reviewing existing code, quality audit |
| Test writing | `python-tester` | Writing unit/integration tests |
| Context discovery | `python-scout` | Finding relevant files and patterns |

## Workflow

1. **Start**: Load `python-fundamentals` skill
2. **Detect**: Identify keywords in request
3. **Load**: Invoke relevant skills
4. **Decide**: Simple query → answer directly; Complex task → delegate
5. **Execute**: Follow skill patterns, verify with tests/linting
