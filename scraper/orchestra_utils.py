import re

from name_utils import normalize_person_name


ARABIC_TO_PERSIAN = str.maketrans({
    "ي": "ی",
    "ك": "ک",
})


def _normalize_text(value: str) -> str:
    text = (value or "").translate(ARABIC_TO_PERSIAN).strip()
    text = re.sub(r"\s+", " ", text)
    return text


def canonicalize_orchestra_name(name: str) -> str:
    text = _normalize_text(name)
    if not text:
        return ""

    text = re.sub(r"^اركستر", "ارکستر", text)
    text = re.sub(r"\bوتلویزیون\b", "و تلویزیون", text)
    text = re.sub(r"\bرادیو\s*و\s*تلویزیون\b", "رادیو و تلویزیون", text)
    text = re.sub(r"\bرادیو\s*وتلویزیون\b", "رادیو و تلویزیون", text)

    if "گل‌ها" in text:
        return "ارکستر گل‌ها"

    if "رادیو" in text and "تلویزیون ملی ایران" in text:
        return "ارکستر رادیو و تلویزیون ملی ایران"

    return text


def split_orchestra_and_leader(name: str):
    text = _normalize_text(name)
    if not text:
        return "", ""

    match = re.match(r"^(.*?)(?:\s+به\s+رهبری\s+(.+))$", text)
    if match:
        base_name = canonicalize_orchestra_name(match.group(1))
        leader_name = normalize_person_name(match.group(2).strip())
        return base_name, leader_name

    return canonicalize_orchestra_name(text), ""
