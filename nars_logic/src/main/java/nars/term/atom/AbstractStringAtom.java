package nars.term.atom;

import nars.Op;

/**
 * Created by me on 12/4/15.
 */
public abstract class AbstractStringAtom extends AbstractStringAtomRaw {

	protected AbstractStringAtom(byte[] id) {
		this(id, null);
	}
	protected AbstractStringAtom(String id) {
		this(id, null);
	}

	protected AbstractStringAtom(byte[] id, Op specificOp) {
		this(new String(id), specificOp);
	}

	protected AbstractStringAtom(String id, Op specificOp) {
		super(id);
		// hash = Atom.hash(
		// id.hashCode(),
		// specificOp!=null ? specificOp : op()
		// );
	}

}
