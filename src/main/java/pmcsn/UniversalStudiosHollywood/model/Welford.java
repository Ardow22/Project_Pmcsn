package pmcsn.UniversalStudiosHollywood.model;

public class Welford {

    private long n = 0;
    private double mean = 0.0;
    private double M2 = 0.0;

    public void add(double x) {
        n++;

        double delta = x - mean;
        mean += delta / n;
        double delta2 = x - mean;

        M2 += delta * delta2;
    }

    public long getCount() {
        return n;
    }

    //media campionaria
    public double getMean() {
        return mean;
    }

    public double getVariance() {
        if (n < 2) {
            return 0.0;
        }
        return M2 / (n - 1);
    }

    public double getStandardDeviation() {
        return Math.sqrt(getVariance());
    }

    public double getStandardError() {
        if (n == 0) {
            return 0.0;
        }
        return getStandardDeviation() / Math.sqrt(n);
    }
}
