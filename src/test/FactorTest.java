package test;

import static org.junit.Assert.*;

import java.util.LinkedHashSet;

import org.junit.Before;
import org.junit.Test;

import variableElimination.Factor;
import util.VariableType;

public class FactorTest {

	public static final double DELTA = 0.0001; 
	
	Factor PhiB, PhiC, PhiI, PhiM, PhiS;
	
	@Before
	public void setup() {
		PhiB = new Factor(new double[] {
				  // B,		M
			0.95, // false, false
			0.8,  // false, true
			0.05, // true,  false
			0.2   // true,  true
		}, VariableType.B, VariableType.M);
	
		PhiC = new Factor(new double[] {
				  // B,		C,	   I
			0.95, // false, false, false
			0.2,  // false, false, true
			0.05, // false, true,  false
			0.8,  // false, true,  true
			0.2,  // true,  false, false
			0.2,  // true,  false, true
			0.8,  // true,  true,  false
			0.8   // true,  true,  true
		}, VariableType.B, VariableType.C, VariableType.I);
	
		PhiI = new Factor(new double[] {
				  // I,		M
			0.8,  // false, false
			0.2,  // false, true
			0.2,  // true,  false
			0.8   // true,  true
		}, VariableType.I, VariableType.M);
	
		PhiM = new Factor(new double[] {
				  // M
			0.8, // false
			0.2	 // true
		}, VariableType.M);
	
		PhiS = new Factor(new double[] {
				  // B,		S
			0.4,  // false, false
			0.6,  // false, true
			0.2,  // true,  false
			0.8   // true,  true
		}, VariableType.B, VariableType.S);
	}
	
	
	@Test
	public void getOverlapMaskTest() {
		VariableType[] subset = {VariableType.B, VariableType.M};
		VariableType[] set = {VariableType.B, VariableType.C, VariableType.I, VariableType.M, VariableType.S};
		
		int result = Factor.getOverlapMask(subset, set);
		assertEquals(0b01001, result);
	}

	@Test
	public void getIndexInOriginalFactorTest() {
		int newIndex = 0b10101010101;
		int bitmask = 0b01001011010;
		
		int result = Factor.getIndexInOriginalFactor(newIndex, bitmask);
		assertEquals(0b01100, result);
	}

	@Test
	public void unionVariableSetsTest1() {
		VariableType[] result = Factor.unionVariableSets(PhiS, PhiC);
		VariableType[] expected = {VariableType.S, VariableType.I, VariableType.C, VariableType.B};
		assertArrayEquals(expected, result);
	}

	@Test
	public void unionVariableSetsTest2() {
		VariableType[] result = Factor.unionVariableSets(PhiI, PhiC);
		VariableType[] expected = {VariableType.M, VariableType.I, VariableType.C, VariableType.B};
		assertArrayEquals(expected, result);
	}
	
	@Test
	public void sumOutTest() {
		Factor result = PhiC.sumOut(VariableType.B);
		
		VariableType[] vars = {VariableType.I, VariableType.C};
		double[] values = { 1.15, 0.4, 0.85, 1.6 };
		
		assertArrayEquals(vars, result.getVariables());
		assertArrayEquals(values, result.getValues(), DELTA);
	}
	
	@Test
	public void fixVariableTest() {
		PhiC.fixVariable(VariableType.C, false);
		
		VariableType[] vars = {VariableType.I, VariableType.B};
		double[] values = { 0.95, 0.2, 0.2, 0.2 };
		
		assertArrayEquals(vars, PhiC.getVariables());
		assertArrayEquals(values, PhiC.getValues(), DELTA);
	}
	
	@Test
	public void pointwiseProductTest() {
		LinkedHashSet<Factor> factors = new LinkedHashSet<Factor>();
		factors.add(PhiS);
		factors.add(PhiB);
		
		Factor result = Factor.pointwiseProduct(factors);
		
		VariableType[] vars = {VariableType.S, VariableType.M, VariableType.B};
		double[] values = { 0.95*0.4, 0.95*0.6, 0.8*0.4, 0.8*0.6, 0.05*0.2, 0.05*0.8, 0.2*0.2, 0.2*0.8 };
		
		assertArrayEquals(vars, result.getVariables());
		assertArrayEquals(values, result.getValues(), DELTA);
	}
	
}
