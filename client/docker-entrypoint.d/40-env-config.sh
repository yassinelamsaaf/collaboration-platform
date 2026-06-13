#!/bin/sh
set -eu

envsubst '${CLIENT_API_BASE_URL}' \
  < /usr/share/nginx/html/assets/env.template.js \
  > /usr/share/nginx/html/assets/env.js
