package com.bergerkiller.bukkit.common.entity;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.inventory.ItemStack;

import com.bergerkiller.bukkit.common.entity.nms.NMSEntity;

public class CommonMinecartTNT extends CommonMinecart<ExplosiveMinecart> {

	public CommonMinecartTNT(ExplosiveMinecart base) {
		super(base);
	}

	@Override
	protected Class<? extends NMSEntity> getNMSType() {
		return null;
	}

	@Override
	public List<ItemStack> getBrokenDrops() {
		return Arrays.asList(new ItemStack(Material.MINECART, 1), new ItemStack(Material.TNT, 1));
	}

	@Override
	public Material getCombinedItem() {
		return Material.EXPLOSIVE_MINECART;
	}
}
