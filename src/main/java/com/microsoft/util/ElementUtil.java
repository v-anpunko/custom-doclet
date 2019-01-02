package com.microsoft.util;

import com.microsoft.model.TypeParameter;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree.Kind;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import jdk.javadoc.doclet.DocletEnvironment;
import org.apache.commons.lang3.StringUtils;

public class ElementUtil {

    private static Map<ElementKind, String> elementKindLookup = new HashMap<>() {{
        put(ElementKind.PACKAGE, "Namespace");
        put(ElementKind.CLASS, "Class");
        put(ElementKind.ENUM, "Enum");
        put(ElementKind.ENUM_CONSTANT, "Enum constant");
        put(ElementKind.INTERFACE, "Interface");
        put(ElementKind.ANNOTATION_TYPE, "Interface");
        put(ElementKind.CONSTRUCTOR, "Constructor");
        put(ElementKind.METHOD, "Method");
        put(ElementKind.FIELD, "Field");
    }};
    private static Map<String, String> typeParamsLookup = new HashMap<>();
    private static DocletEnvironment environment;
    private static final Set<Pattern> excludePackages = new HashSet<>();
    private static final Set<Pattern> excludeClasses = new HashSet<>();

    public ElementUtil(DocletEnvironment environment, String[] excludePackages, String[] excludeClasses) {
        this.environment = environment;

        this.excludePackages.addAll(Stream.of(excludePackages)
            .map(o -> Pattern.compile(o)).collect(Collectors.toSet()));
        this.excludeClasses.addAll(Stream.of(excludeClasses)
            .map(o -> Pattern.compile(o)).collect(Collectors.toSet()));
    }

    public static String extractType(Element element) {
        return elementKindLookup.get(element.getKind());
    }

    public static List<TypeElement> extractSortedElements(Element element) {
        // Need to apply sorting, because order of result items for Element.getEnclosedElements() depend on JDK implementation
        return ElementFilter.typesIn(element.getEnclosedElements()).stream()
            .filter(o -> !matchAnyPattern(excludeClasses, String.valueOf(o.getQualifiedName())))
            .sorted((o1, o2) ->
                StringUtils.compare(String.valueOf(o1.getSimpleName()), String.valueOf(o2.getSimpleName()))
            ).collect(Collectors.toList());
    }

    public static List<PackageElement> extractPackageElements(Set<? extends Element> elements) {
        return ElementFilter.packagesIn(elements).stream()
            .filter(o -> !matchAnyPattern(excludePackages, String.valueOf(o)))
            .sorted((o1, o2) ->
                StringUtils.compare(String.valueOf(o1.getSimpleName()), String.valueOf(o2.getSimpleName()))
            ).collect(Collectors.toList());
    }

    public static List<TypeParameter> extractTypeParameters(TypeElement element) {
        List<TypeParameter> result = new ArrayList<>();
        for (TypeParameterElement typeParameter : element.getTypeParameters()) {
            String key = String.valueOf(typeParameter);
            if (!typeParamsLookup.containsKey(key)) {
                typeParamsLookup.put(key, generateHexString(key));
            }
            String value = typeParamsLookup.get(key);
            result.add(new TypeParameter(key, value));
        }
        return result;
    }

    static String generateHexString(String key) {
        return String.valueOf(key.hashCode());
    }

    public static String convertFullNameToOverload(String fullName) {
        return fullName.replaceAll("\\(.*\\)", "*");
    }

    public static String extractSuperclass(TypeElement classElement) {
        TypeMirror superclass = classElement.getSuperclass();
        if (superclass.getKind() == TypeKind.NONE) {
            return "java.lang.Object";
        }
        return String.valueOf(superclass);
    }

    public static String extractPackageContent(PackageElement packageElement) {
        return "package " + packageElement.getQualifiedName();
    }

    public static String extractClassContent(TypeElement classElement, String shortNameWithGenericsSupport) {
        String type = elementKindLookup.get(classElement.getKind());
        return String.format("%s %s %s",
            classElement.getModifiers().stream().map(String::valueOf)
                .filter(modifier -> !("Interface".equals(type) && "abstract".equals(modifier)))
                .filter(modifier -> !("Enum".equals(type) && ("static".equals(modifier) || "final".equals(modifier))))
                .collect(Collectors.joining(" ")),
            StringUtils.lowerCase(type), shortNameWithGenericsSupport);
    }

    public static String extractComment(Element element) {
        return getDocCommentTree(element).map(docTree -> docTree.getFullBody().stream()
            .map(o -> {
                if (o.getKind() == Kind.LINK) {
                    return replaceLinkWithXrefTag(String.valueOf(o));
                }
                return String.valueOf(o);
            }).collect(Collectors.joining())
        ).orElse(null);
    }

    static String replaceLinkWithXrefTag(String text) {
        text = StringUtils.remove(text, "{@link ");
        text = StringUtils.remove(text, "}");
        String uidContent = "";         // TODO: determine uid content
        return "<xref uid=\"" + uidContent + "\" data-throw-if-not-resolved=\"false\">" + text + "</xref>";
    }

    public static String extractPackageName(Element element) {
        return String.valueOf(environment.getElementUtils().getPackageOf(element));
    }

    public static Optional<DocCommentTree> getDocCommentTree(Element element) {
        return Optional.ofNullable(environment.getDocTrees().getDocCommentTree(element));
    }

    static boolean matchAnyPattern(Set<Pattern> patterns, String stringToCheck) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(stringToCheck).matches()) {
                return true;
            }
        }
        return false;
    }
}
