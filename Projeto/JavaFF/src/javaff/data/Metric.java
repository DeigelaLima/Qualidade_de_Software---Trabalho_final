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

package javaff.data;

import java.io.PrintStream;

import javaff.data.metric.Function;

public class Metric implements PDDLPrintable {
	public static int maximize;
	public static int minimize = 1;

	public int type;
	public Function func;

	public Metric(int t, Function f) {
		type = t;
		func = f;
	}

	public void pddlPrint(PrintStream s, int indent) {
		s.print("(:metric ");
		s.print(toString());
		s.print(")");
	}

	public String toString() {
		String str = "";
		if (type == maximize)
			str += "maximize ";
		else if (type == minimize)
			str += "minimize ";
		return str += func.toString();
	}

	public String toStringTyped() {
		String str = "";
		if (type == maximize)
			str += "maximize ";
		else if (type == minimize)
			str += "minimize ";
		return str += func.toStringTyped();
	}
}
