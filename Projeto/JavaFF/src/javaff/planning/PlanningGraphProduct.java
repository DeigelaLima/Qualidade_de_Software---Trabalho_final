package javaff.planning;


import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javaff.data.strips.Proposition;
import javaff.planning.PlanningGraph.PGProposition;

public class PlanningGraphProduct {
	private Map propositionMap = new Hashtable();
	private Set propositions = new HashSet();

	public Set getPropositions() {
		return propositions;
	}

	public PGProposition getProposition(Proposition p, PlanningGraph g) {
		Object o = propositionMap.get(p);
		PGProposition pgp;
		if (o == null) {
			pgp = g.new PGProposition(p);
			propositionMap.put(p, pgp);
			propositions.add(pgp);
		} else
			pgp = (PGProposition) o;
		return pgp;
	}
}