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
import scala.Function2;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.apache.spark.sql.functions.*;

public class Cities {
    public static void main(String[] args) throws TimeoutException {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 1 ? args[1] : "./";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName("SparkEval")
                //.config("spark.driver.host", "localhost")
                .getOrCreate();
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
        citiesRegions.cache();

        final Dataset<Row> cityRegionPop = citiesRegions.join(citiesPopulation, "city");
        cityRegionPop.cache();

        //Todo: compute the total population for each region
        final Dataset<Row> q1 = cityRegionPop.groupBy("region").sum("population");
        q1.show();

        //Todo: compute the number of cities and the population of the most populated city for each region
        final Dataset<Row> citiesNumber = citiesRegions.groupBy("region").count();
        final Dataset<Row> mostPopulatedCity = cityRegionPop.groupBy("region").max("population").withColumnRenamed("max(population)", "population")
                        .join(cityRegionPop.drop("region"), "population").drop("population");
        final Dataset<Row> q2 = citiesNumber.join(mostPopulatedCity, "region").withColumnRenamed("count", "Number Of Cities").withColumnRenamed("city", "Most Populated City");
        q2.show();

        // JavaRDD where each element is an integer and represents the population of a city
        JavaRDD<Integer> population = citiesPopulation.toJavaRDD().map(r -> r.getInt(2));
        population.cache();
        // TODO: Print the evolution of the population in Italy year by year until the total population in Italy overcomes 100M people
        // In cities > 1000 inhabitants, it increases by 1% every year | In cities < 1000 inhabitants decrease 1%
        int j = 0;
        int sum = population.reduce(Integer::sum);
        System.out.println("Year: " + j + ", total population: " + sum);
        while(sum < 100000000){
            j++;
            population = population.map(i -> {
                if(i < 1000){
                    return (int) (i-i*0.01);
                }else{
                    return (int) (i+i*0.01);
                }
            });
            population.cache();
            sum = population.reduce(Integer::sum);
            System.out.println("Year: " + j + ", total population: " + sum);
        }

        //Todo: compute the total number of bookings for each region, in a window of 30 seconds, sliding every 5 seconds
        // Bookings: the col "value" represents the city of the booking
        final Dataset<Row> bookings = spark
                .readStream()
                .format("rate")
                .option("rowsPerSecond", 100)
                .load()
                .withColumnRenamed("value", "id");

        final StreamingQuery q4 = bookings.join(cityRegionPop, "id")
                .groupBy(
                        window(col("timestamp"), "30 seconds", "5 seconds"),
                        col("region"))
                .count()
                .writeStream()
                .outputMode("update")
                .format("console")
                .start();

        try {
            q4.awaitTermination();
        } catch (final StreamingQueryException e) {
            e.printStackTrace();
        }

        spark.close();
    }
}