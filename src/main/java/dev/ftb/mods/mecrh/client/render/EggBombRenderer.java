package dev.ftb.mods.mecrh.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.ftb.mods.mecrh.entity.EggBomb;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

public class EggBombRenderer extends EntityRenderer<EggBomb> {
    public EggBombRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EggBomb entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        BlockState state = Blocks.DRAGON_EGG.defaultBlockState();
        if (state.getRenderShape() == RenderShape.MODEL) {
            Level world = entity.getCommandSenderWorld();
            if (state != world.getBlockState(entity.blockPosition()) && state.getRenderShape() != RenderShape.INVISIBLE) {
                poseStack.pushPose();
                // spin the block on the Y axis
                poseStack.translate(0, 0.5, 0);
                float angle = ((entity.tickCount + partialTick) * 18);
                poseStack.mulPose(Axis.YP.rotationDegrees(angle));
                poseStack.translate(-0.5, -0.5, -0.5);

                BlockPos blockpos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
                BlockRenderDispatcher renderer = Minecraft.getInstance().getBlockRenderer();
                BakedModel blockModel = renderer.getBlockModel(state);
                for (RenderType type : blockModel.getRenderTypes(state, world.getRandom(), ModelData.EMPTY)) {
                    renderer.getModelRenderer().tesselateBlock(world, blockModel, state, blockpos, poseStack, bufferSource.getBuffer(type), false, world.getRandom(), 0L, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, type);
                }
                poseStack.popPose();
            }
        }
    }

    @Override
    public ResourceLocation getTextureLocation(EggBomb eggBomb) {
        return null;
    }
}
