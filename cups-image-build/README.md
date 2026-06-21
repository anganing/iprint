# CUPS Docker Container

A Docker container for CUPS (Common UNIX Printing System) with CUPS-PDF and CUPS-BSD support.

## Features

- Based on Ubuntu 22.04.4 LTS
- CUPS 2.4.1
- CUPS-PDF 3.0.1
- CUPS-BSD 2.4.1
- CUPS with web interface
- CUPS-PDF for PDF printing
- CUPS-BSD for BSD printing support
- Configurable user authentication
- Automatic random password generation

## Quick Start

### Option 1: Pull from Docker Hub

```bash
docker pull tsc2lmy/cups-pdf:latest
```

OR

```bash
ghcr.io/anganing/iboot-cups-container:master
```

Run with default credentials:

```bash
docker run -d \
  --name cups-server \
  -p 631:631 \
  tsc2lmy/cups-pdf:latest
```

Run with custom credentials:
```bash
docker run -d \
  --name cups-server \
  -p 631:631 \
  -e CUPS_USER=your_username \
  -e CUPS_PASSWORD=your_password \
  tsc2lmy/cups-pdf:latest
```

### Option 2: Build from Source

```bash
docker build -t tsc2lmy/cups-pdf:latest .
```

### Run the Container

#### Option 1: Using Default Credentials
```bash
docker run -d \
  --name cups-server \
  -p 631:631 \
  iboot/cups-pdf:latest
```
The container will:
- Create a default user `admin`
- Generate a random 6-character password
- Display the credentials in the container logs

To view the credentials:
```bash
docker logs cups-server
```

#### Option 2: Using Custom Credentials
```bash
docker run -d \
  --name cups-server \
  -p 631:631 \
  -e CUPS_USER=your_username \
  -e CUPS_PASSWORD=your_password \
  iboot/cups-pdf:latest
```

## Accessing CUPS Web Interface

1. Open your web browser and navigate to:
   ```
   http://localhost:631
   ```

2. For administrative tasks, go to:
   ```
   http://localhost:631/admin
   ```

3. Log in using either:
   - The default credentials (shown in container logs)
   - Your custom credentials (if specified)

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| CUPS_USER | Username for CUPS web interface | admin |
| CUPS_PASSWORD | Password for CUPS web interface | random 6-char string |

### Ports

| Port | Description |
|------|-------------|
| 631 | CUPS web interface and printing service |

## Component Versions

| Component | Version | Description |
|-----------|---------|-------------|
| Ubuntu | 22.04.4 LTS | Base Operating System |
| CUPS | 2.4.1 | Core Printing System |
| CUPS-PDF | 3.0.1 | PDF Virtual Printer |
| CUPS-BSD | 2.4.1 | BSD Printing Support |

## Security Notes

- The default configuration allows access from any IP address
- It's recommended to use custom credentials in production
- Consider using a reverse proxy with SSL in production environments

## License

This project is licensed under the MIT License - see the LICENSE file for details. 