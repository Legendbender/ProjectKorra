package com.projectkorra.projectkorra.waterbending.ice;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.util.TempBlock;

public class IceSpikePillarField extends IceAbility {

	private double damage;
	private double radius;
	private int numberOfSpikes;
	private long cooldown;
	private Vector thrownForce;

	public IceSpikePillarField(final Player player) {
		super(player);

		if (this.bPlayer.isOnCooldown("IceSpikePillarField")) {
			return;
		}

		this.damage = getConfig().getDouble("Abilities.Water.IceSpike.Field.Damage");
		this.radius = getConfig().getDouble("Abilities.Water.IceSpike.Field.Radius");
		this.cooldown = getConfig().getLong("Abilities.Water.IceSpike.Field.Cooldown");
		this.thrownForce = new Vector(0, getConfig().getDouble("Abilities.Water.IceSpike.Field.Push"), 0);

		if (this.bPlayer.isAvatarState()) {
			this.damage = getConfig().getDouble("Abilities.Avatar.AvatarState.Water.IceSpike.Field.Damage");
			this.radius = getConfig().getDouble("Abilities.Avatar.AvatarState.Water.IceSpike.Field.Radius");
			this.thrownForce = new Vector(0, getConfig().getDouble("Abilities.Avatar.AvatarState.Water.IceSpike.Field.Push"), 0);
		}

		this.numberOfSpikes = (int) (((this.radius * 2) * (this.radius * 2)) / 16);

		final Random random = new Random();
		final int locX = player.getLocation().getBlockX();
		final int locY = player.getLocation().getBlockY();
		final int locZ = player.getLocation().getBlockZ();
		final List<Block> iceBlocks = new ArrayList<Block>();

		for (int x = (int) -(this.radius - 1); x <= (this.radius - 1); x++) {
			for (int z = (int) -(this.radius - 1); z <= (this.radius - 1); z++) {
				for (int y = -1; y <= 1; y++) {
					final Block testBlock = player.getWorld().getBlockAt(locX + x, locY + y, locZ + z);

					if (WaterAbility.isIcebendable(player, testBlock.getType(), false) && testBlock.getRelative(BlockFace.UP).getType() == Material.AIR && !(testBlock.getX() == player.getEyeLocation().getBlock().getX() && testBlock.getZ() == player.getEyeLocation().getBlock().getZ()) || (TempBlock.isTempBlock(testBlock) && WaterAbility.isBendableWaterTempBlock(testBlock))) {
						iceBlocks.add(testBlock);
						for (int i = 0; i < iceBlocks.size() / 2 + 1; i++) {
							final Random rand = new Random();
							if (rand.nextInt(5) == 0) {
								playIcebendingSound(iceBlocks.get(i).getLocation());
							}
						}
					}
				}
			}
		}

		final List<Entity> entities = GeneralMethods.getEntitiesAroundPoint(player.getLocation(), this.radius);
		for (int i = 0; i < this.numberOfSpikes; i++) {
			if (iceBlocks.isEmpty()) {
				return;
			}

			Entity target = null;
			Block targetBlock = null;
			for (final Entity entity : entities) {
				if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId()) {
					for (final Block block : iceBlocks) {
						if (block.getX() == entity.getLocation().getBlockX() && block.getZ() == entity.getLocation().getBlockZ()) {
							target = entity;
							targetBlock = block;
							break;
						}
					}
				} else {
					continue;
				}
			}

			if (target != null) {
				entities.remove(target);
			} else {
				targetBlock = iceBlocks.get(random.nextInt(iceBlocks.size()));
			}

			if (targetBlock.getRelative(BlockFace.UP).getType() != Material.ICE) {

				final IceSpikePillar pillar = new IceSpikePillar(player, targetBlock.getLocation(), (int) this.damage, this.thrownForce, this.cooldown);
				pillar.inField = true;
				this.bPlayer.addCooldown("IceSpikePillarField", this.cooldown);
				iceBlocks.remove(targetBlock);
			}
		}
	}

	@Override
	public String getName() {
		return "IceSpike";
	}

	@Override
	public void progress() {
	}

	@Override
	public Location getLocation() {
		return this.player != null ? this.player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public double getDamage() {
		return this.damage;
	}

	public void setDamage(final double damage) {
		this.damage = damage;
	}

	public double getRadius() {
		return this.radius;
	}

	public void setRadius(final double radius) {
		this.radius = radius;
	}

	public int getNumberOfSpikes() {
		return this.numberOfSpikes;
	}

	public void setNumberOfSpikes(final int numberOfSpikes) {
		this.numberOfSpikes = numberOfSpikes;
	}

	public Vector getThrownForce() {
		return this.thrownForce;
	}

	public void setThrownForce(final Vector thrownForce) {
		this.thrownForce = thrownForce;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

}
