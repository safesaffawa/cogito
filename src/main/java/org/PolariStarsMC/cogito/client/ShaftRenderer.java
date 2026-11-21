package org.PolariStarsMC.cogito.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.PolariStarsMC.cogito.content.block.ShaftBlockEntity;
import org.PolariStarsMC.cogito.content.kinetic.KineticBlockEntity;

/**
 * 传动杆的渲染：把方块模型绕自身轴转起来。
 *
 * <p>26.2 的渲染是 render-state 管线：{@code extractRenderState} 里准备好数据（角度、轴、moving block 状态），
 * {@code submit} 里再提交几何。这里借 {@link MovingBlockRenderState} 直接提交原版方块模型，
 * 省掉自己拼 quad 的工作。
 */
public class ShaftRenderer implements BlockEntityRenderer<ShaftBlockEntity, ShaftRenderer.State> {
    private static final double RENDER_DISTANCE = 64.0;

    public ShaftRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ShaftBlockEntity shaft, State state, float partialTick, Vec3 cameraPosition,
            ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(shaft, state, partialTick, cameraPosition, breakProgress);
        // partialTick 让两帧之间也连续。
        state.angle = shaft.getAngle() + shaft.getRpm() * KineticBlockEntity.DEGREES_PER_RPM_TICK * partialTick;
        state.axis = shaft.getAxis();
        state.moving = null;
        if (shaft.getLevel() instanceof ClientLevel level) {
            BlockPos pos = shaft.getBlockPos();
            MovingBlockRenderState moving = new MovingBlockRenderState();
            moving.blockPos = pos;
            moving.randomSeedPos = pos;
            moving.blockState = shaft.getBlockState();
            moving.biome = level.getBiome(pos);
            moving.cardinalLighting = level.cardinalLighting();
            moving.lightEngine = level.getLightEngine();
            state.moving = moving;
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        MovingBlockRenderState moving = state.moving;
        if (moving == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(switch (state.axis) {
            case X -> Axis.XP.rotationDegrees(state.angle);
            case Y -> Axis.YP.rotationDegrees(state.angle);
            case Z -> Axis.ZP.rotationDegrees(state.angle);
        });
        poseStack.translate(-0.5, -0.5, -0.5);
        collector.submitMovingBlock(poseStack, moving, 0);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRender(ShaftBlockEntity shaft, Vec3 cameraPosition) {
        return Vec3.atCenterOf(shaft.getBlockPos()).closerThan(cameraPosition, RENDER_DISTANCE);
    }

    /** 每个传动杆一份的渲染状态。 */
    public static class State extends BlockEntityRenderState {
        public float angle;
        public Direction.Axis axis = Direction.Axis.Y;
        public MovingBlockRenderState moving;
    }
}
