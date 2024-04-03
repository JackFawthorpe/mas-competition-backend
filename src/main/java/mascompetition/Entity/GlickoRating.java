package mascompetition.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * JPA entity for the rating of an agent
 */
@Builder
@Entity
@Data
@AllArgsConstructor
public class GlickoRating {

    private static double INITIAL_RATING = 1500f;
    private static double INITIAL_RATING_DEVIATION = 350f;
    private static double INITIAL_VOLATILITY = 0.06f;
    private static double TAU = 0.5f;

    private static double TRANSITION_CONSTANT = 173.7178f;
    private static double CONVERGENCE_TOLERANCE = 0.000001f;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;
    @Column
    private double rating;
    @Column
    private double deviation;
    @Column
    private double volatility;

    @Transient
    private double primeVolatility;

    @Transient
    private double primePhi;

    @Transient
    private double primeMew;

    public GlickoRating() {
        // JPA Empty Args Constructor
    }

    public static GlickoRating newRating() {
        return GlickoRating.builder()
                .id(UUID.randomUUID())
                .rating(INITIAL_RATING)
                .deviation(INITIAL_RATING_DEVIATION)
                .volatility(INITIAL_VOLATILITY)
                .build();
    }

    public void updateRating() {
        this.rating = TRANSITION_CONSTANT * primeMew + INITIAL_RATING;
        this.deviation = TRANSITION_CONSTANT * primePhi;
        this.volatility = primeVolatility;
    }

    public void calculateNewRating(@NotNull List<GlickoRating> opponents, @NotNull List<Double> scores) {
        double mew = mew(this.rating);
        double phi = phi(this.deviation);

        double invV = 0;
        for (int i = 0; i < opponents.size(); i++) {
            GlickoRating opponent = opponents.get(i);
            double expectedResult = E(mew, mew(opponent.getRating()), opponent.phi(opponent.getDeviation()));
            double opponentG = g(phi(opponent.getDeviation()));
            invV += opponentG * opponentG * expectedResult * (1 - expectedResult);
        }

        double v = 1 / invV;

        double delta = 0f;
        for (int i = 0; i < opponents.size(); i++) {
            GlickoRating opponent = opponents.get(i);
            double score = scores.get(i);
            double expectedResult = E(mew, mew(opponent.getRating()), phi(opponent.getDeviation()));
            double opponentG = g(phi(opponent.getDeviation()));
            delta += opponentG * (score - expectedResult);
        }

        delta *= v;
        double a = a(this.volatility);
        double A = a;
        double B;
        if (delta * delta > phi * phi + v) {
            B = Math.log(delta * delta - phi * phi - v);
        } else {
            int k = 1;
            double x = a - k * TAU;
            while (f(x, delta, phi, v, this.volatility) < 0) {
                k += 1;
            }
            B = a - k * TAU;
        }

        double fA = f(A, delta, phi, v, this.volatility);
        double fB = f(B, delta, phi, v, this.volatility);

        while (Math.abs(B - A) > CONVERGENCE_TOLERANCE) {
            double C = A + (A - B) * fA / (fB - fA);
            double fC = f(C, delta, phi, v, this.volatility);
            if (fC * fB <= 0) {
                A = B;
                fA = fB;
            } else {
                fA = fA / 2;
            }
            B = C;
            fB = fC;
        }

        primeVolatility = Math.exp(A / 2);
        double phiStar = Math.sqrt(phi * phi + primeVolatility * primeVolatility);

        primePhi = 1 / (Math.sqrt((1 / (phiStar * phiStar)) + (1 / v)));
        primeMew = mew + primePhi * primePhi * delta / v;
    }


    private double mew(double rating) {
        return (rating - 1500f) / TRANSITION_CONSTANT;

    }

    private double phi(double deviation) {
        return deviation / TRANSITION_CONSTANT;
    }

    private double E(double mew, double opponentMew, double opponentPhi) {
        return 1 / (1 + Math.exp(-g(opponentPhi) * (mew - opponentMew)));
    }

    private double g(double deviation) {
        return 1f / (Math.sqrt(1 + 3 * deviation * deviation / (Math.PI * Math.PI)));
    }

    private double a(double volatility) {
        return Math.log(volatility * volatility);
    }

    private double f(double x, double delta, double deviation, double v, double volatility) {
        double a = a(volatility);
        double firstNumerator = Math.exp(x) * (delta * delta - deviation * deviation - v - Math.exp(x));
        double firstDenominator = 2 * (deviation * delta + v + Math.exp(x)) * (deviation * delta + v + Math.exp(x));
        double secondNumerator = x - a;
        double secondDenominator = TAU * TAU;

        return firstNumerator / firstDenominator - secondNumerator / secondDenominator;
    }
}
