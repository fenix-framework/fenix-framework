package pt.ist.fenixframework.pstm;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import dml.DomainClass;

import jvstm.ProcessAtomicAnnotations;

public class PostProcessDomainClasses extends AbstractDomainPostProcessor {
    private static final String CONSTRUCTOR_DESC = "(Lorg/apache/ojb/odmg/OJB;)V";

    public static void main (final String args[]) {
        PostProcessDomainClasses loader = new PostProcessDomainClasses();
        loader.processArgs(args);
        loader.start();

        // process, also, the @Atomic annotations
        ProcessAtomicAnnotations processor = new ProcessAtomicAnnotations(Transaction.class, new String[] { "." });
        processor.start();
    }

    protected ClassVisitor makeNewClassVisitor(ClassWriter cw) {
        return new AddOJBConstructorClassAdapter(cw);
    }

    protected void finishedProcessingClass(URL classURL, byte[] classBytecode) {
        super.finishedProcessingClass(classURL, classBytecode);

        try {
            FileOutputStream fos = new FileOutputStream(new File(classURL.toURI()));
            fos.write(classBytecode);
            fos.close();
        } catch (Exception e) {
            throw new Error("Couldn't rewrite class file: " + e);
        }
    }


    class AddOJBConstructorClassAdapter extends ClassAdapter implements Opcodes {
        private String classDesc = null;
        private String superDesc = null;
        private boolean foundConstructor = false;
        private boolean warnOnFiels = false;

        public AddOJBConstructorClassAdapter(ClassVisitor cv) {
            super(cv);
        }

        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.classDesc = name;
            this.superDesc = superName;
            this.warnOnFiels = isDomainNonBaseClass(descToName(classDesc));
            super.visit(version, access, name, signature, superName, interfaces);
        }

        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            if (warnOnFiels && ((access & ACC_STATIC) == 0)) {
                System.err.println(classDesc + ": field not declared on base class -> " + name);
            }
            return super.visitField(access, name, desc, signature, value);
        }

        public void visitEnd() {
            if (! foundConstructor) {
                // force it
                visitMethod(ACC_PUBLIC, "<init>", CONSTRUCTOR_DESC, null, null);
            }
        }

        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

            if ("<init>".equals(name) && CONSTRUCTOR_DESC.equals(desc)) {
                // we process it and remove the original, if any, by returning null
                mv.visitCode();

                // all the domain objects must inherit an allocate-instance only constructor
                // in the present case, it is declared on the AbstractDomainObject class
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitMethodInsn(INVOKESPECIAL, superDesc, "<init>", CONSTRUCTOR_DESC);

                if (isDomainBaseClass(descToName(classDesc))) {
                    // for base classes, we must invoke the initInstance() method
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitMethodInsn(INVOKESPECIAL, classDesc, "initInstance", "()V");
                }
                 
                mv.visitInsn(RETURN);
                mv.visitMaxs(2, 2);
                mv.visitEnd();

                foundConstructor = true;
                return null;
            } else {
                return mv;                 
            }
        }
    }
}
