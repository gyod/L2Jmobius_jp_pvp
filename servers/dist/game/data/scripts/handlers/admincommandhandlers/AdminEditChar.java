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
package handlers.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.commons.database.DatabaseFactory;
import com.l2jmobius.gameserver.data.sql.impl.CharNameTable;
import com.l2jmobius.gameserver.data.xml.impl.ClassListData;
import com.l2jmobius.gameserver.data.xml.impl.SkillTreesData;
import com.l2jmobius.gameserver.data.xml.impl.TransformData;
import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.enums.SubclassInfoType;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.PageResult;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.L2Playable;
import com.l2jmobius.gameserver.model.actor.L2Summon;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PetInstance;
import com.l2jmobius.gameserver.model.base.ClassId;
import com.l2jmobius.gameserver.network.L2GameClient;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.AcquireSkillList;
import com.l2jmobius.gameserver.network.serverpackets.ExSubjobInfo;
import com.l2jmobius.gameserver.network.serverpackets.ExUserInfoInvenWeight;
import com.l2jmobius.gameserver.network.serverpackets.ExVoteSystemInfo;
import com.l2jmobius.gameserver.network.serverpackets.GMViewItemList;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.network.serverpackets.PartySmallWindowAll;
import com.l2jmobius.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.network.serverpackets.UserInfo;
import com.l2jmobius.gameserver.util.HtmlUtil;
import com.l2jmobius.gameserver.util.Util;
import com.l2jmobius.util.StringUtil;

/**
 * EditChar admin command implementation.
 */
public class AdminEditChar implements IAdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminEditChar.class.getName());
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_edit_character",
		"admin_current_player",
		"admin_nokarma", // this is to remove karma from selected char...
		"admin_setkarma", // sets karma of target char to any amount. //setkarma <karma>
		"admin_setfame", // sets fame of target char to any amount. //setfame <fame>
		"admin_character_list", // same as character_info, kept for compatibility purposes
		"admin_character_info", // given a player name, displays an information window
		"admin_show_characters", // list of characters
		"admin_find_character", // find a player by his name or a part of it (case-insensitive)
		"admin_find_ip", // find all the player connections from a given IPv4 number
		"admin_find_account", // list all the characters from an account (useful for GMs w/o DB access)
		"admin_find_dualbox", // list all the IPs with more than 1 char logged in (dualbox)
		"admin_strict_find_dualbox",
		"admin_tracert",
		"admin_rec", // gives recommendation points
		"admin_settitle", // changes char title
		"admin_changename", // changes char name
		"admin_setsex", // changes characters' sex
		"admin_setcolor", // change charnames' color display
		"admin_settcolor", // change char title color
		"admin_setclass", // changes chars' classId
		"admin_setpk", // changes PK count
		"admin_setpvp", // changes PVP count
		"admin_set_pvp_flag",
		"admin_fullfood", // fulfills a pet's food bar
		"admin_remove_clan_penalty", // removes clan penalties
		"admin_summon_info", // displays an information window about target summon
		"admin_unsummon",
		"admin_summon_setlvl",
		"admin_show_pet_inv",
		"admin_partyinfo",
		"admin_setnoble",
		"admin_set_hp",
		"admin_set_mp",
		"admin_set_cp",
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_current_player"))
		{
			showCharacterInfo(activeChar, activeChar);
		}
		else if (command.startsWith("admin_character_info"))
		{
			final String[] data = command.split(" ");
			if (data.length > 1)
			{
				showCharacterInfo(activeChar, L2World.getInstance().getPlayer(data[1]));
			}
			else if (activeChar.getTarget() instanceof L2PcInstance)
			{
				showCharacterInfo(activeChar, activeChar.getTarget().getActingPlayer());
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
			}
		}
		else if (command.startsWith("admin_character_list"))
		{
			listCharacters(activeChar, 0);
		}
		else if (command.startsWith("admin_show_characters"))
		{
			try
			{
				listCharacters(activeChar, Integer.parseInt(command.substring(22)));
			}
			catch (StringIndexOutOfBoundsException e)
			{
				// Case of empty page number
				activeChar.sendMessage("Usage: //show_characters <page_number>");
			}
		}
		else if (command.startsWith("admin_find_character"))
		{
			try
			{
				findCharacter(activeChar, command.substring(21));
			}
			catch (StringIndexOutOfBoundsException e)
			{ // Case of empty character name
				activeChar.sendMessage("Usage: //find_character <character_name>");
				listCharacters(activeChar, 0);
			}
		}
		else if (command.startsWith("admin_find_ip"))
		{
			try
			{
				findCharactersPerIp(activeChar, command.substring(14));
			}
			catch (Exception e)
			{ // Case of empty or malformed IP number
				activeChar.sendMessage("Usage: //find_ip <www.xxx.yyy.zzz>");
				listCharacters(activeChar, 0);
			}
		}
		else if (command.startsWith("admin_find_account"))
		{
			try
			{
				findCharactersPerAccount(activeChar, command.substring(19));
			}
			catch (Exception e)
			{ // Case of empty or malformed player name
				activeChar.sendMessage("Usage: //find_account <player_name>");
				listCharacters(activeChar, 0);
			}
		}
		else if (command.startsWith("admin_edit_character"))
		{
			final String[] data = command.split(" ");
			if (data.length > 1)
			{
				editCharacter(activeChar, data[1]);
			}
			else if (activeChar.getTarget() instanceof L2PcInstance)
			{
				editCharacter(activeChar, null);
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
			}
		}
		// Karma control commands
		else if (command.equals("admin_nokarma"))
		{
			setTargetKarma(activeChar, 0);
		}
		else if (command.startsWith("admin_setkarma"))
		{
			try
			{
				setTargetKarma(activeChar, Integer.parseInt(command.substring(15)));
			}
			catch (Exception e)
			{
				if (Config.DEVELOPER)
				{
					_log.warning("Set karma error: " + e);
				}
				activeChar.sendMessage("Usage: //setkarma <new_karma_value>");
			}
		}
		else if (command.startsWith("admin_setpk"))
		{
			try
			{
				final String val = command.substring(12);
				final int pk = Integer.parseInt(val);
				final L2Object target = activeChar.getTarget();
				if (target instanceof L2PcInstance)
				{
					final L2PcInstance player = (L2PcInstance) target;
					player.setPkKills(pk);
					player.broadcastUserInfo();
					player.sendPacket(new UserInfo(player));
					player.sendMessage("A GM changed your PK count to " + pk);
					activeChar.sendMessage(player.getName() + "'s PK count changed to " + pk);
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
				}
			}
			catch (Exception e)
			{
				if (Config.DEVELOPER)
				{
					_log.warning("Set pk error: " + e);
				}
				activeChar.sendMessage("Usage: //setpk <pk_count>");
			}
		}
		else if (command.startsWith("admin_setpvp"))
		{
			try
			{
				final String val = command.substring(13);
				final int pvp = Integer.parseInt(val);
				final L2Object target = activeChar.getTarget();
				if (target instanceof L2PcInstance)
				{
					final L2PcInstance player = (L2PcInstance) target;
					player.setPvpKills(pvp);
					player.broadcastUserInfo();
					player.sendPacket(new UserInfo(player));
					player.sendMessage("A GM changed your PVP count to " + pvp);
					activeChar.sendMessage(player.getName() + "'s PVP count changed to " + pvp);
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
				}
			}
			catch (Exception e)
			{
				if (Config.DEVELOPER)
				{
					_log.warning("Set pvp error: " + e);
				}
				activeChar.sendMessage("Usage: //setpvp <pvp_count>");
			}
		}
		else if (command.startsWith("admin_setfame"))
		{
			try
			{
				final String val = command.substring(14);
				final int fame = Integer.parseInt(val);
				final L2Object target = activeChar.getTarget();
				if (target instanceof L2PcInstance)
				{
					final L2PcInstance player = (L2PcInstance) target;
					player.setFame(fame);
					player.broadcastUserInfo();
					player.sendPacket(new UserInfo(player));
					player.sendMessage("A GM changed your Reputation points to " + fame);
					activeChar.sendMessage(player.getName() + "'s Fame changed to " + fame);
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
				}
			}
			catch (Exception e)
			{
				if (Config.DEVELOPER)
				{
					_log.warning("Set Fame error: " + e);
				}
				activeChar.sendMessage("Usage: //setfame <new_fame_value>");
			}
		}
		else if (command.startsWith("admin_rec"))
		{
			try
			{
				final String val = command.substring(10);
				final int recVal = Integer.parseInt(val);
				final L2Object target = activeChar.getTarget();
				if (target instanceof L2PcInstance)
				{
					final L2PcInstance player = (L2PcInstance) target;
					player.setRecomHave(recVal);
					player.broadcastUserInfo();
					player.sendPacket(new UserInfo(player));
					player.sendPacket(new ExVoteSystemInfo(player));
					player.sendMessage("A GM changed your Recommend points to " + recVal);
					activeChar.sendMessage(player.getName() + "'s Recommend changed to " + recVal);
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //rec number");
			}
		}
		else if (command.startsWith("admin_setclass"))
		{
			try
			{
				final String val = command.substring(15).trim();
				final int classidval = Integer.parseInt(val);
				final L2Object target = activeChar.getTarget();
				if ((target == null) || !target.isPlayer())
				{
					return false;
				}
				final L2PcInstance player = target.getActingPlayer();
				if ((ClassId.getClassId(classidval) != null) && (player.getClassId().getId() != classidval))
				{
					final boolean wasAwaken = player.isAwaken();
					TransformData.getInstance().transformPlayer(255, player);
					player.setClassId(classidval);
					if (player.isSubClassActive())
					{
						player.getSubClasses().get(player.getClassIndex()).setClassId(player.getActiveClassId());
					}
					else
					{
						player.setBaseClassId(player.getActiveClassId());
						player.setInitialClassId(ClassId.getInitialClassId(player));
					}
					if (player.getBaseClass().getRace().equals(Race.ERTHEIA))
					{
						player.getAppearance().setSex(true);
					}
					final String newclass = ClassListData.getInstance().getClass(player.getClassId()).getClassName();
					player.storeMe();
					player.sendMessage("A GM changed your class to " + newclass + ".");
					player.untransform();
					player.broadcastUserInfo();
					activeChar.setTarget(null);
					activeChar.setTarget(player);
					if (!wasAwaken && player.isAwaken())
					{
						SkillTreesData.getInstance().cleanSkillUponAwakening(player);
					}
					player.sendPacket(new ExSubjobInfo(player, SubclassInfoType.CLASS_CHANGED));
					player.sendPacket(new ExUserInfoInvenWeight(player));
					player.sendPacket(new AcquireSkillList(player));
					activeChar.sendMessage(player.getName() + " is a " + newclass + ".");
				}
				else
				{
					activeChar.sendMessage("Usage: //setclass <valid_new_classid>");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				AdminHtml.showAdminHtml(activeChar, "setclass/human_fighter.htm");
			}
			catch (NumberFormatException e)
			{
				activeChar.sendMessage("Usage: //setclass <valid_new_classid>");
			}
		}
		else if (command.startsWith("admin_settitle"))
		{
			try
			{
				final String val = command.substring(15);
				final L2Object target = activeChar.getTarget();
				L2PcInstance player = null;
				if (target instanceof L2PcInstance)
				{
					player = (L2PcInstance) target;
				}
				else
				{
					return false;
				}
				player.setTitle(val);
				player.sendMessage("Your title has been changed by a GM");
				player.broadcastTitleInfo();
			}
			catch (StringIndexOutOfBoundsException e)
			{ // Case of empty character title
				activeChar.sendMessage("You need to specify the new title.");
			}
		}
		else if (command.startsWith("admin_changename"))
		{
			try
			{
				final String val = command.substring(17);
				final L2Object target = activeChar.getTarget();
				L2PcInstance player = null;
				if (target instanceof L2PcInstance)
				{
					player = (L2PcInstance) target;
				}
				else
				{
					return false;
				}
				if (CharNameTable.getInstance().getIdByName(val) > 0)
				{
					activeChar.sendMessage("Warning, player " + val + " already exists");
					return false;
				}
				player.setName(val);
				player.storeMe();
				
				activeChar.sendMessage("Changed name to " + val);
				player.sendMessage("Your name has been changed by a GM.");
				player.broadcastUserInfo();
				
				if (player.isInParty())
				{
					// Delete party window for other party members
					player.getParty().broadcastToPartyMembers(player, PartySmallWindowDeleteAll.STATIC_PACKET);
					for (L2PcInstance member : player.getParty().getMembers())
					{
						// And re-add
						if (member != player)
						{
							member.sendPacket(new PartySmallWindowAll(member, player.getParty()));
						}
					}
				}
				if (player.getClan() != null)
				{
					player.getClan().broadcastClanStatus();
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{ // Case of empty character name
				activeChar.sendMessage("Usage: //setname new_name_for_target");
			}
		}
		else if (command.startsWith("admin_setsex"))
		{
			final L2Object target = activeChar.getTarget();
			L2PcInstance player = null;
			if (target instanceof L2PcInstance)
			{
				player = (L2PcInstance) target;
			}
			else
			{
				return false;
			}
			player.getAppearance().setSex(!player.getAppearance().getSex());
			player.sendMessage("Your gender has been changed by a GM");
			player.broadcastUserInfo();
		}
		else if (command.startsWith("admin_setcolor"))
		{
			try
			{
				final String val = command.substring(15);
				final L2Object target = activeChar.getTarget();
				L2PcInstance player = null;
				if (target instanceof L2PcInstance)
				{
					player = (L2PcInstance) target;
				}
				else
				{
					return false;
				}
				player.getAppearance().setNameColor(Integer.decode("0x" + val));
				player.sendMessage("Your name color has been changed by a GM");
				player.broadcastUserInfo();
			}
			catch (Exception e)
			{ // Case of empty color or invalid hex string
				activeChar.sendMessage("You need to specify a valid new color.");
			}
		}
		else if (command.startsWith("admin_settcolor"))
		{
			try
			{
				final String val = command.substring(16);
				final L2Object target = activeChar.getTarget();
				L2PcInstance player = null;
				if (target instanceof L2PcInstance)
				{
					player = (L2PcInstance) target;
				}
				else
				{
					return false;
				}
				player.getAppearance().setTitleColor(Integer.decode("0x" + val));
				player.sendMessage("Your title color has been changed by a GM");
				player.broadcastUserInfo();
			}
			catch (Exception e)
			{ // Case of empty color or invalid hex string
				activeChar.sendMessage("You need to specify a valid new color.");
			}
		}
		else if (command.startsWith("admin_fullfood"))
		{
			final L2Object target = activeChar.getTarget();
			if (target instanceof L2PetInstance)
			{
				final L2PetInstance targetPet = (L2PetInstance) target;
				targetPet.setCurrentFed(targetPet.getMaxFed());
				targetPet.broadcastStatusUpdate();
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
			}
		}
		else if (command.startsWith("admin_remove_clan_penalty"))
		{
			try
			{
				final StringTokenizer st = new StringTokenizer(command, " ");
				if (st.countTokens() != 3)
				{
					activeChar.sendMessage("Usage: //remove_clan_penalty join|create charname");
					return false;
				}
				
				st.nextToken();
				
				final boolean changeCreateExpiryTime = st.nextToken().equalsIgnoreCase("create");
				
				final String playerName = st.nextToken();
				final L2PcInstance player = L2World.getInstance().getPlayer(playerName);
				if (player == null)
				{
					final String updateQuery = "UPDATE characters SET " + (changeCreateExpiryTime ? "clan_create_expiry_time" : "clan_join_expiry_time") + " WHERE char_name=? LIMIT 1";
					try (Connection con = DatabaseFactory.getInstance().getConnection();
						PreparedStatement ps = con.prepareStatement(updateQuery))
					{
						ps.setString(1, playerName);
						ps.execute();
					}
				}
				else // removing penalty
				if (changeCreateExpiryTime)
				{
					player.setClanCreateExpiryTime(0);
				}
				else
				{
					player.setClanJoinExpiryTime(0);
				}
				
				activeChar.sendMessage("Clan penalty successfully removed to character: " + playerName);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else if (command.startsWith("admin_find_dualbox"))
		{
			int multibox = 2;
			try
			{
				final String val = command.substring(19);
				multibox = Integer.parseInt(val);
				if (multibox < 1)
				{
					activeChar.sendMessage("Usage: //find_dualbox [number > 0]");
					return false;
				}
			}
			catch (Exception e)
			{
			}
			findDualbox(activeChar, multibox);
		}
		else if (command.startsWith("admin_strict_find_dualbox"))
		{
			int multibox = 2;
			try
			{
				final String val = command.substring(26);
				multibox = Integer.parseInt(val);
				if (multibox < 1)
				{
					activeChar.sendMessage("Usage: //strict_find_dualbox [number > 0]");
					return false;
				}
			}
			catch (Exception e)
			{
			}
			findDualboxStrict(activeChar, multibox);
		}
		else if (command.startsWith("admin_tracert"))
		{
			final String[] data = command.split(" ");
			L2PcInstance pl = null;
			if (data.length > 1)
			{
				pl = L2World.getInstance().getPlayer(data[1]);
			}
			else
			{
				final L2Object target = activeChar.getTarget();
				if (target instanceof L2PcInstance)
				{
					pl = (L2PcInstance) target;
				}
			}
			
			if (pl == null)
			{
				activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
				return false;
			}
			
			final L2GameClient client = pl.getClient();
			if (client == null)
			{
				activeChar.sendMessage("Client is null.");
				return false;
			}
			
			if (client.isDetached())
			{
				activeChar.sendMessage("Client is detached.");
				return false;
			}
			
			String ip;
			final int[][] trace = client.getTrace();
			for (int i = 0; i < trace.length; i++)
			{
				ip = "";
				for (int o = 0; o < trace[0].length; o++)
				{
					ip = ip + trace[i][o];
					if (o != (trace[0].length - 1))
					{
						ip = ip + ".";
					}
				}
				activeChar.sendMessage("Hop" + i + ": " + ip);
			}
		}
		else if (command.startsWith("admin_summon_info"))
		{
			final L2Object target = activeChar.getTarget();
			if (target instanceof L2Summon)
			{
				gatherSummonInfo((L2Summon) target, activeChar);
			}
			else
			{
				activeChar.sendMessage("Invalid target.");
			}
		}
		else if (command.startsWith("admin_unsummon"))
		{
			final L2Object target = activeChar.getTarget();
			if (target instanceof L2Summon)
			{
				((L2Summon) target).unSummon(((L2Summon) target).getOwner());
			}
			else
			{
				activeChar.sendMessage("Usable only with Pets/Summons");
			}
		}
		else if (command.startsWith("admin_summon_setlvl"))
		{
			final L2Object target = activeChar.getTarget();
			if (target instanceof L2PetInstance)
			{
				final L2PetInstance pet = (L2PetInstance) target;
				try
				{
					final String val = command.substring(20);
					final int level = Integer.parseInt(val);
					long newexp, oldexp = 0;
					oldexp = pet.getStat().getExp();
					newexp = pet.getStat().getExpForLevel(level);
					if (oldexp > newexp)
					{
						pet.getStat().removeExp(oldexp - newexp);
					}
					else if (oldexp < newexp)
					{
						pet.getStat().addExp(newexp - oldexp);
					}
				}
				catch (Exception e)
				{
				}
			}
			else
			{
				activeChar.sendMessage("Usable only with Pets");
			}
		}
		else if (command.startsWith("admin_show_pet_inv"))
		{
			L2Object target;
			try
			{
				target = L2World.getInstance().getPet(Integer.parseInt(command.substring(19)));
			}
			catch (Exception e)
			{
				target = activeChar.getTarget();
			}
			
			if (target instanceof L2PetInstance)
			{
				activeChar.sendPacket(new GMViewItemList((L2PetInstance) target));
			}
			else
			{
				activeChar.sendMessage("Usable only with Pets");
			}
		}
		else if (command.startsWith("admin_partyinfo"))
		{
			L2Object target;
			try
			{
				final String val = command.substring(16);
				target = L2World.getInstance().getPlayer(val);
				if (target == null)
				{
					target = activeChar.getTarget();
				}
			}
			catch (Exception e)
			{
				target = activeChar.getTarget();
			}
			
			if (target instanceof L2PcInstance)
			{
				if (((L2PcInstance) target).isInParty())
				{
					gatherPartyInfo((L2PcInstance) target, activeChar);
				}
				else
				{
					activeChar.sendMessage("Not in party.");
				}
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
			}
		}
		else if (command.equals("admin_setnoble"))
		{
			L2PcInstance player = null;
			if (activeChar.getTarget() == null)
			{
				player = activeChar;
			}
			else if (activeChar.getTarget() instanceof L2PcInstance)
			{
				player = (L2PcInstance) activeChar.getTarget();
			}
			
			if (player != null)
			{
				player.setNoble(!player.isNoble());
				if (player.getObjectId() != activeChar.getObjectId())
				{
					activeChar.sendMessage("You've changed nobless status of: " + player.getName());
				}
				player.sendMessage("GM changed your nobless status!");
			}
		}
		else if (command.startsWith("admin_set_hp"))
		{
			final String[] data = command.split(" ");
			try
			{
				final L2Object target = activeChar.getTarget();
				if ((target == null) || !target.isCharacter())
				{
					activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
					return false;
				}
				((L2Character) target).setCurrentHp(Double.parseDouble(data[1]));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //set_hp 1000");
			}
		}
		else if (command.startsWith("admin_set_mp"))
		{
			final String[] data = command.split(" ");
			try
			{
				final L2Object target = activeChar.getTarget();
				if ((target == null) || !target.isCharacter())
				{
					activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
					return false;
				}
				((L2Character) target).setCurrentMp(Double.parseDouble(data[1]));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //set_mp 1000");
			}
		}
		else if (command.startsWith("admin_set_cp"))
		{
			final String[] data = command.split(" ");
			try
			{
				final L2Object target = activeChar.getTarget();
				if ((target == null) || !target.isCharacter())
				{
					activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
					return false;
				}
				((L2Character) target).setCurrentCp(Double.parseDouble(data[1]));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //set_cp 1000");
			}
		}
		else if (command.startsWith("admin_set_pvp_flag"))
		{
			try
			{
				final L2Object target = activeChar.getTarget();
				if ((target == null) || !target.isPlayable())
				{
					activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
					return false;
				}
				((L2Playable) target).updatePvPFlag(Math.abs(((L2Playable) target).getPvpFlag() - 1));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //set_pvp_flag");
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void listCharacters(L2PcInstance activeChar, int page)
	{
		final L2PcInstance[] players = L2World.getInstance().getPlayersSortedBy(Comparator.comparingLong(L2PcInstance::getUptime));
		
		final NpcHtmlMessage html = new NpcHtmlMessage();
		html.setFile(activeChar.getHtmlPrefix(), "html/admin/charlist.htm");
		
		final PageResult result = HtmlUtil.createPage(players, page, 20, i ->
		{
			return "<td align=center><a action=\"bypass -h admin_show_characters " + i + "\">Page " + (i + 1) + "</a></td>";
		}, player ->
		{
			final StringBuilder sb = new StringBuilder();
			sb.append("<tr>");
			sb.append("<td width=80><a action=\"bypass -h admin_character_info " + player.getName() + "\">" + player.getName() + "</a></td>");
			sb.append("<td width=110>" + ClassListData.getInstance().getClass(player.getClassId()).getClientCode() + "</td><td width=40>" + player.getLevel() + "</td>");
			sb.append("</tr>");
			return sb.toString();
		});
		
		if (result.getPages() > 0)
		{
			html.replace("%pages%", "<table width=280 cellspacing=0><tr>" + result.getPagerTemplate() + "</tr></table>");
		}
		else
		{
			html.replace("%pages%", "");
		}
		
		html.replace("%players%", result.getBodyTemplate().toString());
		activeChar.sendPacket(html);
	}
	
	private void showCharacterInfo(L2PcInstance activeChar, L2PcInstance player)
	{
		if (player == null)
		{
			final L2Object target = activeChar.getTarget();
			if (target instanceof L2PcInstance)
			{
				player = (L2PcInstance) target;
			}
			else
			{
				return;
			}
		}
		else
		{
			activeChar.setTarget(player);
		}
		gatherCharacterInfo(activeChar, player, "charinfo.htm");
	}
	
	/**
	 * Retrieve and replace player's info in filename htm file, sends it to activeChar as NpcHtmlMessage.
	 * @param activeChar
	 * @param player
	 * @param filename
	 */
	private void gatherCharacterInfo(L2PcInstance activeChar, L2PcInstance player, String filename)
	{
		String ip = "N/A";
		
		if (player == null)
		{
			activeChar.sendMessage("Player is null.");
			return;
		}
		
		final L2GameClient client = player.getClient();
		if (client == null)
		{
			activeChar.sendMessage("Client is null.");
		}
		else if (client.isDetached())
		{
			activeChar.sendMessage("Client is detached.");
		}
		else
		{
			ip = client.getConnection().getInetAddress().getHostAddress();
		}
		
		final NpcHtmlMessage adminReply = new NpcHtmlMessage();
		adminReply.setFile(activeChar.getHtmlPrefix(), "html/admin/" + filename);
		adminReply.replace("%name%", player.getName());
		adminReply.replace("%level%", String.valueOf(player.getLevel()));
		adminReply.replace("%clan%", String.valueOf(player.getClan() != null ? "<a action=\"bypass -h admin_clan_info " + player.getObjectId() + "\">" + player.getClan().getName() + "</a>" : null));
		adminReply.replace("%xp%", String.valueOf(player.getExp()));
		adminReply.replace("%sp%", String.valueOf(player.getSp()));
		adminReply.replace("%class%", ClassListData.getInstance().getClass(player.getClassId()).getClientCode());
		adminReply.replace("%ordinal%", String.valueOf(player.getClassId().ordinal()));
		adminReply.replace("%classid%", String.valueOf(player.getClassId()));
		adminReply.replace("%baseclass%", ClassListData.getInstance().getClass(player.getBaseClassId()).getClientCode());
		adminReply.replace("%x%", String.valueOf(player.getX()));
		adminReply.replace("%y%", String.valueOf(player.getY()));
		adminReply.replace("%z%", String.valueOf(player.getZ()));
		adminReply.replace("%currenthp%", String.valueOf((int) player.getCurrentHp()));
		adminReply.replace("%maxhp%", String.valueOf(player.getMaxHp()));
		adminReply.replace("%karma%", String.valueOf(player.getReputation()));
		adminReply.replace("%currentmp%", String.valueOf((int) player.getCurrentMp()));
		adminReply.replace("%maxmp%", String.valueOf(player.getMaxMp()));
		adminReply.replace("%pvpflag%", String.valueOf(player.getPvpFlag()));
		adminReply.replace("%currentcp%", String.valueOf((int) player.getCurrentCp()));
		adminReply.replace("%maxcp%", String.valueOf(player.getMaxCp()));
		adminReply.replace("%pvpkills%", String.valueOf(player.getPvpKills()));
		adminReply.replace("%pkkills%", String.valueOf(player.getPkKills()));
		adminReply.replace("%currentload%", String.valueOf(player.getCurrentLoad()));
		adminReply.replace("%maxload%", String.valueOf(player.getMaxLoad()));
		adminReply.replace("%percent%", String.valueOf(Util.roundTo(((float) player.getCurrentLoad() / (float) player.getMaxLoad()) * 100, 2)));
		adminReply.replace("%patk%", String.valueOf((int) player.getPAtk(null)));
		adminReply.replace("%matk%", String.valueOf((int) player.getMAtk(null, null)));
		adminReply.replace("%pdef%", String.valueOf((int) player.getPDef(null)));
		adminReply.replace("%mdef%", String.valueOf((int) player.getMDef(null, null)));
		adminReply.replace("%accuracy%", String.valueOf(player.getAccuracy()));
		adminReply.replace("%evasion%", String.valueOf(player.getEvasionRate(null)));
		adminReply.replace("%critical%", String.valueOf(player.getCriticalHit(null, null)));
		adminReply.replace("%runspeed%", String.valueOf((int) player.getRunSpeed()));
		adminReply.replace("%patkspd%", String.valueOf(player.getPAtkSpd()));
		adminReply.replace("%matkspd%", String.valueOf(player.getMAtkSpd()));
		adminReply.replace("%access%", player.getAccessLevel().getLevel() + " (" + player.getAccessLevel().getName() + ")");
		adminReply.replace("%account%", player.getAccountName());
		adminReply.replace("%ip%", ip);
		adminReply.replace("%ai%", String.valueOf(player.getAI().getIntention().name()));
		adminReply.replace("%inst%", player.getInstanceId() > 0 ? "<tr><td>InstanceId:</td><td><a action=\"bypass -h admin_instance_spawns " + String.valueOf(player.getInstanceId()) + "\">" + String.valueOf(player.getInstanceId()) + "</a></td></tr>" : "");
		adminReply.replace("%noblesse%", player.isNoble() ? "Yes" : "No");
		activeChar.sendPacket(adminReply);
	}
	
	private void setTargetKarma(L2PcInstance activeChar, int newKarma)
	{
		// function to change karma of selected char
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			return;
		}
		
		// for display
		final int oldKarma = player.getReputation();
		// update reputation
		player.setKarma(newKarma);
		// Common character information
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOUR_REPUTATION_HAS_BEEN_CHANGED_TO_S1);
		sm.addInt(newKarma);
		player.sendPacket(sm);
		// Admin information
		activeChar.sendMessage("Successfully Changed karma for " + player.getName() + " from (" + oldKarma + ") to (" + newKarma + ").");
		if (Config.DEBUG)
		{
			_log.fine("[SET KARMA] [GM]" + activeChar.getName() + " Changed karma for " + player.getName() + " from (" + oldKarma + ") to (" + newKarma + ").");
		}
	}
	
	private void editCharacter(L2PcInstance activeChar, String targetName)
	{
		L2Object target = null;
		if (targetName != null)
		{
			target = L2World.getInstance().getPlayer(targetName);
		}
		else
		{
			target = activeChar.getTarget();
		}
		
		if (target instanceof L2PcInstance)
		{
			gatherCharacterInfo(activeChar, (L2PcInstance) target, "charedit.htm");
		}
	}
	
	/**
	 * @param activeChar
	 * @param CharacterToFind
	 */
	private void findCharacter(L2PcInstance activeChar, String CharacterToFind)
	{
		int CharactersFound = 0;
		String name;
		final NpcHtmlMessage adminReply = new NpcHtmlMessage();
		adminReply.setFile(activeChar.getHtmlPrefix(), "html/admin/charfind.htm");
		
		final StringBuilder replyMSG = new StringBuilder(1000);
		
		for (L2PcInstance player : L2World.getInstance().getPlayersSortedBy(Comparator.comparingLong(L2PcInstance::getUptime)))
		{ // Add player info into new Table row
			name = player.getName();
			if (name.toLowerCase().contains(CharacterToFind.toLowerCase()))
			{
				CharactersFound = CharactersFound + 1;
				StringUtil.append(replyMSG, "<tr><td width=80><a action=\"bypass -h admin_character_info ", name, "\">", name, "</a></td><td width=110>", ClassListData.getInstance().getClass(player.getClassId()).getClientCode(), "</td><td width=40>", String.valueOf(player.getLevel()), "</td></tr>");
			}
			if (CharactersFound > 20)
			{
				break;
			}
		}
		adminReply.replace("%results%", replyMSG.toString());
		
		final String replyMSG2;
		
		if (CharactersFound == 0)
		{
			replyMSG2 = "s. Please try again.";
		}
		else if (CharactersFound > 20)
		{
			adminReply.replace("%number%", " more than 20");
			replyMSG2 = "s.<br>Please refine your search to see all of the results.";
		}
		else if (CharactersFound == 1)
		{
			replyMSG2 = ".";
		}
		else
		{
			replyMSG2 = "s.";
		}
		
		adminReply.replace("%number%", String.valueOf(CharactersFound));
		adminReply.replace("%end%", replyMSG2);
		activeChar.sendPacket(adminReply);
	}
	
	/**
	 * @param activeChar
	 * @param IpAdress
	 * @throws IllegalArgumentException
	 */
	private void findCharactersPerIp(L2PcInstance activeChar, String IpAdress) throws IllegalArgumentException
	{
		boolean findDisconnected = false;
		
		if (IpAdress.equals("disconnected"))
		{
			findDisconnected = true;
		}
		else if (!IpAdress.matches("^(?:(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))\\.){3}(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))$"))
		{
			throw new IllegalArgumentException("Malformed IPv4 number");
		}
		
		int CharactersFound = 0;
		L2GameClient client;
		String name, ip = "0.0.0.0";
		final StringBuilder replyMSG = new StringBuilder(1000);
		final NpcHtmlMessage adminReply = new NpcHtmlMessage();
		adminReply.setFile(activeChar.getHtmlPrefix(), "html/admin/ipfind.htm");
		for (L2PcInstance player : L2World.getInstance().getPlayersSortedBy(Comparator.comparingLong(L2PcInstance::getUptime)))
		{
			client = player.getClient();
			if (client == null)
			{
				continue;
			}
			
			if (client.isDetached())
			{
				if (!findDisconnected)
				{
					continue;
				}
			}
			else
			{
				if (findDisconnected)
				{
					continue;
				}
				
				ip = client.getConnection().getInetAddress().getHostAddress();
				if (!ip.equals(IpAdress))
				{
					continue;
				}
			}
			
			name = player.getName();
			CharactersFound = CharactersFound + 1;
			StringUtil.append(replyMSG, "<tr><td width=80><a action=\"bypass -h admin_character_info ", name, "\">", name, "</a></td><td width=110>", ClassListData.getInstance().getClass(player.getClassId()).getClientCode(), "</td><td width=40>", String.valueOf(player.getLevel()), "</td></tr>");
			
			if (CharactersFound > 20)
			{
				break;
			}
		}
		adminReply.replace("%results%", replyMSG.toString());
		
		final String replyMSG2;
		
		if (CharactersFound == 0)
		{
			replyMSG2 = "s. Maybe they got d/c? :)";
		}
		else if (CharactersFound > 20)
		{
			adminReply.replace("%number%", " more than " + String.valueOf(CharactersFound));
			replyMSG2 = "s.<br>In order to avoid you a client crash I won't <br1>display results beyond the 20th character.";
		}
		else if (CharactersFound == 1)
		{
			replyMSG2 = ".";
		}
		else
		{
			replyMSG2 = "s.";
		}
		adminReply.replace("%ip%", IpAdress);
		adminReply.replace("%number%", String.valueOf(CharactersFound));
		adminReply.replace("%end%", replyMSG2);
		activeChar.sendPacket(adminReply);
	}
	
	/**
	 * @param activeChar
	 * @param characterName
	 * @throws IllegalArgumentException
	 */
	private void findCharactersPerAccount(L2PcInstance activeChar, String characterName) throws IllegalArgumentException
	{
		final L2PcInstance player = L2World.getInstance().getPlayer(characterName);
		if (player == null)
		{
			throw new IllegalArgumentException("Player doesn't exist");
		}
		
		final Map<Integer, String> chars = player.getAccountChars();
		final StringBuilder replyMSG = new StringBuilder(chars.size() * 20);
		chars.values().stream().forEachOrdered(name -> StringUtil.append(replyMSG, name, "<br1>"));
		
		final NpcHtmlMessage adminReply = new NpcHtmlMessage();
		adminReply.setFile(activeChar.getHtmlPrefix(), "html/admin/accountinfo.htm");
		adminReply.replace("%account%", player.getAccountName());
		adminReply.replace("%player%", characterName);
		adminReply.replace("%characters%", replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	/**
	 * @param activeChar
	 * @param multibox
	 */
	private void findDualbox(L2PcInstance activeChar, int multibox)
	{
		final Map<String, List<L2PcInstance>> ipMap = new HashMap<>();
		String ip = "0.0.0.0";
		L2GameClient client;
		final Map<String, Integer> dualboxIPs = new HashMap<>();
		
		for (L2PcInstance player : L2World.getInstance().getPlayersSortedBy(Comparator.comparingLong(L2PcInstance::getUptime)))
		{
			client = player.getClient();
			if ((client == null) || client.isDetached())
			{
				continue;
			}
			
			ip = client.getConnection().getInetAddress().getHostAddress();
			if (ipMap.get(ip) == null)
			{
				ipMap.put(ip, new ArrayList<L2PcInstance>());
			}
			ipMap.get(ip).add(player);
			
			if (ipMap.get(ip).size() >= multibox)
			{
				final Integer count = dualboxIPs.get(ip);
				if (count != null)
				{
					dualboxIPs.put(ip, count + 1);
				}
				else
				{
					dualboxIPs.put(ip, multibox);
				}
			}
		}
		
		final List<String> keys = new ArrayList<>(dualboxIPs.keySet());
		keys.sort(Comparator.comparing(s -> dualboxIPs.get(s)).reversed());
		
		final StringBuilder results = new StringBuilder();
		for (String dualboxIP : keys)
		{
			StringUtil.append(results, "<a action=\"bypass -h admin_find_ip " + dualboxIP + "\">" + dualboxIP + " (" + dualboxIPs.get(dualboxIP) + ")</a><br1>");
		}
		
		final NpcHtmlMessage adminReply = new NpcHtmlMessage();
		adminReply.setFile(activeChar.getHtmlPrefix(), "html/admin/dualbox.htm");
		adminReply.replace("%multibox%", String.valueOf(multibox));
		adminReply.replace("%results%", results.toString());
		adminReply.replace("%strict%", "");
		activeChar.sendPacket(adminReply);
	}
	
	private void findDualboxStrict(L2PcInstance activeChar, int multibox)
	{
		final Map<IpPack, List<L2PcInstance>> ipMap = new HashMap<>();
		L2GameClient client;
		final Map<IpPack, Integer> dualboxIPs = new HashMap<>();
		
		for (L2PcInstance player : L2World.getInstance().getPlayersSortedBy(Comparator.comparingLong(L2PcInstance::getUptime)))
		{
			client = player.getClient();
			if ((client == null) || client.isDetached())
			{
				continue;
			}
			
			final IpPack pack = new IpPack(client.getConnection().getInetAddress().getHostAddress(), client.getTrace());
			if (ipMap.get(pack) == null)
			{
				ipMap.put(pack, new ArrayList<L2PcInstance>());
			}
			ipMap.get(pack).add(player);
			
			if (ipMap.get(pack).size() >= multibox)
			{
				final Integer count = dualboxIPs.get(pack);
				if (count != null)
				{
					dualboxIPs.put(pack, count + 1);
				}
				else
				{
					dualboxIPs.put(pack, multibox);
				}
			}
		}
		
		final List<IpPack> keys = new ArrayList<>(dualboxIPs.keySet());
		keys.sort(Comparator.comparing(s -> dualboxIPs.get(s)).reversed());
		
		final StringBuilder results = new StringBuilder();
		for (IpPack dualboxIP : keys)
		{
			StringUtil.append(results, "<a action=\"bypass -h admin_find_ip " + dualboxIP.ip + "\">" + dualboxIP.ip + " (" + dualboxIPs.get(dualboxIP) + ")</a><br1>");
		}
		
		final NpcHtmlMessage adminReply = new NpcHtmlMessage();
		adminReply.setFile(activeChar.getHtmlPrefix(), "html/admin/dualbox.htm");
		adminReply.replace("%multibox%", String.valueOf(multibox));
		adminReply.replace("%results%", results.toString());
		adminReply.replace("%strict%", "strict_");
		activeChar.sendPacket(adminReply);
	}
	
	private final class IpPack
	{
		String ip;
		int[][] tracert;
		
		public IpPack(String ip, int[][] tracert)
		{
			this.ip = ip;
			this.tracert = tracert;
		}
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = (prime * 1) + ((ip == null) ? 0 : ip.hashCode());
			for (int[] array : tracert)
			{
				result = (prime * result) + Arrays.hashCode(array);
			}
			return result;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj == null)
			{
				return false;
			}
			if (getClass() != obj.getClass())
			{
				return false;
			}
			final IpPack other = (IpPack) obj;
			if (!getOuterType().equals(other.getOuterType()))
			{
				return false;
			}
			if (ip == null)
			{
				if (other.ip != null)
				{
					return false;
				}
			}
			else if (!ip.equals(other.ip))
			{
				return false;
			}
			for (int i = 0; i < tracert.length; i++)
			{
				for (int o = 0; o < tracert[0].length; o++)
				{
					if (tracert[i][o] != other.tracert[i][o])
					{
						return false;
					}
				}
			}
			return true;
		}
		
		private AdminEditChar getOuterType()
		{
			return AdminEditChar.this;
		}
	}
	
	private void gatherSummonInfo(L2Summon target, L2PcInstance activeChar)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage();
		html.setFile(activeChar.getHtmlPrefix(), "html/admin/petinfo.htm");
		final String name = target.getName();
		html.replace("%name%", name == null ? "N/A" : name);
		html.replace("%level%", Integer.toString(target.getLevel()));
		html.replace("%exp%", Long.toString(target.getStat().getExp()));
		final String owner = target.getActingPlayer().getName();
		html.replace("%owner%", " <a action=\"bypass -h admin_character_info " + owner + "\">" + owner + "</a>");
		html.replace("%class%", target.getClass().getSimpleName());
		html.replace("%ai%", target.hasAI() ? String.valueOf(target.getAI().getIntention().name()) : "NULL");
		html.replace("%hp%", (int) target.getStatus().getCurrentHp() + "/" + target.getStat().getMaxHp());
		html.replace("%mp%", (int) target.getStatus().getCurrentMp() + "/" + target.getStat().getMaxMp());
		html.replace("%karma%", Integer.toString(target.getReputation()));
		html.replace("%race%", target.getTemplate().getRace().toString());
		if (target instanceof L2PetInstance)
		{
			html.replace("%inv%", " <a action=\"bypass admin_show_pet_inv " + target.getActingPlayer().getObjectId() + "\">view</a>");
		}
		else
		{
			html.replace("%inv%", "none");
		}
		if (target instanceof L2PetInstance)
		{
			html.replace("%food%", ((L2PetInstance) target).getCurrentFed() + "/" + ((L2PetInstance) target).getPetLevelData().getPetMaxFeed());
			html.replace("%load%", ((L2PetInstance) target).getInventory().getTotalWeight() + "/" + ((L2PetInstance) target).getMaxLoad());
		}
		else
		{
			html.replace("%food%", "N/A");
			html.replace("%load%", "N/A");
		}
		activeChar.sendPacket(html);
	}
	
	private void gatherPartyInfo(L2PcInstance target, L2PcInstance activeChar)
	{
		boolean color = true;
		final NpcHtmlMessage html = new NpcHtmlMessage();
		html.setFile(activeChar.getHtmlPrefix(), "html/admin/partyinfo.htm");
		final StringBuilder text = new StringBuilder(400);
		for (L2PcInstance member : target.getParty().getMembers())
		{
			if (color)
			{
				text.append("<tr><td><table width=270 border=0 bgcolor=131210 cellpadding=2><tr><td width=30 align=right>");
			}
			else
			{
				text.append("<tr><td><table width=270 border=0 cellpadding=2><tr><td width=30 align=right>");
			}
			text.append(member.getLevel() + "</td><td width=130><a action=\"bypass -h admin_character_info " + member.getName() + "\">" + member.getName() + "</a>");
			text.append("</td><td width=110 align=right>" + member.getClassId().toString() + "</td></tr></table></td></tr>");
			color = !color;
		}
		html.replace("%player%", target.getName());
		html.replace("%party%", text.toString());
		activeChar.sendPacket(html);
	}
}
