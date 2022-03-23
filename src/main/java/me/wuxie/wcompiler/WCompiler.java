package me.wuxie.wcompiler;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public final class WCompiler extends JavaPlugin {
    public static Map<String,Object> objectMap = new HashMap<>();
    public static String sourceDir = "";
    public static String targetDir = "";
    public static String jarsDir = "";
    public static MyClassLoader classLoader;
    public static List<WRun> loadRun;
    public static List<WRun> enableRun;
    public static List<WRun> stopRun;
    public static Map<String,WRun> commandRun;

    @Override
    public void onLoad() {
        classLoader = new MyClassLoader(this.getClass().getClassLoader());
        sourceDir = getDataFolder().getAbsolutePath()+ File.separator+"source";
        targetDir = getDataFolder().getAbsolutePath()+ File.separator+"target";
        jarsDir = getDataFolder().getAbsolutePath()+ File.separator+"jars";
        saveDefaultConfig();
        loadRun();
        File sourceDirF = new File(sourceDir);
        File targetDirF = new File(targetDir);
        File jarsDirF = new File(jarsDir);
        if(!sourceDirF.exists()){
            sourceDirF.mkdirs();
            saveResource("source/me/wuxie/wcompiler/MainCommand.java".replace("/",File.separator),true);
            saveResource("source/test/test/Test.java".replace("/",File.separator),true);
        }
        if(!targetDirF.exists()){
            targetDirF.mkdirs();
        }
        if(!jarsDirF.exists()){
            jarsDirF.mkdirs();
        }
        try {
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            boolean compilerResult = compiler("UTF-8", getJarFiles(jarsDir), sourceDir, diagnostics);
            if (compilerResult) {
                loadJavaClass();
            } else {
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                    System.out.println(diagnostic.getMessage(null));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (WRun run:loadRun){
            run.run();
        }
    }

    @Override
    public File getFile() {
        return super.getFile();
    }

    @Override
    public void onEnable() {
        new Metrics(this, 14715);
        try {
            run("Bukkit.getConsoleSender().sendMessage(\"§e[WCompiler] §a服务器启动,当前服务器版本--->§b\"+Bukkit.getVersion());","", Collections.singletonList("org.bukkit.Bukkit"),new ArrayList<>());
            for (WRun run:enableRun){
                run.run();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        for (WRun run:stopRun){
            run.run();
        }
    }
    public void loadRun(){
        FileConfiguration config = getConfig();
        loadRun = new ArrayList<>();
        enableRun = new ArrayList<>();
        stopRun = new ArrayList<>();
        commandRun = new HashMap<>();
        if(config.contains("loadCode")){
            ConfigurationSection section = config.getConfigurationSection("loadCode");
            for (String key: section.getKeys(false)){
                loadRun.add(loadRun(section.getConfigurationSection(key)));
            }
        }
        if(config.contains("enableCode")){
            ConfigurationSection section = config.getConfigurationSection("enableCode");
            for (String key: section.getKeys(false)){
                enableRun.add(loadRun(section.getConfigurationSection(key)));
            }
        }
        if(config.contains("stopCode")){
            ConfigurationSection section = config.getConfigurationSection("stopCode");
            for (String key: section.getKeys(false)){
                stopRun.add(loadRun(section.getConfigurationSection(key)));
            }
        }
        if(config.contains("commandCode")){
            ConfigurationSection section = config.getConfigurationSection("commandCode");
            for (String key: section.getKeys(false)){
                commandRun.put(key,loadRun(section.getConfigurationSection(key)));
            }
        }
    }

    private WRun loadRun(ConfigurationSection section){
        List<String> importList = section.getStringList("importList");
        List<String> jar = section.getStringList("importJar");
        List<String> importJar = new ArrayList<>();
        List<String> javaFile = section.getStringList("importJavaFile");
        List<String> importJavaFile = new ArrayList<>();
        for (String s:jar){
                importJar.add(s.replace("/",File.separator));
        }
        for (String s:javaFile){
            File file = new File(sourceDir+File.separator+(s.replace("/",File.separator).replace("\\",File.separator)));
            importJavaFile.add(file.getAbsolutePath());
        }
        String code = section.getString("code");
        return new WRun(code,importList,importJar,importJavaFile);
    }

    public static boolean compiler(String encoding, String jars, String filePath, DiagnosticCollector<JavaFileObject> diagnostics) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
            if (isNotNull(filePath) && isNotNull(sourceDir) && isNotNull(targetDir)) {
                return false;
            }
            loadJvaFiles(new File(sourceDir));
            List<File> sourceFileList = javaFiles;
            if (sourceFileList.size() == 0) {
                return false;
            }
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(sourceFileList);
            Iterable<String> options = Arrays.asList("-encoding", encoding, "-cp", jars, "-d", targetDir, "-sourcepath", sourceDir);
            JavaCompiler.CompilationTask compilationTask = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);
            return compilationTask.call();
        }
    }

    public static boolean isNotNull(String str) {
        if (null == str) {
            return true;
        } else if ("".equals(str)) {
            return true;
        } else return str.equals("null");
    }

    private static final List<File> javaFiles = new ArrayList<>();
    private static void loadJvaFiles(File file){
        if(file.exists()&&file.isDirectory()){
            File[] files = file.listFiles();
            if(files!=null){
                for (File f:files){
                    if(f.isDirectory()){
                        loadJvaFiles(f);
                    } else if(f.getName().endsWith(".java")){
                        try {
                            loadPackage(f);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        javaFiles.add(f);
                    }
                }
            }
        }
    }

    private static String getJarFiles(String jarPath) throws Exception {
        File sourceFile = new File(jarPath);
        AtomicReference<String> jars= new AtomicReference<>("");
        // 核心
        String core = System.getProperty("user.dir")+File.separator+new File(Bukkit.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName()+";";
        jars.set(jars+core);
        // 本插件
        String plugin = System.getProperty("user.dir")+File.separator+"plugins"+File.separator+new File(WCompiler.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName()+";";
        //System.out.println("core "+core);
        //System.out.println("plugin "+plugin);
        jars.set(jars + plugin);
        if (sourceFile.exists()) {
            if (sourceFile.isDirectory()) {
                sourceFile.listFiles(pathname -> {
                    if (pathname.isDirectory()) {
                        return true;
                    } else {
                        String name = pathname.getName();
                        if (name.endsWith(".jar")) {
                            jars.set(jars + pathname.getPath() + ";");
                            return true;
                        }
                        return false;
                    }
                });
            }
        }
        return jars.get();
    }

    private static final Map<File,String> packages = new HashMap<>();

    private static void loadPackage(File javaFile) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(javaFile)));
        String pack = "";
        String line;
        while ((line = reader.readLine()) != null) {
            String[] ss = line.split(";");
            for (String s:ss){
                s = s.replace(" ","");
                if(s.startsWith("package")){
                    pack = s.substring(7);
                    break;
                }
            }
        }
        packages.put(javaFile,pack);
        reader.close();
    }

    private static void loadJavaClass() {
        for (Map.Entry<File, String> mp : packages.entrySet()) {
            String name = mp.getKey().getName();
            String fileHeadName = name.substring(0, name.length() - 5);
            //String simpleName = ((mp.getValue().isEmpty() ? "" : mp.getValue() + ".") + fileHeadName).replace(".", File.separator);
            //String classFile = targetDir + File.separator + simpleName + ".class";
            /*try {
                FileInputStream in = new FileInputStream(classFile);
                int len = in.available();
                byte[] bytes = new byte[len];
                in.read(bytes);
                in.close();
                //classLoader.setIn(bytes);
                String n = (mp.getValue().isEmpty() ? "" : mp.getValue() + ".") + fileHeadName;
                classLoader.findClass(n);
                //new File(classFile).delete();
            } catch (Exception se) {
                se.printStackTrace();
            }*/
            String n = (mp.getValue().isEmpty() ? "" : mp.getValue() + ".") + fileHeadName;
            classLoader.findClass(n);
        }
    }

    private static void loadJavaClass(File file) throws Exception {
        String name = file.getName();
        String fileHeadName = name.substring(0, name.length() - 5);
        String p = packages.get(file);
        if(p==null){
            loadPackage(file);
            p = packages.get(file);
        }
        String s = p.isEmpty() ? "" : p + ".";
        /*String simpleName = (s + fileHeadName).replace(".",File.separator);
        String classFile = targetDir+File.separator+ simpleName +".class";
        FileInputStream in = new FileInputStream(classFile);
        int len = in.available();
        byte[] bytes = new byte[len];
        in.read(bytes);
        in.close();
        new File(classFile).deleteOnExit();*/
        try {
            classLoader.findClass(s+ fileHeadName);
        } catch (Exception se) {
            se.printStackTrace();
        }
    }
    private static String getBaseFileName(File file) {
        String fileName = file.getName();
        if(fileName.contains(".")){
            return fileName.split("\\.")[0];
        }else {
            return fileName;
        }
    }
    public static void run(String code,String jars, List<String> imports,List<String> dependJava) throws Exception {
        File file = File.createTempFile("JavaRuntime", ".java", new File(sourceDir));
        file.createNewFile();
        String classname = getBaseFileName(file);
        boolean flag = System.getProperty("os.name").contains("Windows");
        PrintWriter out = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(file),flag?"utf-8":"GBK")));
        out.println(getClassCode(code, classname,imports));
        out.close();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<String> options = Arrays.asList("-encoding", "UTF-8","-cp", getJarFiles(jarsDir)+jars, "-d", targetDir, "-sourcepath", sourceDir);
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        List<File> files = new ArrayList<>();
        files.add(file);
        for (String s:dependJava){
            File file1 = new File(s);
            files.add(file1);
        }
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(files);
        JavaCompiler.CompilationTask compilationTask = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);
        boolean success = compilationTask.call();
        file.delete();
        if(!success){
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                System.out.println(diagnostic.getMessage(null));
            }
        }
        File classFile = new File(targetDir+File.separator+classname+".class");
        /*FileInputStream in = new FileInputStream(classFile);
        int len = in.available();
        byte[] bytes = new byte[len];
        in.read(bytes);
        classLoader.setIn(bytes);
        in.close();
        classFile.delete();*/
        classLoader.setDelete(true);
        Class<?> c = classLoader.findClass(classname);
        Method main = c.getMethod("method");
        main.invoke(c);
    }

    private static String getClassCode(String code, String className, List<String> imports) {
        StringBuffer text = new StringBuffer();
        for (String s:imports){
            text.append("import "+s+";\n");
        }
        text.append("public class " + className + "{\n");
        text.append(" public static void method(){\n");
        text.append(" " + code + "\n");
        text.append(" }\n");
        text.append("}");
        String c = text.toString();
        return c;
    }

    /*@Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender.isOp()) {
            if (args.length > 0) {
                if(args[0].equalsIgnoreCase("run")){
                    if (args.length > 1) {
                        WRun run = commandRun.get(args[1]);
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
    }*/
}
