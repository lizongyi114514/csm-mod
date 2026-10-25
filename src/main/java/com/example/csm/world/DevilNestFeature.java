package com.example.csm.world;

import com.example.csm.registry.CsmEntities;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * 恶魔巢穴：11x11 的小竞技场，两个刷怪笼 + 一个战利品箱。
 * 只在"够空"的洞穴里生成，否则直接放弃（返回 false），避免把结构埋进石头里。
 */
public class DevilNestFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation LOOT = new ResourceLocation("csm", "chests/devil_nest");

    public DevilNestFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        if (!hasRoom(level, origin)) {
            return false;
        }
        build(level, origin, context.random());
        return true;
    }

    /** 中心 9x5x9 至少要有一半是空气，才认为这是个洞穴 */
    public static boolean hasRoom(WorldGenLevel level, BlockPos origin) {
        int air = 0;
        int total = 0;
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                for (int dy = 0; dy <= 4; dy++) {
                    total++;
                    if (level.isEmptyBlock(origin.offset(dx, dy, dz))) {
                        air++;
                    }
                }
            }
        }
        return air * 2 >= total;
    }

    public static void build(net.minecraft.world.level.LevelAccessor level, BlockPos origin, RandomSource random) {
        BlockState floor = Blocks.COBBLED_DEEPSLATE.defaultBlockState();
        BlockState wall = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        BlockState bone = Blocks.BONE_BLOCK.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // 地面
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                level.setBlock(origin.offset(dx, -1, dz), floor, 2);
                for (int dy = 0; dy <= 4; dy++) {
                    level.setBlock(origin.offset(dx, dy, dz), air, 2);
                }
            }
        }

        // 墙
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                boolean edge = Math.abs(dx) == 5 || Math.abs(dz) == 5;
                if (!edge) {
                    continue;
                }
                for (int dy = 0; dy <= 3; dy++) {
                    BlockState state = (dy == 3 || random.nextInt(5) == 0) ? bone : wall;
                    level.setBlock(origin.offset(dx, dy, dz), state, 2);
                }
            }
        }

        // 四个角的柱子做视线遮挡
        for (int[] corner : new int[][]{{-4, -4}, {-4, 4}, {4, -4}, {4, 4}}) {
            for (int dy = 0; dy <= 3; dy++) {
                level.setBlock(origin.offset(corner[0], dy, corner[1]), bone, 2);
            }
        }

        // 刷怪笼
        spawner(level, origin.offset(-3, 0, -3), net.minecraft.world.entity.EntityType.ZOMBIE, random, 200, 400, 2);
        spawner(level, origin.offset(3, 0, 3), CsmEntities.KATANA_MAN.get(), random, 1200, 2400, 1);

        // 战利品箱
        BlockPos chestPos = origin.offset(0, 0, 2);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState().setValue(
                net.minecraft.world.level.block.ChestBlock.FACING, Direction.SOUTH), 2);
        if (level.getBlockEntity(chestPos) instanceof RandomizableContainerBlockEntity container) {
            container.setLootTable(LOOT, random.nextLong());
        }

        // 一点光源
        level.setBlock(origin.offset(0, 3, 0), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(origin.offset(-4, 0, 0), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(origin.offset(4, 0, 0), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
    }

    private static void spawner(net.minecraft.world.level.LevelAccessor level, BlockPos pos,
                                net.minecraft.world.entity.EntityType<?> type, RandomSource random,
                                int minDelay, int maxDelay, int count) {
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity blockEntity) {
            BaseSpawner spawner = blockEntity.getSpawner();
            spawner.setEntityId(type, random);
            spawner.setMinSpawnDelay(minDelay);
            spawner.setMaxSpawnDelay(maxDelay);
            spawner.setSpawnCount(count);
            spawner.setRequiredPlayerRange(12);
            spawner.setSpawnRange(4);
        }
    }
}
