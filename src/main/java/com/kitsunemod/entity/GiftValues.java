package com.kitsunemod.entity;

import com.kitsunemod.registry.ModItems;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Cuánto afecto da cada regalo. 0 = no es un regalo (abre el chat). */
public final class GiftValues {
    private GiftValues() {}

    public static int value(ItemStack s) {
        if (s.isEmpty()) return 0;
        if (s.is(ModItems.INARI_TOFU.get())) return 12;
        if (s.is(Items.ENCHANTED_GOLDEN_APPLE)) return 25;
        if (s.is(Items.GOLDEN_APPLE)) return 15;
        if (s.is(Items.CAKE)) return 8;
        if (s.is(Items.PUMPKIN_PIE)) return 6;
        if (s.is(Items.DIAMOND) || s.is(Items.EMERALD)) return 6;
        if (s.is(ItemTags.FLOWERS)) return 5;
        if (s.is(Items.AMETHYST_SHARD)) return 4;
        if (s.is(Items.COOKIE) || s.is(Items.HONEY_BOTTLE)) return 4;
        if (s.is(Items.SWEET_BERRIES) || s.is(Items.GLOW_BERRIES)) return 3;
        if (s.is(Items.ROTTEN_FLESH) || s.is(Items.SPIDER_EYE) || s.is(Items.POISONOUS_POTATO)
                || s.is(Items.BONE) || s.is(Items.PUFFERFISH)) return -8;
        return 0;
    }
}
