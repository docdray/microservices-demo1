#!/usr/bin/env python3
"""Legt Testdaten (Autoren + Bücher) über die REST-API an (nginx -> apisix -> quarkus-book-crud/quarkus-author-crud).

Nutzt nur die Python-Standardbibliothek, kein `pip install` nötig.

Beispiel:
    python3 scripts/seed_library_data.py
    python3 scripts/seed_library_data.py --books 50 --authors 20 --base-url http://localhost:8080/api
"""

import argparse
import json
import random
import sys
import urllib.error
import urllib.request
from datetime import date, timedelta

FIRST_NAMES = [
    "Anna", "Ben", "Clara", "David", "Elena", "Felix", "Greta", "Hannes",
    "Ida", "Jakob", "Klara", "Leon", "Mia", "Noah", "Olivia", "Paul",
    "Quirin", "Rosa", "Simon", "Tessa", "Uwe", "Vera", "Wolfgang", "Yara",
    "Zoe", "Amelie", "Bruno", "Carla", "Dieter", "Emma",
]

LAST_NAMES = [
    "Bauer", "Fischer", "Hoffmann", "Klein", "Lang", "Meyer", "Neumann",
    "Otto", "Peters", "Richter", "Schmidt", "Schneider", "Vogel", "Wagner",
    "Weber", "Winter", "Zimmermann", "Berger", "Fuchs", "Krause",
]

TITLE_ADJECTIVES = [
    "Die Verlorene", "Der Geheimnisvolle", "Die Letzte", "Der Vergessene",
    "Die Stille", "Der Ferne", "Die Verborgene", "Der Ewige", "Die Wilde",
    "Der Dunkle", "Die Zerbrochene", "Der Einsame",
]

TITLE_NOUNS = [
    "Stadt", "Wald", "Reise", "Insel", "Nacht", "Legende", "Grenze",
    "Wahrheit", "Brücke", "Küste", "Wüste", "Erinnerung", "Schatten", "Zeit",
]


def random_birthdate() -> str:
    start = date(1900, 1, 1)
    end = date(2005, 12, 31)
    offset = random.randint(0, (end - start).days)
    return (start + timedelta(days=offset)).isoformat()


def isbn13_check_digit(digits12: str) -> str:
    total = sum(int(d) if i % 2 == 0 else int(d) * 3 for i, d in enumerate(digits12))
    return str((10 - total % 10) % 10)


def random_isbn13() -> str:
    digits12 = "978" + "".join(str(random.randint(0, 9)) for _ in range(9))
    return digits12 + isbn13_check_digit(digits12)


def random_title() -> str:
    return f"{random.choice(TITLE_ADJECTIVES)} {random.choice(TITLE_NOUNS)}"


def request(method: str, url: str, payload: dict | None = None):
    data = json.dumps(payload).encode("utf-8") if payload is not None else None
    req = urllib.request.Request(url, data=data, method=method, headers={"Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(req) as resp:
            body = resp.read()
            return resp.status, (json.loads(body) if body else None)
    except urllib.error.HTTPError as e:
        body = e.read()
        try:
            parsed = json.loads(body) if body else None
        except json.JSONDecodeError:
            parsed = None
        return e.code, parsed


def create_author(base_url: str) -> int:
    payload = {
        "firstName": random.choice(FIRST_NAMES),
        "lastName": random.choice(LAST_NAMES),
        "birthDate": random_birthdate(),
    }
    status, body = request("POST", f"{base_url}/authors", payload)
    if status != 201:
        raise RuntimeError(f"Autor anlegen fehlgeschlagen ({status}): {body}")
    return body["id"]


def create_book(base_url: str, author_ids: list[int], max_attempts: int = 5) -> dict:
    # ISBNs werden zufällig erzeugt, Kollisionen (409) sind selten aber moeglich -> neu versuchen.
    for _ in range(max_attempts):
        payload = {"title": random_title(), "isbn": random_isbn13(), "authorIds": author_ids}
        status, body = request("POST", f"{base_url}/books", payload)
        if status == 201:
            return body
        if status != 409:
            raise RuntimeError(f"Buch anlegen fehlgeschlagen ({status}): {body}")
    raise RuntimeError("Zu viele ISBN-Kollisionen, abgebrochen")


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--base-url", default="http://localhost:8080/api", help="API-Basis-URL (Standard: %(default)s)")
    parser.add_argument("--books", type=int, default=100, help="Anzahl anzulegender Bücher (Standard: 100)")
    parser.add_argument("--authors", type=int, default=40, help="Größe des Autoren-Pools (Standard: 40)")
    parser.add_argument("--seed", type=int, default=None, help="Zufalls-Seed für reproduzierbare Läufe")
    args = parser.parse_args()

    if args.seed is not None:
        random.seed(args.seed)

    try:
        print(f"Lege {args.authors} Autoren an...")
        author_ids = [create_author(args.base_url) for _ in range(args.authors)]
        print(f"  -> {len(author_ids)} Autoren angelegt")

        print(f"Lege {args.books} Bücher an...")
        for i in range(args.books):
            num_authors = random.choice([1, 1, 1, 2, 2, 3])
            chosen = random.sample(author_ids, k=min(num_authors, len(author_ids)))
            book = create_book(args.base_url, chosen)
            print(f"  [{i + 1}/{args.books}] #{book['id']}: {book['title']} ({book['isbn']})")

        print(f"Fertig: {len(author_ids)} Autoren, {args.books} Bücher angelegt.")
    except urllib.error.URLError as e:
        print(f"Konnte {args.base_url} nicht erreichen: {e}", file=sys.stderr)
        sys.exit(1)
    except RuntimeError as e:
        print(f"Abgebrochen: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
