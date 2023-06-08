package lr0;

import util.Grammar;
import util.LRParser;

public class LR0Parser extends LRParser<LR0State, LR0Item> {

	public LR0Parser(Grammar grammar) {
		super(grammar);
	}

	public boolean parserLR0() {
		createStates(LR0State::new, new LR0State(grammar, set(new LR0Item(grammar.get(0)))));
		return createActionGoToTable(i-> grammar.getTerminals());
	}
	
	public boolean parserSLR1() {
		createStates(LR0State::new, new LR0State(grammar, set(new LR0Item(grammar.get(0)))));
		return createActionGoToTable(i-> grammar.getFollowSets().get(i.getLhs()));
	}
}
