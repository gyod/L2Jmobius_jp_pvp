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
package com.l2jmobius.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;

public final class WareHouseDepositList extends AbstractItemPacket
{
	public static final int PRIVATE = 1;
	public static final int CLAN = 2;
	public static final int CASTLE = 3;
	public static final int FREIGHT = 4;
	private final long _playerAdena;
	private final L2ItemInstance[] _itemsInWarehouse;
	private final List<L2ItemInstance> _items = new ArrayList<>();
	private final List<Integer> _itemsStackable = new ArrayList<>();
	/**
	 * <ul>
	 * <li>0x01-Private Warehouse</li>
	 * <li>0x02-Clan Warehouse</li>
	 * <li>0x03-Castle Warehouse</li>
	 * <li>0x04-Warehouse</li>
	 * </ul>
	 */
	private final int _whType;
	
	public WareHouseDepositList(L2PcInstance player, int type)
	{
		_whType = type;
		_playerAdena = player.getAdena();
		_itemsInWarehouse = player.getActiveWarehouse().getItems();
		
		final boolean isPrivate = _whType == PRIVATE;
		for (L2ItemInstance temp : player.getInventory().getAvailableItems(true, isPrivate, false))
		{
			if (temp == null)
			{
				continue;
			}
			if (temp.isDepositable(isPrivate))
			{
				_items.add(temp);
			}
		}
		for (L2ItemInstance temp : _itemsInWarehouse)
		{
			if (temp.isStackable())
			{
				_itemsStackable.add(temp.getDisplayId());
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x41);
		writeH(_whType);
		writeQ(_playerAdena);
		writeD(_itemsInWarehouse.length);
		writeH(_itemsStackable.size());
		
		for (int itemId : _itemsStackable)
		{
			writeD(itemId);
		}
		
		writeH(_items.size());
		
		for (L2ItemInstance item : _items)
		{
			writeItem(item);
			writeD(item.getObjectId());
		}
	}
}
