package org.PolariStarsMC.cogito.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.PolariStarsMC.cogito.content.kinetic.KineticBlockEntity;
import org.PolariStarsMC.cogito.content.kinetic.KineticLines;
import org.PolariStarsMC.cogito.registry.CogitoBlockEntities;

/**
 * 创造马达的方块实体。
 *
 * <p>它只记自己的设定转速（1–256，正负代表方向），不消耗任何东西——这就是"创造"的部分。
 * 网络转速由 {@link KineticLines} 推给接在输出面那一侧的传动件；背面接不上。
 */
public class CreativeMotorBlockEntity extends KineticBlockEntity {
    /** 右键循环的档位：二的幂，和齿轮比直觉一致。 */
    public static final int[] SPEEDS = {1, 2, 4, 8, 16, 32, 64, 128, 256};
    public static final int DEFAULT_SPEED = 32;

    private int settingRpm = DEFAULT_SPEED;

    public CreativeMotorBlockEntity(BlockPos pos, BlockState state) {
        super(CogitoBlockEntities.CREATIVE_MOTOR.get(), pos, state);
    }

    /** 输出面。 */
    public Direction getOutputSide() {
        return this.getBlockState().getValue(BlockStateProperties.FACING);
    }

    @Override
    public Direction.Axis getAxis() {
        return this.getOutputSide().getAxis();
    }

    /** 只有输出面那一侧能接东西。 */
    @Override
    public boolean connects(Direction side) {
        return side == this.getOutputSide();
    }

    @Override
    public boolean isSource() {
        return true;
    }

    @Override
    public int getSourceRpm() {
        return this.settingRpm;
    }

    public int getSettingRpm() {
        return this.settingRpm;
    }

    /** 下一档，保持方向：1→2→…→256→1。 */
    public void cycleSpeed() {
        int magnitude = Math.abs(this.settingRpm);
        int next = SPEEDS[(nearestIndex(magnitude) + 1) % SPEEDS.length];
        applySetting(this.settingRpm < 0 ? -next : next);
    }

    /** 反向。 */
    public void reverse() {
        applySetting(-this.settingRpm);
    }

    private void applySetting(int rpm) {
        this.settingRpm = rpm;
        this.setChanged();
        Level level = this.getLevel();
        if (level != null && !level.isClientSide()) {
            KineticLines.refresh(level, this.getBlockPos());
        }
    }

    private static int nearestIndex(int magnitude) {
        int best = 0;
        int bestDistance = Integer.MAX_VALUE;
        for (int i = 0; i < SPEEDS.length; i++) {
            int distance = Math.abs(SPEEDS[i] - magnitude);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = i;
            }
        }
        return best;
    }

    /** 读数里报输出面朝向，而不是抽象的轴。 */
    @Override
    protected Component orientationName() {
        return Component.translatable("message.cogito.facing." + this.getOutputSide().getName());
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CreativeMotorBlockEntity motor) {
        motor.tickServer();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        int saved = input.getIntOr("setting", DEFAULT_SPEED);
        this.settingRpm = saved == 0 ? DEFAULT_SPEED : saved;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("setting", this.settingRpm);
    }
}
