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
package quests.Q00473_InTheCoralGarden;

import com.l2jmobius.gameserver.enums.QuestSound;
import com.l2jmobius.gameserver.enums.QuestType;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.util.Util;

/**
 * @author hlwrave
 * @URL: https://l2wiki.com/In_the_Coral_Garden
 */
public class Q00473_InTheCoralGarden extends Quest
{
	// Npc
	public static final int FIOREN = 33044;
	// Monster
	public static final int MICHAEL = 25799;
	// Misc
	public static final int MIN_LEVEL = 97;
	// Item
	public static final int CERTIFICATE = 30387;
	
	public Q00473_InTheCoralGarden()
	{
		super(473, Q00473_InTheCoralGarden.class.getSimpleName(), "In The Coral Garden");
		addStartNpc(FIOREN);
		addTalkId(FIOREN);
		addKillId(MICHAEL);
		addCondMinLevel(MIN_LEVEL, "33044-02.html");
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final String htmltext = event;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return getNoQuestMsg(player);
		}
		
		switch (event)
		{
			case "33044-04.html":
			{
				qs.startQuest();
				break;
			}
			case "33044-07.html":
			{
				giveItems(player, CERTIFICATE, 8);
				qs.exitQuest(QuestType.DAILY, true);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		
		if (qs.isCreated())
		{
			htmltext = "33044-01.htm";
		}
		else if (qs.isStarted())
		{
			if (qs.isCond(1))
			{
				htmltext = "33044-05.html";
			}
			else if (qs.isCond(2))
			{
				htmltext = "33044-06.html";
			}
		}
		else if (qs.isCompleted())
		{
			htmltext = "33044-08.html";
		}
		
		return htmltext;
	}
	
	@Override
	public void actionForEachPlayer(L2PcInstance player, L2Npc npc, boolean isSummon)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(1) && Util.checkIfInRange(1500, npc, player, false))
		{
			qs.setCond(2, true);
			playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		executeForEachPlayer(killer, npc, isSummon, true, false);
		return super.onKill(npc, killer, isSummon);
	}
}