FROM node:22-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginx:1.27-alpine
WORKDIR /usr/share/nginx/html

COPY --from=build /app/dist/client/browser/ ./
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY docker-entrypoint.d/40-env-config.sh /docker-entrypoint.d/40-env-config.sh
COPY public/assets/env.template.js /usr/share/nginx/html/assets/env.template.js

RUN chmod +x /docker-entrypoint.d/40-env-config.sh

EXPOSE 80
