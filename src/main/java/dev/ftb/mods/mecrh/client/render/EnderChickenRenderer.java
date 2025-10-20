package dev.ftb.mods.mecrh.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.ftb.mods.mecrh.MECRHMod;
import dev.ftb.mods.mecrh.client.model.EnderChickenModel;
import dev.ftb.mods.mecrh.entity.EnderChicken;
import dev.ftb.mods.mecrh.entity.ai.ChickenLaserGoal;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.renderer.specialty.DynamicGeoEntityRenderer;

public class EnderChickenRenderer extends DynamicGeoEntityRenderer<EnderChicken> {
    private static final ResourceLocation SHIELD_TEXTURE = MECRHMod.id("textures/entity/chicken_shield_layer.png");

    public EnderChickenRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new EnderChickenModel());

        addRenderLayer(new LaserLayer());
    }

    public static EnderChickenRenderer scaled(EntityRendererProvider.Context renderManager, float scale) {
        return (EnderChickenRenderer) new EnderChickenRenderer(renderManager).withScale(scale);
    }

    @Override
    protected @Nullable ResourceLocation getTextureOverrideForBone(GeoBone bone, EnderChicken animatable, float partialTick) {
        return isShieldBone(bone) ? SHIELD_TEXTURE : null;
    }

    @Override
    protected @Nullable RenderType getRenderTypeOverrideForBone(GeoBone bone, EnderChicken animatable, ResourceLocation texturePath, MultiBufferSource bufferSource, float partialTick) {
        if (!isShieldBone(bone)) {
            return super.getRenderTypeOverrideForBone(bone, animatable, texturePath, bufferSource, partialTick);
        }
        float f = (float) animatable.tickCount + partialTick;
        return RenderType.energySwirl(SHIELD_TEXTURE, xOffset(f) % 1.0F, f * 0.01F % 1.0F);
    }

    @Override
    protected boolean boneRenderOverride(PoseStack poseStack, GeoBone bone, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay, int colour) {
        return isShieldBone(bone) && !animatable.isForceField();
    }

    private float xOffset(float tickCount) {
        return Mth.cos(tickCount * 0.01F) * 3.0F;
    }

    private boolean isShieldBone(GeoBone bone) {
        return bone.getName().endsWith("_armour");
    }

    private class LaserLayer extends GeoRenderLayer<EnderChicken> {
        public LaserLayer() {
            super(EnderChickenRenderer.this);
        }

        @Override
        public void render(PoseStack poseStack, EnderChicken animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            int progress = animatable.getFiringProgress();
            if (animatable.isAlive() && animatable.isFiringLaser() && progress > ChickenLaserGoal.WARMUP_TIME && progress < ChickenLaserGoal.LASER_DURATION) {
                poseStack.pushPose();

                Vec3 origin = animatable.partBill.position().subtract(animatable.position())
                        .scale(0.66667);  // FIXME why is this scaling necessary???
                poseStack.translate(origin.x, origin.y, origin.z);
                Vec3 target = origin.add(animatable.getViewVector(partialTick).normalize().scale(animatable.getLaserLength()));
                Vec3 laserVec = target.subtract(origin);
                float beamLength = (float)(laserVec.length() + 1.0);
                laserVec = laserVec.normalize();
                float pitch = (float)Math.acos(laserVec.y);
                float yaw = (float)Math.atan2(laserVec.z, laserVec.x);
                poseStack.mulPose(Axis.YP.rotation(Mth.HALF_PI - yaw));
                poseStack.mulPose(Axis.XP.rotation(pitch));

                float beamRadius = 0.2F;
                float glowRadius = 0.25F;
                int color = 0xFFE079FA;

                float time = Math.floorMod(animatable.level().getGameTime(), 40) + partialTick;
                float f1 = beamLength < 0 ? time : -time;
                float f2 = Mth.frac(f1 * 0.2F - (float)Mth.floor(f1 * 0.1F));
                float maxV = f2 - 1.0F;
                float minV = beamLength * (0.5F / beamRadius) + maxV;

                poseStack.pushPose();
                poseStack.mulPose(Axis.YP.rotationDegrees(time * 2.25F - 45.0F));
                renderLaser(poseStack, bufferSource.getBuffer(RenderType.beaconBeam(BeaconRenderer.BEAM_LOCATION, false)),
                        color, 0, beamLength, 0.0F, beamRadius, beamRadius, 0.0F, -beamRadius, 0.0F, 0.0F, -beamRadius,
                        0.0F, 1.0F, minV, maxV);
                poseStack.popPose();

                renderLaser(poseStack, bufferSource.getBuffer(RenderType.beaconBeam(BeaconRenderer.BEAM_LOCATION, true)),
                        FastColor.ARGB32.color(32, color), 0, beamLength, 0.0F, -glowRadius, glowRadius, -glowRadius, -glowRadius, glowRadius, glowRadius, glowRadius,
                        0.0F, 1.0F, minV, maxV);

                poseStack.popPose();
            }
        }

        private static void renderLaser(PoseStack poseStack, VertexConsumer consumer, int color, float minY, float maxY, float x1, float z1, float x2, float z2, float x3, float z3, float x4, float z4, float minU, float maxU, float minV, float maxV) {
            PoseStack.Pose pose = poseStack.last();
            renderQuad(pose, consumer, color, minY, maxY, x1, z1, x2, z2, minU, maxU, minV, maxV);
            renderQuad(pose, consumer, color, minY, maxY, x4, z4, x3, z3, minU, maxU, minV, maxV);
            renderQuad(pose, consumer, color, minY, maxY, x2, z2, x4, z4, minU, maxU, minV, maxV);
            renderQuad(pose, consumer, color, minY, maxY, x3, z3, x1, z1, minU, maxU, minV, maxV);
        }

        private static void renderQuad(PoseStack.Pose pose, VertexConsumer consumer, int color, float minY, float maxY, float minX, float minZ, float maxX, float maxZ, float minU, float maxU, float minV, float maxV) {
            addVertex(pose, consumer, color, maxY, minX, minZ, maxU, minV);
            addVertex(pose, consumer, color, minY, minX, minZ, maxU, maxV);
            addVertex(pose, consumer, color, minY, maxX, maxZ, minU, maxV);
            addVertex(pose, consumer, color, maxY, maxX, maxZ, minU, minV);
        }

        private static void addVertex(PoseStack.Pose pose, VertexConsumer consumer, int color, float y, float x, float z, float u, float v) {
            consumer.addVertex(pose, x, y, z)
                    .setColor(color)
                    .setUv(u, v)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setNormal(pose, 0.0F, 1.0F, 0.0F);
        }
    }
}
