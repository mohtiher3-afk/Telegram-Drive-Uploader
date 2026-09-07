---
name: python-tooling
description: Python package management with UV, performance profiling, optimization techniques, and CI/CD integration. Use this skill for dependency management, Docker integration, code profiling, and build optimization.
auto_load:
  enabled: true
  triggers:
    keywords: [docker, ci, cd, github, profile, optimize, performance, benchmark]
    file_patterns: ["Dockerfile", "*.yaml", "*.yml", ".github/**/*.yml"]
  priority: medium
  load_with: [python-package-management, python-fundamentals]
---

# Python Tooling

Master Python package management with UV, performance profiling, optimization techniques, and CI/CD integration. Use this skill for dependency management, Docker integration, code profiling, and build optimization.

## When to Use This Skill

- Setting up Python projects with UV
- Managing dependencies and virtual environments
- Profiling Python code for performance
- Optimizing slow code
- Setting up CI/CD pipelines
- Docker integration for Python apps

---

## UV Package Manager

### Installation

```bash
# macOS/Linux
curl -LsSf https://astral.sh/uv/install.sh | sh

# Windows (PowerShell)
powershell -c "irm https://astral.sh/uv/install.ps1 | iex"

# Using pip (if you already have Python)
pip install uv

# Using Homebrew (macOS)
brew install uv
```

### Quick Start

```bash
# Create new project with virtual environment
uv init my-project
cd my-project

# Or create in current directory
uv init .

# Initialize creates:
# - .python-version (Python version)
# - pyproject.toml (project config)
# - README.md
# - .gitignore
```

### Install Dependencies

```bash
# Install packages (creates venv if needed)
uv add requests pandas

# Install dev dependencies
uv add --dev pytest black ruff

# Install from requirements.txt
uv pip install -r requirements.txt

# Install from pyproject.toml
uv sync
```

### Virtual Environment Management

```bash
# Create virtual environment with uv
uv venv

# Create with specific Python version
uv venv --python 3.13

# Create with custom name
uv venv my-env

# Or use uv run (no activation needed)
uv run python script.py
uv run pytest
```

### Locking Dependencies

```bash
# Generate uv.lock file
uv lock

# Update lock file
uv lock --upgrade

# Install from lockfile (exact versions)
uv sync --frozen

# Check if lockfile is up to date
uv lock --check
```

### Python Version Management

```bash
# Install Python version
uv python install 3.13

# Install multiple versions
uv python install 3.11 3.12 3.13

# Set Python version for project
uv python pin 3.13
```

### Project Configuration

```toml
# pyproject.toml
[project]
name = "my-project"
version = "0.1.0"
description = "My awesome project"
requires-python = ">=3.13"
dependencies = [
    "requests>=2.31.0",
    "pydantic>=2.0.0",
    "click>=8.1.0",
]

[project.optional-dependencies]
dev = [
    "pytest>=7.4.0",
    "pytest-cov>=4.1.0",
    "black>=23.0.0",
    "ruff>=0.1.0",
    "mypy>=1.5.0",
]

[build-system]
requires = ["hatchling"]
build-backend = "hatchling.build"

[tool.uv]
dev-dependencies = []
```

---

## Performance Profiling

### Basic Timing

```python
import time

def measure_time():
    """Simple timing measurement."""
    start = time.time()

    # Your code here
    result = sum(range(1000000))

    elapsed = time.time() - start
    print(f"Execution time: {elapsed:.4f} seconds")
    return result

# Better: use timeit for accurate measurements
import timeit

execution_time = timeit.timeit(
    "sum(range(1000000))",
    number=100
)
print(f"Average time: {execution_time/100:.6f} seconds")
```

### cProfile - CPU Profiling

```python
import cProfile
import pstats
from pstats import SortKey

def main():
    """Main function to profile."""
    result1 = slow_function()
    result2 = another_function()
    return result1, result2

if __name__ == "__main__":
    profiler = cProfile.Profile()
    profiler.enable()

    main()

    profiler.disable()

    # Print stats
    stats = pstats.Stats(profiler)
    stats.sort_stats(SortKey.CUMULATIVE)
    stats.print_stats(10)  # Top 10 functions

    # Save to file for later analysis
    stats.dump_stats("profile_output.prof")
```

**Command-line profiling:**

```bash
# Profile a script
python -m cProfile -o output.prof script.py

# View results
python -m pstats output.prof
```

### line_profiler - Line-by-Line Profiling

```python
# Install: pip install line-profiler

# Add @profile decorator
@profile
def process_data(data):
    """Process data with line profiling."""
    result = []
    for item in data:
        processed = item * 2
        result.append(processed)
    return result

# Run with:
# kernprof -l -v script.py
```

### memory_profiler - Memory Usage

```python
# Install: pip install memory-profiler

from memory_profiler import profile

@profile
def memory_intensive():
    """Function that uses lots of memory."""
    big_list = [i for i in range(1000000)]
    big_dict = {i: i**2 for i in range(100000)}
    result = sum(big_list)
    return result

# Run with:
# python -m memory_profiler script.py
```

### py-spy - Production Profiling

```bash
# Install: pip install py-spy

# Profile a running Python process
py-spy top --pid 12345

# Generate flamegraph
py-spy record -o profile.svg --pid 12345

# Profile a script
py-spy record -o profile.svg -- python script.py

# Dump current call stack
py-spy dump --pid 12345
```

---

## Optimization Patterns

### List Comprehensions vs Loops

```python
# Slow: Traditional loop
def slow_squares(n):
    result = []
    for i in range(n):
        result.append(i**2)
    return result

# Fast: List comprehension
def fast_squares(n):
    return [i**2 for i in range(n)]
```

### Generator Expressions for Memory

```python
# Memory-intensive list
def list_approach():
    data = [i**2 for i in range(1000000)]
    return sum(data)

# Memory-efficient generator
def generator_approach():
    data = (i**2 for i in range(1000000))
    return sum(data)
```

### String Concatenation

```python
# Slow string concatenation
def slow_concat(items):
    result = ""
    for item in items:
        result += str(item)
    return result

# Fast string concatenation with join
def fast_concat(items):
    return "".join(str(item) for item in items)
```

### Dictionary Lookups vs List Searches

```python
# O(n) search in list
def list_search(items, target):
    return target in items

# O(1) search in dict
def dict_search(lookup_dict, target):
    return target in lookup_dict
```

### Caching with functools.lru_cache

```python
from functools import lru_cache

@lru_cache(maxsize=None)
def fibonacci_fast(n):
    """Recursive fibonacci with caching."""
    if n < 2:
        return n
    return fibonacci_fast(n-1) + fibonacci_fast(n-2)
```

### Multiprocessing for CPU-Bound Tasks

```python
import multiprocessing as mp

def cpu_intensive_task(n):
    return sum(i**2 for i in range(n))

def parallel_processing():
    with mp.Pool(processes=4) as pool:
        results = pool.map(cpu_intensive_task, [1000000] * 4)
    return results
```

### Async I/O for I/O-Bound Tasks

```python
import asyncio
import aiohttp

async def asynchronous_requests(urls: list[str]):
    async with aiohttp.ClientSession() as session:
        tasks = [async_fetch(session, url) for url in urls]
        results = await asyncio.gather(*tasks)
    return results
```

---

## Database Optimization

### Batch Database Operations

```python
# Slow: Individual inserts
def slow_inserts(conn, count):
    cursor = conn.cursor()
    for i in range(count):
        cursor.execute("INSERT INTO users (name) VALUES (?)", (f"User {i}",))
        conn.commit()  # Commit each insert

# Fast: Batch insert with single commit
def fast_inserts(conn, count):
    cursor = conn.cursor()
    data = [(f"User {i}",) for i in range(count)]
    cursor.executemany("INSERT INTO users (name) VALUES (?)", data)
    conn.commit()  # Single commit
```

### Query Optimization

```python
# Use indexes for frequently queried columns
"""
-- Slow: No index
SELECT * FROM users WHERE email = 'user@example.com';

-- Fast: With index
CREATE INDEX idx_users_email ON users(email);
SELECT * FROM users WHERE email = 'user@example.com';
"""

# Use EXPLAIN QUERY PLAN
cursor.execute("EXPLAIN QUERY PLAN SELECT * FROM users WHERE email = ?", ("test@example.com",))
```

---

## Memory Optimization

### Detecting Memory Leaks

```python
import tracemalloc
import gc

def track_memory_usage():
    tracemalloc.start()

    # Take snapshot before
    snapshot1 = tracemalloc.take_snapshot()

    # Run code
    memory_leak_example()

    # Take snapshot after
    snapshot2 = tracemalloc.take_snapshot()

    # Compare
    top_stats = snapshot2.compare_to(snapshot1, 'lineno')

    print("Top 10 memory allocations:")
    for stat in top_stats[:10]:
        print(stat)

    tracemalloc.stop()

# Force garbage collection
gc.collect()
```

### Using __slots__ for Memory

```python
class RegularClass:
    def __init__(self, x, y, z):
        self.x = x
        self.y = y
        self.z = z

class SlottedClass:
    __slots__ = ['x', 'y', 'z']

    def __init__(self, x, y, z):
        self.x = x
        self.y = y
        self.z = z
```

### Weakref for Caches

```python
import weakref

weak_cache = weakref.WeakValueDictionary()

def get_resource_weak(key):
    resource = weak_cache.get(key)
    if resource is None:
        resource = CachedResource(f"Data for {key}")
        weak_cache[key] = resource
    return resource
```

---

## Docker Integration

### Basic Dockerfile

```dockerfile
FROM python:3.13-slim

# Install uv
COPY --from=ghcr.io/astral-sh/uv:latest /uv /usr/local/bin/uv

WORKDIR /app

# Copy dependency files
COPY pyproject.toml uv.lock ./

# Install dependencies
RUN uv sync --frozen --no-dev

# Copy application code
COPY . .

# Run application
CMD ["uv", "run", "python", "app.py"]
```

### Multi-stage Dockerfile

```dockerfile
FROM python:3.13-slim AS builder

# Install uv
COPY --from=ghcr.io/astral-sh/uv:latest /uv /usr/local/bin/uv

WORKDIR /app

# Install dependencies to venv
COPY pyproject.toml uv.lock ./
RUN uv sync --frozen --no-dev --no-editable

# Runtime stage
FROM python:3.13-slim

WORKDIR /app

# Copy venv from builder
COPY --from=builder /app/.venv .venv
COPY . .

# Use venv
ENV PATH="/app/.venv/bin:$PATH"

CMD ["python", "app.py"]
```

---

## CI/CD Integration

### GitHub Actions

```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Install uv
        uses: astral-sh/setup-uv@v2
        with:
          enable-cache: true

      - name: Set up Python
        run: uv python install 3.13

      - name: Install dependencies
        run: uv sync --all-extras --dev

      - name: Run tests
        run: uv run pytest

      - name: Run linting
        run: |
          uv run ruff check .
          uv run ruff format --check .
```

---

## Pre-commit Hooks

```yaml
# .pre-commit-config.yaml
repos:
  - repo: https://github.com/astral-sh/ruff-pre-commit
    rev: v0.8.0
    hooks:
      - id: ruff
        args: [--fix]
      - id: ruff-format

  - repo: https://github.com/pre-commit/mirrors-mypy
    rev: v1.13.0
    hooks:
      - id: mypy
        additional_dependencies: [pydantic]
        args: [--strict]

  - repo: local
    hooks:
      - id: pytest
        name: pytest
        entry: pytest tests/unit -x --tb=short
        language: system
        pass_filenames: false
        always_run: true
```

---

## Benchmarking Tools

### pytest-benchmark

```python
# Install: pip install pytest-benchmark

def test_list_comprehension(benchmark):
    result = benchmark(lambda: [i**2 for i in range(10000)])
    assert len(result) == 10000

def test_map_function(benchmark):
    result = benchmark(lambda: list(map(lambda x: x**2, range(10000))))
    assert len(result) == 10000

# Run with: pytest test_performance.py --benchmark-compare
```

### Custom Benchmark Decorator

```python
import time
from functools import wraps

def benchmark(func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        start = time.perf_counter()
        result = func(*args, **kwargs)
        elapsed = time.perf_counter() - start
        print(f"{func.__name__} took {elapsed:.6f} seconds")
        return result
    return wrapper

@benchmark
def slow_function():
    time.sleep(0.5)
    return sum(range(1000000))
```

---

## Best Practices

1. **Profile before optimizing** - Measure to find real bottlenecks
2. **Focus on hot paths** - Optimize code that runs most frequently
3. **Use appropriate data structures** - Dict for lookups, set for membership
4. **Avoid premature optimization** - Clarity first, then optimize
5. **Use built-in functions** - They're implemented in C
6. **Cache expensive computations** - Use lru_cache
7. **Batch I/O operations** - Reduce system calls
8. **Use generators for large datasets**
9. **Consider NumPy for numerical operations**
10. **Profile production code** - Use py-spy for live systems

---

## References

- [UV Documentation](https://docs.astral.sh/uv/)
- [py-spy Documentation](https://github.com/benfred/py-spy)
- [memory_profiler Documentation](https://pypi.org/project/memory_profiler/)
- [line_profiler Documentation](https://pypi.org/project/line-profiler/)
- [pytest-benchmark Documentation](https://pytest-benchmark.readthedocs.io/)
