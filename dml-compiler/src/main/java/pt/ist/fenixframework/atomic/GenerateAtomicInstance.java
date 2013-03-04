package pt.ist.fenixframework.atomic;

import static java.io.File.separatorChar;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_6;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public final class GenerateAtomicInstance {

    private static final String ATOMIC_INSTANCE = "pt/ist/fenixframework/atomic/AtomicInstance";

    private GenerateAtomicInstance() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Syntax: GenerateAtomicInstance <save-path>");
            System.exit(-1);
        }
        start(new File(args[0]));
    }

    public static void start(File buildDir) throws IOException {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("pt/ist/fenixframework/Atomic.class");
        ClassReader cr = new ClassReader(is);
        ClassNode cNode = new ClassNode();
        cr.accept(cNode, 0);

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(V1_6, ACC_PUBLIC | ACC_FINAL, ATOMIC_INSTANCE, null, "java/lang/Object",
                new String[] { "pt/ist/fenixframework/Atomic" });
        cw.visitSource("Atomic Instance Class", null);

        // Generate fields
        for (MethodNode annotationElems : cNode.methods) {
            cw.visitField(ACC_PRIVATE | ACC_FINAL, annotationElems.name, getReturnTypeDescriptor(annotationElems), null, null);
        }

        // Generate constructor
        {
            StringBuffer ctorDescriptor = new StringBuffer("(");
            for (MethodNode annotationElems : cNode.methods)
                ctorDescriptor.append(getReturnTypeDescriptor(annotationElems));
            ctorDescriptor.append(")V");

            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", ctorDescriptor.toString(), null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
            int localsPos = 0;
            for (MethodNode annotationElems : cNode.methods) {
                Type t = Type.getReturnType(annotationElems.desc);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(t.getOpcode(ILOAD), localsPos + 1);
                mv.visitFieldInsn(PUTFIELD, ATOMIC_INSTANCE, annotationElems.name, t.getDescriptor());
                localsPos += t.getSize();
            }
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        // Generate getters
        for (MethodNode annotationElems : cNode.methods) {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, annotationElems.name, annotationElems.desc, null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, ATOMIC_INSTANCE, annotationElems.name, getReturnTypeDescriptor(annotationElems));
            mv.visitInsn(Type.getReturnType(annotationElems.desc).getOpcode(IRETURN));
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        // Generate annotationType() method
        {
            MethodVisitor mv =
                    cw.visitMethod(ACC_PUBLIC, "annotationType", "()Ljava/lang/Class;",
                            "()Ljava/lang/Class<+Ljava/lang/annotation/Annotation;>;", null);
            mv.visitCode();
            mv.visitLdcInsn(Type.getType(pt.ist.fenixframework.Atomic.class));
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }

        // Write Class
        FileOutputStream fos = null;
        try {
            File parentDir =
                    new File(buildDir, "pt" + separatorChar + "ist" + separatorChar + "fenixframework" + separatorChar + "atomic"
                            + separatorChar);
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                throw new IOException("Could not create required directory: " + parentDir);
            }

            File f = new File(parentDir, "AtomicInstance.class");
            fos = new FileOutputStream(f);
            fos.write(cw.toByteArray());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static String getReturnTypeDescriptor(MethodNode mNode) {
        return Type.getReturnType(mNode.desc).getDescriptor();
    }

}
