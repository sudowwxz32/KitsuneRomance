package com.kitsunemod.registry;

import com.kitsunemod.KitsuneMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, KitsuneMod.MODID);

    /** Tofu inari: el regalo favorito de toda kitsune. También es comida. */
    public static final RegistryObject<Item> INARI_TOFU = ITEMS.register("inari_tofu",
            () -> new TooltipItem(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(6).saturationMod(0.7F).build()),
                    "tooltip.kitsunemod.inari_tofu"));

    /** Anillo de promesa: necesario para pedirle a una kitsune que sea tu novia. */
    public static final RegistryObject<Item> PROMISE_RING = ITEMS.register("promise_ring",
            () -> new TooltipItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE),
                    "tooltip.kitsunemod.promise_ring"));

    public static final RegistryObject<Item> KITSUNE_SPAWN_EGG = ITEMS.register("kitsune_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.KITSUNE, 0xF2A65A, 0xFFF4E6, new Item.Properties()));

    public static class TooltipItem extends Item {
        private final String key;

        public TooltipItem(Properties props, String key) {
            super(props);
            this.key = key;
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable(key).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }
}
