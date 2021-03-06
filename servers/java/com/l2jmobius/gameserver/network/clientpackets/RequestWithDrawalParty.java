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

import com.l2jmobius.gameserver.model.L2Party;
import com.l2jmobius.gameserver.model.L2Party.messageType;
import com.l2jmobius.gameserver.model.PartyMatchRoom;
import com.l2jmobius.gameserver.model.PartyMatchRoomList;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.ExClosePartyRoom;
import com.l2jmobius.gameserver.network.serverpackets.ExPartyRoomMember;
import com.l2jmobius.gameserver.network.serverpackets.PartyMatchDetail;

/**
 * This class ...
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestWithDrawalParty extends L2GameClientPacket
{
	private static final String _C__44_REQUESTWITHDRAWALPARTY = "[C] 44 RequestWithDrawalParty";
	
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		final L2Party party = player.getParty();
		if (party == null)
		{
			return;
		}
		
		party.removePartyMember(player, messageType.Left);
		if (!player.isInPartyMatchRoom())
		{
			return;
		}
		
		final PartyMatchRoom _room = PartyMatchRoomList.getInstance().getPlayerRoom(player);
		if (_room != null)
		{
			player.sendPacket(new PartyMatchDetail(player, _room));
			player.sendPacket(new ExPartyRoomMember(player, _room, 0));
			player.sendPacket(new ExClosePartyRoom());
			_room.deleteMember(player);
		}
		player.setPartyRoom(0);
		player.broadcastUserInfo();
	}
	
	@Override
	public String getType()
	{
		return _C__44_REQUESTWITHDRAWALPARTY;
	}
}
