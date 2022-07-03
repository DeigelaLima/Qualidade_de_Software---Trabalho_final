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
import java.util.List;

import javaff.data.strips.InstantAction;

public class TemporalConstraint extends Constraint
{
	InstantAction x;
	InstantAction y;
	BigDecimal b;

	public TemporalConstraint(InstantAction x, InstantAction y, BigDecimal b)
	{
		this.x = x;
		this.y = y;
		this.b = b;
	}

	public static TemporalConstraint getConstraint(InstantAction first, InstantAction second)
	{
		return new TemporalConstraint(first, second, javaff.JavaFF.epsilon.negate());
	}

	public static TemporalConstraint getConstraintEqual(InstantAction first, InstantAction second)
	{
		return new TemporalConstraint(first, second, new BigDecimal(0));
	}

	public static TemporalConstraint getConstraintMax(InstantAction first, InstantAction second, BigDecimal max)
	{
		return new TemporalConstraint(second, first, max);
	}

	public static TemporalConstraint getConstraintMin(InstantAction first, InstantAction second, BigDecimal min)
	{          
                return new TemporalConstraint(first, second, min.negate());
	}
	
	public static List getExactly(InstantAction first, InstantAction second, BigDecimal value)
	{
		List rList = new ArrayList(2);
		rList.add(getConstraintMax(first, second, value));
		rList.add(getConstraintMin(first, second, value));
		return rList;
	}

	public static List getBounds(InstantAction first, InstantAction second, BigDecimal max, BigDecimal min)
	{
		List rList = new ArrayList(2);
		rList.add(getConstraintMax(first, second, max));
		rList.add(getConstraintMin(first, second, min));
		return rList;
	}
		
	public String toString()
	{
		return (x.toString() + " - " + y.toString() +" <= "+b.toString());
	}

	public boolean equals(Object o)
	{
		if (o instanceof TemporalConstraint)
		{
			TemporalConstraint c = (TemporalConstraint) o;
			return (c.x.equals(x) && c.y.equals(y) && c.b.equals(b));
		}
		return false;
	}

	public int hashCode()
	{
		int hash = 31 * 2 ^ x.hashCode();
		hash = 31 * (31 * hash ^ y.hashCode()) ^ b.hashCode();
		return hash;
	}
}
