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
package com.l2jmobius.gameserver.model.stats.functions.formulas;

import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.model.stats.Stats;
import com.l2jmobius.gameserver.model.stats.functions.AbstractFunction;

/**
 * @author UnAfraid
 */
public class FuncMatkEvasion extends AbstractFunction
{
	private static final FuncMatkEvasion _fae_instance = new FuncMatkEvasion();
	
	public static AbstractFunction getInstance()
	{
		return _fae_instance;
	}
	
	private FuncMatkEvasion()
	{
		super(Stats.MAGIC_EVASION_RATE, 1, null, 0, null);
	}
	
	@Override
	public double calc(L2Character effector, L2Character effected, Skill skill, double initVal)
	{
		final int level = effector.getLevel();
		double value = initVal;
		// [Square(WIT)] * 3 + lvl;
		value += (Math.sqrt(effector.getWIT()) * 3) + (level * 2);
		if (!effector.isPlayer())
		{
			if (level > 69)
			{
				value += (level - 69) + 2;
			}
		}
		return value;
	}
}