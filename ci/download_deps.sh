#!/usr/bin/env bash

IFS=$'\n'
for dep in $(jq -c '.[]' ./libs/deps.json); do
  NAME=$(echo "$dep" | jq -rc '.name')
  URL=$(echo "$dep" | jq -rc '.url')
  LICENSE=$(echo "$dep" | jq -rc '.license')
  echo "Downloading $NAME from $URL..."
  echo "License can be viewed at $LICENSE"

  curl -o ./libs/"$NAME".jar "$URL"
done