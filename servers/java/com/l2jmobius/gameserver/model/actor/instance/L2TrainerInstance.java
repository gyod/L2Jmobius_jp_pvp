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
package com.l2jmobius.gameserver.model.actor.instance;

import com.l2jmobius.gameserver.enums.InstanceType;
import com.l2jmobius.gameserver.model.actor.templates.L2NpcTemplate;

public class L2TrainerInstance extends L2NpcInstance
{
	/**
	 * Creates a trainer.
	 * @param template the trainer NPC template
	 */
	public L2TrainerInstance(L2NpcTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.L2TrainerInstance);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		return "html/trainer/" + (val == 0 ? "" + npcId : npcId + "-" + val) + ".htm";
	}
}