import java.util.function.Predicate;

public class BTreeNodeCondition<E> extends BTreeNode<E> {
    protected Predicate<E> condition;

    protected E e;

    public BTreeNodeCondition(Predicate<E> condition, E e) {
        super();
        this.condition = condition;
        this.e = e;
    }


    public void setPredicate(Predicate<E> condition, E e) {
        this.condition = condition;
        this.e = e;
    }

    public BTreeNode getChild() {
        if (condition.test(this.e)) {
            return this.childNodes.get(0);
        } else {
            return this.childNodes.get(1);
        }
    }

    public boolean check () {
        return condition.test(this.e);
    }
}
