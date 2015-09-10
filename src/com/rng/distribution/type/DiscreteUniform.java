package com.rng.distribution.type;

import java.util.Random;

import org.uncommons.maths.random.DiscreteUniformGenerator;
import org.uncommons.maths.random.MersenneTwisterRNG;

import com.rng.distribution.Distribution;

public class DiscreteUniform extends Distribution{
	
	private DiscreteUniformGenerator gen;
	
	public DiscreteUniform(int min, int max){
        super(min, max);

        Random rng =  new MersenneTwisterRNG();
		gen = new DiscreteUniformGenerator(min, max, rng);
	}

    public DiscreteUniform(double mu, int stream) {
        super(mu, stream);
    }

	@Override
	public Double execute() {

        return Double.valueOf(gen.nextValue());
	}

}
