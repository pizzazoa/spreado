# AI Summary Service

회의록 요약을 위한 AI 서비스입니다. FastAPI와 LangChain을 사용하여 구축되었습니다.

## 기능

- 회의록 텍스트를 입력받아 구조화된 요약 생성
- OpenAI 및 Google Gemini 지원
- 역할별 액션 아이템 추출
- 마일스톤 및 마감일 파싱
- RESTful API 제공

## 디렉토리 구조

```
ai/
├── app/
│   ├── api/
│   │   └── summary.py           # API 엔드포인트
│   ├── schemas/
│   │   ├── request.py           # 요청 스키마
│   │   └── response.py          # 응답 스키마
│   ├── chains/
│   │   ├── prompts.py           # 프롬프트 로더
│   │   └── summary_chain.py     # LangChain 요약 체인
│   ├── core/
│   │   ├── config.py            # 설정
│   │   ├── exceptions.py        # 커스텀 예외
│   │   └── provider.py          # AI Provider 팩토리
│   ├── utils/                   # 유틸리티 (필요시)
│   └── main.py                  # FastAPI 애플리케이션
├── prompts/
│   └── meeting_summary.yml      # 프롬프트 템플릿
├── venv/                        # Python 가상 환경
├── .env                         # 환경 변수 (git ignored)
├── .env.example                 # 환경 변수 예시
├── requirements.txt             # Python 패키지 목록
└── README.md                    # 문서
```

## 설치 및 실행

### 1. 가상 환경 활성화

```bash
cd ai
source venv/bin/activate  # macOS/Linux
# or
venv\Scripts\activate     # Windows
```

### 2. 의존성 설치

```bash
pip install -r requirements.txt
```

### 3. 환경 변수 설정

`.env.example` 파일을 `.env`로 복사하고 필요한 값을 설정합니다:

```bash
cp .env.example .env
```

`.env` 파일 예시:
```env
# Server Configuration
HOST=0.0.0.0
PORT=8000
ENVIRONMENT=development

# OpenAI Configuration
OPENAI_API_KEY=sk-your-api-key-here
OPENAI_MODEL=gpt-4o-2024-08-06

# Google Gemini Configuration (Optional)
GOOGLE_API_KEY=your-google-api-key-here
GEMINI_MODEL=gemini-1.5-pro

# Default Provider
DEFAULT_AI_PROVIDER=openai

# Logging
LOG_LEVEL=WARNING
```

### 4. 서버 실행

#### 개발 모드 (자동 리로드)
```bash
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

#### 프로덕션 모드
```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --workers 4
```

#### Python으로 직접 실행
```bash
python -m app.main
```

### 5. API 문서 확인

서버 실행 후 브라우저에서 다음 주소로 접속:
- Swagger UI: http://localhost:8000/docs
- ReDoc: http://localhost:8000/redoc

## API 사용 예시

### 회의록 요약 생성

```bash
curl -X POST "http://localhost:8000/api/summary" \
  -H "Content-Type: application/json" \
  -d '{
    "meeting_content": "오늘 회의에서는 로그인 기능 구현에 대해 논의했습니다. PM은 요구사항을 정리하고, FE는 UI를 구현하며, BE는 API를 개발하기로 했습니다. 다음 주 금요일까지 완료하기로 합의했습니다.",
    "provider": "openai"
  }'
```

### 응답 예시

```json
{
  "summary": "로그인 기능 구현을 위한 역할별 작업 분담 및 일정 합의",
  "milestones": [
    {
      "task": "로그인 기능 구현 완료",
      "deadline": "다음 주 금요일"
    }
  ],
  "action_items_by_role": {
    "PM": ["요구사항 정리"],
    "PD": [],
    "FE": ["로그인 UI 구현"],
    "BE": ["로그인 API 개발"],
    "AI": [],
    "ALL": []
  }
}
```

## 백엔드 통합 가이드

### Spring Boot에서 호출 예시

```java
@Service
public class AiSummaryClient {
    private final RestTemplate restTemplate;
    private final String aiServerUrl = "http://localhost:8000/api";

    public MeetingSummaryDto requestSummary(String meetingContent) {
        String url = aiServerUrl + "/summary";

        Map<String, Object> request = Map.of(
            "meeting_content", meetingContent,
            "provider", "openai"
        );

        return restTemplate.postForObject(url, request, MeetingSummaryDto.class);
    }
}
```

## 환경 변수 설명

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `HOST` | 서버 호스트 | `0.0.0.0` |
| `PORT` | 서버 포트 | `8000` |
| `ENVIRONMENT` | 실행 환경 | `development` |
| `OPENAI_API_KEY` | OpenAI API 키 | - |
| `OPENAI_MODEL` | OpenAI 모델명 | `gpt-4o-2024-08-06` |
| `GOOGLE_API_KEY` | Google API 키 | - |
| `GEMINI_MODEL` | Gemini 모델명 | `gemini-1.5-pro` |
| `DEFAULT_AI_PROVIDER` | 기본 AI 제공자 | `openai` |
| `LOG_LEVEL` | 로그 레벨 (WARNING, ERROR만 출력) | `WARNING` |

## 개발 참고사항

### 프롬프트 수정

`prompts/meeting_summary.yml` 파일에서 프롬프트 템플릿을 수정할 수 있습니다.

### 새로운 AI 제공자 추가

`app/core/provider.py`의 `AIProviderFactory.get_model` 메서드에 새로운 제공자를 추가할 수 있습니다.

### 로깅

구조화된 로깅(structlog)을 사용하며, JSON 형식으로 출력됩니다.

## 트러블슈팅

### 가상 환경 활성화 오류
- macOS/Linux에서 권한 오류 발생 시: `chmod +x venv/bin/activate`

### OpenAI API 키 오류
- `.env` 파일에 올바른 API 키가 설정되어 있는지 확인
- API 키에 충분한 크레딧이 있는지 확인

### 포트 충돌
- `.env` 파일에서 `PORT` 값을 변경하거나
- 실행 시 포트 지정: `uvicorn app.main:app --port 8001`

## 라이선스

MIT
