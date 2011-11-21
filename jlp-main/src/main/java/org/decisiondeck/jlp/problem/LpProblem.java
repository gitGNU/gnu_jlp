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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;

import org.decisiondeck.jlp.LpConstraint;
import org.decisiondeck.jlp.LpDirection;
import org.decisiondeck.jlp.LpLinear;
import org.decisiondeck.jlp.LpObjective;
import org.decisiondeck.jlp.LpOperator;
import org.decisiondeck.jlp.parameters.LpObjectParameter;

import com.google.common.base.Function;

/**
 * <p>
 * A linear programming problem (in the sense of a mathematical programming problem), or mixed integer programming
 * problem, consisting of variables, constraints and zero or one objective function.
 * </p>
 * <p>
 * The order of additions of the variables and constraints is retained and reused when reading variables and constraints
 * sets.
 * </p>
 * <p>
 * The test for equality between two number values is done after conversion according to {@link Number#doubleValue()},
 * for equality between two problems and for the {@link #setVarBounds(Object, Number, Number)} method.
 * </p>
 * <p>
 * This interface has been designed for use with immutable numbers: the types {@link Double}, {@link Integer},
 * {@link BigDecimal}, {@link BigInteger} will pose no problem. Using other types of numbers is not recommended.
 * </p>
 * <p>
 * Some implementations of this interface may be read-only (either because they are immutable or because they are a
 * read-only view), in which case an attempt to write to the object will throw {@link UnsupportedOperationException}.
 * </p>
 * <p>
 * Two such problems are equivalent when they define the same variables (as per {@link #equals}), constraints, objective
 * function, irrespective of the problem, variables and constraints names.
 * </p>
 * 
 * @param <T>
 *            the class used for the variables.
 * 
 * @author Olivier Cailloux
 * 
 */
public interface LpProblem<T> {

    /**
     * Adds a constraint, or does nothing if the given constraint is already in the problem. The variables used in the
     * objective must have been added to this problem already.
     * 
     * @param constraint
     *            the constraint to be added. Not <code>null</code>.
     * @return <code>true</code> iff the call modified the state of this object. Equivalently, returns
     *         <code>false</code> iff the given constraint already was in the problem.
     */
    public boolean add(LpConstraint<T> constraint);

    /**
     * Adds a constraint, or does nothing if the given constraint is already in the problem. The variables used in the
     * objective must have been added to this problem already.
     * 
     * @param id
     *            the id of the constraint. If <code>null</code>, a no-id constraint is used.
     * @param lhs
     *            the left-hand-side linear expression. Not <code>null</code>.
     * @param operator
     *            the operator. Not <code>null</code>.
     * @param rhs
     *            the right-hand-side number. A real value (not NaN or infinite).
     * @return <code>true</code> iff the call modified the state of this object. Equivalently, returns
     *         <code>false</code> iff the given constraint already was in the problem.
     */
    public boolean add(Object id, LpLinear<T> lhs, LpOperator operator, double rhs);

    /**
     * Adds the variable to this problem if it is not already in with a default type of REAL, no name, no lower and
     * upper bounds.
     * 
     * @param variable
     *            not <code>null</code>.
     * @return <code>true</code> iff the call modified the state of this object.
     */
    public boolean addVariable(T variable);

    /**
     * Removes all the variables and constraints, objective function, and name set in this problem. This problem is then
     * as a newly created, empty problem.
     */
    public void clear();

    /**
     * Retrieves a copy or read-only view of the constraints in this problem. The returned set uses insertion order,
     * thus is iterated in the order the constraints have been added to this problem.
     * 
     * @return not <code>null</code>, but may be empty.
     */
    public Set<LpConstraint<T>> getConstraints();

    /**
     * Retrieves the dimension of this problem in number of variables and constraints. The bounds do not count as
     * constraints.
     * 
     * @return not <code>null</code>.
     */
    public LpDimension getDimension();

    /**
     * Retrieves the name of the problem.
     * 
     * @return never <code>null</code>, empty if not set.
     */
    public String getName();

    /**
     * Retrieves a copy or a read-only view of the objective function. It is possible that the objective function or the
     * direction inside is <code>null</code>, or that both are <code>null</code>.
     * 
     * @return not <code>null</code>.
     */
    public LpObjective<T> getObjective();

    /**
     * Retrieves a copy or a read-only view of the variables. The returned set uses insertion order, thus is iterated in
     * the order the variables have been added to this problem.
     * 
     * @return not <code>null</code>, but may be empty.
     */
    public Set<T> getVariables();

    /**
     * Retrieves the lower bound of the given variable if it is set. The lower bound is less or equal to the upper
     * bound, if both are set.
     * 
     * @param variable
     *            not <code>null</code>, must be a variable of this problem.
     * @return <code>null</code> iff no lower bound is associated to the given variable.
     */
    public Number getVarLowerBound(T variable);

    /**
     * Retrieves the name of the variable as set in this problem.
     * 
     * @param variable
     *            not <code>null</code>, must be a variable of this problem.
     * @return not <code>null</code>, empty string if the variable has no name.
     * @see #setVarName
     */
    public String getVarNameSet(T variable);

    /**
     * Retrieves the name of the variable as computed from the variable namer function, if it is set, or taken from the
     * name of the variable set in this problem, if it has been set.
     * 
     * @param variable
     *            not <code>null</code>, must be a variable of this problem.
     * @return not <code>null</code>, empty string if the variable has no name.
     * @see #setVarNamer
     * @see #setVarName
     */
    public String getVarNameComputed(T variable);

    /**
     * <p>
     * Sets the namer function that is used to associate names to constraints. If the given namer is <code>null</code>,
     * the namer function is set back to the default function. If the given namer sometimes returns <code>null</code>
     * values, it is transformed to return empty strings instead so that the resulting function never returns
     * <code>null</code>. The function is never given a <code>null</code> constraint; however the constraint id may be
     * <code>null</code>.
     * </p>
     * <p>
     * The default namer uses the names deduced from the ids associated to the constraints, using
     * {@link LpConstraint#getName()}. In such a case, a constraint has no name iff it has no id.
     * </p>
     * 
     * @param namer
     *            <code>null</code> to reset default behavior.
     * @see #getConstraintsNamer()
     * @see LpObjectParameter#NAMER_CONSTRAINTS
     */
    public void setConstraintsNamer(Function<LpConstraint<T>, String> namer);

    /**
     * Retrieves the function used to name the constraints. The function returns an empty string for an unnamed
     * constraint and is guaranteed to never return <code>null</code>.
     * 
     * @return not <code>null</code>.
     * @see #setConstraintsNamer(Function)
     */
    public Function<LpConstraint<T>, String> getConstraintsNamer();

    /**
     * Retrieves the type of a variable.
     * 
     * @param variable
     *            not <code>null</code>, must be a variable of this problem.
     * @return not <code>null</code>.
     */
    public LpVariableType getVarType(T variable);

    /**
     * Retrieves the upper bound of the given variable if it is set. The upper bound is greater or equal to the lower
     * bound, if both are set.
     * 
     * @param variable
     *            not <code>null</code>, must be a variable of this problem.
     * @return <code>null</code> iff no upper bound is associated to the given variable.
     */
    public Number getVarUpperBound(T variable);

    /**
     * Sets or removes the name of this problem.
     * 
     * @param name
     *            <code>null</code> or empty string for no name. A <code>null</code> string is converted to an empty
     *            string.
     * @return <code>true</code> iff the call modified the state of this object. Equivalently, returns
     *         <code>false</code> iff the given name was different than this problem name.
     * 
     */
    public boolean setName(String name);

    /**
     * Sets or removes the objective bound to this problem. The variables used in the objective function must have been
     * added to this problem already. Setting both parameters to <code>null</code> is legal.
     * 
     * @param objectiveFunction
     *            <code>null</code> to remove a possibly set objective function.
     * @param direction
     *            <code>null</code> for not set (removes a possibly set optimization direction).
     * @return <code>true</code> iff the call modified the state of this object.
     */
    public boolean setObjective(LpLinear<T> objectiveFunction, LpDirection direction);

    /**
     * Sets or removes the optimization direction. The objective function itself (without direction), if set, is
     * unchanged: only the direction possibly changes.
     * 
     * @param dir
     *            <code>null</code> to remove, if set, the optimization direction information.
     * @return <code>true</code> iff the call modified the state of this object.
     */
    public boolean setObjectiveDirection(LpDirection dir);

    /**
     * Sets or removes the lower and upper bounds of a variable. Adds the variable to this problem if it is not already
     * in, with a REAL type as default type. If both bounds are non <code>null</code>, the lower one must be less or
     * equal to the upper one. If the given lower bound alone is non <code>null</code>, it must be less or equal to the
     * existing upper bound, if set; and similarily for the given upper bound. TODO change this!
     * 
     * @param variable
     *            not <code>null</code>.
     * @param lowerBound
     *            <code>null</code> for no lower bound. Removes the existing lower bound if one is set.
     * @param upperBound
     *            <code>null</code> for no upper bound. Removes the existing upper bound if one is set.
     * @return <code>true</code> iff the call modified the state of this object. When the bounds are not
     *         <code>null</code>, this is asserted by comparing the previous and new bounds values using the number
     *         {@link #equals(Object)} method.
     */
    public boolean setVarBounds(T variable, Number lowerBound, Number upperBound);

    /**
     * Sets or removes the lower bound of a variable. Adds the variable to this problem if it is not already in, with a
     * REAL type as default type.
     * 
     * @param variable
     *            not <code>null</code>.
     * @param name
     *            empty string or <code>null</code> to remove, if set, the name associated to the given variable.
     * @return <code>true</code> iff the call modified the state of this object.
     */
    public boolean setVarName(T variable, String name);

    /**
     * Sets or removes the type of a variable. Adds the variable to this problem if it is not already in.
     * 
     * @param variable
     *            not <code>null</code>.
     * @param type
     *            not <code>null</code>.
     * @return <code>true</code> iff the call modified the state of this object.
     */
    public boolean setVarType(T variable, LpVariableType type);

    public Function<T, String> getVarNamer();

    /**
     * <p>
     * Sets the namer function that is used to associate names to variables. If the namer is <code>null</code> (the
     * default), this object uses the names associated to the variables using {@link #setVarName} to compute the
     * variable names.
     * </p>
     * <p>
     * An example function that can be used is available through {@link LpProblemUtils#getToStringFunction()}.
     * </p>
     * <p>
     * TODO make the tostring function the default; remove manual var names; add wrapper to set the var names through a
     * dedicated namer function (if useful).
     * </p>
     * 
     * @param namer
     *            <code>null</code> to default to the names set with {@link #setVarName}.
     * @see #getVarNameComputed
     * @see LpObjectParameter#NAMER_VARIABLES
     */
    public void setVarNamer(Function<T, String> namer);
}
