# Configuration

This document explains the configuration files in the `.opencode` system.

---

## Overview

The `.opencode` system uses several configuration files:

| File | Purpose |
|------|---------|
| `config.json` | Selects default agent |
| `opencode.json` | OpenCode schema and settings |
| `agent-metadata.json` | Registry of agents, subagents, skills |
| `agent/*.md` | Agent definitions |
| `subagents/*.md` | Subagent definitions |
| `skills/*/SKILL.md` | Skill definitions |

---

## config.json

**Location**: `.opencode/config.json`

Selects the default agent for this project.

### Format

```json
{
  "agent": "python-expert"
}
```

### Fields

| Field | Type | Description |
|-------|------|-------------|
| `agent` | string | Name of the default agent |

### Usage

When OpenCode starts in this project, it reads `config.json` to determine which agent to load. The agent name must match an agent defined in `agent/*.md`.

### Changing Default Agent

Edit `config.json`:

```json
{
  "agent": "different-agent"
}
```

---

## opencode.json

**Location**: `.opencode/opencode.json`

Provides schema reference and project-level OpenCode settings.

### Format

```json
{
  "$schema": "https://opencode.ai/config.json"
}
```

### Available Options

```json
{
  "$schema": "https://opencode.ai/config.json",
  
  "model": "anthropic/claude-sonnet-4-20250514",
  
  "permission": {
    "edit": "ask",
    "bash": {
      "*": "ask",
      "git status": "allow"
    },
    "skill": {
      "*": "allow",
      "internal-*": "deny"
    }
  },
  
  "tools": {
    "write": true,
    "edit": true,
    "bash": true
  },
  
  "agent": {
    "build": {
      "model": "anthropic/claude-sonnet-4-20250514",
      "temperature": 0.3
    },
    "plan": {
      "permission": {
        "edit": "deny",
        "bash": "deny"
      }
    }
  }
}
```

### Key Configuration Areas

#### Model Selection

```json
{
  "model": "anthropic/claude-sonnet-4-20250514"
}
```

#### Permissions

```json
{
  "permission": {
    "edit": "ask",
    "bash": "ask",
    "webfetch": "deny"
  }
}
```

| Value | Behavior |
|-------|----------|
| `allow` | Always allowed without prompting |
| `ask` | Prompt user for approval |
| `deny` | Operation blocked |

#### Tool Access

```json
{
  "tools": {
    "write": true,
    "edit": true,
    "bash": true,
    "skill": true
  }
}
```

#### Agent Overrides

```json
{
  "agent": {
    "plan": {
      "permission": {
        "edit": "deny"
      },
      "tools": {
        "bash": false
      }
    }
  }
}
```

---

## agent-metadata.json

**Location**: `.opencode/config/agent-metadata.json`

Central registry for all agents, subagents, and skills.

### Format

```json
{
  "$schema": "https://opencode.ai/schemas/agent-metadata.json",
  "schema_version": "1.0.0",
  "description": "Centralized metadata for python-backend Python agents",
  
  "agents": {
    "python-expert": {
      "id": "python-expert",
      "name": "Python Expert",
      "category": "core",
      "type": "agent",
      "version": "1.0.0",
      "author": "opencode",
      "tags": ["python", "pytest", "fastapi", "pyproject"],
      "dependencies": [
        "subagent:python-coder",
        "subagent:python-reviewer",
        "subagent:python-tester",
        "subagent:python-scout",
        "skill:python-fundamentals",
        "skill:python-fastapi",
        "skill:python-backend",
        "skill:python-testing-general",
        "skill:python-asyncio",
        "skill:python-type-hints",
        "context:python-standards",
        "context:python-patterns",
        "context:python-security"
      ]
    }
  },
  
  "subagents": {
    "python-coder": {
      "id": "python-coder",
      "name": "Python Coder",
      "category": "development",
      "type": "subagent",
      "version": "1.0.0",
      "author": "opencode",
      "tags": ["coding", "implementation", "generation"],
      "dependencies": [
        "skill:python-fundamentals",
        "context:python-standards"
      ]
    }
  },
  
  "skills": {
    "python-fundamentals": {
      "id": "python-fundamentals",
      "name": "Python Fundamentals",
      "version": "1.0.0",
      "triggers": ["*.py", "python", "dataclass", "async", "type hint"]
    }
  }
}
```

### Sections

#### agents

Defines primary agents:

```json
"agents": {
  "<agent-id>": {
    "id": "agent-id",
    "name": "Display Name",
    "category": "core",
    "type": "agent",
    "version": "1.0.0",
    "author": "author-name",
    "tags": ["tag1", "tag2"],
    "dependencies": [
      "subagent:subagent-name",
      "skill:skill-name",
      "context:context-name"
    ]
  }
}
```

#### subagents

Defines subagents:

```json
"subagents": {
  "<subagent-id>": {
    "id": "subagent-id",
    "name": "Display Name",
    "category": "category",
    "type": "subagent",
    "version": "1.0.0",
    "author": "author-name",
    "tags": ["tag1", "tag2"],
    "dependencies": [
      "skill:skill-name",
      "context:context-name"
    ]
  }
}
```

#### skills

Defines skills:

```json
"skills": {
  "<skill-id>": {
    "id": "skill-id",
    "name": "Display Name",
    "version": "1.0.0",
    "triggers": ["trigger1", "trigger2"]
  }
}
```

### Dependency Types

| Prefix | Example | Description |
|--------|---------|-------------|
| `subagent:` | `subagent:python-coder` | References a subagent |
| `skill:` | `skill:python-fastapi` | References a skill |
| `context:` | `context:python-standards` | References a context file |

---

## Agent Definitions

**Location**: `.opencode/agent/*.md`

### Format

```yaml
---
name: agent-name
description: "Agent description"
mode: primary
temperature: 0.1
model: "provider/model-id"
tools:
  read: true
  write: true
  edit: true
  bash: true
  skill: true
permission:
  edit: ask
  bash:
    "*": ask
    "git status": allow
---

# Agent Name

Agent instructions and behavior...
```

### Required Frontmatter Fields

| Field | Description |
|-------|-------------|
| `name` | Agent identifier |
| `description` | Brief description for UI |
| `mode` | `primary` or `subagent` |

### Optional Frontmatter Fields

| Field | Description | Default |
|-------|-------------|---------|
| `temperature` | Response randomness (0-1) | Model default |
| `model` | Override model | Global default |
| `tools` | Tool access control | All enabled |
| `permission` | Permission overrides | Global settings |
| `hidden` | Hide from @ autocomplete | `false` |
| `color` | UI color | Theme default |
| `top_p` | Response diversity | Model default |

---

## Subagent Definitions

**Location**: `.opencode/subagents/*.md`

### Format

```yaml
---
name: subagent-name
description: "Subagent description"
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

# Subagent Name

Subagent instructions and behavior...
```

### Subagent Types

| Type | Description | Tools |
|------|-------------|-------|
| `general` | Full access | read, write, edit, bash, skill, glob, grep |
| `explore` | Read-only | read, glob, grep |

---

## Skill Definitions

**Location**: `.opencode/skills/<name>/SKILL.md`

### Format

```yaml
---
name: skill-name
description: "Skill description"
license: MIT
compatibility: opencode
metadata:
  audience: developers
  workflow: api
---

# Skill Name

Skill content with patterns, examples, best practices...
```

### Required Frontmatter Fields

| Field | Description | Constraints |
|-------|-------------|-------------|
| `name` | Skill identifier | 1-64 chars, lowercase, hyphen-separated |
| `description` | Brief description | 1-1024 chars |

### Optional Frontmatter Fields

| Field | Description |
|-------|-------------|
| `license` | License identifier |
| `compatibility` | Compatibility info |
| `metadata` | String-to-string map |

### Name Validation

Valid skill names:
- `python-fundamentals` ✓
- `python-fastapi` ✓
- `Python_FastAPI` ✗ (must be lowercase)
- `python--fastapi` ✗ (no consecutive hyphens)
- `-python` ✗ (cannot start with hyphen)

Regex: `^[a-z0-9]+(-[a-z0-9]+)*$`

---

## Customization Guide

### Adding a New Skill

1. Create directory:
   ```bash
   mkdir -p .opencode/skills/my-skill
   ```

2. Create SKILL.md:
   ```markdown
   ---
   name: my-skill
   description: "My custom skill"
   ---
   
   # My Skill
   
   Content here...
   ```

3. Register in agent-metadata.json:
   ```json
   "skills": {
     "my-skill": {
       "id": "my-skill",
       "name": "My Skill",
       "version": "1.0.0",
       "triggers": ["keyword1", "keyword2"]
     }
   }
   ```

### Adding a New Subagent

1. Create subagent file:
   ```bash
   # .opencode/subagents/my-subagent.md
   ```

2. Define subagent:
   ```yaml
   ---
   name: my-subagent
   description: "My custom subagent"
   mode: subagent
   type: general
   tools:
     read: true
     write: true
   ---
   
   # My Subagent
   
   Instructions here...
   ```

3. Register in agent-metadata.json:
   ```json
   "subagents": {
     "my-subagent": {
       "id": "my-subagent",
       "name": "My Subagent",
       "category": "custom",
       "type": "subagent",
       "version": "1.0.0",
       "dependencies": []
     }
   }
   ```

### Modifying Agent Behavior

Edit `.opencode/agent/python-expert.md`:

```yaml
---
name: python-expert
description: "Modified description"
temperature: 0.2  # More deterministic
---
```

### Changing Permissions

In `opencode.json`:

```json
{
  "permission": {
    "edit": "ask",
    "bash": {
      "*": "ask",
      "git status": "allow",
      "git diff": "allow"
    }
  }
}
```

### Disabling a Tool

In agent frontmatter:

```yaml
---
tools:
  bash: false
---
```

---

## Configuration Precedence

Configuration is applied in this order (later overrides earlier):

1. OpenCode defaults
2. Global `~/.config/opencode/opencode.json`
3. Project `.opencode/opencode.json`
4. Agent frontmatter
5. Subagent frontmatter

---

## Related Documentation

- [Overview](./overview.md) - System introduction
- [Architecture](./architecture.md) - Component relationships
- [Agents](./agents.md) - python-expert agent details
- [Subagents](./subagents.md) - All subagent workflows
- [Skills](./skills.md) - Complete skill documentation
- [Troubleshooting](./troubleshooting.md) - Configuration issues
