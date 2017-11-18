import java.util.ArrayList;
import java.util.List;

public class BehaviourTree<E> {
    protected BTreeNode<E> root;

    public void addRoot(BTreeNode<E> node) {
        this.root = node;
    }

    public BTreeAction getAction() {
        List<BTreeNode> visitedNodes = new ArrayList<>();
        return (BTreeAction)deepCircum(root, visitedNodes);
    }

    public BTreeNode<E> deepCircum(BTreeNode<E> currentNode, List<BTreeNode> visitedNodes) {

        if (visitedNodes.contains(currentNode)) {
            return null;
        }

        visitedNodes.add(currentNode);

        if (currentNode instanceof BTreeAction && ((BTreeAction) currentNode).isRun() == false) {
            return currentNode;
        } else if (((BTreeAction) currentNode).isRun() == true) {
            return null;
        }

        //@TODO реализовать не детерминированный выбор
        BTreeNode node = ((BTreeNodeCondition) currentNode).getChild();

        return deepCircum(node, visitedNodes);
    }
}
