package mascompetition.unit.Utility;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static mascompetition.Utility.GlickoCalculator.*;

public class GlickoCalculatorTest {

    @Test
    void mew_tests() {
        assertDouble(0.0, mew(1500));
        assertDouble(0.5756, mew(1600));
        assertDouble(-0.5756, mew(1400));
        assertDouble(2.8782, mew(2000));
        assertDouble(-2.8782, mew(1000));
    }

    void assertDouble(Double expected, Double value) {
        Assertions.assertTrue(Math.abs(expected - value) < 0.01, () -> String.format("expected %s actual %s", expected, value));
    }

    @Test
    void phi_tests() {
        assertDouble(2.0147, phi(350));
        assertDouble(0.5756, phi(100));
        assertDouble(0.2878, phi(50));
        assertDouble(2.8782, phi(500));
    }

    @Test
    void g_tests() {
        assertDouble(0.6717, g(2));
        assertDouble(0.8757, g(1));
        assertDouble(0.9640, g(0.5));
        assertDouble(0.5173, g(3));
    }


    @Test
    void E_tests() {
        assertDouble(0.5480, E(1.5, 1.3, 0.5));
        assertDouble(0.4519, E(1.3, 1.5, 0.5));
        assertDouble(0.6077, E(2.3, 1.8, 1));
    }

    @Test
    void a_tests() {
        assertDouble(1.3863, a(2));
        assertDouble(-5.6268, a(0.06));
        assertDouble(-4.2405, a(0.12));
        assertDouble(-2.4079, a(0.3));
    }

    @Test
    void f_tests() {
        assertDouble(-36.3371, f(3, 2, 0.2878, 2, 0.05));
        assertDouble(-35.9099, f(3, 5, 0.4, 2, 0.05));
        assertDouble(-7.5524, f(0.8, 5, 0.4, 2, 0.5));
    }

    @Test
    void vContribution_tests() {
        assertDouble(.1078, vContribution(1500, 1600, 350));
        assertDouble(.0314, vContribution(1400, 2000, 100));
    }

    @Test
    void deltaContribution_tests() {
        assertDouble(.3981, deltaContribution(1500, 1600, 350, 1));
        assertDouble(-0.03417, deltaContribution(1400, 2000, 100, 0));
        assertDouble(-.2709, deltaContribution(1500, 1600, 350, 0));
        assertDouble(.9189, deltaContribution(1400, 2000, 100, 1));
    }

    @Test
    void C_tests() {
        assertDouble(-2.3333, C(1, 3, 0.5, 0.8));
        assertDouble(4.0183, C(4, 2, 0.1, 11));
        assertDouble(0.3333, C(83, 21, 2, 0.5));
    }
}
