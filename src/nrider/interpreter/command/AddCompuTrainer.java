package nrider.interpreter.command;

import nrider.core.WorkoutSession;
import nrider.interpreter.BaseCommand;
import nrider.io.ComputrainerController;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: Nov 1, 2009
 * Time: 11:16:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class AddCompuTrainer extends BaseCommand
{
	public String getDescription()
	{
		return "Add a CompuTrainer. - <COM PORT NAME>";
	}

	public String run( String[] args )
	{
		if( args.length < 1 || args.length > 2 )
		{
			return "Invalid syntax";
		}
		ComputrainerController compuTrainer = new ComputrainerController();
		compuTrainer.setCommPortName( args[0] );
		try
		{
			compuTrainer.connect();
		}
		catch( Exception e )
		{
			return "Error connecting to computrainer: " + e.getMessage();
		}

		WorkoutSession session = WorkoutSession.instance();

		session.addWorkoutController( compuTrainer );
		compuTrainer.addPerformanceDataListener( session );
		compuTrainer.addControlDataListener( session );

		if( args.length > 1 )
		{
			session.associateRider( args[1], compuTrainer.getIdentifier() );
		}

		return "Added " + compuTrainer.getIdentifier();
	}
}
