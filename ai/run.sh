#!/bin/bash

# AI Summary Service 실행 스크립트

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}Starting AI Summary Service...${NC}"

# 현재 디렉토리 확인
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
cd "$SCRIPT_DIR"

# 가상 환경 확인
if [ ! -d "venv" ]; then
    echo -e "${RED}Virtual environment not found. Creating...${NC}"
    python3 -m venv venv
fi

# 가상 환경 활성화
echo -e "${YELLOW}Activating virtual environment...${NC}"
source venv/bin/activate

# 의존성 설치
if [ ! -f "venv/.installed" ]; then
    echo -e "${YELLOW}Installing dependencies...${NC}"
    pip install -r requirements.txt
    touch venv/.installed
else
    echo -e "${GREEN}Dependencies already installed${NC}"
fi

# .env 파일 확인
if [ ! -f ".env" ]; then
    echo -e "${YELLOW}.env file not found. Creating from .env.example...${NC}"
    cp .env.example .env
    echo -e "${RED}Please edit .env file with your API keys before running the server${NC}"
    exit 1
fi

# 서버 실행
echo -e "${GREEN}Starting FastAPI server...${NC}"
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
