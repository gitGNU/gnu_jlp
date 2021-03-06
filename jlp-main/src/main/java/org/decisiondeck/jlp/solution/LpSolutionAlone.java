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
package org.decisiondeck.jlp.solution;

import java.util.Set;

import org.decisiondeck.jlp.LpConstraint;

/**
 * <p>
 * A valid, but not necessarily optimal, solution to a linear programming problem. The problem that this solution
 * satisfies is <em>not</em> bound to this solution object. The interface {@link LpSolution} should be preferred to this
 * one when the related problem can be bound to the solution, see documentation there.
 * </p>
 * 
 * @param <T>
 *            the class used for the variables.
 * 
 * @author Olivier Cailloux
 * 
 */
public interface LpSolutionAlone<T> {

    /**
     * <p>
     * A convenience method to return the primal value of the given variable as a boolean. This is <code>true</code> iff
     * the value is one ± 1e-6, <code>false</code> iff the value is zero ± 1e-6. Otherwize a runtime exception is
     * thrown.
     * </p>
     * <p>
     * It is suggested to ensure, after a solution is obtained by a solver and before using this method, that the
     * supposedly boolean variables are indeed set to a value that is zero or one ± 1e-6 because some solvers might have
     * an imprecision factor higher than this.
     * </p>
     * 
     * @param variable
     *            not <code>null</code>, must have an associated value close enough to zero or one.
     * @return <code>true</code> for one, <code>false</code> for zero.
     */
    public boolean getBooleanValue(T variable);

    /**
     * Retrieves a copy or read-only view of the primal constraints, i.e. dual variables, which have their dual value
     * set.
     * 
     * @return not <code>null</code>, but may be empty.
     */
    public Set<LpConstraint<T>> getConstraints();

    /**
     * Returns, if it is known, the value corresponding to the dual variable associated to the given primal constraint.
     * 
     * @param constraint
     *            not <code>null</code>.
     * @return <code>null</code> iff the variable has no associated dual value.
     */
    public Number getDualValue(LpConstraint<T> constraint);

    /**
     * Returns the objective value.
     * 
     * @return <code>null</code> if not set.
     */
    public Number getObjectiveValue();

    /**
     * Returns the primal value of the variable, if it is known.
     * 
     * @param variable
     *            not <code>null</code>.
     * @return <code>null</code> iff the variable has no associated primal value.
     */
    public Number getValue(T variable);

    /**
     * Retrieves a copy or read-only view of the variables which have their primal value set.
     * 
     * @return not <code>null</code>, but may be empty.
     */
    public Set<T> getVariables();

}
