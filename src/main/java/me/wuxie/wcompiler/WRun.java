package me.wuxie.wcompiler;

import java.util.List;

public class WRun {
    private List<String> importList;
    private String importJar="";
    private List<String> importJavaFile;
    private String code;
    public WRun(String code,List<String> importList,List<String> importJar,List<String> importJavaFile){
        this.code=code;
        this.importList=importList;
        for (String s:importJar){
            this.importJar = this.importJar + s + ";";
        }
        this.importJavaFile=importJavaFile;
    }
    public void run(){
        try {
            WCompiler.run(code,importJar,importList,importJavaFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
