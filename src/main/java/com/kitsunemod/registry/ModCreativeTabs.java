package com.kitsunemod.registry;

import com.kitsunemod.KitsuneMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, KitsuneMod.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.kitsunemod"))
                    .icon(() -> new ItemStack(ModItems.INARI_TOFU.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.KITSUNE_SPAWN_EGG.get());
                        output.accept(ModItems.INARI_TOFU.get());
                        output.accept(ModItems.PROMISE_RING.get());
                    })
                    .build());
}
