#!/bin/bash
set -e

if [[ "${TRAVIS}" == "true" ]]; then
  readonly TARGET_DIR="${TRAVIS_BUILD_DIR}/images"
else
  readonly TARGET_DIR="images"
fi

mkdir "${TARGET_DIR}" > /dev/null
pushd "${TARGET_DIR}"
wget -q https://dl.bintray.com/hpi-swa-lab/GraalSqueak/images/TestImageWithVMMaker.zip
unzip TestImageWithVMMaker.zip
mv *.image test.image
mv *.changes test.changes
popd > /dev/null