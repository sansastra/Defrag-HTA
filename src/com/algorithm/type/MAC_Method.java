package com.algorithm.type;

import java.util.LinkedList;
import java.util.List;

import com.algorithm.SelectionLPAlgorithm;
import com.launcher.Launcher;
import com.sim.objects.Connection;
import com.sim.objects.LightPath;

public class MAC_Method extends SelectionLPAlgorithm {

    @Override
    public LightPath execute(List<LightPath> setOfCandidateLightPaths,
                             double holdingTime) {

        double minCost = Double.MAX_VALUE;
        LightPath lpWithMinCost = null;

        for (LightPath lp : setOfCandidateLightPaths) {
            if (lp.getSetOfConnections().size() == 0) continue;
            double cost = calculateHmin(lp);
            if (cost < minCost) {
                lpWithMinCost = lp;
                minCost = cost;
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

    public static double calculateHmin(LightPath lp) {

        double Hmin;
        double hminForUnknown = Double.MAX_VALUE;
        double hminForKnown = Double.MAX_VALUE;

        /** Calculate h min and bw for known connections*/
        Connection knownConnection = hminKnown(lp);
        if (knownConnection != null)
            hminForKnown = knownConnection.getResidualTime();

        /** Calculate h min and bw for unknown connections*/
        switch (Launcher.getDistributionTypeForHT()) {
            case "Exp":
                hminForUnknown = hminUnknownForExp(lp);
                break;
            case "HyperExp":
                hminForUnknown = hminUnknownForHExp(lp);
                break;
            case "LogN":
                hminForUnknown = hminUnknownForLogNormal(lp);
                break;
        }
        /** Select Hmin */
        Hmin = Math.min(hminForKnown, hminForUnknown);

        return lp.getPath().getTraversedEdges().size() * Hmin;
    }

    public static double calculateCost(LightPath lp) {

        double cost = Double.MAX_VALUE;
        double E = 1e-5;
        double Hmin = 0;
        double hminForUnknown = Double.MAX_VALUE;
        double hminForKnown = Double.MAX_VALUE;
        double bwForUnknown = Double.MAX_VALUE;
        double bwForKnown = Double.MAX_VALUE;
        Connection knownConnection;

        /**If new lightpath*/
        if (lp.getSetOfConnections().size() == 0) {
            cost = 1.0 / E;
        }
        /** If not...*/
        else {
            /** Calculate h min and bw for known connections*/
            knownConnection = hminKnown(lp);
            if (knownConnection != null) {
                hminForKnown = knownConnection.getResidualTime();
                bwForKnown = knownConnection.getBw();
            }

            /** Calculate h min and bw for unknown connections*/
            switch (Launcher.getDistributionTypeForHT()) {
                case "Exp":
                    hminForUnknown = hminUnknownForExp(lp);
                    break;
                case "HyperExp":
                    hminForUnknown = hminUnknownForHExp(lp);
                    break;
                case "LogN":
                    hminForUnknown = hminUnknownForLogNormal(lp);
                    break;
            }
            bwForUnknown = getMinBwForUnknown(lp);

            /** Select Hmin */
            Hmin = Math.min(hminForKnown, hminForUnknown);

            /** If Hmin is minimum for known*/
            if (Hmin == hminForKnown)
//                cost = bwForKnown * Hmin;
                cost = Hmin / bwForKnown;
//                cost =  NetworkParameters.getMaxCarrierBW() - bwForKnown;
            /** If Hmin is minimum for unknown*/
            else if (Hmin == hminForUnknown)
//                cost = bwForUnknown * Hmin;
                cost = Hmin / bwForUnknown;
//            cost =  NetworkParameters.getMaxCarrierBW() - bwForUnknown;

//            cost = (1.0 / cost) + Hmin;
        }

        cost = cost * lp.getPath().getTraversedEdges().size();
        return cost;
    }

    public static Connection hminKnown(LightPath lp) {

        Connection minConnection = null;
        double hmin = Double.MAX_VALUE;

        for (Connection con : lp.getSetOfConnections()) {
            if (!con.isNotKnown())
                if (con.getResidualTime() < hmin) {
                    hmin = con.getResidualTime();
                    minConnection = con;
                }
        }

        return minConnection;
    }

    public static double hminUnknownForExp(LightPath lp) {

        int n = 0;
        double hminForUnknown;
        double ht = Launcher.getMeanHoldingTime();

        for (Connection con : lp.getSetOfConnections())
            if (con.isNotKnown())
                n++;

        hminForUnknown = ht / n;

        return hminForUnknown;
    }

    public static double getMinBwForUnknown(LightPath lp) {

        double minBW = Double.MAX_VALUE;

        for (Connection con : lp.getSetOfConnections())
            if (con.isNotKnown())
                if (con.getBw() < minBW)
                    minBW = con.getBw();

        return minBW;
    }

    public static double hminUnknownForHExp(LightPath lp) {

        double hmin = Double.MAX_VALUE;
        TrapezoidalRuleForHyperExp.setForhmin(true);
        if (lp.getNumberOfUnknownConnections() != 0)
            hmin = TrapezoidalRuleForHyperExp.integrate(lp.getSetOfConnections().get(0)
                    .getStartingTime(), Double.MAX_VALUE, 10000, lp);

        return hmin;
    }

    private static double hminUnknownForLogNormal(LightPath lp) {

        double cov = Launcher.getCov();
        double sigma = Math.sqrt(Math.log(1 + Math.pow(cov, 2)));
        double mu = Math.log(1 / Launcher.getMeanHoldingTime()) - (Math.log(1 + Math.pow(cov, 2)) / 2.0);
        new TrapezoidalRuleForLogNormal(sigma, mu);

        double hmin = Double.MAX_VALUE;
        TrapezoidalRuleForLogNormal.setForhmin(true);
        if (lp.getNumberOfUnknownConnections() != 0)
            hmin = TrapezoidalRuleForLogNormal.integrate(lp.getSetOfConnections().get(0)
                    .getStartingTime(), 1000000, 10000, lp);

        return hmin;
    }
}
