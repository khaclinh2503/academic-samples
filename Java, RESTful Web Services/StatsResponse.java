import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="stats")
public class StatsResponse
{
	private Long average, currentElement, previousElement, standardDeviation;
	
	@SuppressWarnings("unused")
	private StatsResponse()
	{
		throw new UnsupportedOperationException("Default constructor is not supported.");
	}
	
	public StatsResponse(Long average, Long currentElement, Long previousElement, Long standardDeviation)
	{
		this.average = average;
		this.currentElement = currentElement;
		this.previousElement = previousElement;
		this.standardDeviation = standardDeviation;
	}
	
	@XmlElement(name="average")
	public Long getAverage()
	{
		return this.average;
	}
	
	@XmlElement(name="you-picked")
	public Long getCurrentElement()
	{
		return this.currentElement;
	}
	
	@XmlElement(name="last-pick")
	public Long getPreviousElement()
	{
		return this.previousElement;
	}
	
	@XmlElement(name="standard-deviation")
	public Long getStandardDeviation()
	{
		return this.standardDeviation;
	}
}
