package com.googlecode.opennars.language;

import java.net.URI;

public class URIRef extends Term {
	
	private URI uri;

	public URIRef(URI uri2) {
		super(uri2.toString().trim());
		uri = uri2;
	}

	/* (non-Javadoc)
	 * @see com.googlecode.opennars.language.Term#getConstantName()
	 */
	@Override
	public String getConstantName() {
		return uri.toString().trim();
	}

	/* (non-Javadoc)
	 * @see com.googlecode.opennars.language.Term#getName()
	 */
	@Override
	public String getName() {
		return uri.toString().trim();
	}

	/* (non-Javadoc)
	 * @see com.googlecode.opennars.language.Term#clone()
	 */
	@Override
	public Object clone() {
		return new URIRef(uri);
	}


}
