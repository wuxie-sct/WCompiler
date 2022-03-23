package test.test;

import org.bukkit.Bukkit;

public class Test {
    public static void test(){
        for (int a =0;a<20;a++){
            System.out.println("Hello world!"+a);
        }
		Bukkit.getConsoleSender().sendMessage("§a以上是测试消息");
		Bukkit.getConsoleSender().sendMessage("§a这个类文件位于插件目录 WCompiler/source/test/test/Test.java");
		Bukkit.getConsoleSender().sendMessage("§a可以删除它");
		Bukkit.getConsoleSender().sendMessage("§a删除后注意删除config.yml的enableCode.code1节点");
    }
}