/************************************************************************
 * Strathclyde Planning Group,
 * Department of Computer and Information Sciences,
 * University of Strathclyde, Glasgow, UK
 * http://planning.cis.strath.ac.uk/
 * 
 * Copyright 2007, Keith Halsey
 * Copyright 2008, Andrew Coles and Amanda Smith
 *
 * (Questions/bug reports now to be sent to Andrew Coles)
 *
 * This file is part of JavaFF.
 * 
 * JavaFF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * JavaFF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JavaFF.  If not, see <http://www.gnu.org/licenses/>.
 * 
 ************************************************************************/

package javaff.data.metric;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javaff.data.GroundCondition;
import javaff.data.PDDLPrinter;
import javaff.data.UngroundCondition;
import javaff.data.UngroundEffect;
import javaff.planning.MetricState;
import javaff.planning.State;

public class BinaryComparator implements javaff.data.GroundCondition, javaff.data.UngroundCondition {
	public Function first;
	public Function second;
	public int type;

	public BinaryComparator(String s, Function f1, Function f2) {
		type = MetricSymbolStore.getType(s);
		first = f1;
		second = f2;
	}

	public BinaryComparator(int t, Function f1, Function f2) {
		type = t;
		first = f1;
		second = f2;
	}

	public boolean isStatic() {
		return (first.isStatic() && second.isStatic());
	}

	public boolean effectedBy(ResourceOperator o) {
		return (first.effectedBy(o) || second.effectedBy(o));
	}

	public GroundCondition staticifyCondition(Map fValues) {
		first = first.staticify(fValues);
		second = second.staticify(fValues);
		return this;
	}

	public UngroundCondition minus(UngroundEffect e) {
		return e.effectsAdd(this);
	}

	public Set getStaticPredicates() {
		return new HashSet();
	}

	public GroundCondition groundCondition(Map varMap) {
		return new BinaryComparator(type, first.ground(varMap), second.ground(varMap));
	}

	public boolean stateIsTrue(State s) {
		MetricState ms = (MetricState) s;
		BigDecimal df = first.getValue(ms);
		BigDecimal ds = second.getValue(ms);
		boolean result = false;

		if (type == MetricSymbolStore.greaterThan)
			result = (df.compareTo(ds) > 0);
		else if (type == MetricSymbolStore.greaterThanEqual)
			result = (df.compareTo(ds) >= 0);
		else if (type == MetricSymbolStore.lessThan)
			result = (df.compareTo(ds) < 0);
		else if (type == MetricSymbolStore.lessThanEqual)
			result = (df.compareTo(ds) <= 0);
		else if (type == MetricSymbolStore.equal)
			result = (df.compareTo(ds) == 0);

		return result;
	}

	public void pddlPrint(PrintStream s, int i) {
		PDDLPrinter.printToString(this, s, false, false, i);
	}

	public Set getConditionalPropositions() {
		return new HashSet();
	}

	public Set getComparators() {
		Set s = new HashSet();
		s.add(this);
		return s;
	}

	public String toString() {
		return MetricSymbolStore.getSymbol(type) + " " + first.toString() + " " + second.toString();
	}

	public String toStringTyped() {
		return MetricSymbolStore.getSymbol(type) + " " + first.toStringTyped() + " " + second.toStringTyped();
	}

	public boolean equals(Object o) {
		if (o instanceof BinaryComparator) {
			BinaryComparator bc = (BinaryComparator) o;
			if (bc.type == this.type && first.equals(bc.first) && second.equals(bc.second))
				return true;
			else if (((bc.type == MetricSymbolStore.lessThan && this.type == MetricSymbolStore.greaterThan)
					|| (this.type == MetricSymbolStore.lessThan && bc.type == MetricSymbolStore.greaterThan)
					|| (this.type == MetricSymbolStore.lessThanEqual && bc.type == MetricSymbolStore.greaterThanEqual)

			) && (first.equals(bc.second) && second.equals(bc.first)))
				return true;
			else
				return false;
		} else
			return false;
	}

}
