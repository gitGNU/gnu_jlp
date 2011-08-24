/**
 * Copyright © 2010-2011 Olivier Cailloux
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

import org.decisiondeck.jlp.parameters.LpParameters;
import org.decisiondeck.jlp.problem.LpProblem;
import org.decisiondeck.jlp.problem.LpVariableType;
import org.decisiondeck.jlp.solution.LpSolution;
import org.decisiondeck.jlp.solution.LpSolverDuration;
import org.decisiondeck.jlp.utils.LpSolverUtils;

import com.google.common.base.Function;

/**
 * <p>
 * A solver instance, representing a problem (with data, constraints), a set of parameters for solving the problem, and
 * possibly after having been run, a feasible solution. The solver abstracts several commercial and free solvers such as
 * lp_solve, CPLEX, etc., and thus relies on a solver implementation called the underlying solver.
 * </p>
 * <p>
 * The solver may be run several times. It remembers its bound problem and parameters between runs. Elements may be
 * changed in between runs.
 * </p>
 * <p>
 * The solver also offers a possiblity for the user to mess manually with the underlying solver, that can be used to
 * access advanced functionalities not offered by the wrapper (but please consider rather adding the functionality to
 * the wrapper itself if it makes sense, or introduce a bug report to that effect). However, this possibility must be
 * used with caution. After the underlying solver is retrieved by the user, this solver does not update its content any
 * more (to avoid erasing manually introduced changes). Hence, calling {@link #getUnderlyingSolver()} then modifying the
 * bound problem is forbidden. Although this is currently not enforced, the user <em>should not</em> modify this solver
 * state through the parameters or the problem views after a call to {@link #getUnderlyingSolver()}. The caller must not
 * forget to {@link #close()} the solver after use when using it manually, except that the methods {@link #solve()} and
 * {@link #writeProblem(LpFileFormat, String, boolean)} close the underlying solver after the job is done, thus it is
 * not necessary to call close again after these calls. After a call to {@link #solve()} or
 * {@link #writeProblem(LpFileFormat, String, boolean)}, the underlying solver pointer is not valid any more.
 * 
 * TODO make {@link #getUnderlyingSolver()} bring this object to an immutable state.
 * </p>
 * <p>
 * When a problem is transferred to the underlying solver by this solver, the variables having a type of
 * {@link LpVariableType#BOOL} are associated with 0 and 1 lower and upper bounds except if their associated bounds are
 * even further constrainted. E.g. a variable defined as BOOL with bounds of <0.5, 4> have its bounds modified to become
 * <0.5, 1> for the underlying solver. However this object will report the bounds as they are set in the problem, thus
 * with bounds of <0.5, 4>. Also note that the solver does not attempt to transform the input in a manner dependent on
 * the underlying solver. If, for example, a 0-1 binary solver is used, the bounds are <em>not</em> automatically
 * transformed to conform to this setting. (Thus in that situation the solver will throw an exception when attempted to
 * solve.) An undefined bound is equivalent to a minus, or plus, infinity. This is so, once again, independently of the
 * underlying solver. This permits to ensure that the same problem is solved independently of the chosen underlying
 * solver. An automatic transformation is done in cases it does not modify the problem, e.g. if the underlying solver
 * requires integer bounds for integer variables, the bounds will be set accordingly (e.g. transforming a lower bound of
 * 2.5 to a lowerbound of 3) because this has no impact on the set of feasible solution.
 * </p>
 * <p>
 * The type of the objects used for the variables should have their {@link #equals(Object)} method implemented in a
 * meaningful way as this is used when retrieving the values, and their {@link #hashCode()} method should be correctly
 * implemented. The variables should be immutable.
 * </p>
 * <p>
 * Unless otherwise specified in the documentation of specific solvers, the solution returned by {@link #getSolution()}
 * is set with the exact values returned by the solver, i.e. no attempt is made to be clever and change the returned
 * solution values. E.g. if the optimal value is not returned by the underlying solver, it is not set in the solution,
 * although it could possibly be deduced from the variable values and the objective function. Also no attempt is made to
 * round supposedly integer results. This allows the user to manually take into account the numerical imprecision of the
 * solver used.
 * </p>
 * <p>
 * Note about unconstrained variable: it is admitted to include variables that are used in no constraints in a problem.
 * Such a variable may have bounds. This solver does not take action: if the underlying solver gives a solution
 * (therefore chosen arbitrarily), it will appear as a solution. Forcing, with this solver, no solution for such a
 * variable would be appropriate only if the variable has no bound. If it has, then a solution should be given to avoid
 * exhibiting a different behavior between a bound on a variable and a constraint expressing the same bound.
 * </p>
 * <p>
 * Problems may be solved without an objective function, in which case the solve will simply search for a feasible
 * solution. In this case it never returns the status {@link LpResultStatus#OPTIMAL}. When solve is called, the bound
 * problem must have its objective function defined iff the objective direction is defined. Defining one and not the
 * other one is useless and considered as an error.
 * </p>
 * <p>
 * Solving a problem with no constraints (and even no variables) is allowed and results in a
 * {@link LpResultStatus#FEASIBLE} status.
 * </p>
 * <p>
 * As some solvers (notably CPLEX) sometimes yield a solution that is slightly outside the defined bounds for the
 * variable, this solver may be set to automatically correct such discrepancies when the difference between the bound
 * and the observed solution does not exceed a threshold defined by the user. See
 * {@link #setAutoCorrectThreshold(Double)}.
 * </p>
 * 
 * @param <T>
 *            the class used for the variables.
 * 
 * @author Olivier Cailloux
 */
public interface LpSolver<T> {

    /**
     * Must be used after a call to {@link #getUnderlyingSolver()} to close the solver and release possibly acquired
     * resources after use. If the underlying solver is not used directly by the user, it is not necessary to call this
     * method. If there is nothing to close (e.g. because everything is closed already or no resources have been
     * acquired yet), this method has no effect.
     * 
     * @throws LpSolverException
     *             if a solver-specific exception is thrown.
     */
    public void close() throws LpSolverException;

    /**
     * Retrieves the maximum difference between a bound and an observed value, after a solve, that will be automatically
     * corrected by this solver. The default is <code>null</code>.
     * 
     * @return <code>null</code> if this solver does not correct the obtained solutions.
     */
    public Double getAutoCorrectThreshold();

    /**
     * The duration of solving the bound problem (the last time it was solved). If an error occurred, this is the
     * duration until the error.
     * 
     * @return <code>null</code> iff not executed yet.
     */
    public LpSolverDuration getDuration();

    /**
     * Retrieves a view that allow to query and set the values of the parameters of this solver instance.
     * 
     * @return not <code>null</code>, a view that reads and writes through to this object.
     */
    public LpParameters getParametersView();

    /**
     * Retrieves the format this solver will use when asked to write a problem file with a format of
     * {@link LpFileFormat#SOLVER_PREFERRED}.
     * 
     * @return <code>null</code> if the solver preferred format is not in the set of enum constants in
     *         {@link LpFileFormat}.
     * @throws LpSolverException
     *             if the solver does not implement writing problem files.
     */
    public LpFileFormat getPreferredFormat() throws LpSolverException;

    /**
     * Retrieves a writable view that allows to read and set the problem bound to this solver instance.
     * 
     * @return not <code>null</code>.
     */
    public LpProblem<T> getProblem();

    /**
     * Retrieves the last results status obtained from solving a problem. This is the same as the result status returned
     * from the {@link #solve()} method.
     * 
     * @return {@code null} iff no problem solving has been attempted yet.
     */
    public LpResultStatus getResultStatus();

    /**
     * Retrieves one solution found to the last problem solved. If the result of the solve is optimal, the returned
     * solution is an optimal solution.
     * 
     * @return <code>null</code> iff no feasible solution to the problem have been found (yet). Immutable.
     */
    public LpSolution<T> getSolution();

    public Object getUnderlyingSolver() throws LpSolverException;

    /**
     * @return possibly <code>null</code>.
     */
    public Function<T, String> getVariableNamer();

    /**
     * Sets the maximum difference between a bound and an observed value, after a solve, that will be automatically
     * corrected by this solver.
     * 
     * @param autoCorrectThreshold
     *            <code>null</code> if this solver should not correct the obtained solutions.
     */
    public void setAutoCorrectThreshold(Double autoCorrectThreshold);

    /**
     * <p>
     * Sets the parameters this solver will use to the given parameters. Any value already set in this object is lost.
     * Thus if the given parameters are set to the default value for some parameter p and that parameter has a value set
     * in this object before the method is called, the value of p after the method returns is the default value.
     * </p>
     * <p>
     * The given parameters values are copied in this object, no reference is kept to the given object.
     * </p>
     * 
     * @param parameters
     *            not <code>null</code>.
     * @return <code>true</code> iff this object state changed as a result of this call. Equivalently,
     *         <code>false</code> iff the given parameter values are identical to the current values.
     */
    public boolean setParameters(LpParameters parameters);

    /**
     * Copies the given problem data into the problem bound to this solver. Any information possibly existing in this
     * object problem is lost.
     * 
     * @param problem
     *            not <code>null</code>.
     * @return <code>true</code> iff the state of the bound problem changed as a result of this method execution.
     */
    public boolean setProblem(LpProblem<T> problem);

    /**
     * <p>
     * If not <code>null</code>, this solver will automatically associates names to variables in the bound problem for
     * which no name has been specified, using the given namer, when creating the problem in the underlying solver.
     * </p>
     * <p>
     * Setting the parameter to <code>null</code> disables the auto naming functionality: if a variable has no name set
     * in the problem, it stays unnamed.
     * </p>
     * <p>
     * The default is to use, for variables with no name, the {@link #toString()} method of the variable object. An
     * equivalent function may be retrieved from {@link LpSolverUtils#getToStringFunction()}.
     * </p>
     * <p>
     * Note that giving a name to a variable when defining it in the problem always overrides this parameter.
     * </p>
     * 
     * @param variableNamer
     *            <code>null</code> to disable the behavior of automatic naming of the variables, in which case the
     *            variables to which no name has been associated will be given unnamed to the underlying solver.
     */
    public void setVariableNamer(Function<T, String> variableNamer);

    /**
     * Solves the bound optimization problem. If the bound problem has an objective function set, the optimization
     * direction must be set as well, and conversely.
     * 
     * @return not <code>null</code>.
     * 
     * @throws LpSolverException
     *             if some problem specific to the underlying solver occurs. Happens if some feature required for
     *             solving the bound problem with the bound parameters is missing in this implementation, e.g. a
     *             parameter value is not legal for this solver implementation (see {@link LpParameters}); or if the
     *             underlying solver throws a solver-dependent exception. Runtime exceptions thrown by a solver are not
     *             wrapped into a {@link LpSolverException}, except for exceptions known to be solver-specific.
     */
    public LpResultStatus solve() throws LpSolverException;

    /**
     * Writes the current problem bound to this solver to a file.
     * 
     * @param format
     *            not <code>null</code>.
     * @param file
     *            not <code>null</code>.
     * @param addExtension
     *            <code>true</code> to automatically add an appropriate extension to the given path.
     * @throws LpSolverException
     *             if a solver-specific exception occurs, or the solver does not implement writing problem files, or
     *             does not support the specified format.
     */
    public void writeProblem(LpFileFormat format, String file, boolean addExtension) throws LpSolverException;

}
