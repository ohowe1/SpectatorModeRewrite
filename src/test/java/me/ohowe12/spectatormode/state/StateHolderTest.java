package me.ohowe12.spectatormode.state;

import static org.junit.jupiter.api.Assertions.*;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

import me.ohowe12.spectatormode.SpectatorMode;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.UUID;

class StateHolderTest {

    private ServerMock server;
    private StateHolder stateHolder;

    private PlayerMock playerMock;

    private File fileLocation;

    private SpectatorMode plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();

        plugin = MockBukkit.load(SpectatorMode.class);
        fileLocation = new File(plugin.getDataFolder(), "data.yml");

        stateHolder = plugin.getSpectatorManager().getStateHolder();

        playerMock = server.addPlayer("Player1");
    }

    @AfterEach
    void tearDown() {
        Bukkit.getScheduler().cancelTasks(plugin);
        MockBukkit.unmock();
    }

    @Test
    void addPlayer_ValidPlayerObject_ReportsHasPlayer() {
        stateHolder.addPlayer(playerMock);

        assertTrue(stateHolder.allPlayersInState().contains(playerMock.getUniqueId().toString()));
    }

    @Test
    void addPlayer_NullPlayer_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> stateHolder.addPlayer(null));
    }

    @Test
    void hasPlayer_ValidPlayerObjectNotInState_ReportsFalse() {
        assertFalse(stateHolder.hasPlayer(playerMock));
    }

    @Test
    void hasPlayer_ValidPlayerObjectInState_ReportsTrue() {
        stateHolder.addPlayer(playerMock);

        assertTrue(stateHolder.hasPlayer(playerMock));
    }

    @Test
    void hasPlayer_NullPlayer_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> stateHolder.hasPlayer((Player) null));
    }

    @Test
    void hasPlayer_NullUUID_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> stateHolder.hasPlayer((UUID) null));
    }

    @Test
    void hasPlayer_ValidUUIDNotInState_ReportsFalse() {
        assertFalse(stateHolder.hasPlayer(playerMock.getUniqueId()));
    }

    @Test
    void hasPlayer_ValidUUIDInState_ReportsTrue() {
        stateHolder.addPlayer(playerMock);

        assertTrue(stateHolder.hasPlayer(playerMock.getUniqueId()));
    }

    @Test
    void getPlayer_ValidPlayerObjectNotInState_Null() {
        assertNull(stateHolder.getPlayer(playerMock));
    }

    @Test
    void getPlayer_ValidPlayerObjectInState_NotNull() {
        stateHolder.addPlayer(playerMock);

        assertNotNull(stateHolder.getPlayer(playerMock));
    }

    @Test
    void getPlayer_ValidUUIDNotInState_Null() {
        assertNull(stateHolder.getPlayer(playerMock.getUniqueId()));
    }

    @Test
    void getPlayer_ValidUUIDInState_NotNull() {
        stateHolder.addPlayer(playerMock);

        assertNotNull(stateHolder.getPlayer(playerMock.getUniqueId()));
    }

    @Test
    void getPlayer_NullPlayer_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> stateHolder.getPlayer((Player) null));
    }

    @Test
    void getPlayer_NullUUID_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> stateHolder.getPlayer((UUID) null));
    }

    @Test
    void removePlayer_ValidPlayerInState_ReportsRemoved() {
        stateHolder.addPlayer(playerMock);

        stateHolder.removePlayer(playerMock);

        assertFalse(stateHolder.hasPlayer(playerMock));
    }

    @Test
    void removePlayer_ValidPlayerNotInState_LengthStaysTheSame() {
        PlayerMock player2 = server.addPlayer("Player2");

        stateHolder.addPlayer(playerMock);

        stateHolder.removePlayer(player2);

        assertEquals(1, stateHolder.allPlayersInState().size());
    }

    @Test
    void removePlayer_NullPlayer_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> stateHolder.removePlayer(null));
    }

    @Test
    void save_NoPlayers_EmptyFile() throws FileNotFoundException {
        stateHolder.save();

        Scanner reader = new Scanner(fileLocation);
        assertFalse(reader.hasNextLine());
        reader.close();
    }

    @Test
    void save_OnePlayer_HoldsPlayerData() {
        stateHolder.addPlayer(playerMock);

        stateHolder.save();

        ConfigurationSection dataSection =
                YamlConfiguration.loadConfiguration(fileLocation).getConfigurationSection("data");

        assertNotNull(dataSection);
        assertTrue(dataSection.contains(playerMock.getUniqueId().toString()));
        // Not a perfect test, but it works
    }

    @Test
    void load_OnePlayer_GetsCorrectPlayerData() {
        FileConfiguration configuration =
                YamlConfiguration.loadConfiguration(
                        new File("src/test/resources/data/oneplayerdata.yml"));
        stateHolder.load(configuration);

        assertEquals(1, stateHolder.allPlayersInState().size());

        State playerState =
                stateHolder.getPlayer(UUID.fromString("304ddf45-a2d1-4c73-94f4-210f2192b708"));
        assertEquals(300, playerState.getWaterBubbles());
        assertEquals(-20, playerState.getFireTicks());
        assertEquals(
                new Location(playerMock.getWorld(), 10, 30, 10, 10, 10),
                playerState.getPlayerLocation());
        assertEquals(3, playerState.getMobIds().size());
        assertFalse(playerState.getMobIds().get("edf02864-427b-4d91-b73b-8e3f8d189d56"));
        assertEquals(2, playerState.getPotionEffects().size());
    }

    @Test
    void load_OnePlayerLegacy_GetsCorrectMobData() {
        FileConfiguration configuration =
                YamlConfiguration.loadConfiguration(
                        new File("src/test/resources/data/oneplayerdatalegacy.yml"));
        stateHolder.load(configuration);

        assertEquals(1, stateHolder.allPlayersInState().size());

        State playerState =
                stateHolder.getPlayer(UUID.fromString("304ddf45-a2d1-4c73-94f4-210f2192b708"));
        assertEquals(3, playerState.getMobIds().size());
        assertFalse(playerState.getMobIds().get("edf02864-427b-4d91-b73b-8e3f8d189d56"));
    }
}
