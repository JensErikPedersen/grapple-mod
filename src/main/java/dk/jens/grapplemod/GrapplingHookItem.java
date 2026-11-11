package dk.jens.grapplemod;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class GrapplingHookItem extends Item {
	public GrapplingHookItem(Properties properties) {
		super(properties);
	}

	// Kaldes når spilleren højreklikker med gribekrogen i hånden
	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		// Selve krogen styres af serveren. På klienten (spillerens egen PC-del af spillet)
		// svinger vi bare armen.
		if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
			GrappleManager.use(serverPlayer, serverLevel);
		}
		return InteractionResult.SUCCESS;
	}
}
