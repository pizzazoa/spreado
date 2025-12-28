from pydantic_settings import BaseSettings, SettingsConfigDict
from typing import Literal


class Settings(BaseSettings):
    """Application settings loaded from environment variables."""

    # Server Configuration
    host: str = "0.0.0.0"
    port: int = 8000
    environment: str = "development"

    # AI Provider Configuration
    openai_api_key: str = ""
    openai_model: str = "gpt-5-mini"

    google_api_key: str = ""
    gemini_model: str = "gemini-1.5-flash"

    default_ai_provider: Literal["openai", "gemini"] = "openai"

    # Logging
    log_level: str = "WARNING"

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=False,
        extra="ignore"
    )


settings = Settings()
