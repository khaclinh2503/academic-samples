import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/number/pick")
public class StatsResource
{
	private Stats stats;

	public StatsResource()
	{
		this.stats = new Stats(100);
	}
	
    @GET
    @Path("/{num}")
    @Produces(MediaType.APPLICATION_XML)
    public synchronized StatsResponse processRequest(@PathParam("num") long num)
    {
    	return this.stats.push(num);
    }
}
