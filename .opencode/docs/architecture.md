# Architecture

This document describes the structure and component relationships of the `.opencode` system.

---

## Directory Structure

```
.opencode/
├── opencode.json              # Schema reference for OpenCode
├── config.json                # Default agent selection
├── agent/
│   └── python-expert.md       # Primary agent definition
├── subagents/
│   ├── python-coder.md        # Code generation (type: general)
│   ├── python-reviewer.md     # Code review (type: general)
│   ├── python-tester.md       # Test writing (type: general)
│   └── python-scout.md        # Context discovery (type: explore)
├── skills/
│   ├── python-fundamentals/
│   │   └── SKILL.md           # Core Python patterns
│   ├── python-fastapi/
│   │   ├── SKILL.md           # FastAPI patterns
│   │   └── references/
│   │       └── dependency-injection-patterns.md
│   ├── python-backend/
│   │   └── SKILL.md           # SQLAlchemy/database patterns
│   ├── python-testing-general/
│   │   └── SKILL.md           # pytest patterns
│   ├── python-testing-deep/
│   │   └── SKILL.md           # Advanced testing
│   ├── python-asyncio/
│   │   └── SKILL.md           # Async programming
│   ├── python-type-hints/
│   │   ├── SKILL.md           # Type system
│   │   └── references/
│   │       └── advanced-typing-patterns.md
│   ├── python-package-management/
│   │   └── SKILL.md           # UV/pip/venv
│   ├── python-tooling/
│   │   └── SKILL.md           # Docker/CI/CD
│   └── python-fundamentals-313/
│       └── SKILL.md           # Python 3.13+ features
├── context/
│   ├── navigation.md          # Quick reference
│   └── python/
│       ├── standards.md       # Code quality standards
│       ├── patterns.md        # Common patterns
│       └── security.md        # Security patterns
├── config/
│   └── agent-metadata.json    # Agent/subagent/skill registry
└── docs/                      # This documentation
    ├── overview.md
    ├── architecture.md
    ├── agents.md
    ├── subagents.md
    ├── skills.md
    ├── workflow.md
    ├── configuration.md
    ├── tutorials.md
    └── troubleshooting.md
```

---

## Component Overview

### Configuration Files

| File | Purpose | Format |
|------|---------|--------|
| `config.json` | Selects default agent | JSON |
| `opencode.json` | OpenCode schema reference | JSON |
| `agent-metadata.json` | Registry of all agents, subagents, skills | JSON |

### Agents

| Location | Type | Description |
|----------|------|-------------|
| `agent/python-expert.md` | Primary | Main agent with skill loading protocol |
| `subagents/python-coder.md` | Subagent | Code generation (general) |
| `subagents/python-reviewer.md` | Subagent | Code review (general) |
| `subagents/python-tester.md` | Subagent | Testing (general) |
| `subagents/python-scout.md` | Subagent | Exploration (explore) |

### Skills

Each skill is a folder with `SKILL.md`:

```
skills/
└── <skill-name>/
    ├── SKILL.md              # Required: skill definition
    └── references/           # Optional: additional references
        └── <topic>.md
```

### Context

Context files provide project-specific standards:

```
context/
├── navigation.md             # Quick reference
└── python/
    ├── standards.md          # Code quality
    ├── patterns.md           # Implementation patterns
    └── security.md           # Security guidelines
```

---

## Component Relationships

```
┌─────────────────────────────────────────────────────────────────┐
│                         OpenCode System                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐      reads       ┌─────────────────┐          │
│  │  config.json │ ────────────────>│ python-expert   │          │
│  └──────────────┘                  │   (primary)     │          │
│                                    └────────┬────────┘          │
│                                             │                   │
│                         ┌───────────────────┼───────────────────┐
│                         │                   │                   │
│                         ▼                   ▼                   ▼
│               ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│               │ skill() tool │    │ task() tool  │    │ Read context │
│               └──────┬───────┘    └──────┬───────┘    └──────┬───────┘
│                      │                   │                   │
│                      ▼                   ▼                   ▼
│  ┌───────────────────────────────────────────────────────────────┐
│  │                        Skills (10)                             │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐              │
│  │  │fundamentals │ │  fastapi    │ │  backend    │ ...          │
│  │  └─────────────┘ └─────────────┘ └─────────────┘              │
│  └───────────────────────────────────────────────────────────────┘
│                      │                                           │
│                      ▼                                           │
│  ┌───────────────────────────────────────────────────────────────┐
│  │                     Subagents (4)                              │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌──────────┐ │
│  │  │   coder     │ │  reviewer   │ │   tester    │ │  scout   │ │
│  │  │  (general)  │ │  (general)  │ │  (general)  │ │ (explore)│ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘ └──────────┘ │
│  └───────────────────────────────────────────────────────────────┘
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Data Flow

### Request Processing Flow

```
User Request
     │
     ▼
┌─────────────────────────────────────────────────────────────────┐
│  python-expert (Primary Agent)                                   │
│                                                                  │
│  1. Parse request for keywords                                   │
│  2. Determine task complexity                                    │
│                                                                  │
│  Keyword Detection:                                              │
│  ┌─────────────────┬─────────────────────┐                      │
│  │ "fastapi"       │ → skill("python-fastapi")                  │
│  │ "sqlalchemy"    │ → skill("python-backend")                  │
│  │ "pytest"        │ → skill("python-testing-general")          │
│  │ "async"         │ → skill("python-asyncio")                  │
│  │ "type hint"     │ → skill("python-type-hints")               │
│  └─────────────────┴─────────────────────┘                      │
│                                                                  │
│  Complexity Decision:                                            │
│  ┌─────────────────┬─────────────────────┐                      │
│  │ Simple Query    │ Answer directly     │                      │
│  │ Complex Task    │ Delegate to subagent│                      │
│  └─────────────────┴─────────────────────┘                      │
└─────────────────────────────────────────────────────────────────┘
     │
     ├── Simple ──────────────────────────────────> Direct Response
     │
     └── Complex ──> Delegate to Subagent
                         │
                         ▼
              ┌──────────────────────┐
              │   python-coder       │ ──> Creates/modifies files
              │   python-reviewer    │ ──> Returns review report
              │   python-tester      │ ──> Creates test files
              │   python-scout       │ ──> Returns context findings
              └──────────────────────┘
```

### Skill Loading Flow

```
skill(name="python-fastapi")
         │
         ▼
┌─────────────────────────────────────┐
│  OpenCode Skill Discovery           │
│                                     │
│  1. Search .opencode/skills/        │
│  2. Find python-fastapi/SKILL.md    │
│  3. Parse frontmatter               │
│  4. Load content into context       │
└─────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  Skill Content Available            │
│                                     │
│  - Project structure patterns       │
│  - Pydantic schema examples         │
│  - Dependency injection patterns    │
│  - Error handling patterns          │
│  - Production deployment config     │
└─────────────────────────────────────┘
```

---

## Discovery Mechanism

### Skill Discovery

OpenCode searches for skills in these locations (in order):

1. **Project-local**: `.opencode/skills/<name>/SKILL.md`
2. **Global**: `~/.config/opencode/skills/<name>/SKILL.md`

### Agent Discovery

OpenCode discovers agents in:

1. **Project-local**: `.opencode/agent/<name>.md`
2. **Global**: `~/.config/opencode/agents/<name>.md`

### Subagent Discovery

Subagents are defined in:

1. **Project-local**: `.opencode/subagents/<name>.md`
2. **Global**: `~/.config/opencode/subagents/<name>.md`

---

## File Naming Conventions

### Skills

- Folder name: lowercase, hyphen-separated (e.g., `python-fastapi`)
- File name: `SKILL.md` (all caps)
- Name in frontmatter: must match folder name

Valid names:
- `python-fundamentals` ✓
- `python-fastapi` ✓
- `Python_FastAPI` ✗ (use lowercase)
- `python--fastapi` ✗ (no consecutive hyphens)

### Agents

- File name: lowercase, hyphen-separated (e.g., `python-expert.md`)
- Name in frontmatter: should match file name (without extension)

### Subagents

- File name: lowercase, hyphen-separated (e.g., `python-coder.md`)
- Mode must be set to `subagent`
- Type must be `general` or `explore`

---

## Integration Points

### Agent ↔ Skills

```
Agent                    Skills
  │                         │
  │ skill(name="...")       │
  │ ──────────────────────> │
  │                         │
  │ <────────────────────── │
  │   Skill content         │
```

### Agent ↔ Subagents

```
Primary Agent            Subagent
  │                         │
  │ task(subagent_type,     │
  │      description,       │
  │      prompt)            │
  │ ──────────────────────> │
  │                         │
  │ <────────────────────── │
  │   Result/Output         │
```

### Agent ↔ Context

```
Agent                    Context Files
  │                         │
  │ read context file       │
  │ ──────────────────────> │
  │                         │
  │ <────────────────────── │
  │   Standards/Patterns    │
```

---

## Related Documentation

- [Overview](./overview.md) - System introduction
- [Agents](./agents.md) - python-expert details
- [Subagents](./subagents.md) - All subagent workflows
- [Skills](./skills.md) - Complete skill documentation
- [Configuration](./configuration.md) - Customization guide
