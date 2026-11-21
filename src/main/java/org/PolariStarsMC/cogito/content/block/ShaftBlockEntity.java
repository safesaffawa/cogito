package org.PolariStarsMC.cogito.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.PolariStarsMC.cogito.content.kinetic.KineticBlockEntity;
import org.PolariStarsMC.cogito.registry.CogitoBlockEntities;

/** 传动杆的方块实体：纯传动件，把所在网络的转速传给后续机器。 */
public class ShaftBlockEntity extends KineticBlockEntity {
    public ShaftBlockEntity(BlockPos pos, BlockState state) {
        super(CogitoBlockEntities.SHAFT.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ShaftBlockEntity shaft) {
        shaft.tickServer();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ShaftBlockEntity shaft) {
        shaft.tickClient();
    }
}
