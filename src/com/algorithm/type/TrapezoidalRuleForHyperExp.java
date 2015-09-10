package com.algorithm.type;

import com.launcher.Launcher;
import com.sim.event.eventhandler.EventHandler;
import com.sim.objects.LightPath;

/*************************************************************************
 * Compilation: javac TrapezoidalRule.java Execution: java TrapezoidalRule a b
 * 
 * Numerically integrate the function in the interval [a, b].
 *
 * % java TrapezoidalRule -3 3 0.9973002031388447 // true answer =
 * 0.9973002040...
 *
 * Observation: this says that 99.7% of time a standard normal random variable
 * is within 3 standard deviation of its mean.
 *
 * % java TrapezoidalRule 0 100000 1.9949108930964732 // true answer = 1/2
 *
 * Caveat: this is not the best way to integrate the normal density function.
 * See what happens if you make b very big.
 *
 *************************************************************************/

public class TrapezoidalRuleForHyperExp {

	private static boolean isForhmin;
    private static double mu1;
    private static double mu2;
    private static double p1;
    private static double p2;

    public TrapezoidalRuleForHyperExp(){

        this.mu1 = Launcher.getMeanHoldingTime();
        this.mu2 = Launcher.getMeanHoldingTime() * Launcher.getCov();
        this.p1 = 0.5;
        this.p2 = 0.5;
    }
	static double f(double t, LightPath lp) {
		
		if (isForhmin)
			return hmin(t, lp);
		else
			return 1-hmax(t, lp);
	}

	static double hmin(double t, LightPath lp) {

		double d;
		double n = lp.getSetOfConnections().size();
		double multiplication = 1;
		double x;
		double y;
		double m;

		for (int j = 0; j < n; j++) 
			if(lp.getSetOfConnections().get(j).isNotKnown()){
				d = lp.getSetOfConnections().get(j).getSpentTime(EventHandler.getTime());
				m = p1 * Math.exp(-mu1 * d) + p2 * Math.exp(-mu2 * d);
				x = (1 / m) * p1 * Math.exp(-mu1 * d) * (1 - Math.exp(-mu1 * t));
				y = (1 / m) * p2 * Math.exp(-mu2 * d) * (1 - Math.exp(-mu2 * t));
				multiplication *= (1 - x - y);
			}

		return multiplication;
	}

	static double hmax(double t, LightPath lp) {
		
		double d;
		double n = lp.getSetOfConnections().size();
		double multiplication = 1;
		double x;
		double y;
		double m;

		for (int j = 0; j < n; j++) 
			if(lp.getSetOfConnections().get(j).isNotKnown()){
				d = lp.getSetOfConnections().get(j).getSpentTime(EventHandler.getTime());
				m = p1 * Math.exp(-mu1 * d) + p2 * Math.exp(-mu2 * d);
				x = (1 / m) * p1 * Math.exp(-mu1 * d) * (1 - Math.exp(-mu1 * t));
				y = (1 / m) * p2 * Math.exp(-mu2 * d) * (1 - Math.exp(-mu2 * t));
				multiplication *= (x + y);
			}

		return multiplication;

	}

	/**********************************************************************
	 * Integrate f from a to b using the trapezoidal rule. Increase N for more
	 * precision.
	 **********************************************************************/
	static double integrate(double a, double b, int N, LightPath lp) {
		double h = (b - a) / N; // step size
		double sum = 0.5 * (f(a, lp) + f(b, lp)); // area
		for (int i = 1; i < N; i++) {
			double x = a + h * i;
			sum = sum + f(x, lp);
		}

		return sum * h;
	}

	public static void setForhmin(boolean isForhmin) {
		TrapezoidalRuleForHyperExp.isForhmin = isForhmin;
	}
}