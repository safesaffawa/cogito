package org.PolariStarsMC.cogito.registry;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.PolariStarsMC.cogito.Cogito;

public final class CogitoCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Cogito.MOD_ID);

    public static final Supplier<CreativeModeTab> COGITO = TABS.register("cogito", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.cogito"))
            .icon(() -> new ItemStack(CogitoItems.CREATIVE_MOTOR.get()))
            .displayItems((parameters, output) -> {
                output.accept(CogitoItems.CREATIVE_MOTOR.get());
                output.accept(CogitoItems.SHAFT.get());
            })
            .build());

    private CogitoCreativeTabs() {
    }

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
