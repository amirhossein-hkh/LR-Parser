package lr1;

import util.Grammar;
import util.Rule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;

public class LR1State {
    private LinkedHashSet<LR1Item> items;
    private HashMap<String,LR1State> transition;

    public LR1State(Grammar grammar,HashSet<LR1Item> coreItems){
        items = new LinkedHashSet<>(coreItems);
        transition = new HashMap<>();
        closure(grammar);
    }

    private void closure(Grammar grammar) {
        boolean changeFlag = false;
        do {
            changeFlag = false;
            HashSet<LR1Item> temp = new HashSet<>();
            for(LR1Item item : items){
                if(item.getDotPointer() != item.getRightSide().length && grammar.isVariable(item.getCurrent())){
                    HashSet<String> lookahead = new HashSet<>();
                    if(item.getDotPointer() == item.getRightSide().length - 1){
                        lookahead.addAll(item.getLookahead());
                    }else{
                        HashSet<String> firstSet = grammar.computeFirst(item.getRightSide(),item.getDotPointer()+1);
                        if(firstSet.contains("epsilon")){
                            firstSet.remove("epsilon");
                            firstSet.addAll(item.getLookahead());
                        }
                        lookahead.addAll(firstSet);
                    }
                    HashSet<Rule> rules = grammar.getRuledByLeftVariable(item.getCurrent());
                    for(Rule rule : rules){
                        temp.add(new LR1Item(rule.getLeftSide(),rule.getRightSide(),0,lookahead));
                    }
                }
            }
            if(!items.containsAll(temp)){
                items.addAll(temp);
                changeFlag = true;
            }
        } while (changeFlag);

    }

    public HashMap<String, LR1State> getTransition() {
        return transition;
    }

    public LinkedHashSet<LR1Item> getItems() {
        return items;
    }

    @Override
    public String toString() {
        String s = "";
        for(LR1Item item:items){
            s += item + "\n";
        }
        return s;
    }

}
