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
package org.decisiondeck.jlp;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import org.decisiondeck.jlp.parameters.LpDoubleParameter;
import org.decisiondeck.jlp.parameters.LpIntParameter;
import org.decisiondeck.jlp.parameters.LpObjectParameter;
import org.decisiondeck.jlp.parameters.LpParameters;
import org.decisiondeck.jlp.parameters.LpParametersImpl;
import org.decisiondeck.jlp.parameters.LpParametersUtils;
import org.decisiondeck.jlp.parameters.LpStringParameter;
import org.decisiondeck.jlp.parameters.LpTimingType;
import org.decisiondeck.jlp.problem.LpProblem;
import org.decisiondeck.jlp.problem.LpProblemImpl;
import org.decisiondeck.jlp.solution.LpSolution;
import org.decisiondeck.jlp.solution.LpSolutionImmutable;
import org.decisiondeck.jlp.solution.LpSolverDuration;
import org.decisiondeck.jlp.utils.LpSolverUtils;
import org.decisiondeck.jlp.utils.TimingHelper;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

/**
 * <p>
 * A helper class that can be used as a basis to implement a solver.
 * </p>
 * <p>
 * The following receipt for implementing a new solver type may be followed.
 * <ul>
 * <li>Implement lazy init: init the underlying solver store it in a local field. Init the parameters. Declare the
 * variables, add the constraints.
 * <li>Implement {@link #getUnderlyingSolver()}: lazy init the underlying solver and return it.</li>
 * <li>Implement {@link #solve()}: lazy init the underlying solver (if necessary), then solve using the
 * {@link TimingHelper}. Store the result status. If feasible, build a feasible solution. Set the solution and set the
 * last duration. Call {@link #close()}.</li>
 * </ul>
 * This permits an external user to get the underlying solver, modify it a bit manually, then call solve.
 * </p>
 * 
 * @param <T>
 *            the class used for the variables.
 * 
 * @author Olivier Cailloux
 * 
 */
public abstract class AbstractLpSolver<T> implements LpSolver<T> {
    private Double m_autoCorrectThreshold;
    protected LpSolverDuration m_lastDuration;

    private LpResultStatus m_lastResultStatus;

    private LpSolution<T> m_lastSolution;

    private final LpParameters m_parameters;

    private final LpProblem<T> m_problem;

    final private TimingHelper m_timingHelper;

    public AbstractLpSolver() {
	m_problem = new LpProblemImpl<T>();
	m_parameters = new LpParametersImpl();
	m_timingHelper = new TimingHelper();
	m_lastSolution = null;
	m_lastDuration = null;
	m_lastResultStatus = null;
	m_autoCorrectThreshold = null;
    }

    /**
     * Retrieves the maximum difference between a bound and an observed value, after a solve, that will be automatically
     * corrected by this solver. The default is <code>null</code>.
     * 
     * @return <code>null</code> if this solver does not correct the obtained solutions.
     */
    @Override
    public Double getAutoCorrectThreshold() {
	return m_autoCorrectThreshold;
    }

    /**
     * Computes and returns the corrected solution value, if the given solution value is not inside the bounds defined
     * for the given variable. If no bounds are defined or the solution value is in the defined bounds or is outside the
     * bounds by more than the allowed correction threshold, this method returns the given value. The returned value is
     * the given solution value corrected by at most {@link #getAutoCorrectThreshold()}. The returned value is
     * <em>not necessarily</em> inside the defined bounds.
     * 
     * @param variable
     *            not <code>null</code>.
     * @param solutionValue
     *            a real value.
     * @return the solution value, possibly corrected.
     */
    public double getCorrectedValue(T variable, double solutionValue) {
	checkNotNull(variable);
	if (getAutoCorrectThreshold() == null) {
	    return solutionValue;
	}

	final Number lowerBound = getProblem().getVarLowerBound(variable);
	final Number upperBound = getProblem().getVarUpperBound(variable);
	if (lowerBound != null) {
	    final double outsideBound = lowerBound.doubleValue() - solutionValue;
	    if (outsideBound > 0 && outsideBound <= getAutoCorrectThreshold().doubleValue()) {
		return lowerBound.doubleValue();
	    }
	}
	if (upperBound != null) {
	    final double outsideBound = solutionValue - upperBound.doubleValue();
	    if (outsideBound > 0 && outsideBound <= getAutoCorrectThreshold().doubleValue()) {
		return upperBound.doubleValue();
	    }
	}
	return solutionValue;
    }

    @Override
    public LpSolverDuration getDuration() {
	return m_lastDuration;
    }

    /**
     * Retrieves the value associated with the given parameter. If the value has not been set, returns the default value
     * for that parameter.
     * 
     * @param parameter
     *            not <code>null</code>.
     * @return a meaningful value for that parameter, possibly <code>null</code> as this is a meaningful value for some
     *         parameters.
     */
    public Double getParameter(LpDoubleParameter parameter) {
	return m_parameters.getValue(parameter);
    }

    /**
     * Retrieves the value associated with the given parameter. If the value has not been set, returns the default value
     * for that parameter.
     * 
     * @param parameter
     *            not <code>null</code>.
     * @return a meaningful value for that parameter, possibly <code>null</code> as this is a meaningful value for some
     *         parameters.
     */
    public Integer getParameter(LpIntParameter parameter) {
	return m_parameters.getValue(parameter);
    }

    /**
     * Retrieves the value associated with the given parameter. If the value has not been set, returns the default value
     * for that parameter.
     * 
     * @param parameter
     *            not <code>null</code>.
     * @return a meaningful value for that parameter, possibly <code>null</code> as this is a meaningful value for some
     *         parameters.
     */
    public String getParameter(LpStringParameter parameter) {
	return m_parameters.getValue(parameter);
    }

    @Override
    public LpParameters getParametersView() {
	return m_parameters;
    }

    /**
     * Retrieves the preferred timing type according to the parameters values set in this object. If both the max wall
     * time and max cpu time parameters are set, an exception is thrown. If the max cpu time parameter is set but cpu
     * timing is not supported by the Java virtual machine, an exception is thrown. Otherwise, this method returns the
     * timing type for which a time limit has been set as a parameter, or if none has been set, returns cpu timing if it
     * is supported and wall timing otherwise.
     * 
     * @return not <code>null</code>.
     * @throws LpSolverException
     *             if both cpu and wall time limit parameters have a value; or if cpu time limit parameter is set but
     *             cpu timing is not supported by the Java virtual machine.
     */
    public LpTimingType getPreferredTimingType() throws LpSolverException {
	final boolean hasMaxWall = getParameter(LpDoubleParameter.MAX_WALL_SECONDS) != null;
	final boolean hasMaxCpu = getParameter(LpDoubleParameter.MAX_CPU_SECONDS) != null;
	if (hasMaxCpu && hasMaxWall) {
	    throw new LpSolverException("Can't have both CPU time limit and Wall time limit.");
	}
	final LpTimingType timingType;
	if (hasMaxWall) {
	    timingType = LpTimingType.WALL_TIMING;
	} else if (hasMaxCpu) {
	    if (!m_timingHelper.isCpuTimingSupported()) {
		throw new LpSolverException("Cpu timing not supported but max cpu time is set.");
	    }
	    timingType = LpTimingType.CPU_TIMING;
	} else {
	    if (m_timingHelper.isCpuTimingSupported()) {
		timingType = LpTimingType.CPU_TIMING;
	    } else {
		timingType = LpTimingType.WALL_TIMING;
	    }
	}
	return timingType;
    }

    @Override
    public LpProblem<T> getProblem() {
	return m_problem;
    }

    @Override
    public LpResultStatus getResultStatus() {
	return m_lastResultStatus;
    }

    @Override
    public LpSolution<T> getSolution() {
	return m_lastSolution;
    }

    public Double getTimeLimit(LpTimingType timingType) {
	switch (timingType) {
	case WALL_TIMING:
	    return m_parameters.getValue(LpDoubleParameter.MAX_WALL_SECONDS);
	case CPU_TIMING:
	    return m_parameters.getValue(LpDoubleParameter.MAX_CPU_SECONDS);
	default:
	    throw new IllegalStateException("Unknown timing type.");
	}
    }

    /**
     * Retrieves the name that should be used for a constraint according to the specified export format. This method
     * uses the appropriate naming function if it is set.
     * 
     * @param constraint
     *            not <code>null</code>.
     * @param format
     *            not <code>null</code>.
     * @return not <code>null</code>, empty string for no name.
     */
    public String getConstraintName(LpConstraint<T> constraint, LpFileFormat format) {
	Preconditions.checkNotNull(constraint);
	Preconditions.checkNotNull(format);

	final Map<?, ?> namers = (Map<?, ?>) getParameter(LpObjectParameter.NAMER_CONSTRAINTS_BY_FORMAT);
	if (namers == null || !namers.containsKey(format)) {
	    return getConstraintName(constraint);
	}
	final Object namerObj = namers.get(format);
	if (!(namerObj instanceof Function)) {
	    throw new ClassCastException("Illegal constraint namer '" + namerObj + "', namers should be functions.");
	}
	final Function<?, ?> namer = (Function<?, ?>) namerObj;

	return getConstraintName(constraint, namer);
    }

    private String getConstraintName(LpConstraint<T> constraint, Function<?, ?> namer) {
	@SuppressWarnings("unchecked")
	final Function<LpConstraint<T>, ?> namerTyped = (Function<LpConstraint<T>, ?>) namer;
	final Object named = namerTyped.apply(constraint);
	if (named == null) {
	    return "";
	}
	if (!(named instanceof String)) {
	    throw new ClassCastException("Illegal constraint name '" + named
		    + "', namer should only return strings or nulls.");
	}
	final String name = (String) named;
	return name;
    }

    /**
     * Sets the maximum difference between a bound and an observed value, after a solve, that will be automatically
     * corrected by this solver.
     * 
     * @param autoCorrectThreshold
     *            <code>null</code> if this solver should not correct the obtained solutions.
     */
    @Override
    public void setAutoCorrectThreshold(Double autoCorrectThreshold) {
	m_autoCorrectThreshold = autoCorrectThreshold;
    }

    @Override
    public boolean setParameters(LpParameters parameters) {
	Preconditions.checkNotNull(parameters);
	if (parameters.equals(m_parameters)) {
	    return false;
	}
	LpParametersUtils.removeAllValues(m_parameters);
	LpParametersUtils.setAllValues(m_parameters, parameters);
	return true;
    }

    @Override
    public boolean setProblem(LpProblem<T> problem) {
	Preconditions.checkNotNull(problem);
	return LpSolverUtils.copyProblemTo(problem, m_problem);
    }

    /**
     * Sets the solution as a defensive copy of the given solution.
     * 
     * @param solution
     *            not <code>null</code>.
     */
    protected void setSolution(LpSolution<T> solution) {
	Preconditions.checkNotNull(solution);
	m_lastSolution = new LpSolutionImmutable<T>(solution);
    }

    @Override
    public LpResultStatus solve() throws LpSolverException {
	Preconditions.checkState(getProblem().getObjective().isEmpty() || getProblem().getObjective().isComplete(),
		"Problem must have an objective function iff it has an objective direction.");
	m_lastResultStatus = solveUnderlying();
	return m_lastResultStatus;
    }

    abstract protected LpResultStatus solveUnderlying() throws LpSolverException;

    /**
     * Retrieves the value associated with the given parameter. If the value has not been set, returns the default value
     * for that parameter.
     * 
     * @param parameter
     *            not <code>null</code>.
     * @return a meaningful value for that parameter, possibly <code>null</code> as this is a meaningful value for some
     *         parameters.
     */
    public Object getParameter(LpObjectParameter parameter) {
	return m_parameters.getValue(parameter);
    }

    /**
     * Retrieves the name that should be used for a variable. This method uses the naming function if it is set.
     * 
     * @param variable
     *            not <code>null</code>.
     * @return not <code>null</code>, empty string for no name.
     */
    public String getVariableName(T variable) {
	Preconditions.checkNotNull(variable);

	final Function<?, ?> namer = (Function<?, ?>) getParameter(LpObjectParameter.NAMER_VARIABLES);
	if (namer == null) {
	    return getProblem().getVarNameComputed(variable);
	}

	return getVariableName(variable, namer);
    }

    /**
     * Retrieves the name of the constraint, using the appropriate namer function if it is set.
     * 
     * @param constraint
     *            not <code>null</code>.
     * 
     * @return never <code>null</code>, empty if no id is set.
     */
    public String getConstraintName(LpConstraint<T> constraint) {
	Preconditions.checkNotNull(constraint);

	final Function<?, ?> namer = (Function<?, ?>) getParameter(LpObjectParameter.NAMER_CONSTRAINTS);
	final Function<?, ?> realNamer = namer == null ? getProblem().getConstraintsNamer() : namer;

	return getConstraintName(constraint, realNamer);
    }

    private String getVariableName(T variable, Function<?, ?> namer) {
	@SuppressWarnings("unchecked")
	final Function<T, ?> namerTyped = (Function<T, ?>) namer;
	final Object named = namerTyped.apply(variable);
	if (named == null) {
	    return "";
	}
	if (!(named instanceof String)) {
	    throw new ClassCastException("Illegal variable name '" + named
		    + "', namer should only return strings or nulls.");
	}
	final String name = (String) named;
	return name;
    }

    /**
     * Retrieves the name that should be used for a variable according to the specified export format. This method uses
     * the appropriate naming function if it is set.
     * 
     * @param variable
     *            not <code>null</code>.
     * @param format
     *            not <code>null</code>.
     * @return not <code>null</code>, empty string for no name.
     */
    public String getVariableName(T variable, LpFileFormat format) {
	Preconditions.checkNotNull(variable);
	Preconditions.checkNotNull(format);

	final Map<?, ?> namers = (Map<?, ?>) getParameter(LpObjectParameter.NAMER_VARIABLES_BY_FORMAT);
	if (namers == null || !namers.containsKey(format)) {
	    return getVariableName(variable);
	}
	final Object namerObj = namers.get(format);
	if (!(namerObj instanceof Function)) {
	    throw new ClassCastException("Illegal variable namer '" + namerObj + "', namers should be functions.");
	}
	final Function<?, ?> namer = (Function<?, ?>) namerObj;

	return getVariableName(variable, namer);
    }

}
