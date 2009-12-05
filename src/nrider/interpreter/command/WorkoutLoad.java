package nrider.interpreter.command;

import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;
import nrider.ride.ScriptedRide;

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
		ScriptedRide ride = new ScriptedRide();
		ride.load( args[0] );
		WorkoutSession.instance().setRide( ride );
		return null;
	}
}
