package inc.glamdring.vtables;

class edgeString$CharSequence implements _edge<String,CharSequence> {

    @Override
    public String demote(_edge<String, CharSequence> p) {
        return p.promote(p).toString();
    }

    @Override
    public CharSequence promote(_edge<String, CharSequence> p) {
            return p.demote(p) ;  
    }

    @Override
    public _edge<String, CharSequence> bind(String s, CharSequence charSequence) {
        return new _edge<String, CharSequence>() {
            @Override
            public String demote(_edge<String, CharSequence> p) {
                return s;
            }

            @Override
            public CharSequence promote(_edge<String, CharSequence> p) {
                return s;  
            }

            @Override
            public _edge<String, CharSequence> bind(String s, CharSequence charSequence) {
                return edgeString$CharSequence.this.bind(s, charSequence);  
            }

            @Override
            public String reify(_ptr void$) {
                return s;  
            }
        };  
    }

    @Override
    public String reify(_ptr void$) {
        return void$.l$().asCharBuffer().toString();  //todo: verify for a purpose
    }

}