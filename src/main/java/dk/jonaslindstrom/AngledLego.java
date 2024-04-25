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
        double stepX = 0.5;
        double maxX = 15;
        double stepY = 0.5;
        double maxY = 15;
        double threshold = DEFAULT_THRESHOLD;

        // Parse command line arguments.
        if (arguments.length == 5) {
            maxX = Double.parseDouble(arguments[0]);
            stepX = Double.parseDouble(arguments[1]);
            maxY = Double.parseDouble(arguments[2]);
            stepY = Double.parseDouble(arguments[3]);
            threshold = Double.parseDouble(arguments[4]);
        } else if (arguments.length == 4) {
            maxX = Double.parseDouble(arguments[0]);
            stepX = Double.parseDouble(arguments[1]);
            maxY = Double.parseDouble(arguments[2]);
            stepY = Double.parseDouble(arguments[3]);
        } else if (arguments.length == 0) {
            // Do nothing.
        } else {
            System.err.println("Usage: java AngledLego [maxX stepX maxY stepY [threshold]]");
            System.exit(1);
        }

        // Create all possible triples sorted by angle.
        List<Triple> triples = generateTriples(maxX, stepX, maxY, stepY, threshold);

        // Print LaTeX friendly list of triples.
        triples.forEach(System.out::println);
        System.out.println("Number of triples: " + triples.size());
    }

    /**
     * Generate all possible near Pythagorean triples (x, y, z) where x, y are positive integers or half-integers and x <= maxX,
     * y <= maxY and such that the relative distance from z to the nearest integer of half-integer is less than the threshold.
     */
    private static List<Triple> generateTriples(double maxX, double stepX, double maxY, double stepY, double threshold) {
        // Create all possible triples sorted by angle.
        return DoubleStream.iterate(stepX, x -> x <= maxX, x -> x + stepX).boxed()
                .flatMap(x -> DoubleStream.iterate(stepY, y -> y <= Math.min(maxY, x), y -> y + stepY)
                        .mapToObj(y -> new Triple(x, y)))
                .filter(triple -> triple.isValid(threshold)).sorted(Comparator.comparingDouble(Triple::getAngle)).toList();
    }

    /**
     * This represents a near Pythagorean triple.
     */
    private record Triple(double x, double y) {

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
            DecimalFormatSymbols decimal_point = DecimalFormatSymbols.getInstance();
            decimal_point.setDecimalSeparator('.');
            DecimalFormat format = new DecimalFormat("#.#", decimal_point);

            // Output is in LaTeX friendly format.
            return format.format(getAngle()) +
                    "°&: (" +
                    format.format(x) +
                    ", " +
                    format.format(y) +
                    ", " +
                    format.format(getHypotenuse()) +
                    "),\\newline";
        }
    }
}
