package io.github.nl32.pvp_switch;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandBuildContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.command.argument.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.command.argument.EntityArgumentType.getPlayer;

public class Commands {
	public static LiteralCommandNode<ServerCommandSource> register(CommandDispatcher<ServerCommandSource> dispatcher, CommandBuildContext buildContext, CommandManager.RegistrationEnvironment registrationEnvironment) {
		return dispatcher.register(
				literal("pvp").then(
						literal("toggle").requires(Permissions.require("pvpswitch")).executes(Commands::togglePvp)
				).then(
						literal("admin").then(
								literal("add").then(
										argument("player", player()).requires(Permissions.require("pvpswitch.admin")).executes(Commands::add)
								)
						).then(
								literal("remove").then(
										argument("player", player()).requires(Permissions.require("pvpswitch.admin")).executes(Commands::remove)
								)
						)
				).then(
						literal("list").requires(Permissions.require("pvpswitch.list")).executes(Commands::list)
				).then(
						literal("on").requires(Permissions.require("pvpswitch")).executes(Commands::enablePvp)
				).then(
						literal("off").requires(Permissions.require("pvpswitch")).executes(Commands::disablePvp)
				).then(
						literal("reload").requires(Permissions.require("pvpswitch.admin")).executes(Commands::reload)
				).requires(Permissions.require("pvpswitch")).executes(Commands::status)
		);
	}

	private static int enablePvp(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		ServerCommandSource source = ctx.getSource();
		GameProfile player = source.getPlayer().getGameProfile();
		if (!PvpSwitch.whitelist.isAllowed(player)) {
			PvpSwitch.addPlayer(player);
			source.sendFeedback(Text.of("Pvp is now enabled"), false);
			return 1;
		}
		source.sendFeedback(Text.of("You already have pvp enabled"), false);
		return 1;
	}

	private static int disablePvp(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		ServerCommandSource source = ctx.getSource();
		GameProfile player = source.getPlayer().getGameProfile();
		if (PvpSwitch.whitelist.isAllowed(player)) {
			PvpSwitch.removePlayer(player);
			source.sendFeedback(Text.of("Pvp is now disabled"), false);
			return 1;
		}
		source.sendFeedback(Text.of("You already have pvp disabled"), false);
		return 1;
	}

	private static int togglePvp(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		ServerCommandSource source = ctx.getSource();
		GameProfile player = source.getPlayer().getGameProfile();
		if (PvpSwitch.whitelist.isAllowed(player)) {
			return disablePvp(ctx);
		} else {
			return enablePvp(ctx);
		}
	}

	private static int status(CommandContext<ServerCommandSource> ctx)  {
		ServerCommandSource source = ctx.getSource();
		source.sendFeedback(Placeholders.parseText(Text.of("pvp is %pvp-switch:status%."), PlaceholderContext.of(source)), false);
		return 1;
	}

	private static int add(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		ServerCommandSource source = ctx.getSource();
		GameProfile player = getPlayer(ctx, "player").getGameProfile();
		if (PvpSwitch.whitelist.isAllowed(player)) {
			source.sendFeedback(Text.of(String.format("%s is already in list", player.getName())), false);
		} else {
			PvpSwitch.addPlayer(player);
			source.sendFeedback(Text.of(String.format("%s now has pvp enabled", player.getName())), false);
			getPlayer(ctx, "player").sendMessage(Text.of("You now have PVP enabled."), false);
		}
		return 1;
	}

	private static int remove(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		ServerCommandSource source = ctx.getSource();
		GameProfile player = getPlayer(ctx, "player").getGameProfile();
		if (!PvpSwitch.whitelist.isAllowed(player)) {
			source.sendFeedback(Text.of(String.format("%s already has pvp disabled", player.getName())), false);
		} else {
			PvpSwitch.removePlayer(player);
			source.sendFeedback(Text.of(String.format("%s now has pvp disabled", player.getName())), false);
			getPlayer(ctx, "player").sendMessage(Text.of("You now have PVP disabled."), false);
		}
		return 1;
	}

	private static int reload(CommandContext<ServerCommandSource> ctx) {
		ServerCommandSource source = ctx.getSource();
		PvpSwitch.loadList();
		source.sendFeedback(Text.of("tried to reload"), false);
		return 1;
	}
	private static int list(CommandContext<ServerCommandSource> ctx) {
		ServerCommandSource source = ctx.getSource();
		source.sendFeedback(Text.of("Players with PVP on: " + String.join(", ",PvpSwitch.whitelist.getNames())),false);
		return 1;
	}
}
