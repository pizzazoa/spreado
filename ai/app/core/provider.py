"""AI provider factory and management."""

from typing import Dict, Literal
from langchain_openai import ChatOpenAI
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.language_models.chat_models import BaseChatModel

from app.core.config import settings
from app.core.exceptions import AIProviderError


class AIProviderFactory:
    """AI provider 팩토리 클래스."""

    def __init__(self):
        self._models: Dict[str, BaseChatModel] = {}

    def get_model(self, provider: Literal["openai", "gemini"]) -> BaseChatModel:
        """AI 모델 인스턴스를 가져옵니다."""
        if provider in self._models:
            return self._models[provider]

        try:
            if provider == "openai":
                model = ChatOpenAI(
                    api_key=settings.openai_api_key,
                    model=settings.openai_model,
                    temperature=0.0,
                )
            elif provider == "gemini":
                model = ChatGoogleGenerativeAI(
                    google_api_key=settings.google_api_key,
                    model=settings.gemini_model,
                    temperature=0.0,
                )
            else:
                raise AIProviderError(f"Unsupported provider: {provider}")

            self._models[provider] = model
            return model

        except Exception as e:
            raise AIProviderError(f"Failed to initialize {provider} model: {str(e)}") from e


# Global provider factory instance
ai_provider = AIProviderFactory()
