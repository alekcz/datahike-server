FROM eclipse-temurin:21-jre-jammy

# ── runtime helpers (curl + babashka) ────────────────────────────────────────
RUN apt-get update -qq && \
    apt-get install -y --no-install-recommends curl && \
    curl -sSL https://github.com/babashka/babashka/releases/download/v1.3.190/babashka-1.3.190-linux-amd64-static.tar.gz \
      | tar -xz -C /usr/local/bin bb

WORKDIR /opt/datahike
COPY build/datahike-http-server.jar .
COPY start.edn /usr/local/bin/start.edn

RUN chmod +x /usr/local/bin/start.edn && \
    mkdir -p /opt/datahike/jars

ENTRYPOINT ["bb", "/usr/local/bin/start.edn"]
EXPOSE 4444