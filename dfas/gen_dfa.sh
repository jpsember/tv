#!/usr/bin/env bash
set -eu

# Compile rxp files to dfa
#
DEST="../src/main/resources/tv"
NAME="sample"
dfa input ${NAME}.rxp output "${DEST}/${NAME}.dfa"
