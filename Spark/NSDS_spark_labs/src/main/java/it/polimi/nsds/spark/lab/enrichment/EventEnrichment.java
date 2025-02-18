package it.polimi.nsds.spark.lab.enrichment;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.apache.spark.sql.functions.*;

/**
 * This code snippet exemplifies a typical scenario in event processing: merging
 * incoming events with some background knowledge.
 *
 * A static dataset (read from a file) classifies products (associates products to the
 * class they belong to).  A stream of products is received from a socket.
 *
 * We want to count the number of products of each class in the stream. To do so, we
 * need to integrate static knowledge (product classification) and streaming data
 * (occurrences of products in the stream).
 */
public class EventEnrichment {
    public static void main(String[] args) throws TimeoutException {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String socketHost = args.length > 1 ? args[1] : "localhost";
        final int socketPort = args.length > 2 ? Integer.parseInt(args[2]) : 9999;
        final String filePath = args.length > 3 ? args[3] : "./";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName("EventEnrichment")
                .getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        final List<StructField> productClassificationFields = new ArrayList<>();
        productClassificationFields.add(DataTypes.createStructField("product", DataTypes.StringType, false));
        productClassificationFields.add(DataTypes.createStructField("classification", DataTypes.StringType, false));
        final StructType productClassificationSchema = DataTypes.createStructType(productClassificationFields);

        //Input created by "rate", and is composed of a timestamp (real word clock) + value (an increasing integer)
        final Dataset<Row> inStream = spark
                .readStream()
                .format("rate")
                .option("rowsPerSecond", 1)
                .load()
                .withColumn("value", col("value").mod(10))
                .withColumnRenamed("value", "product");

        final Dataset<Row> productsClassification = spark
                .read()
                .option("header", "false")
                .option("delimiter", ",")
                .schema(productClassificationSchema)
                .csv(filePath + "files/enrichment/product_classification.csv");

//        StreamingQuery query = productsClassification.join(inStream, "product")
//                .groupBy("classification").count()
//                .writeStream()
//                .outputMode("complete")
//                .format("console")
//                .start();  // Start the query

        //Group the data by a windows of "largerzza 30 secondi" that is spawn every 5 secods. (element will ideall be counted 6 times 30/5=6)
        StreamingQuery windowQuery = productsClassification.join(inStream, "product")
                .groupBy(
                        window(col("timestamp"), "30 seconds", "5 seconds"),
                        col("classification"))
                .count()
                .writeStream()
                .outputMode("complete")
                .format("console")
                .start();  // Start the query

        try {
            windowQuery.awaitTermination();
        } catch (StreamingQueryException e) {
            throw new RuntimeException(e);
        }

        spark.close();
    }
}