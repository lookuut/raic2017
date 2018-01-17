
import java.util.function.Predicate;

public class BTreeNodeCondition<E> extends BTreeBinaryNode {
    protected Predicate<E> condition;

    protected E e;

    public BTreeNodeCondition(Predicate<E> condition, E e) {
        super();
        this.condition = condition;
        this.e = e;
    }

    public BTreeNode getChild() {
        if (condition.test(this.e)) {
            return this.getTrueNode();
        } else {
            return this.getFalseNode();
        }
    }

    public boolean check () {
        return condition.test(this.e);
    }
}
