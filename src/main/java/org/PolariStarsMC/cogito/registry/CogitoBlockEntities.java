package org.PolariStarsMC.cogito.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.PolariStarsMC.cogito.Cogito;
import org.PolariStarsMC.cogito.content.block.CreativeMotorBlockEntity;
import org.PolariStarsMC.cogito.content.block.ShaftBlockEntity;

public final class CogitoBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Cogito.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShaftBlockEntity>> SHAFT =
            BLOCK_ENTITIES.register("shaft",
                    () -> new BlockEntityType<>(ShaftBlockEntity::new, CogitoBlocks.SHAFT.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeMotorBlockEntity>> CREATIVE_MOTOR =
            BLOCK_ENTITIES.register("creative_motor",
                    () -> new BlockEntityType<>(CreativeMotorBlockEntity::new, CogitoBlocks.CREATIVE_MOTOR.get()));

    private CogitoBlockEntities() {
    }

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
