import mcmc.MCMC;
import variableElimination.VE;
import util.VariableType;


/**
 * Example of Bayes Net inference using VE and MCMC.
 * 
 * @author Craig Sketchley
 * @author Rohan Brooker
 * 
 */
public class BayesNetExample {

	public static void main(String[] args) {
		long curTime;
		
		curTime = System.nanoTime();
		// Setup Variable Elimination with the elimination ordering
		VE ve = new VE(VariableType.M, VariableType.S, VariableType.C, VariableType.B, VariableType.I);

		ve.turnOnDebugOutput();
		
		// Fix the evidence variables
		ve.setEvidenceObservation(VariableType.S, true);
		ve.setEvidenceObservation(VariableType.C, false);
		
		// Compute the query using Variable Elimination
		System.out.printf("%.4f\n", ve.computeQuery(VariableType.M));
		curTime = System.nanoTime() - curTime;
		System.out.println("Time taken: " + curTime);
	
		
		curTime = System.nanoTime();		
		// Setup MCMC with the number of iterations. 
		MCMC mc = new MCMC(1000, 1000);
		
		// Fix the evidence variables
		mc.setEvidenceObservation(VariableType.S, true);
		mc.setEvidenceObservation(VariableType.C, false);
		
		// Compute the query using MCMC
		System.out.printf("%.4f\n", mc.computeQuery(VariableType.M));
		curTime = System.nanoTime() - curTime;
		System.out.println("Time taken: " + curTime);
	}

}