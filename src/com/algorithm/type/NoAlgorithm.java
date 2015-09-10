package com.algorithm.type;

import java.util.List;

import com.algorithm.SelectionLPAlgorithm;
import com.sim.objects.LightPath;

public class NoAlgorithm extends SelectionLPAlgorithm {

	@Override
	public LightPath execute(List<LightPath> setOfLightPaths, double holdingTime) {

		return setOfLightPaths.get(0);
	}

}
