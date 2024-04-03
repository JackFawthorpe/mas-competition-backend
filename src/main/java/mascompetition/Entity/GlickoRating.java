package mascompetition.Entity;

import jakarta.persistence.*;
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

    private static final float kDefaultR = 1000.0f;
    private static final float kDefaultRD = 350.0f;
    private static final float kScale = 173.7178f;
    private static final float kSystemConst = 0.5f;
    private static final float kConvergence = 0.000001f;

    // Yucky math
    private static float kDefaultS = 0.06f;
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;
    @Column
    private float rating;
    @Column
    private float deviation;
    @Column
    private float volatility;

    public GlickoRating() {
        // JPA Empty Args Constructor
    }

    public static GlickoRating newRating() {
        return new GlickoRating(UUID.randomUUID(), kDefaultR, kDefaultRD, kDefaultS);
    }

    void updateRating(List<GlickoRating> opponents, List<Float> scores) {
        float[] gTable = new float[opponents.size()];
        float[] eTable = new float[opponents.size()];

        float invV = 0.0f;

        for (int i = 0; i < opponents.size(); i++) {
            GlickoRating opponent = opponents.get(i);
            float g = opponent.getG();
            float e = opponent.getE(this);

            gTable[i] = g;
            eTable[i] = e;
            invV += g * g * e * (1.0f - e);
        }

        float v = 1.0f / invV;

        float dInner = 0.0f;
        for (int i = 0; i < opponents.size(); i++) {
            dInner += gTable[i] * (scores.get(i) - eTable[i]);
        }

        float d = v * dInner;

        float sPrime = (float) Math.exp(Convergence(d, v, getP(), getS()) / 2.0f);
        float pPrime = 1.0f / (float) Math.sqrt((1.0f / (getP() * getP() + sPrime * sPrime)) + invV);
        float uPrime = getU() + pPrime * pPrime * dInner;

        deviation = pPrime * kScale;
        volatility = sPrime;
        rating = uPrime * kScale + kDefaultR;
    }


    private float getG() {
        double scale = getP() / Math.PI;
        return (float) (1.0f / Math.sqrt(1.0f + 3.0f * scale * scale));
    }


    private float getE(GlickoRating rating) {
        float exponent = -1.0f * getG() * (rating.getU() - this.getU());
        return 1.0f / (1.0f + (float) Math.exp(exponent));
    }

    private float Convergence(float d, float v, float p, float s) {
        float dS = d * d;
        float pS = p * p;
        float tS = kSystemConst * kSystemConst;
        float a = (float) Math.log(s * s);

        float A = a;
        float B;
        float bTest = dS - pS - v;

        if (bTest > 0) {
            B = (float) Math.log(bTest);
        } else {
            B = a - kSystemConst;
            while (getF(B, dS, pS, v, a, tS) < 0.0f) {
                B -= kSystemConst;
            }
        }

        float fA = getF(A, dS, pS, v, a, tS);
        float fB = getF(B, dS, pS, v, a, tS);
        while (Math.abs(B - A) > kConvergence) {
            float C = A + (A - B) * fA / (fB - fA);
            float fC = getF(C, dS, pS, v, a, tS);

            if (fC * fB < 0.0f) {
                A = B;
                fA = fB;
            } else {
                fA /= 2.0f;
            }

            B = C;
            fB = fC;
        }
        return A;
    }

    private float getP() {
        return deviation / kScale;
    }

    private float getS() {
        return volatility;
    }

    private float getU() {
        return (rating - kDefaultR) / kScale;
    }

    private float getF(float x, float dS, float pS, float v, float a, float tS) {
        float eX = (float) Math.exp(x);
        float num = eX * (dS - pS - v - eX);
        float den = pS + v + eX;

        return (num / (2.0f * den * den)) - ((x - a) / tS);
    }


}
