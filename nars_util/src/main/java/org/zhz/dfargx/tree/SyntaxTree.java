package org.zhz.dfargx.tree;

import org.zhz.dfargx.stack.OperatingStack;
import org.zhz.dfargx.stack.ShuntingStack;
import org.zhz.dfargx.tree.node.*;
import org.zhz.dfargx.tree.node.bracket.LeftBracket;
import org.zhz.dfargx.tree.node.bracket.RightBracket;
import org.zhz.dfargx.util.CommonSets;
import org.zhz.dfargx.util.InvalidSyntaxException;

import java.util.*;

/**
 * Created on 2015/5/8.
 */
public class SyntaxTree {
    private String regex;
    private boolean itemTerminated;
    private List<Node> nodeList;
    private Stack<Node> nodeStack;

    private Node root;

    public SyntaxTree(String regex) {
        root = null;
        this.regex = regex;
        nodeList = new ArrayList<>();
        itemTerminated = false;
        normalize();
//        System.out.println(nodeList);
        shunt();
//        System.out.println(nodeStack);
        buildTree();
    }

    private void buildTree() {
        OperatingStack operatingStack = new OperatingStack();
        while (!nodeStack.isEmpty()) {
            Node node = nodeStack.pop();
            node.accept(operatingStack);
        }
        try {
            root = operatingStack.pop();
        } catch (EmptyStackException e) {
            throw new InvalidSyntaxException(e);
        }
        if (!operatingStack.isEmpty()) {
            throw new InvalidSyntaxException();
        }
    }

    private void shunt() {
        ShuntingStack shuntingStack = new ShuntingStack();
        for (Node node : nodeList) {
            node.accept(shuntingStack);
        }
        nodeStack = shuntingStack.finish();
    }

    private void normalize() {
        int index = 0;
        while (index < regex.length()) {
            char ch = regex.charAt(index++);
            switch (ch) {
                case '[': {
                    tryConcat();
                    List<Character> all = new ArrayList<>();
                    boolean isComplementarySet;
                    if (regex.charAt(index) == '^') {
                        isComplementarySet = true;
                        index++;
                    } else isComplementarySet = false;
                    for (char next = regex.charAt(index++); next != ']'; next = regex.charAt(index++)) {
                        if (next == '\\' || next == '.') {
                            String token;
                            if (next == '\\') {
                                char nextNext = regex.charAt(index++);
                                token = new String(new char[]{next, nextNext});
                            } else token = String.valueOf(next);
                            List<Character> tokenSet = CommonSets.interpretToken(token);
                            all.addAll(tokenSet);
                        } else all.add(next);
                    }
                    char[] chSet = CommonSets.minimum(CommonSets.listToArray(all));
                    if (isComplementarySet) {
                        chSet = CommonSets.complementarySet(chSet);
                    }
                    nodeList.add(new LeftBracket());
                    for (int i = 0; i < chSet.length; i++) {
                        nodeList.add(new LChar(chSet[i]));
                        if (i == chSet.length - 1 || chSet[i + 1] == 0) break;
                        nodeList.add(new BOr());
                    }
                    nodeList.add(new RightBracket());
                    itemTerminated = true;
                    break;
                }
                case '{': {
                    int least;
                    int most = -1;
                    boolean deterministicLength = false;
                    StringBuilder sb = new StringBuilder();
                    for (char next = regex.charAt(index++); ; ) {
                        sb.append(next);
                        next = regex.charAt(index++);
                        if (next == '}') {
                            deterministicLength = true;
                            break;
                        } else if (next == ',') {
                            break;
                        }
                    }
                    least = Integer.parseInt(sb.toString());

                    if (!deterministicLength) {
                        char next = regex.charAt(index);
                        if (next != '}') {
                            sb = new StringBuilder();
                            for (char nextNext = regex.charAt(index++); nextNext != '}'; nextNext = regex.charAt(index++)) {
                                sb.append(nextNext);
                            }
                            if (sb.length() != 0) {
                                most = Integer.parseInt(sb.toString());
                            }
                        }
                    } else most = least;

                    performMany(least, most);
                    itemTerminated = true;
                    break;
                }
                case '(': {
                    tryConcat();
                    nodeList.add(new LeftBracket());
                    itemTerminated = false;
                    break;
                }
                case ')': {
                    nodeList.add(new RightBracket());
                    itemTerminated = true;
                    break;
                }
                case '*': {
                    performMany(0, -1);
                    itemTerminated = true;
                    break;
                }
                case '?': {
                    performMany(0, 1);
                    itemTerminated = true;
                    break;
                }
                case '+': {
                    performMany(1, -1);
                    itemTerminated = true;
                    break;
                }
                case '|': {
                    nodeList.add(new BOr());
                    itemTerminated = false;
                    break;
                }
                default: {
                    tryConcat();
                    if (ch == '\\' || ch == '.') {
                        String token;
                        if (ch == '\\') {
                            char next = regex.charAt(index++);
                            token = new String(new char[]{ch, next});
                        } else token = String.valueOf(ch);
                        List<Character> tokenSet = CommonSets.interpretToken(token);
                        nodeList.add(new LeftBracket());
                        nodeList.add(new LChar(tokenSet.get(0)));
                        for (int i = 1; i < tokenSet.size(); i++) {
                            nodeList.add(new BOr());
                            nodeList.add(new LChar(tokenSet.get(i)));
                        }
                        nodeList.add(new RightBracket());
                    } else nodeList.add(new LChar(ch));

                    itemTerminated = true;
                    break;
                }
            }
        }
    }

    // look back for a completed term
    private void performMany(int least, int most) {
        if (!(least == 1 && most == 1)) {
            if (least == 0 && most == -1) {
                nodeList.add(new BMany());
                nodeList.add(new LNull());
            } else {
                List<Node> sample;
                if (last() instanceof RightBracket) {
                    sample = new LinkedList<>();
                    sample.add(nodeList.remove(nodeList.size() - 1));
                    int stack = 1;
                    for (int i = nodeList.size() - 1; i >= 0; i--) {
                        Node node = nodeList.remove(i);
                        if (node instanceof RightBracket) {
                            stack++;
                        } else if (node instanceof LeftBracket) {
                            stack--;
                        }
                        sample.add(0, node);
                        if (stack == 0) {
                            break;
                        }
                    }
                } else sample = Collections.singletonList(nodeList.remove(nodeList.size() - 1));

                if (most == -1) {
                    for (int i = 0; i < least; i++) {
                        nodeList.addAll(copyNodes(sample));
                        nodeList.add(new BConcat());
                    }
                    nodeList.addAll(copyNodes(sample));
                    nodeList.add(new BMany());
                    nodeList.add(new LNull());
                } else {
                    if (least != most) {
                        nodeList.add(new LeftBracket());
                        for (int i = least; i <= most; i++) {
                            nodeList.add(new LeftBracket());
                            if (i == 0) {
                                nodeList.add(new LClosure());
                            } else {
                                for (int j = 0; j < i; j++) {
                                    nodeList.addAll(copyNodes(sample));
                                    if (j != i - 1) {
                                        nodeList.add(new BConcat());
                                    }
                                }
                            }
                            nodeList.add(new RightBracket());
                            if (i != most) {
                                nodeList.add(new BOr());
                            }
                        }
                        nodeList.add(new RightBracket());
                    } else {
                        nodeList.add(new LeftBracket());
                        for (int i = 0; i < least; i++) {
                            nodeList.addAll(copyNodes(sample));
                            if (i != least - 1) {
                                nodeList.add(new BConcat());
                            }
                        }
                        nodeList.add(new RightBracket());
                    }
                }
            }
        }
    }

    public List<Node> copyNodes(List<Node> sample) {
        List<Node> result = new ArrayList<>(sample.size());
        for (Node node : sample) {
            result.add(node.copy());
        }
        return result;
    }

    private Node last() {
        return nodeList.get(nodeList.size() - 1);
    }


    private void tryConcat() {
        if (itemTerminated) {
            nodeList.add(new BConcat());
            itemTerminated = false;
        }
    }

    public Node getRoot() {
        return root;
    }

}
