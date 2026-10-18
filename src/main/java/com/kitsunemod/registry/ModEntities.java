package com.kitsunemod.registry;

import com.kitsunemod.KitsuneMod;
import com.kitsunemod.entity.KitsuneEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, KitsuneMod.MODID);

    public static final RegistryObject<EntityType<KitsuneEntity>> KITSUNE = ENTITIES.register("kitsune",
            () -> EntityType.Builder.of(KitsuneEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.85F)
                    .clientTrackingRange(10)
                    .build("kitsune"));
}
