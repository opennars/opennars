package nars.tuprolog.gui.ide;

import nars.tuprolog.gui.edit.KeywordMap;
import nars.tuprolog.gui.edit.Token;
import nars.tuprolog.gui.edit.TokenMarker;

import javax.swing.text.Segment;

public class PrologTokenMarker extends TokenMarker
{
    public PrologTokenMarker()
    {
        this(true,getKeywords());
    }

    public PrologTokenMarker(boolean cpp,KeywordMap keywords)
    {
        this.cpp = cpp;
        this.keywords = keywords;
    }

    public byte markTokensImpl(byte token, Segment line, int lineIndex)
    {
        char[] array = line.array;
        int offset = line.offset;
        lastOffset = offset;
        lastKeyword = offset;
        int length = line.count + offset;
        boolean backslash = false;

loop:    for(int i = offset; i < length; i++)
        {
            int i1 = (i+1);

            char c = array[i];
            if(c == '\\')
            {
                backslash = !backslash;
                continue;
            }

            switch(token)
            {
            case Token.NULL:
                switch(c)
                {
                case '%':
                    if (backslash)
                        backslash = false;
                    doKeyword(line,i,c);
                    addToken(i - lastOffset,token);
                    addToken(length - i,Token.COMMENT1);
                    lastOffset = lastKeyword = length;
                    break loop;
                /* a future use for constraints?
                case '#':
                    if(backslash)
                        backslash = false;
                    else {
                        if(doKeyword(line,i,c))
                            break;
                        addToken(i - lastOffset,token);
                        addToken(length - i,Token.LABEL);
                        lastOffset = lastKeyword = length;
                        break loop;
                    }
                    break;
                */

                case '"':
                    doKeyword(line,i,c);
                    if(backslash)
                        backslash = false;
                    else
                    {
                        addToken(i - lastOffset,token);
                        token = Token.LITERAL1;
                        lastOffset = lastKeyword = i;
                    }
                    break;

                case '\'':
                    doKeyword(line,i,c);
                    if(backslash)
                        backslash = false;
                    else
                    {
                        addToken(i - lastOffset,token);
                        token = Token.LITERAL1;
                        lastOffset = lastKeyword = i;
                    }
                    break;
                case '[':
                    doKeyword(line,i,c);
                    if(backslash)
                        backslash = false;
                    else
                    {
                        addToken(i - lastOffset,token);
                        token = Token.LITERAL2;
                        lastOffset = lastKeyword = i;
                    }
                    break;
                /*
                case ':':
                    if(lastKeyword == offset)
                    {
                        if(doKeyword(line,i,c))
                            break;
                        backslash = false;
                        addToken(i1 - lastOffset,Token.LABEL);
                        lastOffset = lastKeyword = i1;
                    }
                    else if(doKeyword(line,i,c))
                        break;
                    break;
                */
                case '/':
                    backslash = false;
                    doKeyword(line,i,c);
                    if(length - i > 1)
                    {
                        switch(array[i1])
                        {
                        /* COMMENT2! */
                        case '*':
                            addToken(i - lastOffset,token);
                            lastOffset = lastKeyword = i;
                            if(length - i > 2 && array[i+2] == '*')
                                token = Token.COMMENT2;
                            else
                                token = Token.COMMENT1;
                            break;
                        /*
                        case '/':
                            addToken(i - lastOffset,token);
                            addToken(length - i,Token.COMMENT1);
                            lastOffset = lastKeyword = length;
                            break loop;
                        */
                        }
                    }
                    break;
                default:
                    backslash = false;
                    if(!Character.isLetterOrDigit(c)
                        && c != '_' && c != '!')
                        doKeyword(line,i,c);
                    break;
                }
                break;
            case Token.COMMENT1:
            /* COMMENT2! */
            case Token.COMMENT2:
                backslash = false;
                if(c == '*' && length - i > 1)
                {
                    if(array[i1] == '/')
                    {
                        i++;
                        addToken((i+1) - lastOffset,token);
                        token = Token.NULL;
                        lastOffset = lastKeyword = i+1;
                    }
                }
                break;
            case Token.LITERAL1:
                if(backslash)
                    backslash = false;
                else if(c == '"')
                {
                    addToken(i1 - lastOffset,token);
                    token = Token.NULL;
                    lastOffset = lastKeyword = i1;
                }
                else if(c == '\'')
                {
                    addToken(i1 - lastOffset,token);
                    token = Token.NULL;
                    lastOffset = lastKeyword = i1;
                }
                break;
            case Token.LITERAL2:
                if(backslash)
                    backslash = false;
                else if(c == ']')
                {
                    addToken(i1 - lastOffset,Token.LITERAL2);
                    token = Token.NULL;
                    lastOffset = lastKeyword = i1;
                }
                break;
            default:
                throw new InternalError("Invalid state: "
                    + token);
            }
        }

        if(token == Token.NULL)
            doKeyword(line,length,'\0');

        switch(token)
        {
        case Token.LITERAL1:
        case Token.LITERAL2:
            addToken(length - lastOffset,Token.INVALID);
            token = Token.NULL;
            break;
        case Token.KEYWORD2:
            addToken(length - lastOffset,token);
            if(!backslash)
                token = Token.NULL;
        default:
            addToken(length - lastOffset,token);
            break;
        }

        return token;
    }

    public static KeywordMap getKeywords()
    {
        if(libraryKeywords == null)
        {
            libraryKeywords = new KeywordMap(false);

            /* Predicates from BasicLibrary */

            libraryKeywords.add("abolish", Token.KEYWORD2);
            libraryKeywords.add("add_theory", Token.KEYWORD2);
            libraryKeywords.add("agent", Token.KEYWORD2);
            libraryKeywords.add("arg", Token.KEYWORD2);
            libraryKeywords.add("append", Token.KEYWORD2);
            libraryKeywords.add("assert", Token.KEYWORD2);
            libraryKeywords.add("asserta", Token.KEYWORD2);
            libraryKeywords.add("assertz", Token.KEYWORD2);
            libraryKeywords.add("atom", Token.KEYWORD2);
            libraryKeywords.add("atomic", Token.KEYWORD2);
            libraryKeywords.add("bagof", Token.KEYWORD2);
            libraryKeywords.add("call", Token.KEYWORD2);
            libraryKeywords.add("clause", Token.KEYWORD2);
            libraryKeywords.add("compound", Token.KEYWORD2);
            libraryKeywords.add("constant", Token.KEYWORD2);
            libraryKeywords.add("copy_term", Token.KEYWORD2);
            libraryKeywords.add("current_op", Token.KEYWORD2);
            libraryKeywords.add("current_prolog_flag", Token.KEYWORD2);
            libraryKeywords.add("delete", Token.KEYWORD2);
            libraryKeywords.add("element", Token.KEYWORD2);
            libraryKeywords.add("findall", Token.KEYWORD2);
            libraryKeywords.add("float", Token.KEYWORD2);
            libraryKeywords.add("functor", Token.KEYWORD2);
            libraryKeywords.add("get_theory", Token.KEYWORD2);
            libraryKeywords.add("ground", Token.KEYWORD2);
            libraryKeywords.add("integer", Token.KEYWORD2);
            libraryKeywords.add("length", Token.KEYWORD2);
            libraryKeywords.add("list", Token.KEYWORD2);
            libraryKeywords.add("member", Token.KEYWORD2);
            libraryKeywords.add("nonvar", Token.KEYWORD2);
            libraryKeywords.add("nospy", Token.KEYWORD1);
            libraryKeywords.add("not", Token.KEYWORD2);
            libraryKeywords.add("num_atom", Token.KEYWORD2);
            libraryKeywords.add("number", Token.KEYWORD2);
            libraryKeywords.add("once", Token.KEYWORD2);
            libraryKeywords.add("quicksort", Token.KEYWORD2);
            libraryKeywords.add("repeat", Token.KEYWORD1);
            libraryKeywords.add("retract", Token.KEYWORD2);
            libraryKeywords.add("retract_bt", Token.KEYWORD2);
            libraryKeywords.add("retract_nb", Token.KEYWORD2);
            libraryKeywords.add("reverse", Token.KEYWORD2);
            libraryKeywords.add("set_prolog_flag", Token.KEYWORD2);
            libraryKeywords.add("set_theory", Token.KEYWORD2);
            libraryKeywords.add("setof", Token.KEYWORD2);
            libraryKeywords.add("spy", Token.KEYWORD2);
            libraryKeywords.add("text_concat", Token.KEYWORD2);
            libraryKeywords.add("text_term", Token.KEYWORD2);
            libraryKeywords.add("unify_with_occurs_check", Token.KEYWORD2);
            libraryKeywords.add("var", Token.KEYWORD2);
            /*Castagna 16/09*/
	        libraryKeywords.add("trace", Token.KEYWORD2);
	        libraryKeywords.add("notrace", Token.KEYWORD1);
            /**/

            /* Predicates from ISOLibrary */

            libraryKeywords.add("atom_length", Token.KEYWORD2);
            libraryKeywords.add("atom_chars", Token.KEYWORD2);
            libraryKeywords.add("atom_codes", Token.KEYWORD2);
            libraryKeywords.add("atom_concat", Token.KEYWORD2);
            libraryKeywords.add("bound", Token.KEYWORD2);
            libraryKeywords.add("char_code", Token.KEYWORD2);
            libraryKeywords.add("number_chars", Token.KEYWORD2);
            libraryKeywords.add("number_codes", Token.KEYWORD2);
            libraryKeywords.add("sub_atom", Token.KEYWORD2);
            // mathematical functions
            libraryKeywords.add("abs", Token.KEYWORD2);
            libraryKeywords.add("atan", Token.KEYWORD2);
            libraryKeywords.add("ceiling", Token.KEYWORD2);
            libraryKeywords.add("cos", Token.KEYWORD2);
            libraryKeywords.add("div", Token.KEYWORD2);
               libraryKeywords.add("exp", Token.KEYWORD2);
            libraryKeywords.add("float_fractional_part", Token.KEYWORD2);
            libraryKeywords.add("float_integer_part", Token.KEYWORD2);
            libraryKeywords.add("floor", Token.KEYWORD2);
            libraryKeywords.add("log", Token.KEYWORD2);
            libraryKeywords.add("mod", Token.KEYWORD2);
            libraryKeywords.add("rem", Token.KEYWORD2);
            libraryKeywords.add("round", Token.KEYWORD2);
            libraryKeywords.add("sign", Token.KEYWORD2);
            libraryKeywords.add("sin", Token.KEYWORD2);
            libraryKeywords.add("sqrt", Token.KEYWORD2);
            libraryKeywords.add("truncate", Token.KEYWORD2);

            /* Predicates from IOLibrary */

            libraryKeywords.add("agent_file", Token.KEYWORD2);
            libraryKeywords.add("consult", Token.KEYWORD2);
            libraryKeywords.add("get", Token.KEYWORD2);
            libraryKeywords.add("get0", Token.KEYWORD2);
            libraryKeywords.add("nl", Token.KEYWORD1);
            libraryKeywords.add("put", Token.KEYWORD2);
            libraryKeywords.add("rand_float", Token.KEYWORD2);
            libraryKeywords.add("rand_int", Token.KEYWORD2);
            libraryKeywords.add("read", Token.KEYWORD2);
            libraryKeywords.add("see", Token.KEYWORD2);
            libraryKeywords.add("seeing", Token.KEYWORD2);
            libraryKeywords.add("seen", Token.KEYWORD1);
            libraryKeywords.add("solve_file", Token.KEYWORD2);
            libraryKeywords.add("tab", Token.KEYWORD2);
            libraryKeywords.add("tell", Token.KEYWORD2);
            libraryKeywords.add("telling", Token.KEYWORD2);
            libraryKeywords.add("text_from_file", Token.KEYWORD2);
            libraryKeywords.add("told", Token.KEYWORD1);
            libraryKeywords.add("write", Token.KEYWORD2);

            /* Predicates from JavaLibrary */

            libraryKeywords.add("as", Token.KEYWORD2);
            libraryKeywords.add("destroy_object", Token.KEYWORD2);
            libraryKeywords.add("java_array_get", Token.KEYWORD2);
            libraryKeywords.add("java_array_get_boolean", Token.KEYWORD2);
            libraryKeywords.add("java_array_get_byte", Token.KEYWORD2);
            libraryKeywords.add("java_array_get_char", Token.KEYWORD2);
            libraryKeywords.add("java_array_get_double", Token.KEYWORD2);
            libraryKeywords.add("java_array_get_float", Token.KEYWORD2);
            libraryKeywords.add("java_array_get_int", Token.KEYWORD2);
            libraryKeywords.add("java_array_get_long", Token.KEYWORD2);
            libraryKeywords.add("java_array_get_short", Token.KEYWORD2);
            libraryKeywords.add("java_array_length", Token.KEYWORD2);
            libraryKeywords.add("java_array_set", Token.KEYWORD2);
            libraryKeywords.add("java_array_set_boolean", Token.KEYWORD2);
            libraryKeywords.add("java_array_set_byte", Token.KEYWORD2);
            libraryKeywords.add("java_array_set_char", Token.KEYWORD2);
            libraryKeywords.add("java_array_set_double", Token.KEYWORD2);
            libraryKeywords.add("java_array_set_float", Token.KEYWORD2);
            libraryKeywords.add("java_array_set_int", Token.KEYWORD2);
            libraryKeywords.add("java_array_set_long", Token.KEYWORD2);
            libraryKeywords.add("java_array_set_short", Token.KEYWORD2);
            libraryKeywords.add("java_call", Token.KEYWORD2);
            libraryKeywords.add("java_class", Token.KEYWORD2);
            libraryKeywords.add("java_object", Token.KEYWORD2);
            libraryKeywords.add("java_object_bt", Token.KEYWORD2);
            libraryKeywords.add("java_object_nb", Token.KEYWORD2);
            libraryKeywords.add("java_object_string", Token.KEYWORD2);
            libraryKeywords.add("returns", Token.KEYWORD2);

            /* other 0-arity predicates not belonging to any particular library */
            libraryKeywords.add("!", Token.KEYWORD1);
            libraryKeywords.add("at_the_end_of_stream", Token.KEYWORD1);
            libraryKeywords.add("fail", Token.KEYWORD1);
            libraryKeywords.add("halt", Token.KEYWORD1);
            libraryKeywords.add("is", Token.KEYWORD1);
            libraryKeywords.add("true", Token.KEYWORD1);

            // singleton variable
            libraryKeywords.add("_", Token.KEYWORD3);

            /* ReSpecT keywords from...? */
/*
            libraryKeywords.add("reaction",Token.KEYWORD1);
            libraryKeywords.add("out",Token.KEYWORD3);
            libraryKeywords.add("in",Token.KEYWORD3);
            libraryKeywords.add("rd",Token.KEYWORD3);
            libraryKeywords.add("inp",Token.KEYWORD3);
            libraryKeywords.add("rdp",Token.KEYWORD3);
            libraryKeywords.add("out_r",Token.KEYWORD2);
            libraryKeywords.add("in_r",Token.KEYWORD2);
            libraryKeywords.add("no_r",Token.KEYWORD2);
            libraryKeywords.add("rd_r",Token.KEYWORD2);
            libraryKeywords.add("current_op",Token.KEYWORD2);
            libraryKeywords.add("current_agent",Token.KEYWORD2);
            libraryKeywords.add("current_tc",Token.KEYWORD2);
            libraryKeywords.add("pre",Token.KEYWORD2);
            libraryKeywords.add("post",Token.KEYWORD2);
            libraryKeywords.add("success",Token.KEYWORD2);
            libraryKeywords.add("failure",Token.KEYWORD2);
            // extensions
            libraryKeywords.add("current_time",Token.KEYWORD2);
            libraryKeywords.add("is",Token.KEYWORD2);
            libraryKeywords.add("spawn",Token.KEYWORD2);
            libraryKeywords.add("java",Token.LITERAL1);
            libraryKeywords.add("prolog",Token.LITERAL1);
            libraryKeywords.add("out_tc",Token.KEYWORD2);
            libraryKeywords.add("include",Token.LABEL);
*/
        }
        return libraryKeywords;
    }

    // private members
    private static KeywordMap libraryKeywords;

    @SuppressWarnings("unused")
    private boolean cpp;
    private KeywordMap keywords;
    private int lastOffset;
    private int lastKeyword;

    private boolean doKeyword(Segment line, int i, char c)
    {
        int i1 = i+1;

        int len = i - lastKeyword;
        byte id = keywords.lookup(line,lastKeyword,len);
        if(id != Token.NULL)
        {
            if(lastKeyword != lastOffset)
                addToken(lastKeyword - lastOffset,Token.NULL);
            addToken(len,id);
            lastOffset = i;
        }
        lastKeyword = i1;
        return false;
    }
}