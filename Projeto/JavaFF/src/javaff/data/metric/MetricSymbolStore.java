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

import java.math.BigDecimal;

public abstract class MetricSymbolStore {
	public static int greaterThan;
	public static int greaterThanEqual = 1;
	public static int lessThan = 2;
	public static int lessThanEqual = 3;
	public static int equal = 4;
	public static int plus = 5;
	public static int minus = 6;
	public static int multiply = 7;
	public static int divide = 8;
	public static int assign = 9;
	public static int increase = 10;
	public static int decrease = 11;
	public static int scaleUP = 12;
	public static int scaleDown = 13;
	public static int scale = 2;
	public static int round = BigDecimal.ROUND_HALF_EVEN;

	public static int getType(String s) {
		if (">".equals(s))
			return greaterThan;
		if (">=".equals(s))
			return greaterThanEqual;
		else if ("<".equals(s))
			return lessThan;
		else if ("<=".equals(s))
			return lessThanEqual;
		else if ("=".equals(s))
			return equal;
		else if ("+".equals(s))
			return plus;
		else if ("-".equals(s))
			return minus;
		else if ("*".equals(s))
			return multiply;
		else if ("/".equals(s))
			return divide;
		else if ("assign".equals(s) || ":=".equals(s))
			return assign;
		else if ("increase".equals(s) || "+=".equals(s))
			return increase;
		else if ("decrease".equals(s) || "-=".equals(s))
			return decrease;
		else if ("scale-up".equals(s) || "*=".equals(s))
			return scaleUP;
		else if ("scale-down".equals(s) || "/=".equals(s))
			return scaleDown;
		else
			return -1;
	}

	public static String getSymbol(int i) {
		switch (i) {
		case 0:
			return ">";
		case 1:
			return ">=";
		case 2:
			return "<";
		case 3:
			return "<=";
		case 4:
			return "=";
		case 5:
			return "+";
		case 6:
			return "-";
		case 7:
			return "*";
		case 8:
			return "/";
		case 9:
			return "assign";
		case 10:
			return "increase";
		case 11:
			return "decrease";
		case 12:
			return "scale-up";
		case 13:
			return "scale-down";
		}
		return "";
	}
}
