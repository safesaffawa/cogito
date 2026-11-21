package org.PolariStarsMC.cogito.registry;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.PolariStarsMC.cogito.Cogito;
import org.PolariStarsMC.cogito.content.block.CreativeMotorBlock;
import org.PolariStarsMC.cogito.content.block.ShaftBlock;

public final class CogitoBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Cogito.MOD_ID);

    /** 传动杆：圆形，原版石头贴图。 */
    public static final DeferredBlock<ShaftBlock> SHAFT = BLOCKS.registerBlock(
            "shaft",
            ShaftBlock::new,
            props -> props
                    .mapColor(MapColor.STONE)
                    .strength(1.5F, 6.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
                    .noOcclusion());

    /** 创造马达：黑紫色纯方块，先不给工具要求，方便随手拆。 */
    public static final DeferredBlock<CreativeMotorBlock> CREATIVE_MOTOR = BLOCKS.registerBlock(
            "creative_motor",
            CreativeMotorBlock::new,
            props -> props
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.STONE));

    private CogitoBlocks() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
