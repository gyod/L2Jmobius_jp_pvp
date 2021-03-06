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
package com.l2jmobius.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jmobius.commons.database.DatabaseFactory;
import com.l2jmobius.gameserver.enums.MacroType;
import com.l2jmobius.gameserver.enums.MacroUpdateType;
import com.l2jmobius.gameserver.enums.ShortcutType;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.interfaces.IRestorable;
import com.l2jmobius.gameserver.network.serverpackets.SendMacroList;
import com.l2jmobius.util.StringUtil;

public class MacroList implements IRestorable
{
	private static final Logger _log = Logger.getLogger(MacroList.class.getName());
	
	private final L2PcInstance _owner;
	private int _macroId;
	private final Map<Integer, Macro> _macroses = Collections.synchronizedMap(new LinkedHashMap<Integer, Macro>());
	
	public MacroList(L2PcInstance owner)
	{
		_owner = owner;
		_macroId = 1000;
	}
	
	public Map<Integer, Macro> getAllMacroses()
	{
		return _macroses;
	}
	
	public void registerMacro(Macro macro)
	{
		MacroUpdateType updateType = MacroUpdateType.ADD;
		if (macro.getId() == 0)
		{
			macro.setId(_macroId++);
			while (_macroses.containsKey(macro.getId()))
			{
				macro.setId(_macroId++);
			}
			_macroses.put(macro.getId(), macro);
		}
		else
		{
			updateType = MacroUpdateType.MODIFY;
			final Macro old = _macroses.put(macro.getId(), macro);
			if (old != null)
			{
				deleteMacroFromDb(old);
			}
		}
		registerMacroInDb(macro);
		_owner.sendPacket(new SendMacroList(1, macro, updateType));
	}
	
	public void deleteMacro(int id)
	{
		final Macro removed = _macroses.remove(id);
		if (removed != null)
		{
			deleteMacroFromDb(removed);
		}
		
		final Shortcut[] allShortCuts = _owner.getAllShortCuts();
		for (Shortcut sc : allShortCuts)
		{
			if ((sc.getId() == id) && (sc.getType() == ShortcutType.MACRO))
			{
				_owner.deleteShortCut(sc.getSlot(), sc.getPage());
			}
		}
		_owner.sendPacket(new SendMacroList(0, removed, MacroUpdateType.DELETE));
	}
	
	public void sendAllMacros()
	{
		final Collection<Macro> allMacros = _macroses.values();
		final int count = allMacros.size();
		
		synchronized (_macroses)
		{
			if (allMacros.isEmpty())
			{
				_owner.sendPacket(new SendMacroList(0, null, MacroUpdateType.LIST));
			}
			else
			{
				for (Macro m : allMacros)
				{
					_owner.sendPacket(new SendMacroList(count, m, MacroUpdateType.LIST));
				}
			}
		}
	}
	
	private void registerMacroInDb(Macro macro)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO character_macroses (charId,id,icon,name,descr,acronym,commands) values(?,?,?,?,?,?,?)"))
		{
			ps.setInt(1, _owner.getObjectId());
			ps.setInt(2, macro.getId());
			ps.setInt(3, macro.getIcon());
			ps.setString(4, macro.getName());
			ps.setString(5, macro.getDescr());
			ps.setString(6, macro.getAcronym());
			final StringBuilder sb = new StringBuilder(300);
			for (MacroCmd cmd : macro.getCommands())
			{
				StringUtil.append(sb, String.valueOf(cmd.getType().ordinal()), ",", String.valueOf(cmd.getD1()), ",", String.valueOf(cmd.getD2()));
				if ((cmd.getCmd() != null) && (cmd.getCmd().length() > 0))
				{
					StringUtil.append(sb, ",", cmd.getCmd());
				}
				sb.append(';');
			}
			
			if (sb.length() > 255)
			{
				sb.setLength(255);
			}
			
			ps.setString(7, sb.toString());
			ps.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not store macro:", e);
		}
	}
	
	private void deleteMacroFromDb(Macro macro)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM character_macroses WHERE charId=? AND id=?"))
		{
			ps.setInt(1, _owner.getObjectId());
			ps.setInt(2, macro.getId());
			ps.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not delete macro:", e);
		}
	}
	
	@Override
	public boolean restoreMe()
	{
		_macroses.clear();
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT charId, id, icon, name, descr, acronym, commands FROM character_macroses WHERE charId=?"))
		{
			ps.setInt(1, _owner.getObjectId());
			try (ResultSet rset = ps.executeQuery())
			{
				while (rset.next())
				{
					final int id = rset.getInt("id");
					final int icon = rset.getInt("icon");
					final String name = rset.getString("name");
					final String descr = rset.getString("descr");
					final String acronym = rset.getString("acronym");
					final List<MacroCmd> commands = new ArrayList<>();
					final StringTokenizer st1 = new StringTokenizer(rset.getString("commands"), ";");
					while (st1.hasMoreTokens())
					{
						final StringTokenizer st = new StringTokenizer(st1.nextToken(), ",");
						if (st.countTokens() < 3)
						{
							continue;
						}
						commands.add(new MacroCmd(commands.size(), MacroType.values()[Integer.parseInt(st.nextToken())], Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), (st.hasMoreTokens() ? st.nextToken() : "")));
					}
					_macroses.put(id, new Macro(id, icon, name, descr, acronym, commands));
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not store shortcuts:", e);
			return false;
		}
		return true;
	}
}
