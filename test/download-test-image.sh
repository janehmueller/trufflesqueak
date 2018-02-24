#!/bin/bash
set -e

readonly TARGET_DIR="${TRAVIS_BUILD_DIR}/images"

mkdir "${TARGET_DIR}" > /dev/null
pushd "${TARGET_DIR}"
wget -q https://www.hpi.uni-potsdam.de/hirschfeld/artifacts/trufflesqueak/TestImageWithVMMaker.zip
unzip TestImageWithVMMaker.zip
mv *.image test.image
mv *.changes test.changes
popd > /dev/null