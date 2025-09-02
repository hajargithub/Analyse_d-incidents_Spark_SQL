#!/usr/bin/env bash
set -euo pipefail

# Éviter Kerberos/UGI & forcer un user "simple"
export USER=spark
export SPARK_USER=spark
export HADOOP_USER_NAME=spark

# Options JDK17 nécessaires pour Spark 3.x
export SPARK_SUBMIT_OPTS="--add-exports=java.base/sun.nio.ch=ALL-UNNAMED --add-exports=java.base/jdk.internal.misc=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED -Duser.name=spark"

# Corrige l'erreur Ivy "basedir must be absolute"
mkdir -p /tmp/.ivy2

# (Re)crée la sortie
rm -rf /shared/output

/opt/bitnami/spark/bin/spark-submit \
  --master spark://spark-master:7077 \
  --class ma.enset.incidents.IncidentsApp \
  --conf spark.jars.ivy=/tmp/.ivy2 \
  --conf spark.sql.warehouse.dir=file:/tmp/spark-warehouse \
  --conf spark.hadoop.hadoop.security.authentication=simple \
  --conf "spark.driver.extraJavaOptions=${SPARK_SUBMIT_OPTS}" \
  --conf "spark.executor.extraJavaOptions=${SPARK_SUBMIT_OPTS}" \
  /shared/app.jar \
  /shared/incidents.csv \
  /shared/output
