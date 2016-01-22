package com.gmail.trentech.pjp.listeners;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.gmail.trentech.pjp.Main;
import com.gmail.trentech.pjp.events.TeleportEvent;
import com.gmail.trentech.pjp.portals.Door;
import com.gmail.trentech.pjp.utils.ConfigManager;
import com.gmail.trentech.pjp.utils.Utils;

public class DoorListener {

	public static ConcurrentHashMap<Player, String> builders = new ConcurrentHashMap<>();

	@Listener
	public void onChangeBlockEvent(ChangeBlockEvent.Break event, @First Player player) {
		for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
			Location<World> location = transaction.getFinal().getLocation().get();		

			if(!Door.get(location).isPresent()){
				continue;
			}
			
			if(!player.hasPermission("pjp.door.break")){
				player.sendMessage(Text.of(TextColors.DARK_RED, "you do not have permission to break door portals"));
				event.setCancelled(true);
			}else{
				Door.remove(location);
				player.sendMessage(Text.of(TextColors.DARK_GREEN, "Broke door portal"));
			}
			return;
		}
	}

	@Listener
	public void onChangeBlockEvent(ChangeBlockEvent.Place event, @First Player player) {
		if(!builders.containsKey(player)){
			return;
		}

		for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
			BlockType blockType = transaction.getFinal().getState().getType();
			
			if(!blockType.equals(BlockTypes.ACACIA_DOOR) && !blockType.equals(BlockTypes.BIRCH_DOOR) && !blockType.equals(BlockTypes.DARK_OAK_DOOR)
					 && !blockType.equals(BlockTypes.IRON_DOOR) && !blockType.equals(BlockTypes.JUNGLE_DOOR) && !blockType.equals(BlockTypes.SPRUCE_DOOR)
					 && !blockType.equals(BlockTypes.TRAPDOOR) && !blockType.equals(BlockTypes.WOODEN_DOOR)){
				continue;
			}

			Location<World> location = transaction.getFinal().getLocation().get();

			if(!player.hasPermission("pjp.door.place")){
	        	player.sendMessage(Text.of(TextColors.DARK_RED, "you do not have permission to place door portals"));
	        	builders.remove(player);
	        	event.setCancelled(true);
	        	return;
			}

            String destination = builders.get(player);
            
            Door.save(location, destination);

    		if(new ConfigManager().getConfig().getNode("options", "particles").getBoolean()){
    			Utils.spawnParticles(location, 1.0, false);
    		}

            player.sendMessage(Text.of(TextColors.DARK_GREEN, "New door portal created"));
            
            builders.remove(player);
		}
	}
	
	@Listener
	public void onDisplaceEntityEvent(DisplaceEntityEvent.TargetPlayer event){
		if (!(event.getTargetEntity() instanceof Player)){
			return;
		}
		Player player = (Player) event.getTargetEntity();

		Location<World> location = player.getLocation();		

		Optional<Door> optionalDoor = Door.get(location);
		
		if(!optionalDoor.isPresent()){
			return;
		}
		Door door = optionalDoor.get();

		if(!player.hasPermission("pjp.door.interact")){
			player.sendMessage(Text.of(TextColors.DARK_RED, "you do not have permission to interact with door portals"));
			event.setCancelled(true);
			return;
		}
		
		Optional<Location<World>> optionalSpawnLocation = door.getDestination();
		
		if(!optionalSpawnLocation.isPresent()){
			player.sendMessage(Text.of(TextColors.DARK_RED, "World does not exist"));
			return;
		}
		Location<World> spawnLocation = optionalSpawnLocation.get();

		TeleportEvent teleportEvent = new TeleportEvent(player, player.getLocation(), spawnLocation, Cause.of("door"));

		if(!Main.getGame().getEventManager().post(teleportEvent)){
			spawnLocation = teleportEvent.getDestination();
			player.setLocation(spawnLocation);
		}
	}
}
