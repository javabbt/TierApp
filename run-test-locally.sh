#!/usr/bin/env sh

echo "####### Kt Lint Check started #######"
./gradlew lint
echo "####### Kt Lint Check finished #######"

echo "####### Dev Unit test started #######"
./gradlew -Punit-test testDevDebug
echo "####### Dev Unit test finished #######"

echo "####### Dev Integration test started #######"
./gradlew -Pit-test testDevDebug
echo "####### Dev Integration test finished #######"

echo "####### Prod Unit test started #######"
./gradlew -Punit-test testProdRelease
echo "####### Prod Unit test finished #######"

echo "####### Prod Integration test started #######"
./gradlew -Pit-test testProdRelease
echo "####### Prod Integration test finished #######"