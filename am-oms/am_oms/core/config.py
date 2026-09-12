from functools import lru_cache
from pydantic import Field, computed_field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    app_name: str = Field(default="am-oms", alias="APP_NAME")
    app_env: str = Field(default="dev", alias="APP_ENV")
    app_port: int = Field(default=8080, alias="APP_PORT")
    log_level: str = Field(default="INFO", alias="LOG_LEVEL")
    mongo_uri: str = Field(default="", alias="AM_OMS_MONGO_URI")
    mongo_database: str = Field(default="am_oms", alias="AM_OMS_MONGO_DATABASE")
    mongo_host: str | None = Field(default=None, alias="AM_OMS_MONGO_HOST")
    mongo_port: int | None = Field(default=None, alias="AM_OMS_MONGO_PORT")
    mongo_user: str = Field(default="am_oms_user", alias="AM_OMS_DB_USER")
    mongo_password: str = Field(default="", alias="AM_OMS_DB_PASSWORD")
    kafka_enabled: bool = Field(default=False, alias="KAFKA_ENABLED")
    kafka_bootstrap: str = Field(default="localhost:9092", alias="KAFKA_BOOTSTRAP_SERVERS")
    kafka_username: str = Field(default="", alias="KAFKA_USERNAME")
    kafka_password: str = Field(default="", alias="KAFKA_PASSWORD")
    kafka_security: str = Field(default="PLAINTEXT", alias="KAFKA_SECURITY_PROTOCOL")
    kafka_sasl: str = Field(default="SCRAM-SHA-256", alias="KAFKA_SASL_MECHANISM")
    fills_topic: str = Field(default="am-oms-fills", alias="AM_OMS_FILLS_TOPIC")
    journal_base_url: str = Field(default="http://am-trade-management-service:8080", alias="AM_TRADE_JOURNAL_BASE_URL")
    market_base_url: str = Field(default="", alias="AM_MARKET_BASE_URL")
    market_ohlc_path: str = Field(default="/v1/market-data/ohlc", alias="AM_MARKET_OHLC_PATH")
    model_config = SettingsConfigDict(env_file=".env", case_sensitive=False, extra="ignore")

    @computed_field  # type: ignore[prop-decorator]
    @property
    def effective_mongo_uri(self) -> str:
        if self.mongo_uri and not self.mongo_uri.startswith("<"):
            return self.mongo_uri
        host, port = self.mongo_host or "localhost", self.mongo_port or 27017
        return (
            f"mongodb://{self.mongo_user}:{self.mongo_password}@{host}:{port}/"
            f"{self.mongo_database}?authSource={self.mongo_database}"
        )


@lru_cache
def get_settings() -> Settings:
    return Settings()
