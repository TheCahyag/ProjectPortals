package com.gmail.trentech.pjp.commands;

import java.util.Optional;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.gmail.trentech.pjp.Main;
import com.gmail.trentech.pjp.listeners.LeverListener;
import com.gmail.trentech.pjp.portals.Lever;
import com.gmail.trentech.pjp.utils.ConfigManager;
import com.gmail.trentech.pjp.utils.Help;
import com.gmail.trentech.pjp.utils.Rotation;

public class CMDLever implements CommandExecutor {

	public CMDLever(){
		String alias = new ConfigManager().getConfig().getNode("settings", "commands", "lever").getString();
		
		Help help = new Help("lever", "lever", " Use this command to create a lever that will teleport you to other worlds");
		help.setSyntax(" /lever [<world>] [-c <x,y,z>] [-d <direction>] [-p <price>]\n /" + alias + " lever [<world>] [-c <x,y,z>] [-d <direction>] [-p <price>]");
		help.setExample(" /lever MyWorld\n /lever MyWorld -c -100,65,254\n /lever MyWorld -c random\n /lever MyWorld -c -100 65 254 -d south\n /lever MyWorld -d southeast\n /lever MyWorld -p 50");
		help.save();
	}
	
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if(!(src instanceof Player)){
			src.sendMessage(Text.of(TextColors.DARK_RED, "Must be a player"));
			return CommandResult.empty();
		}
		Player player = (Player) src;

		if(!args.hasAny("world")) {
			src.sendMessage(Text.of(TextColors.RED, "Usage: /lever [<world>] [-c <x,y,z>] [-d <direction>] [-p <price>]"));
			return CommandResult.empty();
		}
		String worldName = args.<String>getOne("world").get();

		if(!Main.getGame().getServer().getWorld(worldName).isPresent()){
			src.sendMessage(Text.of(TextColors.DARK_RED, worldName, " is not loaded or does not exist"));
			return CommandResult.empty();
		}

		String destination = worldName + ":spawn";
		
		if(args.hasAny("x,y,z")){
			String[] coords = args.<String>getOne("x,y,z").get().split(",");

			if(coords[0].equalsIgnoreCase("random")){
				destination = destination.replace("spawn", "random");
			}else{
				int x;
				int y;
				int z;
				
				try{
					x = Integer.parseInt(coords[0]);
					y = Integer.parseInt(coords[1]);
					z = Integer.parseInt(coords[2]);				
				}catch(Exception e){
					src.sendMessage(Text.of(TextColors.YELLOW, "Coordinates format: x,y,z"));
					return CommandResult.empty();
				}
				destination = destination.replace("spawn", x + "." + y + "." + z);
			}
		}
		
		Rotation rotation = Rotation.EAST;
		
		if(args.hasAny("direction")){
			String direction = args.<String>getOne("direction").get();
			
			Optional<Rotation> optionalRotation = Rotation.get(direction);
			
			if(!optionalRotation.isPresent()){
				src.sendMessage(Text.of(TextColors.YELLOW, "Direction examples NORTH, SOUTH, NORTHEAST, SOUTHWEST...etc"));
				return CommandResult.empty();
			}

			rotation = optionalRotation.get();
		}
		
		double price = 0;
		
		if(args.hasAny("price")){
			price = args.<Double>getOne("price").get();
		}
		
		LeverListener.builders.put(player, new Lever(destination, rotation, price));

		player.sendMessage(Text.of(TextColors.DARK_GREEN, "Place lever to create lever portal"));

		return CommandResult.success();
	}
}
