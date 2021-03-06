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
package com.l2jmobius.gameserver.model.actor.tasks.player;

import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.util.Rnd;

/**
 * Task dedicated for looking for fishes.
 * @author UnAfraid
 */
public class LookingForFishTask implements Runnable
{
	private final L2PcInstance _player;
	private final boolean _isNoob, _isUpperGrade;
	private final int _fishGroup;
	private final double _fishGutsCheck;
	private final long _endTaskTime;
	
	public LookingForFishTask(L2PcInstance player, int startCombatTime, double fishGutsCheck, int fishGroup, boolean isNoob, boolean isUpperGrade)
	{
		_player = player;
		_fishGutsCheck = fishGutsCheck;
		_endTaskTime = System.currentTimeMillis() + (startCombatTime * 1000) + 10000;
		_fishGroup = fishGroup;
		_isNoob = isNoob;
		_isUpperGrade = isUpperGrade;
	}
	
	@Override
	public void run()
	{
		if (_player == null)
		{
			return;
		}
		
		if (System.currentTimeMillis() >= _endTaskTime)
		{
			_player.endFishing(false, true);
			return;
		}
		
		if ((_fishGroup == -1) || (_fishGutsCheck <= Rnd.get(100)))
		{
			return;
		}
		
		_player.stopLookingForFishTask();
		_player.startFishCombat(_isNoob, _isUpperGrade);
	}
}
