package lr1;

import static java.util.stream.Collectors.toSet;
import static util.Grammar.EndToken;
import static util.Utility.set;

import lr0.LR0Item;
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
			var itemsiLR0 = itemsi.stream().map(LR0Item::new).collect(toSet());
			for (int j=statesList.size()-1; j>i; j-=1) {
				var itemsj = statesList.get(j).getItems();
				var itemsjLR0 = itemsj.stream().map(LR0Item::new).collect(toSet());
				if (!itemsiLR0.equals(itemsjLR0)) continue;
				itemsi.forEach(iti-> iti.lookahead.addAll(itemsj.stream().filter(itj-> itj.equalsLR0(iti)).findFirst().get().lookahead));
				statesList.forEach(st1->{ var ts = st1.getTransitions(); ts.forEach((s,st2)->{ if (st2.getItems().equals(itemsj)) ts.put(s,statei); }); });
				statesList.remove(j);
			}
			newStateList.add(statei);
		}
		statesList = newStateList;
	}
}
