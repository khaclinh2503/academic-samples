import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.*;

@ApplicationPath("/rest")
public class JaxRsActivator extends Application
{
	private Set<Object> singletons;
	private Set<Class<?>> empty;

	public JaxRsActivator()
	{
		singletons = new HashSet<Object>();
		singletons.add(new StatsResource());
		
		empty = new HashSet<Class<?>>();
	}

	@Override
	public Set<Class<?>> getClasses()
	{
		return empty;
	}

	@Override
	public Set<Object> getSingletons()
	{
		return singletons;
	}
}

