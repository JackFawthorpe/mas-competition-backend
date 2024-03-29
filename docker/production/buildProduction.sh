#!/bin/bash

# Create the Jar
./gradlew bootJar

# Build the docker image
docker build -t mas-competition-backend-production -f ./docker/production/Dockerfile .