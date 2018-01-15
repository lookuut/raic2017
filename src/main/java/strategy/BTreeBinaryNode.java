package strategy;

public class BTreeBinaryNode extends BTreeNode {
    private BTreeNode trueNode;
    private BTreeNode falseNode;

    void setTrueNode(BTreeNode trueNode) {
        this.trueNode = trueNode;
    }

    void setFalseNode(BTreeNode falseNode) {
        this.falseNode = falseNode;
    }

    public BTreeNode getTrueNode() {
        return trueNode;
    }

    public BTreeNode getFalseNode() {
        return falseNode;
    }
}
