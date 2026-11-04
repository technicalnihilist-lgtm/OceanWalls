# Live Wikimedia Commons API verification

All checks below were run against the real API (`https://commons.wikimedia.org/w/api.php`)
during development, with the same query shapes and filters the app uses at runtime.
Re-run them yourself with:

```bash
python3 tools/verify_commons_api.py
```

## Endpoints used

| Purpose | Parameters |
| --- | --- |
| Category feed | `action=query&generator=categorymembers&gcmtitle=Category:<name>&gcmtype=file&gcmlimit=36&prop=imageinfo|categories&iiprop=url|size|mime|extmetadata|user|timestamp&iiurlwidth=800&iiextmetadatafilter=...&clshow=!hidden&cllimit=max` |
| Search feed | `action=query&generator=search&gsrsearch=<query> filetype:bitmap&gsrnamespace=6&gsrlimit=36&gsroffset=<n>` |
| Recently added | `action=query&list=recentchanges&rcnamespace=6&rctype=new&rcprop=title|timestamp&rclimit=500&rcshow=!bot` |
| Batch metadata | `action=query&titles=File:A|File:B&prop=imageinfo&iiprop=url|size|mime|extmetadata` |

Every request sends a descriptive `User-Agent`; the app never scrapes HTML and never bypasses
access restrictions.

## What was confirmed

* Real files, real metadata. Example from `Category:Megaptera novaeangliae`:
  `File:Humpback Whale Underwater (37209287981).jpg`, 3072×2048, licence **Public domain**,
  description "Every winter, thousands of humpback whales make their way to Hawaiian Islands
  Humpback Whale National Marine Sanctuary", plus its `descriptionurl` back to Commons.
* `extmetadata` reliably returns `LicenseShortName`, `LicenseUrl`, `Artist`, `ImageDescription`
  and `DateTimeOriginal` for these files; the mapper strips HTML and decodes entities.
* Category cursors work: `continue.gcmcontinue` is returned and accepted on the next call.
* Search offsets work: `continue.gsroffset` advances through the result set.
* All 52 catalogue entries (22 marine animals, 10 ocean environments, 12 ocean landscapes,
  8 special collections) were probed against the live API: **52/52 returned licence-clean
  ocean photographs** (11-39 per category across two pages). The licence and quality gates
  removed non-photographic hits that raw search returns, such as `Chelonioidea phylogeny.png`,
  `Atoll (PSF).png`, `Fishes of India. Atlas. Plate LXXX.jpg`, `Platanistidae size comparisson`
  and painting scans. Example survivors: `File:Acropora coral ffs.jpg` (coral reefs),
  `File:Blackbelly Lantern Shark.jpg` (deep sea), `File:Rhincodon typus fgbnms.jpg`
  (whale sharks), `File:Sunset over Atlantic Ocean (23102474191).jpg` (sunset).

## Photograph-only rule

Only `image/jpeg` files are accepted. On Commons, diagrams, plots, taxonomy charts and book
plates are overwhelmingly PNG/SVG/TIFF, so restricting to JPEG removes most non-photographic
content in one step, on top of the title and category heuristics.

## Licence gate (enforced in `LicensePolicy.kt`)

Allowed: `CC0`, `CC zero`, `Public domain`, `PD-*`, `No restrictions`, `CC BY n.n`, `CC BY-SA n.n`.
Rejected: anything containing NC / non-commercial, ND / no-derivatives, fair use,
all rights reserved, copyright, GFDL or an unknown licence name. Files whose licence is
CC BY / CC BY-SA but that carry no licence URL are also rejected, because the attribution
link cannot be shown.

## Quality gate (enforced in `ImageQualityGate.kt`)

Rejects by title and by Commons category: maps, diagrams, charts, logos, signs, posters,
stamps, banknotes, illustrations, drawings, paintings, engravings, lithographs, book plates,
scans, screenshots, phylogenetic and anatomy diagrams, "… in art", models and toys.
Subject keywords per category then keep only on-topic photographs, and a minimum resolution
of 1200×800 removes thumbnails and low-quality crops.

## Ocean only — no photographs of people

`ImageQualityGate.isHumanFree` rejects a file when the title or the Commons description names a
person or group, or when the file sits in a people/diver/tourist category. Word boundaries are
used so marine vocabulary is not caught by accident (`manta` is not `man`, `tide pool` is not a
swimming pool). Blocked terms include people, person, human, man, men, woman, women, girl, boy,
child, children, tourist, visitor, crowd, swimmer, diver, diving, scuba, snorkel, surfer,
lifeguard, model, portrait, selfie, couple, family, wedding, soldier, sailor, navy, marines,
crew, fisherman, fishing, hand, foot, face, swimsuit, wetsuit, swimming pool, beachgoer,
sunbather, freediver, instructor, passenger, tour, class, students — plus the matching
categories (people, divers, diving, tourists, portraits, swimmers, beachgoers, tourism, …).

Related non-photographic content is blocked at the same time: statues, sculptures, monuments,
fossils, museum specimens, market and seafood shots, signs and instructions, military vehicles,
satellite/hurricane imagery and space-telescope images.

## Notes on failure behaviour

* 5xx and 429 responses are retried three times with backoff (`RetryInterceptor`).
* A page that is entirely filtered out is transparently skipped; the UI keeps requesting until
  it has fresh items or the collection is genuinely exhausted.
* If the API is unreachable the app shows an explicit error state with a retry action, and
  previously fetched pages remain browsable from Room. No placeholder or invented image is
  ever shown in place of a real one.
