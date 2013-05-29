package pt.ist.fenixframework.consistencyPredicates;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

/**
 * Checks that all consistency predicates receive no parameters, return a
 * primitive boolean value, and are public, protected, or private. Otherwise,
 * throws an <code>Error</code> during the compilation.
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes({ "pt.ist.fenixframework.pstm.consistencyPredicates.ConsistencyPredicate",
        "jvstm.cps.ConsistencyPredicate" })
public class ConsistencyPredicateAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final Set<Element> elements = (Set<Element>) roundEnv.getElementsAnnotatedWith(ConsistencyPredicate.class);
        elements.addAll(roundEnv.getElementsAnnotatedWith(jvstm.cps.ConsistencyPredicate.class));

        for (Element method : elements) {
            TypeElement classSymbol = (TypeElement) method.getEnclosingElement();
            ExecutableElement executableMethod = (ExecutableElement) method;
            if (!executableMethod.getParameters().isEmpty()) {
                throw new Error("Consistency Predicates cannot have parameters - " + classSymbol.getQualifiedName() + "."
                        + executableMethod.getSimpleName() + "()");
            }
            if (!executableMethod.getReturnType().getKind().equals(TypeKind.BOOLEAN)) {
                throw new Error("Consistency Predicates must return a primitive boolean value - "
                        + classSymbol.getQualifiedName() + "." + executableMethod.getSimpleName() + "()");
            }
            if (!executableMethod.getModifiers().contains(Modifier.PUBLIC)
                    && !executableMethod.getModifiers().contains(Modifier.PRIVATE)
                    && !executableMethod.getModifiers().contains(Modifier.PROTECTED)) {
                throw new Error("Consistency Predicates must be private, protected or public - " + classSymbol.getQualifiedName()
                        + "." + executableMethod.getSimpleName() + "()");
            }
        }

        return true;
    }

}
