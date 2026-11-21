package org.PolariStarsMC.cogito.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.PolariStarsMC.cogito.Cogito;
import org.PolariStarsMC.cogito.registry.CogitoBlockEntities;

/** 客户端注册：目前只有传动杆的旋转渲染器。 */
@EventBusSubscriber(modid = Cogito.MOD_ID, value = Dist.CLIENT)
public final class CogitoClient {
    private CogitoClient() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CogitoBlockEntities.SHAFT.get(), ShaftRenderer::new);
    }
}
