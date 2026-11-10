package org.towerporchfest.shared

/** Synthetic data in the shape of `GET /api/mobile/v1/snapshot`. Never real festival data. */
internal object Fixtures {
    const val SNAPSHOT = """
{
  "schema_version": 1,
  "genres": [
    {"id": 1, "name": "Rock", "slug": "rock"},
    {"id": 2, "name": "Folk", "slug": "folk"},
    {"id": 3, "name": "Polka", "slug": "polka"}
  ],
  "performers": [
    {"id": "band-olives", "slug": "the-olives", "name": "The Olives", "bio": "Two guitars.", "link": "https://example.com/olives", "genre_ids": [1, 2], "image_url": "http://test/media/performers/olives.jpg"},
    {"id": "band-night", "slug": "night-owls", "name": "Night Owls", "bio": "", "link": "", "genre_ids": [1], "image_url": null},
    {"id": "band-solo", "slug": "solo-sam", "name": "Solo Sam", "bio": "", "link": "", "genre_ids": [], "image_url": null}
  ],
  "porches": [
    {"id": "porch-blue", "slug": "blue-porch", "name": "Blue Porch", "number": 7, "description": "Shady.", "street_address": "123 Olive Ave", "city": "Fresno", "lat": 36.765, "lng": -119.805, "image_url": null,
     "sponsor": {"name": "Tower Yoga", "level": 2, "website": "", "logo_url": "http://test/media/sponsors/logos/yoga.jpg"},
     "sponsored": true, "vendor": false, "childrens_activities": false, "after_party": false, "parking": true, "info_booth": false, "porta_potty": true, "drinking_water": false, "bicycle_repair": false},
    {"id": "porch-plain", "slug": "", "name": "", "number": null, "description": "", "street_address": "9 Wishon Ave", "city": "Fresno", "lat": 36.76, "lng": -119.8, "image_url": null, "sponsor": null,
     "sponsored": false, "vendor": false, "childrens_activities": false, "after_party": true, "parking": false, "info_booth": false, "porta_potty": false, "drinking_water": false, "bicycle_repair": false}
  ],
  "performances": [
    {"id": "perf-1", "porch_id": "porch-blue", "performer_id": "band-olives", "start_time": "13:00:00", "end_time": "13:45:00"},
    {"id": "perf-2", "porch_id": "porch-plain", "performer_id": "band-solo", "start_time": "13:00:00", "end_time": "14:00:00"},
    {"id": "perf-3", "porch_id": "porch-plain", "performer_id": "band-night", "start_time": "23:30:00", "end_time": "00:30:00"}
  ],
  "a_field_added_later": true
}
"""

    /** A later snapshot: perf-2 and Solo Sam are gone, perf-1 moved to 3 PM. */
    const val SNAPSHOT_V2 = """
{
  "schema_version": 1,
  "genres": [{"id": 1, "name": "Rock", "slug": "rock"}],
  "performers": [
    {"id": "band-olives", "slug": "the-olives", "name": "The Olives", "bio": "", "link": "", "genre_ids": [1], "image_url": null}
  ],
  "porches": [
    {"id": "porch-blue", "slug": "blue-porch", "name": "Blue Porch", "number": 7, "description": "", "street_address": "123 Olive Ave", "city": "Fresno", "lat": 36.765, "lng": -119.805, "image_url": null, "sponsor": null,
     "sponsored": false, "vendor": false, "childrens_activities": false, "after_party": false, "parking": false, "info_booth": false, "porta_potty": false, "drinking_water": false, "bicycle_repair": false}
  ],
  "performances": [
    {"id": "perf-1", "porch_id": "porch-blue", "performer_id": "band-olives", "start_time": "15:00:00", "end_time": "15:45:00"}
  ]
}
"""

    const val FUTURE_SCHEMA = """{"schema_version": 99}"""
}
