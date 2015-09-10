package com.algorithm;

import java.util.List;

import com.sim.objects.LightPath;

public abstract class SelectionLPAlgorithm {
	
	public abstract LightPath execute(List<LightPath> setOfLightPaths, double holdingTime);

}
