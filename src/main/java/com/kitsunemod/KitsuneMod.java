package com.kitsunemod;

import com.kitsunemod.network.ModNetwork;
import com.kitsunemod.registry.ModCreativeTabs;
import com.kitsunemod.registry.ModEntities;
import com.kitsunemod.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Kitsune Romance - Forge 1.20.1
 * Chicas kitsune de 25 años con chat (IA opcional), citas, noviazgo y harem.
 */
@Mod(KitsuneMod.MODID)
public class KitsuneMod {
    public static final String MODID = "kitsunemod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public KitsuneMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModEntities.ENTITIES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);

        modBus.addListener(this::commonSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
        LOGGER.info("Kitsune Romance cargado. ¡Las kitsunes te esperan en los bosques!");
    }
}
