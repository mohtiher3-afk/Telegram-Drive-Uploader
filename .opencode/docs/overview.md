# Overview

This documentation explains how the `.opencode` system works for this Python backend project.

## What is `.opencode`

The `.opencode` directory contains configuration, agents, subagents, skills, and context that enable OpenCode to work effectively with this codebase. It provides:

- **Specialized Agents** - Primary and subagents tuned for Python/FastAPI development
- **Reusable Skills** - On-demand knowledge modules loaded via `skill()` tool
- **Context Files** - Project standards, patterns, and security guidelines
- **Configuration** - Agent behavior and permission settings

For project-level information (tech stack, commands), see [AGENTS.md](../../AGENTS.md).

---

## Component Overview

### Agents

| Type | Name | Purpose |
|------|------|---------|
| Primary | python-expert | Main agent for Python development |
| Subagent | python-coder | Code generation and implementation |
| Subagent | python-reviewer | Code quality and security review |
| Subagent | python-tester | Test writing and coverage |
| Subagent | python-scout | Context discovery and file finding |

### Skills

Skills are loaded on-demand via the `skill()` tool. Each skill contains domain-specific patterns, examples, and best practices.

| Skill | Triggers | Purpose |
|-------|----------|---------|
| python-fundamentals | `*.py`, `python`, `dataclass` | Core Python patterns |
| python-fastapi | `fastapi`, `pydantic`, `endpoint` | API development |
| python-backend | `sqlalchemy`, `database`, `orm` | Database work |
| python-testing-general | `pytest`, `test`, `mock` | Testing patterns |
| python-testing-deep | `hypothesis`, `property-based` | Advanced testing |
| python-asyncio | `async`, `await`, `asyncio` | Async programming |
| python-type-hints | `typing`, `mypy`, `pyright` | Type system |
| python-package-management | `uv`, `pip`, `pyproject` | Dependencies |
| python-tooling | `docker`, `ci`, `cd` | DevOps tooling |
| python-fundamentals-313 | `3.13`, `jit`, `free-threading` | Python 3.13+ features |

### Context Files

Located in `.opencode/context/`:

| File | Purpose |
|------|---------|
| navigation.md | Quick reference for finding context |
| python/standards.md | Code quality standards |
| python/patterns.md | Common implementation patterns |
| python/security.md | Security patterns and guidelines |

---

## How It Works

### Session Flow

```
1. User opens project
       ↓
2. OpenCode reads config.json → Loads python-expert agent
       ↓
3. Agent detects .py files → Invokes skill(name="python-fundamentals")
       ↓
4. User request: "Create a FastAPI endpoint"
       ↓
5. Agent detects "fastapi" → Invokes skill(name="python-fastapi")
       ↓
6. Complex task? → Delegates to python-coder via task(subagent_type="general", ...)
       ↓
7. Implementation follows loaded skill patterns
```

### Skill Loading

Skills are NOT auto-loaded. They must be explicitly invoked:

```python
skill(name="python-fastapi")
```

The agent determines which skills to load based on:
1. Keywords in the user request
2. File patterns being worked with
3. Task type (API, database, testing, etc.)

### Subagent Delegation

Complex tasks are delegated to specialized subagents:

```python
task(
    subagent_type="general",  # or "explore" for python-scout
    description="Create user authentication endpoint",
    prompt="Detailed instructions including context and requirements..."
)
```

---

## Quick Start

### For Users

1. **Simple Queries**: Ask directly - the agent will load appropriate skills
   ```
   "How do I create a FastAPI endpoint with authentication?"
   ```

2. **Complex Tasks**: Be specific about requirements
   ```
   "Create a user registration endpoint with email validation, 
   password hashing, and JWT token generation"
   ```

3. **Code Review**: Request review explicitly
   ```
   "Review the auth.py file for security issues"
   ```

### For Configuration

1. **Change Default Agent**: Edit `.opencode/config.json`
   ```json
   { "agent": "python-expert" }
   ```

2. **Add Custom Skill**: Create `.opencode/skills/my-skill/SKILL.md`

3. **Modify Agent Behavior**: Edit `.opencode/agent/python-expert.md`

---

## Navigation

| Document | Content |
|----------|---------|
| [Architecture](./architecture.md) | Directory structure, component relationships |
| [Agents](./agents.md) | python-expert agent details |
| [Subagents](./subagents.md) | All subagent workflows |
| [Skills](./skills.md) | Complete skill documentation |
| [Workflow](./workflow.md) | Development patterns |
| [Configuration](./configuration.md) | Customization guide |
| [Tutorials](./tutorials.md) | Step-by-step guides |
| [Troubleshooting](./troubleshooting.md) | FAQ and debugging |

---

## Related Files

- **Project Context**: [AGENTS.md](../../AGENTS.md) - Tech stack, commands, quick reference
- **Main Agent**: [.opencode/agent/python-expert.md](../agent/python-expert.md)
- **Agent Registry**: [.opencode/config/agent-metadata.json](../config/agent-metadata.json)
- **Navigation**: [.opencode/context/navigation.md](../context/navigation.md)
