package io.github.nl32.pvp_switch.mixin;

import com.mojang.authlib.GameProfile;
import io.github.nl32.pvp_switch.PvpSwitch;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.server.Whitelist;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity{
	public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile, @Nullable PlayerPublicKey publicKey) {
		super(world, pos, yaw, gameProfile, publicKey);
	}
	@Inject(method = "shouldDamagePlayer", at = @At("HEAD"), cancellable = true)
	public void checkPvpList(PlayerEntity targetPlayer, CallbackInfoReturnable<Boolean> cir){
		Whitelist pvpList = PvpSwitch.whitelist;
		if(!pvpList.isAllowed(this.getGameProfile()) || !pvpList.isAllowed(targetPlayer.getGameProfile())){
			cir.setReturnValue(false);
		}
	}


}
