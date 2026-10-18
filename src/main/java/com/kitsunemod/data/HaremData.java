package com.kitsunemod.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

/** Guarda en el mundo qué kitsunes son novias de cada jugador (su harem). */
public class HaremData extends SavedData {
    public static final String ID = "kitsunemod_harem";

    public record Entry(String name, int variant, int affection) {}

    private final Map<UUID, Map<UUID, Entry>> harems = new HashMap<>();

    public static HaremData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(HaremData::load, HaremData::new, ID);
    }

    public Map<UUID, Entry> getHarem(UUID player) {
        return Collections.unmodifiableMap(harems.getOrDefault(player, Collections.emptyMap()));
    }

    public void put(UUID player, UUID kitsune, Entry entry) {
        harems.computeIfAbsent(player, k -> new LinkedHashMap<>()).put(kitsune, entry);
        setDirty();
    }

    public void remove(UUID player, UUID kitsune) {
        Map<UUID, Entry> m = harems.get(player);
        if (m != null && m.remove(kitsune) != null) setDirty();
    }

    public static HaremData load(CompoundTag tag) {
        HaremData d = new HaremData();
        ListTag players = tag.getList("Players", Tag.TAG_COMPOUND);
        for (int i = 0; i < players.size(); i++) {
            CompoundTag pt = players.getCompound(i);
            UUID player = pt.getUUID("Id");
            ListTag girls = pt.getList("Harem", Tag.TAG_COMPOUND);
            Map<UUID, Entry> map = new LinkedHashMap<>();
            for (int j = 0; j < girls.size(); j++) {
                CompoundTag g = girls.getCompound(j);
                map.put(g.getUUID("Id"), new Entry(g.getString("Name"), g.getInt("Variant"), g.getInt("Affection")));
            }
            d.harems.put(player, map);
        }
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag players = new ListTag();
        harems.forEach((player, map) -> {
            if (map.isEmpty()) return;
            CompoundTag pt = new CompoundTag();
            pt.putUUID("Id", player);
            ListTag girls = new ListTag();
            map.forEach((id, e) -> {
                CompoundTag g = new CompoundTag();
                g.putUUID("Id", id);
                g.putString("Name", e.name());
                g.putInt("Variant", e.variant());
                g.putInt("Affection", e.affection());
                girls.add(g);
            });
            pt.put("Harem", girls);
            players.add(pt);
        });
        tag.put("Players", players);
        return tag;
    }
}
