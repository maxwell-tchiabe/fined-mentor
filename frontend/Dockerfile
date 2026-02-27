# Stage 1: Build the Angular apps
FROM node:20-alpine AS build

WORKDIR /app

COPY package*.json ./

RUN npm ci

COPY . .

RUN npm run build -- --configuration production

# Stage 2: Serve the application with Nginx
FROM nginx:alpine

COPY --from=build /app/dist/fined-mentor-frontend/browser /usr/share/nginx/html

# Use /etc/nginx/templates/ for dynamic configuration with envsubst
COPY nginx.conf /etc/nginx/templates/default.conf.template

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
