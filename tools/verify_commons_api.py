#!/usr/bin/env python3
"""
OceanWalls - live Wikimedia Commons API verification.

Mirrors the queries and filters the Android app uses, so you can confirm that
every catalogue entry still resolves to real, licence-clean ocean photographs.

    python3 tools/verify_commons_api.py            # all categories
    python3 tools/verify_commons_api.py whales     # only matching categories
"""
import gzip
import json
import re
import sys
import time
import urllib.parse
import urllib.request
from concurrent.futures import ThreadPoolExecutor

API = "https://commons.wikimedia.org/w/api.php"
UA = "OceanWalls/1.0 (Android wallpaper app; contact: nihaonihao54059@gmail.com)"

ALLOW = [re.compile(p, re.I) for p in [
    r"^cc0", r"^cc[ -]?zero", r"^public domain", r"^pd($|[ -])", r"^no restrictions",
    r"^cc[ -]by(-sa)?([ -]?\d(\.\d)?)?$", r"^cc[ -]by(-sa)?[ -]\d\.\d",
]]
BLOCK = re.compile(r"(non[- ]?commercial|\bnc\b|no[- ]?deriv|\bnd\b|fair use|all rights|copyright|gfdl|permission)", re.I)
JUNK_TITLE = re.compile(r"(map|diagram|chart|graph|logo|sign|poster|stamp|illustration|drawing|painting|engraving|"
                        r"lithograph|dictionary|encyclop|scan|book|page|cover|atlas|plate|phylogen|distribution|"
                        r"ranger|study|expedition|narrative|pamphlet|brochure|earth at night|compass|psf|"
                        r"comparison|comparisson|size|plot|scientific|rope|industry|machine|factory|engine|"
                        r"\b(statue|sculpture|monument|fossil|specimen|holotype|museum|usnm|instructions|notice|"
                        r"warning|billboard|placard|chumming|chuming|market|sale|seafood|restaurant|cooking|dish|"
                        r"hurricane|typhoon|cyclone|satellite|military|vehicle|assault|amphibious|zoology|"
                        r"natural history|limestone|devonian|geologic|outcrop|hubble|galaxy|nebula|telescope|nasa|webb|jwst|"
                        r"spitzer|chandra|macs j|deep field|astronom|light-year|quasar|supernova|exoplanet|"
                        r"interstellar|cosmic|universe|space station|\b(stilt|tern|gull|egret|heron|"
                        r"eagle|hawk|owl|sparrow|crane|flamingo|avocet|plover|sandpiper|pelican)\b))", re.I)
HUMAN_TERM = re.compile(
    r"\b(people|person|persons|human|humans|man|men|woman|women|girl|girls|boy|boys|child|children|"
    r"kid|kids|baby|infant|toddler|tourist|tourists|visitor|visitors|crowd|crowds|swimmer|swimmers|"
    r"diver|divers|diving|scuba|snorkel\w*|surfer|surfers|surfing|lifeguard|lifeguards|model|models|"
    r"portrait|portraits|selfie|couple|couples|family|families|wedding|bride|groom|soldier|soldiers|"
    r"sailor|sailors|navy|troops|worker|workers|fisherman|fishermen|fishing|hand|hands|foot|feet|"
    r"leg|legs|face|faces|mermaid|swimsuit|bikini|wetsuit|swimming pool|beachgoer|beachgoers|"
    r"sunbather|sunbathers|freediver|freedivers|freediving|snorkeler|snorkelers|instructor|"
    r"passenger|passengers|crew|guided|tour|class|students|marines|marine corps)\b", re.I)
HUMAN_CAT = re.compile(
    r"(people|persons|humans|\bmen\b|\bwomen\b|children|boys|girls|divers|diving|scuba|snorkel|"
    r"tourists|visitors|portrait|selfie|swimmers|surfers|lifeguard|sailors|soldiers|navy|beachgoers|"
    r"sunbathers|bathers|crowds|models|swimming pools|recreation|tourism|water sports)", re.I)

OCEAN_TERM = re.compile(r"(ocean|sea|marine|underwater|coral|reef|coast|beach|shore|wave|water|dive|diver|whale|"
                        r"shark|dolphin|turtle|fish|island|lagoon|tide|surf|bay|cliff|harbour|harbor|strait|gulf|"
                        r"iceberg|arctic|antarctic|polar|deep|abyss|kelp|seagrass|sand|jellyfish|octopus|squid|"
                        r"seahorse|starfish|crab|lobster|seal|penguin|manta|stingray|nautilus|anemone|plankton|"
                        r"caustic|grotto|cave)", re.I)
JUNK_CAT = re.compile(r"(painting|drawing|illustration|diagram|map|book|engraving|lithograph|print|sculpture|stamp|"
                      r"manuscript|document|chart|logo|sign|poster|cartoon|comic|icon|screenshot|phylogen|anatom|"
                      r"watercolour|sketch|postcard|advertis|encycloped|dictionar|newspaper|magazine|in art|\bart\b)", re.I)

# (id, label, commons category or None, search fallback, subject keywords[, must-match groups])
MARINE_SUBJECT = ["marine,sea,ocean,whale,shark,turtle,seal,coral,dolphin,stingray,fish,crustacean,"
                  "mollusc,octopus,jellyfish,seahorse,penguin,orca,dugong,manatee,seagrass"]

CATEGORIES = [
    ("blue-whales", "Blue Whales", "Balaenoptera musculus", "blue whale", ["whale", "balaenoptera"]),
    ("humpback-whales", "Humpback Whales", "Megaptera novaeangliae", "humpback whale", ["humpback", "megaptera", "whale"]),
    ("dolphins", "Dolphins", "Delphinidae", "dolphin", ["dolphin"]),
    ("sharks", "Sharks", "Selachimorpha", "shark", ["shark"]),
    ("great-white-sharks", "Great White Sharks", "Carcharodon carcharias", "great white shark", ["white shark", "carcharodon"]),
    ("whale-sharks", "Whale Sharks", "Rhincodon typus", "whale shark", ["whale shark", "rhincodon"]),
    ("orcas", "Orcas", "Orcinus orca", "orca", ["orca", "orcinus"]),
    ("sea-turtles", "Sea Turtles", "Chelonioidea", "sea turtle", ["turtle", "cheloni"]),
    ("octopus", "Octopus", "Octopus", "octopus", ["octopus"]),
    ("squid", "Squid", "Teuthida", "squid", ["squid"]),
    ("jellyfish", "Jellyfish", "Jellyfish", "jellyfish", ["jellyfish", "medusa"]),
    ("seahorses", "Seahorses", "Hippocampus", "seahorse", ["seahorse", "hippocampus"]),
    ("stingrays", "Stingrays", "Myliobatiformes", "stingray", ["ray", "stingray"]),
    ("manta-rays", "Manta Rays", "Mobula", "manta ray", ["manta", "mobula"]),
    ("clownfish", "Clownfish", "Amphiprioninae", "clownfish", ["clownfish", "amphiprion", "anemonefish"]),
    ("tropical-fish", "Tropical Fish", "Pomacentridae", "tropical reef fish", ["fish", "pomacentridae"]),
    ("penguins", "Penguins", "Spheniscidae", "penguin", ["penguin"]),
    ("seals", "Seals", "Phoca vitulina", "seal underwater", ["seal", "phoca"]),
    ("sea-lions", "Sea Lions", "Otariidae", "sea lion", ["sea lion", "otari", "zalophus"]),
    ("crabs", "Crabs", "Brachyura", "crab", ["crab"]),
    ("lobsters", "Lobsters", "Nephropidae", "lobster", ["lobster", "homarus"]),
    ("starfish", "Starfish", "Asteroidea", "starfish", ["starfish", "asteroidea"]),
    ("deep-sea", "Deep Sea", "Deep sea", "deep sea", ["deep", "abyss", "benthic"]),
    ("underwater-world", "Underwater World", "Underwater", "underwater reef", ["underwater", "reef", "diver"]),
    ("coral-reefs", "Coral Reefs", "Coral reefs", "coral reef", ["coral", "reef"]),
    ("ocean-caves", "Ocean Caves", None, "sea cave interior", ["cave", "cavern", "grotto"]),
    ("blue-ocean", "Blue Ocean", "Ocean", "blue ocean sea", ["ocean", "sea", "water"]),
    ("tropical-sea", "Tropical Sea", "Turquoise water", "tropical sea", ["turquoise", "lagoon", "sea", "water"]),
    ("open-ocean", "Open Ocean", "Ocean", "open ocean horizon", ["ocean", "sea", "horizon"]),
    ("ocean-floor", "Ocean Floor", "Sand ripples", "seabed sand", ["sand", "seabed", "ripple"]),
    ("underwater-sunlight", "Underwater Sunlight", "Caustics (optics)", "underwater sunlight", ["caustic", "underwater", "light", "sun"]),
    ("underwater-plants", "Underwater Plants", "Seagrasses", "seagrass kelp", ["seagrass", "posidonia", "algae", "kelp"]),
    ("beaches", "Beautiful Beaches", "Beaches", "tropical beach", ["beach", "sand", "coast"]),
    ("ocean-waves", "Ocean Waves", "Water waves", "ocean waves", ["wave", "surf", "sea"]),
    ("sea-cliffs", "Sea Cliffs", "Sea cliffs", "sea cliff", ["cliff", "coast"]),
    ("rocky-coastlines", "Rocky Coastlines", None, "rocky shore ocean", ["rock", "coast", "shore"]),
    ("tropical-islands", "Tropical Islands", "Atolls", "tropical island", ["atoll", "island", "lagoon"]),
    ("sunset-over-ocean", "Sunset Over Ocean", None, "sunset over ocean", ["sunset"]),
    ("sunrise-over-sea", "Sunrise Over Sea", None, "sunrise over sea", ["sunrise", "dawn"]),
    ("stormy-ocean", "Stormy Ocean", None, "stormy sea waves", ["storm", "wave", "rough"]),
    ("aerial-ocean", "Aerial Ocean Photography", "Aerial photographs of coasts", "aerial coast", ["aerial", "coast"]),
    ("frozen-sea", "Frozen Sea", "Sea ice", "frozen sea ice", ["ice", "frozen", "sea"]),
    ("polar-ocean", "Polar Ocean", "Icebergs", "polar ocean iceberg", ["iceberg", "polar", "arctic"]),
    ("sea-caves", "Sea Caves", "Sea caves", "sea cave", ["cave", "grotto"]),
    ("deep-blue-collection", "Deep Blue Collection", "Underwater", "deep blue underwater", ["blue", "underwater", "ocean"]),
    ("mysterious-ocean", "Mysterious Ocean", None, "dark underwater ocean", ["dark", "underwater", "deep"]),
    ("marine-giants", "Marine Giants", "Cetacea", "whale underwater", ["whale", "orca", "cetacea"]),
    ("colorful-underwater", "Colorful Underwater Life", "Coral reefs", "colorful coral reef fish", ["coral", "fish", "color"]),
    ("peaceful-ocean", "Peaceful Ocean", None, "calm ocean horizon", ["calm", "ocean", "horizon"]),
    ("dark-ocean", "Dark Ocean", None, "dark ocean night", ["dark", "night", "ocean"]),
    ("rare-marine-species", "Rare Marine Species", "Vampyroteuthis infernalis", "rare sea creature", ["vampyro", "squid", "rare", "endemic"]),
    ("endangered-marine", "Endangered Marine Animals", None, "endangered marine species", ["endangered"], MARINE_SUBJECT),
]


def api(params, tries=3):
    params = {**params, "format": "json", "formatversion": "2"}
    url = API + "?" + urllib.parse.urlencode(params)
    for attempt in range(tries):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": UA, "Accept-Encoding": "gzip"})
            with urllib.request.urlopen(req, timeout=45) as resp:
                raw = resp.read()
                if resp.headers.get("Content-Encoding") == "gzip":
                    raw = gzip.decompress(raw)
            return json.loads(raw)
        except Exception:
            time.sleep(1.5 * (attempt + 1))
    raise RuntimeError("request failed: " + url)


def clean(value):
    if not value:
        return ""
    return re.sub(r"\s+", " ", re.sub(r"<[^>]+>", "", str(value))).strip()


def licence_ok(name, url):
    if not name or BLOCK.search(name):
        return False
    if not any(p.match(name) for p in ALLOW):
        return False
    needs_url = not name.lower().startswith(("cc0", "public domain", "pd", "no restrictions"))
    return (not needs_url) or bool(url)


def probe(entry):
    cid, label, category, search, keywords = entry[:5]
    must = entry[5] if len(entry) > 5 else []
    hits, seen = [], set()
    cursor = None
    for round_no in range(2):
        if category and round_no == 0:
            params = {
                "action": "query", "generator": "categorymembers", "gcmtitle": "Category:" + category,
                "gcmtype": "file", "gcmlimit": "20", "prop": "imageinfo|categories",
                "iiprop": "url|size|mime|extmetadata|user|timestamp", "iiurlwidth": "800",
                "iiextmetadatafilter": "ObjectName|Artist|Credit|LicenseShortName|LicenseUrl|ImageDescription",
                "clshow": "!hidden", "cllimit": "max",
            }
            if cursor:
                params["gcmcontinue"] = cursor
        else:
            params = {
                "action": "query", "generator": "search", "gsrsearch": search + " filetype:bitmap",
                "gsrnamespace": "6", "gsrlimit": "20", "gsroffset": "0", "prop": "imageinfo|categories",
                "iiprop": "url|size|mime|extmetadata|user|timestamp", "iiurlwidth": "800",
                "iiextmetadatafilter": "ObjectName|Artist|Credit|LicenseShortName|LicenseUrl|ImageDescription",
                "clshow": "!hidden", "cllimit": "max",
            }
        data = api(params)
        pages = data.get("query", {}).get("pages", [])
        cursor = (data.get("continue") or {}).get("gcmcontinue")
        for page in pages:
            if page["pageid"] in seen:
                continue
            info = (page.get("imageinfo") or [None])[0]
            if not info:
                continue
            meta = info.get("extmetadata") or {}
            name = clean(meta.get("LicenseShortName", {}).get("value"))
            url = clean(meta.get("LicenseUrl", {}).get("value"))
            if not licence_ok(name, url):
                continue
            if info.get("mime") != "image/jpeg":
                continue
            if info.get("width", 0) < 1200 or info.get("height", 0) < 800:
                continue
            title = page["title"]
            if JUNK_TITLE.search(title):
                continue
            cats = [c["title"].replace("Category:", "") for c in (page.get("categories") or [])]
            if any(JUNK_CAT.search(c) for c in cats):
                continue
            desc = clean(meta.get("ImageDescription", {}).get("value"))
            if HUMAN_TERM.search(title) or (desc and HUMAN_TERM.search(desc)):
                continue
            if any(HUMAN_CAT.search(c) for c in cats):
                continue
            blob = (title + " " + desc + " " + " ".join(cats)).lower()
            if keywords and not any(k in blob for k in keywords):
                continue
            if not all(any(t.strip() in blob for t in grp.split(",")) for grp in must):
                continue
            if category is None and not OCEAN_TERM.search(blob):
                continue
            seen.add(page["pageid"])
            hits.append({
                "title": title,
                "licence": name,
                "licenceUrl": url,
                "size": f'{info["width"]}x{info["height"]}',
                "artist": clean(meta.get("Artist", {}).get("value"))[:70],
                "source": info.get("descriptionurl", ""),
            })
    return {"id": cid, "label": label, "hits": len(hits), "samples": hits[:2]}


def main():
    needle = sys.argv[1].lower() if len(sys.argv) > 1 else ""
    entries = [c for c in CATEGORIES if needle in c[0] or needle in c[1].lower()]
    with ThreadPoolExecutor(max_workers=6) as pool:
        results = list(pool.map(probe, entries))

    empty = []
    for r in results:
        mark = "OK  " if r["hits"] else "EMPTY"
        if not r["hits"]:
            empty.append(r["label"])
        first = r["samples"][0]["title"][:58] if r["samples"] else "-"
        print(f'{mark} {r["hits"]:3d}  {r["label"]:30s} {first}')

    print(f"\nchecked {len(results)} categories, {len(empty)} empty: {empty}")
    json.dump(results, open("commons_verification.json", "w"), indent=1)
    print("full report written to commons_verification.json")
    return 1 if empty else 0


if __name__ == "__main__":
    sys.exit(main())
