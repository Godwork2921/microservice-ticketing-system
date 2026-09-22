# Kafka (KRaft)

Local Kafka runs via root `docker-compose.yml` using Apache Kafka in KRaft mode (no Zookeeper).

Bootstrap from the host:

`KAFKA_BOOTSTRAP_SERVERS=localhost:9092`

Bootstrap from other Docker services on the `ticketing` network:

`KAFKA_BOOTSTRAP_SERVERS=kafka:29092`

Topic names are defined in `common-lib` (`KafkaTopics`). Stage 13 creates topic provisioning scripts if auto-create remains disabled.
