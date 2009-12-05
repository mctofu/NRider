package nrider.interpreter.command;

import nrider.core.Rider;
import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: Nov 1, 2009
 * Time: 9:31:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class ListRiders extends BaseCommand
{
	public String getDescription()
	{
		return "List riders in the workout";
	}

	public String run( String[] args )
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter( sw );
		for( Rider r : WorkoutSession.instance().getRiders() )
		{
			pw.println( r.getName() + " " + r.getThresholdPower() );
		}
		pw.flush();
		return sw.toString();
	}
}
