package mascompetition.unit.Entity;

import mascompetition.Entity.GlickoRating;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class GlickoRatingTest {

    /**
     * This is the example from http://www.glicko.net/glicko/glicko2.pdf
     */
    @Test
    void test() {
        GlickoRating player = GlickoRating.builder()
                .rating(1500)
                .deviation(200)
                .volatility(0.06f)
                .build();

        List<GlickoRating> opponents = List.of(
                GlickoRating.builder()
                        .rating(1400)
                        .deviation(30)
                        .volatility(0.06f) // This doesn't affect our players rating
                        .build(),
                GlickoRating.builder()
                        .rating(1550)
                        .deviation(100)
                        .volatility(0.06f) // This doesn't affect our players rating
                        .build(),
                GlickoRating.builder()
                        .rating(1700)
                        .deviation(300)
                        .volatility(0.06f) // This doesn't affect our players rating
                        .build()
        );

        List<Double> scores = List.of(1.0, 0.0, 0.0);
        player.calculateNewRating(opponents, scores);
        Assertions.assertEquals(1500.0, player.getRating()); // It shouldn't change until an update is triggered
        Assertions.assertEquals(200.0, player.getDeviation()); // It shouldn't change until an update is triggered
        player.updateRating();
        Assertions.assertTrue(Math.abs(player.getRating() - 1464.06) < 0.01);
        Assertions.assertTrue(Math.abs(player.getDeviation() - 151.52) < 0.01);
    }

}
