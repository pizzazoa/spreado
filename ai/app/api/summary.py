"""Summary API endpoints."""

from fastapi import APIRouter, HTTPException, status
import structlog

from app.schemas.request import SummaryRequest
from app.schemas.response import SummaryResponse, ErrorResponse
from app.chains.summary_chain import summary_chain
from app.core.exceptions import ResponseParseError, AIProviderError, InvalidRequestError

logger = structlog.get_logger()

router = APIRouter(prefix="/summary", tags=["summary"])


@router.post(
    "",
    response_model=SummaryResponse,
    status_code=status.HTTP_200_OK,
    responses={
        400: {"model": ErrorResponse, "description": "잘못된 요청"},
        500: {"model": ErrorResponse, "description": "서버 오류"},
    },
    summary="회의록 요약 생성",
    description="회의록 텍스트를 입력받아 AI를 사용하여 요약을 생성합니다.",
)
async def create_summary(request: SummaryRequest) -> SummaryResponse:
    """
    회의록 요약을 생성합니다.

    - **meeting_content**: 회의록 본문 텍스트 (필수)
    - **provider**: AI 제공자 (openai 또는 gemini, 선택)
    """
    try:
        result = await summary_chain.generate_summary(request)
        return result
    except (ValueError, ResponseParseError, InvalidRequestError) as e:
        logger.warning("Invalid request", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
    except AIProviderError as e:
        logger.error("AI provider error", error=str(e), exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="AI 서비스 연결 중 오류가 발생했습니다."
        )
    except Exception as e:
        logger.error("Failed to generate summary", error=str(e), exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="요약 생성 중 오류가 발생했습니다."
        )
