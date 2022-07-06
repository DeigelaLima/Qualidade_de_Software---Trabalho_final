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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javaff.data.Action;
import javaff.data.GroundCondition;
import javaff.data.GroundProblem;
import javaff.data.Plan;
import javaff.data.TotalOrderPlan;
import javaff.data.strips.Proposition;

public class PlanningGraph {
	private PlanningGraphProduct planningGraphProduct = new PlanningGraphProduct();
	Map actionMap = new Hashtable(); // (Action => PGAction)

	Set actions = new HashSet();

	Set initial;
	Set goal;
	Set propMutexes;
	Set actionMutexes;
	List memorised;

	protected Set readyActions; // PGActions that have all their propositions met, but not their
								// PGBinaryComparators or preconditions are mutex

	boolean level_off;
	static int NUMERIC_LIMIT = 4;
	int numeric_level_off;
	int num_layers;

	// ******************************************************
	// Main methods
	// ******************************************************
	protected PlanningGraph() {

	}

	public PlanningGraph(GroundProblem gp) {
		setActionMap(gp.actions);
		setLinks();
		createNoOps();
		setGoal(gp.goal);
	}

	public Plan getPlan(State s) {
		setInitial(s);
		resetAll(s);
		Set scheduledFacts = new HashSet(initial);
		List scheduledActs = null;
		scheduledActs = createFactLayer(scheduledFacts, 0);
		List plan = null;
		while (true) {
			scheduledFacts = createActionLayer(scheduledActs, num_layers);
			++num_layers;
			scheduledActs = createFactLayer(scheduledFacts, num_layers);
			if (goalMet() && !goalMutex()) {
				plan = extractPlan();
			}
			if (plan != null)
				break;
			if (!level_off)
				numeric_level_off = 0;
			if (level_off || numeric_level_off >= NUMERIC_LIMIT) {
				break;
			}
		}
		if (plan == null)
			return null;
		Iterator pit = plan.iterator();
		TotalOrderPlan p = new TotalOrderPlan();
		while (pit.hasNext()) {
			PGAction a = (PGAction) pit.next();
			if (!(a instanceof PGNoOp))
				p.addAction(a.action);
		}
		return p;
	}

	// ******************************************************
	// Setting it all up
	// ******************************************************
	protected void setActionMap(Set gactions) {
		for (Iterator ait = gactions.iterator(); ait.hasNext();) {
			Action a = (Action) ait.next();
			PGAction pga = new PGAction(a);
			actionMap.put(a, pga);
			actions.add(pga);
		}
	}

	protected PGProposition getProposition(Proposition p) {
		return planningGraphProduct.getProposition(p, this);
	}

	protected void setLinks() {
		for (Iterator ait = actions.iterator(); ait.hasNext();) {
			PGAction pga = (PGAction) ait.next();
			Iterator csit = pga.action.getConditionalPropositions().iterator();
			while (csit.hasNext()) {
				Proposition p = (Proposition) csit.next();
				PGProposition pgp = planningGraphProduct.getProposition(p, this);
				pga.conditions.add(pgp);
				pgp.achieves.add(pga);
			}
			Iterator alit = pga.action.getAddPropositions().iterator();
			while (alit.hasNext()) {
				Proposition p = (Proposition) alit.next();
				PGProposition pgp = planningGraphProduct.getProposition(p, this);
				pga.achieves.add(pgp);
				pgp.achievedBy.add(pga);
			}
			Iterator dlit = pga.action.getDeletePropositions().iterator();
			while (dlit.hasNext()) {
				Proposition p = (Proposition) dlit.next();
				PGProposition pgp = planningGraphProduct.getProposition(p, this);
				pga.deletes.add(pgp);
				pgp.deletedBy.add(pga);
			}
		}
	}

	protected void resetAll(State s) {
		propMutexes = new HashSet();
		actionMutexes = new HashSet();

		memorised = new ArrayList();

		readyActions = new HashSet();

		num_layers = 0;

		for (Iterator ait = actions.iterator(); ait.hasNext();) {
			PGAction a = (PGAction) ait.next();
			a.reset();
		}

		for (Iterator pit = planningGraphProduct.getPropositions().iterator(); pit.hasNext();) {
			PGProposition p = (PGProposition) pit.next();
			p.reset();
		}
	}

	protected void setGoal(GroundCondition c) {
		goal = new HashSet();
		Iterator csit = c.getConditionalPropositions().iterator();
		while (csit.hasNext()) {
			Proposition p = (Proposition) csit.next();
			PGProposition pgp = planningGraphProduct.getProposition(p, this);
			goal.add(pgp);
		}
	}

	protected void setInitial(State s) {
		Set i = ((STRIPSState) s).facts;
		initial = new HashSet();
		Iterator csit = i.iterator();
		while (csit.hasNext()) {
			Proposition p = (Proposition) csit.next();
			PGProposition pgp = planningGraphProduct.getProposition(p, this);
			initial.add(pgp);
		}
	}

	protected void createNoOps() {
		for (Iterator pit = planningGraphProduct.getPropositions().iterator(); pit.hasNext();) {
			PGProposition p = (PGProposition) pit.next();
			PGNoOp n = new PGNoOp(p);
			n.conditions.add(p);
			n.achieves.add(p);
			p.achieves.add(n);
			p.achievedBy.add(n);
			actions.add(n);
		}
	}

	// ******************************************************
	// Graph Construction
	// ******************************************************

	protected ArrayList createFactLayer(Set pFacts, int pLayer) {
		memorised.add(new HashSet());
		ArrayList scheduledActs = new ArrayList();
		HashSet newMutexes = new HashSet();
		for (Iterator fit = pFacts.iterator(); fit.hasNext();) {
			PGProposition f = (PGProposition) fit.next();
			if (f.layer < 0) {
				f.layer = pLayer;
				scheduledActs.addAll(f.achieves);
				level_off = false;
				if (pLayer != 0) {
					Iterator pit = planningGraphProduct.getPropositions().iterator();
					while (pit.hasNext()) {
						PGProposition p = (PGProposition) pit.next();
						if (p.layer >= 0 && f.checkPropMutex(p, pLayer)) {
							f.makeMutex(p, pLayer, newMutexes);
						}
					}
				}
			}
		}

		for (Iterator pmit = propMutexes.iterator(); pmit.hasNext();) {
			MutexPair m = (MutexPair) pmit.next();
			if (checkPropMutex(m, pLayer)) {
				m.node1.makeMutex(m.node2, pLayer, newMutexes);
			} else {
				level_off = false;
			}
		}

		// add new mutexes to old mutexes and remove those which have disappeared
		propMutexes = newMutexes;

		return scheduledActs;
	}

	protected boolean checkPropMutex(MutexPair p, int l) {
		return ((PGProposition) p.node1).checkPropMutex((PGProposition) p.node2, l);
	}

	protected HashSet createActionLayer(List pActions, int pLayer) {
		level_off = true;
		HashSet actionSet = getAvailableActions(pActions, pLayer);
		actionSet.addAll(readyActions);
		readyActions = new HashSet();
		HashSet filteredSet = filterSet(actionSet, pLayer);
		HashSet scheduledFacts = calculateActionMutexesAndProps(filteredSet, pLayer);
		return scheduledFacts;
	}

	protected HashSet getAvailableActions(List pActions, int pLayer) {
		HashSet actionSet = new HashSet();
		for (Iterator ait = pActions.iterator(); ait.hasNext();) {
			PGAction a = (PGAction) ait.next();
			if (a.layer < 0) {
				a.counter++;
				a.difficulty += pLayer;
				if (a.counter >= a.conditions.size()) {
					actionSet.add(a);
					level_off = false;
				}
			}
		}
		return actionSet;
	}

	protected HashSet filterSet(Set pActions, int pLayer) {
		HashSet filteredSet = new HashSet();
		for (Iterator ait = pActions.iterator(); ait.hasNext();) {
			PGAction a = (PGAction) ait.next();
			if (noMutexes(a.conditions, pLayer))
				filteredSet.add(a);
			else
				readyActions.add(a);
		}
		return filteredSet;
	}

	protected HashSet calculateActionMutexesAndProps(Set filteredSet, int pLayer) {
		HashSet newMutexes = new HashSet();

		new HashSet();
		HashSet scheduledFacts = new HashSet();

		for (Iterator ait = filteredSet.iterator(); ait.hasNext();) {
			PGAction a = (PGAction) ait.next();
			scheduledFacts.addAll(a.achieves);
			a.layer = pLayer;
			level_off = false;
			Iterator a2it = actions.iterator();
			while (a2it.hasNext()) {
				PGAction a2 = (PGAction) a2it.next();
				if (a2.layer >= 0 && a.checkActionMutex(a2, pLayer)) {
					a.makeMutex(a2, pLayer, newMutexes);
				}
			}
		}

		for (Iterator amit = actionMutexes.iterator(); amit.hasNext();) {
			MutexPair m = (MutexPair) amit.next();
			if (checkActionMutex(m, pLayer)) {
				m.node1.makeMutex(m.node2, pLayer, newMutexes);
			} else {
				level_off = false;
			}
		}

		// add new mutexes to old mutexes and remove those which have disappeared
		actionMutexes = newMutexes;
		return scheduledFacts;
	}

	protected boolean checkActionMutex(MutexPair p, int l) {
		return ((PGAction) p.node1).checkActionMutex((PGAction) p.node2, l);
	}

	protected boolean goalMet() {
		for (Iterator git = goal.iterator(); git.hasNext();) {
			PGProposition p = (PGProposition) git.next();
			if (p.layer < 0)
				return false;
		}
		return true;
	}

	protected boolean goalMutex() {
		return !noMutexes(goal, num_layers);
	}

	protected boolean noMutexes(Set s, int l) {
		Iterator sit = s.iterator();
		if (!sit.hasNext())
			return true;
		Node n = (Node) sit.next();
		HashSet s2 = new HashSet(s);
		s2.remove(n);
		Iterator s2it = s2.iterator();
		while (s2it.hasNext()) {
			Node n2 = (Node) s2it.next();
			if (n.mutexWith(n2, l))
				return false;
		}
		return noMutexes(s2, l);
	}

	protected boolean noMutexesTest(Node n, Set s, int l) // Tests to see if there is a mutex between n and all nodes in
															// s
	{
		for (Iterator sit = s.iterator(); sit.hasNext();) {
			Node n2 = (Node) sit.next();
			if (n.mutexWith(n2, l))
				return false;
		}
		return true;
	}

	// ******************************************************
	// Plan Extraction
	// ******************************************************

	public List extractPlan() {
		return searchPlan(goal, num_layers);
	}

	public List searchPlan(Set goalSet, int l) {

		if (l == 0)
			if (initial.containsAll(goalSet))
				return new ArrayList();
			else
				return null;
		// do memorisation stuff
		Set badGoalSet = (HashSet) memorised.get(l);
		if (badGoalSet.contains(goalSet))
			return null;

		Iterator assit = searchLevel(goalSet, (l - 1)).iterator();

		while (assit.hasNext()) {
			Set as = (HashSet) assit.next();
			Set newgoal = new HashSet();
			for (Iterator ait = as.iterator(); ait.hasNext();) {
				PGAction a = (PGAction) ait.next();
				newgoal.addAll(a.conditions);
			}

			List al = searchPlan(newgoal, l - 1);
			if (al != null) {
				List plan = new ArrayList(al);
				plan.addAll(as);
				return plan;
			}

		}

		// do more memorisation stuff
		badGoalSet.add(goalSet);
		return null;

	}

	public List searchLevel(Set goalSet, int layer) {
		if (goalSet.isEmpty()) {
			Set s = new HashSet();
			List li = new ArrayList();
			li.add(s);
			return li;
		}

		List actionSetList = new ArrayList();
		Set newGoalSet = new HashSet(goalSet);

		PGProposition g = (PGProposition) goalSet.iterator().next();
		newGoalSet.remove(g);

		Iterator ait = g.achievedBy.iterator();
		while (ait.hasNext()) {
			PGAction a = (PGAction) ait.next();
			if ((a instanceof PGNoOp) && a.layer <= layer && a.layer >= 0) {
				Set newnewGoalSet = new HashSet(newGoalSet);
				newnewGoalSet.removeAll(a.achieves);
				Iterator lit = searchLevel(newnewGoalSet, layer).iterator();
				while (lit.hasNext()) {
					Set s = (HashSet) lit.next();
					if (noMutexesTest(a, s, layer)) {
						s.add(a);
						actionSetList.add(s);
					}
				}
			}
		}

		ait = g.achievedBy.iterator();
		while (ait.hasNext()) {
			PGAction a = (PGAction) ait.next();
			if (!(a instanceof PGNoOp) && a.layer <= layer && a.layer >= 0) {
				Set newnewGoalSet = new HashSet(newGoalSet);
				newnewGoalSet.removeAll(a.achieves);
				Iterator lit = searchLevel(newnewGoalSet, layer).iterator();
				while (lit.hasNext()) {
					Set s = (HashSet) lit.next();
					if (noMutexesTest(a, s, layer)) {
						s.add(a);
						actionSetList.add(s);
					}
				}
			}
		}

		return actionSetList;
	}

	// ******************************************************
	// Useful Methods
	// ******************************************************

	public int getLayer(Action a) {
		return ((PGAction) actionMap.get(a)).layer;
	}

	// ******************************************************
	// protected Classes
	// ******************************************************
	protected class Node {
		public int layer;
		public Set mutexes;

		public Map mutexTable;

		public Node() {
		}

		public void reset() {
			layer = -1;
			mutexes = new HashSet();
			mutexTable = new Hashtable();
		}

		public void setMutex(Node n, int l) {
			n.mutexTable.put(this, Integer.valueOf(l));
			this.mutexTable.put(n, Integer.valueOf(l));
		}

		public boolean mutexWith(Node n, int l) {
			/*
			 * if (this == n) return false; Iterator mit = mutexes.iterator(); while
			 * (mit.hasNext()) { Mutex m = (Mutex) mit.next(); if (m.contains(n)) { return
			 * m.layer >= l; } } return false;
			 */
			Object o = mutexTable.get(n);
			if (o == null)
				return false;
			return ((Integer) o).intValue() >= l;
		}

		public void makeMutex(Node n2, int l, Set mutexPairs) {
			setMutex(n2, l);
			n2.setMutex(this, l);
			mutexPairs.add(new MutexPair(this, n2));
		}
	}

	protected class PGAction extends Node {
		public Action action;
		public int counter;
		public int difficulty;

		public Set conditions = new HashSet();
		public Set achieves = new HashSet();
		public Set deletes = new HashSet();

		public PGAction() {

		}

		public PGAction(Action a) {
			action = a;
		}

		public Set getComparators() {
			return action.getComparators();
		}

		public Set getOperators() {
			return action.getOperators();
		}

		public void reset() {
			super.reset();
			difficulty = counter = 0;
		}

		public String toString() {
			return action.toString();
		}

		public boolean checkActionMutex(PGAction a2, int l) {
			if (a2 == this)
				return false;
			for (Iterator p1it = this.deletes.iterator(); p1it.hasNext();) {
				PGProposition p1 = (PGProposition) p1it.next();
				if (a2.achieves.contains(p1))
					return true;
				if (a2.conditions.contains(p1))
					return true;
			}
			for (Iterator p2it = a2.deletes.iterator(); p2it.hasNext();) {
				PGProposition p2 = (PGProposition) p2it.next();
				if (this.achieves.contains(p2))
					return true;
				if (this.conditions.contains(p2))
					return true;
			}
			for (Iterator pc1it = this.conditions.iterator(); pc1it.hasNext();) {
				PGProposition p1 = (PGProposition) pc1it.next();
				Iterator pc2it = a2.conditions.iterator();
				while (pc2it.hasNext()) {
					PGProposition p2 = (PGProposition) pc2it.next();
					if (p1.mutexWith(p2, l))
						return true;
				}
			}
			return false;
		}
	}

	protected class PGNoOp extends PGAction {
		public PGProposition proposition;

		public PGNoOp(PGProposition p) {
			proposition = p;
		}

		public String toString() {
			return ("No-Op " + proposition);
		}

		public Set getComparators() {
			return new HashSet();
		}

		public Set getOperators() {
			return new HashSet();
		}
	}

	protected class PGProposition extends Node {
		public Proposition proposition;

		public Set achieves = new HashSet();
		public Set achievedBy = new HashSet();
		public Set deletedBy = new HashSet();

		public PGProposition(Proposition p) {
			proposition = p;
		}

		public String toString() {
			return proposition.toString();
		}

		public boolean checkPropMutex(PGProposition p2, int l) {
			if (this == p2 || this.achievedBy.isEmpty() || p2.achievedBy.isEmpty())
				return false;
			Iterator a1it = this.achievedBy.iterator();
			while (a1it.hasNext()) {
				PGAction a1 = (PGAction) a1it.next();
				if (a1.layer >= 0) {
					Iterator a2it = p2.achievedBy.iterator();
					while (a2it.hasNext()) {
						PGAction a2 = (PGAction) a2it.next();
						if (a2.layer >= 0 && !a1.mutexWith(a2, l - 1))
							return false;
					}
				}
			}
			return true;
		}
	}

	protected class MutexPair {
		public Node node1;
		public Node node2;

		public MutexPair(Node n1, Node n2) {
			node1 = n1;
			node2 = n2;
		}
	}

	// ******************************************************
	// Debugging Classes
	// ******************************************************
	public void printGraph() {
		for (int i = 0; i <= num_layers; ++i) {
			System.out.println("-----Layer " + i + "----------------------------------------");
			printLayer(i);
		}
		System.out.println("-----End -----------------------------------------------");
	}

	public void printLayer(int i) {
		System.out.println("Facts:");
		for (Iterator pit = planningGraphProduct.getPropositions().iterator(); pit.hasNext();) {
			PGProposition p = (PGProposition) pit.next();
			if (p.layer <= i && p.layer >= 0) {
				System.out.println("\t" + p);
				System.out.println("\t\tmutex with");
				Iterator mit = p.mutexTable.keySet().iterator();
				while (mit.hasNext()) {
					PGProposition pm = (PGProposition) mit.next();
					Integer il = (Integer) p.mutexTable.get(pm);
					if (il.intValue() >= i) {
						System.out.println("\t\t\t" + pm);
					}
				}
			}
		}
		if (i == num_layers)
			return;
		System.out.println("Actions:");
		for (Iterator ait = actions.iterator(); ait.hasNext();) {
			PGAction a = (PGAction) ait.next();
			if (a.layer <= i && a.layer >= 0) {
				System.out.println("\t" + a);
				System.out.println("\t\tmutex with");
				Iterator mit = a.mutexTable.keySet().iterator();
				while (mit.hasNext()) {
					PGAction am = (PGAction) mit.next();
					Integer il = (Integer) a.mutexTable.get(am);
					if (il.intValue() >= i) {
						System.out.println("\t\t\t" + am);
					}
				}
			}
		}
	}

}