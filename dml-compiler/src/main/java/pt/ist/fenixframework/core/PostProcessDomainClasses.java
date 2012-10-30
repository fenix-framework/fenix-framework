package pt.ist.fenixframework.core;

import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

// import jvstm.ProcessAtomicAnnotations;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class PostProcessDomainClasses extends AbstractDomainPostProcessor {
    private static final String OID_INNER_CLASS_INTERNAL_NAME = Type.getInternalName(DomainObjectAllocator.OID.class);

    private static final String CONSTRUCTOR_DESC = Type.getMethodDescriptor(Type.VOID_TYPE,
	    new Type[] { Type.getType(DomainObjectAllocator.OID.class) });

    private PostProcessDomainClasses() {
	super();
    }

    public PostProcessDomainClasses(List<URL> dmlFiles) {
	this(dmlFiles, Thread.currentThread().getContextClassLoader());
    }

    public PostProcessDomainClasses(List<URL> dmlFiles, ClassLoader parentClassLoader) {
	super(parentClassLoader);
	this.dmlFiles.addAll(dmlFiles);
    }

    public static void main(final String args[]) throws MalformedURLException {
	PostProcessDomainClasses loader = new PostProcessDomainClasses();
	loader.processArgs(args);
	loader.start();

	// // process, also, the @Atomic annotations
	// ProcessAtomicAnnotations processor = new ProcessAtomicAnnotations(Transaction.class, new String[] { "." });
	// processor.start();
    }

    @Override
    protected ClassVisitor makeNewClassVisitor(ClassWriter cw) {
	return new AddOJBConstructorClassAdapter(cw);
    }

    @Override
    protected void finishedProcessingClass(URL classURL, byte[] classBytecode) {
	super.finishedProcessingClass(classURL, classBytecode);
	// No need to post-process classes that are already post-processed
	if (classURL.toExternalForm().startsWith("jar:file")) {
	    return;
	}
	try {
	    FileOutputStream fos = new FileOutputStream(new File(classURL.toURI()));
	    fos.write(classBytecode);
	    fos.close();
	} catch (Exception e) {
	    throw new Error("Couldn't rewrite class file: " + e);
	}
    }

    class AddOJBConstructorClassAdapter extends ClassVisitor {
	private String classDesc = null;
	private String superDesc = null;
	private boolean foundConstructor = false;
	private boolean foundInnerClass = false;
	private boolean warnOnFiels = false;

	public AddOJBConstructorClassAdapter(ClassVisitor cv) {
	    super(Opcodes.ASM4, cv);
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
	    this.classDesc = name;
	    this.superDesc = superName;
	    this.warnOnFiels = isDomainNonBaseClass(descToName(classDesc));
	    super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public void visitInnerClass(String name, String outerName, String innerName, int access) {
	    if (!foundInnerClass) {
		foundInnerClass = OID_INNER_CLASS_INTERNAL_NAME.equals(name);
	    }
	    super.visitInnerClass(name, outerName, innerName, access);
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
	    if (warnOnFiels && ((access & ACC_STATIC) == 0)) {
		System.err.println(classDesc + ": field not declared on base class -> " + name);
	    }
	    return super.visitField(access, name, desc, signature, value);
	}

	@Override
	public void visitEnd() {
	    if (!foundConstructor) {
		// force it
		visitMethod(ACC_PUBLIC, "<init>", CONSTRUCTOR_DESC, null, null);
	    }

	    if (!foundInnerClass) {
		// add, also, the InnerClasses attribute for the
		// DomainObjectAllocator.OID class that is used in the
		// constructor injected
		visitInnerClass(OID_INNER_CLASS_INTERNAL_NAME, Type.getInternalName(DomainObjectAllocator.class), "OID",
			ACC_PUBLIC + ACC_STATIC);
	    }
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
	    MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

	    if ("<init>".equals(name) && CONSTRUCTOR_DESC.equals(desc)) {
                // we process it and remove the original, if any, by returning
		// null
		mv.visitCode();

		// all the domain objects must inherit an allocate-instance only
		// constructor
		// in the present case, it is declared on the
		// AbstractDomainObject class
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKESPECIAL, superDesc, "<init>", CONSTRUCTOR_DESC);

		if (isDomainBaseClass(descToName(classDesc))) {
		    // for base classes, we must invoke the initInstance()
		    // method
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
