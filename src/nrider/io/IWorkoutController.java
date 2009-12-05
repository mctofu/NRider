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
package nrider.io;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: Nov 1, 2009
 * Time: 9:43:17 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IWorkoutController
{
	public enum TrainerMode { ERG, GRADIENT };

	public String getType();
	public String getIdentifier();
	public void setLoad( double load );
	public int getLoad();
	public void setMode( TrainerMode mode );
	public TrainerMode getMode();
	public void close() throws IOException;
}
