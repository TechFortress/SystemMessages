package com.robomwm.systemmessages;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

/**
 * Created on 12/22/2020.
 *
 * @author RoboMWM
 */
public class SystemMessages extends JavaPlugin implements Listener
{
    public String getJoinMessage(String playerName)
    {
        return playerName + " joined Tech Fortress";
    }

    public String getQuitMessage(String playerName)
    {
        return playerName + " disconnected";
    }

    @Override
    public void onEnable()
    {
        getConfig().addDefault("joinMOTD", "&aTech Fortress");
        getConfig().addDefault("pingMOTD", "&aTech Fortress");
        getServer().getPluginManager().registerEvents(this, this);
        Plugin purpleIRC = getServer().getPluginManager().getPlugin("PurpleIRC");
        Plugin gp = getServer().getPluginManager().getPlugin("GriefPrevention");
        if (gp != null && purpleIRC != null)
            new IRCHandler(this, purpleIRC, gp);
    }

    @EventHandler
    public void ping(ServerListPingEvent event)
    {
        event.setMotd(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(getConfig().getString("pingMOTD", ""))));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();

        //send MOTD
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(getConfig().getString("joinMOTD", ""))));

        //Returning player, send normal connect message
        if (player.hasPlayedBefore())
            event.setJoinMessage(ChatColor.YELLOW + getJoinMessage(event.getPlayer().getName()));
        //New player, send welcome message
        else
        {
            event.setJoinMessage(ChatColor.LIGHT_PURPLE + "Welcome to Tech Fortress, " + player.getName() + "! Enjoy the near-vanilla experience!");
            //Fixes player spawning underneath world due to unloaded spawn chunks
            new BukkitRunnable()
            {
                public void run()
                {
                    if (player.isOnline())
                        player.teleport(player.getWorld().getSpawnLocation());
                }
            }.runTaskLater(this, 2L);
        }

        //reset header
        player.setPlayerListHeaderFooter("Tech Fortress", "tf.robomwm.com");
        player.setPlayerListHeaderFooter((String)null, null);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event)
    {
        event.setQuitMessage(ChatColor.YELLOW + getQuitMessage(event.getPlayer().getName()));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (command.getName().equalsIgnoreCase("setmotd"))
        {
            String motdColored = ChatColor.translateAlternateColorCodes('&', String.join(" ", args));
            getConfig().set("joinMotd", motdColored);
            saveConfig();
            sender.sendMessage(motdColored);
            return true;
        }
        else if (command.getName().equalsIgnoreCase("setpingmotd"))
        {
            String motdColored = ChatColor.translateAlternateColorCodes('&', String.join(" ", args));
            getConfig().set("pingMotd", motdColored);
            saveConfig();
            sender.sendMessage(motdColored);
            return true;
        }
        return false;
    }
}
