package org.PolariStarsMC.cogito.content.kinetic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * 旋转网络的拓扑维护。
 *
 * <p>一条网络 = 沿同一根轴、且两端都愿意相连的一串 {@link KineticBlockEntity}。
 * 有源时整条线的转速等于源；没有源时整条线停转（0）。
 * 一条线出现多个源时，以"负方向端点一侧最先遇到的那个源"为准——先来后到，不叠加。
 *
 * <p>注意方向性：马达只在输出面那一侧接得上，所以把传动杆贴在马达背面是带不动的。
 */
public final class KineticLines {
    private KineticLines() {
    }

    /** 重建 {@code pos} 所在的整条网络，并把转速推给线路上每个成员。 */
    public static void refresh(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) {
            return;
        }
        if (!(level.getBlockEntity(pos) instanceof KineticBlockEntity self)) {
            return;
        }
        Direction.Axis axis = self.getAxis();
        List<KineticBlockEntity> negative = walk(level, self, Direction.get(Direction.AxisDirection.NEGATIVE, axis));
        List<KineticBlockEntity> positive = walk(level, self, Direction.get(Direction.AxisDirection.POSITIVE, axis));

        Collections.reverse(negative);
        List<KineticBlockEntity> line = new ArrayList<>(negative.size() + 1 + positive.size());
        line.addAll(negative);
        line.add(self);
        line.addAll(positive);

        int rpm = 0;
        for (KineticBlockEntity member : line) {
            if (member.isSource()) {
                rpm = member.getSourceRpm();
                break;
            }
        }
        for (KineticBlockEntity member : line) {
            member.setRpm(rpm);
        }
    }

    /** 从 {@code start} 沿 {@code dir} 一路走，收集能接上的邻居（不含 start）。 */
    private static List<KineticBlockEntity> walk(Level level, KineticBlockEntity start, Direction dir) {
        List<KineticBlockEntity> out = new ArrayList<>();
        KineticBlockEntity current = start;
        BlockPos.MutableBlockPos cursor = start.getBlockPos().mutable();
        while (current.connects(dir)) {
            cursor.move(dir);
            if (!(level.getBlockEntity(cursor) instanceof KineticBlockEntity next)) {
                break;
            }
            if (!next.connects(dir.getOpposite())) {
                break;
            }
            out.add(next);
            current = next;
        }
        return out;
    }
}
