package com.kitsunemod.entity;

import com.kitsunemod.Config;
import com.kitsunemod.KitsuneMod;
import com.kitsunemod.data.HaremData;
import com.kitsunemod.dialogue.KitsuneAI;
import com.kitsunemod.dialogue.KitsuneDialogue;
import com.kitsunemod.network.ChatLineS2C;
import com.kitsunemod.network.KitsuneInfo;
import com.kitsunemod.network.ModNetwork;
import com.kitsunemod.network.OpenChatS2C;
import com.kitsunemod.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class KitsuneEntity extends PathfinderMob {
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(KitsuneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> HAPPY =
            SynchedEntityData.defineId(KitsuneEntity.class, EntityDataSerializers.BOOLEAN);

    // --- Datos persistentes ---
    private final Map<UUID, Integer> affection = new HashMap<>();
    private final Map<UUID, Integer> dates = new HashMap<>();
    @Nullable private UUID partner;
    @Nullable private UUID following;

    // --- Datos temporales ---
    @Nullable private UUID dateWith;
    private int dateTicks, dateTotal, dateNearTicks;
    private final Map<UUID, Long> chatCooldown = new HashMap<>();
    private final Map<UUID, Long> giftCooldown = new HashMap<>();
    private final Map<UUID, Deque<String[]>> history = new HashMap<>();
    private boolean waitingAI;

    public KitsuneEntity(EntityType<? extends KitsuneEntity> type, Level level) {
        super(type, level);
        this.setCanPickUpLoot(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(VARIANT, 0);
        this.entityData.define(HAPPY, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25D, true));
        this.goalSelector.addGoal(2, new FollowPartnerGoal(this, 1.15D));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    // ------------------------------------------------------------------ getters

    public int getVariant() { return this.entityData.get(VARIANT); }
    public void setVariant(int v) { this.entityData.set(VARIANT, Math.floorMod(v, Personality.values().length)); }
    public Personality getPersonality() { return Personality.byId(getVariant()); }
    public boolean isHappy() { return this.entityData.get(HAPPY); }

    public int getAffection(UUID player) { return affection.getOrDefault(player, 0); }
    public int getDates(UUID player) { return dates.getOrDefault(player, 0); }
    public boolean isPartnerOf(UUID player) { return player.equals(partner); }
    public boolean isOnDateWith(UUID player) { return player.equals(dateWith); }
    public boolean isFollowing(UUID player) { return player.equals(following); }

    public RelationStatus getStatus(UUID player) {
        if (isPartnerOf(player)) return RelationStatus.GIRLFRIEND;
        int a = getAffection(player);
        if (getDates(player) >= 1 && a >= 40) return RelationStatus.DATING;
        if (a >= 25) return RelationStatus.FRIEND;
        return RelationStatus.STRANGER;
    }

    @Override
    protected Component getTypeName() {
        return Component.literal(getPersonality().displayName);
    }

    public String displayName() { return getName().getString(); }

    @Nullable
    public Player getFollowTarget() {
        UUID id = dateWith != null ? dateWith : following;
        return id == null ? null : level().getPlayerByUUID(id);
    }

    @Nullable
    private ServerPlayer serverPlayer(@Nullable UUID id) {
        if (id == null || getServer() == null) return null;
        return getServer().getPlayerList().getPlayer(id);
    }

    // ------------------------------------------------------------------ spawn / guardado

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        setVariant(this.random.nextInt(Personality.values().length));
        return super.finalizeSpawn(level, difficulty, reason, data, tag);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", getVariant());
        CompoundTag aff = new CompoundTag();
        affection.forEach((k, v) -> aff.putInt(k.toString(), v));
        tag.put("Affection", aff);
        CompoundTag dt = new CompoundTag();
        dates.forEach((k, v) -> dt.putInt(k.toString(), v));
        tag.put("Dates", dt);
        if (partner != null) tag.putUUID("Partner", partner);
        if (following != null) tag.putUUID("Following", following);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setVariant(tag.getInt("Variant"));
        readMap(tag.getCompound("Affection"), affection);
        readMap(tag.getCompound("Dates"), dates);
        partner = tag.hasUUID("Partner") ? tag.getUUID("Partner") : null;
        following = tag.hasUUID("Following") ? tag.getUUID("Following") : null;
    }

    private static void readMap(CompoundTag tag, Map<UUID, Integer> map) {
        map.clear();
        for (String key : tag.getAllKeys()) {
            try { map.put(UUID.fromString(key), tag.getInt(key)); } catch (IllegalArgumentException ignored) {}
        }
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return false;
    }

    // ------------------------------------------------------------------ afecto

    public void addAffection(ServerPlayer p, int delta) {
        int v = Math.max(0, Math.min(100, getAffection(p.getUUID()) + delta));
        affection.put(p.getUUID(), v);
        if (isPartnerOf(p.getUUID()) && getServer() != null) {
            HaremData.get(getServer()).put(p.getUUID(), getUUID(),
                    new HaremData.Entry(displayName(), getVariant(), v));
        }
    }

    private void particles(ParticleOptions type, int count) {
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(type, getX(), getY() + 2.1D, getZ(), count, 0.4D, 0.3D, 0.4D, 0.05D);
        }
    }

    // ------------------------------------------------------------------ tick

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) return;

        if (tickCount % 10 == 0) updateDefense();
        if (dateWith != null) tickDate();
        if (tickCount % 20 == 0) {
            Player f = getFollowTarget();
            boolean happy = dateWith != null || (partner != null && f != null && distanceToSqr(f) < 100);
            if (!happy && partner != null) {
                Player p = level().getPlayerByUUID(partner);
                happy = p != null && distanceToSqr(p) < 64;
            }
            this.entityData.set(HAPPY, happy);
        }
    }

    /** Ataca monstruos que la lastimen a ella o a su pareja / acompañante. */
    private void updateDefense() {
        LivingEntity t = getTarget();
        if (t != null && (!t.isAlive() || !(t instanceof Enemy))) setTarget(null);
        if (getTarget() != null) return;

        LivingEntity attacker = getLastHurtByMob();
        if (attacker instanceof Enemy && attacker.isAlive()) { setTarget(attacker); return; }

        for (UUID id : new UUID[]{partner, following, dateWith}) {
            if (id == null) continue;
            Player p = level().getPlayerByUUID(id);
            if (p != null && distanceToSqr(p) < 256) {
                LivingEntity pa = p.getLastHurtByMob();
                if (pa instanceof Enemy && pa.isAlive() && pa != this && p.tickCount - p.getLastHurtByMobTimestamp() < 200) {
                    setTarget(pa);
                    return;
                }
            }
        }
    }

    private void tickDate() {
        ServerPlayer p = serverPlayer(dateWith);
        if (p == null || p.level() != level()) {
            dateWith = null;
            return;
        }
        dateTicks--;
        boolean near = distanceToSqr(p) < 12 * 12;
        if (near) dateNearTicks++;
        if (dateTicks % 400 == 0 && dateTicks > 0 && near) {
            addAffection(p, 1);
            particles(ParticleTypes.HEART, 3);
            if (random.nextFloat() < 0.6F) say(p, KitsuneDialogue.line(this, p, "date_chat"));
        }
        if (dateTicks <= 0) endDate(p);
    }

    private void endDate(ServerPlayer p) {
        float ratio = dateTotal == 0 ? 0 : dateNearTicks / (float) dateTotal;
        dateWith = null;
        if (ratio >= 0.6F) {
            addAffection(p, 10);
            dates.merge(p.getUUID(), 1, Integer::sum);
            particles(ParticleTypes.HEART, 14);
            playSound(SoundEvents.PLAYER_LEVELUP, 0.6F, 1.6F);
            say(p, KitsuneDialogue.line(this, p, "date_good"));
            system(p, "♥ Cita completada con " + displayName() + " (+10 afecto, citas: "
                    + getDates(p.getUUID()) + ")", ChatFormatting.LIGHT_PURPLE);
        } else {
            addAffection(p, -5);
            particles(ParticleTypes.SMOKE, 8);
            say(p, KitsuneDialogue.line(this, p, "date_bad"));
            system(p, "La cita con " + displayName() + " salió mal: casi no estuviste con ella (-5 afecto).", ChatFormatting.GRAY);
        }
    }

    // ------------------------------------------------------------------ interacción

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(net.minecraft.world.item.Items.NAME_TAG) || stack.is(net.minecraft.world.item.Items.LEAD)
                || stack.getItem() instanceof net.minecraft.world.item.SpawnEggItem) {
            return InteractionResult.PASS; // deja que la etiqueta / correa / huevo funcionen normal
        }
        if (level().isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;

        setPersistenceRequired();
        int gift = GiftValues.value(stack);
        if (gift != 0) {
            receiveGift(sp, stack, gift);
            return InteractionResult.CONSUME;
        }
        getNavigation().stop();
        getLookControl().setLookAt(sp, 30.0F, 30.0F);
        sendSnapshot(sp, true);
        say(sp, KitsuneDialogue.greeting(this, sp));
        return InteractionResult.CONSUME;
    }

    private void receiveGift(ServerPlayer p, ItemStack stack, int value) {
        long now = level().getGameTime();
        Long last = giftCooldown.get(p.getUUID());
        if (last != null && now - last < 20 * 20) {
            say(p, KitsuneDialogue.line(this, p, "gift_wait"));
            return;
        }
        giftCooldown.put(p.getUUID(), now);
        if (!p.getAbilities().instabuild) stack.shrink(1);
        addAffection(p, value);

        if (value >= 10) {
            particles(ParticleTypes.HEART, 10);
            say(p, KitsuneDialogue.line(this, p, "gift_love"));
        } else if (value > 0) {
            particles(ParticleTypes.HEART, 4);
            say(p, KitsuneDialogue.line(this, p, "gift_like"));
        } else {
            particles(ParticleTypes.ANGRY_VILLAGER, 5);
            say(p, KitsuneDialogue.line(this, p, "gift_hate"));
        }
        system(p, displayName() + ": afecto " + getAffection(p.getUUID()) + "/100 (" + (value > 0 ? "+" : "") + value + ")",
                value > 0 ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.RED);

        // Celos del harem: otras novias cercanas reaccionan
        if (value > 0) {
            for (KitsuneEntity other : level().getEntitiesOfClass(KitsuneEntity.class, getBoundingBox().inflate(12))) {
                if (other != this && other.isPartnerOf(p.getUUID()) && other.random.nextFloat() < 0.5F) {
                    other.say(p, KitsuneDialogue.line(other, p, "jealous"));
                    if (other.getPersonality() == Personality.AKANE) other.addAffection(p, -1);
                }
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean r = super.hurt(source, amount);
        if (r && !level().isClientSide && source.getEntity() instanceof ServerPlayer p) {
            addAffection(p, -15);
            say(p, KitsuneDialogue.line(this, p, "hurt"));
            particles(ParticleTypes.ANGRY_VILLAGER, 4);
            setLastHurtByMob(null);
            setTarget(null);
        }
        return r;
    }

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide && partner != null && getServer() != null) {
            HaremData.get(getServer()).remove(partner, getUUID());
            ServerPlayer p = serverPlayer(partner);
            if (p != null) system(p, "♥ " + displayName() + " se ha desvanecido en un fuego fatuo...", ChatFormatting.DARK_PURPLE);
        }
        super.die(source);
    }

    // ------------------------------------------------------------------ acciones del menú

    public void startDate(ServerPlayer p) {
        if (dateWith != null) {
            say(p, dateWith.equals(p.getUUID()) ? "¡Ya estamos en una cita! No te alejes de mí~"
                    : "Hoy ya tengo una cita con alguien más...");
            return;
        }
        if (getAffection(p.getUUID()) < Config.DATE_AFFECTION.get()) {
            say(p, KitsuneDialogue.line(this, p, "date_low"));
            system(p, "Necesitas " + Config.DATE_AFFECTION.get() + " de afecto para invitarla a una cita.", ChatFormatting.GRAY);
            return;
        }
        dateWith = p.getUUID();
        dateTotal = dateTicks = Config.DATE_MINUTES.get() * 60 * 20;
        dateNearTicks = 0;
        particles(ParticleTypes.HEART, 8);
        say(p, KitsuneDialogue.line(this, p, "date_start"));
        system(p, "♥ Cita iniciada (" + Config.DATE_MINUTES.get() + " min). Quédate cerca de "
                + displayName() + " y llévala a lugares bonitos.", ChatFormatting.LIGHT_PURPLE);
    }

    public void confess(ServerPlayer p) {
        UUID id = p.getUUID();
        if (isPartnerOf(id)) { say(p, "¡Ya soy tu novia, tonto! ~♥"); return; }
        if (partner != null) { say(p, "Lo siento... mi corazón ya le pertenece a alguien más."); return; }

        HaremData harem = HaremData.get(p.getServer());
        int haremSize = harem.getHarem(id).size();
        if (haremSize >= Config.HAREM_MAX.get()) {
            system(p, "Tu harem ya está completo (" + Config.HAREM_MAX.get() + "). Cambia 'haremMax' en la configuración.", ChatFormatting.RED);
            return;
        }
        int needAff = Config.CONFESS_AFFECTION.get() + (haremSize > 0 ? 10 : 0);
        if (getAffection(id) < Math.min(100, needAff) || getDates(id) < Config.CONFESS_DATES.get()) {
            say(p, KitsuneDialogue.line(this, p, "confess_need"));
            system(p, "Requisitos: " + Math.min(100, needAff) + " de afecto"
                    + (haremSize > 0 ? " (más alto porque ya tienes novias)" : "") + " y "
                    + Config.CONFESS_DATES.get() + " citas exitosas.", ChatFormatting.GRAY);
            return;
        }
        if (!consumeRing(p)) {
            system(p, "Para pedirle que sea tu novia necesitas un Anillo de Promesa en el inventario.", ChatFormatting.GOLD);
            return;
        }
        partner = id;
        following = id;
        addAffection(p, 5);
        harem.put(id, getUUID(), new HaremData.Entry(displayName(), getVariant(), getAffection(id)));
        particles(ParticleTypes.HEART, 25);
        playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.2F);
        say(p, KitsuneDialogue.line(this, p, "confess_yes"));
        system(p, "♥♥ ¡" + displayName() + " ahora es tu novia! Harem: "
                + harem.getHarem(id).size() + "/" + Config.HAREM_MAX.get() + " ♥♥", ChatFormatting.LIGHT_PURPLE);

        // Las demás novias reaccionan
        for (KitsuneEntity other : level().getEntitiesOfClass(KitsuneEntity.class, getBoundingBox().inflate(16))) {
            if (other != this && other.isPartnerOf(id)) other.say(p, KitsuneDialogue.line(other, p, "harem"));
        }
    }

    private boolean consumeRing(ServerPlayer p) {
        if (p.getAbilities().instabuild) return true;
        var inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(ModItems.PROMISE_RING.get())) {
                s.shrink(1);
                return true;
            }
        }
        return false;
    }

    public void toggleFollow(ServerPlayer p) {
        if (isFollowing(p.getUUID())) {
            following = null;
            say(p, KitsuneDialogue.line(this, p, "follow_off"));
            return;
        }
        if (getAffection(p.getUUID()) < 25 && !isPartnerOf(p.getUUID())) {
            say(p, "Todavía no confío lo suficiente en ti para seguirte...");
            return;
        }
        following = p.getUUID();
        say(p, KitsuneDialogue.line(this, p, "follow_on"));
    }

    public void breakUp(ServerPlayer p) {
        if (!isPartnerOf(p.getUUID())) { say(p, "Pero... no somos pareja."); return; }
        partner = null;
        if (isFollowing(p.getUUID())) following = null;
        HaremData.get(p.getServer()).remove(p.getUUID(), getUUID());
        addAffection(p, -30);
        particles(ParticleTypes.SMOKE, 10);
        say(p, KitsuneDialogue.line(this, p, "breakup"));
        system(p, displayName() + " ya no es tu novia.", ChatFormatting.GRAY);
    }

    // ------------------------------------------------------------------ chat

    /** Llamado desde el paquete de chat. Decide quién responde (@nombre, @todas o la seleccionada). */
    public static void handlePlayerChat(ServerPlayer p, KitsuneEntity selected, String text, boolean toAll) {
        List<KitsuneEntity> present = new ArrayList<>(p.level().getEntitiesOfClass(KitsuneEntity.class, p.getBoundingBox().inflate(12)));
        if (!present.contains(selected)) present.add(0, selected);

        List<KitsuneEntity> responders = new ArrayList<>();
        String msg = text;
        if (text.startsWith("@")) {
            String[] parts = text.substring(1).split(" ", 2);
            String who = parts[0].toLowerCase(Locale.ROOT);
            String rest = parts.length > 1 ? parts[1] : "";
            if (who.equals("todas") || who.equals("all")) {
                toAll = true;
                msg = rest;
            } else {
                for (KitsuneEntity k : present) {
                    if (k.displayName().toLowerCase(Locale.ROOT).startsWith(who)) {
                        responders.add(k);
                        msg = rest;
                        break;
                    }
                }
            }
        }
        if (msg.isBlank()) msg = "...";
        if (toAll) responders = present;
        if (responders.isEmpty()) responders.add(selected);

        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new ChatLineS2C("Tú", text, 0xFFFFFF));
        for (KitsuneEntity k : responders) k.respondTo(p, msg, present);
        selected.sendSnapshot(p, false);
    }

    public void respondTo(ServerPlayer p, String msg, List<KitsuneEntity> present) {
        setPersistenceRequired();
        getNavigation().stop();
        getLookControl().setLookAt(p, 30.0F, 30.0F);

        long now = level().getGameTime();
        Long last = chatCooldown.get(p.getUUID());
        if (last == null || now - last > 20 * 30) {
            chatCooldown.put(p.getUUID(), now);
            addAffection(p, 1);
        }

        Deque<String[]> hist = history.computeIfAbsent(p.getUUID(), k -> new ArrayDeque<>());
        pushHistory(hist, "user", p.getGameProfile().getName() + ": " + msg);

        if (!Config.AI_ENABLED.get()) {
            String out = KitsuneDialogue.reply(this, p, msg);
            pushHistory(hist, "assistant", out);
            say(p, out);
            return;
        }
        if (waitingAI) {
            say(p, "*está pensando...*");
            return;
        }
        waitingAI = true;
        MinecraftServer server = p.getServer();
        String system = KitsuneAI.buildSystemPrompt(this, p, present);
        KitsuneAI.ask(system, new ArrayList<>(hist)).whenComplete((reply, err) -> server.execute(() -> {
            waitingAI = false;
            String out;
            if (err != null || reply == null || reply.isBlank()) {
                KitsuneMod.LOGGER.warn("Kitsune IA falló, usando diálogo integrado: {}", err == null ? "respuesta vacía" : err.getMessage());
                out = KitsuneDialogue.reply(this, p, msg);
            } else {
                out = KitsuneAI.clean(reply, displayName());
            }
            pushHistory(hist, "assistant", out);
            say(p, out);
        }));
    }

    private static void pushHistory(Deque<String[]> hist, String role, String content) {
        hist.addLast(new String[]{role, content});
        while (hist.size() > 14) hist.removeFirst();
    }

    public void say(ServerPlayer p, String text) {
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p),
                new ChatLineS2C(displayName(), text, getPersonality().color));
    }

    private static void system(ServerPlayer p, String text, ChatFormatting color) {
        p.displayClientMessage(Component.literal(text).withStyle(color), false);
    }

    /** Envía al jugador la lista de kitsunes presentes con su estado de relación. */
    public void sendSnapshot(ServerPlayer p, boolean open) {
        List<KitsuneEntity> present = new ArrayList<>(level().getEntitiesOfClass(KitsuneEntity.class, p.getBoundingBox().inflate(12)));
        present.remove(this);
        present.add(0, this);
        List<KitsuneInfo> infos = new ArrayList<>();
        for (KitsuneEntity k : present) infos.add(k.info(p.getUUID()));
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new OpenChatS2C(open, getId(), infos));
    }

    public KitsuneInfo info(UUID player) {
        RelationStatus st = getStatus(player);
        return new KitsuneInfo(getId(), displayName(), getVariant(), getAffection(player), st.ordinal(),
                getDates(player), isOnDateWith(player), isFollowing(player));
    }
}
