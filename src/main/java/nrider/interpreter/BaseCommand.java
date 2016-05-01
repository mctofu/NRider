package nrider.interpreter;

import nrider.interpreter.ICommand;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: Oct 31, 2009
 * Time: 11:31:51 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class BaseCommand implements ICommand
{
	public String getName()
	{
		String name = this.getClass().getSimpleName();
		return name.substring( 0, 1 ).toLowerCase() + name.substring( 1, name.length() );
	}

	public abstract String run( String[] args ) throws Exception;

	public String execute( String[] args )
	{
		try
		{
			return run( args );
		}
		catch( Exception e )
		{
			e.printStackTrace( );
			return "Error: " + e.getMessage();
		}
	}
}
