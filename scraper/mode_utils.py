import re


def split_mode_names(value: str):
    text = (value or "").strip()
    if not text:
        return []

    parts = re.split(r"\s*[,،]\s*", text)
    modes = []
    for part in parts:
        name = re.sub(r"\s+", " ", part).strip()
        if name and name not in modes:
            modes.append(name)
    return modes
