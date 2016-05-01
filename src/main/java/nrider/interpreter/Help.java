package nrider.interpreter;

import nrider.interpreter.CommandInterpreter;
import nrider.interpreter.BaseCommand;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: Oct 31, 2009
 * Time: 11:34:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class Help extends BaseCommand
{
	public CommandInterpreter _commandInterpreter;

	public Help( CommandInterpreter ci )
	{
		_commandInterpreter = ci;
	}

	public String getDescription()
	{
		return "Lists available commands";
	}

	public String run( String[] args )
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter( sw );

	    for( ICommand command : _commandInterpreter.getCommands() )
		{
			pw.println( command.getName() + ": " + command.getDescription() );
		}
		pw.println( "exit: Quit the application" );
		pw.flush();
		return sw.toString();
	}
}
