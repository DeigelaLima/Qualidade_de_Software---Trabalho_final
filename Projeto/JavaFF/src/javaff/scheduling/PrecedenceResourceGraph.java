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

package javaff.scheduling;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javaff.data.Action;
import javaff.data.metric.BinaryComparator;
import javaff.data.metric.MetricSymbolStore;
import javaff.data.metric.ResourceOperator;
import javaff.data.strips.InstantAction;
import javaff.data.temporal.DurationFunction;
import javaff.data.temporal.DurativeAction;

//OK for new precedence relations (i.e. meetCosntraints) should move consumers to AFTER the >= etc..) (actually maybe no)
// AND for the new bounds should do incremental sweeps as in precedence relations

public class PrecedenceResourceGraph {
	public Map operators = new Hashtable();
	public Map conditions = new Hashtable();
	public Map states = new Hashtable(); // Maps (Operators || Conditions => States)

	public MatrixSTN stn;

	public PrecedenceResourceGraph(MatrixSTN s) {
		stn = s;
	}

	public void addCondition(BinaryComparator c, Action a) {
		conditions.put(c, a);
	}

	public void addOperator(ResourceOperator o, Action a) {
		operators.put(o, a);
	}

	public boolean meetConditions() {
		boolean changed = false;
		for (Iterator bcit = conditions.keySet().iterator(); bcit.hasNext();) {
			BinaryComparator bc = (BinaryComparator) bcit.next();
			BigDecimal comp = bc.second.getValue(null);
			Action a = (Action) conditions.get(bc);
			if (bc.type == MetricSymbolStore.lessThan || bc.type == MetricSymbolStore.lessThanEqual) {
				BigDecimal value = findBeforeMin(a);
				if (value.compareTo(comp) >= 0) {
					Set u = getUnorderedConsumers(a);
					Action a2 = stn.getEarliest(u);
					stn.addConstraint(TemporalConstraint.getConstraint((InstantAction) a2, (InstantAction) a));
					changed = true;
				}
			} else if (bc.type == MetricSymbolStore.greaterThan || bc.type == MetricSymbolStore.greaterThanEqual) {
				BigDecimal value = findBeforeMax(a);
				if (value.compareTo(comp) <= 0) {
					Set u = getUnorderedProducers(a);
					Action a2 = stn.getEarliest(u);
					stn.addConstraint(TemporalConstraint.getConstraint((InstantAction) a2, (InstantAction) a));
					changed = true;
				}
			}
		}
		return changed;
	}

	private BigDecimal findBeforeMax(Action a) {
		BigDecimal value = new BigDecimal(0);
		for (Iterator opit = operators.keySet().iterator(); opit.hasNext();) {
			ResourceOperator ro = (ResourceOperator) opit.next();
			Action a2 = (Action) operators.get(ro);
			if (ro.type == MetricSymbolStore.increase || ro.type == MetricSymbolStore.scaleUP) {
				if (stn.isMinOrZero(a2, a) || stn.bS(a2, a))
					value = ro.applyMax(value, stn);
			} else if (ro.type == MetricSymbolStore.decrease || ro.type == MetricSymbolStore.scaleDown) {
				if (stn.isMinOrZero(a2, a))
					value = ro.applyMin(value, stn);
			}
		}
		return value;
	}

	private BigDecimal findBeforeMin(Action a) {
		BigDecimal value = new BigDecimal(0);
		for (Iterator opit = operators.keySet().iterator(); opit.hasNext();) {
			ResourceOperator ro = (ResourceOperator) opit.next();
			Action a2 = (Action) operators.get(ro);
			if (ro.type == MetricSymbolStore.increase || ro.type == MetricSymbolStore.scaleUP) {
				if (stn.isMinOrZero(a2, a))
					value = ro.applyMin(value, stn);
			} else if (ro.type == MetricSymbolStore.decrease || ro.type == MetricSymbolStore.scaleDown) {
				if (stn.isMinOrZero(a2, a) || stn.bS(a2, a))
					value = ro.applyMax(value, stn);
			}
		}
		return value;
	}

	private Set getUnorderedProducers(Action a) {
		Set rSet = new HashSet();
		for (Iterator opit = operators.keySet().iterator(); opit.hasNext();) {
			ResourceOperator ro = (ResourceOperator) opit.next();
			Action a2 = (Action) operators.get(ro);
			boolean condition = (ro.type == MetricSymbolStore.increase || ro.type == MetricSymbolStore.scaleUP)
					&& stn.U(a2, a);
			if (condition)
				rSet.add(a2);
		}
		return rSet;
	}

	private Set getUnorderedConsumers(Action a) {
		Set rSet = new HashSet();
		for (Iterator opit = operators.keySet().iterator(); opit.hasNext();) {
			ResourceOperator ro = (ResourceOperator) opit.next();
			Action a2 = (Action) operators.get(ro);
			boolean condition = (ro.type == MetricSymbolStore.decrease || ro.type == MetricSymbolStore.scaleDown)
					&& stn.U(a2, a);
			if (condition)
				rSet.add(a2);
		}
		return rSet;
	}

	private Set getBeforeOperators() {
		Set rSet = new HashSet();
		for (Iterator opit = operators.keySet().iterator(); opit.hasNext();) {
			ResourceOperator ro = (ResourceOperator) opit.next();
			Action a2 = (Action) operators.get(ro);
			rSet.add(ro);
		}
		return rSet;
	}

	public boolean limitBounds() {
		boolean change = false;
		for (Iterator bcit = conditions.keySet().iterator(); bcit.hasNext();) {
			BinaryComparator bc = (BinaryComparator) bcit.next();
			BigDecimal comp = bc.second.getValue(null);
			Action a = (Action) conditions.get(bc);
			if (bc.type == MetricSymbolStore.lessThan || bc.type == MetricSymbolStore.lessThanEqual) {
				BigDecimal value = findBeforeMax(a);
				if (value.compareTo(comp) > 0) {
					BigDecimal diff = value.subtract(comp);
					Set u = getBeforeOperators();
					Iterator uit = u.iterator();
					while (uit.hasNext()) {
						ResourceOperator ro = (ResourceOperator) uit.next();
						if (ro.change instanceof DurationFunction) {
							DurationFunction df = (DurationFunction) ro.change;
							DurativeAction da = df.durativeAction;
							if (ro.type == MetricSymbolStore.increase || ro.type == MetricSymbolStore.scaleUP)
								stn.decreaseMax(da, diff);
							else if (ro.type == MetricSymbolStore.decrease || ro.type == MetricSymbolStore.scaleDown)
								stn.increaseMin(da, diff);
							change = true;
							break;
						}
					}
				}
			} else if (bc.type == MetricSymbolStore.greaterThan || bc.type == MetricSymbolStore.greaterThanEqual) {
				BigDecimal value = findBeforeMin(a);
				if (value.compareTo(comp) < 0) {
					BigDecimal diff = comp.subtract(value);
					Set u = getBeforeOperators();
					Iterator uit = u.iterator();
					while (uit.hasNext()) {
						ResourceOperator ro = (ResourceOperator) uit.next();
						if (ro.change instanceof DurationFunction) {
							DurationFunction df = (DurationFunction) ro.change;
							DurativeAction da = df.durativeAction;
							if (ro.type == MetricSymbolStore.increase || ro.type == MetricSymbolStore.scaleUP)
								stn.increaseMin(da, diff);
							else if (ro.type == MetricSymbolStore.decrease || ro.type == MetricSymbolStore.scaleDown)
								stn.decreaseMax(da, diff);
							change = true;
							break;
						}
					}
				}
			}
		}
		return change;
	}

	public void minimize() {
		for (Iterator roit = operators.keySet().iterator(); roit.hasNext();) {
			ResourceOperator ro = (ResourceOperator) roit.next();
			if (ro.change instanceof DurationFunction) {
				DurativeAction da = ((DurationFunction) ro.change).durativeAction;
				if (ro.type == MetricSymbolStore.increase || ro.type == MetricSymbolStore.scaleUP) {
					stn.minimize(da);
				} else if (ro.type == MetricSymbolStore.decrease || ro.type == MetricSymbolStore.scaleDown) {
					stn.maximize(da);
				}
			}
		}
	}

	public void maximize() {
		for (Iterator roit = operators.keySet().iterator(); roit.hasNext();) {
			ResourceOperator ro = (ResourceOperator) roit.next();
			if (ro.change instanceof DurationFunction) {
				DurativeAction da = ((DurationFunction) ro.change).durativeAction;
				if (ro.type == MetricSymbolStore.increase || ro.type == MetricSymbolStore.scaleUP) {
					stn.maximize(da);
				} else if (ro.type == MetricSymbolStore.decrease || ro.type == MetricSymbolStore.scaleDown) {
					stn.minimize(da);
				}
			}
		}
	}

}
