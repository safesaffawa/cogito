package org.PolariStarsMC.cogito.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.PolariStarsMC.cogito.content.kinetic.KineticBlockEntity;
import org.PolariStarsMC.cogito.content.kinetic.KineticLines;
import org.PolariStarsMC.cogito.registry.CogitoBlockEntities;

/**
 * 创造马达：黑紫色的纯方块，只有 {@code facing} 那一面输出旋转。
 *
 * <p>放置方向：不按 shift 时输出面朝"玩家看出去的方向"（背对玩家），
 * 按住 shift 时反过来，输出面朝着玩家自己。
 *
 * <p>空手右键切换档位（1 / 2 / 4 / … / 256 RPM，循环），潜行右键反向，读数在 action bar 上。
 */
public class CreativeMotorBlock extends Block implements EntityBlock {
    public static final MapCodec<CreativeMotorBlock> CODEC = simpleCodec(CreativeMotorBlock::new);
    /** 输出面：只有这一面能接传动杆。 */
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public CreativeMotorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.UP));
    }

    @Override
    public MapCodec<? extends CreativeMotorBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return this.defaultBlockState().setValue(FACING, context.getClickedFace());
        }
        // 玩家看向哪，输出就朝哪；按住 shift 则掉头朝着玩家。
        Direction look = context.getNearestLookingDirection();
        return this.defaultBlockState().setValue(FACING, player.isShiftKeyDown() ? look.getOpposite() : look);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CreativeMotorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide()
                ? null
                : KineticBlockEntity.ticker(type, CogitoBlockEntities.CREATIVE_MOTOR.get(), CreativeMotorBlockEntity::serverTick);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide()) {
            KineticLines.refresh(level, pos);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean movedByPiston) {
        if (!level.isClientSide()) {
            KineticLines.refresh(level, pos);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof CreativeMotorBlockEntity motor)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            motor.reverse();
        } else {
            motor.cycleSpeed();
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(motor.describe(), true);
        }
        return InteractionResult.SUCCESS;
    }
}
