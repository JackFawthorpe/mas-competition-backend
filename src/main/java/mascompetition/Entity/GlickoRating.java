package mascompetition.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

import static mascompetition.Utility.GlickoCalculator.*;

/**
 * JPA entity for the rating of an agent
 */
@Builder
@Entity
@Data
@AllArgsConstructor
@ToString
public class GlickoRating {

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

    public void cancelRatingChange() {
        primeMew = mew(this.rating);
        primePhi = phi(this.deviation);
        primeVolatility = this.volatility;
    }


    /**
     * Imma keep it real with you chief I don't know how the math behind this works all that well,
     * This is a word for word representation of the formula supplied here <a href="http://www.glicko.net/glicko/glicko2.pdf"/>
     *
     * @param opponents The list of opponents that the agent faced within the game (Should have 3 values)
     * @param scores    A list of 1.0 0.5 0.0 values that represent having more points, the same amount of points or less points than the
     *                  opponent of the same index
     */
    public void calculateNewRating(@NotNull List<GlickoRating> opponents, @NotNull List<Double> scores) {
        double mew = mew(this.rating);
        double phi = phi(this.deviation);

        double invV = 0;
        for (GlickoRating opponent : opponents) {
            invV += vContribution(this.rating, opponent.getRating(), opponent.getDeviation());
        }

        double v = 1 / invV;

        double delta = 0f;
        for (int i = 0; i < opponents.size(); i++) {
            delta += deltaContribution(this.rating, opponents.get(i).getRating(), opponents.get(i).getDeviation(), scores.get(i));
        }
        delta *= v;


        double a = a(this.volatility);
        double A = a;
        double B;

        if (delta * delta > phi * phi + v) {
            B = Math.log(delta * delta - phi * phi - v);
        } else {
            int k = 1;
            while (f(a - k * TAU, delta, phi, v, this.volatility) < 0) {
                k += 1;
            }
            B = a - k * TAU;
        }

        double fA = f(A, delta, phi, v, this.volatility);
        double fB = f(B, delta, phi, v, this.volatility);

        while (Math.abs(B - A) > CONVERGENCE_TOLERANCE) {
            double C = C(A, B, fA, fB);
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

        primePhi = primePhi(phiStar, v);
        primeMew = mew + primePhi * primePhi * delta / v;
    }

    public double getNextRating() {
        return TRANSITION_CONSTANT * primeMew + INITIAL_RATING;
    }

}
