package yohanes.nlp.tools.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yohanes on 12/04/16.
 */
public class Tree {

    private String value;
    private List<Tree> children;
    private Tree parent;

    public Tree () {
        this.children = new ArrayList<Tree>();
    }

    public Tree (String value) {
        this.value = value;
        this.children = new ArrayList<Tree>();
    }

    public Tree (String value, List<Tree> children) {
        this.value = value;
        this.children = children;
    }

    public String getValue() {
        return value != null ? value : "";
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<Tree> getChildren() {
        return children;
    }

    public void setChildren(List<Tree> children) {
        this.children = children;
        for (Tree child:this.children) {
            child.setParent(this);
        }
    }

    public void addChild(Tree child) {
        child.setParent(this);
        this.children.add(child);
    }

    public Tree getParent() {
        return parent;
    }

    public void setParent(Tree parent) {
        this.parent = parent;
    }

    public boolean isLeaf() {
        return this.children.isEmpty();
    }

    public boolean areAllChildrenLeaves() {
        boolean leaf = true;
        for (Tree child:this.children) {
            leaf = leaf && child.isLeaf();
        }
        return leaf;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.children.isEmpty()) {
            sb.append(this.getValue());
        } else {
            sb.append("(").append(this.getValue());
            for (Tree child:this.children) {
                sb.append(" ").append(child.toString());
            }
            sb.append(")");
        }
        return sb.toString();
    }

    public static Tree parse(String str) {
        Tree node = null;
        Tree prev = null;
        str = str.replace("\t", "").trim();
        char prevc = '\0';
        String value = "";
        for (char c:str.toCharArray()) {
            if (c == '(') {
                prev = node;
                node = new Tree();
                if (prev != null) {
                    prev.addChild(node);
                }
                prevc = c;
            } else if (c == ')') {
                if (prevc == '(') {
                    node.setValue(value);
                    value = "";
                    // special handling for compound word
                    // if siblings are leaves then wrap each of them with parent value
                    if (prev.getParent().areAllChildrenLeaves()) {
                        Tree parent = prev.getParent();
                        ArrayList<Tree> newChildren = new ArrayList<Tree>();
                        for (Tree child:parent.getChildren()) {
                            newChildren.add(new Tree(parent.getValue(), Arrays.asList(child)));
                        }
                        parent.setChildren(newChildren);
                    }
                }
                node = (node.getParent() != null) ? node.getParent() : node;
                prevc = c;
            } else if (prevc == '(' && (c == ' ' || c == '\0' || c == '\t')) {
                node.setValue(value);
                value = "";
                prevc = c;
            } else if (c != ' ' && c != '\0' && c != '\t') {
                value += c;
                // special handling for compound word
                // by adding it to same parent first
                if (prevc == ' ') {
                    prev = node;
                    node = new Tree();
                    prev.getParent().addChild(node);
                    prevc = '(';
                }
            }
        }
        return node;
    }

}
