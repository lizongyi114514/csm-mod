package com.example.csm.client;

import com.example.csm.Csm;
import com.example.csm.form.Form;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * 变身时挂在玩家头/双臂上的电锯（纯原版 ModelPart，不依赖 GeckoLib）。
 * 锯刃 bone 每 tick 转 40°，和 GDD 里"独立 bone 旋转"的约定一致。
 *
 * 形态从 ClientSkillState 读（服务端同步过来的），不要读 PlayerResources ——
 * 客户端的 persistentData 不会同步。
 */
public class ChainsawFormLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Csm.MODID, "textures/entity/chainsaw_form.png");

    private static final ModelPart ROOT = createLayer().bakeRoot();
    private static final ModelPart HEAD_SAW = ROOT.getChild("head_saw");
    private static final ModelPart HEAD_BLADE = HEAD_SAW.getChild("head_blade");
    private static final ModelPart RIGHT_SAW = ROOT.getChild("right_saw");
    private static final ModelPart RIGHT_BLADE = RIGHT_SAW.getChild("right_blade");
    private static final ModelPart LEFT_SAW = ROOT.getChild("left_saw");
    private static final ModelPart LEFT_BLADE = LEFT_SAW.getChild("left_blade");

    public ChainsawFormLayer(PlayerRenderer renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        Form form = Form.values()[Math.floorMod(ClientSkillState.form(), Form.values().length)];
        if (form != Form.DENJI && form != Form.POCHITA) {
            return;
        }

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        float spin = (player.tickCount + partialTicks) * 40.0F;

        HEAD_BLADE.yRot = (float) Math.toRadians(spin);
        RIGHT_BLADE.xRot = (float) Math.toRadians(spin);
        LEFT_BLADE.xRot = (float) Math.toRadians(-spin);

        poseStack.pushPose();
        if (form == Form.POCHITA) {
            poseStack.scale(1.15F, 1.15F, 1.15F);
        }

        getParentModel().head.translateAndRotate(poseStack);
        HEAD_SAW.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        poseStack.pushPose();
        getParentModel().rightArm.translateAndRotate(poseStack);
        RIGHT_SAW.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        poseStack.pushPose();
        getParentModel().leftArm.translateAndRotate(poseStack);
        LEFT_SAW.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    /** 骨头坐标是相对 head / arm 的局部坐标，见 PlayerModel 的 createMesh 偏移 */
    private static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition headSaw = root.addOrReplaceChild("head_saw",
                CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -14.0F, -2.0F, 2.0F, 6.0F, 3.0F),
                PartPose.ZERO);
        headSaw.addOrReplaceChild("head_blade",
                CubeListBuilder.create().texOffs(0, 10).addBox(-1.5F, -21.0F, -1.0F, 3.0F, 8.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition rightSaw = root.addOrReplaceChild("right_saw",
                CubeListBuilder.create().texOffs(16, 0).addBox(-1.0F, 6.0F, -8.0F, 2.0F, 3.0F, 8.0F),
                PartPose.ZERO);
        rightSaw.addOrReplaceChild("right_blade",
                CubeListBuilder.create().texOffs(16, 12).addBox(-1.2F, 6.6F, -12.0F, 2.4F, 1.8F, 4.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition leftSaw = root.addOrReplaceChild("left_saw",
                CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, 6.0F, -8.0F, 2.0F, 3.0F, 8.0F),
                PartPose.ZERO);
        leftSaw.addOrReplaceChild("left_blade",
                CubeListBuilder.create().texOffs(32, 12).addBox(-1.2F, 6.6F, -12.0F, 2.4F, 1.8F, 4.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }
}
