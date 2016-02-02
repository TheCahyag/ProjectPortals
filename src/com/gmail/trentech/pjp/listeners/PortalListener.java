package com.gmail.trentech.pjp.listeners;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.gmail.trentech.pjp.Main;
import com.gmail.trentech.pjp.events.ConstructPortalEvent;
import com.gmail.trentech.pjp.events.TeleportEvent;
import com.gmail.trentech.pjp.portals.Portal;
import com.gmail.trentech.pjp.portals.builders.Builder;
import com.gmail.trentech.pjp.portals.builders.PortalBuilder;
import com.gmail.trentech.pjp.utils.ConfigManager;

import ninja.leaping.configurate.ConfigurationNode;

public class PortalListener {

	public static ConcurrentHashMap<Player, Builder> builders = new ConcurrentHashMap<>();

	@Listener
	public void onConstructPortalEvent(ConstructPortalEvent event, @First Player player){
		for(Location<World> location : event.getLocations()){
			if(Portal.get(location).isPresent()){
	        	player.sendMessage(Text.of(TextColors.DARK_RED, "Portals cannot over lap other portals"));
	        	event.setCancelled(true);
	        	return;
			}
		}

        List<Location<World>> locations = event.getLocations();
        
        ConfigurationNode config = new ConfigManager().getConfig();
        
        int size = config.getNode("options", "portal_size").getInt();
        if(locations.size() > size){
        	player.sendMessage(Text.of(TextColors.DARK_RED, "Portals cannot be larger than ", size, " blocks"));
        	event.setCancelled(true);
        	return;
        }
        
        if(locations.size() < 5){
        	player.sendMessage(Text.of(TextColors.DARK_RED, "Portal too small"));
        	event.setCancelled(true);        	
        	return;
        }
	}

	@Listener
	public void onChangeBlockEvent(ChangeBlockEvent.Place event, @First Player player) {
		if(!builders.containsKey(player)){
			for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
				Location<World> location = transaction.getFinal().getLocation().get();		

				if(!Portal.get(location).isPresent()){
					continue;
				}

				event.setCancelled(true);
				break;
			}
			return;
		}
		PortalBuilder builder = (PortalBuilder) builders.get(player);
		
		for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
			if(transaction.getFinal().getState().getType().equals(BlockTypes.FIRE)){
				event.setCancelled(true);
				break;
			}
			
			Location<World> location = transaction.getFinal().getLocation().get();
			
			if(builder.isFill()){
				builder.addFill(location);
			}else{
				builder.addFrame(location);
			}
		}
	}
	
	@Listener
	public void onChangeBlockEvent(ChangeBlockEvent.Break event, @First Player player) {
		if(!builders.containsKey(player)){
			for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
				Location<World> location = transaction.getFinal().getLocation().get();		

				if(!Portal.get(location).isPresent()){
					continue;
				}

				event.setCancelled(true);
				break;
			}
			return;
		}
		PortalBuilder builder = (PortalBuilder) builders.get(player);
		
		for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
			Location<World> location = transaction.getFinal().getLocation().get();
			if(builder.isFill()){
				builder.removeFill(location);
			}else{
				builder.removeFrame(location);
			}
		}
	}

	@Listener
	public void onDisplaceEntityEvent(DisplaceEntityEvent.TargetPlayer event){
		if (!(event.getTargetEntity() instanceof Player)){
			return;
		}
		Player player = (Player) event.getTargetEntity();

		Location<World> location = player.getLocation();		

		Optional<Portal> optionalPortal = Portal.get(location);
		
		if(!optionalPortal.isPresent()){
			return;
		}
		Portal portal = optionalPortal.get();

		if(!player.hasPermission("pjp.portal.interact")){
			player.sendMessage(Text.of(TextColors.DARK_RED, "You do not have permission to use portals"));
			event.setCancelled(true);
			return;
		}
		
		Optional<Location<World>> optionalSpawnLocation = portal.getDestination();
		
		if(!optionalSpawnLocation.isPresent()){
			player.sendMessage(Text.of(TextColors.DARK_RED, portal.destination.split(":")[0], " does not exist"));
			return;
		}
		Location<World> spawnLocation = optionalSpawnLocation.get();

		TeleportEvent teleportEvent = new TeleportEvent(player, player.getLocation(), spawnLocation, Cause.of("portal"));

		if(!Main.getGame().getEventManager().post(teleportEvent)){
			spawnLocation = teleportEvent.getDestination();
			
			Optional<Vector3d> optionalRotation = portal.getRotation();
			
			if(optionalRotation.isPresent()){
				player.setLocationAndRotation(spawnLocation, optionalRotation.get());
			}else{
				player.setLocation(spawnLocation);
			}
		}
	}
	
    @Listener
    public void onDamageEntityEvent(DamageEntityEvent event) {
    	if(!(event.getTargetEntity() instanceof Player)) {
    		return;
    	}
    	Player player = (Player) event.getTargetEntity();

		if(!builders.containsKey(player)){
			return;
		}

		if(!event.getCause().first(DamageSource.class).isPresent()){
			return;
		}
		DamageSource damageSource = event.getCause().first(DamageSource.class).get();

		if(!damageSource.getType().equals(DamageTypes.PROJECTILE)){
			return;
		}
		event.setCancelled(true);
	}
    
}
