package com.rng.distribution;

/**
 * Abstract class for probability distributions
 * 
 * @author Fran Carpio
 * 
 */
public abstract class Distribution {


    public Distribution(double mu, int stream){ }

    public Distribution(double mu, double sigma, int stream){ }

    public Distribution(double min, double max){ }

	public abstract Double execute();

}
