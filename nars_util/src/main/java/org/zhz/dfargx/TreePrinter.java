package org.zhz.dfargx;

import org.zhz.dfargx.node.LNull;
import org.zhz.dfargx.node.Node;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Supplier;

import static java.lang.System.out;

/**
 * Created on 2015/5/9.
 */
public class TreePrinter {

    private static final TreePrinter instance = new TreePrinter();

    public static TreePrinter getInstance() {
        return instance;
    }

    public void printTreeCopied(Node root) {
        printTree(copyTree(root));
    }

    public void printTree(Node root) {
        int h = calculateDepth(root, 0);
        complete(root, 0, h, LNull::new);
        Queue<Node> q = new ArrayDeque<>();
        q.offer(root);
        int t = 1;
        int j = 0;
        while (!q.isEmpty()) {
            Node node = q.poll();
            int width = 4 * (int) Math.pow(2, h - t);
            if (node instanceof LNull) {
                for (int i = 0; i < width * 2; i++) {
                    out.print(' ');
                }
            } else {
                String s = node.toString();
                for (int i = 0; i < width; i++) {
                    if (!(node.left() instanceof LNull)) {
                        if (i < width / 2 || t == h) {
                            out.print(' ');
                        } else if (i == width / 2) {
                            out.print('|');
                        } else out.print('-');
                    } else out.print(' ');
                }
                out.print(s);
                for (int i = s.length(); i < width; i++) {
                    if (!(node.right() instanceof LNull)) {
                        if (i < width / 2 && t != h) {
                            out.print('-');
                        } else if (i == width / 2 && t != h) {
                            out.print('|');
                        } else out.print(' ');
                    } else out.print(' ');
                }
            }
            if (++j == Math.pow(2, t) - 1) {
                out.println();
                t += 1;
            }
            node.ifHasLeft(q::offer);
            node.ifHasRight(q::offer);
        }
    }

    private void complete(Node node, int k, int h) {
        complete(node, k, h, LNull::new);
    }

    private void complete(Node node, int k, int h, Supplier<Node> fillEmptyWith) {
        if (k + 1 < h) {
            node.ifEmptyLeft(fillEmptyWith);
            complete(node.left(), k + 1, h, fillEmptyWith);
            node.ifEmptyRight(fillEmptyWith);
            complete(node.right(), k + 1, h, fillEmptyWith);
        }
    }

    private int calculateDepth(Node node, int depth) {
        depth += 1;
        int l = depth, r = depth;
        if (node.hasLeft()) {
            l = calculateDepth(node.left(), depth);
        }
        if (node.hasRight()) {
            r = calculateDepth(node.right(), depth);
        }
        return l > r ? l : r > depth ? r : depth;
    }

    private Node copyTree(Node root) {
        Node newRoot = root.copy();
        if (root.hasLeft()) {
            newRoot.setLeft(copyTree(root.left()));
        }
        if (root.hasRight()) {
            newRoot.setRight(copyTree(root.right()));
        }
        return newRoot;
    }
}
