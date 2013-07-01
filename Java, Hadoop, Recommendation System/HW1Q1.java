// Philip Scuderi
// Stanford Univeristy
// Winter 2013
// CS 246
// Homework 1
// Question 1

import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class HW1Q1
{
	public static class Map1 extends Mapper<LongWritable, Text, Text, IntWritable>
	{
		private final static IntWritable zero = new IntWritable(0);
	    private final static IntWritable one = new IntWritable(1);

	    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
	    {
	    	StringTokenizer tokenizer = new StringTokenizer(value.toString(), " \t,");

	    	// exit if we hit a blank line
	    	if (! tokenizer.hasMoreTokens())
	    		return;

	    	int user = Integer.parseInt(tokenizer.nextToken());

	    	ArrayList<Integer> friends =  new ArrayList<Integer>();

	    	while (tokenizer.hasMoreTokens())
			{
	    		int friend = Integer.parseInt(tokenizer.nextToken());
	    		friends.add(friend);
			}

	    	Collections.sort(friends);

	    	for (int i = 0; i < friends.size(); i++)
	    	{
	    		int f1 = friends.get(i);

	    		// create a key representing the user and his/her current friend
	    		// assure that the lower of user and f1 is first in the key
	    		// this is a direct (first degree) connection, therefore we
	    		// flag this connection by using the zero flag

	    		String s1;
	    		if (user <= f1)
	    		{
	    			s1 = Integer.toString(user) + " " + Integer.toString(f1);
	    		}
	    		else
	    		{
	    			s1 = Integer.toString(f1) + " " + Integer.toString(user);
	    		}
	    		context.write(new Text(s1), zero);

	    		for (int j = i+1; j < friends.size(); j++)
	    		{
	    			int f2 = friends.get(j);

	    			String s2 = Integer.toString(f1) + " " + Integer.toString(f2);

	    			// f1 is always <= f2 because we sorted the array-list above
	    			// this connection represents one 2nd degree connection, therefore
	    			// we use a one to represent one connection
	    			context.write(new Text(s2), one);
	    		}
	    	}
	    }
	}

	public static class Reduce1 extends Reducer<Text, IntWritable, Text, IntWritable>
	{
	    public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException
	    {
	    	int numMutualFriends = 0;

	        for (IntWritable val : values)
	        {
	        	// if we see a value of 0 then the two users represented by this key
	        	// are already direct friends, so exit the function
	        	if (val.get() == 0)
	        		return;

	        	numMutualFriends += val.get();
	        }

	        // if we make it this far then there is no direct fiend connection
	        // and numMutualFriends is the number of mutual friends
	        context.write(key, new IntWritable(numMutualFriends));
	    }
	 }

	public static class Map2 extends Mapper<LongWritable, Text, IntWritable, Text>
	{
	    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
	    {
	    	StringTokenizer tokenizer = new StringTokenizer(value.toString());

	    	// exit if we hit a blank line
	    	if (! tokenizer.hasMoreTokens())
	    		return;

	    	int user1 = Integer.parseInt(tokenizer.nextToken());
	    	int user2 = Integer.parseInt(tokenizer.nextToken());
	    	int mutualFriends = Integer.parseInt(tokenizer.nextToken());

	    	// string 1 is a value representing that user1 has mutualFriends in common with user2
	    	String s1 = user2 + " " + mutualFriends;
	    	context.write(new IntWritable(user1), new Text(s1));

	    	// string 2 is a value representing that user2 has mutualFriends in common with user1
	    	String s2 = user1 + " " + mutualFriends;
	    	context.write(new IntWritable(user2), new Text(s2));
	    }
	}

	public static class Reduce2 extends Reducer<IntWritable, Text, IntWritable, Text>
	{
	    public void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException
	    {
	    	int user = key.get();
	    	TreeMap<Integer, ArrayList<Integer>> sortedMap = new TreeMap<Integer, ArrayList<Integer>>();

	        for (Text val : values)
	        {
	        	StringTokenizer tokenizer = new StringTokenizer(val.toString());

		    	int potentialFriend = Integer.parseInt(tokenizer.nextToken());
		    	int mutualFriends = Integer.parseInt(tokenizer.nextToken());

		    	if (sortedMap.get(mutualFriends) == null)
		    	{
		    		// there are no users yet with this number of mutual friends

		    		ArrayList<Integer> arrayList = new ArrayList<Integer>();
		    		arrayList.add(potentialFriend);

		    		sortedMap.put(mutualFriends, arrayList);
		    	}
		    	else
		    	{
		    		// there are already users with this number of mutual friends
		    		sortedMap.get(mutualFriends).add(potentialFriend);
		    	}
	        }


	        // now select the top 10 users to recommend as potential friends

	        ArrayList<Integer> recommendations = new ArrayList<Integer>();

	        boolean exitLoops = false;

	        for (int mutualFriends:sortedMap.descendingKeySet())
	        {
	        	ArrayList<Integer> potentialFriends = sortedMap.get(mutualFriends);
	        	Collections.sort(potentialFriends);

	        	for (int potentialFriend:potentialFriends)
	        	{
	        		recommendations.add(potentialFriend);

	        		if (recommendations.size() == 10)
	        		{
	        			exitLoops = true;
	        			break;
	        		}
	        	}

	        	if (exitLoops)
	        		break;
	        }

	        if (recommendations.size() == 0)
	        	return;

	        String s = null;

	        for (int recommendation:recommendations)
	        {
	        	if (s == null)
	        		s = Integer.toString(recommendation);
	        	else
	        		s += "," + recommendation;
	        }

	        context.write(new IntWritable(user), new Text(s));
	    }
	 }

	 public static void main(String[] args) throws Exception
	 {
	    Configuration conf = new Configuration();


	    Job job1 = new Job(conf, "HW1Q1.1");
	    job1.setJarByClass(HW1Q1.class);
	    job1.setOutputKeyClass(Text.class);
	    job1.setOutputValueClass(IntWritable.class);

	    job1.setMapperClass(Map1.class);
	    job1.setReducerClass(Reduce1.class);

	    job1.setInputFormatClass(TextInputFormat.class);
	    job1.setOutputFormatClass(TextOutputFormat.class);

	    FileInputFormat.addInputPath(job1, new Path("/home/cs246/Desktop/InputData"));
	    FileOutputFormat.setOutputPath(job1, new Path("/home/cs246/Desktop/IntermediateData"));

	    job1.waitForCompletion(true);


	    Job job2 = new Job(conf, "HW1Q1.2");
	    job2.setJarByClass(HW1Q1.class);
	    job2.setOutputKeyClass(IntWritable.class);
	    job2.setOutputValueClass(Text.class);

	    job2.setMapperClass(Map2.class);
	    job2.setReducerClass(Reduce2.class);

	    job2.setInputFormatClass(TextInputFormat.class);
	    job2.setOutputFormatClass(TextOutputFormat.class);

	    FileInputFormat.addInputPath(job2, new Path("/home/cs246/Desktop/IntermediateData"));
	    FileOutputFormat.setOutputPath(job2, new Path("/home/cs246/Desktop/OutputData"));

	    job2.waitForCompletion(true);
	 }
}