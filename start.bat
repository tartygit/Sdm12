@echo off
echo =======================================================================
echo Software Development Document Environment (SDM)
echo Platform: Windows
echo Action: Starting Multi-Service Docker Compose System with Local AI & RAG
echo =======================================================================

where docker >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Docker must be installed and running in your path.
    pause
    exit /b %ERRORLEVEL%
)

echo 1. Building services and launching in the background...
docker-compose up --build -d

echo 2. Bootstrapping Local AI Models in Ollama container...
echo Retrieving 'nomic-embed-text' embedding model...
docker-compose exec -T ollama ollama pull nomic-embed-text

echo Retrieving 'llama3.2' chat model...
docker-compose exec -T ollama ollama pull llama3.2

echo =======================================================================
echo SDM Multi-Service Application Suite is running successfully!
echo - React modern Frontend Client: http://localhost:3000
echo - Java transactional Spring Boot Backend: http://localhost:8080
echo - Python AI Backend API Server: http://localhost:5000
echo - Local LLM Server (Ollama): http://localhost:11434
echo =======================================================================
docker-compose logs -f
pause
