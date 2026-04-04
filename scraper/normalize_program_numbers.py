import json
import re

from program_number_utils import extract_program_number


DATA_PATH = "data/programmes_by_category.json"


def main():
    with open(DATA_PATH, "r", encoding="utf-8") as f:
        categories = json.load(f)

    updated = 0

    for _, programs in categories.items():
        for program in programs:
            title = str(program.get("title", ""))
            cleaned_title = re.sub(r"\s*[؟?]#\s*(?=\d)", " ", title)
            cleaned_title = re.sub(r"\s+", " ", cleaned_title).strip()
            if cleaned_title != title:
                program["title"] = cleaned_title

            number, sub_no = extract_program_number(program.get("title", ""), program.get("no", ""))
            current_no = program.get("no")
            current_sub_no = program.get("sub_no")
            normalized_no = str(number)

            if str(current_no) != normalized_no or current_sub_no != sub_no:
                program["no"] = normalized_no
                if sub_no:
                    program["sub_no"] = sub_no
                else:
                    program.pop("sub_no", None)
                updated += 1

    with open(DATA_PATH, "w", encoding="utf-8") as f:
        json.dump(categories, f, ensure_ascii=False, indent=2)

    print(f"Normalized program numbers for {updated} entries.")


if __name__ == "__main__":
    main()
