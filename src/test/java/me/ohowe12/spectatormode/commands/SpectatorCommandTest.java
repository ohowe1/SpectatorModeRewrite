package me.ohowe12.spectatormode.commands;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import me.ohowe12.spectatormode.SpectatorManager;
import me.ohowe12.spectatormode.SpectatorMode;
import me.ohowe12.spectatormode.testutils.TestUtils;
import org.bukkit.GameMode;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpectatorCommandTest {
    ServerMock serverMock;
    SpectatorMode plugin;

    PlayerMock playerMock;
    SpectatorManager spectatorManager;

    @BeforeEach
    void setUp() {
        serverMock = MockBukkit.mock();
        plugin = MockBukkit.load(SpectatorMode.class);

        serverMock.setPlayers(0);
        playerMock = serverMock.addPlayer();
        playerMock.setGameMode(GameMode.SURVIVAL);

        plugin.reloadConfig();

        spectatorManager = plugin.getSpectatorManager();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void defaultCommand_WithPermission_GamemodeChanged() {
        playerMock.addAttachment(plugin, "smpspectator.use", true);

        playerMock.performCommand("spectatormode:s");

        playerMock.assertGameMode(GameMode.SPECTATOR);
    }

    @Test
    void defaultCommand_WithoutPermission_GamemodeNotChanged() {
        playerMock.addAttachment(plugin, "smpspectator.use", false);

        playerMock.performCommand("spectatormode:s");

        playerMock.assertGameMode(GameMode.SURVIVAL);
    }

    @Test
    void enableCommand_WithPermission_ReportsEnabled() {
        spectatorManager.setSpectatorEnabled(false);

        playerMock.addAttachment(plugin, "smpspectator.enable", true);
        playerMock.performCommand("spectatormode:s enable");

        assertTrue(spectatorManager.isSpectatorEnabled());
    }

    @Test
    void enableCommand_WithoutPermission_ReportsDisabled() {
        spectatorManager.setSpectatorEnabled(false);

        playerMock.addAttachment(plugin, "smpspectator.enable", false);
        playerMock.performCommand("spectatormode:s enable");

        assertFalse(spectatorManager.isSpectatorEnabled());
    }

    @Test
    void disableCommand_WithPermission_ReportsDisabled() {
        spectatorManager.setSpectatorEnabled(true);

        playerMock.addAttachment(plugin, "smpspectator.enable", true);
        playerMock.performCommand("spectatormode:s disable");

        assertFalse(spectatorManager.isSpectatorEnabled());
    }

    @Test
    void disableCommand_WithoutPermission_ReportsEnabled() {
        spectatorManager.setSpectatorEnabled(true);

        playerMock.addAttachment(plugin, "smpspectator.enable", false);
        playerMock.performCommand("spectatormode:s disable");

        assertTrue(spectatorManager.isSpectatorEnabled());
    }

    @Test
    void reloadCommand_WithPermission_ReloadMessageSent() {
        playerMock.addAttachment(plugin, "smpspectator.reload", true);
        playerMock.performCommand("spectatormode:s reload");

        TestUtils.assertEqualsColored("&bThe config file has been reloaded!", playerMock.nextMessage());
    }

    @Test
    void reloadCommand_WithoutPermission_ReloadMessageNotSent() {
        playerMock.addAttachment(plugin, "smpspectator.reload", false);
        playerMock.performCommand("spectatormode:s reload");

        assertPermissionMessageSent();
    }

    @Test
    void effectsCommand_WithPermission_DoesntHaveEffects() {
        spectatorManager.togglePlayer(playerMock, true);

        playerMock.addAttachment(plugin, "smpspectator.toggle", true);
        playerMock.performCommand("spectatormode:s effect");

        assertFalse(playerMock.hasPotionEffect(PotionEffectType.CONDUIT_POWER));
        assertFalse(playerMock.hasPotionEffect(PotionEffectType.NIGHT_VISION));
    }

    @Test
    void effectsCommand_WithoutPermission_HasEffects() {
        spectatorManager.togglePlayer(playerMock, true);

        playerMock.addAttachment(plugin, "smpspectator.toggle", false);
        playerMock.performCommand("spectatormode:s effect");

        assertTrue(playerMock.hasPotionEffect(PotionEffectType.CONDUIT_POWER));
        assertTrue(playerMock.hasPotionEffect(PotionEffectType.NIGHT_VISION));
    }

    @Test
    void forceCommand_WithPermission_TargetSwitchesGamemodesAndMessageSent() {
        PlayerMock target = serverMock.addPlayer("Player2");

        playerMock.addAttachment(plugin, "smpspectator.force", true);
        playerMock.performCommand("spectatormode:s force Player2");

        target.assertGameMode(GameMode.SPECTATOR);
        TestUtils.assertEqualsColored("&bSuccessfully forced Player2", playerMock.nextMessage());
    }

    @Test
    void forceCommand_WithoutPermission_TargetKeepsGamemodeAndPermissionMessageSent() {
        PlayerMock target = serverMock.addPlayer("Player2");

        playerMock.addAttachment(plugin, "smpspectator.force", false);
        playerMock.performCommand("spectatormode:s force Player2");

        target.assertGameMode(GameMode.SURVIVAL);
        assertPermissionMessageSent();
    }

    @Test
    void forceCommand_WithPermissionInvalidName_InvalidPlayerMessageSent() {
        playerMock.addAttachment(plugin, "smpspectator.force", true);
        playerMock.performCommand("spectatormode:s force invalidplayer");

        TestUtils.assertEqualsColored("&cThat is not a valid player", playerMock.nextMessage());
    }

    private void assertPermissionMessageSent() {
        TestUtils.assertEqualsColored("&cI'm sorry, but you do not have permission to perform this command.",
                playerMock.nextMessage());
    }
}
