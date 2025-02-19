package cloudburst.rejects.modules;

import cloudburst.rejects.MeteorRejectsAddon;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;


public class AntiBot extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgFilters = settings.createGroup("Filters");

    private final Setting<Boolean> removeInvisible = sgGeneral.add(new BoolSetting.Builder()
            .name("remove-invisible")
            .description("Removes bot only if they are invisible.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> gameMode = sgFilters.add(new BoolSetting.Builder()
            .name("null-gamemode")
            .description("Removes players without a gamemode")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> api = sgFilters.add(new BoolSetting.Builder()
            .name("null-entry")
            .description("Removes players without a player entry")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> profile = sgFilters.add(new BoolSetting.Builder()
            .name("null-profile")
            .description("Removes players without a game profile")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> latency = sgFilters.add(new BoolSetting.Builder()
            .name("ping-check")
            .description("Removes players using ping check")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> nullException = sgFilters.add(new BoolSetting.Builder()
            .name("null-exception")
            .description("Removes players if a NullPointerException occurred")
            .defaultValue(false)
            .build()
    );

    public AntiBot() {
        super(MeteorRejectsAddon.CATEGORY, "anti-bot", "Detects and removes bots.");
    }

    @EventHandler
    public void onTick(TickEvent.Post tickEvent) {
        for (Entity entity : mc.world.getEntities())
        {
            if (removeInvisible.get() && !entity.isInvisible()) continue;

            if (isBot(entity)) entity.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    private boolean isBot(Entity entity) {
        if (entity == null) return false;
        if (!(entity instanceof PlayerEntity)) return false;

        PlayerEntity player = (PlayerEntity)entity;
        try {
            if (gameMode.get() && EntityUtils.getGameMode(player) == null) return true;
            if (api.get() &&
                    mc.getNetworkHandler().getPlayerListEntry(player.getUuid()) == null) return true;
            if (profile.get() &&
                    mc.getNetworkHandler().getPlayerListEntry(player.getUuid()).getProfile() == null) return true;
            if (latency.get() &&
                    mc.getNetworkHandler().getPlayerListEntry(player.getUuid()).getLatency() > 1) return true;
        } catch (NullPointerException e) {
            if (nullException.get()) return true;
        }

        return false;
    }
}
