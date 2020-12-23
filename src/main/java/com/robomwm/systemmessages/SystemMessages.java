package com.robomwm.systemmessages;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created on 12/22/2020.
 *
 * @author RoboMWM
 */
public class SystemMessages extends JavaPlugin implements Listener
{
    @Override
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(this, this);
        Plugin purpleIRC = getServer().getPluginManager().getPlugin("PurpleIRC");
        Plugin gp = getServer().getPluginManager().getPlugin("GriefPrevention");
        if (gp != null && purpleIRC != null)
            new IRCHandler(this, purpleIRC, gp);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();

        //Returning player, send normal connect message and we're done
        if (player.hasPlayedBefore())
        {
            event.setJoinMessage(ChatColor.YELLOW + player.getName() + " joined Tech Fortress");
            return;
        }

        //New player, send welcome message
        event.setJoinMessage(ChatColor.LIGHT_PURPLE + "Welcome to Tech Fortress, " + player.getName() + "! Enjoy the near-vanilla experience!");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event)
    {
        event.setQuitMessage(ChatColor.YELLOW + event.getPlayer().getName() + " disconnected");
    }
}
