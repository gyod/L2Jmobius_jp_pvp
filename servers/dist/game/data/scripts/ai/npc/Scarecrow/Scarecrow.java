/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.npc.Scarecrow;

import ai.npc.AbstractNpcAI;

import com.l2jserver.gameserver.model.actor.L2Npc;

/**
 * Scarecrow AI.
 * @author ivantotov
 */
public final class Scarecrow extends AbstractNpcAI
{
	// NPCs
	private static final int TRAINING_DUMMY = 19546;
	private static final int SCARECROW = 27457;
	private static final int PHYSICAL_PUNCH_MACHINE = 33752;
	private static final int MAGICAL_PUNCH_MACHINE = 33753;
	
	private Scarecrow()
	{
		super(Scarecrow.class.getSimpleName(), "ai");
		addSpawnId(TRAINING_DUMMY, SCARECROW, PHYSICAL_PUNCH_MACHINE, MAGICAL_PUNCH_MACHINE);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.disableCoreAI(true);
		npc.setIsImmobilized(true);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new Scarecrow();
	}
}
