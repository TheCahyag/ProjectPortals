package com.gmail.trentech.pjp.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.gmail.trentech.pjp.Main;
import com.gmail.trentech.pjp.Resource;
import com.gmail.trentech.pjp.portals.CuboidBuilder;

public class CMDCube implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if(!(src instanceof Player)){
			src.sendMessage(Texts.of(TextColors.DARK_RED, "Must be a player"));
			return CommandResult.empty();
		}
		Player player = (Player) src;
		
		if(!args.hasAny("name")) {
			src.sendMessage(Texts.of(TextColors.DARK_GREEN, "Right click the Cuboid to remove"));
			CuboidBuilder.getActiveBuilders().put((Player) src, new CuboidBuilder());
			return CommandResult.success();
		}
		String worldName = args.<String>getOne("name").get();

		if(!Main.getGame().getServer().getWorld(worldName).isPresent()){
			src.sendMessage(Texts.of(TextColors.DARK_RED, "World ", worldName, " does not exist"));
			return CommandResult.empty();
		}
		World world = Main.getGame().getServer().getWorld(worldName).get();
		
		Location<World> location;
		
		if(!args.hasAny("coords")) {
			location = world.getSpawnLocation();
		}else{
			location = Resource.getLocation(world, args.<String>getOne("coords").get());
		}
		
		if(location == null){
			src.sendMessage(Texts.of(TextColors.YELLOW, "/portal plate <world> [x] [y] [z]"));
			return CommandResult.empty();
		}

		CuboidBuilder builder = new CuboidBuilder(location);

		CuboidBuilder.getActiveBuilders().put(player, builder);

		player.sendMessage(Texts.of(TextColors.DARK_GREEN, "Right click starting point"));

		return CommandResult.success();
	}

}