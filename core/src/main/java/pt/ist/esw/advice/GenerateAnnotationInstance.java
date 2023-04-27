/*
 * Advice Library
 * Copyright (C) 2012-2013 INESC-ID Software Engineering Group
 * http://www.esw.inesc-id.pt
 *
 * This file is part of the advice library.
 *
 * advice library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * advice library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with advice library. If not, see <http://www.gnu.org/licenses/>.
 *
 * Author's contact:
 * INESC-ID Software Engineering Group
 * Rua Alves Redol 9
 * 1000 - 029 Lisboa
 * Portugal
 */
package pt.ist.esw.advice;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;

import static java.io.File.separatorChar;
import static org.objectweb.asm.Opcodes.*;

public final class GenerateAnnotationInstance {
    private final String annotation;
    private final String annotationInstance;

    private final Class<? extends Annotation> annotationClass;
    private final File buildDir;

    public GenerateAnnotationInstance(Class<? extends Annotation> annotationClass, File buildDir) {
        this.annotationClass = annotationClass;
        annotation = annotationClass.getName().replace('.', '/');
        annotationInstance = getAnnotationInstanceName(annotationClass);
        this.buildDir = buildDir;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        if (args.length != 2) {
            System.err.println("Syntax: GenerateAnnotationInstance <annotation-class> <save-path>");
            System.exit(-1);
        }
        Class<? extends Annotation> annotationClass = Class.forName(args[0]).asSubclass(Annotation.class);
        new GenerateAnnotationInstance(annotationClass, new File(args[1])).start();
    }

    public void start() throws IOException {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(annotation + ".class");
        ClassReader cr = new ClassReader(is);
        ClassNode cNode = new ClassNode();
        cr.accept(cNode, 0);

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(V1_6, ACC_PUBLIC | ACC_FINAL, annotationInstance, null, "java/lang/Object", new String[] { annotation });
        cw.visitSource("Annotation Instance Class", null);

        // Generate fields
        for (MethodNode annotationElems : cNode.methods) {
            cw.visitField(ACC_PRIVATE | ACC_FINAL, annotationElems.name, getReturnTypeDescriptor(annotationElems), null, null);
        }

        // Generate constructor
        {
            StringBuilder ctorDescriptor = new StringBuilder("(");
            for (MethodNode annotationElems : cNode.methods) {
                ctorDescriptor.append(getReturnTypeDescriptor(annotationElems));
            }
            ctorDescriptor.append(")V");

            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", ctorDescriptor.toString(), null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            int localsPos = 0;
            for (MethodNode annotationElems : cNode.methods) {
                Type t = Type.getReturnType(annotationElems.desc);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(t.getOpcode(ILOAD), localsPos + 1);
                mv.visitFieldInsn(PUTFIELD, annotationInstance, annotationElems.name, t.getDescriptor());
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
            mv.visitFieldInsn(GETFIELD, annotationInstance, annotationElems.name, getReturnTypeDescriptor(annotationElems));
            mv.visitInsn(Type.getReturnType(annotationElems.desc).getOpcode(IRETURN));
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        // Generate annotationType() method
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "annotationType", "()Ljava/lang/Class;",
                    "()Ljava/lang/Class<+Ljava/lang/annotation/Annotation;>;", null);
            mv.visitCode();
            mv.visitLdcInsn(Type.getType(annotationClass));
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }

        // Write Class
        FileOutputStream fos = null;
        try {
            String instancePath =
                    annotationInstance.substring(0, annotationInstance.lastIndexOf('/') + 1).replace('/', separatorChar);
            File parentDir = new File(buildDir, instancePath);
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                throw new IOException("Could not create required directory: " + parentDir);
            }

            File f = new File(parentDir, annotationClass.getSimpleName() + "Instance.class");
            fos = new FileOutputStream(f);
            fos.write(cw.toByteArray());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getReturnTypeDescriptor(MethodNode mNode) {
        return Type.getReturnType(mNode.desc).getDescriptor();
    }

    protected static String getAnnotationInstanceName(Class<? extends Annotation> annotationClass) {
        return (GenerateAnnotationInstance.class.getPackage().getName() + '.' + annotationClass.getName() + "Instance")
                .replace('.', '/');
    }

}
