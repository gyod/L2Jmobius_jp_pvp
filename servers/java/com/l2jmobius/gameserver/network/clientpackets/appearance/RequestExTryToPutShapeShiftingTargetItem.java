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
package com.l2jmobius.gameserver.network.clientpackets.appearance;

import com.l2jmobius.gameserver.data.xml.impl.AppearanceItemData;
import com.l2jmobius.gameserver.enums.ItemLocation;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.request.ShapeShiftingItemRequest;
import com.l2jmobius.gameserver.model.itemcontainer.PcInventory;
import com.l2jmobius.gameserver.model.items.L2Item;
import com.l2jmobius.gameserver.model.items.appearance.AppearanceStone;
import com.l2jmobius.gameserver.model.items.appearance.AppearanceTargetType;
import com.l2jmobius.gameserver.model.items.appearance.AppearanceType;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.items.type.ArmorType;
import com.l2jmobius.gameserver.model.items.type.WeaponType;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket;
import com.l2jmobius.gameserver.network.serverpackets.appearance.ExPutShapeShiftingTargetItemResult;

/**
 * @author UnAfraid
 */
public class RequestExTryToPutShapeShiftingTargetItem extends L2GameClientPacket
{
	private int _targetItemObjId;
	
	@Override
	protected void readImpl()
	{
		_targetItemObjId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		final ShapeShiftingItemRequest request = player.getRequest(ShapeShiftingItemRequest.class);
		
		if (player.isInStoreMode() || player.isInCraftMode() || player.isProcessingRequest() || player.isProcessingTransaction() || (request == null))
		{
			player.sendPacket(ExPutShapeShiftingTargetItemResult.FAILED);
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THIS_SYSTEM_DURING_TRADING_PRIVATE_STORE_AND_WORKSHOP_SETUP);
			return;
		}
		
		final PcInventory inventory = player.getInventory();
		final L2ItemInstance targetItem = inventory.getItemByObjectId(_targetItemObjId);
		L2ItemInstance stone = request.getAppearanceStone();
		if ((targetItem == null) || (stone == null) || !targetItem.isAppearanceable() || ((targetItem.getItemLocation() != ItemLocation.INVENTORY) && (targetItem.getItemLocation() != ItemLocation.PAPERDOLL)) || ((stone = inventory.getItemByObjectId(stone.getObjectId())) == null))
		{
			player.sendPacket(ExPutShapeShiftingTargetItemResult.FAILED);
			player.removeRequest(ShapeShiftingItemRequest.class);
			return;
		}
		final AppearanceStone appearanceStone = AppearanceItemData.getInstance().getStone(stone.getId());
		if ((appearanceStone == null) || ((appearanceStone.getType() != AppearanceType.RESTORE) && (targetItem.getVisualId() > 0)) || ((appearanceStone.getType() == AppearanceType.RESTORE) && (targetItem.getVisualId() == 0)))
		{
			player.sendPacket(ExPutShapeShiftingTargetItemResult.FAILED);
			player.removeRequest(ShapeShiftingItemRequest.class);
			return;
		}
		
		if (!appearanceStone.getCrystalTypes().isEmpty() && !appearanceStone.getCrystalTypes().contains(targetItem.getItem().getCrystalType()))
		{
			player.sendPacket(SystemMessageId.ITEM_GRADES_DO_NOT_MATCH);
			player.sendPacket(ExPutShapeShiftingTargetItemResult.FAILED);
			player.removeRequest(ShapeShiftingItemRequest.class);
			return;
		}
		
		if (appearanceStone.getTargetTypes().isEmpty())
		{
			player.sendPacket(ExPutShapeShiftingTargetItemResult.FAILED);
			player.removeRequest(ShapeShiftingItemRequest.class);
			return;
		}
		if (!appearanceStone.getTargetTypes().contains(AppearanceTargetType.ALL) && ((targetItem.isWeapon() && !appearanceStone.getTargetTypes().contains(AppearanceTargetType.WEAPON)) || (targetItem.isArmor() && !appearanceStone.getTargetTypes().contains(AppearanceTargetType.ARMOR) && !appearanceStone.getTargetTypes().contains(AppearanceTargetType.ACCESSORY)) || (targetItem.isArmor() && !appearanceStone.getBodyParts().isEmpty() && !appearanceStone.getBodyParts().contains(targetItem.getItem().getBodyPart()))))
		{
			player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
			player.sendPacket(ExPutShapeShiftingTargetItemResult.FAILED);
			player.removeRequest(ShapeShiftingItemRequest.class);
			return;
		}
		
		if (appearanceStone.getWeaponType() != WeaponType.NONE)
		{
			if (!targetItem.isWeapon() || (targetItem.getItemType() != appearanceStone.getWeaponType()))
			{
				player.sendPacket(ExPutShapeShiftingTargetItemResult.FAILED);
				player.removeRequest(ShapeShiftingItemRequest.class);
				player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
				return;
			}
			
			switch (appearanceStone.getHandType())
			{
				case ONE_HANDED:
				{
					if ((targetItem.getItem().getBodyPart() & L2Item.SLOT_R_HAND) != L2Item.SLOT_R_HAND)
					{
						player.sendPacket(ExPutShapeShiftingTargetItemResult.FAILED);
						player.removeRequest(ShapeShiftingItemRequest.class);
						player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
						return;
					}
					break;
				}
				case TWO_HANDED:
				{
					if ((targetItem.getItem().getBodyPart() & L2Item.SLOT_LR_HAND) != L2Item.SLOT_LR_HAND)
					{
						player.sendPacket(ExPutShapeShiftingTargetItemResult.FAILED);
						player.removeRequest(ShapeShiftingItemRequest.class);
						player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
						return;
					}
					break;
				}
			}
			
			switch (appearanceStone.getMagicType())
			{
				case MAGICAL:
				{
					if (!targetItem.getItem().isMagicWeapon())
					{
						player.sendPacket(ExPutShapeShiftingTargetItemResult.FAILED);
						player.removeRequest(ShapeShiftingItemRequest.class);
						player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
						return;
					}
					break;
				}
				case PHYISICAL:
				{
					if (targetItem.getItem().isMagicWeapon())
					{
						player.sendPacket(ExPutShapeShiftingTargetItemResult.FAILED);
						player.removeRequest(ShapeShiftingItemRequest.class);
						player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
						return;
					}
				}
			}
		}
		
		if (appearanceStone.getArmorType() != ArmorType.NONE)
		{
			switch (appearanceStone.getArmorType())
			{
				case SHIELD:
				{
					if (!targetItem.isArmor() || (targetItem.getItemType() != ArmorType.SHIELD))
					{
						player.sendPacket(ExPutShapeShiftingTargetItemResult.FAILED);
						player.removeRequest(ShapeShiftingItemRequest.class);
						player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
						return;
					}
					break;
				}
				case SIGIL:
				{
					if (!targetItem.isArmor() || (targetItem.getItemType() != ArmorType.SIGIL))
					{
						player.sendPacket(ExPutShapeShiftingTargetItemResult.FAILED);
						player.removeRequest(ShapeShiftingItemRequest.class);
						player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
						return;
					}
				}
			}
		}
		
		if (targetItem.getOwnerId() != player.getObjectId())
		{
			player.sendPacket(ExPutShapeShiftingTargetItemResult.FAILED);
			player.removeRequest(ShapeShiftingItemRequest.class);
			return;
		}
		
		player.sendPacket(new ExPutShapeShiftingTargetItemResult(ExPutShapeShiftingTargetItemResult.RESULT_SUCCESS, appearanceStone.getCost()));
	}
	
	@Override
	public String getType()
	{
		return getClass().getSimpleName();
	}
}
