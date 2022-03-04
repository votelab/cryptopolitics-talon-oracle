#!/bin/bash -e

# :8080
URL='http://localhost:40006/talon/applyTransformations'

SEEDINDEX=$((1000000 - $RANDOM))
SETUP='{"classes":[{"cardClass":"COMMON","infinite":false,"series":[{"name":"pique","size":13},{"name":"coeur","size":13},{"name":"carreau","size":13},{"name":"trefle","size":13}]}]}'
SG='{"name":"cryptopolitics-dev","index":'"$SEEDINDEX"'}'
RESULT="$(curl -sS "$URL" -H 'Content-Type: application/json' -d '{"talon": null, "transformations":[{"type":"Init","setup":'"$SETUP"',"seedGenerator":'"$SG"'}]}')"
TALON="$(jq -c '.talon' <<<"$RESULT")"
SG="$(jq -c '.seedGenerator' <<<"$RESULT")"

cards=()

while true; do
    RESULT="$(curl -sS "$URL" -H 'Content-type: application/json' -d '{"talon":'"$TALON"', "seedGenerator":'"$SG"', "transformations":[{"type":"PickCards","selection": {"type":"FromClass", "cardClass":"COMMON"}}]}')"
    # Pick first card of the first transformation result
    CARD="$(jq -c '.results[0][0]' <<<"$RESULT")"
    [ "$CARD" = "null" ] && break
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
