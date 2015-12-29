package nars.cfg.bytecode;

import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//http://websvn.ow2.org/filedetails.php?repname=asm&path=%2Ftrunk%2Fasm%2Fexamples%2Fdependencies%2Fsrc%2Forg%2Fobjectweb%2Fasm%2Fdepend%2FDependencyVisitor.java


/**
 * DependencyVisitor
 *
 * @author Eugene Kuleshov
 */
public class DependencyVisitor extends ClassVisitor {
    Set<String> packages = new HashSet<>();

    Map<String, Map<String, Integer>> groups = new HashMap<>();

    Map<String, Integer> current;

    public Map<String, Map<String, Integer>> getGlobals() {
        return groups;
    }

    public Set<String> getPackages() {
        return packages;
    }

    public DependencyVisitor() {
        super(Opcodes.ASM5);
    }

    // ClassVisitor

    @Override
    public void visit(int version, int access, String name,
                      String signature, String superName,
                      String[] interfaces) {
        String p = getGroupKey(name);
        current = groups.get(p);
        if (current == null) {
            current = new HashMap<>();
            groups.put(p, current);
        }

        if (signature == null) {
            if (superName != null) {
                addInternalName(superName);
            }
            addInternalNames(interfaces);
        } else {
            addSignature(signature);
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc,
                                             boolean visible) {
        addDesc(desc);
        return new AnnotationDependencyVisitor();
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef,
                                                 TypePath typePath, String desc, boolean visible) {
        addDesc(desc);
        return new AnnotationDependencyVisitor();
    }

    @Override
    public FieldVisitor visitField(int access, String name,
                                   String desc, String signature, Object value) {
        if (signature == null) {
            addDesc(desc);
        } else {
            addTypeSignature(signature);
        }
        if (value instanceof Type) {
            addType((Type) value);
        }
        return new FieldDependencyVisitor();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name,
                                     String desc, String signature, String[] exceptions) {
        if (signature == null) {
            addMethodDesc(desc);
        } else {
            addSignature(signature);
        }
        addInternalNames(exceptions);
        return new MethodDependencyVisitor();
    }

    class AnnotationDependencyVisitor extends AnnotationVisitor {

        public AnnotationDependencyVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public void visit(String name, Object value) {
            if (value instanceof Type) {
                addType((Type) value);
            }
        }

        @Override
        public void visitEnum(String name, String desc,
                              String value) {
            addDesc(desc);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name,
                                                 String desc) {
            addDesc(desc);
            return this;
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            return this;
        }
    }

    class FieldDependencyVisitor extends FieldVisitor {

        public FieldDependencyVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            addDesc(desc);
            return new AnnotationDependencyVisitor();
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(int typeRef,
                                                     TypePath typePath, String desc,
                                                     boolean visible) {
            addDesc(desc);
            return new AnnotationDependencyVisitor();
        }
    }

    class MethodDependencyVisitor extends MethodVisitor {

        public MethodDependencyVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public AnnotationVisitor visitAnnotationDefault() {
            return new AnnotationDependencyVisitor();
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc,
                                                 boolean visible) {
            addDesc(desc);
            return new AnnotationDependencyVisitor();
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(int typeRef,
                                                     TypePath typePath, String desc,
                                                     boolean visible) {
            addDesc(desc);
            return new AnnotationDependencyVisitor();
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(int parameter,
                                                          String desc, boolean visible) {
            addDesc(desc);
            return new AnnotationDependencyVisitor();
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            addType(Type.getObjectType(type));
        }

        @Override
        public void visitFieldInsn(int opcode, String owner,
                                   String name, String desc) {
            addInternalName(owner);
            addDesc(desc);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner,
                                    String name, String desc, boolean itf) {
            addInternalName(owner);
            addMethodDesc(desc);
        }

        @Override
        public void visitInvokeDynamicInsn(String name, String desc,
                                           Handle bsm, Object... bsmArgs) {
            addMethodDesc(desc);
            addConstant(bsm);
            for (Object bsmArg : bsmArgs) {
                addConstant(bsmArg);
            }
        }

        @Override
        public void visitLdcInsn(Object cst) {
            addConstant(cst);
        }

        @Override
        public void visitMultiANewArrayInsn(String desc, int dims) {
            addDesc(desc);
        }

        @Override
        public AnnotationVisitor visitInsnAnnotation(int typeRef,
                                                     TypePath typePath, String desc, boolean visible) {
            addDesc(desc);
            return new AnnotationDependencyVisitor();
        }

        @Override
        public void visitLocalVariable(String name, String desc,
                                       String signature, Label start, Label end,
                                       int index) {
            addTypeSignature(signature);
        }

        @Override
        public AnnotationVisitor visitLocalVariableAnnotation(int typeRef,
                                                              TypePath typePath, Label[] start, Label[] end, int[] index,
                                                              String desc, boolean visible) {
            addDesc(desc);
            return new AnnotationDependencyVisitor();
        }

        @Override
        public void visitTryCatchBlock(Label start, Label end,
                                       Label handler, String type) {
            if (type != null) {
                addInternalName(type);
            }
        }

        @Override
        public AnnotationVisitor visitTryCatchAnnotation(int typeRef,
                                                         TypePath typePath, String desc, boolean visible) {
            addDesc(desc);
            return new AnnotationDependencyVisitor();
        }
    }

    class SignatureDependencyVisitor extends SignatureVisitor {

        String signatureClassName;

        public SignatureDependencyVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public void visitClassType(String name) {
            signatureClassName = name;
            addInternalName(name);
        }

        @Override
        public void visitInnerClassType(String name) {
            signatureClassName = signatureClassName + '$' + name;
            addInternalName(signatureClassName);
        }
    }

    // ---------------------------------------------

    private String getGroupKey(String name) {
        int n = name.lastIndexOf('/');
        if (n > -1) {
            name = name.substring(0, n);
        }
        packages.add(name);
        return name;
    }

    private void addName(String name) {
        if (name == null) {
            return;
        }
        String p = getGroupKey(name);
        if (current.containsKey(p)) {
            current.put(p, current.get(p) + 1);
        } else {
            current.put(p, 1);
        }
    }

    void addInternalName(String name) {
        addType(Type.getObjectType(name));
    }

    private void addInternalNames(String[] names) {
        for (int i = 0; names != null && i < names.length; i++) {
            addInternalName(names[i]);
        }
    }

    void addDesc(String desc) {
        addType(Type.getType(desc));
    }

    void addMethodDesc(String dscrptor) {
        addType(Type.getReturnType(dscrptor));

        Type[] types = Type.getArgumentTypes(dscrptor);
        for (Type type : types) {
            addType(type);
        }

    }

    void addType(Type t) {
        switch (t.getSort()) {
            case Type.ARRAY:
                addType(t.getElementType());
                break;
            case Type.OBJECT:
                addName(t.getInternalName());
                break;
            case Type.METHOD:
                addMethodDesc(t.getDescriptor());

                break;
            default:
                System.out.println("Unknown: " + t);
                break;
        }
    }

    private void addSignature(String signature) {
        if (signature != null) {
            new SignatureReader(signature)
                    .accept(new SignatureDependencyVisitor());
        }
    }

    void addTypeSignature(String signature) {
        if (signature != null) {
            new SignatureReader(signature)
                    .acceptType(new SignatureDependencyVisitor());
        }
    }

    void addConstant(Object cst) {
        if (cst instanceof Type) {
            addType((Type) cst);
        } else if (cst instanceof Handle) {
            Handle h = (Handle) cst;
            addInternalName(h.getOwner());
            addMethodDesc(h.getDesc());
        }
    }
}
