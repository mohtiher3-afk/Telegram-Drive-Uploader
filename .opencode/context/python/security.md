# Python Security Patterns

Security best practices for python-backend project.

## Input Validation

### Pydantic Validation

```python
from pydantic import BaseModel, EmailStr, Field, field_validator

class UserCreate(BaseModel):
    email: EmailStr
    password: str = Field(min_length=8, max_length=128)
    username: str = Field(min_length=3, max_length=50)
    
    @field_validator("username")
    @classmethod
    def validate_username(cls, v: str) -> str:
        if not v.isalnum():
            raise ValueError("Username must be alphanumeric")
        return v.lower()
```

### Custom Validators

```python
import re

def validate_password(password: str) -> str:
    if len(password) < 8:
        raise ValueError("Password must be at least 8 characters")
    if not re.search(r"[A-Z]", password):
        raise ValueError("Password must contain uppercase letter")
    if not re.search(r"[a-z]", password):
        raise ValueError("Password must contain lowercase letter")
    if not re.search(r"\d", password):
        raise ValueError("Password must contain digit")
    return password
```

## Authentication

### JWT Pattern

```python
from datetime import datetime, timedelta
from jose import jwt, JWTError

SECRET_KEY = "your-secret-key"  # Use environment variable
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30

def create_access_token(data: dict) -> str:
    to_encode = data.copy()
    expire = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)

def verify_token(token: str) -> dict | None:
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        return payload
    except JWTError:
        return None
```

### FastAPI Auth Dependency

```python
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials

security = HTTPBearer()

async def get_current_user(
    credentials: HTTPAuthorizationCredentials = Depends(security),
) -> User:
    token = credentials.credentials
    payload = verify_token(token)
    if not payload:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid authentication credentials",
        )
    user = await get_user_by_id(payload["sub"])
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="User not found",
        )
    return user
```

## Password Handling

### Hashing

```python
from passlib.context import CryptContext

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

def hash_password(password: str) -> str:
    return pwd_context.hash(password)

def verify_password(plain_password: str, hashed_password: str) -> bool:
    return pwd_context.verify(plain_password, hashed_password)
```

## SQL Injection Prevention

### Always Use Parameterized Queries

```python
# GOOD - Parameterized
async def get_user_by_email(email: str, db: AsyncSession) -> User | None:
    result = await db.execute(
        select(User).where(User.email == email)
    )
    return result.scalar_one_or_none()

# GOOD - SQLAlchemy Core with bindparams
from sqlalchemy import text

async def search_users(query: str, db: AsyncSession) -> list[User]:
    result = await db.execute(
        text("SELECT * FROM users WHERE name LIKE :query"),
        {"query": f"%{query}%"}
    )
    return result.fetchall()

# BAD - String concatenation (NEVER DO THIS)
# query = f"SELECT * FROM users WHERE name = '{name}'"
```

## Authorization

### Role-Based Access Control

```python
from enum import StrEnum

class Role(StrEnum):
    ADMIN = "admin"
    USER = "user"
    GUEST = "guest"

def require_role(required_role: Role):
    async def role_checker(
        current_user: User = Depends(get_current_user),
    ) -> User:
        if current_user.role != required_role and current_user.role != Role.ADMIN:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Insufficient permissions",
            )
        return current_user
    return role_checker

@router.delete("/users/{user_id}")
async def delete_user(
    user_id: int,
    current_user: User = Depends(require_role(Role.ADMIN)),
):
    ...
```

## Secrets Management

### Environment Variables

```python
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    secret_key: str
    database_url: str
    redis_url: str
    
    class Config:
        env_file = ".env"

settings = Settings()
```

### Never Hardcode Secrets

```python
# BAD - Never do this
SECRET_KEY = "my-secret-key-12345"
API_KEY = "sk-abc123"

# GOOD - Use environment variables
SECRET_KEY = os.environ["SECRET_KEY"]
API_KEY = settings.api_key
```

## Path Traversal Prevention

```python
from pathlib import Path

def safe_path(base_dir: Path, user_path: str) -> Path:
    """Validate user-provided path doesn't escape base directory."""
    full_path = (base_dir / user_path).resolve()
    if not full_path.is_relative_to(base_dir.resolve()):
        raise ValueError("Invalid path")
    return full_path
```

## Rate Limiting

```python
from slowapi import Limiter
from slowapi.util import get_remote_address

limiter = Limiter(key_func=get_remote_address)

@router.post("/login")
@limiter.limit("5/minute")
async def login(request: Request, credentials: LoginRequest):
    ...
```

## Security Headers

```python
from fastapi.middleware.cors import CORSMiddleware

app.add_middleware(
    CORSMiddleware,
    allow_origins=["https://yourdomain.com"],
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "DELETE"],
    allow_headers=["*"],
)
```

## Security Checklist

- [ ] All inputs validated with Pydantic
- [ ] All SQL uses parameterized queries
- [ ] Passwords hashed with bcrypt
- [ ] JWT tokens have expiration
- [ ] Auth required on protected routes
- [ ] Role-based access control implemented
- [ ] Secrets loaded from environment
- [ ] Rate limiting on sensitive endpoints
- [ ] CORS configured properly
- [ ] HTTPS enforced in production
