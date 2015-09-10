package com.algorithm.type;

import java.util.List;

import com.algorithm.SelectionLPAlgorithm;
import com.launcher.Launcher;
import com.sim.objects.Connection;
import com.sim.objects.LightPath;

public class MAL_Method extends SelectionLPAlgorithm {

    @Override
    public LightPath execute(List<LightPath> setOfCandidateLightPaths, double holdingTime) {

        double maxCost = 0;
        LightPath lpWithMinCost = null;

        for (LightPath lp : setOfCandidateLightPaths) {
            if (lp.getSetOfConnections().size() == 0) continue;
            double cost = calculateHmax(lp);
            if (cost > maxCost) {
                lpWithMinCost = lp;
                maxCost = cost;
            }
        }

        if (lpWithMinCost == null)
            lpWithMinCost = selectLightPathWithLessHops(setOfCandidateLightPaths);

        return lpWithMinCost;
    }

    public LightPath selectLightPathWithLessHops(List<LightPath> setOfCandidateLightPaths) {

        double numOfHops = Double.MAX_VALUE;
        LightPath lpWithMinCost = null;

        for (LightPath lp : setOfCandidateLightPaths)
            if (lp.getPath().getTraversedEdges().size() < numOfHops) {
                numOfHops = lp.getPath().getTraversedEdges().size();
                lpWithMinCost = lp;
            }
        return lpWithMinCost;
    }

    public static double calculateHmax(LightPath lp) {

        double Hmax;
        double hmaxForUnknown = 0;
        double hmaxForKnown;

        /** Calculate h max for known connections*/
        hmaxForKnown = hmaxKnown(lp);

        /** Calculate h max for unknown connections*/
        switch (Launcher.getDistributionTypeForHT()) {
            case "Exp":
                hmaxForUnknown = hmaxUnknownForExp(lp);
                break;
            case "HyperExp":
                hmaxForUnknown = hmaxUnknownForHExp(lp);
                break;
            case "LogN":
                hmaxForUnknown = hmaxUnknownForLogNormal(lp);
                break;
        }

        Hmax = Math.max(hmaxForKnown, hmaxForUnknown);

        return Hmax / lp.getPath().getTraversedEdges().size();
    }

    public static double calculateCost(LightPath lp, double holdingTime) {

        double cost;
        double E = 1e-5;
        double Hmax;
        double hmaxForUnknown = 0;
        double hmaxForKnown = 0;
        double hp = lp.getPath().getTraversedEdges().size();
        ;

        /**If new lightpath*/
        if (lp.getSetOfConnections().size() == 0) {
            cost = hp * holdingTime;
        } /** If not...*/
        else {
            /** Calculate h max for known connections*/
            hmaxForKnown = hmaxKnown(lp);

            /** Calculate h max for unknown connections*/
            switch (Launcher.getDistributionTypeForHT()) {
                case "Exp":
                    hmaxForUnknown = hmaxUnknownForExp(lp);
                    break;
                case "HyperExp":
                    hmaxForUnknown = hmaxUnknownForHExp(lp);
                    break;
                case "LogN":
                    hmaxForUnknown = hmaxUnknownForLogNormal(lp);
                    break;
            }

            Hmax = Math.max(hmaxForKnown, hmaxForUnknown);

            if (Hmax >= holdingTime) {
                cost = hp * E;
            } else {
                double deltaT = holdingTime - Hmax;
                cost = (hp * E) + (hp * deltaT);
            }
        }

        return cost;
    }


    public static double hmaxKnown(LightPath lp) {

        double hmax = 0;

        for (Connection con : lp.getSetOfConnections())
            if (!con.isNotKnown())
                if (con.getResidualTime() > hmax)
                    hmax = con.getResidualTime();

        return hmax;
    }

    public static double hmaxUnknownForExp(LightPath lp) {

        double hmax = 0;
        int n = 0;
        double ht = Launcher.getMeanHoldingTime();

        for (Connection con : lp.getSetOfConnections())
            if (con.isNotKnown())
                n++;

        if (n == 1)
            hmax = Launcher.getMeanHoldingTime();
        for (int i = 0; i < n - 1; i++) {
            int combinations = combination(n - 1, i);
            hmax += n * combinations * Math.pow(-1, i) * Math.pow((1 + i), -2);
            hmax = hmax * ht;
        }

        return hmax;
    }

    public static int combination(int n, int k) {
        return permutation(n) / (permutation(k) * permutation(n - k));
    }

    public static int permutation(int i) {
        if (i == 1 || i == 0) {
            return 1;
        }
        return i * permutation(i - 1);
    }

    public static double hmaxUnknownForHExp(LightPath lp) {

        double hmax = 0;
        TrapezoidalRuleForHyperExp.setForhmin(false);
        if (lp.getNumberOfUnknownConnections() != 0)
            hmax = TrapezoidalRuleForHyperExp.integrate(lp.getSetOfConnections().get(0)
                    .getStartingTime(), Double.MAX_VALUE, 10000, lp);
        return hmax;
    }

    private static double hmaxUnknownForLogNormal(LightPath lp) {

        double cov = Launcher.getCov();
        double sigma = Math.sqrt(Math.log(1 + Math.pow(cov, 2)));
        double mu = Math.log(1 / Launcher.getMeanHoldingTime()) - (Math.log(1 + Math.pow(cov, 2)) / 2.0);
        new TrapezoidalRuleForLogNormal(sigma, mu);
        double hmax = 0;
        TrapezoidalRuleForLogNormal.setForhmin(false);
        if (lp.getNumberOfUnknownConnections() != 0)
            hmax = TrapezoidalRuleForLogNormal.integrate(lp.getSetOfConnections().get(0)
                    .getStartingTime(), Double.MAX_VALUE, 10000, lp);
        return hmax;
    }

}
