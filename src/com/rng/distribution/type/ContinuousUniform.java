package com.rng.distribution.type;

import java.util.Random;

import org.uncommons.maths.random.ContinuousUniformGenerator;
import org.uncommons.maths.random.MersenneTwisterRNG;

import com.rng.distribution.Distribution;

public class ContinuousUniform extends Distribution{
	
	private ContinuousUniformGenerator gen;
	
	public ContinuousUniform(double min, double max){
        super(min, max);

        Random rng =  new MersenneTwisterRNG();
		gen = new ContinuousUniformGenerator(min, max, rng);
	}

	@Override
	public Double execute() {
		return gen.nextValue();
	}
}
