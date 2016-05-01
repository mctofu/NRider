package nrider.interpreter.command;

import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: Nov 1, 2009
 * Time: 2:35:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class StartWorkout extends BaseCommand
{
	public String getDescription()
	{
		return "Starts the loaded workout";
	}

	@Override
	public String run( String[] args )
	{
		WorkoutSession.instance().startRide();
		return null;
	}
}
