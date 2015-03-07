#!/usr/bin/env bash

mkdir -p ~/.overtone/orchestra/

function get_instr() {
    name="$1"
    directory="${name// /-}"
    cd ~/.overtone/orchestra/
    mkdir -p "${directory}"
    wget --quiet --no-clobber "http://www.philharmonia.co.uk/assets/audio/samples/${name}/${name}.zip"
    unzip -n -d "${directory}" "${name}.zip"
    find "${directory}" -name "*.mp3" -exec basename "\{\}.mp3" \; | \
        xargs -I\{\} ffmpeg -loglevel quiet -y -i "\{\}.mp3" "\{\}.wav"
    find "${directory}" -name "*.mp3" -delete
}

instruments=( "cello" "oboe" "double bass" "percussion" "violin" )
for inst in "${instruments[@]}"; do
    get_instr "${inst}"
done
