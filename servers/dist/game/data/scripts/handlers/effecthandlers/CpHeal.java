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
package handlers.effecthandlers;

import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.conditions.Condition;
import com.l2jmobius.gameserver.model.effects.AbstractEffect;
import com.l2jmobius.gameserver.model.effects.L2EffectType;
import com.l2jmobius.gameserver.model.skills.BuffInfo;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * Cp Heal effect implementation.
 * @author UnAfraid
 */
public final class CpHeal extends AbstractEffect
{
	private final double _power;
	
	public CpHeal(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_power = params.getDouble("power", 0);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CPHEAL;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		final L2Character target = info.getEffected();
		if ((target == null) || target.isDead() || target.isDoor())
		{
			return;
		}
		
		// Prevents overheal and negative amount
		final double amount = Math.max(Math.min(_power, target.getMaxRecoverableCp() - target.getCurrentCp()), 0);
		if (amount != 0)
		{
			target.setCurrentCp(amount + target.getCurrentCp());
		}
		
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CP_HAS_BEEN_RESTORED);
		sm.addInt((int) amount);
		target.sendPacket(sm);
	}
}
