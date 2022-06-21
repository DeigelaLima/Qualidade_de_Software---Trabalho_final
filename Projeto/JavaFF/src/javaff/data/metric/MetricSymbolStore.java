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

public abstract class MetricSymbolStore
{
	public static int GREATER_THAN = 0;
    public static int GREATER_THAN_EQUAL = 1;
    public static int LESS_THAN = 2;
    public static int LESS_THAN_EQUAL = 3;
    public static int EQUAL = 4;
	
	public static int PLUS = 5;
    public static int MINUS = 6;
    public static int MULTIPLY = 7;
    public static int DIVIDE = 8;

	public static int ASSIGN = 9;
    public static int INCREASE = 10;
    public static int DECREASE = 11;
    public static int SCALE_UP = 12;
    public static int SCALE_DOWN = 13;

	public static int SCALE = 2;
    public static int ROUND = BigDecimal.ROUND_HALF_EVEN;

	public static int getType(String s)
	{
		if (">".equals(s)) return GREATER_THAN;
		else if (">=".equals(s)) return GREATER_THAN_EQUAL;
		else if ("<".equals(s)) return LESS_THAN;
		else if ("<=".equals(s)) return LESS_THAN_EQUAL;
		else if ("=".equals(s)) return EQUAL;
		else if ("+".equals(s)) return PLUS;
		else if ("-".equals(s)) return MINUS;
		else if ("*".equals(s)) return MULTIPLY;
		else if ("/".equals(s)) return DIVIDE;
		else if ("assign".equals(s) || ":=".equals(s)) return ASSIGN;
		else if ("increase".equals(s) || "+=".equals(s)) return INCREASE;
		else if ("decrease".equals(s) || "-=".equals(s)) return DECREASE;
		else if ("scale-up".equals(s) || "*=".equals(s)) return SCALE_UP;
        else if ("scale-down".equals(s) || "/=".equals(s)) return SCALE_DOWN;
		else return -1;
	}

	public static String getSymbol(int t)
	{
		switch (t)
		{
			case 0: return ">";
			case 1: return ">=";
			case 2: return "<";
			case 3: return "<=";
			case 4: return "=";
			case 5: return "+";
			case 6: return "-";
			case 7: return "*";
			case 8: return "/";
			case 9: return "assign";
			case 10: return "increase";
			case 11: return "decrease";
			case 12: return "scale-up";
			case 13: return "scale-down";
		}
		return "";
	}
}
