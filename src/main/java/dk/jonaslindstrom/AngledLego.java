package dk.jonaslindstrom;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Comparator;
import java.util.List;
import java.util.stream.DoubleStream;

public class AngledLego {

    // The relative error for the triple (12, 12, 17) which has been used by LEGO is used as a threshold.
    private static final double DEFAULT_THRESHOLD = 0.001734606680942415;

    public static void main(String[] arguments) {

        // Default values.
        double maxX = 15;
        double maxY = 15;
        double threshold = DEFAULT_THRESHOLD;

        // Parse command line arguments.
        if (arguments.length == 3) {
            maxX = Double.parseDouble(arguments[0]);
            maxY = Double.parseDouble(arguments[1]);
            threshold = Double.parseDouble(arguments[2]);
        } else if (arguments.length == 2) {
            maxX = Double.parseDouble(arguments[0]);
            maxY = Double.parseDouble(arguments[1]);
        } else if (arguments.length == 0) {
            // Do nothing.
        } else {
            System.err.println("Usage: java AngledLego [maxX maxY threshold]");
            System.exit(1);
        }

        // Create all possible triples sorted by angle.
        List<Triple> triples = generateTriples(maxX, maxY, threshold);

        // Print LaTeX friendly list of triples.
        triples.forEach(System.out::println);
        System.out.println("Number of triples: " + triples.size());
    }

    /**
     * Generate all possible near Pythagorean triples (x, y, z) where x, y are positive integers or half-integers and x <= maxX,
     * y <= maxY and such that the relative distance from z to the nearest integer of half-integer is less than the threshold.
     */
    private static List<Triple> generateTriples(double maxX, double maxY, double threshold) {
        // Create all possible triples sorted by angle.
        return DoubleStream.iterate(0.5, x -> x <= maxX, x -> x + 0.5).boxed()
                .flatMap(x -> DoubleStream.iterate(0.5, y -> y <= Math.min(maxY, x), y -> y + 0.5)
                        .mapToObj(y -> new Triple(x, y)))
                .filter(triple -> triple.isValid(threshold)).sorted(Comparator.comparingDouble(Triple::getAngle)).toList();
    }

    /**
     * This represents a near Pythagorean triple.
     */
    private record Triple(double x, double y) {

        private static final DecimalFormatSymbols DECIMAL_POINT = DecimalFormatSymbols.getInstance();
        private static final DecimalFormat FORMAT = new DecimalFormat("#.#", DECIMAL_POINT);

        static {
            DECIMAL_POINT.setDecimalSeparator('.');
        }

        /**
         * Get the length of the hypotenuse.
         */
        private double getHypotenuse() {
            return Math.sqrt(x * x + y * y);
        }

        /**
         * Returns the angle of the vector (x, y) in degrees.
         */
        private double getAngle() {
            return Math.atan2(y, x) * 180 / Math.PI;
        }

        /**
         * Returns true if the triple is valid, i.e. the relative error is less than the threshold.
         */
        private boolean isValid(double threshold) {
            return getRelativeError() <= threshold;
        }

        /**
         * Returns the relative error of the triple.
         */
        private double getRelativeError() {
            double z = getHypotenuse();
            double error_integer = Math.abs(z - Math.round(z)) / z;
            double error_half_integer = Math.abs(2 * z - Math.round(2 * z)) / z;
            return Math.min(error_integer, error_half_integer);
        }

        public String toString() {
            // Output is in LaTeX friendly format.
            return FORMAT.format(getAngle()) +
                    "°&: (" +
                    FORMAT.format(x) +
                    ", " +
                    FORMAT.format(y) +
                    ", " +
                    FORMAT.format(getHypotenuse()) +
                    "),\\newline";
        }
    }
}
