package com.github.zathrus_writer.commandsex;

import static com.github.zathrus_writer.commandsex.Language._;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.zathrus_writer.commandsex.commands.Command_cex_tpa;
import com.github.zathrus_writer.commandsex.commands.Command_cex_tpahere;
import com.github.zathrus_writer.commandsex.handlers.Handler_economypromote;
import com.github.zathrus_writer.commandsex.handlers.Handler_playtimepromote;
import com.github.zathrus_writer.commandsex.helpers.Commands;
import com.github.zathrus_writer.commandsex.helpers.LogHelper;
import com.github.zathrus_writer.commandsex.helpers.Permissions;
import com.github.zathrus_writer.commandsex.helpers.Utils;

public class CexCommands {
	
	protected static String[] unconfigurables = {"enableDatabase", "sqlType", "database", "host", "port", "name", "password", "prefix", "chatReplaceFile", "playerCommandsReplaceFile", "consoleCommandsReplaceFile", "replacements", "xmppUser", "xmppHost", "xmppPassword", "xmppRoom.name", "xmppRoom.password", "xmppBotNick", "xmppCommandPrefix", "xmppAdmins", "timedPromote", "ecoPromote", "quizDiff", "quizzes", "kits", "deathGroupChanges"};
	
	/***
	 * Handles reactions on the /cex command.
	 * @param sender
	 * @param alias
	 * @param args
	 * @return
	 */
	public static Boolean handle_cex(CommandsEX p, CommandSender sender, String alias, String[] args) {
		int aLength = args.length;

		// normalize arguments
		if (aLength > 0) {
			for (int i = 0; i < aLength; i++) {
				args[i] = args[i].toLowerCase();
			}
		}
		
		if (aLength == 0) {

			/***
			 * VERSION
			 */

			if (!p.getConfig().getBoolean("disableVersion")) {
				sender.sendMessage(ChatColor.YELLOW + CommandsEX.pdfFile.getName() + ", " + _("version", sender.getName()) + " " + CommandsEX.pdfFile.getVersion());
			}
		} else if ((aLength == 1) && args[0].equals("null")) {
			// does nothing, prints nothing - used for commands replacements/aliasing
		} else if ((aLength == 1) && args[0].equals("reload")) {

			/***
			 * RELOAD
			 */
			
			if (sender.getName().toLowerCase().equals("console") || ((sender instanceof Player) && Permissions.checkPerms((Player)sender, "cex.reload"))) {
				p.reloadConfig();
				sender.sendMessage(ChatColor.GREEN + _("configReloaded", sender.getName()));
			} else {
				LogHelper.logWarning("["+ CommandsEX.pdfFile.getName() +"]: Player " + sender.getName() + " tried to execute reload command without permission.");
			}
		} else if ((aLength == 1) && (args[0].equals("?") || args[0].equals("help"))) {

			/***
			 * USAGE HELP REQUEST
			 */
			Commands.showCommandHelpAndUsage(sender, "cex", "cex");
		} else if ((aLength < 3) && args[0].equals("config")) {
			
			/***
			 * SHOWING ALL AVAILABLE OPTIONS
			 */
			Set<String> s = p.getConfig().getKeys(false);
			Set<String> opts = new HashSet<String>();
			for (String ss : s) {
				Boolean canAdd = true;
				for (String u : unconfigurables) {
					if (u.equals(ss)) {
						canAdd = false;
						break;
					}
				}

				if (canAdd) {
					opts.add(ss);
				}
			}
			sender.sendMessage(ChatColor.WHITE + _("configAvailableNodes", sender.getName()) + opts.toString());
			sender.sendMessage(ChatColor.WHITE + _("configAvailableNodesUsage", sender.getName()));
		} else if (
					((aLength >= 3) && args[0].equals("config"))
					||
					((aLength >= 2) && (args[0].equals("cs") || args[0].equals("cg")))
				) {
			
			/***
			 * CONFIGURATION GETTING / SETTING
			 */
			
			if (!args[1].equals("get") && !args[1].equals("set") && !args[0].equals("cs") && !args[0].equals("cg")) {
				// unrecognized config action
				LogHelper.showWarning("configUnrecognizedAction", sender);
			} else {
				if (args[1].equals("get") || args[0].equals("cg")) {
					
					/***
					 * GETTING CONFIG VALUES
					 */
					String v = (args[0].equals("cg") ? args[1].toLowerCase() : args[2].toLowerCase());
					if (v.equals("disableversion")) {
						sender.sendMessage(ChatColor.YELLOW + _("configVersionDisableStatus", sender.getName()) + (!p.getConfig().getBoolean("disableVersion") ? ChatColor.GREEN + _("configStatusTrue", sender.getName()) : ChatColor.RED + _("configStatusFalse", sender.getName())));
					} else if (v.equals("logcommands")) {
						sender.sendMessage(ChatColor.YELLOW + _("configCommandsLoggingStatus", sender.getName()) + (p.getConfig().getBoolean("logCommands") ? ChatColor.GREEN + _("configStatusTrue", sender.getName()) : ChatColor.RED + _("configStatusFalse", sender.getName())));
					} else if (v.equals("defaultlang")) {
						sender.sendMessage(ChatColor.YELLOW + _("configDefaultLang", sender.getName()) + p.getConfig().getString("defaultLang"));
					} else if (v.equals("tpatimeout")) {
						sender.sendMessage(ChatColor.YELLOW + _("configTpaTimeout", sender.getName()) + p.getConfig().getString("tpaTimeout"));
					} else if (v.equals("tpaheretimeout")) {
						sender.sendMessage(ChatColor.YELLOW + _("configTpahereTimeout", sender.getName()) + p.getConfig().getString("tpahereTimeout"));
					} else if (v.equals("debugmode")) {
						sender.sendMessage(ChatColor.YELLOW + _("configDebugMode", sender.getName()) + p.getConfig().getString("debugMode"));
					} else if (v.equals("commandcooldowntime")) {
						sender.sendMessage(ChatColor.YELLOW + _("configCommandCooldownTime", sender.getName()) + p.getConfig().getInt("commandCooldownTime") + " " + _("seconds", sender.getName()));
					} else if (v.equals("homequalifytime")) {
						sender.sendMessage(ChatColor.YELLOW + _("configHomeQualifyTime", sender.getName()) + p.getConfig().getInt("homeQualifyTime") + " " + _("seconds", sender.getName()));
					} else if (v.equals("allowmultiworldhomes")) {
						sender.sendMessage(ChatColor.YELLOW + _("configAllowMultiworlds", sender.getName()) + (p.getConfig().getBoolean("allowMultiworldHomes") ? ChatColor.GREEN + _("configStatusTrue", sender.getName()) : ChatColor.RED + _("configStatusFalse", sender.getName())));
					} else if (v.equals("silentkicks")) {
						sender.sendMessage(ChatColor.YELLOW + _("configSilentKicks", sender.getName()) + (!p.getConfig().getBoolean("silentKicks") ? ChatColor.GREEN + _("configStatusTrue", sender.getName()) : ChatColor.RED + _("configStatusFalse", sender.getName())));
					} else if (v.equals("weathernotifyenabled")) {
						sender.sendMessage(ChatColor.YELLOW + _("configWeatherNotifyEnabled", sender.getName()) + (p.getConfig().getBoolean("weatherNotifyEnabled") ? ChatColor.GREEN + _("configStatusTrue", sender.getName()) : ChatColor.RED + _("configStatusFalse", sender.getName())));
					} else if (v.equals("maxwarpsperplayer")) {
						sender.sendMessage(ChatColor.YELLOW + _("configMaxWarpsPerPlayer", sender.getName()) + p.getConfig().getString("maxWarpsPerPlayer"));
					} else if (v.equals("maxipholdtime")) {
						sender.sendMessage(ChatColor.YELLOW + _("configMaxIPholdTime", sender.getName()) + p.getConfig().getString("maxIPholdTime"));
					} else if (v.equals("mintempbanswarn")) {
						sender.sendMessage(ChatColor.YELLOW + _("configMinTempBansWarn", sender.getName()) + p.getConfig().getString("minTempBansWarn"));
					} else if (v.equals("silentbans")) {
						sender.sendMessage(ChatColor.YELLOW + _("configSilentBans", sender.getName()) + (!p.getConfig().getBoolean("silentBans") ? ChatColor.GREEN + _("configStatusTrue", sender.getName()) : ChatColor.RED + _("configStatusFalse", sender.getName())));
					} else if (v.equals("slappreventdamage")) {
						sender.sendMessage(ChatColor.YELLOW + _("configSlapPreventDamage", sender.getName()) + (p.getConfig().getBoolean("slapPreventDamage") ? ChatColor.GREEN + _("configStatusTrue", sender.getName()) : ChatColor.RED + _("configStatusFalse", sender.getName())));
					} else if (v.equals("joinsilenttime")) {
						sender.sendMessage(ChatColor.YELLOW + _("configJoinSilentTime", sender.getName()) + p.getConfig().getString("joinSilentTime") + " " + _("seconds", sender.getName()));
					} else if (v.equals("jailarea")) {
						sender.sendMessage(ChatColor.YELLOW + _("configJailArea", sender.getName()) + p.getConfig().getString("jailArea") + " " + _("blocks", sender.getName()));
					} else if (v.equals("kamikazeinstakill")) {
						sender.sendMessage(ChatColor.YELLOW + _("configKamikazeInstaKill", sender.getName()) + (p.getConfig().getBoolean("kamikazeInstaKill") ? ChatColor.GREEN + _("configStatusTrue", sender.getName()) : ChatColor.RED + _("configStatusFalse", sender.getName())));
					} else if (v.equals("kamikazetimeout")) {
						sender.sendMessage(ChatColor.YELLOW + _("configKamikazeTimeout", sender.getName()) + p.getConfig().getString("kamikazeTimeout") + " " + _("seconds", sender.getName()));
					} else if (v.equals("timedpromotetasktime")) {
						sender.sendMessage(ChatColor.YELLOW + _("configTimedPromoteTaskTime", sender.getName()) + p.getConfig().getString("timedPromoteTaskTime") + " " + _("seconds", sender.getName()));
					} else if (v.equals("ecopromotetasktime")) {
						sender.sendMessage(ChatColor.YELLOW + _("configEcoPromoteTaskTime", sender.getName()) + p.getConfig().getString("ecoPromoteTaskTime") + " " + _("seconds", sender.getName()));
					}  else if (v.equals("ecopromoteautodemote")) {
						sender.sendMessage(ChatColor.YELLOW + _("configEcoPromoteAutoDemote", sender.getName()) + (p.getConfig().getBoolean("ecoPromoteAutoDemote") ? ChatColor.GREEN + _("configStatusTrue", sender.getName()) : ChatColor.RED + _("configStatusFalse", sender.getName())));
					} else if (v.equals("ecopromoteexclude")) {
						sender.sendMessage(ChatColor.YELLOW + _("configEcoPromoteExclude", sender.getName()) + Utils.implode(p.getConfig().getList("ecoPromoteTaskTime"), ", "));
					} else if (v.equals("timedpromoteexclude")) {
						sender.sendMessage(ChatColor.YELLOW + _("configTimedPromoteExclude", sender.getName()) + Utils.implode(p.getConfig().getList("timedPromoteTaskTime"), ", "));
					} else if (v.equals("privatemsgcommands")) {
						sender.sendMessage(ChatColor.YELLOW + _("configPrivateMsgCommands", sender.getName()) + Utils.implode(p.getConfig().getList("privateMsgCommands"), ", "));
					} else if (v.equals("quizrepeattime")) {
						sender.sendMessage(ChatColor.YELLOW + _("configQuizRepeatTime", sender.getName()) + p.getConfig().getInt("quizRepeatTime") + " " + _("seconds", sender.getName()));
					} else if (v.equals("quizdelay")) {
						sender.sendMessage(ChatColor.YELLOW + _("configQuizDelay", sender.getName()) + p.getConfig().getInt("quizDelay") + " " + _("seconds", sender.getName()));
					} else if (v.equals("quizduration")) {
						sender.sendMessage(ChatColor.YELLOW + _("configQuizDuration", sender.getName()) + p.getConfig().getInt("quizDuration") + " " + _("seconds", sender.getName()));
					} else if (v.equals("weathernotifytime")) {
						sender.sendMessage(ChatColor.YELLOW + _("configWeatherNotifyTime", sender.getName()) + p.getConfig().getInt("weatherNotifyTime") + " " + _("seconds", sender.getName()));
					} else if (v.equals("defaultslapheight")) {
						sender.sendMessage(ChatColor.YELLOW + _("configDefaultSlapHeight", sender.getName()) + p.getConfig().getInt("defaultSlapHeight") + " " + _("blocks", sender.getName()));
					} else if (v.equals("deathgroupscleanuptime")) {
						sender.sendMessage(ChatColor.YELLOW + _("configDeathGroupsCleanupTime", sender.getName()) + p.getConfig().getInt("deathGroupsCleanupTime") + " " + _("days", sender.getName()));
					} else if (v.equals("motd")) {
						sender.sendMessage(ChatColor.YELLOW + _("configMotd", sender.getName()) + p.getConfig().getString("motd"));
					} else if (v.equals("motdnewplayer")) {
						sender.sendMessage(ChatColor.YELLOW + _("configMotdNewPlayer", sender.getName()) + p.getConfig().getString("motdNewPlayer"));
					} else {
						LogHelper.showWarning("configUnrecognized", sender);
					}
				} else if (args[1].equals("set") || args[0].equals("cs")) {
					
					/***
					 * SETTING CONFIG VALUES
					 */
					String v = (args[0].equals("cs") ? args[1].toLowerCase() : args[2].toLowerCase());
					if (v.equals("disableversion")) {
						p.getConfig().set("disableVersion", !p.getConfig().getBoolean("disableVersion"));
						p.saveConfig();
						sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + (p.getConfig().getBoolean("disableVersion") ? ChatColor.GREEN + _("configStatusTrue", sender.getName()) : ChatColor.RED + _("configStatusFalse", sender.getName())));
					} else if (v.equals("logcommands")) {
						p.getConfig().set("logCommands", !p.getConfig().getBoolean("logCommands"));
						p.saveConfig();
						sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + (p.getConfig().getBoolean("logCommands") ? ChatColor.GREEN + _("configStatusTrue", sender.getName()) : ChatColor.RED + _("configStatusFalse", sender.getName())));
					} else if (v.equals("defaultlang")) {
						if ((aLength > 2) && args[2] != null) {
							p.getConfig().set("defaultLang", args[2]);
							p.saveConfig();
							Language.defaultLocale = args[2];
							sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + ChatColor.WHITE + p.getConfig().getString("defaultLang"));
						} else {
							LogHelper.showWarnings(sender, "configUnspecifiedError1", "configUnspecifiedError2", "configUnspecifiedError3");
						}
					} else if (v.equals("tpatimeout")) {
						if ((aLength > 2) && args[2] != null) {
							if (args[2].matches(CommandsEX.intRegex)) {
								p.getConfig().set("tpaTimeout", args[2]);
								p.saveConfig();
								try {
									Command_cex_tpa.tTimeout = Integer.parseInt(args[2]);
								} catch (Throwable e) {
									// the tpa command might not be present in the plugin
								}
								sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + ChatColor.WHITE + p.getConfig().getString("tpaTimeout"));
							} else {
								// timeout not numeric
								LogHelper.showWarning("configProvideNumericValue", sender);
							}
						} else {
							LogHelper.showWarnings(sender, "configUnspecifiedError1", "configUnspecifiedError2", "configUnspecifiedError3");
						}
					} else if (v.equals("tpaheretimeout")) {
						if ((aLength > 2) && args[2] != null) {
							if (args[2].matches(CommandsEX.intRegex)) {
								p.getConfig().set("tpahereTimeout", args[2]);
								p.saveConfig();
								try {
									Command_cex_tpahere.tTimeout = Integer.parseInt(args[2]);
								} catch (Throwable e) {
									// the tpahere command might not be present in the plugin
								}
								sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + ChatColor.WHITE + p.getConfig().getString("tpahereTimeout"));
							} else {
								// timeout not numeric
								LogHelper.showWarning("configProvideNumericValue", sender);
							}
						} else {
							LogHelper.showWarnings(sender, "configUnspecifiedError1", "configUnspecifiedError2", "configUnspecifiedError3");
						}
					} else if (v.equals("commandcooldowntime")) {
						if ((aLength > 2) && args[2] != null) {
							if (args[2].matches(CommandsEX.intRegex)) {
								p.getConfig().set("commandCooldownTime", args[2]);
								p.saveConfig();
								sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + ChatColor.WHITE + p.getConfig().getString("commandCooldownTime"));
							} else {
								// timeout not numeric
								LogHelper.showWarning("configProvideNumericValue", sender);
							}
						} else {
							LogHelper.showWarnings(sender, "configUnspecifiedError1", "configUnspecifiedError2", "configUnspecifiedError3");
						}
					} else if (v.equals("homequalifytime")) {
						if ((aLength > 2) && args[2] != null) {
							if (args[2].matches(CommandsEX.intRegex)) {
								p.getConfig().set("homeQualifyTime", args[2]);
								p.saveConfig();
								sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + ChatColor.WHITE + p.getConfig().getString("homeQualifyTime"));
							} else {
								// timeout not numeric
								LogHelper.showWarning("configProvideNumericValue", sender);
							}
						} else {
							LogHelper.showWarnings(sender, "configUnspecifiedError1", "configUnspecifiedError2", "configUnspecifiedError3");
						}
					} else if (v.equals("quizrepeattime")) {
						if ((aLength > 2) && args[2] != null) {
							if (args[2].matches(CommandsEX.intRegex)) {
								p.getConfig().set("quizRepeatTime", args[2]);
								p.saveConfig();
								sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + ChatColor.WHITE + p.getConfig().getString("quizRepeatTime"));
							} else {
								// timeout not numeric
								LogHelper.showWarning("configProvideNumericValue", sender);
							}
						} else {
							LogHelper.showWarnings(sender, "configUnspecifiedError1", "configUnspecifiedError2", "configUnspecifiedError3");
						}
					} else if (v.equals("quizdelay")) {
						if ((aLength > 2) && args[2] != null) {
							if (args[2].matches(CommandsEX.intRegex)) {
								p.getConfig().set("quizDelay", args[2]);
								p.saveConfig();
								sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + ChatColor.WHITE + p.getConfig().getString("quizDelay"));
							} else {
								// timeout not numeric
								LogHelper.showWarning("configProvideNumericValue", sender);
							}
						} else {
							LogHelper.showWarnings(sender, "configUnspecifiedError1", "configUnspecifiedError2", "configUnspecifiedError3");
						}
					} else if (v.equals("quizduration")) {
						if ((aLength > 2) && args[2] != null) {
							if (args[2].matches(CommandsEX.intRegex)) {
								p.getConfig().set("quizDuration", args[2]);
								p.saveConfig();
								sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + ChatColor.WHITE + p.getConfig().getString("quizDuration"));
							} else {
								// timeout not numeric
								LogHelper.showWarning("configProvideNumericValue", sender);
							}
						} else {
							LogHelper.showWarnings(sender, "configUnspecifiedError1", "configUnspecifiedError2", "configUnspecifiedError3");
						}
					} else if (v.equals("weathernotifytime")) {
						if ((aLength > 2) && args[2] != null) {
							if (args[2].matches(CommandsEX.intRegex)) {
								p.getConfig().set("weatherNotifyTime", args[2]);
								p.saveConfig();
								sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + ChatColor.WHITE + p.getConfig().getString("weatherNotifyTime"));
							} else {
								// timeout not numeric
								LogHelper.showWarning("configProvideNumericValue", sender);
							}
						} else {
							LogHelper.showWarnings(sender, "configUnspecifiedError1", "configUnspecifiedError2", "configUnspecifiedError3");
						}
					} else if (v.equals("defaultslapheight")) {
						if ((aLength > 2) && args[2] != null) {
							if (args[2].matches(CommandsEX.intRegex)) {
								p.getConfig().set("defaultSlapHeight", args[2]);
								p.saveConfig();
								sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + ChatColor.WHITE + p.getConfig().getString("defaultSlapHeight"));
							} else {
								// timeout not numeric
								LogHelper.showWarning("configProvideNumericValue", sender);
							}
						} else {
							LogHelper.showWarnings(sender, "configUnspecifiedError1", "configUnspecifiedError2", "configUnspecifiedError3");
						}
					} else if (v.equals("maxwarpsperplayer")) {
						if ((aLength > 2) && args[2] != null) {
							if (args[2].matches(CommandsEX.intRegex)) {
								p.getConfig().set("maxWarpsPerPlayer", args[2]);
								p.saveConfig();
								sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + ChatColor.WHITE + p.getConfig().getString("maxWarpsPerPlayer"));
							} else {
								// timeout not numeric
								LogHelper.showWarning("configProvideNumericValue", sender);
							}
						} else {
							LogHelper.showWarnings(sender, "configUnspecifiedError1", "configUnspecifiedError2", "configUnspecifiedError3");
						}
					} else if (v.equals("maxipholdtime")) {
						if ((aLength > 2) && args[2] != null) {
							if (args[2].matches(CommandsEX.intRegex)) {
								p.getConfig().set("maxIPholdTime", args[2]);
								p.saveConfig();
								sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + ChatColor.WHITE + p.getConfig().getString("maxIPholdTime"));
							} else {
								// timeout not numeric
								LogHelper.showWarning("configProvideNumericValue", sender);
							}
						} else {
							LogHelper.showWarnings(sender, "configUnspecifiedError1", "configUnspecifiedError2", "configUnspecifiedError3");
						}
					} else if (v.equals("mintempbanswarn")) {
						if ((aLength > 2) && args[2] != null) {
							if (args[2].matches(CommandsEX.intRegex)) {
								p.getConfig().set("minTempBansWarn", args[2]);
								p.saveConfig();
								sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + ChatColor.WHITE + p.getConfig().getString("minTempBansWarn"));
							} else {
								// timeout not numeric
								LogHelper.showWarning("configProvideNumericValue", sender);
							}
						} else {
							LogHelper.showWarnings(sender, "configUnspecifiedError1", "configUnspecifiedError2", "configUnspecifiedError3");
						}
					} else if (v.equals("joinsilenttime")) {
						if ((aLength > 2) && args[2] != null) {
							if (args[2].matches(CommandsEX.intRegex)) {
								p.getConfig().set("joinSilentTime", args[2]);
								p.saveConfig();
								sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + ChatColor.WHITE + p.getConfig().getString("joinSilentTime"));
							} else {
								// timeout not numeric
								LogHelper.showWarning("configProvideNumericValue", sender);
							}
						} else {
							LogHelper.showWarnings(sender, "configUnspecifiedError1", "configUnspecifiedError2", "configUnspecifiedError3");
						}
					} else if (v.equals("jailarea")) {
						if ((aLength > 2) && args[2] != null) {
							if (args[2].matches(CommandsEX.intRegex)) {
								p.getConfig().set("jailArea", args[2]);
								p.saveConfig();
								sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + ChatColor.WHITE + p.getConfig().getString("jailArea"));
							} else {
								// area not numeric
								LogHelper.showWarning("configProvideNumericValue", sender);
							}
						} else {
							LogHelper.showWarnings(sender, "configUnspecifiedError1", "configUnspecifiedError2", "configUnspecifiedError3");
						}
					} else if (v.equals("kamikazetimeout")) {
						if ((aLength > 2) && args[2] != null) {
							if (args[2].matches(CommandsEX.intRegex)) {
								p.getConfig().set("kamikazeTimeout", args[2]);
								p.saveConfig();
								sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + ChatColor.WHITE + p.getConfig().getString("kamikazeTimeout"));
							} else {
								// timeout not numeric
								LogHelper.showWarning("configProvideNumericValue", sender);
							}
						} else {
							LogHelper.showWarnings(sender, "configUnspecifiedError1", "configUnspecifiedError2", "configUnspecifiedError3");
						}
					} else if (v.equals("deathgroupscleanuptime")) {
						if ((aLength > 2) && args[2] != null) {
							if (args[2].matches(CommandsEX.intRegex)) {
								p.getConfig().set("deathGroupsCleanupTime", args[2]);
								p.saveConfig();
								sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + ChatColor.WHITE + p.getConfig().getString("deathGroupsCleanupTime"));
							} else {
								// timeout not numeric
								LogHelper.showWarning("configProvideNumericValue", sender);
							}
						} else {
							LogHelper.showWarnings(sender, "configUnspecifiedError1", "configUnspecifiedError2", "configUnspecifiedError3");
						}
					} else if (v.equals("timedpromotetasktime")) {
						if ((aLength > 2) && args[2] != null) {
							if (args[2].matches(CommandsEX.intRegex)) {
								p.getConfig().set("timedPromoteTaskTime", args[2]);
								p.saveConfig();

								// cancel old task and create a new one with this new timeout value
								try {
									CommandsEX.plugin.getServer().getScheduler().cancelTask(Handler_playtimepromote.promotionTaskID);
									Handler_playtimepromote.promotionTaskID = CommandsEX.plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(CommandsEX.plugin, new Runnable() {
										@Override
										public void run() {
											// create ExecutorService to manage threads                        
											ExecutorService threadExecutor = Executors.newFixedThreadPool(1);
											threadExecutor.execute(new Runnable() {
												@Override
												public void run() {
													Handler_playtimepromote.checkTimedPromotions();
												}
											});
											threadExecutor.shutdown(); // shutdown worker threads
										}
									}, (20 * Integer.parseInt(args[2])), (20 * Integer.parseInt(args[2])));
								} catch (Throwable ex) {}
								
								// show message
								sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + ChatColor.WHITE + p.getConfig().getString("timedPromoteTaskTime"));
							} else {
								// timeout not numeric
								LogHelper.showWarning("configProvideNumericValue", sender);
							}
						} else {
							LogHelper.showWarnings(sender, "configUnspecifiedError1", "configUnspecifiedError2", "configUnspecifiedError3");
						}
					} else if (v.equals("ecopromotetasktime")) {
						if ((aLength > 2) && args[2] != null) {
							if (args[2].matches(CommandsEX.intRegex)) {
								p.getConfig().set("ecoPromoteTaskTime", args[2]);
								p.saveConfig();

								// cancel old task and create a new one with this new timeout value
								try {
									CommandsEX.plugin.getServer().getScheduler().cancelTask(Handler_economypromote.promotionTaskID);
									Handler_economypromote.promotionTaskID = CommandsEX.plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(CommandsEX.plugin, new Runnable() {
										@Override
										public void run() {
											// create ExecutorService to manage threads                        
											ExecutorService threadExecutor = Executors.newFixedThreadPool(1);
											threadExecutor.execute(new Runnable() {
												@Override
												public void run() {
													Handler_economypromote.checkTimedPromotions();
												}
											});
											threadExecutor.shutdown(); // shutdown worker threads
										}
									}, (20 * Integer.parseInt(args[2])), (20 * Integer.parseInt(args[2])));
								} catch (Throwable ex) {}
								
								// show message
								sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + ChatColor.WHITE + p.getConfig().getString("ecoPromoteTaskTime"));
							} else {
								// timeout not numeric
								LogHelper.showWarning("configProvideNumericValue", sender);
							}
						} else {
							LogHelper.showWarnings(sender, "configUnspecifiedError1", "configUnspecifiedError2", "configUnspecifiedError3");
						}
					} else if (v.equals("allowmultiworldhomes")) {
						p.getConfig().set("allowMultiworldHomes", !p.getConfig().getBoolean("allowMultiworldHomes"));
						p.saveConfig();
						sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + (p.getConfig().getBoolean("allowMultiworldHomes") ? ChatColor.GREEN + _("configStatusTrue", sender.getName()) : ChatColor.RED + _("configStatusFalse", sender.getName())));
					} else if (v.equals("silentkicks")) {
						p.getConfig().set("silentKicks", !p.getConfig().getBoolean("silentKicks"));
						p.saveConfig();
						sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + (p.getConfig().getBoolean("silentKicks") ? ChatColor.GREEN + _("configStatusTrue", sender.getName()) : ChatColor.RED + _("configStatusFalse", sender.getName())));
					} else if (v.equals("silentbans")) {
						p.getConfig().set("silentBans", !p.getConfig().getBoolean("silentBans"));
						p.saveConfig();
						sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + (p.getConfig().getBoolean("silentBans") ? ChatColor.GREEN + _("configStatusTrue", sender.getName()) : ChatColor.RED + _("configStatusFalse", sender.getName())));
					} else if (v.equals("kamikazeinstakill")) {
						p.getConfig().set("kamikazeInstaKill", !p.getConfig().getBoolean("kamikazeInstaKill"));
						p.saveConfig();
						sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + (p.getConfig().getBoolean("kamikazeInstaKill") ? ChatColor.GREEN + _("configStatusTrue", sender.getName()) : ChatColor.RED + _("configStatusFalse", sender.getName())));
					} else if (v.equals("weathernotifyenabled")) {
						p.getConfig().set("weatherNotifyEnabled", !p.getConfig().getBoolean("weatherNotifyEnabled"));
						p.saveConfig();
						sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + (p.getConfig().getBoolean("weatherNotifyEnabled") ? ChatColor.GREEN + _("configStatusTrue", sender.getName()) : ChatColor.RED + _("configStatusFalse", sender.getName())));
					} else if (v.equals("slappreventdamage")) {
						p.getConfig().set("slapPreventDamage", !p.getConfig().getBoolean("slapPreventDamage"));
						p.saveConfig();
						sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + (p.getConfig().getBoolean("slapPreventDamage") ? ChatColor.GREEN + _("configStatusTrue", sender.getName()) : ChatColor.RED + _("configStatusFalse", sender.getName())));
					} else if (v.equals("debugmode")) {
						p.getConfig().set("debugMode", !p.getConfig().getBoolean("debugMode"));
						p.saveConfig();
						sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + (p.getConfig().getBoolean("debugMode") ? ChatColor.GREEN + _("configStatusTrue", sender.getName()) : ChatColor.RED + _("configStatusFalse", sender.getName())));
					} else if (v.equals("timedpromoteexclude")) {
						if (args[3].equals("add") || args[2].equals("add")) {
							@SuppressWarnings("unchecked")
							List<String> l = (List<String>) p.getConfig().getList("timedPromoteExclude");
							String toAdd = args[2].equals("add") ? args[3] : args[4];
							if (!l.contains(toAdd)) {
								l.add(args[2].equals("add") ? args[3] : args[4]);
								p.getConfig().set("timedPromoteExclude", l);
								p.saveConfig();
							}
							sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + p.getConfig().getList("timedPromoteExclude").toString());
						} else if (args[3].equals("remove") || (args[2].equals("remove"))) {
							@SuppressWarnings("unchecked")
							List<String> l = (List<String>) p.getConfig().getList("timedPromoteExclude");
							String toRemove = args[2].equals("remove") ? args[3] : args[4];
							if (l.contains(toRemove)) {
								l.remove(args[2].equals("remove") ? args[3] : args[4]);
								p.getConfig().set("timedPromoteExclude", l);
								p.saveConfig();
							}
							sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + p.getConfig().getList("timedPromoteExclude").toString());
						} else {
							LogHelper.showWarnings(sender, "configUnspecifiedError1", "configUnspecifiedError2", "configUnspecifiedError3");
						}
					} else if (v.equals("ecopromoteexclude")) {
						if (args[3].equals("add") || args[2].equals("add")) {
							@SuppressWarnings("unchecked")
							List<String> l = (List<String>) p.getConfig().getList("ecoPromoteExclude");
							String toAdd = args[2].equals("add") ? args[3] : args[4];
							if (!l.contains(toAdd)) {
								l.add(args[2].equals("add") ? args[3] : args[4]);
								p.getConfig().set("ecoPromoteExclude", l);
								p.saveConfig();
							}
							sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + p.getConfig().getList("ecoPromoteExclude").toString());
						} else if (args[3].equals("remove") || (args[2].equals("remove"))) {
							@SuppressWarnings("unchecked")
							List<String> l = (List<String>) p.getConfig().getList("ecoPromoteExclude");
							String toRemove = args[2].equals("remove") ? args[3] : args[4];
							if (l.contains(toRemove)) {
								l.remove(args[2].equals("remove") ? args[3] : args[4]);
								p.getConfig().set("ecoPromoteExclude", l);
								p.saveConfig();
							}
							sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + p.getConfig().getList("ecoPromoteExclude").toString());
						} else {
							LogHelper.showWarnings(sender, "configUnspecifiedError1", "configUnspecifiedError2", "configUnspecifiedError3");
						}
					} else if (v.equals("privatemsgcommands")) {
						if (args[3].equals("add") || args[2].equals("add")) {
							@SuppressWarnings("unchecked")
							List<String> l = (List<String>) p.getConfig().getList("privateMsgCommands");
							String toAdd = args[2].equals("add") ? args[3] : args[4];
							if (!l.contains(toAdd)) {
								l.add(args[2].equals("add") ? args[3] : args[4]);
								p.getConfig().set("privateMsgCommands", l);
								p.saveConfig();
							}
							sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + p.getConfig().getList("privateMsgCommands").toString());
						} else if (args[3].equals("remove") || (args[2].equals("remove"))) {
							@SuppressWarnings("unchecked")
							List<String> l = (List<String>) p.getConfig().getList("privateMsgCommands");
							String toRemove = args[2].equals("remove") ? args[3] : args[4];
							if (l.contains(toRemove)) {
								l.remove(args[2].equals("remove") ? args[3] : args[4]);
								p.getConfig().set("privateMsgCommands", l);
								p.saveConfig();
							}
							sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + p.getConfig().getList("privateMsgCommands").toString());
						} else {
							LogHelper.showWarnings(sender, "configUnspecifiedError1", "configUnspecifiedError2", "configUnspecifiedError3");
						}
					} else if (v.equals("motd")) {
						if ((aLength > 2) && args[2] != null) {
							p.getConfig().set("motd", args[2]);
							p.saveConfig();
							sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + ChatColor.WHITE + Utils.replaceChatColors(p.getConfig().getString("motd")));
						} else {
							LogHelper.showWarnings(sender, "configUnspecifiedError1", "configUnspecifiedError2", "configUnspecifiedError3");
						}
					} else if (v.equals("motdnewplayer")) {
						if ((aLength > 2) && args[2] != null) {
							p.getConfig().set("motdNewPlayer", args[2]);
							p.saveConfig();
							sender.sendMessage(ChatColor.YELLOW + _("configUpdated", sender.getName()) + ChatColor.WHITE + Utils.replaceChatColors(p.getConfig().getString("motdNewPlayer")));
						} else {
							LogHelper.showWarnings(sender, "configUnspecifiedError1", "configUnspecifiedError2", "configUnspecifiedError3");
						}
					} else {
						LogHelper.showWarning("configUnrecognized", sender);
					}
				}
			}
		} else {
			
			/***
			 * UNRECOGNIZED
			 */
			
			sender.sendMessage(ChatColor.RED + _("configUnrecognized", sender.getName()));
		}
		
		return true;
	}
}
