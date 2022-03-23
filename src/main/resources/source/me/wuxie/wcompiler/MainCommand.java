package me.wuxie.wcompiler;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MainCommand implements CommandExecutor {
    public static void init(){
        Bukkit.getPluginCommand("wcr").setExecutor(new MainCommand());
        Bukkit.getConsoleSender().sendMessage("§e[WCompiler] §a注册主命令 wcr 成功!");
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender.isOp()) {
            if (args.length > 0) {
                if(args[0].equalsIgnoreCase("run")){
                    if (args.length > 1) {
                        WRun run = WCompiler.commandRun.get(args[1]);
                        if(run!=null){
                            run.run();
                        } else {
                            sender.sendMessage("§e[WCompiler] §7没有找到代码块 "+args[1]+" ！");
                        }
                    } else {
                        sender.sendMessage("§e[WCompiler] §7/wcr run <code> 缺少成员参数 <code>!");
                    }
                } else if(args[0].equalsIgnoreCase("reload")){
                    WCompiler.getPlugin(WCompiler.class).reloadConfig();
                    WCompiler.getPlugin(WCompiler.class).loadRun();
                    sender.sendMessage("§e[WCompiler] §a重载完成!");
                }
            } else {
                sender.sendMessage("§7/wcr run <code> §f执行commandCode代码块!");
                sender.sendMessage("§7/wcr reload §f重载配置文件以及更新代码块!");
            }
            return true;
        }
        return false;
    }
}
