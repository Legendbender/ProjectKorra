package com.projectkorra.ProjectKorra;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.kitteh.tag.AsyncPlayerReceiveNameTagEvent;

import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.airbending.AirBlast;
import com.projectkorra.ProjectKorra.airbending.AirBurst;
import com.projectkorra.ProjectKorra.airbending.Tornado;
import com.projectkorra.ProjectKorra.chiblocking.ChiPassive;
import com.projectkorra.ProjectKorra.earthbending.EarthPassive;
import com.projectkorra.ProjectKorra.firebending.Enflamed;
import com.projectkorra.ProjectKorra.firebending.FireStream;
import com.projectkorra.ProjectKorra.waterbending.WaterCore;
import com.projectkorra.ProjectKorra.waterbending.WaterPassive;

public class PKListener implements Listener {

	ProjectKorra plugin;

	public PKListener(ProjectKorra plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockFlowTo(BlockFromToEvent event) {
		Block toblock = event.getToBlock();
		Block fromblock = event.getBlock();
		if (Methods.isWater(fromblock)) {
			if (!event.isCancelled()) {
				if (Methods.isAdjacentToFrozenBlock(toblock) || Methods.isAdjacentToFrozenBlock(fromblock)) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Methods.createBendingPlayer(e.getPlayer().getUniqueId(), e.getPlayer().getName());
		Player player = e.getPlayer();
		List<Element> elements = Methods.getBendingPlayer(e.getPlayer().getName()).getElements();
		if (plugin.getConfig().getBoolean("Properties.Chat.ChatPrefixes")) {
			if (elements.size() > 1)
				player.setDisplayName(plugin.getConfig().getString("Properties.Chat.AvatarPrefix") + player.getName());
			else if (elements.get(0).equals(Element.Earth))
				player.setDisplayName(plugin.getConfig().getString("Properties.Chat.EarthPrefix") + player.getName());
			else if (elements.get(0).equals(Element.Air))
				player.setDisplayName(plugin.getConfig().getString("Properties.Chat.AirPrefix") + player.getName());
			else if (elements.get(0).equals(Element.Water))
				player.setDisplayName(plugin.getConfig().getString("Properties.Chat.WaterPrefix") + player.getName());
			else if (elements.get(0).equals(Element.Fire))
				player.setDisplayName(plugin.getConfig().getString("Properties.Chat.FirePrefix") + player.getName());
			else if (elements.get(0).equals(Element.Chi))
				player.setDisplayName(plugin.getConfig().getString("Properties.Chat.ChiPrefix") + player.getName());
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Methods.saveBendingPlayer(e.getPlayer().getName());
		BendingPlayer.players.remove(e.getPlayer().getName());
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerSneak(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();
		
		if (Paralyze.isParaylzed(player) || Bloodbending.isBloodbended(player)) {
			event.setCancelled(true);
		}
		
		AirScooter.check(player);
		
		String abil = Methods.getBoundAbility(player);
		if (abil == null) {
			return;
		}
		
		if (!player.isSneaking() && Methods.canBend(player.getName(), abil)) {
			if (Methods.isAirAbility(abil)) {
				if (Methods.isWeapon(player.getItemInHand().getType()) && !plugin.getConfig().getBoolean("Properties.Air.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("Tornado")) {
					new Tornado(player);
				}
				if (abil.equalsIgnoreCase("AirBlast")) {
					AirBlast.setOrigin(player);
				}
				if (abil.equalsIgnoreCase("AirBurst")) {
					new AirBurst(player);
				}
			}
		}
	}
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true) 
	public void onPlayerSwing(PlayerAnimationEvent event) {
		Player player = event.getPlayer();
		
		if (Bloodbending.isBloodbended(player) || Paralyze.isParalyzed(player)) {
			event.setCancelled(true);
		}
		
		String abil = Methods.getBoundAbility(player);
		if (abil == null) return;
		if (Methods.canBend(player.getName(), abil)) {
			if (abil.equalsIgnoreCase("AvatarState")) {
				new AvatarState(player);
			}
			if (Methods.isAirAbility(abil)) {
				if (Methods.isWeapon(player.getItemInHand().getType()) && !plugin.getConfig().getBoolean("Properties.Air.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("AirBlast")) {
					new AirBlast(player);
				}
				if (abil.equalsIgnoreCase("AirBurst")) {
					AirBurst.coneBurst(player);
				}
			}
		}
	}
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true) 
	public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
		Player p = event.getPlayer();
		if (Tornado.getPlayers().contains(p) || Bloodbending.isBloodbended(p)
				|| FireJet.getPlayers().contains(p)
				|| AvatarState.getPlayers().contains(p)) {
			event.setCancelled(p.getGameMode() != GameMode.CREATIVE);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityCombust(EntityCombustEvent event) {
		Entity entity = event.getEntity();
		Block block = entity.getLocation().getBlock();
		if (FireStream.ignitedblocks.containsKey(block) && entity instanceof LivingEntity) {
			new Enflamed(entity, FireStream.ignitedblocks.get(block));
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityDamageEvent(EntityDamageEvent event) {
		Entity entity = event.getEntity();

		if (event.getCause() == DamageCause.FIRE && FireStream.ignitedblocks.containsKey(entity.getLocation().getBlock())) {
			new Enflamed(entity, FireStream.ignitedblocks.get(entity.getLocation().getBlock()));
		}

		if (Enflamed.isEnflamed(entity) && event.getCause() == DamageCause.FIRE_TICK) {
			event.setCancelled(true);
			Enflamed.dealFlameDamage(entity);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockMeltEvent(BlockFadeEvent event) {
		Block block = event.getBlock();
		if (block.getType() == Material.FIRE) {
			return;
		}
		if (FireStream.ignitedblocks.containsKey(block)) {
			FireStream.remove(block);
		}
	}
	@EventHandler
	public void onPlayerDamageByPlayer(EntityDamageByEntityEvent e) {
		Entity en = e.getEntity();
		if (en instanceof Player) {
			Player p = (Player) en; // This is the player getting hurt.
			if (e.getDamager() instanceof Player) { // This is the player hitting someone.
				Player damager = (Player) e.getDamager();
				if (Methods.canBendPassive(damager.getName(), Element.Chi)) {
					if (e.getCause() == DamageCause.ENTITY_ATTACK) {
						if (damager.getItemInHand() != null && Methods.isWeapon(damager.getItemInHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.Chi.CanBendWithWeapons")) {
							// Above method checks if the player has an item in their hand, if it is a weapon, and if they can bend with weapons.
							if (Methods.getBoundAbility(damager) == null) { // We don't want them to be able to block chi if an ability is bound.
								if (ChiPassive.willChiBlock(p)) {
									ChiPassive.blockChi(p);
								}
							}
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			
			if (Methods.isBender(player.getName(), Element.Earth) && event.getCause() == DamageCause.FALL) {
				Shockwave.fallShockwave(player);
			}
			
			if (Methods.isBender(player.getName(), Element.Air) && event.getCause() == DamageCause.FALL && Methods.canBendPassive(player.getName(), Element.Air)) {
				new Flight(player);
				player.setAllowFlight(true);
				AirBurst.fallBurst(player);
				player.setFallDistance(0);
				event.setDamage(0);
				event.setCancelled(true);
			}
			
			if (!event.isCancelled() && Methods.isBender(player.getName(), Element.Water) && event.getCause() == DamageCause.FALL && Methods.canBendPassive(player.getName(), Element.Water)) {
				if (WaterPassive.applyNoFall(player)) {
					new Flight(player);
					player.setAllowFlight(true);
					player.setFallDistance(0);
					event.setDamage(0);
					event.setCancelled(true);
				}
			}
			
			if (!event.isCancelled()
					&& Methods.isBender(player.getName(), Element.Earth)
					&& event.getCause() == DamageCause.FALL
					&& Methods.canBendPassive(player.getName(), Element.Earth)) {
				if (EarthPassive.softenLanding(player)) {
					new Flight(player);
					player.setAllowFlight(true);
					player.setFallDistance(0);
					event.setDamage(0);
					event.setCancelled(true);
				}
			}
			
			if (!event.isCancelled()
					&& Methods.isBender(player.getName(), Element.Chi)
					&& event.getCause() == DamageCause.FALL
					&& Methods.canBendPassive(player.getName(), Element.Chi)) {
				double initdamage = event.getDamage();
				double newdamage = event.getDamage() * ChiPassive.FallReductionFactor;
				double finaldamage = initdamage - newdamage;
				event.setDamage(finaldamage);
			}
			
			if (!event.isCancelled() && event.getCause() == DamageCause.FALL) {
				Player source = Flight.getLaunchedBy(player);
				if (source != null) {
					event.setCancelled(true);
					Methods.damageEntity(source, player, event.getDamage());
				}
			}
			
			if (Methods.canBendPassive(player.getName(), Element.Fire)
					&& Methods.isBender(player.getName(),  Element.Fire)
					&& (event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FIRE_TICK)) {
				event.setCancelled(!Extinguish.canBurn(player));
			}
			
			if (Methods.isBender(player.getName(), Element.Earth)
					&& event.getCause() == DamageCause.SUFFOCATION && TempBlock.isTempBlock(player.getEyeLocation().getBlock())) {
						event.setDamage(0);
						event.setCancelled(true);
					}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPhysics(BlockPhysicsEvent event) {
		Block block = event.getBlock();
		if (TempBlock.isTempBlock(block) || TempBlock.isTouchingTempBlock(block)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockForm(BlockFormEvent event) {
		if (!WaterCore.canPhysicsChange(event.getBlock()))
			event.setCancelled(true);
	}

	public void onNameTag(AsyncPlayerReceiveNameTagEvent e) {
		List<Element> elements = Methods.getBendingPlayer(e.getNamedPlayer().getName()).getElements();
		if (elements.size() > 1)
			e.setTag(ChatColor.LIGHT_PURPLE + e.getNamedPlayer().getName());
		else if (elements.get(0).equals(Element.Earth))
			e.setTag(ChatColor.GREEN + e.getNamedPlayer().getName());
		else if (elements.get(0).equals(Element.Air))
			e.setTag(ChatColor.GRAY + e.getNamedPlayer().getName());
		else if (elements.get(0).equals(Element.Water))
			e.setTag(ChatColor.AQUA + e.getNamedPlayer().getName());
		else if (elements.get(0).equals(Element.Fire))
			e.setTag(ChatColor.RED + e.getNamedPlayer().getName());
		else if (elements.get(0).equals(Element.Chi))
			e.setTag(ChatColor.GOLD + e.getNamedPlayer().getName());
	}
}
