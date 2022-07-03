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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import javaff.data.Action;
import javaff.data.Plan;
import javaff.data.TimeStampedPlan;
import javaff.data.strips.InstantAction;
import javaff.data.strips.OperatorName;
import javaff.data.strips.STRIPSInstantAction;
import javaff.data.temporal.DurativeAction;
import javaff.data.temporal.EndInstantAction;
import javaff.data.temporal.StartInstantAction;

public class MatrixSTN implements SimpleTemporalNetwork {
	static BigDecimal epsilon = javaff.JavaFF.epsilon;
	static BigDecimal zero = new BigDecimal(0);
	static BigDecimal inf = new BigDecimal(100000);
	static BigDecimal negEpsilon = epsilon.negate();
	static int scale = 2;
	static int ROUND = BigDecimal.ROUND_HALF_EVEN;

	BigDecimal[][] theArray;
	ArrayList timePoints;
	Plan pop;
	int size;

	InstantAction start = new STRIPSInstantAction();

	public MatrixSTN(Plan plan) {
		pop = plan;
		start.name = new OperatorName("TIME_ZERO");

		timePoints = new ArrayList();
		timePoints.add(start);
		timePoints.addAll(pop.getActions());

		zero = zero.setScale(scale, ROUND);
		inf = inf.setScale(scale, ROUND);
		negEpsilon = negEpsilon.setScale(scale, ROUND);

		size = timePoints.size();
		theArray = new BigDecimal[size][size];
		theArray[0][0] = zero;
		for (int i = 1; i < size; ++i) {
			theArray[0][i] = inf;
			theArray[i][0] = negEpsilon;

			for (int j = 1; j < size; ++j)
				if (i == j)
					theArray[i][j] = zero;
				else
					theArray[i][j] = inf;
		}
	}

	public void addConstraints(Set constraints) {
		for (Iterator oit = constraints.iterator(); oit.hasNext();) {
			TemporalConstraint c = (TemporalConstraint) oit.next();
			addConstraint(c);
		}
	}

	public void addConstraint(TemporalConstraint c) {
		int firstpos = timePoints.indexOf(c.y);
		int secondpos = timePoints.indexOf(c.x);
		theArray[firstpos][secondpos] = theArray[firstpos][secondpos].min(c.b).setScale(scale, ROUND);
	}

	public void constrain() {
		for (int k = 0; k < size; ++k)
			for (int i = 0; i < size; ++i) {
				for (int j = 0; j < size; ++j) {
					if (theArray[i][j].compareTo(theArray[i][k].add(theArray[k][j])) > 0) {
						theArray[i][j] = theArray[i][k].add(theArray[k][j]).setScale(scale, ROUND);
					}
				}
			}
	}

	public boolean check() {
		for (int i = 0; i < size; ++i)
			if (theArray[i][i].compareTo(zero) < 0) {
				return false;
			}
		return true;
	}

	public boolean consistent() {
		constrain();
		return check();
	}

	public boolean consistentSource(InstantAction a) {
		return consistent();
	}

	public TimeStampedPlan getTimes() {
		TimeStampedPlan plan = new TimeStampedPlan();
		for (Iterator ait = timePoints.iterator(); ait.hasNext();) {
			InstantAction a = (InstantAction) ait.next();
			if (a instanceof StartInstantAction) {
				DurativeAction da = ((StartInstantAction) a).parent;
				BigDecimal time = theArray[timePoints.indexOf(a)][0].negate().setScale(scale, ROUND);
				BigDecimal dur = theArray[timePoints.indexOf(da.endAction)][0].negate().subtract(time).setScale(scale,
						ROUND);
				plan.addAction(da, time, dur);
			} else if (a instanceof STRIPSInstantAction && a != start) {
				BigDecimal time = theArray[timePoints.indexOf(a)][0].negate().setScale(scale, ROUND);
				plan.addAction(a, time);
			}
		}
		return plan;
	}

	public boolean isMinOrZero(Action a, Action b) {
		return (theArray[timePoints.indexOf(b)][timePoints.indexOf(a)].compareTo(zero) < 0);
	}

	public boolean bS(Action a, Action b) {
		return theArray[timePoints.indexOf(b)][timePoints.indexOf(a)].compareTo(zero) <= 0;
	}

	public boolean U(Action a, Action b) {
		BigDecimal v1 = theArray[timePoints.indexOf(b)][timePoints.indexOf(a)];
		BigDecimal v2 = theArray[timePoints.indexOf(b)][timePoints.indexOf(a)];
		return (v1.compareTo(zero) > 0 && v2.compareTo(zero) > 0);
	}

	public Action getEarliest(Set s) {
		Iterator sit = s.iterator();
		Action c = null;
		while (sit.hasNext()) {
			Action a = (Action) sit.next();
			if (c == null)
				c = a;
			else if (theArray[timePoints.indexOf(c)][0].compareTo(theArray[timePoints.indexOf(a)][0]) > 0)
				c = a;
		}
		return c;
	}

	public BigDecimal getMinimum(DurativeAction a) {
		return theArray[timePoints.indexOf(a.endAction)][timePoints.indexOf(a.startAction)].negate();

	}

	public BigDecimal getMaximum(DurativeAction a) {
		return theArray[timePoints.indexOf(a.startAction)][timePoints.indexOf(a.endAction)];
	}

	public void increaseMin(DurativeAction a, BigDecimal diff) {
		BigDecimal v1 = theArray[timePoints.indexOf(a.endAction)][timePoints.indexOf(a.startAction)];
		BigDecimal v2 = v1.subtract(diff);
		theArray[timePoints.indexOf(a.endAction)][timePoints.indexOf(a.startAction)] = v1.min(v2);
	}

	public void decreaseMax(DurativeAction a, BigDecimal diff) {
		BigDecimal v1 = theArray[timePoints.indexOf(a.startAction)][timePoints.indexOf(a.endAction)];
		BigDecimal v2 = v1.subtract(diff);
		theArray[timePoints.indexOf(a.startAction)][timePoints.indexOf(a.endAction)] = v1.min(v2);
	}

	public void maximize(DurativeAction a) {
		theArray[timePoints.indexOf(a.endAction)][timePoints.indexOf(
				a.startAction)] = theArray[timePoints.indexOf(a.startAction)][timePoints.indexOf(a.endAction)]
						.negate();
	}

	public void minimize(DurativeAction a) {
		theArray[timePoints.indexOf(a.startAction)][timePoints
				.indexOf(a.endAction)] = theArray[timePoints.indexOf(a.endAction)][timePoints.indexOf(a.startAction)]
						.negate();
	}

	public void minimizeTime() {
		for (Iterator ait = timePoints.iterator(); ait.hasNext();) {
			InstantAction a = (InstantAction) ait.next();
			if (a instanceof EndInstantAction) {
				int i = timePoints.indexOf(a);
				if (theArray[i][0].compareTo(theArray[0][i].negate()) != 0) {
					theArray[0][i] = theArray[i][0].negate();
					constrain();
				}
			}
		}

	}

	public void minimizeDuration() {
		for (Iterator ait = timePoints.iterator(); ait.hasNext();) {
			InstantAction a = (InstantAction) ait.next();
			if (a instanceof StartInstantAction) {
				DurativeAction da = ((StartInstantAction) a).parent;
				minimize(da);
			}
		}
	}

	public void printArray() {
		System.out.print("                                       ");
		for (int i = 0; i < size; ++i) {
			String istr = (new Integer(i)).toString() + " ";
			istr = "  " + istr.substring(0, 2) + " ";
			System.out.print(istr);
		}

		for (int i = 0; i < size; ++i) {
			String istr = (new Integer(i)).toString() + " ";
			istr = "  " + istr.substring(0, 2) + " ";
			System.out.print((timePoints.get(i).toString() + "                                                 ")
					.substring(0, 35) + istr);
			for (int j = 0; j < size; ++j)
				if (theArray[i][j].compareTo(inf) == 0)
					System.out.print("INF  ");
				else
					System.out.print(theArray[i][j] + "  ");
			System.out.print("\n");
		}
	}

	public Object clone() throws CloneNotSupportedException {
		MatrixSTN stn = (MatrixSTN) super.clone();
		stn.size = size;
		stn.timePoints = timePoints;
		stn.start = start;
		for (int i = 0; i < size; ++i)
			for (int j = 0; j < size; ++j) {
				stn.theArray[i][j] = ((BigDecimal) theArray[i][j]).setScale(scale, ROUND);
			}
		return stn;
	}

}
