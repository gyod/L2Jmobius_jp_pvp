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
package com.l2jmobius.gameserver.instancemanager;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.zone.ZoneId;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.ExPCCafePointInfo;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.util.Rnd;

public final class PcCafePointsManager
{
	public PcCafePointsManager()
	{
	}
	
	public void givePcCafePoint(final L2PcInstance player, final long exp)
	{
		if (!Config.PC_BANG_ENABLED)
		{
			return;
		}
		
		if (player.isInsideZone(ZoneId.PEACE) || player.isInsideZone(ZoneId.PVP) || player.isInsideZone(ZoneId.SIEGE) || (player.isOnlineInt() == 0) || player.isJailed())
		{
			return;
		}
		
		if (player.getPcBangPoints() >= Config.PC_BANG_MAX_POINTS)
		{
			final SystemMessage message = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EARNED_THE_MAXIMUM_NUMBER_OF_PC_POINTS);
			player.sendPacket(message);
			return;
		}
		
		int points = (int) (exp * 0.0001 * Config.PC_BANG_POINT_RATE);
		
		if (Config.PC_BANG_RANDOM_POINT)
		{
			points = Rnd.get(points / 2, points);
		}
		
		if ((points == 0) && (exp > 0) && Config.PC_BANG_REWARD_LOW_EXP_KILLS && (Rnd.get(100) < Config.PC_BANG_LOW_EXP_KILLS_CHANCE))
		{
			points = 1; // minimum points
		}
		
		SystemMessage message = null;
		if (points > 0)
		{
			if (Config.PC_BANG_ENABLE_DOUBLE_POINTS && (Rnd.get(100) < Config.PC_BANG_DOUBLE_POINTS_CHANCE))
			{
				points *= 2;
				message = SystemMessage.getSystemMessage(SystemMessageId.DOUBLE_POINTS_YOU_EARNED_S1_PC_POINT_S);
			}
			else
			{
				message = SystemMessage.getSystemMessage(SystemMessageId.YOU_EARNED_S1_PC_POINT_S);
			}
			if ((player.getPcBangPoints() + points) > Config.PC_BANG_MAX_POINTS)
			{
				points = Config.PC_BANG_MAX_POINTS - player.getPcBangPoints();
			}
			message.addLong(points);
			player.sendPacket(message);
			player.setPcBangPoints(player.getPcBangPoints() + points);
			player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), points, 1));
		}
	}
	
	/**
	 * Gets the single instance of {@code PcCafePointsManager}.
	 * @return single instance of {@code PcCafePointsManager}
	 */
	public static final PcCafePointsManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final PcCafePointsManager _instance = new PcCafePointsManager();
	}
}