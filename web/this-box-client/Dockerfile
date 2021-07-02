FROM node:14.16.1 as builder
WORKDIR /app/this-box-client
COPY package.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx
EXPOSE 3000
COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/this-box-client/build /usr/share/nginx/html