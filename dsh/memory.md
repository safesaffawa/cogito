# 记忆区 · Cogito

> 本工作区的跨会话记忆。根目录 `AGENTS.md` 指向本文件：每次会话开始时读取，
> 出现"下一会话仍然有用"的信息时追加。它是给人看也给 agent 读的笔记，不是日志。

## 使用规则

- 只记可复用内容：项目事实、约定、决策及其理由、踩过的坑、待办。
- 每条一行，带日期 `YYYY-MM-DD`；过时条目直接删掉或标「已废弃」，不要层层堆积。
- 不写密钥、令牌、账号、个人隐私，也不抄一次性调试输出和整段对话。
- 新条目追加到对应小节末尾；结构真撑不住时再重组小节。

## 项目速览

- 2026-11-20：Gradle 项目 `cogito`，Minecraft NeoForge 模组；Minecraft 26.2、NeoForge 26.2.0.75、Java 25、moddev 插件 2.0.141。
- 2026-11-20：模组元信息集中在 `gradle.properties`（`mod_id=cogito`、版本 `0.1.0`、许可 `GPL-3.0-only`、作者 `safesaffawa`）；`build.gradle` 用 `src/main/templates` 展开生成 mod 元数据。
- 2026-11-20：Minecraft 侧工作稿在 [`dsh/minecraft_work.md`](minecraft_work.md)：26.2 版本锚点（含源码出处）+ 设计路线；本文件只留跨会话结论。
- 2026-11-21：首个方块 `cogito:shaft`（传动杆）落地——8 条绕轴 22.5° 的长方体拼十六边形圆柱、原版石头贴图、`AXIS` 三向放置；javac 对 26.2.0.75 编译通过，尚未启动客户端看外观。
- 2026-11-21：`cogito:creative_motor`（创造马达，黑紫/黑曜石方块）+ 旋转网络落地：网络 = 同轴直线段（`KineticLines`），第一个源定转速；马达 **单面输出**（`FACING`，只有输出面能接），1–256 RPM 二的幂档位，空手右键换档、潜行右键反向，action bar 报读数；放置时不按 shift 输出朝视线方向、按住 shift 输出朝自己。无应力、无分支。
- 2026-11-21：传动杆现在会转了——`ShaftBlock` 设 `RenderShape.INVISIBLE`，`ShaftRenderer`（client 包）用 `MovingBlockRenderState` + `submitMovingBlock` 提交原版模型并绕轴旋转；角度服务端算、每 10 tick 走原版 BE 数据包同步（`getUpdateTag`/`getUpdatePacket`），客户端自走 + partialTick 外推。马达输出面换成石头并加了 1px 轴头；杆上有一条深板岩标记用来看出转动。

## 决策记录

- 2026-11-20：主线设定定为「如果机械动力走向了旋风人而不是烈焰人」——以压差/气动取代热与蒸汽、不依赖下界；理由：与 Create 正史形成清晰对照，且 26.2 原版新增的硫黄内容天然充当"脏支线"的反面教材。

## 踩坑与已知限制

- 2026-11-21：26.2 客户端资源变了——物品模型走 `assets/<ns>/items/<id>.json`（`{"model":{"type":"minecraft:model","model":"..."}}`），`models/item/*` 只放被引用的模型；方块模型元素 `rotation.angle` 可为任意浮点数（不再限 22.5 的倍数）。重叠且共面的模型面片会 z-fighting：圆柱杆件的端面要让每条棱错开一点（本次为 0.03px/条）。
- 2026-11-21：沙箱下 `gradlew` 写不了 `~/.gradle`（`.zip.lck` 拒绝访问）→ 验证编译改用 Zulu 25 的 `javac` 直接编译，classpath = `build/moddev/artifacts/minecraft-patched-26.2.0.75-merged.jar` + gradle 缓存里的 neoforge/mojang 等 jar。
- 2026-11-21：26.2 的 BER 是 render-state 管线（`createRenderState`/`extractRenderState`/`submit`）。要复用原版方块模型，最省事的是在 `extractRenderState` 里填 `MovingBlockRenderState`，`submit` 里 `collector.submitMovingBlock(poseStack, state, 0)`——它按 pose 矩阵在原位画模型，不会自己平移。`@EventBusSubscriber` 在 26.2 只有 `value=Dist[]` 和 `modid` 两个成员（没有 `bus`）。
- 2026-11-21：`BlockPlaceContext#getNearestLookingDirection()` = 玩家视线方向；按住 shift 放置时取 `getOpposite()` 就是"输出面朝自己"。方块朝向用 `BlockStateProperties.FACING` + 手写 `rotate`/`mirror`，blockstate 的六个变体照抄 `lightning_rod.json` 的旋转表（模型默认朝上）。

## 用户偏好

- 2026-11-21：新机器先要"纯方块 + 数据能跑通"的原型（用原版贴图占位即可），模型、贴图、渲染一律后置。—— 适用于所有新方块。

## 待查明 / 待办

- [ ] （2026-11-20）按 `dsh/minecraft_work.md` §7 六章推进：先搭旋转网络与风压网络骨架，再落 `breeze_chamber` / `wind_cell` 两个标志方块。
- [ ] （2026-11-21）换向与分支：齿轮、齿轮箱、离合；应力/过载判定；这些和风压网络一起设计。
