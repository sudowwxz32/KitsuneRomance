package com.kitsunemod.client;

import com.kitsunemod.KitsuneMod;
import com.kitsunemod.entity.KitsuneEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Modelo humanoide con orejas de zorro, cabello largo, falda y tres colas animadas. */
public class KitsuneModel extends HumanoidModel<KitsuneEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(KitsuneMod.MODID, "kitsune"), "main");

    private static final float[] TAIL_SPREAD = {-0.55F, 0.0F, 0.55F};

    private final ModelPart leftEar, rightEar;
    private final ModelPart[] tails = new ModelPart[3];
    private final ModelPart[] tips = new ModelPart[3];

    public KitsuneModel(ModelPart root) {
        super(root);
        this.leftEar = this.head.getChild("left_ear");
        this.rightEar = this.head.getChild("right_ear");
        for (int i = 0; i < 3; i++) {
            tails[i] = this.body.getChild("tail" + i);
            tips[i] = tails[i].getChild("tip");
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");
        PartDefinition body = root.getChild("body");

        // Orejas de zorro
        head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(0, 32)
                        .addBox(-1.5F, -4.0F, -0.5F, 3, 4, 1),
                PartPose.offsetAndRotation(2.6F, -8.0F, -0.5F, 0.0F, 0.0F, 0.3F));
        head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(8, 32)
                        .addBox(-1.5F, -4.0F, -0.5F, 3, 4, 1),
                PartPose.offsetAndRotation(-2.6F, -8.0F, -0.5F, 0.0F, 0.0F, -0.3F));

        // Cabello largo por la espalda
        head.addOrReplaceChild("hair", CubeListBuilder.create().texOffs(32, 32)
                        .addBox(-4.0F, -8.0F, 4.6F, 8, 13, 1),
                PartPose.ZERO);

        // Falda / hakama
        body.addOrReplaceChild("skirt", CubeListBuilder.create().texOffs(0, 52)
                        .addBox(-4.5F, 10.0F, -2.5F, 9, 6, 5, new CubeDeformation(0.25F)),
                PartPose.ZERO);

        // Tres colas esponjosas (base + punta)
        for (int i = 0; i < 3; i++) {
            PartDefinition tail = body.addOrReplaceChild("tail" + i, CubeListBuilder.create().texOffs(0, 40)
                            .addBox(-1.5F, 0.0F, -1.5F, 3, 7, 3),
                    PartPose.offsetAndRotation(0.0F, 11.0F, 2.0F, 1.9F, 0.0F, TAIL_SPREAD[i]));
            tail.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(44, 50)
                            .addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4),
                    PartPose.offsetAndRotation(0.0F, 6.5F, 0.0F, 0.35F, 0.0F, 0.0F));
        }
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(KitsuneEntity e, float limbSwing, float limbSwingAmount, float age, float netHeadYaw, float headPitch) {
        super.setupAnim(e, limbSwing, limbSwingAmount, age, netHeadYaw, headPitch);
        boolean happy = e.isHappy();
        float speed = happy ? 0.30F : 0.09F;
        float amp = happy ? 0.30F : 0.14F;
        for (int i = 0; i < 3; i++) {
            float phase = age * speed + i * 1.3F;
            tails[i].xRot = 1.9F + Mth.sin(phase * 0.7F) * 0.10F + limbSwingAmount * 0.25F;
            tails[i].zRot = TAIL_SPREAD[i] + Mth.sin(phase) * amp;
            tips[i].xRot = 0.35F + Mth.sin(phase + 0.8F) * 0.15F;
        }
        // Las orejas se mueven de vez en cuando
        float twitch = Mth.sin(age * 0.15F) > 0.97F ? 0.25F : 0.0F;
        leftEar.zRot = 0.3F + twitch;
        rightEar.zRot = -0.3F - (happy ? twitch : 0.0F);
        leftEar.xRot = rightEar.xRot = happy ? -0.15F : 0.0F;
    }
}
