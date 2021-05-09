FROM node:latest AS build

RUN mkdir -p /app/src
WORKDIR /app/src

COPY . .

RUN npm i
RUN npm run build-prod

RUN cp /app/src/dist/TimeGuess/assets/config.example.json /app/src/dist/TimeGuess/assets/config.json -n

FROM httpd:latest
COPY --from=build /app/src/dist/TimeGuess /usr/local/apache2/htdocs
COPY httpd.conf /usr/local/apache2/conf/httpd.conf