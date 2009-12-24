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
package nrider.core;

import nrider.ride.IRide;

import java.util.EventListener;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: Nov 1, 2009
 * Time: 11:06:23 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IWorkoutListener extends EventListener
{
	public void handleLoadAdjust( String riderId, RideLoad newLoad );
	public void handleAddRider( Rider rider );
	public void handleRiderThresholdAdjust( String riderId, double newThreshold );
	public void handleRideLoaded( IRide ride );
	public void handleRideTimeUpdate( long rideTime );
	public void handleRiderDistanceUpdate( String riderId, double distance );
	public void handleAddRiderAlert( String riderId, WorkoutSession.RiderAlertType type );
	public void handleRemoveRiderAlert( String riderId, WorkoutSession.RiderAlertType type );
}
