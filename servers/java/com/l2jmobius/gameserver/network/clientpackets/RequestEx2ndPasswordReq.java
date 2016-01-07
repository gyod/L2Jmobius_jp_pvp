/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jmobius.gameserver.network.clientpackets;

import com.l2jmobius.gameserver.data.xml.impl.SecondaryAuthData;
import com.l2jmobius.gameserver.network.serverpackets.Ex2ndPasswordAck;
import com.l2jmobius.gameserver.security.SecondaryPasswordAuth;

/**
 * (ch)cS{S} c: change pass? S: current password S: new password
 * @author mrTJO
 */
public class RequestEx2ndPasswordReq extends L2GameClientPacket
{
	private static final String _C__D0_AF_REQUESTEX2NDPASSWORDREQ = "[C] D0:AF RequestEx2ndPasswordReq";
	
	private int _changePass;
	private String _password, _newPassword;
	
	@Override
	protected void readImpl()
	{
		_changePass = readC();
		_password = readS();
		if (_changePass == 2)
		{
			_newPassword = readS();
		}
	}
	
	@Override
	protected void runImpl()
	{
		if (!SecondaryAuthData.getInstance().isEnabled())
		{
			return;
		}
		
		final SecondaryPasswordAuth secondAuth = getClient().getSecondaryAuth();
		boolean success = false;
		
		if ((_changePass == 0) && !secondAuth.passwordExist())
		{
			success = secondAuth.savePassword(_password);
		}
		else if ((_changePass == 2) && secondAuth.passwordExist())
		{
			success = secondAuth.changePassword(_password, _newPassword);
		}
		
		if (success)
		{
			sendPacket(new Ex2ndPasswordAck(_changePass, Ex2ndPasswordAck.SUCCESS));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_AF_REQUESTEX2NDPASSWORDREQ;
	}
}