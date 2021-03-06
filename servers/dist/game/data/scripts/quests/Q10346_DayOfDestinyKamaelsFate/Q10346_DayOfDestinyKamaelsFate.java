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
package quests.Q10346_DayOfDestinyKamaelsFate;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.cache.HtmCache;
import com.l2jmobius.gameserver.enums.QuestSound;
import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.events.EventType;
import com.l2jmobius.gameserver.model.events.ListenerRegisterType;
import com.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import com.l2jmobius.gameserver.model.events.annotations.RegisterType;
import com.l2jmobius.gameserver.model.events.impl.character.player.OnPlayerLevelChanged;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.network.serverpackets.ExQuestNpcLogList;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Day of Destiny: Kamael's Fate (10346)
 * @author Mobius
 */
public class Q10346_DayOfDestinyKamaelsFate extends Quest
{
	// NPCs
	private static final int OLTRAN = 32221;
	private static final int QUARTERMASTER = 33407;
	private static final int DEAD_SOLDIER_1 = 33166;
	private static final int DEAD_SOLDIER_2 = 33167;
	private static final int DEAD_SOLDIER_3 = 33168;
	private static final int DEAD_SOLDIER_4 = 33169;
	private static final int VANGUARD_MEMBER = 33165;
	// Items
	private static final int DEAD_SOLDIER_TAGS = 17753;
	// Rewards
	private static final int STEEL_DOOR_GUILD_COIN = 37045;
	private static final int M_SOULSHOT_S = 22576;
	private static final int M_SPIRITSHOT_S = 22607;
	// Requirement
	private static final int MIN_LEVEL = 76;
	
	public Q10346_DayOfDestinyKamaelsFate()
	{
		super(10346, Q10346_DayOfDestinyKamaelsFate.class.getSimpleName(), "Day of Destiny: Kamael's Fate");
		addStartNpc(OLTRAN);
		addTalkId(OLTRAN, QUARTERMASTER, DEAD_SOLDIER_1, DEAD_SOLDIER_2, DEAD_SOLDIER_3, DEAD_SOLDIER_4, VANGUARD_MEMBER);
		registerQuestItems(DEAD_SOLDIER_TAGS);
		addCondMinLevel(MIN_LEVEL, "no_level.html");
		addCondRace(Race.KAMAEL, "no_race.html");
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "32221-02.htm":
			case "32221-03.htm":
			case "32221-04.htm":
			case "32221-06.html":
			case "32221-07.html":
			{
				htmltext = event;
				break;
			}
			case "32221-05.html":
			{
				qs.startQuest();
				qs.setCond(2); // arrow hack
				qs.setCond(1);
				htmltext = event;
				break;
			}
			case "33407-02.html":
			{
				if (qs.isCond(1))
				{
					qs.setCond(2, true);
					htmltext = event;
				}
				break;
			}
			case "dead_collect.html":
			{
				if (qs.isCond(2))
				{
					if (qs.get("" + npc.getId()) == null)
					{
						qs.set("" + npc.getId(), 1);
						giveItems(player, DEAD_SOLDIER_TAGS, 1);
						playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
						
					}
					final int count = qs.getMemoState() + 1;
					if (count >= 4)
					{
						qs.setCond(3, true);
						qs.unset("" + DEAD_SOLDIER_1);
						qs.unset("" + DEAD_SOLDIER_2);
						qs.unset("" + DEAD_SOLDIER_3);
						qs.unset("" + DEAD_SOLDIER_4);
						htmltext = "dead_complete.html";
					}
					else
					{
						qs.setMemoState(count);
						final ExQuestNpcLogList log = new ExQuestNpcLogList(getId());
						log.addItem(DEAD_SOLDIER_TAGS, count);
						player.sendPacket(log);
						htmltext = event;
					}
				}
				break;
			}
			case "33407-05.html":
			{
				if (qs.isCond(3))
				{
					takeItems(player, DEAD_SOLDIER_TAGS, -1);
					qs.setCond(4, true);
					htmltext = event;
				}
				break;
			}
			case "transfer_info":
			{
				if (qs.isCond(13))
				{
					switch (player.getClassId().getId())
					{
						// TODO: Return detail htmls.
					}
				}
				break;
			}
			case "32221-08.html":
			{
				if (qs.isCond(13))
				{
					switch (player.getClassId().getId())
					{
						// Berserker -> Doombringer
						case 127:
						{
							player.setBaseClassId(131);
							player.setClassId(131);
							break;
						}
						// Soul Breaker -> Soul Hound (Male)
						case 128:
						{
							player.setBaseClassId(132);
							player.setClassId(132);
							break;
						}
						// Soul Breaker -> Soul Hound (Female)
						case 129:
						{
							player.setBaseClassId(133);
							player.setClassId(133);
							break;
						}
						// Arbalester -> Trickster
						case 130:
						{
							player.setBaseClassId(134);
							player.setClassId(134);
							break;
						}
					}
					rewardItems(player, STEEL_DOOR_GUILD_COIN, 87);
					rewardItems(player, M_SOULSHOT_S, 1);
					rewardItems(player, M_SPIRITSHOT_S, 1);
					addExpAndSp(player, 2050000, 0);
					player.broadcastUserInfo();
					qs.exitQuest(false, true);
				}
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
		
		switch (qs.getState())
		{
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case OLTRAN:
					{
						if (qs.isCond(1))
						{
							htmltext = "32221-05.html";
						}
						else if (qs.isCond(13))
						{
							htmltext = "32221-06.html";
						}
						break;
					}
					case QUARTERMASTER:
					{
						switch (qs.getCond())
						{
							case 1:
							{
								htmltext = "33407-01.html";
								break;
							}
							case 2:
							{
								htmltext = "33407-03.html";
								break;
							}
							case 3:
							{
								htmltext = "33407-04.html";
								break;
							}
							case 4:
							{
								htmltext = "33407-05.html";
								break;
							}
						}
						break;
					}
					case DEAD_SOLDIER_1:
					case DEAD_SOLDIER_2:
					case DEAD_SOLDIER_3:
					case DEAD_SOLDIER_4:
					{
						if (qs.isCond(2))
						{
							if (qs.get("" + npc.getId()) != null)
							{
								htmltext = "dead_collect.html";
							}
							else
							{
								htmltext = "dead_soldier.html";
							}
						}
						else if (qs.isCond(3))
						{
							htmltext = "dead_complete.html";
						}
						break;
					}
					case VANGUARD_MEMBER:
					{
						if ((qs.getCond() > 3) && (qs.getCond() < 13))
						{
							htmltext = "33165-01.html";
						}
						break;
					}
				}
				break;
			}
			case State.CREATED:
			{
				if ((npc.getId() == OLTRAN) && !player.isSubClassActive() && !player.isDualClassActive() && (player.getClassId().level() == 2))
				{
					htmltext = "32221-01.htm";
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg(player);
				break;
			}
		}
		return htmltext;
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LEVEL_CHANGED)
	@RegisterType(ListenerRegisterType.GLOBAL)
	public void OnPlayerLevelChanged(OnPlayerLevelChanged event)
	{
		if (Config.DISABLE_TUTORIAL)
		{
			return;
		}
		final L2PcInstance player = event.getActiveChar();
		if ((player.getLevel() >= MIN_LEVEL) && (player.getRace() == Race.KAMAEL))
		{
			final QuestState qs = getQuestState(player, false);
			if (qs == null)
			{
				final NpcHtmlMessage html = new NpcHtmlMessage();
				html.setHtml(HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "scripts/quests/Q10346_DayOfDestinyKamaelsFate/announce.html"));
				player.sendPacket(html);
			}
		}
	}
}