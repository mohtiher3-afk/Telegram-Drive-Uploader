# AGENTS.md - OpenCode Configuration

This project uses the Python Expert Agent pack for OpenCode.

## Project Info

| Field | Value |
|-------|-------|
| Type | Python |
| Framework | FastAPI |
| Python Version | 3.13+ |

## Available Skills

| Skill | Triggers | Purpose |
|-------|----------|---------|
| python-fundamentals | `*.py`, `python`, `dataclass` | Core Python patterns |
| python-fastapi | `fastapi`, `pydantic`, `endpoint` | FastAPI production patterns |
| python-backend | `sqlalchemy`, `database`, `orm` | SQLAlchemy 2.0 async |
| python-testing-general | `pytest`, `test`, `mock` | pytest fundamentals |
| python-testing-deep | `hypothesis`, `property-based` | Advanced testing |
| python-asyncio | `async`, `await`, `asyncio` | Async patterns |
| python-type-hints | `typing`, `mypy`, `pyright` | Type system |
| python-package-management | `uv`, `pip`, `pyproject` | UV package manager |
| python-tooling | `docker`, `ci`, `cd` | DevOps/CI-CD |
| python-fundamentals-313 | `3.13`, `jit`, `free-threading` | Python 3.13+ features |

## Usage

```
skill(name="python-fastapi")
```

## Subagents

| Subagent | Use For |
|----------|---------|
| python-coder | Code generation |
| python-reviewer | Code review |
| python-tester | Writing tests |
| python-scout | Finding context |

## Configuration

Main config: `.opencode/config.json`

```json
{
  "agent": "python-expert"
}
```

## Resources

- Skills: `.opencode/skills/*/SKILL.md`
- Standards: `.opencode/context/python/standards.md`
- Patterns: `.opencode/context/python/patterns.md`
- Security: `.opencode/context/python/security.md`

## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

When the user types `/graphify`, use the installed graphify skill or instructions before doing anything else.

Rules:
- For codebase questions, first run `graphify query "<question>"` when graphify-out/graph.json exists. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- Dirty graphify-out/ files are expected after hooks or incremental updates; dirty graph files are not a reason to skip graphify. Only skip graphify if the task is about stale or incorrect graph output, or the user explicitly says not to use it.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).
