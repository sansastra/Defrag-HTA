package com.rng.distribution.type;

import com.rng.distribution.Distribution;
import jsim.variate.Exponential;

/**
 * Created by Fran on 4/2/2015.
 */
public class ExponentialDistribution extends Distribution {

    Exponential exponential;
    public ExponentialDistribution(double mu, int stream) {
        super(mu, stream);

        exponential = new Exponential(mu, stream);
    }

    @Override
    public Double execute() {
        return exponential.gen();
    }
}
