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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;

import org.decisiondeck.jlp.LpConstraint;
import org.decisiondeck.jlp.problem.LpProblem;
import org.decisiondeck.jlp.problem.LpVariableType;

/**
 * <p>
 * A valid, but not necessarily optimal, result of a {@link LpProblem}. The problem that this solution satisfies is
 * bound to this solution object. This permits to also query for, e.g., constraints values, provided the adequate
 * variables have a value set.
 * </p>
 * <p>
 * The type of the objects used for the variables should have their {@link #equals(Object)} method implemented in a
 * meaningful way as this is used when retrieving the values, and their {@link #hashCode()} method should be correctly
 * implemented. The variables should be immutable.
 * </p>
 * <p>
 * Two solutions are {@link #equals(Object)} iff they have the same values after conversion by
 * {@link Number#doubleValue()}.
 * </p>
 * <p>
 * This interface has been designed for use with immutable numbers. The types {@link Double}, {@link Integer},
 * {@link BigDecimal}, {@link BigInteger} will pose no problem. Using other types as numbers is not recommended.
 * </p>
 * 
 * @param <T>
 *            the class used for the variables.
 * 
 * @author Olivier Cailloux
 * 
 */
public interface LpSolution<T> extends LpSolutionAlone<T> {

    /**
     * Tests whether the variables that have a type {@link LpVariableType#BOOL} (according to the bound problem) and
     * that have a solution indeed have a value of zero or one ± 1e-6.
     * 
     * @return <code>true</code> iff the boolean variables have boolean values.
     */
    public boolean boolsAreBools();

    /**
     * <p>
     * A convenience method to return the primal value of the given variable as a boolean. This is <code>true</code> iff
     * the value is one ± 1e-6, <code>false</code> iff the value is zero ± 1e-6. Otherwize a runtime exception is
     * thrown.
     * </p>
     * <p>
     * If the given variable is not in the bound problem, an exception is thrown.
     * </p>
     * <p>
     * It is suggested to ensure, after a solution is obtained by a solver and before using this method, that the
     * supposedly boolean variables are indeed set to a value that is zero or one ± 1e-6 because some solvers might have
     * an imprecision factor higher than this. The method {@link #boolsAreBools()} may be used to do so.
     * </p>
     * 
     * @param variable
     *            not <code>null</code>, must have an associated value close enough to zero or one.
     * @return <code>true</code> for one, <code>false</code> for zero.
     */
    @Override
    public boolean getBooleanValue(T variable);

    /**
     * Retrieves the value of the objective function computed from the objective function itself with the values of the
     * variables set in this solution. Returns <code>null</code> if the objective function is not set in the bound
     * problem or one of the variables required value is not set.
     * 
     * @return possibly <code>null</code>.
     */
    public Number getComputedObjectiveValue();

    /**
     * Returns, if it is known, the value corresponding to the dual variable associated to the given primal constraint.
     * Returns necessarily <code>null</code> if the constraint is not in the associated problem.
     * 
     * @param constraint
     *            not <code>null</code>.
     * @return <code>null</code> iff the variable has no associated dual value.
     */
    @Override
    public Number getDualValue(LpConstraint<T> constraint);

    /**
     * Returns the objective value. Returns necessarily <code>null</code> if the bound problem has no objective
     * function.
     * 
     * @return <code>null</code> if not set.
     */
    @Override
    public Number getObjectiveValue();

    /**
     * Retrieves the problem that this solution solves.
     * 
     * @return not <code>null</code>, immutable.
     */
    public LpProblem<T> getProblem();

    /**
     * Returns the primal value of the variable, if it is known. Returns necessarily <code>null</code> if the given
     * variable is not in the bound problem.
     * 
     * @param variable
     *            not <code>null</code>.
     * @return <code>null</code> iff the variable has no associated primal value.
     */
    @Override
    public Number getValue(T variable);

    /**
     * Retrieves a copy or read-only view of the variables which have a solution value. The returned set is guaranteed
     * to be included in the set of variables contained in the bound problem.
     * 
     * @return not <code>null</code>, but may be empty.
     */
    @Override
    public Set<T> getVariables();

}
