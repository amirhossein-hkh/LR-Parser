package lr1;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

import lr0.LR0Item;
import util.Rule;

public class LR1Item extends LR0Item {

	protected Set<String> lookahead;

	public LR1Item(Rule rule, Set<String> lookahead){
		super(rule);
		this.lookahead = lookahead;
	}

	public LR1Item(LR1Item item){
		super(item);
		this.lookahead = item.lookahead;
	}

	@Override
	public <T extends LR0Item> T nextSymbol() {
		return new LR1Item(this).advanceDot();
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + dot;
		hash = 31 * hash + Objects.hashCode(lhs);
		hash = 31 * hash + Arrays.deepHashCode(rhs);
		hash = 31 * hash + Objects.hashCode(lookahead);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj)
			&& lookahead.equals(((LR1Item) obj).lookahead)
		;
	}

	public  boolean equalLR0(LR1Item item){
		return super.equals(item);
	}

	@Override
	public String toString() {
		return super.toString() + " , " + lookahead;
	}
}
