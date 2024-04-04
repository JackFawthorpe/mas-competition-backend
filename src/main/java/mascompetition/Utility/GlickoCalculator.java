package mascompetition.Utility;

/**
 * Helper math functions for the Glick rating system
 */
public class GlickoCalculator {


    public static double INITIAL_RATING = 1500f;
    public static double INITIAL_RATING_DEVIATION = 350f;
    public static double INITIAL_VOLATILITY = 0.06f;
    public static double TAU = 0.5f;

    public static double TRANSITION_CONSTANT = 173.7178f;
    public static double CONVERGENCE_TOLERANCE = 0.000001f;

    public static double f(double x, double delta, double phi, double v, double volatility) {
        double a = a(volatility);
        double firstNumerator = Math.exp(x) * (delta * delta - phi * phi - v - Math.exp(x));
        double denomBracketed = phi * phi + v + Math.exp(x);
        double firstDenominator = 2 * denomBracketed * denomBracketed;
        double secondNumerator = x - a;
        double secondDenominator = TAU * TAU;

        return firstNumerator / firstDenominator - secondNumerator / secondDenominator;
    }

    public static double a(double volatility) {
        return Math.log(volatility * volatility);
    }

    public static double vContribution(double selfRating, double opponentRating, double opponentDeviation) {
        double expectedResult = E(mew(selfRating), mew(opponentRating), phi(opponentDeviation));
        double opponentG = g(phi(opponentDeviation));
        return opponentG * opponentG * expectedResult * (1 - expectedResult);
    }

    public static double E(double mew, double opponentMew, double opponentPhi) {
        return 1 / (1 + Math.exp(-g(opponentPhi) * (mew - opponentMew)));
    }

    public static double mew(double rating) {
        return (rating - 1500f) / TRANSITION_CONSTANT;
    }

    public static double phi(double deviation) {
        return deviation / TRANSITION_CONSTANT;
    }

    public static double g(double phi) {
        return 1f / (Math.sqrt(1 + 3 * phi * phi / (Math.PI * Math.PI)));
    }

    public static double deltaContribution(double selfRating, double opponentRating, double opponentDeviation, double score) {
        return g(phi(opponentDeviation)) * (score - E(mew(selfRating), mew(opponentRating), phi(opponentDeviation)));
    }

    public static double C(double A, double B, double fA, double fB) {
        return A + (A - B) * fA / (fB - fA);
    }
}
