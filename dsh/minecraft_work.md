# Minecraft 工作记忆 · Cogito

> 定位：`dsh/memory.md` 记跨会话的"事实与决策"，本文件是 **Minecraft 侧的工作底稿**——
> 上半部分只写 26.2 上真正成立、且能指出来源的事实；下半部分写我们要走的**那一条**设计路线。
> 规则：标「待核实」的条目不要当事实引用；补新结论时把出处写进 §11。

## 0. 一页速览

| 项 | 值 |
| --- | --- |
| 项目 | `cogito`，GPL-3.0-only，作者 safesaffawa，包名 `org.PolariStarsMC.cogito` |
| 定位 | Create 系（机械动力类）科技模组；**是**同类模组，**不是** Create 附属，不依赖 Create |
| 平台 | Minecraft **26.2**（Chaos Cubed）/ NeoForge **26.2.0.75** / Java **25** / moddev 2.0.141 |
| 设定 | **如果机械动力走向了旋风人而不是烈焰人……** |
| 工程学 | 旋转是骨架，**压力**是血液；没有火、没有蒸汽、没有下界 |
| 三条差异 | 可蓄压 · 故障是吹飞不是烧毁 · 风要用空间与天气去买 |

## 1. 版本锚点（26.2）

| # | 事实 | 出处 |
| --- | --- | --- |
| 1 | 版本号规则为 `year.drop.hotfix`，1.x 止于 1.21.11；26.2 = 2026 年第二个 drop，代号 **Chaos Cubed**，2026-06-16 发布 | 外部资料 §11 |
| 2 | 26.1 起最低 **Java 25**，26.2 同；26.1 起发布**不再混淆**，反编译与查源码可靠 | 外部资料 + 本仓库 patched sources |
| 3 | 26.2 新增：硫黄洞穴、硫黄/朱砂两套建材、硫方怪、热泉毒气、间歇泉喷发、好友列表、实验性 Vulkan 渲染器（源码里对应 `SulfurCube` / `PotentSulfurBlock` / 通风口粒子、`com/mojang/blaze3d/vulkan/**`、friends 界面资源） | 源码 + 外部资料 |
| 4 | 26.2 中**床不再是方块实体**（`BedBlock` 无任何 BlockEntity 引用）；告示牌仍有 `SignBlockEntity` | 源码 |
| 5 | 实体新增属性 `air_drag_modifier` / `friction_modifier` / `gravity`（`Attributes`） | 源码 |
| 6 | `ResourceLocation` 已被 **`Identifier`** 取代（`net.minecraft.resources.Identifier`；旧的 `ResourceLocation` 类不存在） | 源码 |
| 7 | 注册常量拆分：`EntityTypes`、`BlockItemIds` 与 `Items` / `Blocks` 并存，别再假定 `Items` 里有全部东西 | 源码 |
| 8 | 方块实体持久化：`loadAdditional(ValueInput)` / `saveAdditional(ValueOutput)`；`TagValueInput/TagValueOutput` 是 tag 适配层 | 源码 |
| 9 | 环境属性系统 `net.minecraft.world.attribute`：`EnvironmentAttributes`、`EnvironmentAttributeSystem`、`EnvironmentAttributeMap`，支持 `spatiallyInterpolated()` 与 `syncable()`；天气由 `WeatherAttributes.RAIN/THUNDER` 两组映射叠加；另有时间轴包 `net.minecraft.world.timeline` | 源码 |
| 10 | 风系原版素材齐全：`Breeze`、`WindCharge` / `BreezeWindCharge`、`AbstractWindCharge`（`JUMP_SCALE = 0.25`）、`MobEffects.WIND_CHARGED`（击杀时产生阵风爆炸）、`ParticleTypes.GUST_EMITTER_SMALL/LARGE`、`SMALL_GUST`、`SoundEvents.BREEZE_WIND_CHARGE_BURST` | 源码 |
| 11 | 旋风人掉落 `minecraft:breeze_rod` **1–2**（需玩家击杀，受抢夺加成）；试炼密室结构仍在 `data/minecraft/structure/trial_chambers/**`，含 `eruption/breeze_slice_1.nbt` 与 breeze 刷怪笼 | 源码 |
| 12 | NeoForge 26.2：`DeferredRegister` / `DeferredBlock` / `DeferredItem`（`net.neoforged.neoforge.registries`）；能力系统是 `Capabilities` + `BlockCapability<T, C>` + `BlockCapabilityCache`，**不是**旧的 `getCapability` 覆写 | NeoForge sources |

由此对开发的三条实际影响：

1. **反编译友好**：26.1 起无混淆，`build/moddev/artifacts/*-sources.jar` 可直接读，遇到 API 不明先读源码再猜。
2. **风有原生底座**：阵风粒子、风弹推力、`air_drag_modifier`、天气环境属性——"风"不需要从零造物理。
3. **别抄旧教程**：`ResourceLocation`、`Items` 单点、`CompoundTag` 手写持久化、床的 BE 渲染，这些在 26.2 全是错的。

## 2. 设定：分叉点

**正史（Create）**：开拓者见到了烈焰人——一团不肯熄灭的火。把它关进燃烧室，于是"热"成为第一种被驯服的工业量：锅炉、蒸汽、黄铜、煤烟。热的麻烦是它只能**用**，不能**存**。

**本时间线**：第一次接触发生在试炼密室，他看见的是旋风人：一具由气流组成的机器，会在半空跳跃，把风压缩成弹丸投出去。被驯服的不是火，是**压差**。

三条推论，也就是三条玩法：

1. **可蓄**——风能装进罐子。生产与消耗可以错时：白天蓄、夜里放，蓄满再干活。Create 没有电池，这里必须有。
2. **可输**——风能沿管道走。物品运输变成气动：快、隐蔽、要压。
3. **可爆**——风放的错是"吹"，不是"烧"。过压不是起火，而是把周围吹飞：叶片碎、物品散、玩家被掀开。所以**泄压阀是刚需，安全是设计支柱**。

世界观代价：这条路线**不依赖下界**。没有地狱岩、烈焰棒、灵魂沙，稀缺资源变成**天空、高度、好天气**，以及试炼密室的旋风棒。

硫黄（26.2 的原版新增）在这个设定里正好是"另一条路留下的脏东西"：点得着、能量密度高，但得到的是浊风与毒气——技术上可行，工程上被鄙视。原版把"火"这条路摆在了旁边，我们不必自己造敌人的燃料。

模组名 `Cogito` 在这里是双关：这台机器**记得**一条没走过的路。所以最后一章叫"记忆"，所以这份文件本身也是设定的一部分。

## 3. 设计戒律（用来否决提案）

1. **旋转是骨架，压力是血液。** 齿轮、传动轴、离合、应力这一套保留（这是 Create 系的身份）；新增的一切以"压"为核心。
2. **风必须有价格。** 三张账单：空间（迎风面、高度、天空暴露）、天气（雨/雷加成）、蓄放（罐容上限 + 泄漏 + 爆罐）。
3. **故障要像物理。** 过压 → 风爆：推飞实体、吹散物品、打碎叶片与玻璃；**不点燃方块**。
4. **不碰火。** 不做蒸汽、不做烈焰人、不要求下界。硫黄只做"脏支线"。
5. **机器要会呼吸。** 每台机器都要有可见气旋、可听啸声、一眼可读的状态——这是 Create 手感的核心，不能丢。

## 4. 机器谱系（Create 原型 → Cogito）

| Create 原型 | 本项目 | 关键差异 |
| --- | --- | --- |
| 风车轴承 / 风帆 | **迎风轴 + 风帆组** `windward_rotor` | 仍是早期旋转源，但输出随迎风面、高度、天气浮动；可加导流罩换稳定 |
| 烈焰人燃烧室 | **旋风人风室** `breeze_chamber` | 从"热源"变"压差源"，四级（见 §5）；喂风弹→微风、旋风棒→强风、暴风核心→暴风；忘记泄压它会自己鼓风 |
| 蒸汽引擎 | **风压引擎** `pneumatic_engine` | 输入压差 + 飞轮旋转，输出旋转；无锅炉、无水位，停风即停 |
| —（新增） | **压缩机** `compressor` | 旋转→压力，把力量存进罐子；没有它就没有蓄压玩法 |
| —（新增） | **储风罐** `wind_cell` | 压力缓冲 + 能量银行；有泄漏与爆罐阈值，是最有辨识度的方块 |
| —（新增） | **风道** `wind_duct` / `glass_wind_duct` | 气动物流：比传送带快，但吃压；玻璃款好看但脆 |
| —（新增） | **流量阀** `flow_valve` | 限流、开关、分流；红石联动 |
| —（新增） | **泄压阀** `relief_valve` | 过压泄放 + 风哨；安全件兼压力表 |
| 机械搅拌器 | **涡流搅拌器** `vortex_mixer` | 要"微风"不要"加热"；可做气-固混合（把风弹打进配方） |
| 机械压床 | **气动冲压机** `pneumatic_stamper` | 蓄压→单次重击，节拍是"蓄-放"而不是匀速，手感完全不同 |
| 粉碎轮 | **风轮粉碎机** `gale_crusher` | 高速旋转 + 强风；副作用会把轻物品吹走，需要挡风罩 |
| 部署器 | **气动臂** `pneumatic_arm` | 用气压"弹"出交互，射程随压力；近距离反而不准 |
| 置物台 | **悬浮台** `air_table` | 物品悬在气垫上，可被风道吸附 |
| 传送带 / 漏斗 / 滑槽 | **风道 + 流量阀** | 没有可见的传送面；吞吐靠压，分拣靠阀 |
| 蓝图加农炮 | **回忆仪** `reminiscence_engine` | 点题 Cogito：把结构"记"下来再放回去；耗压不耗火药 |
| 精密构件 | **精密风轮** `precision_rotor` | 高阶合成件，暴风级机器的门槛 |
| 蒸汽哨 / 过热爆炸 | **风哨 / 风爆** | 声音信号；事故形态是推飞而非燃烧 |

## 5. 四级风压（数值骨架，初稿）

| 级 | 名称 | 来源 | 解锁的活 | Create 对应 |
| --- | --- | --- | --- | --- |
| 0 | 静风 Still | 无 | 只有装饰性的被动气流 | 熄灭 |
| 1 | 微风 Breeze | 风室（风弹/雪球）、低压罐 | 搅拌、轻运输、气动臂 | 微燃 |
| 2 | 强风 Gale | 风室（旋风棒）、压缩机 + 高压罐 | 冲压、粉碎、风压引擎 | 燃烧 |
| 3 | 暴风 Storm | 风室（暴风核心）+ 雷雨/高海拔加成 | 精密加工、回忆仪、大型机械 | 烈焰 |

初稿数值（**待调**）：风压 `WP` 0–64，档位阈值 0 / 8 / 24 / 48；风道传输上限 32 WV/t；罐容量 0–4096 WV；压强 = 存量 / 容量 × 64；泄漏 0.5 %/s，超过 90 % 压强开始啸叫。旋转侧沿用 RPM 与应力（SU）的直觉，但**不要照抄 Create 的平衡数字**，我们两侧都要付账。

能量流示意：

```
环境风 ──迎风轴──> 旋转 ──压缩机──> 压差 ⇄ 储风罐 ──> 机器
                     ↑                  │
                     └──── 风压引擎 ─────┘
```

## 6. 材料与美学

| 维度 | 取 | 弃 |
| --- | --- | --- |
| 主色 | 风成岩的苍青灰、白云石/方解石的雾白、凝灰岩的灰、氧化铜的青绿、玻璃、石英 | 下界砖红、灵魂沙蓝黑、岩浆橙 |
| 光源 | 冷光：发光地衣、海晶灯、玻璃管内的幽光 | 火把、营火、熔炉的暖橙 |
| 声音 | 风啸、玻璃管共鸣、低频嗡鸣、泄压的"呼——" | 蒸汽嘶声、锅炉轰鸣、火焰噼啪 |
| 粒子 | `GUST_EMITTER_SMALL/LARGE`、`SMALL_GUST`、自定气旋环与叶片残影 | 烟、火星、熔岩滴 |
| 形体 | 竖帆、螺旋桨、圆筒罐、玻璃管、羽毛压力指示器 | 锅炉、活塞、烟囱剪影 |
| 脏支线 | 硫黄只在这里用：黄色、毒气、浊风——视觉上明确"这条路不好" | 硫黄进入主线材料表 |

## 7. 进度（六章）

1. **触风 Breathe**：风弹、风瓶、手动风箱——第一次做出"微风"。
2. **立轮 Windward**：迎风轴 + 风帆 + 齿轮/传动轴——稳定旋转。
3. **蓄压 Charge**：压缩机、储风罐、风道、泄压阀——第一次把力量存下来。
4. **试风 Trial**：试炼密室、旋风棒、强风——第一批真正的机器。
5. **风暴 Stormworks**：暴风级、天气联动、大型机械与移动风帆车。
6. **记忆 Cogito**：回忆仪与全自动工厂——"这个版本记得另一条路"。

## 8. 技术落地（26.2 / NeoForge）

### 8.1 项目骨架（已存在，别重复造）

- 元信息单一来源是 `gradle.properties`；`build.gradle` 用 `src/main/templates` 展开生成 `neoforge.mods.toml`。
- run 配置：`client` / `server` / `clientData` / `serverData`（数据生成输出到 `src/generated/{client,server}`）。
- GPL-3.0-only：可以吸收 MIT 代码，但**不要**直接搬 Create 的 kinetic 实现——许可证要干净，且 26.x 的 API 已经变（`Identifier`、`ValueInput/ValueOutput`），旧代码搬来是负资产。
- 空壳入口：`src/main/java/org/PolariStarsMC/cogito/Cogito.java`。

### 8.2 旋转网络（骨架）

- 自研图结构：节点 = 轴/齿轮/机器，边 = 连接；结构变化时重建、缓存版本号，每 tick 只走活跃节点。
- 数据：RPM、应力容量/消耗、朝向；网络合并/分裂用并查集 + 失效标记。
- 同步：客户端只需要角度与 RPM（渲染用），按 N tick 发 payload；禁止逐 tick 全量包。
- 现状（2026-11-21）：旋转网络先做成**同轴直线段**——`KineticLines` 沿轴两端走，遇到不同轴/非旋转件就停；线上第一个源决定整条线的转速（多源时"先到先得"，不叠加）。源是 `creative_motor`（创造马达，**单面输出**：只有 `facing` 那一面接得上，1–256 RPM 二的幂档位，空手右键 +1 档、潜行右键反向；放置时不按 shift 输出朝视线方向，按住 shift 输出朝自己），传动件是 `shaft`。**传动杆现在真的会转**：服务端算角度、每 10 tick 同步一次（转速变化立刻补发），客户端自走 + partialTick 外推，渲染走 BER。
- 连通性由 `KineticBlockEntity#connects(Direction)` 决定：传动杆沿轴两头都通，马达只有输出面那一侧通——所以贴在马达背面带不动，符合"只有一面输出"的直觉。
- 缺口：没有应力/过载、没有分支与换向（齿轮/齿轮箱）。这些等风压那一轮一起上。

### 8.3 风压网络（血液）

- 走 NeoForge 能力：`BlockCapability<IWindHandler, Direction>`（或直接自定义接口）+ `BlockCapabilityCache` 做邻接缓存，别每 tick 全图扫。
- 元件分工：罐 = 缓冲（容量/存量/泄漏）；管 = 传导（每 tick 传输上限）；阀 = 可控元件。
- 环境输入：高度、天空暴露（`level.canSeeSky`）、天气（`level.getRainLevel/getThunderLevel`，或直接读 §1 第 9 条的环境属性）、群系。
- 可选更漂亮的做法：把"迎风面"做成自定义 `EnvironmentAttribute`（`spatiallyInterpolated()` + `syncable()`），服务端权威、客户端插值，正好贴合 26.2 的新系统。

### 8.4 数据与内容

- 注册用 `DeferredRegister.Blocks / Items / BlockEntityTypes`；物品状态用数据组件（DataComponents），别堆 NBT 字符串。
- 持久化用 `saveAdditional(ValueOutput)` / `loadAdditional(ValueInput)`——26.2 的标准写法。
- 配方、战利品、标签全走 JSON + `clientData`/`serverData` 生成，别手写。
- 方块实体优先 `BlockEntity` + ticker；26.2 的床已经没有 BE，别照抄旧渲染教程。

### 8.5 渲染与手感

- 26.2 带**实验性 Vulkan 渲染器**（默认关；后端在 `com.mojang.blaze3d.vulkan`）→ 渲染必须走 blaze3d 的抽象（`RenderType`、模型/提交管线），直接调 GL 状态会在 Vulkan 后端下炸掉。
- 传动杆的旋转渲染：方块自身 `RenderShape.INVISIBLE`，交给 BER 画（否则会画两遍）。BER 里不自己拼 quad，而是用 `MovingBlockRenderState` + `submitMovingBlock` 提交原版方块模型（`PistonHeadRenderer` 同款），PoseStack 先平移到方块中心、绕轴转、再平移回去。
- 同步：走原版方块实体数据包（`getUpdateTag`/`getUpdatePacket` + `sendBlockUpdated(UPDATE_CLIENTS)`），不自己造 payload；客户端每 tick 自走角度，服务端数据只用来纠偏。
- 石头杆转起来几乎看不出，所以在模型里留了**一条深板岩标记**（第 1 条棱的可见面）当"转速指示"；换正式贴图后可以删掉。
- 压力要"看得见"：罐体视窗液面、管内粒子流速、叶片转速、风哨音高。可读性优先于写实。
- Flywheel（Create 的实例化渲染）对 26.2 的适配**待确认**；一期先不用它。

### 8.6 性能红线

- 每 tick 预算：旋转网络 O(活跃节点)，风压网络 O(边界管段)；禁止每 tick 全区块扫描。
- 长管道压成"段"（segment）计算，不要逐方块算压。
- 玩家 64 格外降频（每 4 tick 算一次）。
- 服务端权威，客户端只做表现。

## 9. 命名表（中英对照 + 注册名）

| 中文 | English | `cogito:` |
| --- | --- | --- |
| 传动杆 | Shaft | `shaft` |
| 创造马达 | Creative Motor | `creative_motor` |
| 旋风人风室 | Breeze Chamber | `breeze_chamber` |
| 迎风轴 | Windward Rotor | `windward_rotor` |
| 压缩机 | Compressor | `compressor` |
| 储风罐 | Wind Cell | `wind_cell` |
| 风道 / 玻璃风道 | Wind Duct / Glass Duct | `wind_duct` / `glass_wind_duct` |
| 流量阀 | Flow Valve | `flow_valve` |
| 泄压阀 | Relief Valve | `relief_valve` |
| 风哨 | Wind Whistle | `wind_whistle` |
| 风压引擎 | Pneumatic Engine | `pneumatic_engine` |
| 涡流搅拌器 | Vortex Mixer | `vortex_mixer` |
| 气动冲压机 | Pneumatic Stamper | `pneumatic_stamper` |
| 风轮粉碎机 | Gale Crusher | `gale_crusher` |
| 气动臂 | Pneumatic Arm | `pneumatic_arm` |
| 悬浮台 | Air Table | `air_table` |
| 回忆仪 | Reminiscence Engine | `reminiscence_engine` |
| 精密风轮 | Precision Rotor | `precision_rotor` |
| 风暴仪 | Storm Gauge | `storm_gauge` |
| 风成岩 / 白云石 | Aeolian Rock / Dolomite | `aeolian_rock` / `dolomite` |
| 风瓶 / 暴风核心 | Bottled Gust / Gale Core | `bottled_gust` / `gale_core` |

## 10. 待核实 / 待定

- [ ] 读 `AbstractWindCharge` 与 `WindCharge` 的完整实现，定"一颗风弹 = 多少 WP"。
- [ ] 26.2 的试炼密室刷新与战利品是否有变（对照 `data/minecraft/structure/trial_chambers/**`）。
- [ ] 是否与 Create 共存：两套旋转网络会打架，需要评估是否硬依赖或明确"二选一"。
- [ ] 暴风等级与雷雨/高度的绑定强度——别让玩家觉得"看天吃饭"。
- [ ] 硫黄支线做到什么程度：纯装饰，还是"浊风"危险燃料。
- [ ] 数值定稿：WP 上限、罐容、每 tick 传输、RPM 与应力的换算表。
- [ ] 26.2 的 Vulkan 渲染对自定义方块实体渲染的实际影响（跑一次 client 实测）。
- [ ] 中文译名终稿（社区习惯 vs 直译）。

## 11. 出处与核实记录

- 2026-11-20：§1 第 4–12 条从本地源码核实——`build/moddev/artifacts/minecraft-patched-26.2.0.75-sources.jar` 与 Gradle 缓存中的 `neoforge-26.2.0.75-sources.jar`。
- 2026-11-20：§1 第 1–3 条（发布日、Java 25、26.2 内容概要）来自外部资料：[Crafty 的 26.x 综述](https://crafty.gg/articles/minecraft-26-update)、[mcmod 版本页](https://www.mcmod.cn/class/version/1.html)；Vulkan/好友列表/床与告示牌渲染的细节以实测为准。
- 引用前请确认条目在 26.2（而非 26.3）上仍成立；26.3 已于 2026-09-15 发布，本项目暂不跟进。
