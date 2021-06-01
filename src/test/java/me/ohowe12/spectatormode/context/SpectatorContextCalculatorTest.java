package me.ohowe12.spectatormode.context;

import me.ohowe12.spectatormode.SpectatorManager;
import me.ohowe12.spectatormode.SpectatorMode;
import me.ohowe12.spectatormode.state.StateHolder;
import me.ohowe12.spectatormode.utils.mocks.ContextConsumerMock;
import net.luckperms.api.context.ContextConsumer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.mockito.Mockito.*;

class SpectatorContextCalculatorTest {

    private static SpectatorContextCalculator spectatorContextCalculator;

    private static SpectatorMode pluginMock;
    private static SpectatorManager spectatorManagerMock;
    private static StateHolder stateHolderMock;
    private static Player playerMock;
    private static ContextConsumer contextConsumerMock;

    @BeforeAll
    public static void setUp() {
        pluginMock = mock(SpectatorMode.class);
        spectatorManagerMock = mock(SpectatorManager.class);
        stateHolderMock = mock(StateHolder.class);
        playerMock = mock(Player.class);
        contextConsumerMock = mock(ContextConsumerMock.class);


        when(pluginMock.getSpectatorManager()).thenReturn(spectatorManagerMock);
        when(spectatorManagerMock.getStateHolder()).thenReturn(stateHolderMock);

        spectatorContextCalculator = new SpectatorContextCalculator(pluginMock);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testCalculate(boolean inState) {
        when(stateHolderMock.hasPlayer(playerMock)).thenReturn(inState);

        spectatorContextCalculator.calculate(playerMock, contextConsumerMock);
        verify(contextConsumerMock, times(1)).accept("SMP Spectator", String.valueOf(inState));
    }
}