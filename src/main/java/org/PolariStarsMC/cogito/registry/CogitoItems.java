package org.PolariStarsMC.cogito.registry;

import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.PolariStarsMC.cogito.Cogito;

public final class CogitoItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Cogito.MOD_ID);

    public static final DeferredItem<BlockItem> SHAFT = ITEMS.registerSimpleBlockItem(CogitoBlocks.SHAFT);
    public static final DeferredItem<BlockItem> CREATIVE_MOTOR = ITEMS.registerSimpleBlockItem(CogitoBlocks.CREATIVE_MOTOR);

    private CogitoItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
