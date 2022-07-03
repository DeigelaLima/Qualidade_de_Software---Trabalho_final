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

package javaff.search;

import java.util.Comparator;
import java.util.Hashtable;
import java.util.TreeSet;

import javaff.planning.Filter;
import javaff.planning.State;

public class BestFirstSearch extends Search {

	protected Hashtable closed;
	protected TreeSet open;
	protected Filter filter;

	public BestFirstSearch(State state) {
		this(state, new HValueComparator());
	}

	public BestFirstSearch(State state, Comparator comparator) {
		super(state);
		setComparator(comparator);

		closed = new Hashtable();
		open = new TreeSet(comparator);
	}

	public void setFilter(Filter f) {
		this.filter = f;
	}

	public void updateOpen(State s) {
		open.addAll(s.getNextStates(filter.getActions(s)));
	}

	public State removeNext() {
		State state = (State) ((TreeSet) this.open).first();
		this.open.remove(state);
		return state;
	}

	public boolean needToVisit(State s) {
		Integer shash = Integer.valueOf(s.hashCode());
		State dState = (State) closed.get(shash);

		if (closed.containsKey(shash) && dState.equals(s))
			return false;

		closed.put(shash, s);
		return true;
	}

	public State search() {

		open.add(start);

		while (!open.isEmpty()) {
			State s = removeNext();
			if (needToVisit(s)) {
				++nodeCount;
				if (s.goalReached()) {
					return s;
				}
				updateOpen(s);
			}

		}
		return null;
	}

}
