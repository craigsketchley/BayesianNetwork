package variableElimination;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import util.VariableType;

/**
 * Holds information stored within a Factor table with operations defined in
 * order to complete variable elimination.
 * 
 * @author Craig Sketchley
 * @author Rohan Brooker
 * 
 */
public class Factor {

	double[] values;
	VariableType[] variables;
	HashSet<VariableType> variableSet;

	/**
	 * Create a Factor from an array of values, and a family of unique variable
	 * types.
	 * 
	 * Assumptions: - binary domains for each variable. - variables are in a
	 * fixed order, alphabetic. - the length of values proportional to the
	 * number of variables. 2^k, for k variables.
	 * 
	 * Using the fact that the indices for the values array in binary can
	 * represent a permutation of true/false value assignments. In other words,
	 * given some 3 binary variable Factor table, the probability of value
	 * assignment (true, false, true) is at index 0b101, or 5. (false, true,
	 * true) would be at index 0b011, or 3.
	 * 
	 * @param values
	 * @param variables
	 */
	public Factor(double[] values, VariableType... variables) {
		// TODO: Check for repeated variable types in variables.
		// TODO: Check for the correct number of values.

		this.values = values;
		this.variables = new VariableType[variables.length];
		this.variableSet = new HashSet<VariableType>();

		// store the variables in reverse order so their index matches the bit
		// digit.
		for (int i = variables.length - 1; i >= 0; i--) {
			this.variables[variables.length - i - 1] = variables[i];
			this.variableSet.add(variables[i]);
		}
	}

	/**
	 * Returns the array of variable types within this factor table.
	 * 
	 * Note: they are in the reverse order to when they were supplied.
	 * 
	 * @return
	 */
	public VariableType[] getVariables() {
		return this.variables;
	}

	/**
	 * Return the array of values
	 * 
	 * @return
	 */
	public double[] getValues() {
		return this.values;
	}

	/**
	 * Using pointwise product, multiply the given Factor tables together,
	 * returning the resulting factor table.
	 * 
	 * @param factors
	 * @return
	 */
	public static Factor pointwiseProduct(Set<Factor> factors) {
		if (factors.size() == 0)
			return null;

		Iterator<Factor> iter = factors.iterator();
		Factor output = iter.next();
		while (iter.hasNext()) {
			output = output.pointwiseProduct(iter.next());
		}
		return output;
	}

	/**
	 * Using pointwise product, multiply this Factor table with the given Factor
	 * table and returns a new Factor table.
	 * 
	 * @param that
	 * @return
	 */
	private Factor pointwiseProduct(Factor that) {
		// Variable that are contained in both tables.
		VariableType[] outputVariableSet = unionVariableSets(this, that);

		int thisFactorOverlapMask = getOverlapMask(this.variables,
				outputVariableSet);
		int thatFactorOverlapMask = getOverlapMask(that.variables,
				outputVariableSet);

		double[] outputValues = new double[(int) Math.pow(2,
				outputVariableSet.length)];

		for (int i = 0; i < outputValues.length; i++) {
			int thisFactorIndex = getIndexInOriginalFactor(i,
					thisFactorOverlapMask);
			int thatFactorIndex = getIndexInOriginalFactor(i,
					thatFactorOverlapMask);
			outputValues[i] = this.values[thisFactorIndex]
					* that.values[thatFactorIndex];
		}

		reverseArrayInPlace(outputVariableSet);

		return new Factor(outputValues, outputVariableSet);
	}

	/**
	 * Given some new index in a new factor table, and a bit mask indicating
	 * which bits correspond to the original factor table variables, output the
	 * index in the original factor table.
	 * 
	 * @param newIndex
	 * @param origBitMask
	 * @return
	 */
	public static int getIndexInOriginalFactor(int newIndex, int origBitMask) {
		int numOfBits = Integer.bitCount(origBitMask);

		if (numOfBits == 0)
			return 0; // no common bits.

		int output = 0;
		int j = 0;

		for (int i = 0; i < Integer.SIZE; i++) {
			if ((origBitMask & (1 << i)) != 0) {
				output |= ((newIndex & (1 << i)) >> (i - j));
				j++;
			}
		}

		return output;
	}

	/**
	 * Returns the bit mask for the variable subset, in the variable set given.
	 * 
	 * So if:
	 * 
	 * varSubset = [ B, M ], and varSet = [ B, I, M, S ].
	 * 
	 * Then the output would be a bit mask of 0b1010, which is where the
	 * variables of varSubset occur in the varSet.
	 * 
	 * @param varSubset
	 * @param overlapVars
	 * @return
	 */
	public static int getOverlapMask(VariableType[] varSubset,
			VariableType[] varSet) {
		int overlapMask = 0;

		int j = 0;
		for (int i = 0; i < varSet.length; i++) {
			if (varSubset[j].equals(varSet[i])) {
				overlapMask |= (1 << i);
				j++;
			}
			if (j == varSubset.length) {
				break;
			}
		}

		return overlapMask;
	}

	/**
	 * Returns an array of VariableTypes as the union of the given factors
	 * variables.
	 * 
	 * @param f
	 * @return
	 */
	public static VariableType[] unionVariableSets(Factor f1, Factor f2) {
		ArrayList<VariableType> set = new ArrayList<VariableType>();

		// Since both variable type arrays are relatively ordered...
		int f1Index = 0, f2Index = 0;

		while (f1Index < f1.variables.length && f2Index < f2.variables.length) {
			if (f1.variables[f1Index].compareTo(f2.variables[f2Index]) > 0) {
				set.add(f1.variables[f1Index]);
				f1Index++;
			} else if (f1.variables[f1Index].compareTo(f2.variables[f2Index]) < 0) {
				set.add(f2.variables[f2Index]);
				f2Index++;
			} else {
				set.add(f1.variables[f1Index]);
				f1Index++;
				f2Index++;
			}
		}

		// Add the rest...
		if (f1Index < f1.variables.length) {
			for (; f1Index < f1.variables.length; f1Index++) {
				set.add(f1.variables[f1Index]);
			}
		} else if (f2Index < f2.variables.length) {
			for (; f2Index < f2.variables.length; f2Index++) {
				set.add(f2.variables[f2Index]);
			}
		}

		VariableType[] output = new VariableType[set.size()];

		return set.toArray(output);
	}

	/**
	 * Given some variable type, marginalise over that variable within the
	 * factor table.
	 * 
	 * @param var
	 * @return
	 */
	private void sumOut(VariableType var) {
		// Find the index of the variable to be marginalised. Also, create a new
		// array of all other variables. Keeping relative ordering.
		int i = 0;
		int varIndex = -1;
		VariableType[] vars = new VariableType[variables.length - 1];
		for (VariableType v : variables) {
			if (!v.equals(var) && i < variables.length - 1) {
				vars[i] = v;
				i++;
			} else if (v.equals(var)) {
				varIndex = i;
			}
		}

		if (varIndex < 0) {
			// TODO: No such element. Is this correct behaviour?
			return;
		}

		// Marginalise for the true and false values of the variable into a new
		// value array.
		int varBitMask = 1 << varIndex;
		int aboveMask = (0xFFFFFFFF >> varIndex + 1) << varIndex + 1;
		int belowMask = ~((0xFFFFFFFF >> varIndex) << varIndex);
		double[] newValues = new double[this.values.length / 2];
		for (i = 0; i < values.length; i++) {
			if ((i & varBitMask) == 0) { // variable is false
				double oneProb = values[i]; // false variable prob
				double twoProb = values[i | varBitMask]; // true variable prob
				int newIndex = ((aboveMask & i) >> 1) | (belowMask & i);
				newValues[newIndex] = oneProb + twoProb;
			}
		}

		this.values = newValues;
		this.variables = vars;
		this.variableSet.remove(var);
	}

	/**
	 * Reverse the contents of a VariableType array in place.
	 * 
	 * @param vars
	 */
	private static void reverseArrayInPlace(VariableType[] vars) {
		VariableType temp;
		for (int i = 0; i < vars.length / 2; i++) {
			temp = vars[i];
			vars[i] = vars[vars.length - i - 1];
			vars[vars.length - i - 1] = temp;
		}
	}

	/**
	 * Given some variable types, marginalise over those variable within this
	 * factor table.
	 * 
	 * @param var
	 * @return
	 */
	public void sumOut(VariableType... vars) {
		for (VariableType v : vars) {
			sumOut(v);
		}
	}

	/**
	 * Fix a value for a variable for this Factor table.
	 * 
	 * @param var
	 * @param value
	 */
	public void fixVariable(VariableType var, boolean value) {
		if (!contains(var)) {
			return;
		}

		int i = 0;
		int varIndex = -1;
		VariableType[] vars = new VariableType[variables.length - 1];
		for (VariableType v : variables) {
			if (!v.equals(var) && i < variables.length - 1) {
				vars[i] = v;
				i++;
			} else if (v.equals(var)) {
				varIndex = i;
			}
		}

		int varBitMask = 1 << varIndex;
		int aboveMask = (0xFFFFFFFF >> varIndex + 1) << varIndex + 1;
		int belowMask = ~((0xFFFFFFFF >> varIndex) << varIndex);
		double[] newValues = new double[this.values.length / 2];
		for (i = 0; i < values.length; i++) {
			if ((i & varBitMask) == 0) { // variable is false
				int newIndex = ((aboveMask & i) >> 1) | (belowMask & i);
				if (!value) {
					newValues[newIndex] = values[i]; // var is true
				} else {
					newValues[newIndex] = values[i | varBitMask]; // var is
																	// false
				}
			}
		}

		this.values = newValues;
		this.variables = vars;
		this.variableSet.remove(var);
	}

	/**
	 * Return the respective probability given the boolean assignment for the
	 * factor variables.
	 * 
	 * @param variableValues
	 * @return
	 */
	public double getProbability(boolean... variableValues) {
		if (variableValues.length != variableSet.size()) {
			System.out.println("Wrong number of variable assignments");
			return -1;
		}
		return values[getIndex(variableValues)];
	}

	/**
	 * Gets the probability index in the values array given a set of true/false
	 * values of variables.
	 * 
	 * @param variableValues
	 * @return
	 */
	private int getIndex(boolean[] variableValues) {
		int index = 0;

		for (int i = 0; i < variableValues.length; i++) {
			index |= (variableValues[i]) ? 1 : 0;
			if (i < variableValues.length - 1) {
				index <<= 1;
			}
		}
		return index;
	}

	/**
	 * Returns true if this Factor contains the given variable type.
	 * 
	 * @param var
	 * @return
	 */
	public boolean contains(VariableType var) {
		return this.variableSet.contains(var);
	}
	
	/**
	 * Returns true if this factor has no free variables.
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return variableSet.isEmpty();
	}

	@Override
	public String toString() {
		StringBuilder output = new StringBuilder();

		for (int i = variables.length - 1; i >= 0; i--) {
			output.append(variables[i].toString() + '\t');
		}

		output.append("Phi\n");

		for (int i = 0; i < values.length; i++) {
			int mask = 1 << variables.length - 1;
			for (int j = 0; j < variables.length; j++) {
				output.append((i & (mask >> j)) > 0 ? "T\t" : "F\t");
			}
			output.append(String.format("%.4f\n", values[i]));
		}

		return output.toString();
	}
}
