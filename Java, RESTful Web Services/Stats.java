import java.util.LinkedList;

public class Stats
{
	private LinkedList<Double> queue;
	private int maxQueueSize;
	private Double mean, standardDeviation, currentElement, previousElement;
	
	public Stats(int maxQueueSize)
	{
		if (maxQueueSize <= 0)
			throw new IllegalArgumentException("maxQueueSize must be > 0");
		
		this.maxQueueSize = maxQueueSize;
		this.queue = new LinkedList<Double>();
		
		this.mean = null;
		this.standardDeviation = null;
		this.currentElement = null;
		this.previousElement = null;
	}
	
	public StatsResponse push(double num)
	{
		// update the previous element before we push the new one to the queue
		if (this.queue.size() > 0)
			this.previousElement = this.queue.getFirst();
		
		// if the queue is at its' maximum size, remove the last element
		if (this.queue.size() == this.maxQueueSize)
			this.queue.pollLast();
		
		this.currentElement = num;
		
		this.queue.push(num);
		
		
		// update the mean
		double tempSum = 0.0;
		
		for (double element : this.queue)
			tempSum += element;
		
		this.mean =  tempSum / (double)this.queue.size();
		
		
		// update the standard deviation
		tempSum = 0.0;
		
		for (double element : this.queue)
			tempSum += Math.pow(element - this.mean, 2.0);
		
		this.standardDeviation =  Math.pow(tempSum / (double)this.queue.size(), 0.5);
		
		
		return new StatsResponse(getAverage(), getCurrentElement(), getPreviousElement(), getStandardDeviation());
	}
	
	public Long getAverage()
	{
		if (this.mean == null)
			return null;
		
		return this.mean.longValue();
	}
	
	public Long getCurrentElement()
	{
		if (this.currentElement == null)
			return null;
		
		return this.currentElement.longValue();
	}
	
	public Long getPreviousElement()
	{
		if (this.previousElement == null)
			return null;
		
		return this.previousElement.longValue();
	}
	
	public Long getStandardDeviation()
	{
		if (this.standardDeviation == null)
			return null;
			
		return this.standardDeviation.longValue();
	}
}