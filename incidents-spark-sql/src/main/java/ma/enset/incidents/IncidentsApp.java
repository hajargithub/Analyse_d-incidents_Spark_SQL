package ma.enset.incidents;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import static org.apache.spark.sql.functions.*;

/**
 * TP3 Spark SQL - Incidents
 *
 * Arguments:
 *   args[0] = chemin du CSV d'incidents (défaut: data/incidents.csv)
 *   args[1] = dossier de sortie (défaut: output)
 *
 * Sorties:
 *   output/incidents_by_service
 *   output/top_years
 */
public class IncidentsApp {
    public static void main(String[] args) {
        String inputPath = args.length > 0 ? args[0] : "data/incidents.csv";
        String outputDir = args.length > 1 ? args[1] : "output";

        SparkSession spark = SparkSession.builder()
                .appName("Incidents Analytics - TP3 Spark SQL")
                // local[*] pour l'exécution locale (sera ignoré si spark-submit --master ... est utilisé)
                .master("local[*]")
                .getOrCreate();

        spark.sparkContext().setLogLevel("WARN");

        // Lecture CSV
        Dataset<Row> df = spark.read()
                .option("header", true)
                .option("inferSchema", true)
                .csv(inputPath)
                .toDF("id", "titre", "description", "service", "date");

        // 1) Nombre d'incidents par service
        Dataset<Row> incidentsByService = df.groupBy("service")
                .agg(count(lit(1)).alias("nb_incidents"))
                .orderBy(desc("nb_incidents"));

        // 2) Les deux années avec le plus d'incidents (extraction robuste de l'année)
        Dataset<Row> topYears = df.withColumn("year",
                        regexp_extract(col("date").cast("string"), "(\\d{4})", 1))
                .groupBy("year")
                .agg(count(lit(1)).alias("nb_incidents"))
                .orderBy(desc("nb_incidents"))
                .limit(2);

        System.out.println("=== Nombre d'incidents par service ===");
        incidentsByService.show(false);

        System.out.println("=== Top 2 années avec le plus d'incidents ===");
        topYears.show(false);

        // Ecriture des résultats
        incidentsByService.coalesce(1).write()
                .mode("overwrite")
                .option("header", true)
                .csv(outputDir + "/incidents_by_service");

        topYears.coalesce(1).write()
                .mode("overwrite")
                .option("header", true)
                .csv(outputDir + "/top_years");

        spark.stop();
    }
}
