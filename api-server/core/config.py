from pydantic import BaseModel, PostgresDsn
from pydantic_settings import BaseSettings, SettingsConfigDict
import os

env = os.getenv("ENV", "dev")

class RunConfig(BaseSettings):
    host: str = "127.0.0.1"
    port: int = 8000

class ApiPrefix(BaseModel):
    prefix: str = "/api"

class DatabaseConfig(BaseModel):
    url: PostgresDsn
    echo: bool = False
    echo_pool: bool = False
    max_overflow: int = 5
    pool_size: int = 50

class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=(".env.template", f"api-server/.env.{env}"),
        case_sensitive=False,
        env_nested_delimiter="__",
        env_prefix="APP_CONFIG__",
    )
    run: RunConfig = RunConfig()
    api: ApiPrefix = ApiPrefix()
    db: DatabaseConfig


settings = Settings()
