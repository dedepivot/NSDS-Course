package it.polimi.nsds.spark.lab.cities;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.RelationalGroupedDataset;
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

public class Cities {
    public static void main(String[] args) throws TimeoutException {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 1 ? args[1] : "./";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName("SparkEval")
                .config("spark.driver.host", "localhost").getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        final List<StructField> citiesRegionsFields = new ArrayList<>();
        citiesRegionsFields.add(DataTypes.createStructField("city", DataTypes.StringType, false));
        citiesRegionsFields.add(DataTypes.createStructField("region", DataTypes.StringType, false));
        final StructType citiesRegionsSchema = DataTypes.createStructType(citiesRegionsFields);

        final List<StructField> citiesPopulationFields = new ArrayList<>();
        citiesPopulationFields.add(DataTypes.createStructField("id", DataTypes.IntegerType, false));
        citiesPopulationFields.add(DataTypes.createStructField("city", DataTypes.StringType, false));
        citiesPopulationFields.add(DataTypes.createStructField("population", DataTypes.IntegerType, false));
        final StructType citiesPopulationSchema = DataTypes.createStructType(citiesPopulationFields);

        final Dataset<Row> citiesPopulation = spark
                .read()
                .option("header", "true")
                .option("delimiter", ";")
                .schema(citiesPopulationSchema)
                .csv(filePath + "files/cities/cities_population.csv");

        final Dataset<Row> citiesRegions = spark
                .read()
                .option("header", "true")
                .option("delimiter", ";")
                .schema(citiesRegionsSchema)
                .csv(filePath + "files/cities/cities_regions.csv");

        //IF u write the join with the "string" so that *usingColumn* appears, then we have a NATURAL JOIN
        //and only a single "join column" will appear
        //In this case we will have only a single "city" column
        final Dataset<Row> cityPopulationRegion = citiesPopulation.select("city", "population")
                .join(citiesRegions.select("city", "region"), "city")
                        .drop(citiesRegions.col("city")); //remove the second "city" column
        cityPopulationRegion.cache();
        //citiesRegions.show();

        //Compute the total population for each region
        final Dataset<Row> q1 = cityPopulationRegion.groupBy("region").sum("population");
        //q1.show();

        //Compute the number of cities and the population of the most populated city for each region
        final Dataset<Row> numberOfCityByRegion = cityPopulationRegion.groupBy("region").count();
        final Dataset<Row> inhabitantOfMostPopulatedCityByRegion = cityPopulationRegion.groupBy("region").max("population");

        final Dataset<Row> mostPopulatedCity = cityPopulationRegion.join(
                //This rename is only INSIDE THE JOIN
                inhabitantOfMostPopulatedCityByRegion
                        .withColumnRenamed("max(population)", "population"),"population")
                        .drop(inhabitantOfMostPopulatedCityByRegion.col("region"));

        mostPopulatedCity.show();

        final Dataset<Row> q2 = numberOfCityByRegion.join(mostPopulatedCity, "region")
                                .withColumnRenamed("count", "numberOfCity")
                                        .withColumnRenamed("city", "mostPopulatedCity");
        q2.show();

        // JavaRDD where each element is an integer and represents the population of a city
        JavaRDD<Integer> population = citiesPopulation.toJavaRDD().map(r -> r.getInt(2));
        // TODO: add code here to produce the output for query Q3

        // Bookings: the value represents the city of the booking
        final Dataset<Row> bookings = spark
                .readStream()
                .format("rate")
                .option("rowsPerSecond", 100)
                .load();

        final StreamingQuery q4 = null; // TODO query Q4

        try {
            q4.awaitTermination();
        } catch (final StreamingQueryException e) {
            e.printStackTrace();
        }

        spark.close();
    }
}