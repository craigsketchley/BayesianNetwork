package variableElimination;

import java.util.Iterator;
import java.util.LinkedHashSet;

import util.VariableType;

/**
 * A Bayes Net representation of the given scenario.
 * 
 * @author Craig Sketchley
 * @author Rohan Brooker
 * 
 */
public class VE {

	private Factor PhiB, PhiC, PhiI, PhiM, PhiS;

	private VariableType[] eliminationOrdering;

	private LinkedHashSet<VariableType> hiddenVariables;

	private LinkedHashSet<Factor> factors;

	/**
	 * Creates a Bayes net for the specific example with an elimination ordering.
	 * 
	 * @param eliminationOrder
	 */
	public VE(VariableType... eliminationOrder) {
		// TODO: Validate the elimination ordering.
		setEliminationOrdering(eliminationOrder);
		resetBayesNet();
	}

	/**
	 * Resets the the Bayes net ready for another query. Removes all evidence observations.
	 */
	public void resetBayesNet() {
		PhiB = new Factor(new double[] {
				// B, M
				0.95, // false, false
				0.8, // false, true
				0.05, // true, false
				0.2 // true, true
				}, VariableType.B, VariableType.M);

		PhiC = new Factor(new double[] {
				// B, C, I
				0.95, // false, false, false
				0.2, // false, false, true
				0.05, // false, true, false
				0.8, // false, true, true
				0.2, // true, false, false
				0.2, // true, false, true
				0.8, // true, true, false
				0.8 // true, true, true
				}, VariableType.B, VariableType.C, VariableType.I);

		PhiI = new Factor(new double[] {
				// I, M
				0.8, // false, false
				0.2, // false, true
				0.2, // true, false
				0.8 // true, true
				}, VariableType.I, VariableType.M);

		PhiM = new Factor(new double[] {
				// M
				0.8, // false
				0.2 // true
				}, VariableType.M);

		PhiS = new Factor(new double[] {
				// B, S
				0.4, // false, false
				0.6, // false, true
				0.2, // true, false
				0.8 // true, true
				}, VariableType.B, VariableType.S);

		factors = new LinkedHashSet<Factor>();
		factors.add(PhiB);
		factors.add(PhiC);
		factors.add(PhiI);
		factors.add(PhiM);
		factors.add(PhiS);

		hiddenVariables = new LinkedHashSet<VariableType>();
		hiddenVariables.add(VariableType.B);
		hiddenVariables.add(VariableType.C);
		hiddenVariables.add(VariableType.I);
		hiddenVariables.add(VariableType.M);
		hiddenVariables.add(VariableType.S);
	}

	/**
	 * Sets the Bayes Net like the one in the textbook, used for validation.
	 * 
	 * Where:
	 * 
	 * C->A
	 * B->B
	 * I->E
	 * S->J
	 * M->M
	 * 
	 */
	public void setupExampleBayesNet() {
		PhiB = new Factor(new double[] {
					   // B(
				0.999, // false
				0.001  // true
				}, VariableType.B);

		PhiC = new Factor(new double[] {
					   // B(B), C(A), I(E)
				0.999, // false, false, false
				0.71,  // false, false, true
				0.001, // false, true, false
				0.29,  // false, true, true
				0.06,  // true, false, false
				0.05,  // true, false, true
				0.94,  // true, true, false
				0.95   // true, true, true
				}, VariableType.B, VariableType.C, VariableType.I);

		PhiI = new Factor(new double[] {
					   // I(E)
				0.998, // false
				0.002, // true
				}, VariableType.I);

		PhiM = new Factor(new double[] {
					 // C(A), M(M)
				0.99, // false, false
				0.01, // false, true
				0.30, // true, false
				0.70  // true, true
				}, VariableType.C, VariableType.M);

		PhiS = new Factor(new double[] {
					 // C(A), S(J)
				0.95, // false, false
				0.05, // false, true
				0.10, // true, false
				0.90  // true, true
				}, VariableType.C, VariableType.S);

		factors = new LinkedHashSet<Factor>();
		factors.add(PhiB);
		factors.add(PhiC);
		factors.add(PhiI);
		factors.add(PhiM);
		factors.add(PhiS);

		hiddenVariables = new LinkedHashSet<VariableType>();
		hiddenVariables.add(VariableType.B);
		hiddenVariables.add(VariableType.C);
		hiddenVariables.add(VariableType.I);
		hiddenVariables.add(VariableType.M);
		hiddenVariables.add(VariableType.S);
	}

	/**
	 * Sets the elimination ordering for this Bayes Net.
	 * 
	 * @param eliminationOrder
	 */
	public void setEliminationOrdering(VariableType... eliminationOrder) {
		// TODO: Check for correct number of variables
		// TODO: Check for unique variables.
		this.eliminationOrdering = eliminationOrder;
	}

	/**
	 * Returns the probability of a true assignment to the query variable in the
	 * Bayes Net given any evidence assignments.
	 * 
	 * @param queryVariable
	 * @return
	 */
	public double computeQuery(VariableType queryVariable) {
		hiddenVariables.remove(queryVariable);

		LinkedHashSet<Factor> tempFactors;
		Iterator<Factor> iter;
		Factor factor = null;

		for (VariableType var : eliminationOrdering) {
			tempFactors = new LinkedHashSet<Factor>();
			iter = factors.iterator();

			// Collect all the factors associated with this variable.
			while (iter.hasNext()) {
				factor = iter.next();
				if (factor.contains(var)) {
					tempFactors.add(factor);
					iter.remove();
				}
			}

			// If no factors then go on to the next variable in the elimination
			// order.
			if (tempFactors.size() == 0) {
				continue;
			}

			factor = Factor.pointwiseProduct(tempFactors);
			
			if (isHiddenVariable(var)) {
				factor = factor.sumOut(var);
			}
			
			if (!factor.isEmpty()) {
				factors.add(factor);
			}
		}

		// Remaining factors should just contain 1 factor table
		if (factors.size() != 1) {
			System.out.println(factors);
			System.out.println(this);
			System.out.println("Something went wrong. Too many factors left...");
			return -1;
		}

		iter = factors.iterator();
		Factor finalFactor = iter.next();

		// The final factor table should just contain 1 variable.
		if (finalFactor.variableSet.size() != 1) {
			System.out.println("Not the right amount of variables in the final factor...");
			return -1;
		}

		// Normalise the final probability.
		double trueVal = finalFactor.getProbability(true);
		double falseVal = finalFactor.getProbability(false);
		double sum = trueVal + falseVal;

		return trueVal / sum;
	}

	/**
	 * Sets an observation of a variable for all factor tables. Removes the variable from hidden variables.
	 * @param var
	 * @param observation
	 */
	public void setEvidenceObservation(VariableType var, boolean observation) {
		Iterator<Factor> iter = factors.iterator();
		Factor factor;
		while (iter.hasNext()) {
			factor = iter.next();
			factor.fixVariable(var, observation);
			if (factor.isEmpty()) {
				iter.remove();
			}
		}
		hiddenVariables.remove(var);
	}

	@Override
	public String toString() {
		StringBuffer output = new StringBuffer();
		for (Factor f : factors) {
			output.append(f);
		}
		return output.toString();
	}
	
	/**
	 * Returns true if the variable is a hidden variable.
	 * 
	 * @param var
	 * @return
	 */
	private boolean isHiddenVariable(VariableType var) {
		return hiddenVariables.contains(var);
	}

}
