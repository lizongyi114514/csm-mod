# CSM — Chainsaw Man Mod（1.20.1 Forge 骨架）

这是一份**能跑通"按键出招"这条链**的工程骨架，配套 `GDD.md` 的设定与 `data/csm/skills` 的 30 个招式。

```
chainsawman/
├── GDD.md                      设定书（形象 / 招式 / 系统 / Boss / 版权）
├── gen_skills.py               招式表：改一行 → 重导出 30 份 JSON
├── gen_assets.py               从 JSON 反推 lang + sounds.json
├── check_java.py               无 JDK 也能跑的静态自检
├── data/csm/skills/            30 个招式（源）
├── gen_textures.py             生成两张 64x64 占位贴图
├── gen_player_anims.py         按招式生成 playeranimator 动作（30 份 + 变身）
├── src/main/java/com/example/csm/
│   ├── Csm.java                入口
│   ├── registry/CsmEffects     13 个效果（overdrive / bleeding / …）
│   ├── damage/CsmDamage        自定义伤害类型 csm:technique
│   ├── player/PlayerResources  能量 / 形态 / flag / 冷却（NBT）
│   ├── form/{Form,FormManager} 变身、计时、虚弱
│   ├── skill/SkillDefinition   JSON → record + Codec
│   ├── skill/SkillManager      读 data/csm/skills，支持 /reload
│   ├── skill/SkillRuntime      WINDUP → ACTIVE → RECOVERY → DONE
│   ├── skill/SkillEngine       校验 / 扣费 / 冷却 / 建 runtime
│   ├── skill/SkillSlots        槽位（按 tier 排序取前 4）
│   ├── contract/               契约账本（狐狸 3 次 / 诅咒层数 / 未来预支）
│   ├── config/                 common（数值/PvP/粒子/Boss 开关）+ client（HUD）
│   ├── fx/SkillFx              三层粒子（起手 / 轨迹 / 命中）+ Boss 用形状
│   ├── entity/                 武士刀男（近战读招）+ 枪之恶魔（远程压制），均为 GeckoLib 模型
│   ├── world/DevilNestFeature  恶魔巢穴（世界生成 + /csm nest）
│   ├── item/ChainsawItem       GeckoLib 电锯物品（spin 动画）
│   ├── network/                3 个包：Cast / FormToggle / State
│   ├── event/CommonEvents      重载监听、玩家 tick、重生、/csm 命令
│   └── client/                 键位、HUD、GeckoLib 模型/渲染器、变身层、playeranimator、图鉴
└── src/main/resources/
    ├── META-INF/mods.toml      pack.mcmeta
    ├── data/csm/damage_type/technique.json
    ├── data/csm/skills/        30 个招式（构建时自动同步）
    └── assets/csm/             lang、sounds、geo、animations、player_animation、codex、贴图
```

---

## 1. 怎么跑

需要 **JDK 17**；Gradle 不用装，构建脚本会自动下载。

**最省事：双击上一级目录里的 `build-csm.cmd`。** 它会依次做：

1. 找 JDK 17（优先用 PCL 自带的 `java-runtime-gamma-snapshot`）
2. 找 Gradle，没有就下载到 `tools\`
3. `gradle build`（首次要下 Forge/MCP，约 1 GB，10–30 分钟）
4. 把 `build\libs\csm-0.1.0.jar` 复制进 PCL 的 `mods`

也可以手动跑：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File ..\build-csm.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File ..\build-csm.ps1 -CheckOnly   # 只查环境
```

如果只想在开发环境里跑（不装进 PCL）：IDEA 打开本目录，等 ForgeGradle 同步完，运行 `runClient`。

> 本机没有 JDK，所以这份代码**没有经过编译验证**。见第 5 节的"最可能需要微调的三处"。

---

## 2. 进游戏怎么测

```
/csm skills                     列出载入的所有招式（按流派分组）
/csm form denji                 切形态（hunter/denji/pochita/power/makima/reze）
/csm cast csm:denji/chainsaw_rush
/csm summon [katana|gun]         召唤武士刀男 / 枪之恶魔
/csm nest                        在面前 6 格生成一座恶魔巢穴
/csm contract info              看三条契约的账
/csm contract reset             清零（会重新挂上狐狸契约）
/give @s csm:chainsaw           拿 GeckoLib 电锯
```

键位：`1` `2` `3` `4` 技能槽，`R` 变身 / 解除。

默认是 `hunter`（早川秋），所以进游戏直接按 `1` 就能放 `katana_draw`。

槽位由 JSON 的 `slot` 字段（0–3）决定，**不是按名字排序**：

| 形态 | 键1 | 键2 | 键3 | 键4 |
|---|---|---|---|---|
| hunter（秋） | 拔刀 | 三连斩 | **狐之契约** | 诅咒钉 |
| denji | 链锯冲锋 | 双锯斩 | 头锯突进 | 飞锯 |
| power | 血锤 | 血矛 | 血剑 | 血盾 |
| makima | 锁链牵引 | 支配 | 替身 | 鼠群 |
| pochita | 地狱英雄突进 | 四臂乱舞 | **概念抹除** | 引擎咆哮 |
| reze | 爆炸踢 | 爆破拳 | **引爆** | 空中引爆 |

没写 `slot` 的招式（比如飞锯之外的 `overdrive`、`guts_finisher`、`snipe`）用 `/csm cast` 放。

验证清单：

1. 进游戏，左下角出现能量条 + 4 个技能格 + 形态名。
2. 按 `1`，怪物掉血、冷却转圈、能量减少。
3. `/csm form denji` → 技能格变成电锯系，按 `R` 会回到人形并附带 5 秒虚弱。
4. `F3+T` 或 `/reload` 改 JSON → 数值立刻生效（施法中的技能跑完旧数据）。
5. `/csm form denji` 后切第三人称（F5）→ 头和双臂上出现旋转的锯；`/give` 一把电锯，手持时锯链在动。
6. 按 `3` 放狐狸契约三次 → 攻速一次比一次慢，第三次契约作废；`/csm contract info` 能看到账。
7. 放任意技能 → 起手时手边有聚能粒子、出招时沿判定方向有轨迹、命中处有爆点；第三人称能看到挥砍/突刺/砸地动作。
8. `/csm form reze` → 用 1/2 叠炸弹层数（目标身上有橙点），再用 3 引爆；5 层引爆 31 点伤害。
9. 按 `C` 打开图鉴：左侧分区（总览 / 6 个角色 / 契约 / Boss / 形态），右侧滚轮翻页。
10. `/csm nest` → 生成 11×11 的巢穴：两个刷怪笼（僵尸 / 武士刀男）+ 一个战利品箱，箱子有概率开电锯。
11. `/csm summon` → 武士刀男；`/csm summon gun` → 枪之恶魔（8 格高，240/120 是阶段线，每次开火后有 24–50 tick 换弹）。站在他正面 12 格内：他会先收刀蓄力 24 tick（手里冒白点），**这时候侧移就能躲开**；躲开后他有 0.8 秒硬直，是输出窗口。打到 150 血进入二阶段（三连突进），120 血进入三阶段（刀身发烫、前摇缩短、追加 6 格刀风）。

---

## 3. 已经实现的

| 能力 | 位置 |
|---|---|
| JSON 技能表 + Codec 解析 + `/reload` | `SkillManager` / `SkillDefinition` |
| 三段状态机，命中只结算一次 | `SkillRuntime` |
| 锥形 / 直线 / 球形 / 单体 四种判定 | `SkillRuntime#findTargets` |
| 能量（血）、冷却、等级/flag/物品前置 | `SkillEngine` / `PlayerResources` |
| 形态切换（含"残血才能变波奇塔"） | `FormManager` |
| 3 个网络包，状态 20 tick 一同步 | `network/` |
| 自定义伤害类型 `csm:technique` | `CsmDamage` + `damage_type/technique.json` |
| 13 个效果（流血有真实 DoT，其余带属性修饰或做标记） | `CsmEffects` |
| HUD（能量条 / 冷却 / 形态 / 契约账本） | `client/SkillHud` |
| **契约账本**：狐狸 3 次、每次攻速 -10%、第 3 次作废；诅咒每层 -1 生命、每 3 层反噬；未来每次 -5 生命 | `contract/` |
| **GeckoLib 电锯**：geo 模型 + `spin` 动画（锯链往复）+ 物品渲染器 | `item/ChainsawItem`、`client/ChainsawGeoModel` |
| **玩家变身层**：头锯 + 双臂锯，锯刃每 tick 转 40° | `client/ChainsawFormLayer` |
| **三层粒子**：起手聚能 / 轨迹采样 / 命中爆点，颜色随形态（血 / 锁链灰 / 火花橙） | `fx/SkillFx` |
| **施法动作**：playeranimator 动作层（优先级 1000），每个招式一份 JSON，变身另有 `transform_pull` | `client/CsmPlayerAnimations` + `gen_player_anims.py` |
| **蕾塞（炸弹混血）**：7 个招式，`prime` 叠层 / `detonate` 引爆（6 + 5×层） | `skill/BombStacks` + `csm:bomb_primed` |
| **配置项**：`config/csm-common.toml`（伤害/冷却/PvP/粒子/变身时长/契约代价/Boss 开关）+ `config/csm-client.toml`（HUD 开关与偏移） | `config/` |
| **图鉴**：按 C 打开，内容来自 `assets/csm/codex/entries_<语言>.json`（由 `gen_codex.py` 从招式数据生成） | `client/codex/` |
| **结构**：恶魔巢穴（世界生成 + `/csm nest` 手动生成），含刷怪笼与战利品表 | `world/DevilNestFeature` + `data/csm/worldgen` |
| **自定义粒子**：`blood_spark` / `chain_spark` / `blast_ring`，8×8 贴图 + 染色 Provider | `registry/CsmParticles` + `client/particle` |
| **Boss 模型**：两个 Boss 都已换成 GeckoLib geo + 动画（idle / walk / attack） | `geo/*.geo.json` + `client/*GeoModel` |
| **Boss·枪之恶魔**：三阶段远程压制（点射 / 三连扇形 / 标记轰炸），每次开火后换弹 | `entity/GunDevilEntity` + `GunDevilAttackGoal` |
| **Boss·武士刀男**：三阶段（居合 / 三连突进 / 暴走+刀风），每招都有可读前摇与惩罚窗口 | `entity/KatanaManEntity` + `KatanaManAttackGoal` |
| `/csm` 命令（form / cast / skills / contract / summon） | `CommonEvents` |

## 4. 还没做的（明确 TODO）

- **自定义粒子**：现在用的是原版粒子（DUST / CRIT / SWEEP_ATTACK）染色，还没有自己的粒子贴图与 `particles/*.json`。
- **动作细分**：30 个招式共用 5 套模板动作（挥砍/突刺/砸地/指认/蓄力），需要美术逐条替换。
- **傀儡 / 支配**：`csm:dominated` 只是标记，没有接管 AI 的 Goal。
- **概念抹除**：目前是 60 点伤害 + `csm:erased` 标记，还没做"取消掉落 + 溶解演出"。
- **音效资源**：`sounds.json` 把每个招式映射到了原版音效（能立刻出声），正式版要换成自制素材。
- **巢穴的战利品与刷怪节奏**需要实机调（现在刷怪笼延迟 1200–2400 tick 是拍脑袋的）。
- **动画细节**：Boss 的 attack 动画只有一条，缺前摇/后摇分段。
- **Boss 模型**：已是 GeckoLib geo，但 UV 是按玩家皮肤布局推的占位比例，正式美术替换后要重新对 UV。

---

## 5. 已知风险：最可能需要微调的三处

代码按 **1.20.1 / Forge 47.x / official mappings** 写。如果编译报错，先看这三处：

1. `client/SkillHud.java` → `VanillaGuiOverlay.HOTBAR.id()`
   有些版本是 `VanillaGuiOverlay.HOTBAR.type().id()`，改这一行即可。
2. `skill/SkillManager.java` → `apply(Map<ResourceLocation, JsonElement>, …)`
   1.20.1 是 `Map`；更高版本会变成 `Map` 的其它实现或 `Object2ObjectMap`，签名跟着改。
3. `network/CsmNetwork.java` → `NetworkRegistry.ChannelBuilder`
   在 1.20.1 可用但已弃用；升到 1.20.2+ 要换成 `ChannelBuilder.named(...)`。
4. GeckoLib 相关（`ChainsawItem` / `ChainsawGeoModel` / `ChainsawItemRenderer`）
   `AnimationController` 的构造签名与 `GeoModel` 的抽象方法在 4.x 各小版本偶有差异；本项目按 **4.8.4** 写。若报错，只改这三个文件。
5. `client/CsmClientSetup` → `EntityRenderersEvent.AddLayers#getSkins()`
   1.20.1 返回 `Set<PlayerSkin>`；更早版本是 `Iterable<String>`，需要改循环。
6. playeranimator 依赖坐标（`build.gradle` / `gradle.properties`）
   按 `dev.kosmx.player-anim:player-animation-lib-forge:1.0.2-rc1+1.20` 写。若 Maven 拉不到，把 jar 放进 `libs/`，改成
   `implementation fg.deobf(files("libs/player-animation-lib-forge-1.0.2-rc1+1.20.jar"))`。
7. `CsmPlayerAnimations` 的 API（`PlayerAnimationAccess.getPlayerAnimLayer` / `ModifierLayer` / `PlayerAnimationRegistry.getAnimation`）
   playeranimator 1.0.x 与 0.4.x 包名不同；本项目按 1.0.2 写，报错只改这一个文件。

其它：`CsmEffects` 里 `AttributeModifier` 的 UUID 必须唯一（已按效果分开）；`PlayerResources` 用 `getPersistentData()`，如果以后要给别的模组读，再包一层 Capability。

---

## 6. 加一个招式

```bash
# 1) 在 gen_skills.py 的 S 表里加一行，或直接新建 JSON
#    文件必须叫 data/csm/skills/<school>/<name>.json，id 必须是 csm:<school>/<name>

# 2) 校验（路径一致性 + 字段 + 手感阈值）
python ../modstudy/validate_skills.py --skills data/csm/skills --modid csm

# 3) 同步到 resources（gen_assets 会顺带复制）
python gen_assets.py

# 4) 进游戏 /reload
```

`validate_skills.py` 会拦住三类最常见的问题：**id 与路径不一致**（技能会静默加载不到）、**引用了不存在的粒子/音效/伤害类型**（要带 `--jar`）、**冷却短于动作总长**（可以无缝连发）。

---


---

## 7. 配置

次启动会在 `config/` 下生成两份：

**`csm-common.toml`**（服务端/通用，改完需要重启或 `/reload config`）

| 键 | 默认 | 说明 |
|---|---|---|
| `combat.damageMultiplier` | 1.0 | 所有招式伤害倍率（0.1–5.0） |
| `combat.pvp` | false | 是否允许招式伤害其他玩家；Boss 不受影响 |
| `combat.cooldownMultiplier` | 1.0 | 冷却倍率（0.5 = 减半） |
| `resource.energyRegenTicks` | 20 | 每多少 tick 回 1 点血 |
| `resource.formDurationMultiplier` | 1.0 | 变身持续倍率 |
| `resource.contractPenalties` | true | 关掉后狐狸可无限用、不再掉攻速 |
| `visual.particles` | true | 是否发送招式粒子 |
| `visual.particleDensity` | 1.0 | 粒子数量倍率（不影响判定） |
| `misc.bossSummon` | true | 是否允许 `/csm summon` |

**`csm-client.toml`**

| 键 | 默认 | 说明 |
|---|---|---|
| `hud.enabled` | true | 显示技能条与能量条 |
| `hud.offsetX` / `hud.offsetY` | 0 / 0 | HUD 整体偏移（像素） |

> 客户端配置不参与判定，服务器配置不发给客户端——所以 HUD 用的是本地文件，粒子与伤害用的是服务器文件。

## 8. 版权

角色、名称、设定归 **藤本树 / 集英社**。本工程是非官方同人，发布时必须：

- 标题与简介写明"非官方 / Unofficial"；
- 不使用官方 Logo、动画截帧、漫画分镜、动画音轨；
- 图鉴标注来源（原作 / 公式书 / 本模组补充）；
- 不做付费解锁。
