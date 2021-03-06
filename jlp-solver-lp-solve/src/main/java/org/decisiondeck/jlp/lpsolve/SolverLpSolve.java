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
package org.decisiondeck.jlp.lpsolve;

import java.util.HashMap;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

import org.decisiondeck.jlp.AbstractLpSolver;
import org.decisiondeck.jlp.LpConstraint;
import org.decisiondeck.jlp.LpDirection;
import org.decisiondeck.jlp.LpFileFormat;
import org.decisiondeck.jlp.LpLinear;
import org.decisiondeck.jlp.LpResultStatus;
import org.decisiondeck.jlp.LpSolverException;
import org.decisiondeck.jlp.LpTerm;
import org.decisiondeck.jlp.parameters.LpDoubleParameter;
import org.decisiondeck.jlp.parameters.LpTimingType;
import org.decisiondeck.jlp.problem.LpVariableType;
import org.decisiondeck.jlp.solution.LpSolutionImpl;
import org.decisiondeck.jlp.utils.LpSolverUtils;
import org.decisiondeck.jlp.utils.TimingHelper;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.Maps;

/**
 * The lp_solve solver.</p>
 * 
 * @param <T>
 *            the class used for the variables.
 * 
 * @author Olivier Cailloux
 * 
 */
public class SolverLpSolve<T> extends AbstractLpSolver<T> {
    private LpSolve m_lp;
    private BiMap<T, Integer> m_variablesIds;

    /**
     * Creates a new solver instance.
     */
    public SolverLpSolve() {
	m_lp = null;
	m_variablesIds = null;
    }

    @Override
    public void close() throws LpSolverException {
	if (m_lp != null) {
	    m_lp.deleteLp();
	}
	m_lp = null;
    }

    private void getAsArrays(LpLinear<T> linear, int[] var, double[] coeffs) {
	int i = 0;
	for (LpTerm<T> term : linear) {
	    var[i] = m_variablesIds.get(term.getVariable()).intValue();
	    coeffs[i] = term.getCoefficient();
	    i++;
	}
    }

    @Override
    public LpFileFormat getPreferredFormat() throws LpSolverException {
	return null;
    }

    private LpResultStatus getStatus(int ret) throws LpSolverException {
	// public static final int UNKNOWNERROR = -5;
	// public static final int DATAIGNORED = -4;
	// public static final int NOBFP = -3;
	// public static final int NOMEMORY = -2;
	// public static final int NOTRUN = -1;
	// public static final int OPTIMAL = 0;
	// public static final int SUBOPTIMAL = 1;
	// public static final int INFEASIBLE = 2;
	// public static final int UNBOUNDED = 3;
	// public static final int DEGENERATE = 4;
	// public static final int NUMFAILURE = 5;
	// public static final int USERABORT = 6;
	// public static final int TIMEOUT = 7;
	// public static final int RUNNING = 8;
	// public static final int PRESOLVED = 9;
	//
	// /* Branch & Bound and Lagrangean extra status values */
	// public static final int PROCFAIL = 10;
	// public static final int PROCBREAK = 11;
	// public static final int FEASFOUND = 12;
	// public static final int NOFEASFOUND = 13;
	switch (ret) {
	case -2:
	    /** TODO Find how to know if a solution has been reached. */
	    // return ResultStatus.MEMORY_LIMIT_REACHED_NO_SOLUTION;
	    throw new LpSolverException("Unsupported return status: " + ret + ".");
	case LpSolve.NOTRUN:
	    /**
	     * Observed that this results is obtained when an empty problem (no constraint, no variable) is solved.
	     * Should check that no other condition exist...
	     */
	    return LpResultStatus.FEASIBLE;
	case LpSolve.OPTIMAL:
	    /** Note that LP SOLVE returns optimal even when no objective function is defined. */
	    if (getProblem().getObjective().isEmpty()) {
		return LpResultStatus.FEASIBLE;
	    }
	    return LpResultStatus.OPTIMAL;
	case 1:
	    // SUBOPTIMAL (1) The model is sub-optimal. Only happens if there are integer variables and there is
	    // already
	    // an integer solution found. The solution is not guaranteed the most optimal one.
	    //
	    // * A timeout occured (set via set_timeout or with the -timeout option in lp_solve)
	    // * set_break_at_first was called so that the first found integer solution is found (-f option in
	    // lp_solve)
	    // * set_break_at_value was called so that when integer solution is found that is better than the
	    // specified
	    // value that it stops (-o option in lp_solve)
	    // * set_mip_gap was called (-g/-ga/-gr options in lp_solve) to specify a MIP gap
	    // * An abort function is installed (put_abortfunc) and this function returned TRUE
	    // * At some point not enough memory could not be allocated
	    return LpResultStatus.FEASIBLE;
	case 2:
	    return LpResultStatus.INFEASIBLE;
	case 3:
	    return LpResultStatus.UNBOUNDED;
	case 4:
	case 5:
	case 6:
	    // DEGENERATE (4) The model is degenerative
	    // NUMFAILURE (5) Numerical failure encountered
	    // USERABORT (6) The abort routine returned TRUE. See put_abortfunc
	    return LpResultStatus.ERROR_NO_SOLUTION;
	case 7:
	    // TIMEOUT (7) TODO A timeout occurred. A timeout was set via set_timeout
	    throw new LpSolverException("Unsupported return status: " + ret + ".");
	    // return ResultStatus.TIME_LIMIT_REACHED;
	case 10:
	case 11:
	    // PROCFAIL (10) The B&B routine failed
	    // PROCBREAK (11) The B&B was stopped because of a break-at-first (see set_break_at_first) or a
	    // break-at-value (see set_break_at_value)
	    return LpResultStatus.ERROR_NO_SOLUTION;
	case 12:
	    // FEASFOUND (12) A feasible B&B solution was found
	    return LpResultStatus.FEASIBLE;
	case 13:
	    // NOFEASFOUND (13) No feasible B&B solution found
	    return LpResultStatus.INFEASIBLE;
	default:
	    /** 8, 9 should not happen (undocumented). */
	    throw new IllegalStateException();
	}
    }

    @Override
    public Object getUnderlyingSolver() throws LpSolverException {
	lazyInit();
	return m_lp;
    }

    private void lazyInit() throws LpSolverException {
	if (m_lp != null) {
	    return;
	}
	Preconditions.checkState(getProblem().getObjective().isEmpty() || getProblem().getObjective().isComplete(),
		"Objective function set without a direction (or inverse).");

	m_variablesIds = LpSolverUtils.getVariablesIds(getProblem(), 1);

	try {
	    m_lp = LpSolve.makeLp(0, getProblem().getVariables().size());

	    setParameters();

	    if (!getProblem().getName().isEmpty()) {
		m_lp.setLpName(getProblem().getName());
	    }

	    m_lp.setAddRowmode(true);

	    for (LpConstraint<T> constraint : getProblem().getConstraints()) {
		int size = constraint.getLhs().size();

		int[] var = new int[size];
		double[] coeffs = new double[size];
		getAsArrays(constraint.getLhs(), var, coeffs);

		int operator;
		switch (constraint.getOperator()) {
		case LE:
		    operator = LpSolve.LE;
		    break;
		case GE:
		    operator = LpSolve.GE;
		    break;
		case EQ:
		    operator = LpSolve.EQ;
		    break;
		default:
		    throw new IllegalStateException("Unknown op.");
		}

		double rhs = constraint.getRhs();

		m_lp.addConstraintex(size, coeffs, var, operator, rhs);
	    }

	    m_lp.setAddRowmode(false);

	    for (T variable : getProblem().getVariables()) {
		int index = m_variablesIds.get(variable).intValue();

		final LpVariableType varType = getProblem().getVarType(variable);
		final Number lowerBound = LpSolverUtils.getVarLowerBoundBounded(getProblem(), variable);
		final Number upperBound = LpSolverUtils.getVarUpperBoundBounded(getProblem(), variable);

		m_lp.setInt(index, varType.isInt());

		if (lowerBound != null) {
		    m_lp.setLowbo(index, lowerBound.doubleValue());
		}
		if (upperBound != null) {
		    m_lp.setUpbo(index, upperBound.doubleValue());
		}
	    }

	    LpLinear<T> objective = getProblem().getObjective().getFunction();
	    if (objective != null) {

		int size = objective.size();
		int[] var = new int[size];
		double[] coeffs = new double[size];

		getAsArrays(objective, var, coeffs);

		m_lp.setObjFnex(size, coeffs, var);

		final LpDirection dir = getProblem().getObjective().getDirection();
		if (dir == LpDirection.MIN) {
		    m_lp.setMinim();
		} else {
		    m_lp.setMaxim();
		}
	    }
	} catch (LpSolveException e) {
	    close();
	    throw new LpSolverException(e);
	}
    }

    private void setParameters() throws LpSolverException {
	final HashMap<Enum<?>, Object> mandatory = Maps.newHashMap();
	mandatory.put(LpDoubleParameter.MAX_CPU_SECONDS, null);
	LpSolverUtils.assertConform(getParametersView(), mandatory);

	Double timeout = getParameter(LpDoubleParameter.MAX_WALL_SECONDS);
	if (timeout != null) {
	    long seconds = Math.round(timeout.doubleValue());
	    if (seconds == 0) {
		throw new LpSolverException("Illegal timeout value: " + timeout + ".");
	    }
	    m_lp.setTimeout(seconds);
	}

	/** REPORT defines, TODO treat log */
	// public static final int NEUTRAL = 0;
	// public static final int CRITICAL = 1;
	// public static final int SEVERE = 2;
	// public static final int IMPORTANT = 3;
	// public static final int NORMAL = 4;
	// public static final int DETAILED = 5;
	// public static final int FULL = 6;
	m_lp.setVerbose(4);

	// lp.setOutputfile("lpsolvetry.out");
	try {
	    m_lp.putLogfunc(new LpSolveLogger(), null);
	} catch (LpSolveException exc) {
	    throw new LpSolverException("Problem while setting the logger.", exc);
	}
    }

    @Override
    protected LpResultStatus solveUnderlying() throws LpSolverException {
	try {
	    lazyInit();

	    final TimingHelper timingHelper = new TimingHelper();
	    timingHelper.start();
	    int ret = m_lp.solve();
	    timingHelper.stop();
	    timingHelper.setSolverDuration_ms(LpTimingType.WALL_TIMING, m_lp.timeElapsed() * 1000);

	    final LpResultStatus status = getStatus(ret);

	    if (status.isFeasible()) {
		final LpSolutionImpl<T> solution = new LpSolutionImpl<T>(getProblem());

		final double[] values = new double[m_variablesIds.inverse().size()];
		m_lp.getVariables(values);
		if (status == LpResultStatus.OPTIMAL) {
		    solution.setObjectiveValue(Double.valueOf(m_lp.getObjective()));
		}

		for (Integer idInt : m_variablesIds.inverse().keySet()) {
		    final int id = idInt.intValue();
		    final T variable = m_variablesIds.inverse().get(idInt);

		    final double value = values[id - 1];
		    final double correctedValue = getCorrectedValue(variable, value);

		    solution.putValue(variable, Double.valueOf(correctedValue));
		}
		setSolution(solution);
	    }
	    m_lastDuration = timingHelper.getDuration();

	    return status;
	} catch (LpSolveException exc) {
	    throw new LpSolverException(exc);
	} finally {
	    close();
	}
    }

    @Override
    public void writeProblem(LpFileFormat format, String file, boolean addExtension) throws LpSolverException {
	try {
	    lazyInit();

	    final String ext;
	    switch (format) {
	    case SOLVER_PREFERRED:
		if (addExtension) {
		    ext = ".lp";
		} else {
		    ext = "";
		}
		m_lp.writeLp(file + ext);
		break;
	    case CPLEX_SAV:
	    case CPLEX_LP:
		throw new LpSolverException("Unsupported format: " + format + ".");
	    case MPS:
		if (addExtension) {
		    ext = ".mps";
		} else {
		    ext = "";
		}
		m_lp.writeMps(file + ext);
		break;
	    }
	} catch (LpSolveException exc) {
	    throw new LpSolverException(exc);
	} finally {
	    close();
	}
    }
}
