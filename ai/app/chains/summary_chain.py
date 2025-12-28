"""Meeting summary chain using LangChain."""

import json
import structlog
from typing import Dict, Any
from langchain_core.messages import HumanMessage

from app.core.config import settings
from app.core.provider import ai_provider
from app.core.exceptions import ResponseParseError
from app.chains.prompts import prompt_loader
from app.schemas.request import SummaryRequest
from app.schemas.response import SummaryResponse, Milestone, ActionItemsByRole

logger = structlog.get_logger()


class SummaryChain:
    """회의록 요약 체인."""

    def _safe_parse_json(self, response_text: str) -> Dict[str, Any]:
        """
        AI 응답에서 JSON을 안전하게 파싱합니다 (Fallback용).
        마크다운 코드 블록이나 추가 텍스트를 제거합니다.
        """
        try:
            # 공백 제거
            response_text = response_text.strip()

            # 마크다운 코드 블록 제거
            if response_text.startswith("```json"):
                response_text = response_text[7:]
            elif response_text.startswith("```"):
                response_text = response_text[3:]

            if response_text.endswith("```"):
                response_text = response_text[:-3]

            response_text = response_text.strip()

            # JSON 파싱
            data = json.loads(response_text)
            return data

        except json.JSONDecodeError as e:
            logger.error("JSON parsing failed", error=str(e), response=response_text[:200])
            raise ResponseParseError(f"Invalid JSON response: {str(e)}")

    def _manual_convert_to_response(self, data: Dict[str, Any]) -> SummaryResponse:
        """
        파싱된 딕셔너리를 SummaryResponse로 수동 변환합니다 (Fallback용).
        """
        try:
            # milestones 변환
            milestones = [
                Milestone(**milestone) for milestone in data.get("milestones", [])
            ]

            # actionItemsByRole 변환 (camelCase 키 사용)
            action_items_data = data.get("actionItemsByRole", {})
            action_items = ActionItemsByRole(
                PM=action_items_data.get("PM", []),
                PD=action_items_data.get("PD", []),
                FE=action_items_data.get("FE", []),
                BE=action_items_data.get("BE", []),
                AI=action_items_data.get("AI", []),
                ALL=action_items_data.get("ALL", []),
            )

            return SummaryResponse(
                summary=data.get("summary", ""),
                milestones=milestones,
                action_items_by_role=action_items,
            )

        except Exception as e:
            logger.error("Manual conversion failed", error=str(e), data=data)
            raise ResponseParseError(f"Failed to convert response: {str(e)}")

    async def generate_summary(self, request: SummaryRequest) -> SummaryResponse:
        """회의록 요약을 생성합니다."""
        provider = request.provider or settings.default_ai_provider

        try:
            prompt = prompt_loader.format_prompt(
                "meeting_summary",
                meeting_content=request.meeting_content
            )

            model = ai_provider.get_model(provider)

            try:
                # 1차 시도: Structured Output 사용
                structured_model = model.with_structured_output(
                    SummaryResponse,
                    method="json_schema"
                )
                result = await structured_model.ainvoke([HumanMessage(content=prompt)])
                logger.info("Structured output succeeded", provider=provider)
                return result

            except Exception as structured_error:
                # 2차 시도: Fallback to manual parsing
                logger.warning(
                    "Structured output failed, trying manual parsing",
                    provider=provider,
                    error=str(structured_error)
                )

                # 일반 텍스트 응답 받기
                response = await model.ainvoke([HumanMessage(content=prompt)])
                response_text = response.content

                # 수동 파싱 및 변환
                parsed_data = self._safe_parse_json(response_text)
                result = self._manual_convert_to_response(parsed_data)

                logger.info("Manual parsing succeeded", provider=provider)
                return result

        except Exception as e:
            logger.error("Failed to generate summary", error=str(e), exc_info=True)
            raise


# Global chain instance
summary_chain = SummaryChain()
