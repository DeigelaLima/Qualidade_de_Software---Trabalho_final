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
import java.util.Collection;
import java.util.Iterator;

import javaff.data.metric.NamedFunction;
import javaff.data.strips.Operator;

public abstract class PDDLPrinter {
	public static void printToString(PDDLPrintable p, PrintStream s, boolean typed, boolean bracketed, int indent) {
		printIndent(s, indent);
		printToString(p, s, typed, bracketed);
	}

	public static void printToString(PDDLPrintable p, PrintStream s, boolean typed, boolean bracketed) {
		if (bracketed)
			s.print("(");
		if (typed)
			s.print(p.toStringTyped());
		else
			s.print(p.toString());
		if (bracketed)
			s.print(")");
	}

	public static void printIndent(PrintStream s, int indent) {
		for (int i = 0; i < indent; ++i) {
			s.print("\t");
		}
	}

	public static void printToString(Collection c, PrintStream s, boolean typed, boolean bracketed, int indent) {
		Iterator it = c.iterator();
		while (it.hasNext()) {
			printIndent(s, indent);
			PDDLPrintable p = (PDDLPrintable) it.next();
			printToString(p, s, typed, bracketed);
			if (it.hasNext())
				s.println();
		}
	}

	public static void printToString(Collection c, PrintStream s, boolean typed, boolean bracketed) {
		printToString(c, s, typed, bracketed, 0);
	}

	public static void printToString(Collection c, String label, PrintStream s, boolean typed, boolean bracketed,
			int indent) {
		s.println();
		printIndent(s, indent);
		s.print("(");
		s.println(label);
		printToString(c, s, typed, bracketed, indent + 1);
		s.print(")");
	}

	public static void printToString(Collection c, String label, PrintStream s, boolean typed, boolean bracketed) {
		printToString(c, label, s, typed, bracketed, 0);
	}

	public static void printDomainFile(UngroundProblem p, java.io.PrintStream pstream) {
		pstream.println("(define (domain " + p.domainName + ")");

		pstream.print("\t(:requirements");
		Iterator it = p.requirements.iterator();
		while (it.hasNext())
			pstream.print(" " + it.next());
		pstream.print(")");

		printToString(p.types, ":types", pstream, true, false, 1);
		if (!p.constants.isEmpty())
			printToString(p.constants, ":constants", pstream, true, false, 1);
		printToString(p.predSymbols, ":predicates", pstream, true, true, 1);
		if (!p.funcSymbols.isEmpty())
			printToString(p.funcSymbols, ":functions", pstream, true, true, 1);

		it = p.actions.iterator();
		while (it.hasNext()) {
			pstream.println();
			((Operator) it.next()).pddlPrint(pstream, 1);
		}

		pstream.println(")");
	}

	public static void printProblemFile(UngroundProblem p, java.io.PrintStream pstream) {
		pstream.println("(define (problem " + p.problemName + ")");
		pstream.print("\t(:domain " + p.problemDomainName + ")");

		printToString(p.objects, ":objects", pstream, true, false, 1);
		pstream.println();
		printIndent(pstream, 1);
		pstream.print("(");
		pstream.println(":init");
		printToString(p.initial, pstream, false, true, 2);
		for (Iterator it = p.funcValues.keySet().iterator(); it.hasNext();) {
			NamedFunction nf = (NamedFunction) it.next();
			pstream.println();
			printIndent(pstream, 2);
			pstream.print("(= ");
			printToString(nf, pstream, false, false);
			pstream.print(" " + p.funcValues.get(nf) + ")");
		}
		pstream.print(")");

		pstream.print("\n\t(:goal ");
		p.goal.pddlPrint(pstream, 2);
		pstream.println(")");
		printIndent(pstream, 1);
		if (p.metric != null)
			p.metric.pddlPrint(pstream, 1);
		pstream.print(")");
	}
}
