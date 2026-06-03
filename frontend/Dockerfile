FROM node:20-alpine AS builder
WORKDIR /app

ARG REACT_APP_BFF_URL=http://localhost:3000
ENV REACT_APP_BFF_URL=$REACT_APP_BFF_URL

COPY package.json ./
RUN npm install --omit=dev --legacy-peer-deps

COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/build /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
