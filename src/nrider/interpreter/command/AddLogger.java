/*
 * Copyright (c) 2009 David McIntosh (david.mcintosh@yahoo.com)
 *  
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package nrider.interpreter.command;

import nrider.core.WorkoutSession;
import nrider.datalog.RideLogger;
import nrider.interpreter.BaseCommand;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Jul 29, 2010
 * Time: 7:32:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddLogger extends BaseCommand
{
	public String getDescription()
	{
		return "Add a data logger";
	}

	public String run( String[] args )
	{
		RideLogger rideLogger = new RideLogger();
		WorkoutSession.instance().addPerformanceDataListener( rideLogger );
		WorkoutSession.instance().addWorkoutListener( rideLogger );
		return null;
	}
}