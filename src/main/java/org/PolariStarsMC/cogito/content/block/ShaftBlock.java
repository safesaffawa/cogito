package org.PolariStarsMC.cogito.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.PolariStarsMC.cogito.content.kinetic.KineticBlockEntity;
import org.PolariStarsMC.cogito.content.kinetic.KineticLines;
import org.PolariStarsMC.cogito.registry.CogitoBlockEntities;

/**
 * 传动杆：沿三个轴放置的圆柱形传动件。
 *
 * <p>外观是 8 条绕轴旋转的长方体拼出的十六边形圆柱，两端各条棱错开 0.03px
 * 以避免端面共面闪烁；贴图直接用原版石头。
 *
 * <p>接入网络后，它会跟随同一轴线上的第一个动力源（见 {@link KineticLines}）。
 * 空手右键可以读出当前转速。
 */
public class ShaftBlock extends RotatedPillarBlock implements EntityBlock {
    public static final MapCodec<ShaftBlock> CODEC = simpleCodec(ShaftBlock::new);

    /** 圆柱半径（格）：模型直径 6/16，所以半径 3/16。 */
    private static final double RADIUS = 3.0 / 16.0;
    private static final double MIN = 0.5 - RADIUS;
    private static final double MAX = 0.5 + RADIUS;

    private static final VoxelShape SHAPE_X = Shapes.box(0.0, MIN, MIN, 1.0, MAX, MAX);
    private static final VoxelShape SHAPE_Y = Shapes.box(MIN, 0.0, MIN, MAX, 1.0, MAX);
    private static final VoxelShape SHAPE_Z = Shapes.box(MIN, MIN, 0.0, MAX, MAX, 1.0);

    public ShaftBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends ShaftBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ShaftBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return KineticBlockEntity.ticker(type, CogitoBlockEntities.SHAFT.get(), ShaftBlockEntity::clientTick);
        }
        return KineticBlockEntity.ticker(type, CogitoBlockEntities.SHAFT.get(), ShaftBlockEntity::serverTick);
    }

    /** 模型交给方块实体渲染器画（见 ShaftRenderer），不然会画两遍。 */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide()) {
            KineticLines.refresh(level, pos);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block block,
            Orientation orientation, boolean movedByPiston) {
        if (!level.isClientSide()) {
            KineticLines.refresh(level, pos);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof KineticBlockEntity shaft)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(shaft.describe(), true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(BlockStateProperties.AXIS)) {
            case X -> SHAPE_X;
            case Y -> SHAPE_Y;
            case Z -> SHAPE_Z;
        };
    }
}
