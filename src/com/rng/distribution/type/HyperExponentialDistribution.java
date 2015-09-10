package com.rng.distribution.type;

import jsim.variate.HyperExponential;

import com.rng.distribution.Distribution;

public class HyperExponentialDistribution extends Distribution{

	HyperExponential hyperExponential;
	public HyperExponentialDistribution(double mu, double sigma, int i){
        super(mu, sigma, i);

        hyperExponential = new HyperExponential(mu, sigma, i);
	}


	@Override
	public Double execute() {
		return hyperExponential.gen();
	}
}
