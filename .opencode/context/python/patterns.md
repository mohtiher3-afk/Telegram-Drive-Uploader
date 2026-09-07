# Python Patterns

Common patterns used in python-backend project.

## Dependency Injection

### FastAPI Pattern

```python
from typing import Annotated
from fastapi import Depends
from sqlalchemy.ext.asyncio import AsyncSession

async def get_db() -> AsyncGenerator[AsyncSession, None]:
    async with async_session() as session:
        yield session

DbSession = Annotated[AsyncSession, Depends(get_db)]

@router.post("/users")
async def create_user(
    user: UserCreate,
    db: DbSession,
) -> UserResponse:
    ...
```

## Repository Pattern

```python
from typing import Protocol

class UserRepository(Protocol):
    async def get(self, user_id: int) -> User | None: ...
    async def create(self, user: UserCreate) -> User: ...
    async def update(self, user_id: int, data: UserUpdate) -> User: ...

class SQLAlchemyUserRepository:
    def __init__(self, db: AsyncSession):
        self._db = db
    
    async def get(self, user_id: int) -> User | None:
        result = await self._db.execute(
            select(User).where(User.id == user_id)
        )
        return result.scalar_one_or_none()
```

## Service Layer Pattern

```python
class UserService:
    def __init__(
        self,
        repository: UserRepository,
        cache: Cache,
    ):
        self._repository = repository
        self._cache = cache
    
    async def get_user(self, user_id: int) -> User:
        cached = await self._cache.get(f"user:{user_id}")
        if cached:
            return cached
        
        user = await self._repository.get(user_id)
        if user:
            await self._cache.set(f"user:{user_id}", user)
        return user
```

## Async Patterns

### Concurrent Execution

```python
async def fetch_all(urls: list[str]) -> list[dict]:
    async with httpx.AsyncClient() as client:
        tasks = [client.get(url) for url in urls]
        responses = await asyncio.gather(*tasks)
        return [r.json() for r in responses]
```

### Rate Limiting

```python
async def fetch_with_limit(
    urls: list[str],
    max_concurrent: int = 10,
) -> list[dict]:
    semaphore = asyncio.Semaphore(max_concurrent)
    
    async def fetch_one(url: str) -> dict:
        async with semaphore:
            async with httpx.AsyncClient() as client:
                response = await client.get(url)
                return response.json()
    
    return await asyncio.gather(*[fetch_one(url) for url in urls])
```

### Task Groups (3.11+)

```python
async def process_all(items: list[Item]):
    async with asyncio.TaskGroup() as tg:
        for item in items:
            tg.create_task(process_item(item))
```

## Pydantic Schemas

### Input/Output Separation

```python
from pydantic import BaseModel, EmailStr, Field

class UserBase(BaseModel):
    email: EmailStr
    full_name: str | None = None

class UserCreate(UserBase):
    password: str = Field(min_length=8)

class UserUpdate(BaseModel):
    email: EmailStr | None = None
    full_name: str | None = None

class UserResponse(UserBase):
    id: int
    is_active: bool
    created_at: datetime
    
    model_config = {"from_attributes": True}
```

## Error Handling

### Custom Exceptions

```python
class AppError(Exception):
    """Base exception for application."""

class NotFoundError(AppError):
    def __init__(self, resource: str, id: int):
        self.resource = resource
        self.id = id
        super().__init__(f"{resource} with id {id} not found")

class ValidationError(AppError):
    def __init__(self, field: str, message: str):
        self.field = field
        self.message = message
        super().__init__(f"{field}: {message}")
```

### Exception Groups (3.11+)

```python
def validate_data(data: dict):
    errors = []
    if not data.get("name"):
        errors.append(ValueError("name is required"))
    if not data.get("email"):
        errors.append(ValueError("email is required"))
    
    if errors:
        raise ExceptionGroup("Validation failed", errors)
```

## Context Managers

### Resource Management

```python
from contextlib import asynccontextmanager

@asynccontextmanager
async def async_session() -> AsyncIterator[AsyncSession]:
    session = AsyncSession(engine)
    try:
        yield session
        await session.commit()
    except Exception:
        await session.rollback()
        raise
    finally:
        await session.close()
```

## Factory Pattern

```python
from dataclasses import dataclass

@dataclass
class ServiceFactory:
    db: AsyncSession
    cache: Cache
    
    def user_service(self) -> UserService:
        return UserService(
            repository=SQLAlchemyUserRepository(self.db),
            cache=self.cache,
        )
```
