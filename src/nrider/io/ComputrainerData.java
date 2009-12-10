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

/**
 * 7 byte messages from the CompuTrainer
 */
public class ComputrainerData
{
	public enum Type
	{ SPEED, POWER, HEARTRATE, CADENCE, RRC, SENSOR };

	public final static int CT_RESET = 0x01;
	public final static int CT_F1 = 0x02;
	public final static int CT_F3 =0x04;
	public final static int CT_PLUS = 0x08;
	public final static int CT_F2 = 0x10;
	public final static int CT_MINUS = 0x20;
	public final static int CT_SSS = 0x40;    // spinscan sync is not a button!
	public final static int CT_NONE = 0x80;
	private int _ss1;
	private int _ss2;
	private int _ss3;
	private int _buttons;
	private Type _type;
	private int _value8;
	private int _value12;


	/**
	 * Construct telemetry from computrainer message
	 * @param msg
	 */
	public ComputrainerData( byte[] msg )
	{
		// inbound data is in the 7 byte array Computrainer::buf[]
		// for code clarity they hjave been put into these holdiing
		// variables. the overhead is minimal and makes the code a
		// lot easier to decipher! :-)

		short s1 = msg[0]; // ss data
		short s2 = msg[1]; // ss data
		short s3 = msg[2]; // ss data
		short bt = msg[3]; // button data
		short b1 = msg[4]; // message and value
		short b2 = msg[5]; // value
		short b3 = msg[6]; // the dregs (sync, z and lsb for all the others)

		// ss vars
		_ss1 = s1<<1 | (b3&32)>>5;
		_ss2 = s2<<1 | (b3&16)>>4;
		_ss3 = s3<<1 | (b3&8)>>3;

		// buttons
		_buttons = bt<<1 | (b3&4)>>2;

		// 4-bit message type
		switch( (b1&120)>>3 )
		{
			case 0x01:
				_type = Type.SPEED;
				break;
			case 0x02:
				_type = Type.POWER;
				break;
			case 0x03:
				_type = Type.HEARTRATE;
				break;
			case 0x06:
				_type = Type.CADENCE;
				break;
			case 0x09:
				_type = Type.RRC;
				break;
			case 0x0b:
				_type = Type.SENSOR;
				break;
		}
		// 8 bit value
		_value8 = (b2&~128)<<1 | (b3&1); // 8 bit values

		if( _type == Type.RRC )
		{
			if( ( b1 & 4 ) > 0 )
			{
				_value12 = _value8 | ( b1 & 3 ) << 9 | (b3 & 2 ) << 7;
			}
			else
			{
				_value12 = -256;
			}
		}
		else
		{
			// 12 bit value
			_value12 = _value8 | (b1&7)<<9 | (b3&2)<<7;
		}

	}

	public int getSs1()
	{
		return _ss1;
	}

	public int getSs2()
	{
		return _ss2;
	}

	public int getSs3()
	{
		return _ss3;
	}

	public int getButtons()
	{
		return _buttons;
	}

	public Type getType()
	{
		return _type;
	}

	public int getValue8()
	{
		return _value8;
	}

	public int getValue12()
	{
		return _value12;
	}

	public boolean isResetPressed()
	{
		return ( _buttons & CT_RESET ) > 0;
	}

	public boolean isF1Pressed()
	{
		return ( _buttons & CT_F1 ) > 0;
	}

	public boolean isF2Pressed()
	{
		return ( _buttons & CT_F2 ) > 0;
	}

	public boolean isF3Pressed()
	{
		return ( _buttons & CT_F3 ) > 0;
	}

	public boolean isPlusPressed()
	{
		return ( _buttons & CT_PLUS ) > 0;
	}

	public boolean isMinusPressed()
	{
		return ( _buttons & CT_MINUS ) > 0;
	}

}
