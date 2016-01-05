package org.zhz.dfargx.node;

/**
 * Created on 2015/5/10.
 */
public abstract class BranchNode extends Node {

	public abstract int getPri();

	public void operate(Node left, Node right) {
		setLeft(left);
		setRight(right);
	}
}
