package com.algorithm.type;

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

public class TrapezoidalRuleForLogNormal {

	private static boolean isForhmin;
    private static double sigma;
    private static double mu;

    public TrapezoidalRuleForLogNormal(double _sigma, double _mu){
        sigma = _sigma;
        mu = _mu;
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
		double m;
		double alpha;
		double erf1;
		double erf2;
		
		for (int j = 0; j < n; j++) 
			if(lp.getSetOfConnections().get(j).isNotKnown()){
				d = lp.getSetOfConnections().get(j).getSpentTime(EventHandler.getTime());
				alpha = (Math.log(d)-mu)/(sigma*(Math.sqrt(2.0)));
				erf1 = org.apache.commons.math3.special.Erf.erf(alpha);
				m = 1- erf1;
				alpha = (Math.log(t+d)-mu)/(sigma*(Math.sqrt(2.0)));
				erf2 = org.apache.commons.math3.special.Erf.erf(alpha);
				double x = (1.0 - ((1.0/m)*(erf2-erf1)));
				multiplication *= x;
			}

		return multiplication;
	}

	static double hmax(double t, LightPath lp) {
		
		double d;
		double n = lp.getSetOfConnections().size();
		double multiplication = 1;
		double m;
		double alpha;
		double erf1;
		double erf2;
		
		for (int j = 0; j < n; j++) 
			if(lp.getSetOfConnections().get(j).isNotKnown()){
				d = lp.getSetOfConnections().get(j).getSpentTime(EventHandler.getTime());
				alpha = (Math.log(d)-mu)/(sigma*(Math.sqrt(2.0)));
				erf1 = org.apache.commons.math3.special.Erf.erf(alpha);
				m = 1- erf1;
				alpha = (Math.log(t+d)-mu)/(sigma*(Math.sqrt(2.0)));
				erf2 = org.apache.commons.math3.special.Erf.erf(alpha);

				multiplication *=  ((1.0/m)*(erf2-erf1));
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
		TrapezoidalRuleForLogNormal.isForhmin = isForhmin;
	}
}