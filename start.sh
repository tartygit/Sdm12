#!/usr/bin/env bash
# =======================================================================
# Software Development Document Environment (SDM)
# Platform: Linux / Unix
# Action: Starting Multi-Service Docker Compose System with Local AI & RAG
# =======================================================================

set -e

echo "======================================================================="
echo "Software Development Document Environment (SDM)"
echo "Action: Building & Launching containerized RAG platform..."
echo "======================================================================="

# Verify Docker Compose
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "[ERROR] docker-compose or 'docker compose' is required but not installed."
    exit 1
fi

echo "1. Building services and launching in the background..."
docker compose up --build -d

echo "2. Bootstrapping Local AI Models in Ollama container..."
echo "Retrieving 'nomic-embed-text' embedding model..."
docker compose exec -T ollama ollama pull nomic-embed-text || true

echo "Retrieving 'llama3.2' chat model..."
docker compose exec -T ollama ollama pull llama3.2 || true

echo "======================================================================="
echo "SDM Multi-Service Application Suite is running successfully!"
echo "- React modern Frontend Client: http://localhost:3000"
echo "- Java transactional Spring Boot Backend: http://localhost:8080"
echo "- Python AI Backend API Server: http://localhost:5000"
echo "- Local LLM Server (Ollama): http://localhost:11434"
echo "======================================================================="
docker compose logs -f
