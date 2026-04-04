import re


FA_TO_EN = str.maketrans("۰۱۲۳۴۵۶۷۸۹", "0123456789")


def normalize_digits(value: str) -> str:
    return (value or "").translate(FA_TO_EN)


def extract_program_number(title: str, raw_no: str):
    normalized_title = normalize_digits(str(title or "")).replace("\u200f", " ").strip()
    normalized_no = normalize_digits(str(raw_no or "")).replace("\u200f", " ").strip()

    if normalized_no in {"?#", "؟#"}:
        normalized_no = ""

    raw_digits = re.sub(r"[^\d]", "", normalized_no)
    raw_suffix = re.sub(r"\d", "", normalized_no).strip()

    if raw_digits:
        number = int(raw_digits)
        suffix = raw_suffix or None
        if not suffix:
            title_match = re.search(rf"{re.escape(str(number))}\s*([^\d]+)?$", normalized_title)
            title_suffix = (title_match.group(1) or "").strip() if title_match else ""
            suffix = title_suffix or None
        return number, suffix

    title_match = re.search(r"(\d+)\s*([^\d]+)?$", normalized_title)
    if title_match:
        number = int(title_match.group(1))
        suffix = (title_match.group(2) or "").strip() or None
        return number, suffix

    title_anywhere = re.search(r"(\d+)", normalized_title)
    if title_anywhere:
        number = int(title_anywhere.group(1))
        suffix = normalized_no or None
        return number, suffix

    return 0, normalized_no or None
