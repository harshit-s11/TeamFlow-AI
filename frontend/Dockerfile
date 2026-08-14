# Stage 1: Build React SPA
FROM node:20-alpine AS builder
WORKDIR /app

# Accept build-time API base URL
ARG VITE_API_BASE_URL=http://localhost:8080
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

# Install package dependencies
COPY package.json package-lock.json ./
RUN npm ci

# Copy source code and build production bundle
COPY . .
RUN npm run build

# Stage 2: Serve static bundle via NGINX
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Install curl for container health check
RUN apk add --no-cache curl

# Remove default static contents
RUN rm -rf ./*

# Copy build artifacts and custom NGINX SPA configuration
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=10s --timeout=5s --start-period=5s --retries=5 \
  CMD curl -f http://localhost:80 || exit 1

CMD ["nginx", "-g", "daemon off;"]
