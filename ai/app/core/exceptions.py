"""Custom exceptions for the application."""


class AIProviderError(Exception):
    """AI provider 관련 오류."""
    pass


class PromptLoadError(Exception):
    """프롬프트 로드 오류."""
    pass


class ResponseParseError(Exception):
    """AI 응답 파싱 오류."""
    pass


class InvalidRequestError(Exception):
    """잘못된 요청 오류."""
    pass
