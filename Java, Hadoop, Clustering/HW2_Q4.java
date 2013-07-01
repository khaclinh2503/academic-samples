// Philip Scuderi
// Stanford University
// Winter 2013
// CS 246
// Homework 2
// Question 4

import java.io.IOException;
import java.util.*;

import java.io.*;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class HW2_Q4
{
	public static class Map1 extends Mapper<LongWritable, Text, IntWritable, Text>
	{
		protected static List<List<Double>> centroids = new ArrayList<List<Double>>();
		protected int k;

	    protected void setup(Context context) throws IOException, InterruptedException
	    {
	    	FileSystem fs = FileSystem.get(context.getConfiguration());

	    	Path cFile = new Path(context.getConfiguration().get("CFILE"));
	    	DataInputStream d = new DataInputStream(fs.open(cFile));
	    	BufferedReader reader = new BufferedReader(new InputStreamReader(d));

	    	String line;
	    	while ((line = reader.readLine()) != null)
	    	{
	    		StringTokenizer tokenizer = new StringTokenizer(line.toString());

	    		if (tokenizer.hasMoreTokens())
	    		{
		    		List<Double> centroid = new ArrayList<Double>(58);

		    		while (tokenizer.hasMoreTokens())
		    		{
		    			centroid.add(Double.parseDouble(tokenizer.nextToken()));
		    		}

		    		centroids.add(centroid);
	    		}
	    	}

	    	k = centroids.size();
	    }

	    protected static double Distance(List<Double> p1, List<Double> p2)
	    {
	    	double sumOfSquaredDifferences = 0.0;

	    	for (int i = 0; i < p1.size(); i++)
	    	{
	    		sumOfSquaredDifferences += Math.pow(p1.get(i) - p2.get(i), 2.0);
	    	}

	    	return Math.pow(sumOfSquaredDifferences, 0.5);
	    }

	    private int IndexOfClosestCentroid(List<Double> p)
	    {
	    	int idxClosestCentoid = -1;
	    	double distanceToClosestCentroid = Double.MAX_VALUE;

	    	for (int i = 0; i < k; i++)
	    	{
	    		double d = Distance(centroids.get(i), p);

	    		if (d < distanceToClosestCentroid)
	    		{
	    			idxClosestCentoid = i;
	    			distanceToClosestCentroid = d;
	    		}
	    	}

	    	return idxClosestCentoid;
	    }

	    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
	    {
	    	// read in a document (point in 58-dimensional space)

	    	List<Double> p = GetPoint(value.toString());

	    	int idxClosestCentoid = IndexOfClosestCentroid(p);

	    	context.write(new IntWritable(idxClosestCentoid), value);
	    }
	}

	public static class Reduce1 extends Reducer<IntWritable, Text, IntWritable, Text>
	{
		protected static List<String> newCentroids = new ArrayList<String>(10);

	    public void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException
	    {
	    	List<Double> newCentroid = null;
	    	int numPoints = 0;

	        for (Text value : values)
	        {
	        	++numPoints;

	        	List<Double> p = GetPoint(value.toString());

	        	if (newCentroid == null)
	        	{
	        		// initialize the new centroid to the first element
	        		newCentroid = new ArrayList<Double>(p);
	        	}
	        	else
	        	{
	        		for (int i = 0; i < newCentroid.size(); i++)
	        		{
	        			newCentroid.set(i, newCentroid.get(i) + p.get(i));
	        		}
	        	}
	        }

	        // now the newCentroid contains the sum of all the points
	        // so to get the average of all the points, we need to
	        // divide each entry by the total number of points

    		for (int i = 0; i < newCentroid.size(); i++)
    		{
    			newCentroid.set(i, newCentroid.get(i) / (double)numPoints);
    		}

    		// now create a string containing all the new centroid's coordinates

    		String s = null;
    		for (Double d : newCentroid)
    		{
    			if (s == null)
    			{
    				s = d.toString();
    			}
    			else
    			{
    				s += " " + d.toString();
    			}
    		}

    		newCentroids.add(s);

    		if (newCentroids.size() == 10)
    		{
    			WriteNewCentroids(context);
    		}

    		// output the centroid ID and the centroid data
	        context.write(key, new Text(s));
	    }

	    private static void WriteNewCentroids(Context context) throws IOException
	    {
	    	FileSystem fs = FileSystem.get(context.getConfiguration());
	    	Path nextCFile = new Path(context.getConfiguration().get("NEXTCFILE"));
	    	DataOutputStream d = new DataOutputStream(fs.create(nextCFile, false));
	    	BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(d));

	    	for (String centroid : newCentroids)
	    	{
	    		writer.write(centroid + "\n");
	    	}

	    	writer.close();
	    }
	}

    public static List<Double> GetPoint(String s)
    {
    	StringTokenizer tokenizer = new StringTokenizer(s);

    	List<Double> p = new ArrayList<Double>(58);

    	while (tokenizer.hasMoreTokens())
    	{
    		p.add(Double.parseDouble(tokenizer.nextToken()));
    	}

    	return p;
    }

	 public static void main(String[] args) throws Exception
	 {
		String inputDirectory = "/home/cs246/Desktop/HW2/input";
		String outputDirectory = "/home/cs246/Desktop/HW2/output";
		String centroidDirectory = "/home/cs246/Desktop/HW2/config";

		int iterations = 20;

		for (int i = 1; i <= iterations; i++)
		{
			Configuration conf = new Configuration();

			String cFile = centroidDirectory + "/c" + i + ".txt";
			String nextCFile = centroidDirectory + "/c" + (i+1) + ".txt";
			conf.set("CFILE", cFile);
			conf.set("NEXTCFILE", nextCFile);

			String cFile = centroidDirectory + "/c" + i + ".txt";
			String nextCFile = centroidDirectory + "/c" + (i+1) + ".txt";
			conf.set("CFILE", cFile);
			conf.set("NEXTCFILE", nextCFile);

			Job job = new Job(conf, "HW2_Q4." + i);
			job.setJarByClass(HW2_Q4.class);
			job.setOutputKeyClass(IntWritable.class);
			job.setOutputValueClass(Text.class);
			job.setMapperClass(Map1.class);
			job.setReducerClass(Reduce1.class);
			job.setInputFormatClass(TextInputFormat.class);
			job.setOutputFormatClass(TextOutputFormat.class);

			FileInputFormat.addInputPath(job, new Path(inputDirectory));
			FileOutputFormat.setOutputPath(job, new Path(outputDirectory + "/output" + i));

			job.waitForCompletion(true);
		}
	 }
}