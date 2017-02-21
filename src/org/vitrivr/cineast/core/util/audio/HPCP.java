package org.vitrivr.cineast.core.util.audio;

import org.vitrivr.cineast.core.util.fft.STFT;
import org.vitrivr.cineast.core.util.fft.Spectrum;

/**
 * This class can be used to calculate the Harmonic Pitch Class Profile (HPCP) of a Shor-Term Fourier Transform or a single
 * Spectrum using the approach outlined by E. Gomez in [1].
 *
 * Harmonic Pitch Class Profiles assign the frequencies in a power spectrum to the pitch classes on the equal tempered
 * scale. Depending on whether only semitones, 1/2 semitones or 1/3 semitones are considered, the HPCP feature vector
 * holds either 12, 24 or 36 entries.
 *
 * [1] Gómez, E. (2006). Tonal description of polyphonic audio for music content processing.
 *    INFORMS Journal on Computing, 18(3), 294–304. http://doi.org/10.1287/ijoc.1040.0126
 *
 * @see STFT
 * @see Spectrum
 *
 * @author rgasser
 * @version 1.0
 * @created 03.02.17
 */
public class HPCP {
    /**
     * Defines the resolution of the HPCP (full semitones, 1/2 semitones or 1/3 semitones).
     */
    public enum Resolution {
        FULLSEMITONE (12),
        HALFSEMITONE (24),
        THIRDSEMITON (36);

        public final int bins;

        Resolution(int bins) {
            this.bins = bins;
        }
    }

    /** Reference frequency in Hz. Corresponds to the musical note A (A440 or A4) above the middle C. */
    private static final double F_REF = 440.0000000000;

    /** Window-size parameter, which defaults to 4/3 semitones as per [1]. */
    private static final float WINDOW = 4f/3f;

    /** Float array holding the HPCP data. */
    private final float[] hpcp;

    /** Minimum frequency to consider. Defaults to 100Hz as per [1]. */
    private final float minFrequency;

    /** Maximum frequency to consider. Defaults to 5000Hz as per [1]. */
    private final float maxFrequency;

    /** Resolution of the HPCP. */
    private final Resolution resolution;

    /** Number of contributions to the HPCP. Used for normalization. */
    private int contributions = 0;

    /**
     * Calculates the center-frequency of a bin defined by its bin number. The bin numbers
     * are defined such that bin 0 corresponds to F_REF (440 Hz). An increase of the bin number
     * is equivalent to moving 1, 1/2 or 1/3 of a semitone up on the scale.
     *
     * @param n Desired bin number
     * @return r Resolution to use (1, 1/2, or 1/3)
     */
    public static double binToCenterFrequency(int n, Resolution r) {
        return F_REF*Math.pow(2, ((float)n/(float)r.bins));
    }

    /**
     * Default constructor. Creates an HPCP container for a full semitone, 12 entries HPCP that consider
     * frequencies between 100Hz and 5000Hz.
     */
    public HPCP() {
        this(Resolution.FULLSEMITONE, 100.0f, 5000.0f);
    }

    /**
     * Constructor for a custom HPCP container.
     *
     * @param resolution Desired resolution in semitones (1, 1/2 or 1/3)
     * @param minFrequency Minimum frequency to consider. Frequencies below that limit will be ignored.
     * @param maxFrequency Maximum frequency to consider. Frequencies above that limit will be ignored.
     */
    public HPCP(Resolution resolution, float minFrequency, float maxFrequency) {
        this.resolution = resolution;
        this.maxFrequency = maxFrequency;
        this.minFrequency = minFrequency;
        this.hpcp = new float[resolution.bins];
    }

    /**
     * Adds the contribution of a STFT to the HPCP by adding the contributions all the power spectra
     * of that STFT to the HPCP.
     *
     * @param stft STFT that should contribute to the Harmonic Pitch Class Profile.
     */
    public void addContribution(STFT stft) {
        for (Spectrum spectrum : stft.getPowerSpectrum()) {
            this.addContribution(spectrum);
        }
    }

    /**
     * Adds the contribution of a single spectrum to the HPCP. The spectrum can either be a power
     * or magnitude spectrum.
     *
     * @param spectrum Spectrum that should contribute to the Harmonic Pitch Class Profile.
     */
    public void addContribution(Spectrum spectrum) {
        /* Prune the PowerSpectrum and the Frequencies to the range that is interesting according to min frequency and max frequency.*/
        Spectrum pruned = spectrum.reduced(this.minFrequency, this.maxFrequency);

        int[] peaks = pruned.findLocalMaxima(0.5);
        float[] hpcp = new float[this.resolution.bins];

        /* For each of the semi-tones (according to resolution), add the contribution of every peak. */
        float max = 0.0f;
        for (int n=0;n<this.resolution.bins;n++) {
            for (int peak : peaks) {
                if (pruned.getType() == Spectrum.Type.POWER) {
                    hpcp[n] += pruned.getValue(peak) * this.weight(n, pruned.getFrequeny(peak));
                } else if (pruned.getType() == Spectrum.Type.MAGNITUDE) {
                    hpcp[n] += Math.pow(pruned.getValue(peak),2) * this.weight(n, pruned.getFrequeny(peak));
                }
            }
            max = Math.max(max, hpcp[n]);
        }

        /* Normalize the resulting HPCP contribution so that the highest entry becomes 1.0. */
        if (max > 0.0f) {
            for (int n = 0; n < this.resolution.bins; n++) {
                this.hpcp[n] += hpcp[n] / max;
            }
        }

        /* Increases the contributions counter. */
        this.contributions += 1;
    }

    /**
     * Returns the raw, un-normalized HPCP float array.
     *
     * @return Float array containing the HPCP.
     */
    public float[] getHpcp() {
        return hpcp;
    }

    /**
     * Returns the normalized HPCP array (i.e. the HPCP array that has been divided
     * by the number of contributions).
     *
     * @return Float array containing the normalized HPCP.
     */
    public float[] getNormalizedHpcp() {
        float[] normalized = new float[this.hpcp.length];
        for (int i=0;i<this.hpcp.length;i++) {
            normalized[i] = this.hpcp[i]/(this.contributions+1);
        }
        return normalized;
    }

    /**
     * Calculates the contribution of a given frequency (usually from a spectrum) to
     * a given bin in the HPCP.
     *
     * @param n Number of the bin.
     * @param frequency Frequency to calculate the contribution for.
     * @return Contribution of the frequency to the bin.
     */
    private double weight(int n, double frequency) {
        double frequency_n = binToCenterFrequency(n, this.resolution);


        double p = Math.log(frequency/frequency_n)/Math.log(2);

        int m1 = (int) Math.ceil(p) * (-1);
        int m2 = (int) Math.floor(p) * (-1);


        double distance = this.resolution.bins*Math.min(Math.abs(p + m1), Math.abs(p + m2));


        if (distance > 0.5 * WINDOW) {
            return 0;
        } else {
            return Math.pow(Math.cos((Math.PI/2) * (distance/(0.5 * WINDOW))),2);
        }
    }
}