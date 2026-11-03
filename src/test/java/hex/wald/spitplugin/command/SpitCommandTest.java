package hex.wald.spitplugin.command;

import hex.wald.spitplugin.SpitPlugin;
import hex.wald.spitplugin.service.SpitProjectileService;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SpitCommandTest {

    private final SpitPlugin plugin = mock(SpitPlugin.class);
    private final SpitProjectileService projectiles = mock(SpitProjectileService.class);
    private final SpitCommand command = new SpitCommand(plugin, projectiles);

    @Test
    public void consoleCanReloadWithItsOwnPermission() {
        CommandSender console = mock(CommandSender.class);
        when(console.hasPermission("spit.reload")).thenReturn(true);
        when(plugin.reloadSettings()).thenReturn(true);
        assertTrue(command.onCommand(console, null, "spit", new String[] {"reload"}));
        verify(plugin).reloadSettings();
        verify(console).sendMessage(ChatColor.GREEN + "Spit settings reloaded.");
        verifyNoInteractions(projectiles);
    }

    @Test
    public void usePermissionDoesNotAllowReloading() {
        Player player = mock(Player.class);
        when(player.hasPermission("spit.use")).thenReturn(true);
        command.onCommand(player, null, "spit", new String[] {"reload"});
        verify(player).sendMessage(ChatColor.RED + "No Permission.");
        verifyNoInteractions(plugin, projectiles);
    }

    @Test
    public void aFailedReloadIsReportedWithoutFiringASpit() {
        Player player = mock(Player.class);
        when(player.hasPermission("spit.reload")).thenReturn(true);
        when(plugin.reloadSettings()).thenReturn(false);
        command.onCommand(player, null, "spit", new String[] {"RELOAD"});
        verify(player).sendMessage(ChatColor.RED + "Could not reload config.yml. Previous settings are still active. Check the console.");
        verifyNoInteractions(projectiles);
    }

    @Test
    public void unknownAndExtraArgumentsDoNotFireASpit() {
        Player player = mock(Player.class);
        when(player.hasPermission("spit.use")).thenReturn(true);
        assertFalse(command.onCommand(player, null, "spit", new String[] {"unknown"}));
        assertFalse(command.onCommand(player, null, "spit", new String[] {"reload", "extra"}));
        verifyNoInteractions(plugin, projectiles);
    }

    @Test
    public void firingStillNeedsUsePermission() {
        Player player = mock(Player.class);
        command.onCommand(player, null, "spit", new String[0]);
        verifyNoInteractions(projectiles);
        when(player.hasPermission("spit.use")).thenReturn(true);
        command.onCommand(player, null, "spit", new String[0]);
        verify(projectiles).launch(player);
    }

    @Test
    public void consoleCannotFireASpit() {
        CommandSender console = mock(CommandSender.class);
        command.onCommand(console, null, "spit", new String[0]);
        verify(console).sendMessage("Only players can use this command.");
        verifyNoInteractions(projectiles);
    }

    @Test
    public void reloadCompletionNeedsPermissionAndAMatchingPrefix() {
        Player player = mock(Player.class);
        assertTrue(command.onTabComplete(player, null, "spit", new String[] {"r"}).isEmpty());
        when(player.hasPermission("spit.reload")).thenReturn(true);
        assertEquals("reload", command.onTabComplete(player, null, "spit", new String[] {"RE"}).get(0));
        assertTrue(command.onTabComplete(player, null, "spit", new String[] {"x"}).isEmpty());
        assertTrue(command.onTabComplete(player, null, "spit", new String[] {"reload", "x"}).isEmpty());
    }
}
