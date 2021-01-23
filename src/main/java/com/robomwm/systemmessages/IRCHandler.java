package com.robomwm.systemmessages;

import com.cnaude.purpleirc.PurpleBot;
import com.cnaude.purpleirc.PurpleIRC;
import com.cnaude.purpleirc.ext.org.pircbotx.User;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.kitteh.vanish.event.VanishFakeJoinEvent;
import org.kitteh.vanish.event.VanishFakeQuitEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 12/22/2020.
 *
 * Prevents system messages being sent to IRC unless the player chats
 *
 * @author RoboMWM
 */
public class IRCHandler implements Listener
{
    private DataStore dataStore;
    private PurpleIRC purpleIRC;
    private Plugin plugin;

    public IRCHandler(Plugin plugin, Plugin purpleIRC, Plugin gp)
    {
        this.plugin = plugin;
        this.dataStore = ((GriefPrevention)gp).dataStore;
        this.purpleIRC = (PurpleIRC)purpleIRC;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void sendToIRC(String msg, boolean dontPing)
    {
        new BukkitRunnable()
        {
            String message = msg;
            @Override
            public void run()
            {
                Iterator<PurpleBot> bots = purpleIRC.ircBots.values().iterator();
                while (bots.hasNext())
                {
                    if (dontPing)
                    {
                        PurpleBot bot = bots.next();
                        try
                        {
                            StringBuilder messageBuilder = new StringBuilder(message);
                            for (User user : bot.getBot().getUserBot().getChannels().first().getUsers())
                            {
                                if (message.toLowerCase().contains(user.getNick().toLowerCase()) && !message.toLowerCase().contains(user.getNick().toLowerCase() + ".com"))
                                {
                                    plugin.getLogger().info("matched " + user.getNick());
                                    Matcher matcher = Pattern.compile("(?i)\\b" + user.getNick() + "\\b").matcher(message);
                                    for (int i = 1; matcher.find(); i++)
                                    {
                                        messageBuilder.insert(matcher.start() + i, "\u200B");
                                        plugin.getLogger().info("replaced position " + matcher.start() + " with offset " + i);
                                    }
                                    message = messageBuilder.toString();
                                }
                            }
                        }
                        catch (Throwable rock)
                        {
                            continue;
                        }
                    }
                    break;
                }

                //message = removeLineBreaks.matcher(message).replaceAll(" \u00B6 ");
                if (message.length() > 440)
                    message = message.substring(0, 440);
                final String finalMessage = message;
                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        for (PurpleBot bot : purpleIRC.ircBots.values())
                            bot.asyncIRCMessage("#TechFortress", finalMessage);
                    }
                }.runTaskAsynchronously(plugin);
            }
        }.runTaskAsynchronously(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event)
    {
        if (event.getJoinMessage() != null && !event.getJoinMessage().isEmpty() && recentlyLeft.remove(event.getPlayer().getUniqueId()))
            sendToIRC(ChatColor.GREEN + getWhitespacedName(event.getPlayer().getName()) + " rejoined", false);

        //always announce new player joins
        if (!event.getPlayer().hasPlayedBefore())
        {
            sendToIRC(event.getJoinMessage(), false);
            playerSentMessage.add(event.getPlayer()); //include disconnect message since we decided to bother people about the join
        }
    }

    @EventHandler
    public void onJoin(VanishFakeJoinEvent event)
    {
        onJoin(new PlayerJoinEvent(event.getPlayer(), "."));
    }

    private Set<Player> playerSentMessage = Collections.newSetFromMap(new ConcurrentHashMap<Player, Boolean>());
    private Set<UUID> recentlyLeft = new HashSet<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event)
    {
        if (!playerSentMessage.remove(event.getPlayer()))
            return;
        recentlyLeft.add(event.getPlayer().getUniqueId());

        sendToIRC(ChatColor.DARK_GRAY + getWhitespacedName(event.getPlayer().getName()) + " disconnected", false);
    }

    @EventHandler
    public void onQuit(VanishFakeQuitEvent event)
    {
        onQuit(new PlayerQuitEvent(event.getPlayer(), "."));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event)
    {
        if (dataStore.isSoftMuted(event.getPlayer().getUniqueId()))
            return;
        playerSentMessage.add(event.getPlayer());
    }

    public String getWhitespacedName(String name)
    {
        StringBuilder nameWithZeroWidthWhitespaceBuilder = new StringBuilder(name);
        return nameWithZeroWidthWhitespaceBuilder.insert(1, "\u200B").toString();
    }
}
