package nrider.interpreter;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: Oct 31, 2009
 * Time: 11:09:37 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ICommand
{
	String getName();
	String getDescription();
	String execute( String[] args );
}
