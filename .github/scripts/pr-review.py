#!/usr/bin/env python3
"""
AI-powered PR review script for McRPG.

Reads environment variables set by the GitHub Actions workflow to determine
which personas to run, calls the Anthropic API for each active persona, and
writes a consolidated Markdown review comment to review_comment.txt.

Environment variables (set by pr-review.yml):
  ANTHROPIC_API_KEY   — required; API key for the Anthropic Messages API
  NEEDS_GUI           — "true" if GUI/localization files changed
  NEEDS_CONFIG        — "true" if YAML config files changed
  NEEDS_API           — "true" if event/registry files changed
  NEEDS_TEST          — "true" if src/main Java files changed (check for missing tests)
  REVIEW_MODEL        — optional; overrides model (default: claude-haiku-4-5-20251001)
  DIFF_FILE           — optional; path to the diff file (default: /tmp/pr.diff)
  MAX_DIFF_CHARS      — optional; character cap on diff sent to API (default: 30000)
"""

import json
import os
import re
import sys
import time
import urllib.request
import urllib.error

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------

API_URL = "https://api.anthropic.com/v1/messages"
API_VERSION = "2023-06-01"
DEFAULT_MODEL = "claude-haiku-4-5-20251001"
MAX_OUTPUT_TOKENS = 1024
DEFAULT_MAX_DIFF_CHARS = 30_000
RETRY_DELAYS = [2, 4, 8]  # seconds between retries

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
REPO_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "..", ".."))

PERSONAS = {
    "gui": {
        "label": "GUI / UX",
        "file": ".cursor/rules/persona-gui-ux.mdc",
        "env": "NEEDS_GUI",
    },
    "config": {
        "label": "Server Owner",
        "file": ".cursor/rules/persona-server-owner.mdc",
        "env": "NEEDS_CONFIG",
    },
    "api": {
        "label": "Third-Party Extensibility",
        "file": ".cursor/rules/persona-extensibility.mdc",
        "env": "NEEDS_API",
    },
    "test": {
        "label": "Testing",
        "file": ".cursor/rules/persona-testing.mdc",
        "env": "NEEDS_TEST",
    },
}

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

_SENSITIVE_PATTERNS: list[tuple[re.Pattern, str]] = [
    # Bare (unquoted) key forms: api_key=value, api-key: value, apiKey: value
    (re.compile(r"(?i)(api[_-]?key\s*[:=]\s*)\S+"), r"\1[REDACTED_API_KEY]"),
    (re.compile(r"(?i)(secret\s*[:=]\s*)\S+"), r"\1[REDACTED_SECRET]"),
    (re.compile(r"(?i)(token\s*[:=]\s*)\S+"), r"\1[REDACTED_TOKEN]"),
    (re.compile(r"(?i)(password\s*[:=]\s*)\S+"), r"\1[REDACTED_PASSWORD]"),
    (re.compile(r"(?i)(private[_-]?key\s*[:=]\s*)\S+"), r"\1[REDACTED_PRIVATE_KEY]"),
    # Quoted-key JSON/YAML forms: "apiKey": "value", 'api-key' = 'value', etc.
    # Opening quote on the key is required; closing key-quote and value-quote are optional.
    (re.compile(r"""(?i)([\"']api[_-]?key[\"']?\s*[:=]\s*[\"']?)\S+"""), r"\1[REDACTED_API_KEY]"),
    (re.compile(r"""(?i)([\"']secret[\"']?\s*[:=]\s*[\"']?)\S+"""), r"\1[REDACTED_SECRET]"),
    (re.compile(r"""(?i)([\"']token[\"']?\s*[:=]\s*[\"']?)\S+"""), r"\1[REDACTED_TOKEN]"),
    (re.compile(r"""(?i)([\"']password[\"']?\s*[:=]\s*[\"']?)\S+"""), r"\1[REDACTED_PASSWORD]"),
    (re.compile(r"""(?i)([\"']private[_-]?key[\"']?\s*[:=]\s*[\"']?)\S+"""), r"\1[REDACTED_PRIVATE_KEY]"),
    (re.compile(r"-----BEGIN (?:RSA |EC |DSA |OPENSSH )?PRIVATE KEY-----.*?-----END (?:RSA |EC |DSA |OPENSSH )?PRIVATE KEY-----", re.DOTALL), "[REDACTED_PRIVATE_KEY]"),
    # Long base64-like tokens (32+ chars containing at least one +, / or = to exclude hex/UUIDs)
    (re.compile(r"(?<![A-Za-z0-9])(?=[A-Za-z0-9+/=_-]*[+/=])[A-Za-z0-9+/=_-]{32,}(?![A-Za-z0-9])"), "[REDACTED_TOKEN]"),
]


def sanitize_diff(diff: str) -> str:
    """Redact common sensitive patterns from a diff before sending to the API."""
    for pattern, replacement in _SENSITIVE_PATTERNS:
        diff = pattern.sub(replacement, diff)
    return diff


def read_file(path: str) -> str:
    try:
        with open(path, encoding="utf-8") as f:
            return f.read()
    except OSError:
        return ""


def strip_mdc_frontmatter(content: str) -> str:
    """Remove YAML frontmatter (--- ... ---) from .mdc files."""
    if content.startswith("---"):
        end = content.find("\n---", 3)
        if end != -1:
            return content[end + 4:].lstrip("\n")
    return content


def call_api(api_key: str, model: str, system: str, user: str) -> str | None:
    """
    POST to the Anthropic Messages API. Returns the text response or None on
    permanent failure. Retries transient errors with exponential backoff.
    """
    headers = {
        "Content-Type": "application/json",
        "x-api-key": api_key,
        "anthropic-version": API_VERSION,
    }
    payload = json.dumps({
        "model": model,
        "max_tokens": MAX_OUTPUT_TOKENS,
        "system": system,
        "messages": [{"role": "user", "content": user}],
    }).encode("utf-8")

    for attempt, delay in enumerate([0, *RETRY_DELAYS]):
        if delay:
            print(f"  Retrying in {delay}s (attempt {attempt + 1})...", file=sys.stderr)
            time.sleep(delay)
        try:
            req = urllib.request.Request(API_URL, data=payload, headers=headers, method="POST")
            with urllib.request.urlopen(req, timeout=60) as resp:
                data = json.loads(resp.read().decode("utf-8"))
                if (
                    not isinstance(data, dict)
                    or not isinstance(data.get("content"), list)
                    or not data["content"]
                ):
                    return None
                texts = [
                    item["text"]
                    for item in data["content"]
                    if isinstance(item, dict) and isinstance(item.get("text"), str)
                ]
                if not texts:
                    return None
                return "".join(texts)
        except urllib.error.HTTPError as e:
            body = e.read().decode("utf-8", errors="replace")
            print(f"  HTTP {e.code}: {body[:200]}", file=sys.stderr)
            if e.code in (429, 529) or 500 <= e.code < 600:  # rate limit / overload / transient 5xx — retry
                continue
            return None  # other HTTP errors are permanent
        except (urllib.error.URLError, TimeoutError, json.JSONDecodeError) as e:
            print(f"  Network/parse error: {e}", file=sys.stderr)
            continue  # retry on transient errors

    print("  All retries exhausted.", file=sys.stderr)
    return None


def is_no_findings(text: str) -> bool:
    """Heuristic: true when the model reported no concerns.

    Requires one of the no-concern phrases to appear as a standalone sentence
    with no other substantive content around it.
    """
    no_concern_phrases = [
        "no gui/ux concerns",
        "no server owner concerns",
        "no extensibility concerns",
        "no testing concerns",
        "no concerns found",
        "nothing to flag",
    ]
    if len(text) >= 300:
        return False
    sentences = [s.strip() for s in re.split(r"[.!?]\s*", text) if s.strip()]
    for phrase in no_concern_phrases:
        for sentence in sentences:
            if sentence.lower() == phrase:
                # Ensure no other sentence has substantive content
                others = [s for s in sentences if s.lower() != phrase]
                _conj_prefix = re.compile(r"^(and|but|however|also)[,;:\s-]*", re.I)
                if not any(
                    _conj_prefix.sub("", s).strip()
                    for s in others
                ):
                    return True
    return False


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main() -> None:
    api_key = os.environ.get("ANTHROPIC_API_KEY", "")
    if not api_key:
        print("ANTHROPIC_API_KEY not set — skipping AI review.", file=sys.stderr)
        with open("review_comment.txt", "w") as f:
            f.write("<!-- AI review skipped: ANTHROPIC_API_KEY not configured -->")
        return

    model = os.environ.get("REVIEW_MODEL", DEFAULT_MODEL)
    diff_file = os.environ.get("DIFF_FILE", "/tmp/pr.diff")
    try:
        max_diff_chars = int(os.environ.get("MAX_DIFF_CHARS", DEFAULT_MAX_DIFF_CHARS))
        if max_diff_chars <= 0:
            raise ValueError("must be positive")
    except (ValueError, TypeError):
        print("WARNING: MAX_DIFF_CHARS is not a valid positive integer — using default.", file=sys.stderr)
        max_diff_chars = DEFAULT_MAX_DIFF_CHARS

    # Load the diff
    diff = read_file(diff_file)
    if not diff:
        print("Diff is empty — nothing to review.", file=sys.stderr)
        with open("review_comment.txt", "w") as f:
            f.write("<!-- AI review skipped: empty diff -->")
        return

    if len(diff) > max_diff_chars:
        diff = diff[:max_diff_chars] + f"\n\n[Diff truncated at {max_diff_chars} characters]"

    # Load the project CLAUDE.md for domain context
    claude_md = read_file(os.path.join(REPO_ROOT, "CLAUDE.md"))

    sections: list[str] = []
    all_clear: list[str] = []

    for persona in PERSONAS.values():
        if os.environ.get(persona["env"], "false").lower() != "true":
            continue

        persona_path = os.path.join(REPO_ROOT, persona["file"])
        persona_content = strip_mdc_frontmatter(read_file(persona_path))
        if not persona_content:
            print(f"  Persona file not found: {persona_path} — skipping.", file=sys.stderr)
            continue

        label = persona["label"]
        print(f"Running {label} review...", file=sys.stderr)

        system_prompt = (
            "You are an expert code reviewer.\n\n"
            "## Project Context\n\n"
            + claude_md
            + "\n\n## Review Persona\n\n"
            + persona_content
        )
        user_prompt = (
            "Review the following pull request diff using your persona and checklist.\n\n"
            "```diff\n" + sanitize_diff(diff) + "\n```"
        )

        response = call_api(api_key, model, system_prompt, user_prompt)
        if response is None:
            sections.append(f"## {label} Review\n\n> ⚠️ Review failed due to API error.")
            continue

        if is_no_findings(response):
            all_clear.append(label)
        else:
            sections.append(f"## {label} Review\n\n{response.strip()}")

    # Build the output comment
    if not sections and not all_clear:
        comment = "<!-- AI review: no personas were triggered -->"
    elif not sections:
        clear_list = ", ".join(all_clear)
        comment = f"### AI Review\n\n✅ No concerns found ({clear_list})."
    else:
        parts = ["### AI Review\n"]
        parts.extend(sections)
        if all_clear:
            parts.append(f"\n---\n✅ No concerns from: {', '.join(all_clear)}.")
        comment = "\n\n".join(parts)

    with open("review_comment.txt", "w", encoding="utf-8") as f:
        f.write(comment)

    print(f"Done. {len(sections)} section(s) with findings, {len(all_clear)} all-clear.", file=sys.stderr)


if __name__ == "__main__":
    main()
