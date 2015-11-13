package inc.glamdring.vtables;

class edgeString$CharSequence implements _edge<String,CharSequence> {

    public String demote(_edge<String, CharSequence> p) {
        return p.promote(p).toString();
    }

    public CharSequence promote(_edge<String, CharSequence> p) {
            return p.demote(p) ;  
    }

    public _edge<String, CharSequence> bind(final String s, final CharSequence charSequence) {
        return new _edge<String, CharSequence>() {
            public String demote(_edge<String, CharSequence> p) {
                return s;
            }

            public CharSequence promote(_edge<String, CharSequence> p) {
                return s;  
            }

            public _edge<String, CharSequence> bind(String s, CharSequence charSequence) {
                return edgeString$CharSequence.this.bind(s, charSequence);  
            }

            public String reify(_ptr void$) {
                return s;  
            }
        };  
    }

    public String reify(_ptr void$) {
        return void$.l$().asCharBuffer().toString();  //todo: verify for a purpose
    }

}