package edu.neu.ccs.prl.zeugma.internal.guidance;

import ec.util.MersenneTwister;
import edu.neu.ccs.prl.zeugma.internal.fuzz.GuidanceManager;
import edu.neu.ccs.prl.zeugma.internal.guidance.linked.LinkedIndividual;
import edu.neu.ccs.prl.zeugma.internal.guidance.linked.LinkedModifier;
import edu.neu.ccs.prl.zeugma.internal.guidance.modify.*;
import edu.neu.ccs.prl.zeugma.internal.util.IntProducer;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class ParametricGuidanceBuilder implements GuidanceBuilder {
    private final Random random = new MersenneTwister();
    private final int count = Integer.getInteger("zeugma.count", 4);
    private final String crossoverType = System.getProperty("zeugma.crossover");
    private final long duration;
    private final File outputDirectory;
    private final boolean breakTies = Boolean.getBoolean("zeugma.tiebreaker");

    public ParametricGuidanceBuilder(long duration, File outputDirectory) {
        this.duration = duration;
        this.outputDirectory = outputDirectory;
    }

    @Override
    public Guidance build(FuzzTarget target) throws IOException, ReflectiveOperationException {
        GuidanceManager manager = new GuidanceManager(target, outputDirectory, duration);
        Mutator mutator = new RegionMutator(random, new ShiftedGeometricSampler(random, 8));
        IntProducer countSampler = new ShiftedGeometricSampler(random, count);
        if (!"linked".equals(crossoverType)) {
            Splicer splicer;
            if (crossoverType == null) {
                splicer = null;
            } else if ("two_point".equals(crossoverType)) {
                splicer = new TwoPointSplicer(random, false);
            } else if ("one_point".equals(crossoverType)) {
                splicer = new OnePointSplicer(random, false);
            } else {
                throw new IllegalArgumentException("Invalid crossover type: " + crossoverType);
            }
            return new ParametricGuidance<>(target,
                    manager,
                    random,
                    new PopulationTracker<>(Individual::new, breakTies),
                    new BasicModifier(random, mutator, countSampler, splicer));
        } else {
            return new ParametricGuidance<>(target,
                    manager,
                    random,
                    new PopulationTracker<>(LinkedIndividual::new, breakTies),
                    new LinkedModifier<>(random, mutator, countSampler));
        }
    }
}