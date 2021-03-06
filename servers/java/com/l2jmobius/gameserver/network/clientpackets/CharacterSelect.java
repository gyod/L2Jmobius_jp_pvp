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
package com.l2jmobius.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.data.sql.impl.CharNameTable;
import com.l2jmobius.gameserver.data.xml.impl.SecondaryAuthData;
import com.l2jmobius.gameserver.instancemanager.AntiFeedManager;
import com.l2jmobius.gameserver.instancemanager.PunishmentManager;
import com.l2jmobius.gameserver.model.CharSelectInfoPackage;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.events.Containers;
import com.l2jmobius.gameserver.model.events.EventDispatcher;
import com.l2jmobius.gameserver.model.events.impl.character.player.OnPlayerSelect;
import com.l2jmobius.gameserver.model.events.returns.TerminateReturn;
import com.l2jmobius.gameserver.model.punishment.PunishmentAffect;
import com.l2jmobius.gameserver.model.punishment.PunishmentType;
import com.l2jmobius.gameserver.network.L2GameClient;
import com.l2jmobius.gameserver.network.L2GameClient.GameClientState;
import com.l2jmobius.gameserver.network.serverpackets.CharSelected;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.network.serverpackets.ServerClose;

/**
 * This class ...
 * @version $Revision: 1.5.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class CharacterSelect extends L2GameClientPacket
{
	private static final String _C__12_CHARACTERSELECT = "[C] 12 CharacterSelect";
	protected static final Logger _logAccounting = Logger.getLogger("accounting");
	
	// cd
	private int _charSlot;
	
	@SuppressWarnings("unused")
	private int _unk1; // new in C4
	@SuppressWarnings("unused")
	private int _unk2; // new in C4
	@SuppressWarnings("unused")
	private int _unk3; // new in C4
	@SuppressWarnings("unused")
	private int _unk4; // new in C4
	
	@Override
	protected void readImpl()
	{
		_charSlot = readD();
		_unk1 = readH();
		_unk2 = readD();
		_unk3 = readD();
		_unk4 = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2GameClient client = getClient();
		if (!client.getFloodProtectors().getCharacterSelect().tryPerformAction("CharacterSelect"))
		{
			return;
		}
		
		if (SecondaryAuthData.getInstance().isEnabled() && !client.getSecondaryAuth().isAuthed())
		{
			client.getSecondaryAuth().openDialog();
			return;
		}
		
		// We should always be able to acquire the lock
		// But if we can't lock then nothing should be done (i.e. repeated packet)
		if (!client.getActiveCharLock().tryLock())
		{
			return;
		}
		
		try
		{
			// should always be null
			// but if not then this is repeated packet and nothing should be done here
			if (client.getActiveChar() == null)
			{
				final CharSelectInfoPackage info = client.getCharSelection(_charSlot);
				if (info == null)
				{
					return;
				}
				
				// Banned?
				if (PunishmentManager.getInstance().hasPunishment(info.getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.BAN) || PunishmentManager.getInstance().hasPunishment(client.getAccountName(), PunishmentAffect.ACCOUNT, PunishmentType.BAN) || PunishmentManager.getInstance().hasPunishment(client.getConnectionAddress().getHostAddress(), PunishmentAffect.IP, PunishmentType.BAN) || (info.getAccessLevel() < 0))
				{
					client.close(ServerClose.STATIC_PACKET);
					return;
				}
				
				if ((Config.L2JMOD_DUALBOX_CHECK_MAX_PLAYERS_PER_IP > 0) && !AntiFeedManager.getInstance().tryAddClient(AntiFeedManager.GAME_ID, client, Config.L2JMOD_DUALBOX_CHECK_MAX_PLAYERS_PER_IP))
				{
					final NpcHtmlMessage msg = new NpcHtmlMessage();
					msg.setFile(info.getHtmlPrefix(), "html/mods/IPRestriction.htm");
					msg.replace("%max%", String.valueOf(AntiFeedManager.getInstance().getLimit(client, Config.L2JMOD_DUALBOX_CHECK_MAX_PLAYERS_PER_IP)));
					client.sendPacket(msg);
					return;
				}
				
				if (Config.FACTION_SYSTEM_ENABLED && Config.FACTION_BALANCE_ONLINE_PLAYERS)
				{
					if (info.isGood() && (L2World.getInstance().getAllGoodPlayersCount() >= (L2World.getInstance().getAllEvilPlayersCount() + Config.FACTION_BALANCE_PLAYER_EXCEED_LIMIT)))
					{
						final NpcHtmlMessage msg = new NpcHtmlMessage();
						msg.setFile(info.getHtmlPrefix(), "html/mods/Faction/ExceededOnlineLimit.htm");
						msg.replace("%more%", Config.FACTION_GOOD_TEAM_NAME);
						msg.replace("%less%", Config.FACTION_EVIL_TEAM_NAME);
						client.sendPacket(msg);
						return;
					}
					if (info.isEvil() && (L2World.getInstance().getAllEvilPlayersCount() >= (L2World.getInstance().getAllGoodPlayersCount() + Config.FACTION_BALANCE_PLAYER_EXCEED_LIMIT)))
					{
						final NpcHtmlMessage msg = new NpcHtmlMessage();
						msg.setFile(info.getHtmlPrefix(), "html/mods/Faction/ExceededOnlineLimit.htm");
						msg.replace("%more%", Config.FACTION_EVIL_TEAM_NAME);
						msg.replace("%less%", Config.FACTION_GOOD_TEAM_NAME);
						client.sendPacket(msg);
						return;
					}
				}
				
				// The L2PcInstance must be created here, so that it can be attached to the L2GameClient
				if (Config.DEBUG)
				{
					_log.fine("selected slot:" + _charSlot);
				}
				
				// load up character from disk
				final L2PcInstance cha = client.loadCharFromDisk(_charSlot);
				if (cha == null)
				{
					return; // handled in L2GameClient
				}
				L2World.getInstance().addPlayerToWorld(cha);
				CharNameTable.getInstance().addName(cha);
				
				cha.setClient(client);
				client.setActiveChar(cha);
				cha.setOnlineStatus(true, true);
				
				final TerminateReturn terminate = EventDispatcher.getInstance().notifyEvent(new OnPlayerSelect(cha, cha.getObjectId(), cha.getName(), getClient()), Containers.Players(), TerminateReturn.class);
				if ((terminate != null) && terminate.terminate())
				{
					cha.deleteMe();
					return;
				}
				
				client.setState(GameClientState.IN_GAME);
				sendPacket(new CharSelected(cha, client.getSessionId().playOkID1));
			}
		}
		finally
		{
			client.getActiveCharLock().unlock();
		}
		
		final LogRecord record = new LogRecord(Level.INFO, "Logged in");
		record.setParameters(new Object[]
		{
			client
		});
		_logAccounting.log(record);
	}
	
	@Override
	public String getType()
	{
		return _C__12_CHARACTERSELECT;
	}
}
