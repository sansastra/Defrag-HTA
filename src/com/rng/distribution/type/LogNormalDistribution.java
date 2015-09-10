package com.rng.distribution.type;

import jsim.variate.LogNormal;

import com.rng.distribution.Distribution;

public class LogNormalDistribution extends Distribution{

	LogNormal logNormal;
	public LogNormalDistribution(double mu, double sigma, int i){
        super(mu, sigma, i);

        logNormal = new LogNormal(mu, sigma, i);
	}

	@Override
	public Double execute() {
		return logNormal.gen();
	}
}
