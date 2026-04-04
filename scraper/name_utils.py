def normalize_person_name(name: str) -> str:
    value = (name or '').strip()
    if '،' not in value:
        return value

    last_name, first_name = value.split('،', 1)
    last_name = last_name.strip()
    first_name = first_name.strip()

    if not last_name or not first_name:
      return value

    return f'{first_name} {last_name}'.strip()
