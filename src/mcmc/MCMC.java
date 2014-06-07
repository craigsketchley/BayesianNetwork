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

	LinkedHashMap<VariableType, Variable> variables;
	LinkedHashMap<VariableType, Variable> evidence;
	
	int N;
	
	/**
	 * Sets up MCMC for the specified Bayes Net for N iterations.
	 * 
	 * @param N
	 */
	public MCMC(int N) {
		this.N = N;
		variables = new LinkedHashMap<VariableType, Variable>();
		evidence = new LinkedHashMap<VariableType, Variable>();
		
		// Setup the network for this specific example...
		
		Variable B = new Variable(VariableType.B);
		Variable C = new Variable(VariableType.C);
		Variable I = new Variable(VariableType.I);
		Variable M = new Variable(VariableType.M);
		Variable S = new Variable(VariableType.S);
		
		B.addParentsAndProbabilities(new Variable [] {
				M
		}, new double [] {
					  // M
				0.05, // F
				0.20  // T
		});
		B.addChildren(new Variable [] {
				C,
				S
		});
		
		C.addParentsAndProbabilities(new Variable [] {
				B,
				I
		}, new double [] {
					  // B I
				0.05, // F F
				0.80, // F T
				0.80, // T F
				0.80  // T T
		});
		C.addChildren(new Variable [] {});
		
		I.addParentsAndProbabilities(new Variable [] {
				M
		}, new double [] {
					  // M
				0.20, // F
				0.80  // T
		});
		I.addChildren(new Variable [] {
				C
		});
		
		M.addParentsAndProbabilities(new Variable [] {}, new double [] {
				0.20
		});
		M.addChildren(new Variable [] {
				B,
				I
		});
		
		S.addParentsAndProbabilities(new Variable [] {
				B
		}, new double [] {
					  // B
				0.60, // F
				0.80  // T
		});
		S.addChildren(new Variable [] {});
		
		
		// Add variable in alphabetical order.
		variables.put(VariableType.B, B);
		variables.put(VariableType.C, C);
		variables.put(VariableType.I, I);
		variables.put(VariableType.M, M);
		variables.put(VariableType.S, S);
	}
	
	/**
	 * Run MCMC on the network N iterations for the given query variable.
	 * 
	 * @param var
	 * @param N
	 * @return
	 */
	public double computeQuery(VariableType var) {
		if (variables.values().size() == 0) {
			return evidence.get(var).currentAssignment ? 1 : 0;
		}
		
		Iterator<Variable> iter = variables.values().iterator();
		
		for (int i = 0; i < N; i++) {
			if (!iter.hasNext()) {
				iter = variables.values().iterator();
			}
			Variable v = iter.next();
			v.setAssignment(Math.random() < v.getProbabilityGivenMB());
			incrementTrueCounters();
		}
		
		return variables.get(var).trueCount / (double) N;
	}
	
	/**
	 * Increment the trueCounters in each non-evidence variable if they are currently true.
	 */
	private void incrementTrueCounters() {
		for (Variable v : variables.values()) {
			v.trueCount += (v.currentAssignment) ? 1 : 0;
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
