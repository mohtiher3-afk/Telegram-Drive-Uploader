# Troubleshooting

This document provides solutions to common issues and answers to frequently asked questions.

---

## Common Issues

### Skills

#### Issue: Skill not loading

**Symptoms:**
- Skill doesn't appear in available skills
- `skill(name="...")` doesn't load expected content

**Solutions:**

1. **Verify SKILL.md spelling**
   ```
   # Correct
   .opencode/skills/python-fastapi/SKILL.md
   
   # Incorrect
   .opencode/skills/python-fastapi/skill.md  (lowercase)
   .opencode/skills/python-fastapi/SKILL.MD  (wrong extension)
   ```

2. **Check frontmatter**
   ```yaml
   ---
   name: python-fastapi        # Required
   description: "Description"  # Required
   ---
   ```

3. **Verify name matches folder**
   ```
   skills/python-fastapi/SKILL.md
   name: python-fastapi  # Must match folder name
   ```

4. **Check permissions**
   ```json
   // opencode.json
   {
     "permission": {
       "skill": {
         "python-fastapi": "allow"  // Not "deny"
       }
     }
   }
   ```

#### Issue: Wrong skill triggered

**Symptoms:**
- Agent loads wrong skill for request
- Expected skill not loaded

**Solutions:**

1. **Use explicit skill invocation**
   ```
   Load the python-fastapi skill and create an endpoint...
   ```

2. **Use more specific keywords**
   ```
   # Vague
   "Create an endpoint"
   
   # Specific
   "Create a FastAPI endpoint with Pydantic validation"
   ```

3. **Check trigger keywords**
   Review `.opencode/config/agent-metadata.json`:
   ```json
   "python-fastapi": {
     "triggers": ["fastapi", "pydantic", "endpoint", "api"]
   }
   ```

---

### Subagents

#### Issue: Subagent not invoked

**Symptoms:**
- Complex task not delegated to subagent
- Agent tries to do everything itself

**Solutions:**

1. **Use explicit delegation keywords**
   ```
   # Weak
   "Create a user service"
   
   # Strong
   "Implement and create files for user service"
   ```

2. **Request delegation explicitly**
   ```
   Use the python-coder subagent to implement this feature
   ```

3. **Check subagent is registered**
   Verify in `.opencode/config/agent-metadata.json`:
   ```json
   "subagents": {
     "python-coder": { ... }
   }
   ```

#### Issue: Subagent produces unexpected results

**Symptoms:**
- Subagent output doesn't match expectations
- Wrong files created/modified

**Solutions:**

1. **Provide more specific prompt**
   ```
   # Vague
   Create user endpoint
   
   # Specific
   Create POST /api/v1/users endpoint:
   - File: src/api/v1/endpoints/users.py
   - Schema: src/schemas/user.py (UserCreate, UserResponse)
   - Use dependency injection from src/api/deps.py
   - Follow pattern from src/api/v1/endpoints/items.py
   ```

2. **Include acceptance criteria**
   ```
   Acceptance criteria:
   - Returns 201 on success
   - Returns 409 for duplicate email
   - Password is hashed with bcrypt
   - Tests pass: uv run pytest tests/api/test_users.py
   ```

---

### Configuration

#### Issue: Agent not loading

**Symptoms:**
- Wrong agent active
- Agent configuration ignored

**Solutions:**

1. **Check config.json**
   ```json
   // .opencode/config.json
   { "agent": "python-expert" }
   ```

2. **Verify agent file exists**
   ```
   .opencode/agent/python-expert.md
   ```

3. **Check frontmatter syntax**
   ```yaml
   ---
   name: python-expert
   mode: primary
   ---
   ```

#### Issue: Permissions not working

**Symptoms:**
- Permissions ignored
- Expected prompts not appearing

**Solutions:**

1. **Check configuration order**
   Later configs override earlier:
   - OpenCode defaults
   - Global `~/.config/opencode/opencode.json`
   - Project `.opencode/opencode.json`
   - Agent frontmatter

2. **Verify permission syntax**
   ```json
   {
     "permission": {
       "edit": "ask",           // Correct
       "bash": {
         "*": "ask",            // Wildcard first
         "git status": "allow"  // Specific after
       }
     }
   }
   ```

3. **Check agent overrides**
   ```yaml
   # Agent frontmatter overrides global
   ---
   permission:
     edit: deny
   ---
   ```

---

### Code Quality

#### Issue: Type checking fails

**Symptoms:**
- mypy reports errors
- Type hints incorrect

**Solutions:**

1. **Load type hints skill**
   ```
   Load python-type-hints skill and fix type errors
   ```

2. **Check common issues**
   ```python
   # Missing return type
   def get_user(id: int):  # Bad
   def get_user(id: int) -> User | None:  # Good
   
   # Wrong generic syntax
   def process(items: List[str]):  # Old
   def process(items: list[str]):  # Modern (3.9+)
   ```

3. **Verify mypy configuration**
   ```toml
   # pyproject.toml
   [tool.mypy]
   python_version = "3.11"
   strict = true
   ```

#### Issue: Linting errors

**Symptoms:**
- ruff reports errors
- Code doesn't follow style

**Solutions:**

1. **Run ruff with auto-fix**
   ```bash
   uv run ruff check . --fix
   uv run ruff format .
   ```

2. **Check ruff configuration**
   ```toml
   # pyproject.toml
   [tool.ruff]
   line-length = 88
   select = ["E", "F", "I"]
   ```

---

## FAQ

### General

**Q: How do I force a specific skill to load?**

A: Mention the skill explicitly in your request:
```
Use the python-fastapi skill to create an endpoint
```

Or be more specific with keywords:
```
Create a FastAPI endpoint with Pydantic validation
```

---

**Q: How do I disable a subagent?**

A: In the subagent's frontmatter:
```yaml
---
name: python-coder
disable: true
---
```

Or in opencode.json:
```json
{
  "agent": {
    "python-coder": {
      "disable": true
    }
  }
}
```

---

**Q: How do I add custom context?**

A: Add files to `.opencode/context/`:
```
.opencode/context/
├── navigation.md
└── python/
    ├── standards.md
    ├── patterns.md
    ├── security.md
    └── my-custom-context.md  # Add here
```

Reference in agent instructions:
```
Read .opencode/context/python/my-custom-context.md for additional guidelines
```

---

**Q: Can I use multiple agents in one session?**

A: Yes, use Tab key to cycle between primary agents, or @ mention subagents:
```
@python-reviewer review this code
```

---

**Q: How do I see what skills are available?**

A: The agent sees available skills in the skill tool description. You can ask:
```
What skills are available?
```

Or check `.opencode/skills/` directory.

---

### Skills

**Q: Can I override a built-in skill?**

A: Yes, create a skill with the same name in your project:
```
.opencode/skills/python-fastapi/SKILL.md
```

Project skills take precedence over global skills.

---

**Q: How do I share skills between projects?**

A: Place skills in the global directory:
```
~/.config/opencode/skills/my-skill/SKILL.md
```

---

**Q: What's the maximum skill size?**

A: No hard limit, but keep skills focused. Large skills use more context tokens.

---

### Subagents

**Q: What's the difference between general and explore subagents?**

A:

| Type | Access | Use For |
|------|--------|---------|
| general | Full (read, write, edit, bash) | Implementation, file changes |
| explore | Read-only (read, glob, grep) | Discovery, analysis |

---

**Q: Can subagents invoke other subagents?**

A: No, subagents cannot spawn other subagents. Only the primary agent can delegate.

---

**Q: How do I control which subagents an agent can use?**

A: Use task permissions in opencode.json:
```json
{
  "agent": {
    "python-expert": {
      "permission": {
        "task": {
          "*": "allow",
          "internal-*": "deny"
        }
      }
    }
  }
}
```

---

## Debugging Tips

### Check Skill Discovery

Verify skills are discovered:

1. Check directory structure:
   ```bash
   ls -la .opencode/skills/
   ```

2. Verify SKILL.md exists:
   ```bash
   ls .opencode/skills/*/SKILL.md
   ```

3. Check frontmatter:
   ```bash
   head -10 .opencode/skills/python-fastapi/SKILL.md
   ```

### Verify Agent Configuration

1. Check config.json:
   ```bash
   cat .opencode/config.json
   ```

2. Check agent file:
   ```bash
   cat .opencode/agent/python-expert.md
   ```

3. Verify metadata:
   ```bash
   cat .opencode/config/agent-metadata.json
   ```

### Test Skill Invocation

Ask the agent to report loaded skills:
```
What skills have you loaded? Show me the skill names.
```

### Check Permissions

Review permission hierarchy:
1. Global: `~/.config/opencode/opencode.json`
2. Project: `.opencode/opencode.json`
3. Agent: `.opencode/agent/*.md`

---

## Error Messages Reference

| Error | Cause | Solution |
|-------|-------|----------|
| "Skill not found" | SKILL.md missing or misnamed | Check file exists and is uppercase |
| "Permission denied" | Tool access restricted | Check permission configuration |
| "Agent not found" | Agent file missing | Verify agent/*.md exists |
| "Invalid frontmatter" | YAML syntax error | Validate YAML in frontmatter |
| "Name mismatch" | Frontmatter name != folder name | Ensure names match exactly |

---

## Getting Help

1. **Check documentation**:
   - [Overview](./overview.md)
   - [Configuration](./configuration.md)
   - [Skills](./skills.md)

2. **Review examples**:
   - [Tutorials](./tutorials.md)

3. **Verify configuration**:
   - Check all files exist
   - Validate JSON/YAML syntax
   - Review permission settings

---

## Related Documentation

- [Overview](./overview.md) - System introduction
- [Configuration](./configuration.md) - Configuration details
- [Skills](./skills.md) - Skill documentation
- [Tutorials](./tutorials.md) - Step-by-step guides
