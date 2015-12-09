/*
 * Copyright (C) 2014 Francis Galiegue <fgaliegue@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.fge.grappa.stack;

import com.google.common.collect.Lists;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public final class DefaultValueStackTest
{
    private ValueStack<Object> stack;

    @BeforeMethod
    public void initStack()
    {
        stack = new DefaultValueStack<>();
    }

    @Test
    public void defaultStackIsEmptyAndHasZeroSize()
    {
        assertThat(stack.isEmpty()).as("new stack should be empty").isTrue();
        assertThat(stack.size()).as("new stack should have size 0")
            .isEqualTo(0);
    }

    @Test
    public void cannotPeekPopPokeDupFromEmptyStack()
    {
        try {
            stack.peek();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException ignored) {
        }

        try {
            stack.pop();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException ignored) {
        }

        try {
            stack.poke(new Object());
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException ignored) {
        }

        try {
            stack.dup();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException ignored) {
        }
    }

    @Test
    public void singleElementStackPushPeekPopPokeWorks()
    {
        Object element = new Object();
        SoftAssertions soft = new SoftAssertions();

        stack.push(element);

        soft.assertThat(stack.size()).as("stack has the correct size")
            .isEqualTo(1);
        soft.assertThat(stack.isEmpty())
            .as("stack with at least one element is not empty").isFalse();
        soft.assertThat(stack.peek()).as("peek() gives last push()ed")
            .isSameAs(element);
        soft.assertThat(stack.peek(0)).as("peek(0) is same as peek()")
            .isSameAs(element);
        soft.assertThat(stack.pop()).as("pop() gives last push()ed")
            .isSameAs(element);
        soft.assertThat(stack.isEmpty())
            .as("one-element stack popped from becomes empty").isTrue();

        stack.push(element);
        element = new Object();
        stack.poke(element);
        soft.assertThat(stack.peek()).as("poke() replaces the first element")
            .isSameAs(element);

        element = new Object();
        stack.poke(0, element);
        soft.assertThat(stack.pop()).as("poke(0) is the same as poke()")
            .isSameAs(element);

        soft.assertAll();
    }

    @Test
    public void multiPushPeekPopPokeDupAndClearWorks()
    {
        SoftAssertions soft = new SoftAssertions();
        Integer two = 2000000000;
        stack.push(1);
        stack.push(two);

        soft.assertThat(stack.size()).as("stack has the correct size")
            .isEqualTo(2);
        soft.assertThat(stack)
            .as("elements are in the correct order after single element pushes")
            .containsExactly(two, 1);

        stack.dup();
        soft.assertThat(stack.size()).as("stack has the correct size")
            .isEqualTo(3);
        soft.assertThat(stack)
            .as("elements are in the correct order after dup()")
            .containsExactly(two, two, 1);

        stack.pop();
        stack.push("helo");
        stack.push(3);
        soft.assertThat(stack.size()).as("stack has the correct size")
            .isEqualTo(4);
        soft.assertThat(stack)
            .as("elements are in the correct order after multi element push")
            .containsExactly(3, "helo", two, 1);

        Object element;

        element = stack.peek(2);
        soft.assertThat(element).as("down-peek() works correctly")
            .isSameAs(two);

        element = stack.pop(2);
        soft.assertThat(element).as("down-pop() works correctly")
            .isSameAs(two);
        soft.assertThat(stack.size()).as("stack has the correct size")
            .isEqualTo(3);
        soft.assertThat(stack)
            .as("elements are in the correct order after multi element push")
            .containsExactly(3, "helo", 1);

        stack.push("sally");
        stack.push("harry");
        soft.assertThat(stack.size()).as("stack has the correct size")
            .isEqualTo(5);
        soft.assertThat(stack)
            .as("elements are in the correct order after iterable element push")
            .containsExactly("harry", "sally", 3, "helo", 1);

        element = "meh";
        stack.poke(2, element);
        soft.assertThat(stack.size()).as("stack has the correct size")
            .isEqualTo(5);
        soft.assertThat(stack)
            .as("elements are in the correct order after element poke")
            .containsExactly("harry", "sally", element, "helo", 1);

        stack.push(5, 'x');
        soft.assertThat(stack.size()).as("stack has the correct size")
            .isEqualTo(6);
        soft.assertThat(stack)
            .as("elements are in the correct order after element poke")
            .containsExactly("harry", "sally", element, "helo", 1, 'x');

        stack.clear();
        soft.assertThat(stack.isEmpty()).as("cleared stack becomes empty")
            .isTrue();

        soft.assertAll();
    }

    @Test
    public void wrongIndicesYieldExpectedExceptions()
    {
        stack.push(1);
        stack.push(2);
        stack.push(3);

        try {
            stack.pop(3);
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException ignored) {
        }

        try {
            stack.poke(3, new Object());
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException ignored) {
        }

        try {
            stack.peek(3);
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException ignored) {
        }

        try {
            stack.push(4, new Object());
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException ignored) {
        }
    }

    @DataProvider
    public Iterator<Object[]> getSwapData()
    {
        List<Object[]> list = Lists.newArrayList();

        int n;
        List<Object> l;

        n = 2;
        l = Arrays.<Object>asList(2, 1, 3, 4, 5, 6);
        list.add(new Object[] { n, l });

        n = 3;
        l = Arrays.<Object>asList(3, 2, 1, 4, 5, 6);
        list.add(new Object[] { n, l });

        n = 4;
        l = Arrays.<Object>asList(4, 3, 2, 1, 5, 6);
        list.add(new Object[] { n, l });

        n = 5;
        l = Arrays.<Object>asList(5, 4, 3, 2, 1, 6);
        list.add(new Object[] { n, l });

        n = 6;
        l = Arrays.<Object>asList(6, 5, 4, 3, 2, 1);
        list.add(new Object[] { n, l });

        return list.iterator();
    }

    @Test(dataProvider = "getSwapData")
    public void swappingWorks(int n, List<Object> expected)
    {
        List<Object> orig = Arrays.<Object>asList(1, 2, 3, 4, 5, 6);
        SoftAssertions soft = new SoftAssertions();

        List<Object> l = new ArrayList<>(orig);
        Collections.reverse(l);
        for (Object o: l)
            stack.push(o);

        stack.swap(n);
        soft.assertThat(stack).as("swap of " + n + " works correctly")
            .containsExactlyElementsOf(expected);

        stack.swap(n);
        soft.assertThat(stack)
            .as("double swap of " + n + " gives back the original")
            .containsExactlyElementsOf(orig);

        soft.assertAll();
    }

    @Test
    public void iteratorReturnedByStackDoesNotSupportRemovals()
    {
        stack.push(1);

        try {
            Iterator<Object> iterator = stack.iterator();
            iterator.next();
            iterator.remove();
            failBecauseExceptionWasNotThrown(
                UnsupportedOperationException.class);
        } catch (UnsupportedOperationException ignored) {
        }
    }

    @Test
    public void snapshotAndRestoreWorksAsExpected()
    {
        List<Object> orig = Arrays.<Object>asList(1, 2, 3);
        List<Object> replace = Arrays.<Object>asList(4, 5, 6);
        SoftAssertions soft = new SoftAssertions();

        stack.push(3);
        stack.push(2);
        stack.push(1);

        Object snapshot = stack.takeSnapshot();
        Object poison = Lists.newLinkedList();

        try {
            stack.restoreSnapshot(poison);
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (Exception ignored) {
        }

        stack.clear();
        stack.push(6);
        stack.push(5);
        stack.push(4);

        soft.assertThat(stack)
            .as("stack contents are correct after snapshot plus modifications")
            .containsExactlyElementsOf(replace);

        stack.restoreSnapshot(snapshot);
        soft.assertThat(stack)
            .as("stack contents are completely restored from snapshot")
            .containsExactlyElementsOf(orig);

        soft.assertAll();
    }
}
