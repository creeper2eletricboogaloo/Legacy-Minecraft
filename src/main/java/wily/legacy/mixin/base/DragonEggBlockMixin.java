package wily.legacy.mixin.base;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DragonEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonEggBlock.class)
public class DragonEggBlockMixin {
    @Inject(method = "teleport", at = @At("HEAD"), cancellable = true)
    private void teleport(BlockState state, Level level, BlockPos pos, CallbackInfo ci) {
        if (level.isClientSide()) {
            ci.cancel();
            return;
        }
        WorldBorder border = level.getWorldBorder();
        ServerLevel serverLevel = (ServerLevel) level;
        for (int i = 0; i < 1000; i++) {
            BlockPos target = pos.offset(level.random.nextInt(16) - level.random.nextInt(16), level.random.nextInt(8) - level.random.nextInt(8), level.random.nextInt(16) - level.random.nextInt(16));
            if (!level.getBlockState(target).isAir() || !border.isWithinBounds(target) || level.isOutsideBuildHeight(target)) continue;
            double startX = pos.getX() + 0.5;
            double startY = pos.getY() + 0.5;
            double startZ = pos.getZ() + 0.5;
            double endX = target.getX() + 0.5;
            double endY = target.getY() + 0.5;
            double endZ = target.getZ() + 0.5;
            for (ServerPlayer player : serverLevel.players()) {
                for (int j = 0; j < 128; j++) {
                    double progress = level.random.nextDouble();
                    double x = Mth.lerp(progress, startX, endX) + (level.random.nextDouble() - 0.5) * 0.25;
                    double y = Mth.lerp(progress, startY, endY) + (level.random.nextDouble() - 0.5) * 0.25;
                    double z = Mth.lerp(progress, startZ, endZ) + (level.random.nextDouble() - 0.5) * 0.25;
                    serverLevel.sendParticles(player, ParticleTypes.REVERSE_PORTAL, false, false, startX, startY, startZ, 0, (x - startX) * 0.03, (y - startY) * 0.03, (z - startZ) * 0.03, 1);
                }
            }
            level.setBlock(target, state, 2);
            level.removeBlock(pos, false);
            ci.cancel();
            return;
        }
        ci.cancel();
    }
}
