# Datahike Server

A lightweight, container-ready wrapper around [Datahike](https://github.com/replikativ/datahike) that turns the in-process database into a standalone **HTTP server**.

The project provides:

- 📦 **Pre-built Docker images** published to GHCR.
- ⚡️ A tiny [`start.edn`](start.edn) Babashka script that bootstraps the server anywhere Java `17+` is available.
- 🛠️ GitHub Workflow to automatically build and publish new versions.

> If you want to embed Datahike directly inside your own Clojure/JVM project you probably want the core [Datahike](https://github.com/replikativ/datahike) library.  This repository is only about running it as a **service**.

---

## 💡 Quick start

### 1. Run with Docker (recommended)

```bash
# Pull the latest image
docker pull ghcr.io/alekcz/datahike-server:latest

# Start the server on port 4444
# (change the environment variables to fit your needs)

docker run \
  -p 4444:4444 \
  -e DATAHIKE_TOKEN="s3cr3t" \
  ghcr.io/alekcz/datahike-server:latest
```

You can now send requests to `http://localhost:4444`.

### 2. Run locally with Babashka

```bash
# Prerequisites: Java 17+  and  Babashka ≥ 1.3

# Clone the repository
$ git clone https://github.com/alekcz/datahike-server.git
$ cd datahike-server

# Start the server (defaults to port 4444)
$ bb start.edn
```

The script will:

1. Generate a default configuration file (or read the one you provide via `DATAHIKE_CONFIG_EDN`).
2. Download any **extra dependencies** you specify.
3. Launch `datahike.http.server` with the computed classpath.

---

## ⚙️ Configuration

Configuration is passed to the server as an **EDN file**. You have three options:

1. Let the script generate the file from environment variables (default).
2. Mount an existing file and point to it via `DATAHIKE_CONFIG_EDN` (inline EDN string).
3. Build a custom Docker image on top of this one and copy your file into the container.

### Environment variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DATAHIKE_PORT` / `PORT` | `4444` | Port the server will bind to |
| `DATAHIKE_LEVEL` | `info` | Log level (`trace`/`debug`/`info`/`warn`/`error`) |
| `DATAHIKE_DEV_MODE` | `false` | Enable dev-mode conveniences |
| `DATAHIKE_TOKEN` | `securerandompassword` | Shared bearer token used for authentication |
| `DATAHIKE_STORES` | `"[]"` | Namespaces of other datahike backends to load at runtime `["datahike-jdbc.core" "datahike-redis.core"]` |
| `DATAHIKE_CONFIG_EDN` | – | Full EDN config string. **Overrides** all other vars if set (doesn't include the extra deps)|
| `DATAHIKE_EXTRA_DEPS` | – | The extra dependencies for the stores required at runtime |

`DATAHIKE_EXTRA_DEPS` accepts both leinigen style dependencies. 
```clojure
[[io.replikativ/datahike-jdbc "0.3.50"]
 [org.postgresql/postgresql "42.7.5"]
 [io.replikativ/datahike-redis "0.1.7"]]
```
Or deps.edn style dependencies if you prefer. 

```clojure
{io.replikativ/datahike-jdbc {:mvn/version "0.3.50"}
 org.postgresql/postgresql   {:mvn/version "42.7.5"}
 io.replikativ/datahike-redis {:mvn/version "0.1.7"}}
```

⚠️  When using Docker from the terminal you must pass these variables with `-e`.

---

## 🛠️ Building the image yourself

```bash
# Clone along with the main Datahike repo (needed for the uber-jar)
$ git clone https://github.com/alekcz/datahike-server.git && cd datahike-server
$ git clone --depth 1 https://github.com/alekcz/datahike.git

# Build the Datahike HTTP-server uber-jar
$ cd datahike && bb http-server-uber && cd ..

# Copy the jar into the build context
$ mkdir -p build && cp datahike/target-http-server/*.jar build/datahike-http-server.jar

# Build the container
$ docker build -t datahike-server . -f .github/docker/Dockerfile
```

---

## 🤝 Contributing

Pull requests are welcome!  If you spot an issue or have an enhancement in mind, please open an [issue](https://github.com/alekcz/datahike-server/issues) first to discuss.

1. Fork & clone the repo
2. Create your branch: `git checkout -b feature/my-amazing-idea`
3. Commit your changes
4. Push and open a PR

Please make sure your code passes `clj-kondo`/`cljfmt`/`zprint` linters (the same ones configured in CI).

---

## 📄 License

This project is distributed under the terms of the **EPL License** – see [LICENSE](LICENSE) for details. 