# Multi-stage build for React frontend
FROM node:18-alpine AS build

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies using Taobao registry (faster in Asia/Vietnam)
RUN npm install --registry=https://registry.npmmirror.com

# Copy source code
COPY . .

# Build React app
ARG REACT_APP_API_URL=http://localhost:8080/api
ENV REACT_APP_API_URL=$REACT_APP_API_URL
RUN npm run build

# Production stage with nginx
FROM nginx:alpine

# Install curl for health check
RUN apk add --no-cache curl

# Copy nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Copy built app from build stage
COPY --from=build /app/build /usr/share/nginx/html

# Create app user for security
RUN addgroup -S appuser && adduser -S appuser -G appuser -h /app -u 1001

# Create required directories and change ownership
RUN mkdir -p /etc/nginx/cache && chown -R appuser:appuser /usr/share/nginx/html /etc/nginx/cache /var/cache/nginx /var/run /run /etc/nginx/conf.d

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=10s \
    CMD curl -f http://localhost/health || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
