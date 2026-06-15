param(
    [string]$BootstrapServers = "localhost:9092",
    [string]$AssessmentsTopic = "sentinel.agents.assessments",
    [string]$DltTopic = "sentinel.agents.assessments.DLT",
    [int]$Partitions = 6,
    [int]$ReplicationFactor = 1
)

$containerName = "sentinel-kafka"

Write-Host "Creando topics Kafka en $BootstrapServers..."

docker exec $containerName /opt/bitnami/kafka/bin/kafka-topics.sh --bootstrap-server $BootstrapServers --create --if-not-exists --topic $AssessmentsTopic --partitions $Partitions --replication-factor $ReplicationFactor
docker exec $containerName /opt/bitnami/kafka/bin/kafka-topics.sh --bootstrap-server $BootstrapServers --create --if-not-exists --topic $DltTopic --partitions $Partitions --replication-factor $ReplicationFactor

Write-Host "Topics listos."