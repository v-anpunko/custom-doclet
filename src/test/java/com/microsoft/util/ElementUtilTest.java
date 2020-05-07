package com.microsoft.util;

import com.google.testing.compile.CompilationRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ElementUtilTest {

    @Rule
    public CompilationRule rule = new CompilationRule();
    private Elements elements;
    private ElementUtil elementUtil;

    @Before
    public void setup() {
        elements = rule.getElements();
        elementUtil = new ElementUtil(
            new String[]{"samples\\.someexcludedpackage"},
            new String[]{"com\\.microsoft\\..*SomeExcludedClass"});
    }

    @Test
    public void extractPackageElements() {
        Set<? extends Element> elementsSet = new HashSet<>() {{
            add(elements.getPackageElement("com.microsoft.samples"));
            add(elements.getTypeElement("com.microsoft.samples.SuperHero"));
            add(elements.getPackageElement("com.microsoft.samples.subpackage"));
        }};

        List<String> result = elementUtil.extractPackageElements(elementsSet)
            .stream().map(String::valueOf).collect(Collectors.toList());

        assertThat("Wrong result list size", result.size(), is(2));
        assertThat("Unexpected first item", result.get(0), is("com.microsoft.samples"));
        assertThat("Unexpected second item", result.get(1), is("com.microsoft.samples.subpackage"));
    }

    @Test
    public void extractSortedElements() {
        Element element = elements.getPackageElement("com.microsoft.samples.subpackage");

        List<String> result = elementUtil.extractSortedElements(element)
            .stream().map(String::valueOf).collect(Collectors.toList());

        assertThat("Wrong result list size", result.size(), is(4));
        assertThat("Unexpected first item", result.get(0), is("com.microsoft.samples.subpackage.CustomException"));
        assertThat("Unexpected second item", result.get(1), is("com.microsoft.samples.subpackage.Display"));
        assertThat("Unexpected third item", result.get(2), is("com.microsoft.samples.subpackage.Person"));
    }

    @Test
    public void matchAnyPattern() {
        HashSet<Pattern> patterns = new HashSet<>(
            Arrays.asList(Pattern.compile("com\\.ms\\.Some.*"), Pattern.compile(".*UsualClass")));
        assertTrue(elementUtil.matchAnyPattern(patterns, "com.ms.SomeStrangeClass"));
        assertTrue(elementUtil.matchAnyPattern(patterns, "UsualClass"));
        assertFalse(elementUtil.matchAnyPattern(patterns, "EngineFive"));
        assertFalse(elementUtil.matchAnyPattern(patterns, "com.ms.Awesome"));
    }

    @Test
    public void isPackagePrivate() {
        Element element = elements.getTypeElement("com.microsoft.samples.SuperHero");

        List<Element> result = element.getEnclosedElements()
                .stream().filter(e -> ElementUtil.isPackagePrivate(e)).collect(Collectors.toList());

        assertThat("Wrong result list size", result.size(), is(2));
        assertThat("Unexpected package private field", String.valueOf(result.get(0)), is("hobby"));
        assertThat("Unexpected package private method", String.valueOf(result.get(1)), is("somePackagePrivateMethod()"));
    }

    @Test
    public void isPrivateOrPackagePrivate() {
        Element element = elements.getTypeElement("com.microsoft.samples.SuperHero");

        List<Element> result = element.getEnclosedElements()
                .stream().filter(e -> ElementUtil.isPrivateOrPackagePrivate(e)).collect(Collectors.toList());

        assertThat("Wrong result list size", result.size(), is(7));
        assertThat("Unexpected private method", String.valueOf(result.get(5)), is("somePrivateMethod()"));
        assertThat("Unexpected package private method", String.valueOf(result.get(6)), is("somePackagePrivateMethod()"));
    }
}
