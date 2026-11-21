package org.PolariStarsMC.cogito;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.PolariStarsMC.cogito.registry.CogitoBlockEntities;
import org.PolariStarsMC.cogito.registry.CogitoBlocks;
import org.PolariStarsMC.cogito.registry.CogitoCreativeTabs;
import org.PolariStarsMC.cogito.registry.CogitoItems;

@Mod(Cogito.MOD_ID)
public final class Cogito {
    public static final String MOD_ID = "cogito";

    public Cogito(IEventBus modBus, ModContainer modContainer) {
        CogitoBlocks.register(modBus);
        CogitoItems.register(modBus);
        CogitoBlockEntities.register(modBus);
        CogitoCreativeTabs.register(modBus);
    }
}
