package nrider.interpreter.command;

import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: Nov 1, 2009
 * Time: 4:10:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class RiderChangeThreshold extends BaseCommand
{
	public String getDescription()
	{
		return "Change threshold setting of a rider: <riderId> <new threshold>";
	}

	@Override
	public String run( String[] args )
	{
		WorkoutSession.instance().setRiderThreshold( args[0], Integer.parseInt( args[1] ) );
		return null;
	}
}
