package com.gmail.trentech.pjp.commands.warp;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import com.gmail.trentech.pjc.help.Help;
import com.gmail.trentech.pjp.portal.Portal;
import com.gmail.trentech.pjp.portal.PortalService;
import com.gmail.trentech.pjp.portal.Portal.PortalType;

public class CMDRename implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!args.hasAny("oldName")) {
			Help help = Help.get("warp rename").get();
			throw new CommandException(Text.builder().onClick(TextActions.executeCallback(help.execute())).append(help.getUsageText()).build(), false);
		}
		Portal portal = args.<Portal>getOne("oldName").get();

		if (!args.hasAny("newName")) {
			Help help = Help.get("warp rename").get();
			throw new CommandException(Text.builder().onClick(TextActions.executeCallback(help.execute())).append(help.getUsageText()).build(), false);
		}
		String newName = args.<String>getOne("newName").get().toLowerCase();
		
		PortalService portalService = Sponge.getServiceManager().provide(PortalService.class).get();
		
		if (portalService.get(newName, PortalType.WARP).isPresent()) {
			throw new CommandException(Text.of(TextColors.RED, newName, " already exists"), false);
		}

		portalService.remove(portal);
		portalService.create(portal, newName);

		src.sendMessage(Text.of(TextColors.DARK_GREEN, "Warp renamed to ", newName));

		return CommandResult.success();
	}

}