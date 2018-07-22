package lr0;

import util.Grammar;
import util.Rule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;

public class LR0State {
    
    LinkedHashSet<LR0Item> items;
    HashMap<String, LR0State> transition;
    
    public LR0State(Grammar grammar, HashSet<LR0Item> coreItems) {
        items = new LinkedHashSet<>(coreItems);
        transition = new HashMap<>();
        closure(grammar);
    }
    
    private void closure(Grammar grammar) {
        boolean changeFlag = false;
        do {
            changeFlag = false;
            HashSet<LR0Item> temp = new HashSet<>();
            for (LR0Item item : items) {
                
                if (item.getCurrentTerminal()!=null && grammar.isVariable(item.getCurrentTerminal())) {
                    HashSet<Rule> rules = grammar.getRuledByLeftVariable(item.getCurrentTerminal());
                    temp.addAll(createLR0Item(rules));
                }
            }
            if(!items.containsAll(temp)){
                items.addAll(temp);
                changeFlag = true;
            }
        } while (changeFlag);
    }
    
    private HashSet<LR0Item> createLR0Item(HashSet<Rule> rules) {
        HashSet<LR0Item> results = new HashSet<>();
        for (Rule rule : rules) {
            results.add(new LR0Item(rule));
        }
        return results;
    }
    
    public void addTransition(String s, LR0State state){
        transition.put(s, state);
    }

    public HashSet<LR0Item> getItems() {
        return items;
    }

    public HashMap<String, LR0State> getTransition() {
        return transition;
    }
   
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.items);
        hash = 83 * hash + Objects.hashCode(this.transition);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LR0State other = (LR0State) obj;
        if (!(this.items.containsAll(other.items) && other.items.containsAll(this.items))) {
            return false;
        }
        if (!Objects.equals(this.transition, other.transition)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String s = "";
        for(LR0Item item:items){
            s += item + "\n";
        }
        return s;
    }
    
    
    
}
