import yaml
from pathlib import Path
from typing import Dict, Any


class PromptLoader:
    """프롬프트 템플릿 로더."""

    def __init__(self, prompts_dir: str = "prompts"):
        self.prompts_dir = Path(prompts_dir)
        self._prompts_cache: Dict[str, Any] = {}

    def load_prompt(self, prompt_name: str) -> str:
        """프롬프트 템플릿을 로드합니다."""
        if prompt_name in self._prompts_cache:
            return self._prompts_cache[prompt_name]

        prompt_file = self.prompts_dir / f"{prompt_name}.yml"
        if not prompt_file.exists():
            raise FileNotFoundError(f"Prompt file not found: {prompt_file}")

        with open(prompt_file, "r", encoding="utf-8") as f:
            data = yaml.safe_load(f)

        # Extract template from nested structure
        template = data.get("summary", {}).get("prompt", {}).get("template", "")
        if not template:
            raise ValueError(f"Invalid prompt format in {prompt_file}")

        self._prompts_cache[prompt_name] = template
        return template

    def format_prompt(self, prompt_name: str, **kwargs) -> str:
        """프롬프트 템플릿을 포맷팅합니다."""
        template = self.load_prompt(prompt_name)
        return template.format(**kwargs)


# Global prompt loader instance
prompt_loader = PromptLoader()
