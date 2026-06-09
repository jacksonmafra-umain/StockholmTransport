# Static-API demo. Wraps the :stockholm-transport JS bundle with Express and
# adds a /v1/* passthrough proxy for SL Transport. Used by the talk's Act 1.
FROM node:22-alpine

WORKDIR /usr/src/app
RUN chown -R node:node /usr/src/app

# The Kotlin/JS bundle the demo depends on lives outside this Dockerfile's
# context (../../build/js/packages/stockholm-transport — the directory the
# Kotlin/JS pipeline emits, named after `outputModuleName` in shared/build.gradle.kts).
# The package metadata inside it advertises the scoped npm name
# `@jacksonmafra-umain/stockholm-transport`, so we COPY the directory into
# node_modules under that scoped path. When building from the unified compose
# at the repo root the build context resolves; from this directory, run
# `./sl publish` first so the bundle exists, then `docker build .`.
COPY --chown=node:node demo/node-api/package*.json ./
RUN npm install --omit=dev --no-audit --no-fund

COPY --chown=node:node demo/node-api/ ./
COPY --chown=node:node build/js/packages/stockholm-transport ./node_modules/@jacksonmafra-umain/stockholm-transport

USER node

EXPOSE 3000

CMD ["node", "server.js"]
