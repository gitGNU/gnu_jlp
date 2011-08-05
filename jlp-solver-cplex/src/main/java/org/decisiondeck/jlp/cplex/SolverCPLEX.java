/**
 * Copyright © 2010-2011 Olivier Cailloux
 *
 *     This file is part of JLP.
 *
 *     JLP is free software: you can redistribute it and/or modify it under the
 *     terms of the GNU Lesser General Public License version 3 as published by
 *     the Free Software Foundation.
 *
 *     JLP is distributed in the hope that it will be useful, but WITHOUT ANY
 * 	WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * 	FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * 	more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with JLP. If not, see <http://www.gnu.org/licenses/>.
 */
package org.decisiondeck.jlp.cplex;

import ilog.concert.IloException;
import ilog.concert.IloLPMatrix;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloLinearNumExprIterator;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.CplexStatus;
import ilog.cplex.IloCplex.DoubleParam;
import ilog.cplex.IloCplex.IntParam;
import ilog.cplex.IloCplex.ParallelMode;
import ilog.cplex.IloCplex.StringParam;

import java.util.Iterator;

import org.decisiondeck.jlp.AbstractLpSolver;
import org.decisiondeck.jlp.LpConstraint;
import org.decisiondeck.jlp.LpDirection;
import org.decisiondeck.jlp.LpFileFormat;
import org.decisiondeck.jlp.LpLinear;
import org.decisiondeck.jlp.LpResultStatus;
import org.decisiondeck.jlp.LpSolverException;
import org.decisiondeck.jlp.LpTerm;
import org.decisiondeck.jlp.parameters.LpDoubleParameter;
import org.decisiondeck.jlp.parameters.LpIntParameter;
import org.decisiondeck.jlp.parameters.LpStringParameter;
import org.decisiondeck.jlp.parameters.LpTimingType;
import org.decisiondeck.jlp.problem.LpVariableType;
import org.decisiondeck.jlp.solution.LpSolutionImpl;
import org.decisiondeck.jlp.utils.LpSolverUtils;
import org.decisiondeck.jlp.utils.TimingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableBiMap.Builder;

/**
 * The ILOG CPLEX solver.
 * 
 * @param <T>
 *            the class used for the variables.
 * 
 * @author Olivier Cailloux
 * 
 * 
 */
public class SolverCPLEX<T> extends AbstractLpSolver<T> {
    private static final int CPLEX_CLOCK_TYPE_CPU = 1;

    private static final int CPLEX_CLOCK_TYPE_WALL = 2;

    private static final Logger s_logger = LoggerFactory.getLogger(SolverCPLEX.class);

    private IloCplex m_cplex;

    private BiMap<T, IloNumVar> m_variablesToCplex;

    /**
     * Creates a new solver instance.
     */
    public SolverCPLEX() {
	m_cplex = null;
	m_variablesToCplex = null;
    }

    @Override
    public void close() throws LpSolverException {
	if (m_cplex != null) {
	    m_cplex.end();
	    m_cplex = null;
	}
    }

    private void exportModel(String file) throws LpSolverException {
	lazyInit();
	try {
	    m_cplex.exportModel(file);
	} catch (IloException exc) {
	    throw new LpSolverException(exc);
	} catch (RuntimeException exc2) {
	    logContent(m_cplex);
	    throw exc2;
	}
    }

    private IloLinearNumExpr getAsCplex(LpLinear<T> linear) throws IloException {
	IloLinearNumExpr lin = m_cplex.linearNumExpr();
	for (LpTerm<T> term : linear) {
	    lin.addTerm(term.getCoefficient(), m_variablesToCplex.get(term.getVariable()));
	}
	return lin;
    }

    @Override
    public LpFileFormat getPreferredFormat() throws LpSolverException {
	return null;
    }

    /**
     * Retrieves the result status equivalent to the given cplex status.
     * 
     * @param cplexStatus
     *            not <code>null</code>.
     * @param hasSolution
     *            <code>true</code> iff the given status comes with a solution.
     * @return not <code>null</code>.
     */
    public LpResultStatus getResultStatus(CplexStatus cplexStatus, boolean hasSolution) {
	Preconditions.checkNotNull(cplexStatus);
	final LpResultStatus resultStatus;
	if (cplexStatus == CplexStatus.InfOrUnbd) {
	    resultStatus = LpResultStatus.INFEASIBLE_OR_UNBOUNDED;
	} else if (cplexStatus == CplexStatus.AbortTimeLim) {
	    if (hasSolution) {
		resultStatus = LpResultStatus.TIME_LIMIT_REACHED_WITH_SOLUTION;
	    } else {
		resultStatus = LpResultStatus.TIME_LIMIT_REACHED_NO_SOLUTION;
	    }
	} else if (cplexStatus == CplexStatus.Infeasible) {
	    resultStatus = LpResultStatus.INFEASIBLE;
	} else if (cplexStatus == CplexStatus.Optimal) {
	    /**
	     * NB cplex possibly returns optimal (and an objective value of zero) when no objective function is defined!
	     */
	    if (getProblem().getObjective().isComplete()) {
		resultStatus = LpResultStatus.OPTIMAL;
	    } else {
		resultStatus = LpResultStatus.FEASIBLE;
	    }
	} else if (cplexStatus == CplexStatus.OptimalTol) {
	    if (getProblem().getObjective().isComplete()) {
		resultStatus = LpResultStatus.OPTIMAL;
	    } else {
		resultStatus = LpResultStatus.FEASIBLE;
	    }
	} else if (cplexStatus == CplexStatus.MemLimInfeas) {
	    resultStatus = LpResultStatus.MEMORY_LIMIT_REACHED_WITHOUT_SOLUTION;
	} else if (cplexStatus == CplexStatus.MemLimFeas) {
	    resultStatus = LpResultStatus.MEMORY_LIMIT_REACHED_WITH_SOLUTION;
	} else {
	    if (hasSolution) {
		resultStatus = LpResultStatus.ERROR_WITH_SOLUTION;
	    } else {
		resultStatus = LpResultStatus.ERROR_NO_SOLUTION;
	    }
	}
	return resultStatus;
    }

    @Override
    public Object getUnderlyingSolver() throws LpSolverException {
	lazyInit();
	return m_cplex;
    }

    private void lazyInit() throws LpSolverException {
	if (m_cplex != null) {
	    return;
	}
	Preconditions.checkState(getProblem().getObjective().isEmpty() || getProblem().getObjective().isComplete(),
		"Objective function set without a direction (or inverse).");

	try {
	    m_cplex = new IloCplex();

	    setParameters(m_cplex);

	    m_cplex.setName(getProblem().getName());
	    s_logger.info("Building problem {}.", getProblem().getName());

	    setVariables();

	    for (LpConstraint<T> constraint : getProblem().getConstraints()) {
		LpLinear<T> linear = constraint.getLhs();
		final IloLinearNumExpr lin = getAsCplex(linear);

		final double rhs = constraint.getRhs();

		switch (constraint.getOperator()) {
		case EQ:
		    m_cplex.addEq(lin, rhs, constraint.getName());
		    break;
		case GE:
		    m_cplex.addGe(lin, rhs, constraint.getName());
		    break;
		case LE:
		    m_cplex.addLe(lin, rhs, constraint.getName());
		    break;
		}
		s_logger.debug("Set constraint {}.", constraint);
	    }

	    if (getProblem().getObjective().isComplete()) {
		final LpLinear<T> objective = getProblem().getObjective().getFunction();
		final IloLinearNumExpr lin = getAsCplex(objective);

		final LpDirection direction = getProblem().getObjective().getDirection();
		switch (direction) {
		case MAX:
		    m_cplex.addMaximize(lin);
		    break;
		case MIN:
		    m_cplex.addMinimize(lin);
		    break;
		}
	    }
	} catch (IloException exc) {
	    close();
	    throw new LpSolverException(exc);
	} catch (LpSolverException exc) {
	    close();
	    throw exc;
	}
    }

    /**
     * A method useful for debug which logs everly information that can be found in the given solver instance.
     * 
     * @param cplex
     *            not <code>null</code>.
     */
    public void logContent(IloCplex cplex) {
	Preconditions.checkNotNull(cplex);
	try {
	    for (Iterator<?> iterator = cplex.iterator(); iterator.hasNext();) {
		final Object obj = iterator.next();
		if (obj instanceof IloLPMatrix) {
		    IloLPMatrix mat = (IloLPMatrix) obj;
		    final IloNumVar[] numVars = mat.getNumVars();
		    for (final IloNumVar var : numVars) {
			final double value;
			try {
			    value = cplex.getValue(var);
			    s_logger.debug("Var {}, value " + value + ".", var);
			} catch (IloException exc) {
			    s_logger.debug("Var {}, value unknown.", var);
			}
		    }
		    final IloRange[] ranges = mat.getRanges();
		    for (final IloRange range : ranges) {
			final double value;
			try {
			    value = cplex.getValue(range.getExpr());
			    s_logger.debug("Range {}, value " + value + ".", range);
			} catch (IloException exc) {
			    s_logger.debug("Range {}, value unknown.", range);
			}
		    }
		} else if (obj instanceof IloRange) {
		    final IloRange range = (IloRange) obj;
		    final double value;
		    try {
			value = cplex.getValue(range.getExpr());
			s_logger.debug("Range {}, value " + value + ".", range);
		    } catch (IloException exc) {
			s_logger.debug("Range {}, value unknown.", range);
		    }
		    IloLinearNumExprIterator it2 = ((IloLinearNumExpr) range.getExpr()).linearIterator();
		    while (it2.hasNext()) {
			final IloNumVar numVar = it2.nextNumVar();
			final double varValue;
			try {
			    varValue = cplex.getValue(numVar);
			    s_logger.debug("Var {}, value " + varValue + ".", numVar);
			} catch (IloException exc) {
			    s_logger.debug("Var {}, value unknown.", numVar);
			}
			// final String name = numVar.getName();
		    }
		} else if (obj instanceof IloNumExpr) {
		    IloNumExpr expr = (IloNumExpr) obj;
		    // final double value = cplex.getValue(expr);
		    // s_logger.info("Expr {}, value " + value + ".", expr);
		    s_logger.debug("Expr {}.", expr);
		}
	    }
	} catch (Exception exc) {
	    s_logger.warn("Exception while trying to log content.", exc);
	}
    }

    private void setDefaultParameters(IloCplex cplex) throws LpSolverException {
	try {
	    final int writeNodesToDisk = 2;
	    cplex.setParam(IntParam.NodeFileInd, writeNodesToDisk);
	} catch (IloException exc) {
	    throw new LpSolverException(exc);
	}
    }

    /**
     * Sets the parameter and transform a possible {@link IloException} into an {@link LpSolverException}.
     * 
     * @param cplex
     *            not <code>null</code>.
     * @param param
     *            not <code>null</code>.
     * @param value
     *            the value.
     * @throws LpSolverException
     *             if an {@link IloException} occurs while setting the parameter.
     */
    public void setParam(IloCplex cplex, final DoubleParam param, final double value) throws LpSolverException {
	Preconditions.checkNotNull(cplex);
	Preconditions.checkNotNull(param);
	try {
	    cplex.setParam(param, value);
	} catch (IloException exc) {
	    throw new LpSolverException(exc);
	}
    }

    /**
     * Sets the parameter and transform a possible {@link IloException} into an {@link LpSolverException}.
     * 
     * @param cplex
     *            not <code>null</code>.
     * @param param
     *            not <code>null</code>.
     * @param value
     *            the value.
     * @throws LpSolverException
     *             if an {@link IloException} occurs while setting the parameter.
     */
    public void setParam(IloCplex cplex, final IntParam param, final int value) throws LpSolverException {
	Preconditions.checkNotNull(cplex);
	Preconditions.checkNotNull(param);
	try {
	    cplex.setParam(param, value);
	} catch (IloException exc) {
	    throw new LpSolverException(exc);
	}
    }

    /**
     * Sets the parameter and transform a possible {@link IloException} into an {@link LpSolverException}.
     * 
     * @param cplex
     *            not <code>null</code>.
     * @param param
     *            not <code>null</code>.
     * @param value
     *            the value.
     * @throws LpSolverException
     *             if an {@link IloException} occurs while setting the parameter.
     */
    public void setParam(IloCplex cplex, final StringParam param, final String value) throws LpSolverException {
	Preconditions.checkNotNull(cplex);
	Preconditions.checkNotNull(param);
	try {
	    cplex.setParam(param, value);
	} catch (IloException exc) {
	    throw new LpSolverException(exc);
	}
    }

    /**
     * Initializes the parameters, including logging parameters, of the given solver instance to appropriate values
     * considering the parameters set in this object, or to default values.
     * 
     * @param cplex
     *            not <code>null</code>.
     * @throws LpSolverException
     *             if an exception occurs while setting the parameters, or if some parameters have values that are
     *             impossible to satisfy (e.g. if both cpu and wall timings are set).
     */
    public void setParameters(IloCplex cplex) throws LpSolverException {
	setDefaultParameters(cplex);

	final LpTimingType timingType = getPreferredTimingType();

	final int clockType;
	switch (timingType) {
	case WALL_TIMING:
	    clockType = CPLEX_CLOCK_TYPE_WALL;
	    break;
	case CPU_TIMING:
	    clockType = CPLEX_CLOCK_TYPE_CPU;
	    break;
	default:
	    throw new IllegalStateException("Unknown timing type.");
	}
	setParam(cplex, IntParam.ClockType, clockType);

	final Double timeLimit_s = getTimeLimit(timingType);
	if (timeLimit_s != null) {
	    setParam(cplex, DoubleParam.TiLim, timeLimit_s.doubleValue());
	}

	final Integer maxThreads = getParameter(LpIntParameter.MAX_THREADS);
	final int maxThreadsValue;
	if (maxThreads == null) {
	    maxThreadsValue = 0;
	} else {
	    maxThreadsValue = maxThreads.intValue();
	}
	setParam(cplex, IntParam.Threads, maxThreadsValue);

	if (getParameter(LpDoubleParameter.MAX_TREE_SIZE_MB) != null) {
	    setParam(cplex, DoubleParam.TreLim, getParameter(LpDoubleParameter.MAX_TREE_SIZE_MB).doubleValue());
	}

	if (getParameter(LpStringParameter.WORK_DIR) != null) {
	    setParam(cplex, StringParam.WorkDir, getParameter(LpStringParameter.WORK_DIR));
	}

	final int mode;
	if (getParameter(LpIntParameter.DETERMINISTIC).intValue() == 0) {
	    mode = ParallelMode.Opportunistic;
	} else {
	    mode = ParallelMode.Deterministic;
	}
	setParam(cplex, IntParam.ParallelMode, mode);

	m_cplex.setOut(new CplexLogger(CplexLogger.OutLevel.INFO));
	m_cplex.setWarning(new CplexLogger(CplexLogger.OutLevel.WARNING));
    }

    private void setVariables() throws IloException {
	final Builder<T, IloNumVar> variablesToCplexBuilder = ImmutableBiMap.builder();
	for (T variable : getProblem().getVariables()) {
	    final String varName = getVarName(variable);
	    LpVariableType varType = getProblem().getVarType(variable);
	    Number lowerBound = LpSolverUtils.getVarLowerBoundBounded(getProblem(), variable);
	    Number upperBound = LpSolverUtils.getVarUpperBoundBounded(getProblem(), variable);

	    final IloNumVar num;
	    final double lb = (lowerBound != null ? lowerBound.doubleValue() : Double.NEGATIVE_INFINITY);
	    final double ub = (upperBound != null ? upperBound.doubleValue() : Double.POSITIVE_INFINITY);
	    final IloNumVarType type;
	    switch (varType) {
	    case BOOL:
		type = IloNumVarType.Bool;
		break;
	    case INT:
		type = IloNumVarType.Int;
		break;
	    case REAL:
		type = IloNumVarType.Float;
		break;
	    default:
		throw new IllegalStateException("Unexpected type.");
	    }
	    num = m_cplex.numVar(lb, ub, type, varName);
	    m_cplex.add(num);
	    s_logger.debug("Set variable {} with bounds " + lb + ", " + ub + ", type " + type + ", name " + varName
		    + ".", variable);

	    variablesToCplexBuilder.put(variable, num);
	}
	m_variablesToCplex = variablesToCplexBuilder.build();
	// m_cplex.iterator();
	// m_cplex.LPMatrixIterator();
    }

    @Override
    protected LpResultStatus solveUnderlying() throws LpSolverException {
	lazyInit();

	try {
	    final LpTimingType timingType = getPreferredTimingType();
	    final TimingHelper timingHelper = new TimingHelper();
	    timingHelper.setSolverStart_ms(timingType, m_cplex.getCplexTime() * 1000);
	    timingHelper.start();
	    final boolean solved = m_cplex.solve();
	    timingHelper.stop();
	    timingHelper.setSolverEnd_ms(timingType, m_cplex.getCplexTime() * 1000);

	    CplexStatus cplexStatus = m_cplex.getCplexStatus();
	    final LpResultStatus resultStatus = getResultStatus(cplexStatus, solved);
	    if (resultStatus.isFeasible()) {
		final LpSolutionImpl<T> solution = new LpSolutionImpl<T>(getProblem());

		for (IloNumVar num : m_variablesToCplex.inverse().keySet()) {
		    final T variable = m_variablesToCplex.inverse().get(num);

		    s_logger.debug("Querying value of {}.", num);
		    final double value = m_cplex.getValue(num);
		    s_logger.debug("Value is " + value + ".", num);

		    final double correctedValue = getCorrectedValue(variable, value);
		    solution.putValue(variable, Double.valueOf(correctedValue));
		}
		if (resultStatus == LpResultStatus.OPTIMAL) {
		    solution.setObjectiveValue(Double.valueOf(m_cplex.getObjValue()));
		}
		setSolution(solution);
	    }
	    m_lastDuration = timingHelper.getDuration();

	    return resultStatus;
	} catch (IloException e) {
	    throw new LpSolverException(e);
	} finally {
	    close();
	}
    }

    @Override
    public void writeProblem(LpFileFormat format, String file, boolean addExtension) throws LpSolverException {
	if (!addExtension) {
	    throw new LpSolverException("Not supported without ext.");
	}
	switch (format) {
	case CPLEX_LP:
	    exportModel(file + ".lp");
	    break;
	case MPS:
	    exportModel(file + ".mps");
	    break;
	case SOLVER_PREFERRED:
	    exportModel(file + ".sav");
	    break;
	}
    }

}
