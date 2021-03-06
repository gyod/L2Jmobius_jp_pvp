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
package com.l2jmobius.gameserver.network.serverpackets;

import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.items.L2Henna;

/**
 * @author Zoey76
 */
public final class HennaItemRemoveInfo extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final L2Henna _henna;
	
	public HennaItemRemoveInfo(L2Henna henna, L2PcInstance player)
	{
		_henna = henna;
		_activeChar = player;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xE7);
		writeD(_henna.getDyeId()); // symbol Id
		writeD(_henna.getDyeItemId()); // item id of dye
		writeQ(_henna.getCancelCount()); // total amount of dye require
		writeQ(_henna.getCancelFee()); // total amount of Adena require to remove symbol
		writeD(_henna.isAllowedClass(_activeChar.getClassId()) ? 0x01 : 0x00); // able to remove or not
		writeQ(_activeChar.getAdena());
		writeD(_activeChar.getINT()); // current INT
		writeD(_activeChar.getINT() - _henna.getStatINT()); // equip INT
		writeD(_activeChar.getSTR()); // current STR
		writeD(_activeChar.getSTR() - _henna.getStatSTR()); // equip STR
		writeD(_activeChar.getCON()); // current CON
		writeD(_activeChar.getCON() - _henna.getStatCON()); // equip CON
		writeD(_activeChar.getMEN()); // current MEN
		writeD(_activeChar.getMEN() - _henna.getStatMEN()); // equip MEN
		writeD(_activeChar.getDEX()); // current DEX
		writeD(_activeChar.getDEX() - _henna.getStatDEX()); // equip DEX
		writeD(_activeChar.getWIT()); // current WIT
		writeD(_activeChar.getWIT() - _henna.getStatWIT()); // equip WIT
		writeD(_activeChar.getLUC()); // current LUC
		writeD(_activeChar.getLUC() - _henna.getStatLUC()); // equip LUC
		writeD(_activeChar.getCHA()); // current CHA
		writeD(_activeChar.getCHA() - _henna.getStatCHA()); // equip CHA
		writeD(0x00);
	}
}
