#!/bin/sh
# Regenerates the app icon, splash mark and logo for both apps from the festival website's art.
# Run it when the organizers publish new art, then commit the changed images.
#
#   tools/make-brand-assets.sh /path/to/porch-fest-django
#
# Needs ImageMagick 7 (`brew install imagemagick`).
#
# Sources, both in the website repository:
#   static/images/favicon.png                         sunburst mark on gold (512px, rounded corners)
#   website/static/website/images/general-hero.png    navy "Tower Porchfest" logo, transparent
set -eu

SITE="${1:?usage: tools/make-brand-assets.sh /path/to/porch-fest-django}"
FAVICON="$SITE/static/images/favicon.png"
LOGO="$SITE/website/static/website/images/general-hero.png"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ANDROID_RES="$ROOT/android/app/src/main/res"
IOS_ASSETS="$ROOT/ios/PorchFest/Assets.xcassets"
GOLD="#F2B814"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

# The mark as a black shape on transparent, 1024px. The favicon is only 512px, so it is
# enlarged to 4096px, cut to a hard edge, then reduced: that gives clean anti-aliased edges
# instead of the blur of a plain 2x upscale. Its rounded corners are discarded.
magick "$FAVICON" -background "$GOLD" -alpha remove -alpha off -colorspace Gray \
    -filter Lanczos -resize 4096x4096 -threshold 38% -negate \
    -filter Box -resize 1024x1024 "$WORK/mask.png"
magick -size 1024x1024 xc:black "$WORK/mask.png" -alpha off -compose CopyOpacity -composite "$WORK/mark.png"

# iOS app icon: full-bleed square, no transparency (iOS applies its own corner mask).
mkdir -p "$IOS_ASSETS/AppIcon.appiconset"
magick -size 1024x1024 "xc:$GOLD" "$WORK/mark.png" -compose Over -composite -alpha off \
    -strip "$IOS_ASSETS/AppIcon.appiconset/AppIcon.png"

# Android adaptive icon foreground (432px = 108dp at xxxhdpi). Launchers show only the middle
# two thirds (72..360px), through a circle or rounded square. The mark is drawn at 58% with its
# base on the bottom of that visible area, and the spire is continued below it to the edge of
# the layer, so the join is hidden and the spire never looks cut off when a launcher shifts
# the layer. Also used as the splash screen icon and, through its alpha channel, as the themed
# (monochrome) icon.
magick "$WORK/mark.png" -resize 250x250 "$WORK/small.png"
magick "$WORK/small.png" -gravity South -crop 250x1+0+0 +repage -resize '250x72!' "$WORK/spire.png"
mkdir -p "$ANDROID_RES/drawable-xxxhdpi"
magick -size 432x432 xc:none \
    "$WORK/small.png" -geometry +91+110 -composite \
    "$WORK/spire.png" -geometry +91+360 -composite \
    -strip "$ANDROID_RES/drawable-xxxhdpi/ic_launcher_foreground.png"

# The logo, unchanged apart from size. iOS gets it as a 267pt-wide image set.
mkdir -p "$ANDROID_RES/drawable-nodpi" "$IOS_ASSETS/BrandLogo.imageset"
magick "$LOGO" -strip "$ANDROID_RES/drawable-nodpi/brand_logo.png"
magick "$LOGO" -strip "$IOS_ASSETS/BrandLogo.imageset/BrandLogo@3x.png"
magick "$LOGO" -resize 533x -strip "$IOS_ASSETS/BrandLogo.imageset/BrandLogo@2x.png"
magick "$LOGO" -resize 267x -strip "$IOS_ASSETS/BrandLogo.imageset/BrandLogo.png"

echo "Brand assets written under android/app/src/main/res and ios/PorchFest/Assets.xcassets"
