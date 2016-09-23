/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.analytics;

import com.google.wireless.android.sdk.stats.AndroidStudioStats;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static org.junit.Assert.*;

public class PercentilesTest {

  private static final int NUM_SAMPLES = 10000;
  private static final int RAW_DATA_SIZE = 80;

  private static final Random r = new Random(349855);

  @Test
  public void constantDistributionTest() {
    double[] targets = {0.5};
    Percentiles p = new Percentiles(targets, RAW_DATA_SIZE);
    for (int i = 0; i < NUM_SAMPLES; ++i) {
      p.addSample(1.0);
    }

    assertEquals(1.0, p.getApproximateValue(0.5), 0.01);
  }

  @Test
  public void uniformDistributionTest() {
    double[] targets = {0.25, 0.5, 0.75};
    Percentiles p = new Percentiles(targets, RAW_DATA_SIZE);
    for (int i = 0; i < NUM_SAMPLES; ++i) {
      p.addSample(r.nextDouble());
    }

    assertEquals(0.25, p.getApproximateValue(0.25), 0.01);
    assertEquals(0.5, p.getApproximateValue(0.5), 0.01);
    assertEquals(0.75, p.getApproximateValue(0.75), 0.01);
  }

  @Test
  public void bimodalDistributionTest() {
    double[] targets = {0.5};
    Percentiles p = new Percentiles(targets, RAW_DATA_SIZE);
    for (int i = 0; i < NUM_SAMPLES; ++i) {
      double d = r.nextDouble();
      double v;
      if (d < 0.45) {
        v = 1.0;
      } else if (d < 0.55) {
        v = 2.0;
      } else {
        v = 3.0;
      }
      p.addSample(v);
    }

    assertEquals(2.0, p.getApproximateValue(0.5), 0.05);
  }

  @Test
  public void gaussianDistributionTest() {
    double[] targets = {0.5};
    Percentiles p = new Percentiles(targets, RAW_DATA_SIZE);
    for (int i = 0; i < NUM_SAMPLES; ++i) {
      double d = r.nextGaussian();
      p.addSample(d + 6.0);
    }

    assertEquals(6.0, p.getApproximateValue(0.5), 0.03);
  }

  @Test
  public void gaussianMultiplePointsTest() {
    double[] targets = {0.4, 0.5, 0.6};
    Percentiles p = new Percentiles(targets, RAW_DATA_SIZE);
    ArrayList<Double> entries = new ArrayList<>(NUM_SAMPLES);
    for (int i = 0; i < NUM_SAMPLES; ++i) {
      double d = r.nextGaussian() + 6.0;
      p.addSample(d);
      entries.add(d);
    }

    Collections.sort(entries);

    double actual40th = entries.get((int) (0.4 * entries.size()));
    double actual50th = entries.get((int) (0.5 * entries.size()));
    double actual60th = entries.get((int) (0.6 * entries.size()));

    assertEquals(actual40th, p.getApproximateValue(0.4), 0.03);
    assertEquals(actual50th, p.getApproximateValue(0.5), 0.03);
    assertEquals(actual60th, p.getApproximateValue(0.6), 0.03);
  }

  private static ArrayList<Double> gaussianSamples(int numSamples, ArrayList<Double> samples) {
    if (samples == null) {
      samples = new ArrayList<>(numSamples);
    }
    for (int i = 0; i < numSamples; ++i) {
      samples.add(r.nextGaussian());
    }
    return samples;
  }

  private static Percentiles createGaussianEstimator(double[] targets, int numSamples, ArrayList<Double> samples) {
    samples = gaussianSamples(numSamples, samples);
    return createEstimator(targets, samples);
  }

  private static Percentiles createEstimator(double[] targets, ArrayList<Double> samples) {
    Percentiles p = new Percentiles(targets, RAW_DATA_SIZE);
    for (Double d : samples) {
      p.addSample(d);
    }
    return p;
  }

  @Test
  public void mergeTest() {
    double[] targets = {0.5};
    ArrayList<Percentiles> estimators = new ArrayList<>(500);
    ArrayList<Double> allSamples = new ArrayList<>();
    double worstEstimation = 0.0;
    for (int i = 0; i < 500; ++i) {
      ArrayList<Double> samples = new ArrayList<>();
      Percentiles p = createGaussianEstimator(targets, NUM_SAMPLES, samples);
      allSamples.addAll(samples);
      double medianEstimation = p.getApproximateValue(0.5);
      Collections.sort(samples);
      double actualMedian = samples.get(samples.size() / 2);
      worstEstimation = Math.max(Math.abs(actualMedian - medianEstimation), worstEstimation);
      estimators.add(p);
    }

    Percentiles merged;
    try {
      merged = Percentiles.merge(targets, estimators, RAW_DATA_SIZE);
    } catch(Percentiles.MergeException e) {
      fail();
      return;
    }

    Collections.sort(allSamples);
    double actualMedian = allSamples.get(allSamples.size() / 2);

    assertEquals(actualMedian, merged.getApproximateValue(0.5), worstEstimation + 0.01);
  }

  @Test
  public void smallEstimationMerge() {
    // Small estimations are bad. They do not have a chance to normalize to the actual distribution.
    // This can be mitigated by allowing more raw samples to accumulate before forming the estimation.
    // Here all the estimators will not be interpolated yet, so the merge is the same as building an estimation from samples.
    // Note that the sample order may be different, so estimations may be slightly different.
    double[] targets = {0.25, 0.5, 0.75};
    final int NUM_ESTIMATORS = 500;
    ArrayList<Percentiles> estimators = new ArrayList<>(NUM_ESTIMATORS);
    ArrayList<Double> samples = new ArrayList<>(NUM_ESTIMATORS * 20);
    int numSamples = 20;
    for (int i = 0; i < NUM_ESTIMATORS; ++i) {
      ArrayList<Double> s = gaussianSamples(numSamples, null);
      samples.addAll(s);
      Percentiles p = createEstimator(targets, s);
      estimators.add(p);
    }

    Percentiles singleEstimator = createEstimator(targets, samples);

    Collections.sort(samples);
    double actualMedian = samples.get(samples.size() / 2);

    Percentiles merged;
    try {
      merged = Percentiles.merge(targets, estimators, RAW_DATA_SIZE);
    } catch (Percentiles.MergeException e) {
      fail();
      return;
    }
    assertEquals(actualMedian, merged.getApproximateValue(0.5), 0.01);

    // The estimations should be EXACTLY the same. Each of the smaller estimators does not actually
    // interpolate the data, and it gets fed in the same order when merged.
    assertEquals(singleEstimator.getApproximateValue(0.5), merged.getApproximateValue(0.5), 0.00);
  }

  @Test
  public void subdistibutionsMerge() {
    // Merge abs(normal) and -abs(normal) distributions.
    // Note that in this case, the max in a -abs(normal) distribution will be < the min of an abs(normal) distribution,
    // and merge needs to handle that properly.
    double[] targets = {0.5};
    ArrayList<Double> samples = new ArrayList<>();
    ArrayList<Percentiles> estimators = new ArrayList<>(100);
    for (int i = 0; i < 100; ++i) {
      ArrayList<Double> subsamples = gaussianSamples(NUM_SAMPLES, null);
      if (i % 2 == 0) {
        for (int j = 0; j < subsamples.size(); ++j) {
          subsamples.set(j, Math.abs(subsamples.get(j)));
        }
      } else {
        for (int j = 0; j < subsamples.size(); ++j) {
          subsamples.set(j, -Math.abs(subsamples.get(j)));
        }
      }
      samples.addAll(subsamples);
      estimators.add(createEstimator(targets, subsamples));
    }

    Collections.sort(samples);
    double actualMedian = samples.get(samples.size() / 2);

    Percentiles merged;
    try {
      merged = Percentiles.merge(targets, estimators, RAW_DATA_SIZE);
    } catch (Percentiles.MergeException e) {
      fail();
      return;
    }

    assertEquals(actualMedian, merged.getApproximateValue(0.5), 0.01);
  }

  @Test
  public void skewSubdistributionsMerge() {
    // Merge abs(normal) and -abs(normal) subdistributions again, but this time with uneven
    // numbers of each. The median falls somewhere on the side with more estimators, forcing
    // interpolation.
    double[] targets = {0.5};
    // Increment by 3 to save time.
    for (int nNegativeEstimators = 0; nNegativeEstimators < 100; nNegativeEstimators += 3) {
      ArrayList<Double> samples = new ArrayList<>();
      ArrayList<Percentiles> estimators = new ArrayList<>();
      for (int i = 0; i < 100 - nNegativeEstimators; ++i) {
        ArrayList<Double> subsamples = gaussianSamples(NUM_SAMPLES, null);
        for (int j = 0; j < subsamples.size(); ++j) {
          subsamples.set(j, Math.abs(subsamples.get(j)));
        }
        samples.addAll(subsamples);
        estimators.add(createEstimator(targets, subsamples));
      }
      for (int i = 0; i < nNegativeEstimators; ++i) {
        ArrayList<Double> subsamples = gaussianSamples(NUM_SAMPLES, null);
        for (int j = 0; j < subsamples.size(); ++j) {
          subsamples.set(j, -Math.abs(subsamples.get(j)));
        }
        samples.addAll(subsamples);
        estimators.add(createEstimator(targets, subsamples));
      }

      Collections.sort(samples);
      double actualMedian = samples.get(samples.size() / 2);

      Percentiles merged;
      try {
        merged = Percentiles.merge(targets, estimators, RAW_DATA_SIZE);
      } catch (Percentiles.MergeException e) {
        fail();
        return;
      }

      assertEquals(actualMedian, merged.getApproximateValue(0.5), 0.01);
    }
  }

  @Test
  public void shiftedSubdistributionsMerge() {
    // Randomly shift subdistributions and merge them together.
    double[] targets = {0.5};
    ArrayList<Double> samples = new ArrayList<>();
    ArrayList<Percentiles> estimators = new ArrayList<>();
    for (int i = 0; i < 100; ++i) {
      ArrayList<Double> subsamples = gaussianSamples(NUM_SAMPLES, null);
      double mean = r.nextDouble();
      for (int j = 0; j < subsamples.size(); ++j) {
        subsamples.set(j, mean + subsamples.get(j));
      }
      samples.addAll(subsamples);
      estimators.add(createEstimator(targets, subsamples));
    }

    Collections.sort(samples);
    double actualMedian = samples.get(samples.size() / 2);

    Percentiles merged;
    try {
      merged = Percentiles.merge(targets, estimators, RAW_DATA_SIZE);
    } catch (Percentiles.MergeException e) {
      fail();
      return;
    }

    assertEquals(actualMedian, merged.getApproximateValue(0.5), 0.01);
  }

  @Test
  public void tailQuantile() {
    // Test accuracy at the tails, which will be generally worse.
    double[] targets = {0.01, 0.99};
    Percentiles p = new Percentiles(targets, RAW_DATA_SIZE);
    ArrayList<Double> samples = gaussianSamples(NUM_SAMPLES, null);
    for (Double s : samples) {
      p.addSample(s);
    }

    Collections.sort(samples);
    double actual1st = samples.get((int) (samples.size() * 0.01));
    double actual99th = samples.get((int) (samples.size() * 0.99));

    assertEquals(actual1st, p.getApproximateValue(0.01), 0.2);
    assertEquals(actual99th, p.getApproximateValue(0.99), 0.2);
  }

  @Test
  public void mergeTailQuantile() {
    double[] targets = {0.01, 0.99};
    ArrayList<Percentiles> estimators = new ArrayList<>();
    ArrayList<Double> samples = new ArrayList<>();
    for (int i = 0; i < 100; ++i) {
      ArrayList<Double> subsamples = new ArrayList<>();
      estimators.add(createGaussianEstimator(targets, NUM_SAMPLES, subsamples));
      samples.addAll(subsamples);
    }

    Collections.sort(samples);
    double actual1st = samples.get((int) (samples.size() * 0.01));
    double actual99th = samples.get((int) (samples.size() * 0.99));

    Percentiles merged;
    try {
      merged = Percentiles.merge(targets, estimators, RAW_DATA_SIZE);
    } catch (Percentiles.MergeException e) {
      fail();
      return;
    }

    assertEquals(actual1st, merged.getApproximateValue(0.01), 0.3);
    assertEquals(actual99th, merged.getApproximateValue(0.99), 0.3);
  }

  @Test
  public void mergeShiftedTailQuantile() {
    double[] targets = {0.005, 0.01, 0.99, 0.995};
    ArrayList<Percentiles> estimators = new ArrayList<>();
    ArrayList<Double> allSamples = new ArrayList<>();
    for (int i = 0; i < 100; ++i) {
      ArrayList<Double> samples = gaussianSamples(NUM_SAMPLES, null);
      double mean = r.nextDouble();
      for (int j = 0; j < samples.size(); ++j) {
        samples.set(j, samples.get(j) + mean);
      }
      estimators.add(createEstimator(targets, samples));
      allSamples.addAll(samples);
    }

    Collections.sort(allSamples);
    double actual1st = allSamples.get((int) (allSamples.size() * 0.01));
    double actual99th = allSamples.get((int) (allSamples.size() * 0.99));

    Percentiles merged;
    try {
      merged = Percentiles.merge(targets, estimators, RAW_DATA_SIZE);
    } catch (Percentiles.MergeException e) {
      fail();
      return;
    }

    assertEquals(actual1st, merged.getApproximateValue(0.01), 0.3);
    assertEquals(actual99th, merged.getApproximateValue(0.99), 0.3);
  }

  @Test
  public void importExportTest() {
    double[] targets = {0.5};
    Percentiles p = createGaussianEstimator(targets, NUM_SAMPLES, null);

    AndroidStudioStats.PercentileEstimator estimatorProto = p.export();

    Percentiles importedEstimator;
    try {
      importedEstimator = Percentiles.fromProto(estimatorProto, targets, RAW_DATA_SIZE);
    } catch (Percentiles.MismatchedTargetsException e) {
      fail();
      return;
    }

    assertEquals(p.getApproximateValue(0.5), importedEstimator.getApproximateValue(0.5), 0.0001);
  }
}
