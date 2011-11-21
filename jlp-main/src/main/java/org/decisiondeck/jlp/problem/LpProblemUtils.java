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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import org.decisiondeck.jlp.LpConstraint;
import org.decisiondeck.jlp.LpOperator;
import org.decisiondeck.jlp.utils.LpSolverUtils.ToString;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * Utilities methods (e.g. views, copies) related to a {@link LpProblem}.
 * 
 * @author Olivier Cailloux
 * 
 */
public class LpProblemUtils {
    /**
     * Retrieves a long description, with line breaks, of the given problem.
     * 
     * @param <T>
     *            the type of the variables in the problem.
     * @param problem
     *            not <code>null</code>.
     * @return not <code>null</code>, not empty.
     */
    static public <T> String getAsLongString(LpProblem<T> problem) {
	Preconditions.checkNotNull(problem);
	String N = System.getProperty("line.separator");
	final String name = problem.getName().equals("") ? "" : " " + problem.getName();
	String s = "Problem" + name + N;

	if (!problem.getObjective().isEmpty()) {
	    s += problem.getObjective().getDirection() + N;
	    s += " " + problem.getObjective().getFunction() + N;
	} else {
	    s += "Find one solution" + N;
	}
	s += "Subject To" + N;
	for (LpConstraint<T> constraint : problem.getConstraints()) {
	    s += "\t" + constraint + N;
	}
	s += "Bounds" + N;
	for (T variable : problem.getVariables()) {
	    Number lb = problem.getVarLowerBound(variable);
	    Number ub = problem.getVarUpperBound(variable);

	    if (lb != null || ub != null) {
		s += "\t";
		if (lb != null) {
		    s += lb + " <= ";
		}
		s += variable;
		if (ub != null) {
		    s += " <= " + ub;
		}
		s += N;
	    }
	}

	s += "Variables" + N;
	for (T variable : problem.getVariables()) {
	    s += "\t" + variable + " " + problem.getVarType(variable) + N;
	}

	return s;

    }

    static public <T> LpProblem<T> getImmutableCopy(LpProblem<T> source) {
	return new LpProblemImmutable<T>(source);
    }

    static public <T> LpProblem<T> getReadView(LpProblem<T> delegate) {
	return new LpProblemReadView<T>(delegate);
    }

    /**
     * Retrieves all the variables in the given problem that have a given type.
     * 
     * @param <T>
     *            the class of the variables.
     * @param problem
     *            not <code>null</code>.
     * @param type
     *            not <code>null</code>.
     * @return not <code>null</code>.
     */
    static public <T> Set<T> getVariables(LpProblem<T> problem, LpVariableType type) {
	Preconditions.checkNotNull(problem);
	Preconditions.checkNotNull(type);
	final Set<T> searched = Sets.newHashSet();
	final Set<T> variables = problem.getVariables();
	for (T variable : variables) {
	    final LpVariableType varType = problem.getVarType(variable);
	    if (varType == type) {
		searched.add(variable);
	    }
	}
	return searched;
    }

    /**
     * <p>
     * Retrieves a view of the given problem that has its own name. The name of the underlying problem is ignored even
     * if the provided new name is empty.
     * </p>
     * <p>
     * This method is provided for efficiency reasons, as copying the problem to a new problem in order to change its
     * name may be long for big problems. The provided view is writable, writing to it delegates to the underlying
     * problem except that changing its name change only its internal name, not the name of the delegate problem.
     * </p>
     * 
     * @param <T>
     *            the type of the variables in the problem.
     * @param problem
     *            the underlying problem.
     * @param problemName
     *            the new problem name, <code>null</code> or empty for no name.
     * @return not <code>null</code>.
     */
    static public <T> LpProblem<T> getViewWithName(LpProblem<T> problem, String problemName) {
	final LpProblemOwnName<T> view = new LpProblemOwnName<T>(problem);
	view.setName(problemName);
	return view;
    }

    /**
     * <p>
     * Restricts the bounds set for the given variable in the given problem to make sure the variable satisfies the
     * given contraint. For example, if the operator is less or equal to and the value is 3, the variable will have its
     * upper bound set to three if the current upper bound set in the problem for that variable is greater than three.
     * </p>
     * <p>
     * Restricting the bounds with this method has the same effect on the set of admitted solutions than adding an
     * equivalent constraint. In the previous example, instead of restricting the bound, a constraint could have been
     * added specifying that the variable must be less or equal to three. Restricting the bounds instead of adding
     * constraints may be used in specific cases e.g. to enhance readability in case the bounds and the constraints have
     * different semantics.
     * </p>
     * <p>
     * The resulting bounds must define a non empty interval: it is not allowed, for example, to restrict the upper
     * bound to a lower value than the current lower bound. In such a case the problem is not modified and this method
     * throws an exception. Defining such contradictory constraints (and, thus, defining a problem with no satisfactory
     * solution) is permitted through addition of constraints but not through restriction of bounds.
     * </p>
     * 
     * @param <T>
     *            the type of the variables in the problem.
     * @param problem
     *            not <code>null</code>.
     * @param variable
     *            not <code>null</code>.
     * @param op
     *            not <code>null</code>.
     * @param value
     *            a real number.
     * @return <code>true</code> iff the problem has been modified.
     */
    static public <T> boolean restrictBounds(LpProblem<T> problem, T variable, LpOperator op, double value) {
	checkNotNull(problem);
	checkNotNull(variable);
	checkNotNull(op);
	checkArgument(!Double.isInfinite(value));
	checkArgument(!Double.isNaN(value));

	final Double newLower;
	final Double newUpper;
	switch (op) {
	case EQ:
	    newLower = Double.valueOf(value);
	    newUpper = Double.valueOf(value);
	    break;
	case GE:
	    newLower = Double.valueOf(value);
	    newUpper = null;
	    break;
	case LE:
	    newLower = null;
	    newUpper = Double.valueOf(value);
	    break;
	default:
	    throw new IllegalStateException("Unknown OP.");
	}

	final Number currentLower = problem.getVarLowerBound(variable);
	final Number currentUpper = problem.getVarUpperBound(variable);
	final Number effectiveLower;
	final Number effectiveUpper;
	if (newLower != null && (currentLower == null || newLower.doubleValue() > currentLower.doubleValue())) {
	    effectiveLower = newLower;
	} else {
	    effectiveLower = currentLower;
	}
	if (newUpper != null && (currentUpper == null || newUpper.doubleValue() < currentUpper.doubleValue())) {
	    effectiveUpper = newUpper;
	} else {
	    effectiveUpper = currentUpper;
	}
	if (newLower != null && effectiveUpper != null && newLower.doubleValue() > effectiveUpper.doubleValue()) {
	    throw new IllegalStateException("New lower bound of " + newLower + " contradicts upper bound of "
		    + effectiveUpper + ".");
	}
	if (newUpper != null && effectiveLower != null && newUpper.doubleValue() < effectiveLower.doubleValue()) {
	    throw new IllegalStateException("New upper bound of " + newUpper + " contradicts lower bound of "
		    + effectiveLower + ".");
	}
	return problem.setVarBounds(variable, effectiveLower, effectiveUpper);
    }

    /**
     * @param <T>
     *            the type of input.
     * @return a function that applies the {@link #toString()} method to its input. Does not accept <code>null</code>
     *         values as input.
     */
    static public <T> Function<T, String> getToStringFunction() {
	return new ToString<T>();
    }
}
