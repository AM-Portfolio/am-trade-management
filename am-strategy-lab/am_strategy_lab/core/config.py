from functools import lru_cache

from pydantic import Field, computed_field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    app_name: str = Field(default="am-strategy-lab", alias="APP_NAME")
    app_env: str = Field(default="dev", alias="APP_ENV")
    app_port: int = Field(default=8080, alias="APP_PORT")
    log_level: str = Field(default="INFO", alias="LOG_LEVEL")
    mongo_uri: str = Field(default="", alias="AM_STRATEGY_LAB_MONGO_URI")
    mongo_database: str = Field(default="am_strategy_lab", alias="AM_STRATEGY_LAB_MONGO_DATABASE")
    mongo_host: str | None = Field(default=None, alias="AM_STRATEGY_LAB_MONGO_HOST")
    mongo_port: int | None = Field(default=None, alias="AM_STRATEGY_LAB_MONGO_PORT")
    mongo_user: str = Field(default="am_strategy_lab_user", alias="AM_STRATEGY_LAB_DB_USER")
    mongo_password: str = Field(default="", alias="AM_STRATEGY_LAB_DB_PASSWORD")
    market_base_url: str = Field(default="https://am-dev.asrax.in/market", alias="AM_MARKET_BASE_URL")
    market_historical_path: str = Field(
        default="/v1/market-data/historical-data", alias="AM_MARKET_HISTORICAL_PATH"
    )
    market_http_timeout: float = Field(default=30.0, alias="AM_STRATEGY_LAB_MARKET_TIMEOUT")
    use_fixture_bars: bool = Field(default=False, alias="AM_STRATEGY_LAB_USE_FIXTURE_BARS")
    memory_db: bool = Field(default=False, alias="AM_STRATEGY_LAB_MEMORY_DB")
    model_config = SettingsConfigDict(env_file=".env", case_sensitive=False, extra="ignore")

    @computed_field  # type: ignore[prop-decorator]
    @property
    def effective_mongo_uri(self) -> str:
        if self.mongo_uri and not self.mongo_uri.startswith("<"):
            return self.mongo_uri
        host, port = self.mongo_host or "localhost", self.mongo_port or 27017
        if self.mongo_password:
            return (
                f"mongodb://{self.mongo_user}:{self.mongo_password}@{host}:{port}/"
                f"{self.mongo_database}?authSource={self.mongo_database}"
            )
        return f"mongodb://{host}:{port}/{self.mongo_database}"


@lru_cache
def get_settings() -> Settings:
    return Settings()
