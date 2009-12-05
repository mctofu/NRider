package nrider.interpreter.command;

import nrider.core.Rider;
import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: Oct 31, 2009
 * Time: 11:30:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddRider extends BaseCommand
{
	public String getDescription()
	{
		return "Add a rider to the workout: <name> <threshold power>";  //To change body of implemented methods use File | Settings | File Templates.
	}

	public String run( String[] args )
	{
		if( args.length != 2 )
		{
			return "Invalid usage";
		}
		Rider rider = new Rider();
		rider.setName( args[0] );
		rider.setThresholdPower( Integer.parseInt( args[1] ) );
		WorkoutSession.instance().addRider( rider );
		return null;
	}
}
