package mcmc;

import util.VariableType;

/**
 * A variable within the Bayes Net. Holds information about it's children and
 * parents, and the probabilities for this variable.
 * 
 * @author Craig Sketchley
 * @author Rohan Brooker
 * 
 */
public class Variable {

	private VariableType var;

	private Variable[] parents;
	private double[] probabilities;
	
	private Variable[] children;

	private boolean currentAssignment;

	private int trueCount;

	/**
	 * Construct a random variable. Assumes variables are always ordered
	 * alphabetically. Gives the variable a random assignment.
	 * 
	 * @param var
	 */
	public Variable(VariableType var) {
		this.var = var;
		this.currentAssignment = (Math.random() < 0.5) ? true : false;
		this.trueCount = 0;

		this.parents = new Variable[0];
		this.probabilities = new double[0];
		this.children = new Variable[0];
	}

	/**
	 * Add all the variables parents and the probabilities.
	 * 
	 * @param parents
	 * @param probabilities
	 */
	public void addParentsAndProbabilities(Variable[] parents,
			double[] probabilities) {
		this.parents = parents;
		this.probabilities = probabilities;
	}

	/**
	 * Add all the variables children in the Bayes Net.
	 * 
	 * @param children
	 */
	public void addChildren(Variable[] children) {
		this.children = children;
	}

	/**
	 * 
	 * @param assignment
	 */
	public void setAssignment(boolean assignment) {
		this.currentAssignment = assignment;
	}

	/**
	 * Gets the probability of this node given the current state of its parents.
	 * 
	 * @return
	 */
	public double getProbabilityGivenParents() {
		boolean[] parentAssignments = new boolean[parents.length];

		for (int i = 0; i < parents.length; i++) {
			parentAssignments[i] = parents[i].currentAssignment;
		}

		return probabilities[getIndexFromBooleans(parentAssignments)];
	}

	/**
	 * Returns the true probability of this variable given the current state of
	 * it's Markov Blanket.
	 * 
	 * @return
	 */
	public double getProbabilityGivenMB() {
		double trueOutput = getProbabilityGivenParents();
		double falseOutput = 1 - trueOutput;

		boolean temp = currentAssignment;
		double prob;

		// Get the prob if this variable was true.
		currentAssignment = true;
		for (Variable v : children) {
			prob = v.getProbabilityGivenParents();
			trueOutput *= (v.currentAssignment) ? prob : 1 - prob;
		}

		// Get the prob if this variable was false.
		currentAssignment = false;
		for (Variable v : children) {
			prob = v.getProbabilityGivenParents();
			falseOutput *= (v.currentAssignment) ? prob : 1 - prob;
		}
		currentAssignment = temp;

		// Return the normalised true prob.
		return trueOutput / (trueOutput + falseOutput);
	}
	
	/**
	 * Sets the current assignment.
	 * 
	 * @param assignment
	 */
	public void setCurrentAssignment(boolean assignment) {
		this.currentAssignment = assignment;
	}
	
	/**
	 * Gets the current assignment.
	 * 
	 * @return
	 */
	public boolean currentAssignment() {
		return this.currentAssignment;
	}
	
	/**
	 * Increments the true count if the current assignment is true.
	 */
	public void incrementTrueCount() {
		if (currentAssignment) {
			trueCount++;
		}
	}
	
	/**
	 * Resets the true count to zero.
	 */
	public void resetTrueCount() {
		this.trueCount = 0;
	}
	
	/**
	 * Gets the true count.
	 * 
	 * @return
	 */
	public int getTrueCount() {
		return this.trueCount;
	}

	/**
	 * That is some super awesome bitwise code.
	 * 
	 * @param assignments
	 * @return
	 */
	private int getIndexFromBooleans(boolean... assignments) {
		int output = 0;

		for (int i = 0; i < assignments.length; i++) {
			output |= (assignments[assignments.length - i - 1]) ? (1 << i) : 0;
		}

		return output;
	}

	@Override
	public String toString() {
		StringBuffer output = new StringBuffer();

		output.append(var);
		output.append(": ");
		output.append(currentAssignment);
		output.append('\n');

		return output.toString();
	}
}
