package me.wuxie.wcompiler;

public class MyClassLoader extends ClassLoader {
    private byte[] in;
    public MyClassLoader(ClassLoader parent) {
        super(parent);
    }
    public void setIn(byte[] in) {
        this.in = subByte(in,in.length);
    }
    @Override
    protected Class<?> findClass(String name) {
        return defineClass(name, in, 0, in.length);
    }
    static byte[] subByte(byte[] b, int length){
        byte[] b1 = new byte[length];
        System.arraycopy(b, 0, b1, 0, length);
        return b1;
    }
}