package lr1;

import static util.Grammar.EndToken;

import java.util.LinkedHashSet;
import java.util.Set;

import util.Grammar;
import util.LRParser;

public class LR1Parser extends LRParser<LR1State, LR1Item> {

	public LR1Parser(Grammar grammar){
		super(grammar);
	}

	public boolean parseCLR1() {
		createStates(LR1State::new, new LR1State(grammar, set(new LR1Item(grammar.get(0), set(EndToken)))));
		return createActionGoToTable(i-> ((LR1Item) i).lookahead);
	}

	public boolean parseLALR1() {
		createStates(LR1State::new, new LR1State(grammar, set(new LR1Item(grammar.get(0), set(EndToken)))));
		createStatesForLALR1();
		return createActionGoToTable(i-> ((LR1Item) i).lookahead);
	}

	private void createStatesForLALR1(){
		StateList newStateList = new StateList();
		for (int i=0; i<statesList.size(); i+=1) {
			var statei = statesList.get(i);
			var itemsi = statei.getItems();
			//var itemsiLR0 = itemsi.stream().map(LR0Item::new).collect(toSet());
			for (int j=statesList.size()-1; j>i; j-=1) {
				var itemsj = statesList.get(j).getItems();
				//var itemsjLR0 = itemsj.stream().map(LR0Item::new).collect(toSet());
				//if (!itemsiLR0.equals(itemsjLR0)) continue;
				if (!equalsLR0(itemsi, itemsj)) continue;
				//if (!((SetLR1Item) itemsi).equalsLR0(itemsj)) continue;
				itemsi.forEach(iti-> iti.lookahead.addAll(itemsj.stream().filter(itj-> itj.equalsLR0(iti)).findFirst().get().lookahead));
				statesList.stream().map(LR1State::getTransitions).forEach(ts-> ts.forEach((s,st)->{ if (st.getItems().equals(itemsj)) ts.put(s,statei); }));
				//statesList.stream().map(LR1State::getTransitions).forEach(ts-> ts.entrySet().stream().filter(e->e.getValue().getItems().equals(itemsj)).map(e->ts.put(e.getKey(),statei)));
				statesList.remove(j);
			}
			newStateList.add(statei);
		}
		statesList = newStateList;
	}
	
	//*
	private boolean equalsLR0(Set<LR1Item> itemsi, Set<LR1Item> itemsj) {
        if (itemsi == itemsj) return true;
        if (itemsi.size() != itemsj.size()) return false;
        itemsj = new LinkedHashSet(itemsj);
        loop: for (LR1Item itemi: itemsi) {
        	for (LR1Item item2: itemsj) if (itemi.equalsLR0(item2)) {
        		itemsj.remove(item2);
        		continue loop;
        	}
         	return false;
    	}
        return true;
	}
	//*/
}
