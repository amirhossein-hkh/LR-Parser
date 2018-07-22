package lr1;

import lr0.LR0Item;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

public class LR1Item {

    private HashSet<String> lookahead;
    private String leftSide;
    private String[] rightSide;
    private int dotPointer;

    public LR1Item(String leftSide, String[] rightSide, int dotPointer, HashSet<String> lookahead){
        this.leftSide = leftSide;
        this.rightSide = rightSide;
        this.dotPointer = dotPointer;
        this.lookahead = lookahead;
    }

    public String getCurrent(){
        if(dotPointer == rightSide.length){
            return null;
        }
        return rightSide[dotPointer];
    }

    boolean goTo() {
        if (dotPointer >= rightSide.length) {
            return false;
        }
        dotPointer++;
        return true;
    }

    public int getDotPointer() {
        return dotPointer;
    }

    public String[] getRightSide() {
        return rightSide;
    }

    public HashSet<String> getLookahead() {
        return lookahead;
    }

    public String getLeftSide() {
        return leftSide;
    }

    public void setLookahead(HashSet<String> lookahead) {
        this.lookahead = lookahead;
    }

    public void setRightSide(String[] rightSide) {
        this.rightSide = rightSide;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LR1Item lr1Item = (LR1Item) o;
        return dotPointer == lr1Item.dotPointer &&
                Objects.equals(lookahead, lr1Item.lookahead) &&
                Objects.equals(leftSide, lr1Item.leftSide) &&
                Arrays.equals(rightSide, lr1Item.rightSide);
    }

    public  boolean equalLR0(LR1Item item){
        return leftSide.equals(item.getLeftSide()) && Arrays.equals(rightSide,item.getRightSide()) && dotPointer == item.getDotPointer();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + this.dotPointer;
        hash = 31 * hash + Objects.hashCode(this.leftSide);
        hash = 31 * hash + Arrays.deepHashCode(this.rightSide);
        hash = 31 * hash + Objects.hashCode(this.lookahead);
        return hash;
    }

    @Override
    public String toString() {
        String str = leftSide + " -> ";
        for (int i = 0; i < rightSide.length; i++) {
            if (i == dotPointer) {
                str += ".";
            }
            str += rightSide[i];
            if(i != rightSide.length - 1){
                str+= " ";
            }
        }
        if (rightSide.length == dotPointer) {
            str += ".";
        }
        str += " , " + lookahead;
        return str;
    }
}
