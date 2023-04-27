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

import org.objectweb.asm.*;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

import static org.objectweb.asm.Opcodes.*;

public class ProcessAnnotations {
    private final Type ADVICE = Type.getType(Advice.class);

    private final Type annotation;
    private final Type annotationInstance;

    private final Map<String, Object> defaultAnnotationElements;
    private final List<FieldNode> annotationFields;
    private final String annotationInstanceCtorDesc;
    private final ProgramArgs args;

    public ProcessAnnotations(ProgramArgs args) {
        this.args = args;
        annotation = Type.getType(args.annotationClass);
        annotationInstance = Type.getObjectType(GenerateAnnotationInstance.getAnnotationInstanceName(args.annotationClass));

        Map<String, Object> annotationElements = new HashMap<>();
        for (java.lang.reflect.Method element : args.annotationClass.getDeclaredMethods()) {
            if (element.getReturnType().isArray()) {
                throw new Error("FIXME: Annotations containing arrays are not yet supported");
            }
            Object defaultValue = element.getDefaultValue();
            if (defaultValue instanceof Class) {
                defaultValue = Type.getType((Class<?>) defaultValue);
            }
            annotationElements.put(element.getName(), defaultValue);
        }
        defaultAnnotationElements = Collections.unmodifiableMap(annotationElements);

        try {
            InputStream is = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(annotationInstance.getInternalName() + ".class");
            ClassReader cr = new ClassReader(is);
            ClassNode cNode = new ClassNode();
            cr.accept(cNode, 0);
            annotationFields = cNode.fields != null ? cNode.fields : Collections.emptyList();

            StringBuilder ctorDescriptor = new StringBuilder("(");
            for (FieldNode field : annotationFields) {
                ctorDescriptor.append(field.desc);
            }
            ctorDescriptor.append(")V");
            annotationInstanceCtorDesc = ctorDescriptor.toString();
        } catch (IOException e) {
            throw new RuntimeException("Error opening " + annotationInstance + " class. Have you run GenerateAnnotationInstance?",
                    e);
        }
    }

    public static void main(final String args[]) throws Exception {
        ProgramArgs progArgs = new ProgramArgs(args);
        ProcessAnnotations processor = new ProcessAnnotations(progArgs);
        processor.process();
    }

    public void process() {
        for (File f : args.fileList) {
            processFile(f);
        }
    }

    protected void processFile(File file) {
        if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
                processFile(subFile);
            }
        } else {
            String fileName = file.getName();
            if (fileName.toLowerCase().endsWith(".class")) {
                processClassFile(file);
            }
        }
    }

    protected void processClassFile(File classFile) {
        InputStream is = null;

        try {
            // get an input stream to read the bytecode of the class
            is = new FileInputStream(classFile);
            ClassReader cr = new ClassReader(is);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

            ClassVisitor cv = cw;
            // Add here other visitors to run AFTER the MethodTransformer
            cv = new MethodTransformer(cv, classFile);
            // Add here other visitors to run BEFORE the MethodTransformer

            cr.accept(cv, 0);
            writeClassFile(classFile, cw.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error processing class file " + classFile.getPath(), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected static void writeClassFile(File classFile, byte[] bytecode) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(classFile);
            fos.write(bytecode);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't write class file" + classFile.getPath(), e);
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

    private class MethodTransformer extends ClassVisitor {
        private final List<MethodNode> methods = new ArrayList<>();
        private final List<String> advisedMethodNames = new ArrayList<>();
        private final MethodNode advisedClInit;
        private final File classFile;

        private String className;

        public MethodTransformer(ClassVisitor cv, File originalClassFile) {
            super(ASM9, cv);

            classFile = originalClassFile;

            advisedClInit = new MethodNode(ACC_STATIC, "<clinit>", "()V", null, null);
            advisedClInit.visitCode();
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            className = name;
            cv.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            // Use a MethodNode to represent the method
            MethodNode mn = new MethodNode(access, name, desc, signature, exceptions);
            methods.add(mn);
            return mn;
        }

        @Override
        public void visitEnd() {
            MethodNode clInit = null;
            boolean isAnnotated = false;
            for (MethodNode mn : methods) {
                if (mn.name.equals("<clinit>")) {
                    clInit = mn;
                    continue;
                }

                for (AnnotationNode an : getAnnotations(mn)) {
                    if (an.desc.equals(annotation.getDescriptor())) {
                        // System.out.println("Method " + mn.name + " is tagged
                        // with annotation");
                        isAnnotated = true;
                        // Create new advised method
                        adviseMethod(mn, an);
                        break;
                    }
                }

                // Visit method, so it will be present on the output class
                mn.accept(cv);
            }

            if (isAnnotated) {
                // Insert <clinit> into class
                if (clInit != null) {
                    // Merge existing clinit with our additions
                    clInit.instructions.accept(advisedClInit);
                } else {
                    advisedClInit.visitInsn(RETURN);
                }
                advisedClInit.visitMaxs(0, 0);
                advisedClInit.visitEnd();
                advisedClInit.accept(cv);
            } else {
                // Preserve existing <clinit>
                if (clInit != null) {
                    clInit.accept(cv);
                }
            }

            cv.visitEnd();
        }

        /**
         * Returns the invisible or visible annotations list, depending on the
         * RetentionPolicy of the client annotation.
         **/
        private List<AnnotationNode> getAnnotations(MethodNode mn) {
            Retention retAnnot = args.annotationClass.getAnnotation(Retention.class);
            RetentionPolicy policy = retAnnot == null ? RetentionPolicy.CLASS : retAnnot.value();
            List<AnnotationNode> list = policy == RetentionPolicy.CLASS ? mn.invisibleAnnotations : mn.visibleAnnotations;
            return list != null ? list : Collections.emptyList();
        }

        /**
         * To advise method add annotated with @Annot, part of the class Xpto,
         * and with signature
         * 
         * @Annot @SomethingElse public long add(Object o, int i) we generate
         *        the following code:
         *
         *        public static [final] Advice advice$add =
         *        ClientAdviceFactory.getInstance().newAdvice(annotation);
         *
         * @SomethingElse public long add(Object o, int i) { static final class
         *                callable$add implements Callable { Xpto arg0; Object
         *                arg1; int arg2;
         *
         *                callable$add(Xpto arg0, Object arg1, int arg2) {
         *                this.arg0 = arg0; this.arg1 = arg1; this.arg2 = arg2;
         *                }
         *
         *                public Object call() { return Xpto.advised$add(arg0,
         *                arg1, arg2); } } return advice$add.perform(new
         *                callable$add(this, o, i)); }
         *
         *                synthetic static long advised$add(Xpto this, Object o,
         *                int i) { // original method }
         *
         *                Note that any annotations from the original method are
         *                removed from the advised$ version.
         **/
        private void adviseMethod(MethodNode mn, AnnotationNode advisedAnnotation) {
            // Mangle name if there are multiple advised methods with the same
            // name
            String methodName = getMethodName(mn.name);
            // Name for advice field
            String fieldName = "advice$" + methodName;
            // Name for callable class
            String callableClass = className + "$callable$" + methodName;

            // Generate new method which will invoke the advice with the
            // Callable
            MethodVisitor advisedMethod =
                    cv.visitMethod(mn.access, mn.name, mn.desc, mn.signature, mn.exceptions.toArray(new String[0]));

            // Remove advised annotation and copy other annotations from the
            // original method to the newly created method
            getAnnotations(mn).remove(advisedAnnotation);
            copyAnnotations(mn, advisedMethod);

            // Create field to save advice
            cv.visitField(ACC_PUBLIC | ACC_STATIC | ACC_FINAL, fieldName, ADVICE.getDescriptor(), null, null);

            // Add code to clinit to initialize the field
            // Add default parameters from annotation
            Map<String, Object> annotationElements = new HashMap<>(defaultAnnotationElements);
            // Copy parameters from method annotation
            if (advisedAnnotation.values != null) {
                Iterator<Object> it = advisedAnnotation.values.iterator();
                while (it.hasNext()) {
                    // ASM stores annotation values as String1, Object1,
                    // String2, Object2, ... in the values list
                    annotationElements.put((String) it.next(), it.next());
                }
            }

            // Decide whether the annotation defines its own AdviceFactory and,
            // if so, use that. Otherwise, use either the
            // factory specified in this program's execution parameters or the
            // default factory.
            Type factoryType = (Type) annotationElements.get("adviceFactory");
            if (factoryType == null) {
                factoryType = Type.getObjectType((args.annotationFactoryClass != null ? args.annotationFactoryClass
                        .getCanonicalName() : AdviceFactory.DEFAULT_ADVICE_FACTORY).replace('.', '/'));
            }

            advisedClInit.visitMethodInsn(INVOKESTATIC, factoryType.getInternalName(), "getInstance",
                    "()" + Type.getType(AdviceFactory.class).getDescriptor(), false);

            // Push annotation parameters on the stack and create
            // AnnotationInstance
            advisedClInit.visitTypeInsn(NEW, annotationInstance.getInternalName());
            advisedClInit.visitInsn(DUP);
            for (FieldNode field : annotationFields) {
                // Support for enums
                if (fieldIsEnum(field)) {
                    // ASM supplies enums as String[], while the defaults read
                    // by reflection are Enum instances
                    Object value = annotationElements.get(field.name);
                    Enum<?> enumValue = value instanceof String[] ? getEnumElement((String[]) value) : (Enum<?>) value;
                    Type enumType = Type.getType(enumValue.getClass());
                    advisedClInit.visitFieldInsn(GETSTATIC, enumType.getInternalName(), enumValue.name(),
                            enumType.getDescriptor());
                } else {
                    advisedClInit.visitLdcInsn(annotationElements.get(field.name));
                }
            }
            advisedClInit.visitMethodInsn(INVOKESPECIAL, annotationInstance.getInternalName(), "<init>",
                    annotationInstanceCtorDesc, false);
            // Obtain advice for this method
            advisedClInit.visitMethodInsn(INVOKEVIRTUAL, Type.getType(AdviceFactory.class).getInternalName(), "newAdvice",
                    "(" + Type.getType(Annotation.class).getDescriptor() + ")" + ADVICE.getDescriptor(), false);

            advisedClInit.visitFieldInsn(PUTSTATIC, className, fieldName, ADVICE.getDescriptor());

            // Repurpose original method
            modifyOriginalMethod(mn);

            // Generate replacement method
            generateMethodCode(mn, advisedMethod, fieldName, callableClass);

            // Generate callable class
            generateCallable(callableClass, mn);
        }

        private void copyAnnotations(MethodNode mn, MethodVisitor advisedMethod) {
            // InvisibleAnnotations
            if (mn.invisibleAnnotations != null) {
                for (AnnotationNode an : mn.invisibleAnnotations) {
                    an.accept(advisedMethod.visitAnnotation(an.desc, false));
                }
            }
            // VisibleAnnotations
            if (mn.visibleAnnotations != null) {
                for (AnnotationNode an : mn.visibleAnnotations) {
                    an.accept(advisedMethod.visitAnnotation(an.desc, true));
                }
            }
            // InvisibleParameterAnnotations
            if (mn.invisibleParameterAnnotations != null) {
                for (int i = 0; i < mn.invisibleParameterAnnotations.length; i++) {
                    if (mn.invisibleParameterAnnotations[i] != null) {
                        for (AnnotationNode an : mn.invisibleParameterAnnotations[i]) {
                            an.accept(advisedMethod.visitParameterAnnotation(i, an.desc, false));
                        }
                    }
                }
            }
            // VisibleParameterAnnotations
            if (mn.visibleParameterAnnotations != null) {
                for (int i = 0; i < mn.visibleParameterAnnotations.length; i++) {
                    if (mn.visibleParameterAnnotations[i] != null) {
                        for (AnnotationNode an : mn.visibleParameterAnnotations[i]) {
                            an.accept(advisedMethod.visitParameterAnnotation(i, an.desc, true));
                        }
                    }
                }
            }
        }

        private void modifyOriginalMethod(MethodNode mn) {
            // Rename original method
            mn.name = "advised$" + mn.name;
            // Remove annotations from original method
            mn.invisibleAnnotations = Collections.emptyList();
            mn.visibleAnnotations = Collections.emptyList();
            // Modify the access flags, setting the method as package protected,
            // so that the callable can access it
            mn.access &= ~ACC_PRIVATE & ~ACC_PUBLIC;
            // Also mark it as synthetic, so Java tools ignore it
            mn.access |= ACC_SYNTHETIC;
            // Check for, and clear any attributes seen
            if (mn.attrs != null) {
                System.err.println("WARNING: Modified method " + mn.name + " has non-standard attributes");
            }
            // Clear parameter annotations
            mn.visibleParameterAnnotations = null;
            mn.invisibleParameterAnnotations = null;

            if (!isStatic(mn)) {
                // Convert original method to static method with instance as
                // first argument
                // Note that the bytecode is still valid, as ALOAD 0 (an access
                // to this) will still have
                // the same semantics
                mn.access |= ACC_STATIC;
                mn.desc = "(L" + className + ";" + mn.desc.substring(1);
            }
        }

        private void generateMethodCode(MethodNode mn, MethodVisitor mv, String fieldName, String callableClass) {
            mv.visitCode();
            mv.visitFieldInsn(GETSTATIC, className, fieldName, ADVICE.getDescriptor());
            mv.visitTypeInsn(NEW, callableClass);
            mv.visitInsn(DUP);

            int pos = 0;
            // Push arguments for original method on the stack
            for (Type t : Type.getArgumentTypes(mn.desc)) {
                mv.visitVarInsn(t.getOpcode(ILOAD), pos);
                pos += t.getSize();
            }
            mv.visitMethodInsn(INVOKESPECIAL, callableClass, "<init>", getCallableCtorDesc(mn), false);
            mv.visitMethodInsn(INVOKEINTERFACE, ADVICE.getInternalName(), "perform",
                    "(Ljava/util/concurrent/Callable;)Ljava/lang/Object;", true);

            // Return value
            Type returnType = Type.getReturnType(mn.desc);
            if (returnType.getSort() == Type.OBJECT || returnType.getSort() == Type.ARRAY) {
                mv.visitTypeInsn(CHECKCAST, returnType.getInternalName());
            } else if (isPrimitive(returnType)) {
                // Return is native, we have to unbox the value from the Advice
                boxUnwrap(returnType, mv);
            }
            mv.visitInsn(returnType.getOpcode(IRETURN));
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private boolean isStatic(MethodNode mn) {
            return (mn.access & ACC_STATIC) > 0;
        }

        private String getCallableCtorDesc(MethodNode mn) {
            return mn.desc.substring(0, mn.desc.indexOf(')') + 1) + 'V';
        }

        private String getMethodName(String methodName) {
            // Count number of advised methods with same name
            int count = 0;
            for (String name : advisedMethodNames) {
                if (name.equals(methodName)) {
                    count++;
                }
            }
            // Add another one
            advisedMethodNames.add(methodName);

            return methodName + (count > 0 ? "$" + count : "");
        }

        private void generateCallable(String callableClass, MethodNode mn) {
            Type returnType = Type.getReturnType(mn.desc);
            Type[] arguments = Type.getArgumentTypes(mn.desc);

            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            cw.visit(V1_6, ACC_FINAL, callableClass,
                    "Ljava/lang/Object;Ljava/util/concurrent/Callable<" + (isPrimitive(returnType) ? toObject(
                            returnType) : (returnType.equals(Type.VOID_TYPE) ? Type.getObjectType("java/lang/Void") : returnType))
                                    .getDescriptor()
                            + ">;",
                    "java/lang/Object", new String[] { "java/util/concurrent/Callable" });
            cw.visitSource("Advice Library Automatically Generated Class", null);

            // Create fields to hold arguments
            {
                int fieldPos = 0;
                for (Type t : arguments) {
                    cw.visitField(ACC_PRIVATE | ACC_FINAL, "arg" + (fieldPos++), t.getDescriptor(), null, null);
                }
            }

            // Create constructor
            {
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", getCallableCtorDesc(mn), null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
                int localsPos = 0;
                int fieldPos = 0;
                for (Type t : arguments) {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitVarInsn(t.getOpcode(ILOAD), localsPos + 1);
                    mv.visitFieldInsn(PUTFIELD, callableClass, "arg" + fieldPos++, t.getDescriptor());
                    localsPos += t.getSize();
                }
                mv.visitInsn(RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }

            // Create call method
            {
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "call", "()Ljava/lang/Object;", null, null);
                mv.visitCode();
                int fieldPos = 0;
                for (Type t : arguments) {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, callableClass, "arg" + fieldPos++, t.getDescriptor());
                }
                mv.visitMethodInsn(INVOKESTATIC, className, mn.name, mn.desc, false);
                if (returnType.equals(Type.VOID_TYPE)) {
                    mv.visitInsn(ACONST_NULL);
                } else if (isPrimitive(returnType)) {
                    boxWrap(returnType, mv);
                }
                mv.visitInsn(ARETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }

            // Write the callable class file in the same directory (package) as
            // the original class file
            String callableFileName = callableClass.substring(Math.max(callableClass.lastIndexOf('/'), 0)) + ".class";
            writeClassFile(new File(classFile.getParent() + File.separatorChar + callableFileName), cw.toByteArray());
        }

        private final Object[][] primitiveWrappers =
                new Object[][] { { "java/lang/Boolean", Type.BOOLEAN_TYPE }, { "java/lang/Byte", Type.BYTE_TYPE },
                        { "java/lang/Character", Type.CHAR_TYPE }, { "java/lang/Short", Type.SHORT_TYPE },
                        { "java/lang/Integer", Type.INT_TYPE }, { "java/lang/Long", Type.LONG_TYPE },
                        { "java/lang/Float", Type.FLOAT_TYPE }, { "java/lang/Double", Type.DOUBLE_TYPE } };

        private Type toObject(Type primitiveType) {
            for (Object[] map : primitiveWrappers) {
                if (primitiveType.equals(map[1])) {
                    return Type.getObjectType((String) map[0]);
                }
            }
            throw new AssertionError();
        }

        private boolean isPrimitive(Type type) {
            int sort = type.getSort();
            return sort != Type.VOID && sort != Type.ARRAY && sort != Type.OBJECT && sort != Type.METHOD;
        }

        private void boxWrap(Type primitiveType, MethodVisitor mv) {
            Type objectType = toObject(primitiveType);
            mv.visitMethodInsn(INVOKESTATIC, objectType.getInternalName(), "valueOf",
                    "(" + primitiveType.getDescriptor() + ")" + objectType.getDescriptor(), false);
        }

        private void boxUnwrap(Type primitiveType, MethodVisitor mv) {
            Type objectType = toObject(primitiveType);
            mv.visitTypeInsn(CHECKCAST, objectType.getInternalName());
            mv.visitMethodInsn(INVOKEVIRTUAL, objectType.getInternalName(), primitiveType.getClassName() + "Value",
                    "()" + primitiveType.getDescriptor(), false);
        }

        private boolean fieldIsEnum(FieldNode field) {
            return field.desc.charAt(0) == 'L' && !field.desc.equals("Ljava/lang/Object;")
                    && !field.desc.equals("Ljava/lang/String;") && !field.desc.equals("Ljava/lang/Class;");
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private Enum<?> getEnumElement(String[] enumInfo) {
            try {
                Class<? extends Enum> enumClass = Class
                        .forName(enumInfo[0].substring(1, enumInfo[0].length() - 1).replace('/', '.')).asSubclass(Enum.class);
                return Enum.valueOf(enumClass, enumInfo[1]);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // smf: Shamelessly adapted from CompilerArgs in Fenix Framework's DML
    // compiler
    public static class ProgramArgs {
        Class<? extends Annotation> annotationClass;
        Class<? extends AdviceFactory<?>> annotationFactoryClass;
        List<File> fileList = new ArrayList<>();

        public ProgramArgs(Class<? extends Annotation> annotationClass,
                Class<? extends AdviceFactory<?>> annotationFactoryClass) {
            this.annotationClass = annotationClass;
            this.annotationFactoryClass = annotationFactoryClass;
        }

        public ProgramArgs(Class<? extends Annotation> annotationClass, Class<? extends AdviceFactory<?>> annotationFactoryClass,
                File file) {
            this(annotationClass, annotationFactoryClass);
            this.fileList.add(file);
        }

        public ProgramArgs(Class<? extends Annotation> annotationClass, Class<? extends AdviceFactory<?>> annotationFactoryClass,
                List<File> fileList) {
            this(annotationClass, annotationFactoryClass);
            this.fileList.addAll(fileList);
        }

        public ProgramArgs(String[] args) throws Exception {
            if (args.length < 3) {
                error("wrong syntax");
            }
            processCommandLineArgs(args);
            checkArguments();
        }

        void checkArguments() {
            if (annotationClass == null) {
                error("annotation class is not specified");
            }
            if (annotationFactoryClass == null) {
                message("no factory class specified: using defaults");
            }
            if (fileList.isEmpty()) {
                error("no class files or dirs specified");
            }
        }

        void processCommandLineArgs(String[] args) throws Exception {
            int num = 0;
            while (num < args.length) {
                num = processOption(args, num);
            }
        }

        @SuppressWarnings("unchecked")
        int processOption(String[] args, int pos) throws Exception {
            if (args[pos].equals("-a")) {
                annotationClass = Class.forName(getNextArgument(args, pos)).asSubclass(Annotation.class);
                return pos + 2;
            } else if (args[pos].equals("-f")) {
                annotationFactoryClass =
                        (Class<AdviceFactory<?>>) Class.forName(getNextArgument(args, pos)).asSubclass(AdviceFactory.class);
                return pos + 2;
            } else {
                fileList.add(new File(args[pos]));
                return pos + 1;
            }
        }

        String getNextArgument(String[] args, int pos) {
            int nextPos = pos + 1;
            if (nextPos < args.length) {
                return args[nextPos];
            } else {
                error("option " + args[pos] + " requires argument");
            }
            return null;
        }

        void error(String msg) {
            System.err.println("ProcessAnnotations: " + msg);
            System.err.println(
                    "Syntax: ProcessAnnotations -a <annotation-class> [-f <advice-factory-class>] [class files or dirs]");
            System.exit(1);
        }

        void message(String msg) {
            System.out.println("ProcessAnnotations: " + msg);
        }

    }

}
