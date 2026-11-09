from pydantic_settings import BaseSettings, SettingsConfigDict
from pydantic import Field

class Settings(BaseSettings):
    app_env: str = Field(default="development", alias="APP_ENV")
    app_port: int = Field(default=8000, alias="APP_PORT")
    secret_key: str = Field(default="dev-secret-key-32-chars-minimum-safe", alias="SECRET_KEY")

    # LiveKit WebRTC Settings
    livekit_url: str = Field(default="ws://localhost:7880", alias="LIVEKIT_URL")
    livekit_api_key: str = Field(default="devkey", alias="LIVEKIT_API_KEY")
    livekit_api_secret: str = Field(default="secretsecretsecretsecretsecretsecret32bytes", alias="LIVEKIT_API_SECRET")

    # Voice pipeline credentials
    deepgram_api_key: str = Field(default="", alias="DEEPGRAM_API_KEY")
    cartesia_api_key: str = Field(default="", alias="CARTESIA_API_KEY")
    openai_api_key: str = Field(default="", alias="OPENAI_API_KEY")

    # Storage
    database_url: str = Field(default="postgresql+asyncpg://postgres:postgres@localhost:5432/grocery_db", alias="DATABASE_URL")
    redis_url: str = Field(default="redis://localhost:6379/0", alias="REDIS_URL")

    # Stripe
    stripe_secret_key: str = Field(default="sk_test_placeholder", alias="STRIPE_SECRET_KEY")
    stripe_webhook_secret: str = Field(default="whsec_placeholder", alias="STRIPE_WEBHOOK_SECRET")

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

settings = Settings()
