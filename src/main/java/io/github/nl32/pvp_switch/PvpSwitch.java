package io.github.nl32.pvp_switch;

import com.mojang.authlib.GameProfile;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import net.minecraft.server.Whitelist;
import net.minecraft.server.WhitelistEntry;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.server.DedicatedServerModInitializer;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class PvpSwitch implements DedicatedServerModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("Pvp Switch");
	public static Whitelist whitelist;
	@Override
	public void onInitializeServer(ModContainer mod) {
		LOGGER.info("Starting Pvp Switch Initialization");
		whitelist = new Whitelist(new File("pvp_list.json"));
		loadList();
		Placeholders.register(new Identifier("pvp-switch","status"),(ctx,args) -> {
			if(ctx.hasPlayer()){
				if(whitelist.isAllowed(ctx.gameProfile())) {
					return PlaceholderResult.value(TextParserUtils.formatText("<green>On</green>"));
				}else{
					return PlaceholderResult.value(TextParserUtils.formatText("<red>Off</red>"));
				}
			}
			return PlaceholderResult.invalid("Not a Player");
		});
		Placeholders.register(new Identifier("pvp-switch","status_icon"),(ctx,args)->{
			if(ctx.hasPlayer()){
				if(whitelist.isAllowed(ctx.gameProfile())) {
					return PlaceholderResult.value(TextParserUtils.formatText("<green>\uD83D\uDDE1</green>"));
				}else{
					return PlaceholderResult.value(TextParserUtils.formatText("<red>\uD83D\uDDE1</red>"));
				}
			}
			return PlaceholderResult.invalid("Not a Player");
		});
		CommandRegistrationCallback.EVENT.register(Commands::register);
		LOGGER.info("Pvp Switch Initialization finished");
	}
	public static void loadList(){
		try {
			whitelist.load();
		} catch (Exception error) {
			LOGGER.error("Failed to load pvp list", error);
		}
	}
	public static void addPlayer(GameProfile player){
		whitelist.add(new WhitelistEntry(player));
	}
	public static void removePlayer(GameProfile player){
		whitelist.remove(player);
	}
}
