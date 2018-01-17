
import java.util.ArrayList;
import java.util.List;

public class BTreeNode<E> {

    protected List<BTreeNode> childNodes;

    public BTreeNode () {
        childNodes = new ArrayList<>();
    }

    public BTreeNode getTrueNode() {
        return childNodes.get(0);
    }

    public BTreeNode getFalseNode() {
        return childNodes.get(1);
    }

    void setTrueNode(BTreeNode trueNode) {
        childNodes.set(0, trueNode);
    }

    void setFalseNode(BTreeNode falseNode) {
        childNodes.set(1, falseNode);
    }
}
