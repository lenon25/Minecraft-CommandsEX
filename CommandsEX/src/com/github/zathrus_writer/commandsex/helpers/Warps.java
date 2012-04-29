package com.github.zathrus_writer.commandsex.helpers;

import static com.github.zathrus_writer.commandsex.Language._;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.zathrus_writer.commandsex.CommandsEX;
import com.github.zathrus_writer.commandsex.SQLManager;

/***
 * Contains set of commands to be executed for home setting/getting purposes.
 * @author zathrus-writer
 *
 */
public class Warps {
	/***
	 * INIT - initialization function, called when plugin is enabled to check for and create warps table
	 * @return
	 */
	public static void init(CommandsEX plugin) {
		// first of all, check if we can use any DB
		if (!CommandsEX.sqlEnabled) return;
		
		// next create warps table if it's not present yet
		SQLManager.query((SQLManager.sqlType.equals("mysql") ? "" : "BEGIN; ") + "CREATE TABLE IF NOT EXISTS "+ SQLManager.prefix +"warps (id_warp integer " + (SQLManager.sqlType.equals("mysql") ? "unsigned " : "") +"NOT NULL" + (SQLManager.sqlType.equals("mysql") ? " AUTO_INCREMENT" : "") +", owner_name varchar(32) NOT NULL" + (SQLManager.sqlType.equals("mysql") ? "" : " COLLATE 'NOCASE'") + ", world_name varchar(32) NOT NULL, warp_name varchar(50) NOT NULL" + (SQLManager.sqlType.equals("mysql") ? "" : " COLLATE 'NOCASE'") + ", x double NOT NULL, y double NOT NULL, z double NOT NULL, is_public BOOLEAN NOT NULL DEFAULT '0', PRIMARY KEY (id_warp), UNIQUE " + (SQLManager.sqlType.equals("mysql") ? "KEY warp_name " : "") +"(warp_name)" + (SQLManager.sqlType.equals("mysql") ? ", KEY owner_name (owner_name)" : "") + ")" + (SQLManager.sqlType.equals("mysql") ? " ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='stores warp points for the server' AUTO_INCREMENT=1" : "") + (SQLManager.sqlType.equals("mysql") ? "" : "; CREATE INDEX IF NOT EXISTS owner_name ON "+ SQLManager.prefix +"warps (owner_name); COMMIT;"));
	}

	
	/***
	 * WARP CREATE - creates a warp point
	 * @param sender
	 * @param args
	 * @param command
	 * @param alias
	 * @return
	 */
	public static Boolean create(CommandSender sender, String[] args, String command, String alias) {
		if (CommandsEX.sqlEnabled) {
			Player player = (Player)sender;

			// check if player is not spamming the command too much
			if (!Utils.checkCommandSpam(player, "warp-create")) {
				String pName = player.getName();
				// check if we have enough parameters
				if (args.length >= 2) {
					// if the player is trying to create a public warp, check his permissions
					if ((args.length > 2) && args[2].equals("public") && !Permissions.checkPermEx(player, "cex.warp.public")) {
						LogHelper.showWarning("warpNoPublicPerms", sender);
						return true;
					}
					
					// if the player is trying to create a warp for another player, check for permissions
					Boolean createForOther = false;
					if ((args.length > 2) && !args[2].equals("public") && !args[2].equals("private") && !Permissions.checkPermEx(player, "cex.warp.create.others")) {
						LogHelper.showWarning("warpNoOthersCreatePerms", sender);
						return true;
					} else if ((args.length > 2) && !args[2].equals("public") && !args[2].equals("private")) {
						// check if we have Vault present, otherwise we cannot give permissions to players
						if (!CommandsEX.vaultPresent) {
							LogHelper.showWarning("warpCreateForOtherFailed", sender);
							return true;
						}
						
						// give this player permission to warp to his own warps
						Permissions.addPerm(player.getWorld().getName(), args[2], "cex.warp.own");
						Permissions.addPerm(player.getWorld().getName(), args[2], "cex.warp.listprivate");
						createForOther = true;
					}
					
					// check how many warps this player has created, if limit is imposed
					if ((CommandsEX.getConf().getInt("maxWarpsPerPlayer") > 0) && !Permissions.checkPermEx(player, "cex.warp.bypasslimits")) {
						try {
							ResultSet res = SQLManager.query_res("SELECT Count(*) as Total FROM " + SQLManager.prefix + "warps WHERE owner_name = ?", pName);
							Integer numWarps = 0;
							while (res.next()) {
								numWarps = res.getInt("Total");
							}
							res.close();
							
							if (numWarps >= CommandsEX.getConf().getInt("maxWarpsPerPlayer")) {
								// no more warps for this player
								LogHelper.showWarning("warpTooManyWarps", sender);
								return true;
							}
						} catch (Throwable e) {
							// unable to load warp point
							LogHelper.showWarning("internalError", sender);
							LogHelper.logSevere("[CommandsEX] " + _("dbReadError", ""));
							LogHelper.logDebug("Message: " + e.getMessage() + ", cause: " + e.getCause() + ", from = warp create");
							return true;
						}
					}
					
					Location l = player.getLocation();
					if (SQLManager.query("INSERT "+ (SQLManager.sqlType.equals("mysql") ? "" : "OR REPLACE ") +"INTO " + SQLManager.prefix + "warps (owner_name, world_name, warp_name, x, y, z, is_public) VALUES(?, ?, ?, ?, ?, ?, ?)" + (SQLManager.sqlType.equals("mysql") ? " ON DUPLICATE KEY UPDATE owner_name = VALUES(owner_name), x = VALUES(x), y = VALUES(y), z = VALUES(z), is_public = VALUES(is_public)" : ""), (createForOther ? args[2] : pName), player.getWorld().getName(), args[1], l.getX(), l.getY(), l.getZ(), ((args.length > 2) && args[2].equals("public")) ? 1 : 0)) {
						// warp successfuly created
						LogHelper.showInfo("warpCreated#####[" + args[1], sender);
						
						if (createForOther) {
							// send message to the new proud warp owner :)
							Player px = Bukkit.getServer().getPlayer(args[2]);
							if (px != null) {
								LogHelper.showInfo("warpCreatedForYou1#####[" + args[1], px);
								LogHelper.showInfo("warpCreatedForYou2#####[" + args[1], px);
							}
						}
					} else {
						// an error has occured...
						LogHelper.showWarning("internalError", sender);
					}
				} else {
					// show usage info
					Commands.showCommandHelpAndUsage(sender, "cex_warp", "warp_create");
					return true;
				}
			}
		} else {
			// no database, no warps
			LogHelper.showInfo("warpsNoDatabase", sender);
		}
		
        return true;
	}
	
	/***
	 * WARP - teleports a player to warp location, if they have permissions
	 * @param sender
	 * @param args
	 * @param command
	 * @param alias
	 * @return
	 */
	public static Boolean warp(CommandSender sender, String[] args, String command, String alias) {
		if (CommandsEX.sqlEnabled) {
			Player player = (Player)sender;

			// get warp data from DB, if we didn't spam too much :)
			if (!Utils.checkCommandSpam(player, "warp-go")) {
				if (args.length > 0) {
					String pName = player.getName();
					try {
						String warpForSQL = args[0].toLowerCase();
						String foundWarpName = "";
						Integer numWarps = 0;
						Location l = null;
						ResultSet res = SQLManager.query_res("SELECT * FROM " + SQLManager.prefix + "warps WHERE (warp_name = ? OR warp_name LIKE ?)", warpForSQL, warpForSQL + "%");
	
						while (res.next()) {
							numWarps++;
							foundWarpName = res.getString("warp_name");
							// check if player is allowed to go to this warp
							if (!player.hasPermission("cex.warp.any") && !res.getBoolean("is_public") && !res.getString("owner_name").equals(pName)) {
								// player is not allowed in
								LogHelper.showWarning("warpNotAllowed", sender);
								return true;
							} else if (res.getString("owner_name").equals(pName) && !Permissions.checkPermEx(player, "cex.warp.own")) {
								// the player is trying to warp to his own warp but doesn't have permissions
								LogHelper.showWarning("warpNotAllowed", sender);
								return true;
							}
	
							// assemble location for the warp point
							l = new Location(CommandsEX.plugin.getServer().getWorld(res.getString("world_name")), res.getDouble("x"), res.getDouble("y"), res.getDouble("z"));
							
							// if the name matches exactly what we've been looking for, adjust numHomes and break the loop
							if (foundWarpName.toLowerCase().equals(warpForSQL)) {
								numWarps = 1;
								break;
							}
						}
						res.close();
	
						if (numWarps > 1) {
							// too many warp points match the selection
							numWarps = 0;
							foundWarpName = args[0];
						} else if (numWarps == 1) {
							// teleport player to the warp point
							Teleportation.delayedTeleport(player, l);
							LogHelper.showInfo("warpYouHaveWarpedTo#####[" + foundWarpName, sender);
							return true;
						}
						
						// if warp was not found, show message
						if (numWarps == 0) {
							LogHelper.showWarning("warpNotFound", sender);
						}
					} catch (Throwable e) {
						// unable to load warp point
						LogHelper.showWarning("internalError", sender);
						LogHelper.logSevere("[CommandsEX] " + _("dbReadError", ""));
						LogHelper.logDebug("Message: " + e.getMessage() + ", cause: " + e.getCause() + ", from = warp");
						return true;
					}
				} else {
					// show usage info
					Commands.showCommandHelpAndUsage(sender, "cex_warp", "warp_to");
					return true;
				}
			}
		} else {
			// no database, no warps
			LogHelper.showInfo("warpsNoDatabase", sender);
		}
		
        return true;
	}
	
	/***
	 * LISTWARPS - list all public warps created on the server
	 * @param sender
	 * @param args
	 * @param command
	 * @param alias
	 * @return
	 */
	public static Boolean list(CommandSender sender, String[] args, String command, String alias) {
		if (CommandsEX.sqlEnabled) {
			Player player = (Player)sender;
			if (!Utils.checkCommandSpam(player, "warp-listwarps")) {
				try {
					ResultSet res;
					if ((args.length == 0) || ((args.length == 1) && args[0].toLowerCase().equals("list"))) {
						if ((player.hasPermission("cex.warp.listpublic")) && (player.hasPermission("cex.warp.listprivate"))) {
							// list all warps - possibly an admin request
							res = SQLManager.query_res("SELECT warp_name FROM " + SQLManager.prefix + "warps");
						} else if (player.hasPermission("cex.warp.listpublic")) {
							// list public warps as well as our own
							res = SQLManager.query_res("SELECT warp_name FROM " + SQLManager.prefix + "warps WHERE is_public = 1 OR owner_name = ?", player.getName());
						} else {
							// list own warps only
							res = SQLManager.query_res("SELECT warp_name FROM " + SQLManager.prefix + "warps WHERE owner_name = ?", player.getName());
						}
					} else if (((args.length > 1) && args[1].toLowerCase().equals("public")) || ((args.length == 1) && args[0].toLowerCase().equals("public"))) {
						if (player.hasPermission("cex.warp.listpublic")) {
							// list public warps only
							res = SQLManager.query_res("SELECT warp_name FROM " + SQLManager.prefix + "warps WHERE is_public = 1");
						} else {
							// player is not allowed to list public warps
							LogHelper.showWarning("warpInsufficientListingPerms", sender);
							return true;
						}
					} else if (((args.length > 1) && args[1].toLowerCase().equals("private")) || ((args.length == 1) && args[0].toLowerCase().equals("private"))) {
						// list private warps if we can
						if (player.hasPermission("cex.warp.listprivate")) {
							res = SQLManager.query_res("SELECT warp_name FROM " + SQLManager.prefix + "warps WHERE is_public = 0 OR owner_name = ?", player.getName());
						} else {
							// player is not allowed to list private warps
							LogHelper.showWarning("warpInsufficientListingPerms", sender);
							return true;
						}
					} else if (((args.length > 1) && args[1].toLowerCase().equals("own")) || ((args.length == 1) && args[0].toLowerCase().equals("own"))) {
						// list only our own warp points
						res = SQLManager.query_res("SELECT warp_name FROM " + SQLManager.prefix + "warps WHERE owner_name = ?", player.getName());
					} else {
						// unrecognized parameter
						LogHelper.showWarning("warpUnrecognizedParam", sender);
						return true;
					}
					
					List<Object> warps = new ArrayList<Object>();
					while (res.next()) {
						warps.add(res.getString("warp_name"));
					}
					res.close();

					if (warps.size() > 0) {
						LogHelper.showInfo("warpsList#####[ " + Utils.implode(warps, ", "), sender);
					} else {
						// no warps data available for current player
						LogHelper.showWarning("warpsNoWarps", sender);
					}
				} catch (Throwable e) {
					// something went wrong with our SQL...
					LogHelper.showWarning("internalError", sender);
					LogHelper.logSevere("[CommandsEX] " + _("dbReadError", ""));
					LogHelper.logDebug("Message: " + e.getMessage() + ", cause: " + e.getCause() + ", from = listwarps");
				}
			}
		} else {
			// no database, no warps
			LogHelper.showInfo("warpsNoDatabase", sender);
		}
		
        return true;
	}
	
	/***
	 * WARP PUBLIC - makes warp point public
	 * @param sender
	 * @param args
	 * @param command
	 * @param alias
	 * @return
	 */
	public static Boolean make_public(CommandSender sender, String[] args, String command, String alias) {
		if (CommandsEX.sqlEnabled) {
			Player player = (Player)sender;
			
			// make warp point public, if we didn't spam too much :)
			if (!Utils.checkCommandSpam(player, "warp-public")) {
				if (args.length >= 2) {
					try {
						Boolean warpFound = false;
						ResultSet res = SQLManager.query_res("SELECT id_warp FROM " + SQLManager.prefix + "warps WHERE warp_name = ?", args[1]);
						
						while (res.next()) {
							SQLManager.query("UPDATE " + SQLManager.prefix + "warps SET is_public = 1 WHERE id_warp = ?", res.getInt("id_warp"));
							warpFound = true;
						}
						res.close();
	
						if (warpFound) {
							// inform player about successful operation
							LogHelper.showInfo("warpMadePublic#####[ " + args[1], sender);
						} else {
							// warp was not found
							LogHelper.showWarning("warpNotFound", sender);
						}
					} catch (Throwable e) {
						// something went wrong with our SQL...
						LogHelper.showWarning("internalError", sender);
						LogHelper.logSevere("[CommandsEX] " + _("dbReadError", ""));
						LogHelper.logDebug("Message: " + e.getMessage() + ", cause: " + e.getCause() + ", from = warp public");
					}
				} else {
					// insufficient parameters, show usage
					Commands.showCommandHelpAndUsage(sender, "cex_warp", "warp_public");
					return true;
				}
			}
		} else {
			// no database, no warps
			LogHelper.showInfo("warpsNoDatabase", sender);
		}
		
        return true;
	}
	
	/***
	 * WARP PRIVATE - makes warp point public
	 * @param sender
	 * @param args
	 * @param command
	 * @param alias
	 * @return
	 */
	public static Boolean make_private(CommandSender sender, String[] args, String command, String alias) {
		if (CommandsEX.sqlEnabled) {
			Player player = (Player)sender;
			
			// make warp point private, if we didn't spam too much :)
			if (!Utils.checkCommandSpam(player, "warp-private")) {
				if (args.length >= 2) {
					try {
						Boolean warpFound = false;
						ResultSet res = SQLManager.query_res("SELECT id_warp FROM " + SQLManager.prefix + "warps WHERE warp_name = ?", args[1]);
						
						while (res.next()) {
							SQLManager.query("UPDATE " + SQLManager.prefix + "warps SET is_public = 0 WHERE id_warp = ?", res.getInt("id_warp"));
							warpFound = true;
						}
						res.close();
	
						if (warpFound) {
							// inform player about successful operation
							LogHelper.showInfos(sender, new String[] {"warpMadePrivate1#####[ " + args[1], "warpMadePrivate2"});
						} else {
							// warp was not found
							LogHelper.showWarning("warpNotFound", sender);
						}
					} catch (Throwable e) {
						// something went wrong with our SQL...
						LogHelper.showWarning("internalError", sender);
						LogHelper.logSevere("[CommandsEX] " + _("dbReadError", ""));
						LogHelper.logDebug("Message: " + e.getMessage() + ", cause: " + e.getCause() + ", from = warp private");
					}
				} else {
					// insufficient parameters, show usage
					Commands.showCommandHelpAndUsage(sender, "cex_warp", "warp_private");
					return true;
				}
			}
		} else {
			// no database, no warps
			LogHelper.showInfo("warpsNoDatabase", sender);
		}
		
        return true;
	}
	
	/***
	 * WARP RENAME - renames a warp point
	 * @param sender
	 * @param args
	 * @param command
	 * @param alias
	 * @return
	 */
	public static Boolean rename(CommandSender sender, String[] args, String command, String alias) {
		if (CommandsEX.sqlEnabled) {
			Player player = (Player)sender;

			// check if player is not spamming the command too much
			if (!Utils.checkCommandSpam(player, "warp-rename")) {
				// check if we have enough parameters
				if (args.length >= 3) {
					try {
						Boolean warpFound = false;
						// first check for warp with same name as the new one in database
						ResultSet res = SQLManager.query_res("SELECT id_warp FROM " + SQLManager.prefix + "warps WHERE warp_name = ?", args[2]);
						
						while (res.next()) {
							// same name already exists, inform user and abort
							LogHelper.showWarning("warpSameNameExist", sender);
							res.close();
							return true;
						}
						res.close();
						
						// now try to find the warp we're looking for
						res = SQLManager.query_res("SELECT id_warp FROM " + SQLManager.prefix + "warps WHERE warp_name = ?", args[1]);
						while (res.next()) {
							SQLManager.query("UPDATE " + SQLManager.prefix + "warps SET warp_name = ? WHERE id_warp = ?", args[2], res.getInt("id_warp"));
							warpFound = true;
						}
						res.close();
	
						if (warpFound) {
							// inform player about successful operation
							LogHelper.showInfo("warp#####[ " + args[1] + " #####warpRenamedTo#####[ " + args[2] + ".", sender);
						} else {
							// warp was not found
							LogHelper.showWarning("warpNotFound", sender);
						}
					}  catch (Throwable e) {
						// something went wrong with our SQL...
						LogHelper.showWarning("internalError", sender);
						LogHelper.logSevere("[CommandsEX] " + _("dbReadError", ""));
						LogHelper.logDebug("Message: " + e.getMessage() + ", cause: " + e.getCause() + ", from = warp rename");
					}
				} else {
					// show usage info
					Commands.showCommandHelpAndUsage(sender, "cex_warp", "warp_rename");
					return true;
				}
			}
		} else {
			// no database, no warps
			LogHelper.showInfo("warpsNoDatabase", sender);
		}
		
        return true;
	}
	
	/***
	 * WARP DELETE - removes a warp point
	 * @param sender
	 * @param args
	 * @param command
	 * @param alias
	 * @return
	 */
	public static Boolean delete(CommandSender sender, String[] args, String command, String alias) {
		if (CommandsEX.sqlEnabled) {
			Player player = (Player)sender;
			
			// delete the warp point, if we didn't spam too much :)
			if (!Utils.checkCommandSpam(player, "warp-delete")) {
				if (args.length >= 2) {
					try {
						// let's see if we can find the warp point
						ResultSet res = SQLManager.query_res("SELECT * FROM " + SQLManager.prefix + "warps WHERE warp_name = ?", args[1]);
						Integer warpID = 0;
						while (res.next()) {
							// check if the player actually is allowed to delete the point
							if (!player.hasPermission("cex.warp.delete.all") && !res.getString("owner_name").equals(player.getName())) {
								// not allowed to delete
								LogHelper.showWarning("warpNoDeletePerms", sender);
								return true;
							}
							warpID = res.getInt("id_warp");
						}
						res.close();
						
						// remove the warp
						if (warpID > 0) {
							SQLManager.query("DELETE FROM " + SQLManager.prefix + "warps WHERE id_warp = ?", warpID);
							// inform player about successful operation
							LogHelper.showInfo("warpDeleted#####[ " + args[1], sender);
						} else {
							// couldn't find the warp
							LogHelper.showWarning("warpNotFound", sender);
						}
					} catch (Throwable e) {
						// something went wrong with our SQL...
						LogHelper.showWarning("internalError", sender);
						LogHelper.logSevere("[CommandsEX] " + _("dbReadError", ""));
						LogHelper.logDebug("Message: " + e.getMessage() + ", cause: " + e.getCause() + ", warp delete");
					}
				}  else {
					// insufficient parameters, show usage
					Commands.showCommandHelpAndUsage(sender, "cex_warp", "warp_delete");
					return true;
				}
			}
		} else {
			// no database, no homes
			LogHelper.showInfo("warpsNoDatabase", sender);
		}
		
        return true;
	}
	
	/***
	 * HELP - displays warps usage
	 * @param sender
	 * @param args
	 * @param command
	 * @param alias
	 * @return
	 */
	public static Boolean help(CommandSender sender, String[] args, String command, String alias) {
		if (CommandsEX.sqlEnabled) {
			// show help
			LogHelper.showInfos(sender, "warpHelp1", "warpHelp2", "warpHelp3", "warpHelp4", "warpHelp5", "warpHelp6", "warpHelp7", "warpHelp8", "warpHelp9");
		} else {
			// no database, no warps
			LogHelper.showInfo("warpsNoDatabase", sender);
		}
		
        return true;
	}
}