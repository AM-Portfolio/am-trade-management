FROM python:3.12-slim

ENV PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1 \
    PIP_NO_CACHE_DIR=1 \
    APP_PORT=8080 \
    TZ=Asia/Kolkata

RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -r /var/lib/apt/lists/*

WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY am_strategy_lab/ am_strategy_lab/
ENV PYTHONPATH=/app
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=45s --retries=3 \
  CMD curl -f http://localhost:8080/health/live || exit 1
CMD ["uvicorn", "am_strategy_lab.main:app", "--host", "0.0.0.0", "--port", "8080"]
