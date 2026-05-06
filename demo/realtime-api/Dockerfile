# Pin to Node 22 LTS on Alpine — Node 18 hit EOL in April 2024.
# Bump alongside the engines.node range in package.json.
FROM node:22-alpine

# Drop privileges: alpine ships a `node` user/group at uid/gid 1000.
WORKDIR /usr/src/app
RUN chown -R node:node /usr/src/app

# Install dependencies first (leverage Docker layer caching). --no-audit and
# --no-fund keep the build deterministic; npm-audit-fix happens in CI, not at
# image build time. Dev deps (nodemon, jest, supertest) are kept in this
# image because the demo runs in --watch mode behind docker-compose. For a
# real production push, use a multi-stage build that strips them.
COPY --chown=node:node package*.json ./
RUN npm install --no-audit --no-fund

# Copy app source under the unprivileged user.
COPY --chown=node:node . .

USER node

EXPOSE 3000

# Default command — overridden by docker-compose `command:` for dev (npm run dev).
CMD ["node", "presentation/server.js"]
