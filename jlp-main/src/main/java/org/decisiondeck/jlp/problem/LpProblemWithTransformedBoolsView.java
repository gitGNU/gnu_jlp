/**
 * Copyright © 2010-2011 École Centrale Paris
 *
 * 	This file is part of JLP.
 *
 * 	JLP is free software: you can redistribute it and/or modify it under the
 * 	terms of the GNU Lesser General Public License version 3 as published by
 * 	the Free Software Foundation.
 *
 * 	JLP is distributed in the hope that it will be useful, but WITHOUT ANY
 * 	WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * 	FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * 	more details.
 *
 * 	You should have received a copy of the GNU Lesser General Public License
 * 	along with JLP. If not, see <http://www.gnu.org/licenses/>.
 */
package org.decisiondeck.jlp.problem;

import org.decisiondeck.jlp.LpConstraint;
import org.decisiondeck.jlp.utils.LpSolverUtils;

/**
 * <p>
 * This object may be used to get rid of the boolean types in a problem and view them as integers. It implements a view
 * of a {@link LpProblem} that views every variables defined as {@link LpVariableType#BOOL} rather as
 * {@link LpVariableType#INT} type with possibly modified bounds. Consider a variable defined in the delegate problem
 * having the type {@link LpVariableType#BOOL} and lower and upper bounds <l, u>. This view sees it as a variable of
 * type {@link LpVariableType#INT} with as lower bound the integer 0 if l is <code>null</code>, the integer 0 if
 * l.doubleValue() is lower than zero, and l otherwise; and as upper bound the integer 1 if u is <code>null</code>, the
 * integer 1 if u.doubleValue() is greater than one, and u otherwise. Thus a {@link LpVariableType#BOOL} variable with
 * bounds of <-1, 0.5> become, through the view, an {@link LpVariableType#INT} variable with bounds of <0, 0.5>. The
 * rest of the data is viewed unmodified.
 * </p>
 * <p>
 * The view writes to the delegated objects. Written data are not modified by the view: writing a
 * {@link LpVariableType#BOOL} variable ends up as a {@link LpVariableType#BOOL} variable in the delegate and then will
 * be viewed as an {@link LpVariableType#INT} .
 * </p>
 * <p>
 * Methods that provide the transformations described here only on the bounds, on demand, are also available, see
 * {@link LpSolverUtils#getVarLowerBoundBounded(LpProblem, Object)}.
 * </p>
 * <p>
 * The set of constraints and the set of variables ordering is not changed by this object: the order of iteration of
 * these sets is the order used by the delegate.
 * 
 * @param <T>
 *            the type of the variables objects.
 * 
 * @author Olivier Cailloux
 * 
 */
public class LpProblemWithTransformedBoolsView<T> extends LpProblemForwarder<T> implements LpProblem<T> {

    /**
     * Creates a view that delegates to the given object.
     * 
     * @param delegate
     *            not <code>null</code>.
     */
    public LpProblemWithTransformedBoolsView(LpProblem<T> delegate) {
	super(delegate);
    }

    @Override
    public boolean add(LpConstraint<T> constraint) {
	return delegate().add(constraint);
    }

    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof LpProblem<?>)) {
	    return false;
	}
	LpProblem<?> p2 = (LpProblem<?>) obj;
	return LpSolverUtils.equivalent(this, p2);
    }

    @Override
    public Number getVarLowerBound(T variable) {
	return LpSolverUtils.getVarLowerBoundBounded(delegate(), variable);
    }

    @Override
    public LpVariableType getVarType(T variable) {
	final LpVariableType type = delegate().getVarType(variable);
	switch (type) {
	case BOOL:
	case INT:
	    return LpVariableType.INT;
	case REAL:
	    return LpVariableType.REAL;
	}
	throw new IllegalStateException("Unknown type.");
    }

    @Override
    public int hashCode() {
	return LpSolverUtils.getProblemEquivalence().hash(this);
    }

    @Override
    public String toString() {
	return LpSolverUtils.getAsString(this);
    }

}
