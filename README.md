# Kafka example

docker-compose up -d

curl -X POST http://localhost:8080/send
curl -X POST http://localhost:8080/send/error
curl -X POST http://localhost:8080/send/count/10

curl -X POST http://localhost:8080/reactive/send
curl -X POST http://localhost:8080/reactive/send/error
curl -X POST http://localhost:8080/reactive/send/count/10
