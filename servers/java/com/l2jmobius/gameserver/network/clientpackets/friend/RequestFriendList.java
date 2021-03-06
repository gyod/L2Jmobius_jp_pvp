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
package com.l2jmobius.gameserver.network.clientpackets.friend;

import com.l2jmobius.gameserver.data.sql.impl.CharNameTable;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * @version $Revision: 1.3.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestFriendList extends L2GameClientPacket
{
	private static final String _C__79_REQUESTFRIENDLIST = "[C] 79 RequestFriendList";
	
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		SystemMessage sm;
		
		// ======<Friend List>======
		activeChar.sendPacket(SystemMessageId.FRIENDS_LIST);
		
		L2PcInstance friend = null;
		for (int id : activeChar.getFriendList().keySet())
		{
			// int friendId = rset.getInt("friendId");
			final String friendName = CharNameTable.getInstance().getNameById(id);
			
			if (friendName == null)
			{
				continue;
			}
			
			friend = L2World.getInstance().getPlayer(friendName);
			
			sm = (friend == null) || !friend.isOnline() ? SystemMessage.getSystemMessage(SystemMessageId.S1_CURRENTLY_OFFLINE) : SystemMessage.getSystemMessage(SystemMessageId.S1_CURRENTLY_ONLINE);
			sm.addString(friendName);
			activeChar.sendPacket(sm);
		}
		
		// =========================
		activeChar.sendPacket(SystemMessageId.EMPTY3);
	}
	
	@Override
	public String getType()
	{
		return _C__79_REQUESTFRIENDLIST;
	}
}
