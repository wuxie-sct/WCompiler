package me.wuxie.wcompiler;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.ConcurrentHashMap;

public class MyClassLoader extends ClassLoader {
    private byte[] in;
    private final ConcurrentHashMap<String,Class<?>> classMap = new ConcurrentHashMap<>();
    public MyClassLoader(ClassLoader parent) {
        super(parent);
    }

    public void setIn(byte[] in) {
        this.in = subByte(in,in.length);
    }

    /*protected Class<?> findClass(byte[] bytes,String name){
        this.in = subByte(bytes,bytes.length);
        return findClass(name);
    }*/

    @Override
    protected Class<?> findClass(String name) {
        if (classMap.containsKey(name)) {
            return classMap.get(name);
        }
        String classFileS = WCompiler.targetDir+File.separator + name.replace(".",File.separator)+".class";
        File classFileF = new File(classFileS);
        if(classFileF.exists()) {
            try {
                FileInputStream in = new FileInputStream(classFileS);
                int len = in.available();
                byte[] bytes = new byte[len];
                in.read(bytes);
                in.close();
                this.in = subByte(bytes, bytes.length);
                if (b) {
                    classFileF.delete();
                    b = false;
                }
            } catch (Exception se) {
                se.printStackTrace();
            }
            Class<?> c = defineClass(name, in, 0, in.length);
            classMap.put(name, c);
            return c;
        }
        return null;
    }

    static byte[] subByte(byte[] b, int length){
        byte[] b1 = new byte[length];
        System.arraycopy(b, 0, b1, 0, length);
        return b1;
    }
    boolean b = false;
    public void setDelete(boolean b) {
        this.b = b;
    }
}