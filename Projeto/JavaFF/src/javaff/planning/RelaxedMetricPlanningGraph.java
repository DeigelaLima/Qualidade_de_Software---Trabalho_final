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

package javaff.planning;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javaff.data.GroundCondition;
import javaff.data.GroundProblem;
import javaff.data.metric.BinaryComparator;
import javaff.data.metric.BinaryFunction;
import javaff.data.metric.Function;
import javaff.data.metric.MetricSymbolStore;
import javaff.data.metric.NamedFunction;
import javaff.data.metric.NumberFunction;
import javaff.data.metric.ResourceOperator;

public class RelaxedMetricPlanningGraph extends RelaxedPlanningGraph {
	protected Set metricGoal = new HashSet();

	protected List minResources; // List of Maps [(PGFunction => BigDecimal)]
	protected List maxResources;

	protected Map PGFuncMap = new Hashtable(); // (NamedFunction => PGNamedFunction)
	protected Map ActionComparators = new Hashtable(); // (PGAction => (PGBinaryComparator))
	protected Map ActionOperators = new Hashtable(); // (PGAction => (PGFunctionOperator))

	// Used during graph construction
	private Set resOps; // PGResourceOperators that are in the graph

	public RelaxedMetricPlanningGraph(GroundProblem gp) {
		super(gp);
		setupPGFuncMap(gp.functionValues.keySet());
		makeComparators(actions);
		makeOperators(actions);
		resOps = new HashSet();
	}

	// *************************************
	// Initial Setup
	// *************************************

	private void setupPGFuncMap(Set funcs) {
		for (Iterator fit = funcs.iterator(); fit.hasNext();) {
			NamedFunction nf = (NamedFunction) fit.next();
			PGFuncMap.put(nf, new PGNamedFunction(nf));
		}
	}

	private void makeComparators(Set actions) {
		for (Iterator ait = actions.iterator(); ait.hasNext();) {
			PGAction pga = (PGAction) ait.next();
			Set ss = new HashSet();
			ActionComparators.put(pga, ss);
			Set cs = pga.getComparators();
			Iterator cit = cs.iterator();
			while (cit.hasNext()) {
				BinaryComparator bc = (BinaryComparator) cit.next();
				ss.add(makeComparator(bc));
			}
		}
	}

	private PGBinaryComparator makeComparator(BinaryComparator c) {
		PGFunction f = makeFunction(c.first);
		PGFunction s = makeFunction(c.second);
		return new PGBinaryComparator(f, s, c.type);
	}

	private void makeOperators(Set actions) {
		for (Iterator ait = actions.iterator(); ait.hasNext();) {
			PGAction pga = (PGAction) ait.next();
			Set s = new HashSet();
			ActionOperators.put(pga, s);
			Set os = pga.getOperators();
			Iterator oit = os.iterator();
			while (oit.hasNext()) {
				ResourceOperator ro = (ResourceOperator) oit.next();
				s.add(makeOperator(ro));
			}
		}
	}

	private PGResourceOperator makeOperator(ResourceOperator o) {
		PGNamedFunction r = (PGNamedFunction) PGFuncMap.get(o.resource);
		PGFunction c = makeFunction(o.change);
		return new PGResourceOperator(r, c, o.type);
	}

	protected PGFunction makeFunction(Function f) {
		if (f instanceof NamedFunction)
			return (PGNamedFunction) PGFuncMap.get(f);
		if (f instanceof NumberFunction)
			return new PGNumberFunction(f.getValue(null));
		else if (f instanceof BinaryFunction) {
			BinaryFunction bf = (BinaryFunction) f;
			PGFunction f1 = makeFunction(bf.first);
			PGFunction s2 = makeFunction(bf.second);
			return new PGBinaryFunction(f1, s2, bf.type);
		} else
			return null;
	}

	protected void setGoal(GroundCondition g) {
		super.setGoal(g);
		for (Iterator cit = g.getComparators().iterator(); cit.hasNext();) {
			BinaryComparator c = (BinaryComparator) cit.next();
			metricGoal.add(makeComparator(c));
		}
	}

	// *************************************
	// Graph Setup
	// *************************************
	protected void resetAll(State s) {
		super.resetAll(s);
		maxResources = new ArrayList();
		minResources = new ArrayList();
		setInitialValues((MetricState) s);
	}

	protected void setInitialValues(MetricState s) {
		Iterator fit = s.funcValues.keySet().iterator();
		Map max = new Hashtable();
		Map min = new Hashtable();
		while (fit.hasNext()) {
			NamedFunction nf = (NamedFunction) fit.next();
			PGFunction pgf = (PGFunction) PGFuncMap.get(nf);
			BigDecimal bd = (BigDecimal) s.funcValues.get(nf);
			max.put(pgf, bd);
			min.put(pgf, bd);
		}
		maxResources.add(max);
		minResources.add(min);
	}

	// *************************************
	// Graph Construction Methods
	// *************************************

	protected ArrayList createFactLayer(Set scheduledFacts, int layer) {
		ArrayList rActionList = super.createFactLayer(scheduledFacts, layer);
		updateResourceValues(layer);
		return rActionList;
	}

	private void updateResourceValues(int layer) {
		if (layer == 0)
			return;
		Hashtable maxminus = (Hashtable) maxResources.get(layer - 1);
		Hashtable minminus = (Hashtable) minResources.get(layer - 1);
		Map newmax = (Hashtable) maxminus.clone();
		Map newmin = (Hashtable) minminus.clone();
		maxResources.add(layer, newmax);
		minResources.add(layer, newmin);

		for (Iterator roit = resOps.iterator(); roit.hasNext();) {
			PGResourceOperator ro = (PGResourceOperator) roit.next();
			BigDecimal max = ro.resource.getMaxValue(layer - 1, maxResources, minResources);
			BigDecimal min = ro.resource.getMinValue(layer - 1, maxResources, minResources);
			BigDecimal nmax = ro.maximise(layer - 1, maxResources, minResources);
			BigDecimal nmin = ro.minimise(layer - 1, maxResources, minResources);
			if (nmax.compareTo(max) > 0) {
				newmax.put(ro.resource, nmax);
				numeric_level_off++;
			}
			if (nmin.compareTo(min) < 0) {
				newmin.put(ro.resource, nmin);
				numeric_level_off++;
			}
		}
	}

	protected HashSet filterSet(Set pActions, int layer) {
		Set fActions = super.filterSet(pActions, layer);
		HashSet rSet = new HashSet();
		for (Iterator ait = fActions.iterator(); ait.hasNext();) {
			PGAction a = (PGAction) ait.next();
			if (actionReady(a, layer))
				rSet.add(a);
			else
				readyActions.add(a);
		}
		readyActions.removeAll(rSet);
		return rSet;
	}

	private boolean actionReady(PGAction a, int layer) {
		Iterator csit = ((HashSet) ActionComparators.get(a)).iterator();
		boolean allmet = true;
		while (csit.hasNext() && allmet) {
			allmet = ((PGBinaryComparator) csit.next()).met(layer, maxResources, minResources);
		}
		return allmet;
	}

	protected HashSet calculateActionMutexesAndProps(Set filteredSet, int pLayer) {
		HashSet rSet = super.calculateActionMutexesAndProps(filteredSet, pLayer);
		for (Iterator ait = filteredSet.iterator(); ait.hasNext();) {
			PGAction a = (PGAction) ait.next();
			Set ops = (Set) ActionOperators.get(a);
			resOps.addAll(ops);
		}
		return rSet;
	}

	// *************************************
	// Graph Extraction
	// *************************************

	protected boolean goalMet() {
		boolean met = super.goalMet();
		for (Iterator mgit = metricGoal.iterator(); met && mgit.hasNext();) {
			PGBinaryComparator c = (PGBinaryComparator) mgit.next();
			met = c.met(num_layers, maxResources, minResources);
		}
		return met;
	}

	public List extractPlan() // Should be done better buts its not
	{
		return searchRelaxedPlan(goal, metricGoal, num_layers);
	}

	public List searchRelaxedPlan(Set goalSet, Set mgoalSet, int l) {
		if (l == 0)
			return new ArrayList();
		Set chosenActions = new HashSet();
		for (Iterator git = goalSet.iterator(); git.hasNext();) {
			PGProposition g = (PGProposition) git.next();
			PGAction a = null;
			Iterator ait = g.achievedBy.iterator();
			while (ait.hasNext()) {
				PGAction na = (PGAction) ait.next();
				if (na.layer < l && na.layer >= 0) {
					if (na instanceof PGNoOp) {
						a = na;
						break;
					} else if (chosenActions.contains(na)) {
						a = na;
						break;
					} else {
						if (a == null)
							a = na;
						else if (a.difficulty > na.difficulty)
							a = na;
					}
				}
			}
			if (a != null)
				chosenActions.add(a);
		}

		Set newMGoalSet = new HashSet();
		for (Iterator mit = mgoalSet.iterator(); mit.hasNext();) {
			PGBinaryComparator c = (PGBinaryComparator) mit.next();
			Iterator ait = actions.iterator();
			while (!c.met(l - 1, maxResources, minResources) && ait.hasNext()) {
				PGAction a = null;
				while (ait.hasNext()) {
					a = (PGAction) ait.next();
					if (a.layer < l && a.layer >= 0 && c.makeBetter(a))
						break;
				}
				Iterator roit = ((Set) ActionOperators.get(a)).iterator();
				while (roit.hasNext()) {
					PGResourceOperator o = (PGResourceOperator) roit.next();
					if (c.makeBetter(o)) {
						c = c.affect(o);
					}
				}
				chosenActions.add(a);
			}
			newMGoalSet.add(c);
		}

		Set newGoalSet = new HashSet();
		for (Iterator cait = chosenActions.iterator(); cait.hasNext();) {
			PGAction ca = (PGAction) cait.next();
			newGoalSet.addAll(ca.conditions);
			newMGoalSet.addAll((Set) ActionComparators.get(ca));
		}

		List rplan = searchRelaxedPlan(newGoalSet, newMGoalSet, l - 1);
		rplan.addAll(chosenActions);
		return rplan;
	}

	// *************************************
	// Internal Metric Classes
	// *************************************

	// Note these do not deal with max and mins where there a -ve changes with
	// multiplication and division. For example if x*y could be max if max*max or
	// min*min if both mins were negative

	protected interface PGFunction {
		public BigDecimal getMaxValue(int layer, List maxes, List mins);

		public BigDecimal getMinValue(int layer, List maxes, List mins);

		public boolean effectedBy(PGResourceOperator ro);

		public boolean increase(PGResourceOperator ro);

		public boolean decrease(PGResourceOperator ro);
	}

	protected class PGNamedFunction implements PGFunction {
		public NamedFunction namedFunction;

		protected PGNamedFunction() {

		}

		public PGNamedFunction(NamedFunction nf) {
			namedFunction = nf;
		}

		public BigDecimal getMaxValue(int layer, List maxes, List mins) {
			return (BigDecimal) ((Hashtable) maxes.get(layer)).get(this);
		}

		public BigDecimal getMinValue(int layer, List maxes, List mins) {
			return (BigDecimal) ((Hashtable) mins.get(layer)).get(this);
		}

		public int hashcode() {
			return namedFunction.hashCode();
		}

		public boolean effectedBy(PGResourceOperator ro) {
			return (ro.resource == this);
		}

		public boolean increase(PGResourceOperator ro) {
			return ro.increase(this);
		}

		public boolean decrease(PGResourceOperator ro) {
			return ro.decrease(this);
		}
	}

	protected class PGNumberFunction implements PGFunction {
		public BigDecimal value;

		public PGNumberFunction(BigDecimal v) {
			value = v;
		}

		public BigDecimal getMaxValue(int layer, List maxes, List mins) {
			return value;
		}

		public BigDecimal getMinValue(int layer, List maxes, List mins) {
			return value;
		}

		public boolean effectedBy(PGResourceOperator ro) {
			return false;
		}

		public boolean increase(PGResourceOperator ro) {
			return false;
		}

		public boolean decrease(PGResourceOperator ro) {
			return false;
		}
	}

	protected class PGBinaryFunction implements PGFunction {
		public PGFunction first;
		public PGFunction second;
		public int type;

		public PGBinaryFunction(PGFunction f, PGFunction s, int t) {
			first = f;
			second = s;
			type = t;
		}

		public BigDecimal getMaxValue(int layer, List maxes, List mins) {
			if (type == MetricSymbolStore.plus)
				return first.getMaxValue(layer, maxes, mins).add(second.getMaxValue(layer, maxes, mins));
			if (type == MetricSymbolStore.minus)
				return first.getMaxValue(layer, maxes, mins).subtract(second.getMinValue(layer, maxes, mins));
			else if (type == MetricSymbolStore.multiply)
				return first.getMaxValue(layer, maxes, mins).multiply(second.getMaxValue(layer, maxes, mins));
			else if (type == MetricSymbolStore.divide)
				return first.getMaxValue(layer, maxes, mins).divide(second.getMinValue(layer, maxes, mins),
						MetricSymbolStore.scale, MetricSymbolStore.round);
			else
				return null;
		}

		public BigDecimal getMinValue(int layer, List maxes, List mins) {
			return getMaxValue(layer, mins, maxes);
		}

		public boolean effectedBy(PGResourceOperator ro) {
			return (first.effectedBy(ro) || second.effectedBy(ro));
		}

		public boolean increase(PGResourceOperator ro) {
			if (type == MetricSymbolStore.plus)
				return (first.increase(ro) || second.increase(ro));
			if (type == MetricSymbolStore.minus)
				return (first.increase(ro) || second.decrease(ro));
			else if (type == MetricSymbolStore.multiply)
				return (first.increase(ro) || second.increase(ro));
			else if (type == MetricSymbolStore.divide)
				return (first.increase(ro) || second.decrease(ro));
			else
				return false;
		}

		public boolean decrease(PGResourceOperator ro) {
			if (type == MetricSymbolStore.plus)
				return (first.decrease(ro) || second.decrease(ro));
			if (type == MetricSymbolStore.minus)
				return (first.decrease(ro) || second.increase(ro));
			else if (type == MetricSymbolStore.multiply)
				return (first.decrease(ro) || second.decrease(ro));
			else if (type == MetricSymbolStore.divide)
				return (first.decrease(ro) || second.increase(ro));
			else
				return false;
		}
	}

	protected class PGResourceOperator {
		public PGNamedFunction resource;
		public PGFunction change;
		public int type;

		public PGResourceOperator(PGNamedFunction r, PGFunction c, int t) {
			resource = r;
			change = c;
			type = t;
		}

		public BigDecimal maximise(int layer, List maxes, List mins) {
			if (type == MetricSymbolStore.assign)
				return change.getMaxValue(layer, maxes, mins);
			if (type == MetricSymbolStore.increase)
				return resource.getMaxValue(layer, maxes, mins).add(change.getMaxValue(layer, maxes, mins));
			else if (type == MetricSymbolStore.decrease)
				return resource.getMaxValue(layer, maxes, mins).subtract(change.getMinValue(layer, maxes, mins));
			else if (type == MetricSymbolStore.scaleUP)
				return resource.getMaxValue(layer, maxes, mins).multiply(change.getMaxValue(layer, maxes, mins));
			else if (type == MetricSymbolStore.scaleDown)
				return resource.getMaxValue(layer, maxes, mins).divide(change.getMinValue(layer, maxes, mins),
						MetricSymbolStore.scale, MetricSymbolStore.round);
			else
				return null;
		}

		public BigDecimal minimise(int layer, List maxes, List mins) {
			return maximise(layer, mins, maxes);
		}

		public PGFunction invertFunction(PGFunction f) {
			if (type == MetricSymbolStore.assign)
				return change;
			if (type == MetricSymbolStore.increase)
				return new PGBinaryFunction(f, change, MetricSymbolStore.minus);
			else if (type == MetricSymbolStore.decrease)
				return new PGBinaryFunction(f, change, MetricSymbolStore.plus);
			else if (type == MetricSymbolStore.scaleUP)
				return new PGBinaryFunction(f, change, MetricSymbolStore.divide);
			else if (type == MetricSymbolStore.scaleDown)
				return new PGBinaryFunction(f, change, MetricSymbolStore.multiply);
			else
				return null;
		}

		public boolean increase(PGFunction f) {
			if (resource != f)
				return false;
			if (type == MetricSymbolStore.assign)
				return true;
			else if (type == MetricSymbolStore.increase)
				return true;
			else if (type == MetricSymbolStore.decrease)
				return false;
			else if (type == MetricSymbolStore.scaleUP)
				return true;
			else if (type == MetricSymbolStore.scaleDown)
				return false;
			else
				return false;
		}

		public boolean decrease(PGFunction f) {
			if (resource != f)
				return false;
			if (type == MetricSymbolStore.assign)
				return true;
			else if (type == MetricSymbolStore.increase)
				return false;
			else if (type == MetricSymbolStore.decrease)
				return true;
			else if (type == MetricSymbolStore.scaleUP)
				return false;
			else if (type == MetricSymbolStore.scaleDown)
				return true;
			else
				return false;
		}
	}

	protected class PGBinaryComparator {
		public PGFunction left;
		public PGFunction right;
		public int type;

		public PGBinaryComparator(PGFunction l, PGFunction r, int t) {
			left = l;
			right = r;
			type = t;
		}

		public boolean met(int layer, List maxes, List mins) {
			if (type == MetricSymbolStore.greaterThan)
				return left.getMaxValue(layer, maxes, mins).compareTo(right.getMinValue(layer, maxes, mins)) > 0;
			if (type == MetricSymbolStore.greaterThanEqual)
				return left.getMaxValue(layer, maxes, mins).compareTo(right.getMinValue(layer, maxes, mins)) >= 0;
			else if (type == MetricSymbolStore.lessThan)
				return left.getMinValue(layer, maxes, mins).compareTo(right.getMaxValue(layer, maxes, mins)) < 0;
			else if (type == MetricSymbolStore.lessThanEqual)
				return left.getMinValue(layer, maxes, mins).compareTo(right.getMaxValue(layer, maxes, mins)) <= 0;
			else if (type == MetricSymbolStore.equal)
				return (left.getMaxValue(layer, maxes, mins).compareTo(right.getMinValue(layer, maxes, mins)) >= 0
						&& left.getMinValue(layer, maxes, mins).compareTo(right.getMaxValue(layer, maxes, mins)) <= 0);
			else
				return true;
		}

		public boolean effectedBy(PGResourceOperator ro) {
			return (left.effectedBy(ro) || right.effectedBy(ro));
		}

		public PGBinaryComparator affect(PGResourceOperator ro) {
			if (!effectedBy(ro))
				return this;
			PGFunction newLeft = left;
			PGFunction newRight = right;
			if (left.effectedBy(ro)) {
				newLeft = ro.invertFunction(left);
			}
			if (right.effectedBy(ro)) {
				newRight = ro.invertFunction(right);
			}
			PGBinaryComparator nbc = new PGBinaryComparator(newLeft, newRight, type);
			return nbc;
		}

		public boolean makeBetter(PGResourceOperator ro) {
			if (type == MetricSymbolStore.greaterThan || type == MetricSymbolStore.greaterThanEqual)
				return left.increase(ro) || right.decrease(ro);
			if (type == MetricSymbolStore.lessThan || type == MetricSymbolStore.lessThanEqual)
				return left.decrease(ro) || right.increase(ro);
			else if (type == MetricSymbolStore.equal)
				return true;
			else
				return true;
		}

		public boolean makeBetter(PGAction a) {
			for (Iterator oit = ((Set) ActionOperators.get(a)).iterator(); oit.hasNext();) {
				PGResourceOperator o = (PGResourceOperator) oit.next();
				if (makeBetter(o))
					return true;
			}
			return false;
		}
	}

	// *************************************
	// Debugging Classes
	// *************************************

	public void printLayer(int i) {
		super.printLayer(i);
		Map max = (Hashtable) maxResources.get(i);
		Map min = (Hashtable) minResources.get(i);
		System.out.println("Resources:");
		for (Iterator rit = max.keySet().iterator(); rit.hasNext();) {
			PGNamedFunction r = (PGNamedFunction) rit.next();
			System.out.print("\t" + r.namedFunction.toString());
			System.out.print(" max:" + max.get(r));
			System.out.println(" min:" + min.get(r));
		}
	}

}