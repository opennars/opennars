/**
 * Copyright (C) 2009 Future Invent Informationsmanagement GmbH. All rights
 * reserved. <http://www.fuin.org/>
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package nars.obj.util.bytecode;

import objenome.util.bytecode.SgUtils;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Modifier;


// CHECKSTYLE:OFF
public class SgUtilsTest {

    /**
     * Merge two packages into one. If any package is null or empty no "." will
     * be added. If both packages are null an empty string will be returned.
     *
     * @param package1
     *            First package - Can also be null or empty.
     * @param package2
     *            Second package - Can also be null or empty.
     *
     * @return Both packages added with ".".
     */
    public static String concatPackages(String package1, String package2) {
        if ((package1 == null) || (package1.isEmpty())) {
            return (package2 == null) || (package2.isEmpty()) ? "" : package2;
        } else {
            return (package2 == null) || (package2.isEmpty()) ? package1 : package1 + '.' + package2;
        }
    }

    @Test
    public void concatPackages() {
        Assert.assertEquals(concatPackages(null, null), "");
        Assert.assertEquals(concatPackages(null, ""), "");
        Assert.assertEquals(concatPackages("", null), "");
        Assert.assertEquals(concatPackages("", ""), "");
        Assert.assertEquals(concatPackages("a", null), "a");
        Assert.assertEquals(concatPackages("a", ""), "a");
        Assert.assertEquals(concatPackages(null, "a"), "a");
        Assert.assertEquals(concatPackages("", "a"), "a");
        Assert.assertEquals(concatPackages("a", "b"), "a.b");
    }

    @Test
    public void uppercaseToUnderscore() {
        Assert.assertEquals(SgUtils.uppercaseToUnderscore(null), null);
        Assert.assertEquals(SgUtils.uppercaseToUnderscore(""), "");
        Assert.assertEquals(SgUtils.uppercaseToUnderscore("abcDef"), "abc_def");
        Assert.assertEquals(SgUtils.uppercaseToUnderscore("AbcDefG"), "abc_def_g");
        Assert.assertEquals(SgUtils.uppercaseToUnderscore("abc"), "abc");
        Assert.assertEquals(SgUtils.uppercaseToUnderscore("ABC"), "a_b_c");
    }

    @Test
    public void firstCharUpper() {
        Assert.assertEquals(SgUtils.firstCharUpper(null), null);
        Assert.assertEquals(SgUtils.firstCharUpper(""), "");
        Assert.assertEquals(SgUtils.firstCharUpper("a"), "A");
        Assert.assertEquals(SgUtils.firstCharUpper("abc"), "Abc");
    }
    
    @Test
    public void modifiersFromString() {

        Assert.assertEquals(SgUtils.toModifiers("abstract"), Modifier.ABSTRACT);
        Assert.assertEquals(SgUtils.toModifiers("final"), Modifier.FINAL);
        Assert.assertEquals(SgUtils.toModifiers("native"), Modifier.NATIVE);
        Assert.assertEquals(SgUtils.toModifiers("private"), Modifier.PRIVATE);
        Assert.assertEquals(SgUtils.toModifiers("protected"), Modifier.PROTECTED);
        Assert.assertEquals(SgUtils.toModifiers("public"), Modifier.PUBLIC);
        Assert.assertEquals(SgUtils.toModifiers("static"), Modifier.STATIC);
        Assert.assertEquals(SgUtils.toModifiers("synchronized"), Modifier.SYNCHRONIZED);
        Assert.assertEquals(SgUtils.toModifiers("transient"), Modifier.TRANSIENT);
        Assert.assertEquals(SgUtils.toModifiers("volatile"), Modifier.VOLATILE);
        Assert.assertEquals(SgUtils.toModifiers("strictfp"), Modifier.STRICT);

        int modifiers = SgUtils.toModifiers("abstract final native private protected public "
                + "static synchronized transient volatile strictfp");
        Assert.assertTrue(Modifier.isPublic(modifiers));
        Assert.assertTrue(Modifier.isFinal(modifiers));
        Assert.assertTrue(Modifier.isNative(modifiers));
        Assert.assertTrue(Modifier.isPrivate(modifiers));
        Assert.assertTrue(Modifier.isProtected(modifiers));
        Assert.assertTrue(Modifier.isPublic(modifiers));
        Assert.assertTrue(Modifier.isStatic(modifiers));
        Assert.assertTrue(Modifier.isSynchronized(modifiers));
        Assert.assertTrue(Modifier.isTransient(modifiers));
        Assert.assertTrue(Modifier.isVolatile(modifiers));
        Assert.assertTrue(Modifier.isStrict(modifiers));

        Assert.assertEquals(SgUtils.toModifiers(null), 0);
        Assert.assertEquals(SgUtils.toModifiers(""), 0);
        Assert.assertEquals(SgUtils.toModifiers(" "), 0);
        
    }
}
// CHECKSTYLE:ON
