package com.kitsunemod.client;

import com.kitsunemod.KitsuneMod;
import com.kitsunemod.entity.KitsuneEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class KitsuneRenderer extends HumanoidMobRenderer<KitsuneEntity, KitsuneModel> {
    private static final ResourceLocation[] TEXTURES = {
            tex("yuki"), tex("akane"), tex("hana"), tex("kuro")
    };

    private static ResourceLocation tex(String name) {
        return new ResourceLocation(KitsuneMod.MODID, "textures/entity/kitsune_" + name + ".png");
    }

    public KitsuneRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new KitsuneModel(ctx.bakeLayer(KitsuneModel.LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(KitsuneEntity entity) {
        return TEXTURES[Math.floorMod(entity.getVariant(), TEXTURES.length)];
    }

    @Override
    protected void scale(KitsuneEntity entity, PoseStack pose, float partialTick) {
        pose.scale(0.94F, 0.94F, 0.94F);
    }
}
