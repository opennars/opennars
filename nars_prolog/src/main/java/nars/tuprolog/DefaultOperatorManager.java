/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package nars.tuprolog;

/**
 *  This class defines an operate manager with
 *  some standard operators defined
 *
 */
@SuppressWarnings("serial")
public class DefaultOperatorManager extends Operators {
    
    public DefaultOperatorManager() {
        opNew(":-", "xfx", 1200);
        opNew("-->", "xfx", 1200);
        opNew(":-", "fx", 1200);
        opNew("?-", "fx", 1200);
        opNew(";", "xfy", 1100);
        opNew("->", "xfy", 1050);
        opNew(",", "xfy", 1000);
        opNew("\\+", "fy", 900);
        opNew("not", "fy", 900);
        opNew("=", "xfx", 700);
        opNew("\\=", "xfx", 700);
        opNew("==", "xfx", 700);
        opNew("\\==", "xfx", 700);
        //opNew("@==","xfx",700);
        //opNew("@\\==","xfx",700);
        opNew("@>", "xfx", 700);
        opNew("@<", "xfx", 700);
        opNew("@=<", "xfx", 700);
        opNew("@>=", "xfx", 700);
        opNew("=:=", "xfx", 700);
        opNew("=\\=", "xfx", 700);
        opNew(">", "xfx", 700);
        opNew("<", "xfx", 700);
        opNew("=<", "xfx", 700);
        opNew(">=", "xfx", 700);
        opNew("is", "xfx", 700);
        opNew("=..", "xfx", 700);
        //opNew("?","xfx",600);
        //opNew("@","xfx",550);
        opNew("+", "yfx", 500);
        opNew("-", "yfx", 500);
        opNew("/\\", "yfx", 500);
        opNew("\\/", "yfx", 500);
        opNew("*", "yfx", 400);
        opNew("/", "yfx", 400);
        opNew("//", "yfx", 400);
        opNew(">>", "yfx", 400);
        opNew("<<", "yfx", 400);
        opNew("rem", "yfx", 400);
        opNew("mod", "yfx", 400);
        opNew("**", "xfx", 200);
        opNew("^", "xfy", 200);
        opNew("\\", "fx", 200);
        opNew("-", "fy", 200);
    }
    
}