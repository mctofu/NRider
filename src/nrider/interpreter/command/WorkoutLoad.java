package nrider.interpreter.command;

import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;
import nrider.ride.IRide;
import nrider.ride.RideLoader;
import nrider.ride.TimeBasedRide;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: Nov 1, 2009
 * Time: 4:26:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkoutLoad extends BaseCommand
{
	public String getDescription()
	{
		return "Loads a workout file.  <filename>";
	}

	@Override
	public String run( String[] args ) throws Exception
	{
		IRide ride = new RideLoader().loadRide( args[0] );
		if( args.length > 1 )
		{
			ride.getScript().adjustLoad( Double.parseDouble( args[1] ));
		}
		WorkoutSession.instance().setRide( ride );
		return null;
	}
}
