from pydantic import BaseModel, Field
from typing import Optional, Literal


class SummaryRequest(BaseModel):
    """Request model for meeting summary generation."""

    meeting_content: str = Field(
        ...,
        description="회의록 본문 텍스트",
        min_length=1
    )

    provider: Optional[Literal["openai", "gemini"]] = Field(
        None,
        description="사용할 AI 제공자 (미지정 시 기본값 사용)"
    )

    class Config:
        json_schema_extra = {
            "example": {
                "meeting_content": "오늘 회의에서는 로그인 기능 구현에 대해 논의했습니다...",
                "provider": "openai"
            }
        }
