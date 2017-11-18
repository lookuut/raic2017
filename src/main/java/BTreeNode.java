import java.util.ArrayList;
import java.util.List;

public class BTreeNode<E> {

    protected BTreeNode<E> parentNode;
    protected List<BTreeNode> childNodes;

    public BTreeNode () {
        childNodes = new ArrayList<>();
    }

    public void addChildNode(BTreeNode node) {
        childNodes.add(node);
    }

    public List<BTreeNode> getChildNodes() {
        return childNodes;
    }

}
