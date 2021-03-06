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
package ai.zones.TalkingIsland.AwakeningMaster;

import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.data.xml.impl.SkillTreesData;
import com.l2jmobius.gameserver.enums.CategoryType;
import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.enums.UserInfoType;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.base.ClassId;
import com.l2jmobius.gameserver.model.entity.Hero;
import com.l2jmobius.gameserver.model.events.EventType;
import com.l2jmobius.gameserver.model.events.ListenerRegisterType;
import com.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import com.l2jmobius.gameserver.model.events.annotations.RegisterType;
import com.l2jmobius.gameserver.model.events.impl.character.player.OnPlayerChangeToAwakenedClass;
import com.l2jmobius.gameserver.model.holders.SkillHolder;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.AcquireSkillList;
import com.l2jmobius.gameserver.network.serverpackets.ExChangeToAwakenedClass;
import com.l2jmobius.gameserver.network.serverpackets.ExShowUsm;
import com.l2jmobius.gameserver.network.serverpackets.SocialAction;
import com.l2jmobius.gameserver.network.serverpackets.UserInfo;

import ai.AbstractNpcAI;
import quests.Q10338_SeizeYourDestiny.Q10338_SeizeYourDestiny;
import quests.Q10472_WindsOfFate_EncroachingShadows.Q10472_WindsOfFate_EncroachingShadows;

/**
 * AwakeningMaster AI.
 * @author Sdw
 */
public final class AwakeningMaster extends AbstractNpcAI
{
	// NPCs
	private static final int SIGEL_MASTER = 33397;
	private static final int TYRR_MASTER = 33398;
	private static final int OTHELL_MASTER = 33399;
	private static final int YUL_MASTER = 33400;
	private static final int FEOH_MASTER = 33401;
	private static final int ISS_MASTER = 33402;
	private static final int WYNN_MASTER = 33403;
	private static final int AEORE_MASTER = 33404;
	// Skills
	private static final SkillHolder NPC_WYNN = new SkillHolder(16390, 1);
	private static final SkillHolder NPC_FEOH = new SkillHolder(16391, 1);
	private static final SkillHolder NPC_TYRR = new SkillHolder(16392, 1);
	private static final SkillHolder NPC_OTHELL = new SkillHolder(16393, 1);
	private static final SkillHolder NPC_YUL = new SkillHolder(16394, 1);
	private static final SkillHolder NPC_ISS = new SkillHolder(16395, 1);
	private static final SkillHolder NPC_SIGEL = new SkillHolder(16396, 1);
	private static final SkillHolder NPC_AEORE = new SkillHolder(16397, 1);
	// Items
	private static final int SCROLL_OF_AFTERLIFE = 17600;
	private static final int ABELIUS_POWER = 32264;
	private static final int SAPYROS_POWER = 32265;
	private static final int ASHAGEN_POWER = 32266;
	private static final int CRANIGG_POWER = 32267;
	private static final int SOLTKREIG_POWER = 32268;
	private static final int NAVIAROPE_POWER = 32269;
	private static final int LEISTER_POWER = 32270;
	private static final int LAKCIS_POWER = 32271;
	private static final int COUNTERFEIT_ATELIA = 40059;
	// Other
	private static final int AWAKENING_END_USM_ID = 10;
	
	private AwakeningMaster()
	{
		super(AwakeningMaster.class.getSimpleName(), "ai/zones/TalkingIsland");
		addStartNpc(SIGEL_MASTER, TYRR_MASTER, OTHELL_MASTER, YUL_MASTER, FEOH_MASTER, ISS_MASTER, WYNN_MASTER, AEORE_MASTER);
		addTalkId(SIGEL_MASTER, TYRR_MASTER, OTHELL_MASTER, YUL_MASTER, FEOH_MASTER, ISS_MASTER, WYNN_MASTER, AEORE_MASTER);
		addFirstTalkId(SIGEL_MASTER, TYRR_MASTER, OTHELL_MASTER, YUL_MASTER, FEOH_MASTER, ISS_MASTER, WYNN_MASTER, AEORE_MASTER);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		if (qs == null)
		{
			return null;
		}
		final String htmltext = null;
		switch (event)
		{
			case "awakening":
			{
				final QuestState qs2 = player.getQuestState(Q10338_SeizeYourDestiny.class.getSimpleName());
				if (hasQuestItems(player, SCROLL_OF_AFTERLIFE) && (player.getLevel() > 84) && (!player.isSubClassActive() || player.isDualClassActive()) && player.isInCategory(CategoryType.FOURTH_CLASS_GROUP) && (qs2 != null) && qs2.isCompleted())
				{
					switch (npc.getId())
					{
						case SIGEL_MASTER:
						{
							if (!player.isInCategory(CategoryType.SIGEL_CANDIDATE))
							{
								return SIGEL_MASTER + "-no_class.htm";
							}
							break;
						}
						case TYRR_MASTER:
						{
							if (!player.isInCategory(CategoryType.TYRR_CANDIDATE))
							{
								return TYRR_MASTER + "-no_class.htm";
							}
							break;
						}
						case OTHELL_MASTER:
						{
							if (!player.isInCategory(CategoryType.OTHELL_CANDIDATE))
							{
								return OTHELL_MASTER + "-no_class.htm";
							}
							break;
						}
						case YUL_MASTER:
						{
							if (!player.isInCategory(CategoryType.YUL_CANDIDATE))
							{
								return YUL_MASTER + "-no_class.htm";
							}
							break;
						}
						case FEOH_MASTER:
						{
							if (!player.isInCategory(CategoryType.FEOH_CANDIDATE))
							{
								return FEOH_MASTER + "-no_class.htm";
							}
							break;
						}
						case ISS_MASTER:
						{
							if (!player.isInCategory(CategoryType.ISS_CANDIDATE))
							{
								return ISS_MASTER + "-no_class.htm";
							}
							break;
						}
						case WYNN_MASTER:
						{
							if (!player.isInCategory(CategoryType.WYNN_CANDIDATE))
							{
								return WYNN_MASTER + "-no_class.htm";
							}
							break;
						}
						case AEORE_MASTER:
						{
							if (!player.isInCategory(CategoryType.AEORE_CANDIDATE))
							{
								return AEORE_MASTER + "-no_class.htm";
							}
							break;
						}
					}
					
					for (ClassId newClass : player.getClassId().getNextClassIds())
					{
						player.sendPacket(new ExChangeToAwakenedClass(newClass.getId()));
					}
				}
				else
				{
					return npc.getId() + "-no.htm";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (player.getRace() != Race.ERTHEIA)
		{
			return npc.getId() + ".html";
		}
		
		final QuestState qs3 = player.getQuestState(Q10472_WindsOfFate_EncroachingShadows.class.getSimpleName());
		String htmltext = getNoQuestMsg(player);
		
		switch (npc.getId())
		{
			case WYNN_MASTER:
			{
				if (qs3.isCond(8) && (getQuestItemsCount(player, COUNTERFEIT_ATELIA) >= 1))
				{
					htmltext = "33403-01.html";
					npc.doCast(NPC_WYNN.getSkill());
					qs3.setCond(9, true);
				}
				break;
			}
			case FEOH_MASTER:
			{
				if (qs3.isCond(9) && (getQuestItemsCount(player, COUNTERFEIT_ATELIA) >= 1))
				{
					htmltext = "33401-01.html";
					npc.doCast(NPC_FEOH.getSkill());
					qs3.setCond(10, true);
				}
				break;
			}
			case TYRR_MASTER:
			{
				if (qs3.isCond(10) && (getQuestItemsCount(player, COUNTERFEIT_ATELIA) >= 1))
				{
					htmltext = "33398-01.html";
					npc.doCast(NPC_TYRR.getSkill());
					qs3.setCond(11, true);
				}
				break;
			}
			case OTHELL_MASTER:
			{
				if (qs3.isCond(11) && (getQuestItemsCount(player, COUNTERFEIT_ATELIA) >= 1))
				{
					htmltext = "33399-01.html";
					npc.doCast(NPC_OTHELL.getSkill());
					qs3.setCond(12, true);
				}
				break;
			}
			case ISS_MASTER:
			{
				if (qs3.isCond(12) && (getQuestItemsCount(player, COUNTERFEIT_ATELIA) >= 1))
				{
					htmltext = "33402-01.html";
					npc.doCast(NPC_ISS.getSkill());
					qs3.setCond(13, true);
				}
				break;
			}
			case YUL_MASTER:
			{
				if (qs3.isCond(13) && (getQuestItemsCount(player, COUNTERFEIT_ATELIA) >= 1))
				{
					htmltext = "33400-01.html";
					npc.doCast(NPC_YUL.getSkill());
					qs3.setCond(14, true);
				}
				break;
			}
			case SIGEL_MASTER:
			{
				if (qs3.isCond(14) && (getQuestItemsCount(player, COUNTERFEIT_ATELIA) >= 1))
				{
					htmltext = "33397-01.html";
					npc.doCast(NPC_SIGEL.getSkill());
					qs3.setCond(15, true);
				}
				break;
			}
			case AEORE_MASTER:
			{
				if (qs3.isCond(15) && (getQuestItemsCount(player, COUNTERFEIT_ATELIA) >= 1))
				{
					htmltext = "33404-01.html";
					npc.doCast(NPC_AEORE.getSkill());
					qs3.setCond(16, true);
				}
				break;
			}
		}
		return htmltext;
	}
	
	@RegisterEvent(EventType.ON_PLAYER_CHANGE_TO_AWAKENED_CLASS)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerChangeToAwakenedClass(OnPlayerChangeToAwakenedClass event)
	{
		final L2PcInstance player = event.getActiveChar();
		
		if (player.isSubClassActive() && !player.isDualClassActive())
		{
			return;
		}
		
		if ((player.getLevel() < 85) || !player.isInCategory(CategoryType.FOURTH_CLASS_GROUP))
		{
			return;
		}
		
		final QuestState st = player.getQuestState(Q10338_SeizeYourDestiny.class.getSimpleName());
		
		if ((st == null) || !st.isCompleted())
		{
			return;
		}
		
		if (player.isHero() || Hero.getInstance().isUnclaimedHero(player.getObjectId()))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AWAKEN_WHEN_YOU_ARE_A_HERO_OR_ON_THE_WAIT_LIST_FOR_HERO_STATUS);
			return;
		}
		
		if (player.getInventory().getSize(false) >= (player.getInventoryLimit() * 0.8))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AWAKEN_DUE_TO_YOUR_CURRENT_INVENTORY_WEIGHT_PLEASE_ORGANIZE_YOUR_INVENTORY_AND_TRY_AGAIN_DWARVEN_CHARACTERS_MUST_BE_AT_20_OR_BELOW_THE_INVENTORY_MAX_TO_AWAKEN);
			return;
		}
		
		if (player.isMounted() || player.isTransformed())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AWAKEN_WHILE_YOU_RE_TRANSFORMED_OR_RIDING);
			return;
		}
		
		final L2ItemInstance item = player.getInventory().getItemByItemId(SCROLL_OF_AFTERLIFE);
		if (item == null)
		{
			return;
		}
		
		if (!player.destroyItem("Awakening", item, player, true))
		{
			return;
		}
		
		for (ClassId newClass : player.getClassId().getNextClassIds())
		{
			player.setClassId(newClass.getId());
			if (player.isDualClassActive())
			{
				player.getSubClasses().get(player.getClassIndex()).setClassId(player.getActiveClassId());
			}
			else
			{
				player.setBaseClassId(player.getActiveClassId());
			}
			player.sendPacket(SystemMessageId.CONGRATULATIONS_YOU_VE_COMPLETED_A_CLASS_TRANSFER);
			final UserInfo ui = new UserInfo(player, false);
			ui.addComponentType(UserInfoType.BASIC_INFO);
			ui.addComponentType(UserInfoType.MAX_HPCPMP);
			player.sendPacket(ui);
			player.broadcastInfo();
			
			int itemId = ABELIUS_POWER; // Sigel
			if (player.isInCategory(CategoryType.TYRR_GROUP))
			{
				itemId = SAPYROS_POWER;
			}
			else if (player.isInCategory(CategoryType.OTHELL_GROUP))
			{
				itemId = ASHAGEN_POWER;
			}
			else if (player.isInCategory(CategoryType.YUL_GROUP))
			{
				itemId = CRANIGG_POWER;
			}
			else if (player.isInCategory(CategoryType.FEOH_GROUP))
			{
				itemId = SOLTKREIG_POWER;
			}
			else if (player.isInCategory(CategoryType.ISS_GROUP))
			{
				itemId = NAVIAROPE_POWER;
			}
			else if (player.isInCategory(CategoryType.WYNN_GROUP))
			{
				itemId = LEISTER_POWER;
			}
			else if (player.isInCategory(CategoryType.AEORE_GROUP))
			{
				itemId = LAKCIS_POWER;
			}
			player.broadcastPacket(new SocialAction(player.getObjectId(), 20));
			giveItems(player, itemId, 1);
			
			SkillTreesData.getInstance().cleanSkillUponAwakening(player);
			player.sendPacket(new AcquireSkillList(player));
			player.sendSkillList();
		}
		
		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			player.sendPacket(new ExShowUsm(AWAKENING_END_USM_ID));
		}, 10000);
	}
	
	public static void main(String[] args)
	{
		new AwakeningMaster();
	}
}
