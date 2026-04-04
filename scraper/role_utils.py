import re


POET_KEYWORDS = (
    "سراینده",
    "شاعر",
    "اشعار",
    "شعر",
    "غزل",
    "مثنوی",
    "ترانه سرا",
    "ترانه‌سرا",
    "مطلع شعر",
)

SINGER_KEYWORDS = (
    "خواننده",
    "آوازخوان",
)


def normalize_role_text(role: str) -> str:
    role = re.sub(r"\s+", " ", (role or "").replace("\n", " ")).strip()
    if not role:
        return ""

    canonical_map = {
        "سرایندگان اشعار متن برنامه": "شاعر متن برنامه",
        "سراینده شعر آواز": "شاعر آواز",
        "ترانه سرا": "ترانه‌سرا",
        "مطلع شعر ترانه": "مطلع شعر ترانه",
        "خواننده آواز": "خواننده آواز",
        "خواننده ترانه": "خواننده ترانه",
        "نوازندگان": "نوازندگان",
        "گوینده": "گوینده",
        "آهنگساز": "آهنگساز",
        "تنظیم آهنگ": "تنظیم آهنگ",
        "روایت از": "روایت از",
    }
    return canonical_map.get(role, role)


def classify_timeline_role(role: str, name: str = "") -> str:
    role = normalize_role_text(role)
    name = (name or "").strip()

    if "ارکستر" in name or "اركستر" in name or "ارکستر" in role or "اركستر" in role:
        return "orchestra"
    if role == "گوینده" or "گوینده" in role or "روایت" in role:
        return "announcer"
    if role == "آهنگساز" or "آهنگساز" in role:
        return "composer"
    if role == "تنظیم آهنگ" or "تنظیم" in role:
        return "arranger"
    if any(keyword in role for keyword in POET_KEYWORDS):
        return "poet"
    if any(keyword in role for keyword in SINGER_KEYWORDS):
        return "singer"
    if role in {"آواز", "آوازخوان"}:
        return "singer"
    return "performer"
