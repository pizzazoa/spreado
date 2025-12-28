from pydantic import BaseModel, Field
from typing import List, Dict


class Milestone(BaseModel):
    """마일스톤 정보."""

    task: str = Field(..., description="마일스톤 작업명")
    deadline: str = Field(..., description="마감일")


class ActionItemsByRole(BaseModel):
    """역할별 액션 아이템."""

    PM: List[str] = Field(default_factory=list, description="프로젝트 매니저 할 일")
    PD: List[str] = Field(default_factory=list, description="프로덕트 디자이너 할 일")
    FE: List[str] = Field(default_factory=list, description="프론트엔드 개발자 할 일")
    BE: List[str] = Field(default_factory=list, description="백엔드 개발자 할 일")
    AI: List[str] = Field(default_factory=list, description="AI 엔지니어 할 일")
    ALL: List[str] = Field(default_factory=list, description="팀 전체 할 일")


class SummaryResponse(BaseModel):
    """Response model for meeting summary."""

    summary: str = Field(..., description="회의 핵심 요약")
    milestones: List[Milestone] = Field(default_factory=list, description="마일스톤 목록")
    action_items_by_role: ActionItemsByRole = Field(
        ...,
        description="역할별 액션 아이템",
        serialization_alias="actionItemsByRole"
    )

    class Config:
        populate_by_name = True
        json_schema_extra = {
            "example": {
                "summary": "로그인 기능 구현 및 UI 개선에 대한 논의",
                "milestones": [
                    {
                        "task": "로그인 기능 구현 완료",
                        "deadline": "다음 주 금요일"
                    }
                ],
                "actionItemsByRole": {
                    "PM": ["요구사항 문서 작성"],
                    "PD": ["로그인 화면 디자인"],
                    "FE": ["로그인 페이지 구현"],
                    "BE": ["인증 API 개발"],
                    "AI": [],
                    "ALL": ["다음 회의 참석"]
                }
            }
        }


class ErrorResponse(BaseModel):
    """Error response model."""

    error: str = Field(..., description="에러 메시지")
    detail: str = Field(default="", description="에러 상세 정보")
