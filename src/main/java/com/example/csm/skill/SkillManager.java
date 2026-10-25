package com.example.csm.skill;

import com.example.csm.Csm;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 读取 data/<modid>/skills/**\/*.json，支持 /reload。
 * 注册在 AddReloadListenerEvent（见 CommonEvents）。
 */
public final class SkillManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static final Map<ResourceLocation, SkillDefinition> BY_ID = new HashMap<>();
    private static final Map<String, List<SkillDefinition>> BY_SCHOOL = new HashMap<>();

    public SkillManager() {
        super(GSON, "skills");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resourceManager, ProfilerFiller profiler) {
        BY_ID.clear();
        BY_SCHOOL.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : files.entrySet()) {
            ResourceLocation fileId = entry.getKey();
            try {
                SkillDefinition def = SkillDefinition.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                        .resultOrPartial(error -> Csm.LOGGER.error("[CSM] 技能 {} 解析失败：{}", fileId, error))
                        .orElse(null);
                if (def == null) {
                    continue;
                }
                if (!def.id().equals(fileId)) {
                    Csm.LOGGER.warn("[CSM] 技能 id 与文件路径不一致：{} != {}（数据包按路径索引，会读不到）", def.id(), fileId);
                }
                BY_ID.put(def.id(), def);
                BY_SCHOOL.computeIfAbsent(def.school(), key -> new ArrayList<>()).add(def);
            } catch (Exception exception) {
                Csm.LOGGER.error("[CSM] 技能 {} 加载异常", fileId, exception);
            }
        }

        Comparator<SkillDefinition> order = Comparator
                .comparingInt(SkillDefinition::slot)
                .thenComparingInt(SkillDefinition::tier)
                .thenComparing(def -> def.id().getPath());
        BY_SCHOOL.values().forEach(list -> list.sort(order));

        Csm.LOGGER.info("[CSM] 载入 {} 个招式，{} 个流派", BY_ID.size(), BY_SCHOOL.size());
    }

    public static boolean isLoaded() {
        return !BY_ID.isEmpty();
    }

    public static SkillDefinition byId(ResourceLocation id) {
        return BY_ID.get(id);
    }

    public static List<SkillDefinition> bySchool(String school) {
        return BY_SCHOOL.getOrDefault(school, List.of());
    }

    public static Map<String, List<SkillDefinition>> all() {
        return BY_SCHOOL;
    }

    public static int count() {
        return BY_ID.size();
    }
}
