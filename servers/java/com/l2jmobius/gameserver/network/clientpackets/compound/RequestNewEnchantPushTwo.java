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
package com.l2jmobius.gameserver.network.clientpackets.compound;

import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.request.CompoundRequest;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket;
import com.l2jmobius.gameserver.network.serverpackets.compound.ExEnchantOneFail;
import com.l2jmobius.gameserver.network.serverpackets.compound.ExEnchantTwoFail;
import com.l2jmobius.gameserver.network.serverpackets.compound.ExEnchantTwoOK;

/**
 * @author UnAfraid
 */
public class RequestNewEnchantPushTwo extends L2GameClientPacket
{
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		if (activeChar.isInStoreMode())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_DO_THAT_WHILE_IN_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			activeChar.sendPacket(ExEnchantOneFail.STATIC_PACKET);
			return;
		}
		if (activeChar.isProcessingTransaction() || activeChar.isProcessingRequest())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_USE_THIS_SYSTEM_DURING_TRADING_PRIVATE_STORE_AND_WORKSHOP_SETUP);
			activeChar.sendPacket(ExEnchantOneFail.STATIC_PACKET);
			return;
		}
		
		final CompoundRequest request = activeChar.getRequest(CompoundRequest.class);
		if ((request == null) || request.isProcessing())
		{
			activeChar.sendPacket(ExEnchantTwoFail.STATIC_PACKET);
			return;
		}
		
		request.setItemTwo(_objectId);
		final L2ItemInstance itemOne = request.getItemOne();
		final L2ItemInstance itemTwo = request.getItemTwo();
		if ((itemOne == null) || (itemTwo == null) || (itemOne.getObjectId() == itemTwo.getObjectId()) || (itemOne.getItem().getId() != itemTwo.getItem().getId()) || (itemOne.getItem().getCompoundItem() == 0) || (itemOne.getItem().getCompoundChance() == 0))
		{
			activeChar.sendPacket(ExEnchantTwoFail.STATIC_PACKET);
			return;
		}
		
		activeChar.sendPacket(ExEnchantTwoOK.STATIC_PACKET);
	}
}
