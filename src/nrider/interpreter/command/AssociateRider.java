package nrider.interpreter.command;

import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: Nov 1, 2009
 * Time: 11:22:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class AssociateRider extends BaseCommand
{
	public String getDescription()
	{
		return "Associate a rider with a device.  <user id> <device id>";
	}

	public String run( String[] args )
	{
		if( args.length != 2 )
		{
			return "Invalid syntax";
		}

		WorkoutSession session = WorkoutSession.instance();

		session.associateRider( args[0], args[1] );
		return null;
	}
}
