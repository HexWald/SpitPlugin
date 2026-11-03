package hex.wald.spitplugin.service;

import hex.wald.spitplugin.SpitPlugin;
import hex.wald.spitplugin.config.SpitSettings;
import hex.wald.spitplugin.model.SpitType;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class SpitProjectileServiceTest {

    private final SpitPlugin plugin = mock(SpitPlugin.class);
    private final Player player = mock(Player.class);
    private final World world = mock(World.class);
    private final LlamaSpit spit = mock(LlamaSpit.class);
    private final BukkitScheduler scheduler = mock(BukkitScheduler.class);
    private final SpitSelectionService selection = new SpitSelectionService();
    private MockedStatic<Bukkit> bukkit;
    private SpitProjectileService service;

    @Before
    public void setUp() {
        bukkit = mockStatic(Bukkit.class);
        bukkit.when(() -> Bukkit.createBlockData(Material.LIGHT_GRAY_CONCRETE_POWDER)).thenReturn(mock(BlockData.class));
        bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
        when(scheduler.runTaskTimer(eq(plugin), any(Runnable.class), eq(0L), eq(1L))).thenReturn(mock(BukkitTask.class));
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getWorld()).thenReturn(world);
        when(player.getLocation()).thenAnswer(call -> location());
        when(player.getEyeLocation()).thenAnswer(call -> location());
        when(world.spawnEntity(any(Location.class), any())).thenReturn(spit);
        when(spit.getUniqueId()).thenReturn(UUID.randomUUID());
        when(spit.getWorld()).thenReturn(world);
        when(spit.getLocation()).thenAnswer(call -> location());
        when(spit.getVelocity()).thenReturn(new Vector(0, 0, 1));
        when(spit.isValid()).thenReturn(true);
        when(plugin.getSettings()).thenReturn(new SpitSettings(new YamlConfiguration()));
        service = new SpitProjectileService(plugin, selection);
    }

    @After
    public void tearDown() {
        bukkit.close();
    }

    @Test
    public void launchUsesConfiguredSpeedAndSound() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("speed.min", 2.0);
        config.set("speed.max", 2.0);
        config.set("sound.name", "ENTITY_ARROW_SHOOT");
        config.set("sound.volume", 0.5);
        config.set("sound.pitch", 1.5);
        when(plugin.getSettings()).thenReturn(new SpitSettings(config));

        service.launch(player);

        ArgumentCaptor<Vector> velocity = ArgumentCaptor.forClass(Vector.class);
        verify(spit).setVelocity(velocity.capture());
        assertEquals(2.0, velocity.getValue().length(), 0.00001);
        verify(spit).setShooter(player);
        verify(world).playSound(any(Location.class), eq(Sound.ENTITY_ARROW_SHOOT), eq(0.5f), eq(1.5f));
    }

    @Test
    public void damageUsesTheCurrentSettingsIncludingZero() {
        service.launch(player);
        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(spit);
        YamlConfiguration config = new YamlConfiguration();
        config.set("damage", 0);
        when(plugin.getSettings()).thenReturn(new SpitSettings(config));

        service.handleDamage(event);

        verify(event).setDamage(0.0);
    }

    @Test
    public void disabledSoundDoesNotPlay() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("sound.enabled", false);
        when(plugin.getSettings()).thenReturn(new SpitSettings(config));
        service.launch(player);
        verify(world, never()).playSound(any(Location.class), any(Sound.class), anyFloat(), anyFloat());
    }

    @Test
    public void zeroParticlesStillChecksForCollisions() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("particles.amount", 0);
        when(plugin.getSettings()).thenReturn(new SpitSettings(config));
        selection.setSelected(player.getUniqueId(), SpitType.DUST);
        service.launch(player);
        trail().run();

        verify(world).rayTrace(any(), any(), anyDouble(), any(), anyBoolean(), anyDouble(), any());
        verify(world, never()).spawnParticle(any(Particle.class), any(Location.class), anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), any());
    }

    @Test
    public void particleAmountAppliesToTheTrail() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("particles.amount", 2);
        when(plugin.getSettings()).thenReturn(new SpitSettings(config));
        selection.setSelected(player.getUniqueId(), SpitType.DUST);
        service.launch(player);
        trail().run();

        verify(world).spawnParticle(eq(Particle.ASH), any(Location.class), eq(8), anyDouble(), anyDouble(), anyDouble(), anyDouble(), isNull());
    }

    private Runnable trail() {
        ArgumentCaptor<Runnable> task = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).runTaskTimer(eq(plugin), task.capture(), eq(0L), eq(1L));
        return task.getValue();
    }

    private Location location() {
        return new Location(world, 0, 64, 0);
    }
}
