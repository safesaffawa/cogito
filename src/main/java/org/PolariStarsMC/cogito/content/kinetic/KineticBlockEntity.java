package org.PolariStarsMC.cogito.content.kinetic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 旋转件的最小内核：一根轴、一个网络转速、一个用来渲染的累计角度。
 *
 * <p>网络模型：旋转件只沿自己"能接"的方向连成一条直线（见 {@link #connects(Direction)}）。
 * 传动杆两头都能接，马达只有输出那一面能接。源给出转速，其余件只负责传递。
 *
 * <p>同步走原版方块实体数据包：转速变化时立即发，平时每 {@value #SYNC_INTERVAL_TICKS} tick 发一次角度，
 * 客户端自己按转速把角度推下去，因此画面是连续的。
 */
public abstract class KineticBlockEntity extends BlockEntity {
    /** 每 tick 转过的角度：RPM / 60 圈每秒 / 20 tick = RPM × 0.3 度。 */
    public static final float DEGREES_PER_RPM_TICK = 0.3F;
    /** 服务端每隔多少 tick 同步一次角度（转速变化时会立刻补发）。 */
    public static final int SYNC_INTERVAL_TICKS = 10;

    private int rpm;
    private float angle;
    private boolean initialized;
    private int syncTimer;

    protected KineticBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** 自身轴线；只有一面输出的元件应该覆写成"输出面的轴"。 */
    public Direction.Axis getAxis() {
        return this.getBlockState().getValue(BlockStateProperties.AXIS);
    }

    /** 这个方向能不能接上邻居。传动杆两头都行，源只在自己输出面那一侧接。 */
    public boolean connects(Direction side) {
        return side.getAxis() == this.getAxis();
    }

    /** 是否是动力源。 */
    public boolean isSource() {
        return false;
    }

    /** 源自己设定的输出转速（带符号）；传动件返回 0。 */
    public int getSourceRpm() {
        return 0;
    }

    /** 当前网络转速（带符号，负数代表反向）。 */
    public int getRpm() {
        return this.rpm;
    }

    public void setRpm(int rpm) {
        if (this.rpm != rpm) {
            this.rpm = rpm;
            this.setChanged();
            this.sendUpdate();
        }
    }

    /** 累计角度（度，0–360），渲染用。 */
    public float getAngle() {
        return this.angle;
    }

    /** 服务端 tick：首次接网、推进角度、定期同步。 */
    public void tickServer() {
        Level level = this.getLevel();
        if (level == null) {
            return;
        }
        if (!this.initialized) {
            this.initialized = true;
            KineticLines.refresh(level, this.getBlockPos());
        }
        this.advanceAngle();
        // 停转的件不用刷角度；转速变化时 setRpm 会立刻补一条。
        if (this.rpm != 0 && ++this.syncTimer >= SYNC_INTERVAL_TICKS) {
            this.syncTimer = 0;
            level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    /** 客户端 tick：自己把角度推下去，数据包只用来纠偏。 */
    public void tickClient() {
        this.advanceAngle();
    }

    private void advanceAngle() {
        if (this.rpm != 0) {
            this.angle = (this.angle + this.rpm * DEGREES_PER_RPM_TICK) % 360.0F;
            if (this.angle < 0.0F) {
                this.angle += 360.0F;
            }
        }
    }

    private void sendUpdate() {
        Level level = this.getLevel();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    /** 右键时的状态文本（服务端发到 action bar）。 */
    public Component describe() {
        if (this.isSource()) {
            int sourceRpm = this.getSourceRpm();
            return Component.translatable("message.cogito.motor.speed",
                    Math.abs(sourceRpm), direction(sourceRpm), orientationName());
        }
        int networkRpm = this.getRpm();
        if (networkRpm == 0) {
            return Component.translatable("message.cogito.shaft.idle", orientationName());
        }
        return Component.translatable("message.cogito.shaft.speed",
                Math.abs(networkRpm), direction(networkRpm), orientationName());
    }

    /** 状态文本里的朝向：传动件报轴，马达报输出面。 */
    protected Component orientationName() {
        return Component.translatable("message.cogito.axis." + this.getAxis().getName());
    }

    private static Component direction(int rpm) {
        return Component.translatable(rpm < 0 ? "message.cogito.direction.reverse" : "message.cogito.direction.forward");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.rpm = input.getIntOr("rpm", 0);
        this.angle = input.getFloatOr("angle", 0.0F);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("rpm", this.rpm);
        output.putFloat("angle", this.angle);
    }

    /** 让 {@code type} 与期望的方块实体类型对上时才交出 ticker，避免强转事故。 */
    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity, E extends KineticBlockEntity> BlockEntityTicker<T> ticker(
            BlockEntityType<T> type, BlockEntityType<E> expected, BlockEntityTicker<E> ticker) {
        if ((BlockEntityType<?>) type != expected) {
            return null;
        }
        return (BlockEntityTicker<T>) (BlockEntityTicker<?>) ticker;
    }
}
