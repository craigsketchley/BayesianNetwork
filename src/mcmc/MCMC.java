package mcmc;

import java.util.Iterator;
import java.util.LinkedHashMap;

import util.VariableType;

/**
 * A Bayes Net used for computing queries using the MCMC algorithm.
 * 
 * @author Craig Sketchley
 * @author Rohan Brooker
 * 
 */
public class MCMC {

	private LinkedHashMap<VariableType, Variable> variables;
	private LinkedHashMap<VariableType, Variable> evidence;
	
	private int N, M;
	
	/**
	 * Sets up MCMC for the specified Bayes Net for N iterations.
	 * 
	 * @param N
	 * @param M
	 */
	public MCMC(int N, int M) {
		this.N = N;
		this.M = M;
		
		resetBayesNet();
	}
	
	/**
	 * Sets the number of iterations used in MCMC.
	 * 
	 * @param N
	 */
	public void setN(int N) {
		this.N = N;
	}
	
	/**
	 * Sets the number of times to run MCMC and averaged.
	 * 
	 * @param M
	 */
	public void setM(int M) {
		this.M = M;
	}
	
	/**
	 * Resets the Bayes Net to begin assigning evidence and computing queries.
	 */
	public void resetBayesNet() {
		variables = new LinkedHashMap<VariableType, Variable>();
		evidence = new LinkedHashMap<VariableType, Variable>();
		
		// Setup the network for this specific example...
		
		Variable b = new Variable(VariableType.B);
		Variable c = new Variable(VariableType.C);
		Variable i = new Variable(VariableType.I);
		Variable m = new Variable(VariableType.M);
		Variable s = new Variable(VariableType.S);
		
		b.addParentsAndProbabilities(new Variable [] {
				m
		}, new double [] {
					  // M
				0.05, // F
				0.20  // T
		});
		b.addChildren(new Variable [] {
				c,
				s
		});
		
		c.addParentsAndProbabilities(new Variable [] {
				b,
				i
		}, new double [] {
					  // B I
				0.05, // F F
				0.80, // F T
				0.80, // T F
				0.80  // T T
		});
		c.addChildren(new Variable [] {});
		
		i.addParentsAndProbabilities(new Variable [] {
				m
		}, new double [] {
					  // M
				0.20, // F
				0.80  // T
		});
		i.addChildren(new Variable [] {
				c
		});
		
		m.addParentsAndProbabilities(new Variable [] {}, new double [] {
				0.20
		});
		m.addChildren(new Variable [] {
				b,
				i
		});
		
		s.addParentsAndProbabilities(new Variable [] {
				b
		}, new double [] {
					  // B
				0.60, // F
				0.80  // T
		});
		s.addChildren(new Variable [] {});
		
		
		// Add variable in alphabetical order.
		variables.put(VariableType.B, b);
		variables.put(VariableType.C, c);
		variables.put(VariableType.I, i);
		variables.put(VariableType.M, m);
		variables.put(VariableType.S, s);
	}
	
	/**
	 * Run MCMC on the network N iterations for the given query variable.
	 * 
	 * @param var
	 * @param N
	 * @return
	 */
	public double computeQuery(VariableType var) {
		if (variables.values().size() == 0 || evidence.containsKey(var)) {
			// If the variable has already been observed, don't need to compute...
			// Return 1 if the variable has been observed to be true, 0 otherwise.
			return evidence.get(var).currentAssignment() ? 1 : 0;
		}
		
		Iterator<Variable> iter = variables.values().iterator();
		
		double sum = 0;
		
		for (int i = 0; i < M; i++) {
			randomiseVariableAssignments();
			for (int j = 0; j < N; j++) {
				if (!iter.hasNext()) {
					iter = variables.values().iterator();
				}
				Variable v = iter.next();
				v.setAssignment(Math.random() < v.getProbabilityGivenMB());
				incrementTrueCounters();
			}
			sum += variables.get(var).getTrueCount();
			resetTrueCounters();
		}

		return sum / (M * N);
	}
	
	/**
	 * Increment the trueCounters in each non-evidence variable if they are currently true.
	 */
	private void incrementTrueCounters() {
		for (Variable v : variables.values()) {
			v.incrementTrueCount();
		}
	}
	
	/**
	 * Resets all the true counts for each non-evidence variable.
	 */
	private void resetTrueCounters() {
		for (Variable v : variables.values()) {
			v.resetTrueCount();
		}
	}
	
	/**
	 * Randomises all the non-evidence variable assignments.
	 */
	private void randomiseVariableAssignments() {
		for (Variable v : variables.values()) {
			v.setCurrentAssignment((Math.random() < 0.5) ? true : false);
		}
	}
	
	/**
	 * Set a variable type to observed.
	 * 
	 * @param var
	 * @param observedValue
	 */
	public void setEvidenceObservation(VariableType var, boolean observedValue) {
		Variable v = variables.remove(var);
		v.setAssignment(observedValue);
		evidence.put(var, v);
	}
	
	@Override
	public String toString() {
		StringBuilder output = new StringBuilder();
		
		output.append("Variables:\n");
		
		if (variables.size() > 0) {
			for (Variable v : variables.values()) {
				output.append(v);
			}
		} else {
			output.append("[none]\n");
		}
		
		output.append("Evidence:\n");
		
		if (evidence.size() > 0) {
			for (Variable v : evidence.values()) {
				output.append(v);
			}
		} else {
			output.append("[none]\n");
		}
		
		return output.toString();
	}
}
