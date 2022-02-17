#!/bin/bash -e

# :8080
URL='http://localhost:40006/talon/applyTransformations'

SEEDINDEX=$((1000000 - $RANDOM))
# {"cardClass":"FREE","infinite":true,"series":[{"name":"free"}]},
SETUP='{"classes":[{"cardClass":"COMMON","infinite":true,"series":[{"name":"pique"},{"name":"coeur"},{"name":"carreau"},{"name":"trefle"}]}]}'
SG='{"name":"cryptopolitics-dev","index":'"$SEEDINDEX"'}'
RESULT="$(curl -sS "$URL" -H 'Content-Type: application/json' -d '{"talon": null, "transformations":[{"type":"Init","setup":'"$SETUP"',"seedGenerator":'"$SG"'}]}')"
TALON="$(jq -c '.talon' <<<"$RESULT")"
SG="$(jq -c '.seedGenerator' <<<"$RESULT")"

cards=()

while true; do
    RESULT="$(curl -sS "$URL" -H 'Content-type: application/json' -d '{"talon":'"$TALON"', "seedGenerator":'"$SG"', "transformations":[{"type":"PickCards","cardClasses": ["COMMON"]}]}')"
    # Pick first card of the first transformation result
    CARD="$(jq -c '.results[0][0]' <<<"$RESULT")"
    [ "$CARD" = "null" ] && break
    # 1% chance to give up
    [ $RANDOM -lt 328 ] && break
    echo "$CARD"
    cards+=("$CARD")
    TALON="$(jq -c '.talon' <<<"$RESULT")"
    SG="$(jq -c '.seedGenerator' <<<"$RESULT")"
done

cards=( $(shuf -e "${cards[@]}") )

for card in "${cards[@]}"; do
    RESULT="$(curl -sS "$URL" -H 'Content-type: application/json' -d '{"talon":'"$TALON"', "seedGenerator":'"$SG"', "transformations":[{"type":"AddCards","cards":['"$card"']}]}')"
    TALON="$(jq -c '.talon' <<<"$RESULT")"
    SG="$(jq -c '.seedGenerator' <<<"$RESULT")"
done

jq . <<< "$TALON"
