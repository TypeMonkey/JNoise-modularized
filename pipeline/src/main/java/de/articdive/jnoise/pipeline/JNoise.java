package de.articdive.jnoise.pipeline;

import de.articdive.jnoise.core.api.functions.Combiner;
import de.articdive.jnoise.core.api.functions.Interpolation;
import de.articdive.jnoise.core.api.modifiers.NoiseModifier;
import de.articdive.jnoise.core.api.noisegen.NoiseResult;
import de.articdive.jnoise.core.api.pipeline.ExplicitNoiseSource;
import de.articdive.jnoise.core.api.pipeline.NoiseSource;
import de.articdive.jnoise.core.api.pipeline.NoiseSourceBuilder;
import de.articdive.jnoise.core.api.transformers.DetailedTransformer;
import de.articdive.jnoise.core.api.transformers.SimpleTransformer;
import de.articdive.jnoise.core.util.vectors.Vector;
import de.articdive.jnoise.core.util.vectors.Vector2D;
import de.articdive.jnoise.core.util.vectors.Vector3D;
import de.articdive.jnoise.core.util.vectors.Vector4D;
import de.articdive.jnoise.generators.noise_parameters.distance_functions.DistanceFunction;
import de.articdive.jnoise.generators.noise_parameters.fade_functions.FadeFunction;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex2DVariant;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex3DVariant;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex4DVariant;
import de.articdive.jnoise.generators.noisegen.constant.ConstantNoiseGenerator;
import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator;
import de.articdive.jnoise.generators.noisegen.opensimplex.SuperSimplexNoiseGenerator;
import de.articdive.jnoise.generators.noisegen.perlin.PerlinNoiseGenerator;
import de.articdive.jnoise.generators.noisegen.random.gaussian.GaussianWhiteNoiseGenerator;
import de.articdive.jnoise.generators.noisegen.random.white.WhiteNoiseGenerator;
import de.articdive.jnoise.generators.noisegen.value.ValueNoiseGenerator;
import de.articdive.jnoise.generators.noisegen.worley.WorleyNoiseGenerator;
import de.articdive.jnoise.generators.noisegen.worley.WorleyNoiseResult;
import de.articdive.jnoise.modifiers.absolute_value.AbsoluteValueModifier;
import de.articdive.jnoise.modifiers.clamp.ClampModifier;
import de.articdive.jnoise.modifiers.inverter.InvertModifier;
import de.articdive.jnoise.modules.combination.CombinationModule;
import de.articdive.jnoise.modules.octavation.OctavationModule;
import de.articdive.jnoise.modules.octavation.fractal_functions.FractalFunction;
import de.articdive.jnoise.transformers.scale.ScaleTransformer;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.IntToLongFunction;

/**
 * Main class for the JNoise Pipeline.
 *
 * @author Articdive
 */
@NullMarked
public class JNoise implements NoiseSource {
    protected final SimpleTransformer[] simpleTransformers;
    protected final DetailedTransformer[] detailedTransformers;
    protected final NoiseModifier[] modifiers;
    private final NoiseSource source;

    JNoise(
        SimpleTransformer[] simpleTransformers,
        DetailedTransformer[] detailedTransformers,
        NoiseSource source,
        NoiseModifier[] modifiers
    ) {
        this.simpleTransformers = simpleTransformers;
        this.detailedTransformers = detailedTransformers;
        this.source = source;
        this.modifiers = modifiers;
    }

    @Override
    public double evaluateNoise(double x) {
        for (SimpleTransformer simpleTransformer : simpleTransformers) {
            x = simpleTransformer.transformX(x);
        }
        for (DetailedTransformer detailedTransformer : detailedTransformers) {
            x = detailedTransformer.transform(x);
        }
        double output = source.evaluateNoise(x);
        for (NoiseModifier modifier : modifiers) {
            output = modifier.apply(output);
        }
        return output;
    }

    @Override
    public double evaluateNoise(double x, double y) {
        for (SimpleTransformer simpleTransformer : simpleTransformers) {
            x = simpleTransformer.transformX(x);
            y = simpleTransformer.transformY(y);
        }
        for (DetailedTransformer detailedTransformer : detailedTransformers) {
            Vector2D vector2D = detailedTransformer.transform(x, y);
            x = vector2D.x();
            y = vector2D.y();
        }
        double output = source.evaluateNoise(x, y);
        for (NoiseModifier modifier : modifiers) {
            output = modifier.apply(output);
        }
        return output;
    }

    @Override
    public double evaluateNoise(double x, double y, double z) {
        for (SimpleTransformer simpleTransformer : simpleTransformers) {
            x = simpleTransformer.transformX(x);
            y = simpleTransformer.transformY(y);
            z = simpleTransformer.transformZ(z);
        }
        for (DetailedTransformer detailedTransformer : detailedTransformers) {
            Vector3D vector3D = detailedTransformer.transform(x, y, z);
            x = vector3D.x();
            y = vector3D.y();
            z = vector3D.z();
        }
        double output = source.evaluateNoise(x, y, z);
        for (NoiseModifier modifier : modifiers) {
            output = modifier.apply(output);
        }
        return output;
    }

    @Override
    public double evaluateNoise(double x, double y, double z, double w) {
        for (SimpleTransformer simpleTransformer : simpleTransformers) {
            x = simpleTransformer.transformX(x);
            y = simpleTransformer.transformY(y);
            z = simpleTransformer.transformZ(z);
            w = simpleTransformer.transformW(w);
        }
        for (DetailedTransformer detailedTransformer : detailedTransformers) {
            Vector4D vector4D = detailedTransformer.transform(x, y, z, w);
            x = vector4D.x();
            y = vector4D.y();
            z = vector4D.z();
            w = vector4D.w();
        }
        double output = source.evaluateNoise(x, y, z, w);
        for (NoiseModifier modifier : modifiers) {
            output = modifier.apply(output);
        }
        return output;
    }


    public static JNoiseBuilder<NoiseResult> newBuilder() {
        return new JNoiseBuilder<>();
    }

    @SuppressWarnings({"unchecked", "unused"})
    @NullMarked
    public static final class JNoiseBuilder<T extends NoiseResult> implements NoiseSourceBuilder {
        private final List<SimpleTransformer> simpleTransformers = new ArrayList<>();
        private final List<DetailedTransformer> detailedTransformers = new ArrayList<>();
        private final List<NoiseModifier> modifiers = new LinkedList<>();
        private NoiseSource source;

        private JNoiseBuilder() {

        }

        // SIMPLE TRANSFORMERS
        public JNoiseBuilder<T> addSimpleTransformer(SimpleTransformer transformer) {
            simpleTransformers.add(transformer);
            return this;
        }

        public JNoiseBuilder<T> scale(double factor) {
            simpleTransformers.add(new ScaleTransformer(factor));
            return this;
        }

        // DETAILED TRANSFORMERS
        public JNoiseBuilder<T> addDetailedTransformer(DetailedTransformer transformer) {
            detailedTransformers.add(transformer);
            return this;
        }

        // SOURCES
        public JNoiseBuilder<?> setNoiseSource(NoiseSource source) {
            this.source = source;
            return this;
        }

        public JNoiseBuilder<?> setNoiseSource(NoiseSourceBuilder sourceBuilder) {
            this.source = sourceBuilder.build();
            return this;
        }

        public <K extends NoiseResult> JNoiseBuilder<K> setNoiseSource(ExplicitNoiseSource<K> noiseGenerator) {
            this.source = noiseGenerator;
            return (JNoiseBuilder<K>) this;
        }

        public JNoiseBuilder<?> octavation(
            NoiseSource a,
            int octaves,
            double persistence,
            double lacunarity,
            FractalFunction fractalFunction,
            boolean incrementSeed) {
            return setNoiseSource(OctavationModule.newBuilder()
                .setNoiseSource(a)
                .setOctaves(octaves)
                .setGain(persistence)
                .setLacunarity(lacunarity)
                .setFractalFunction(fractalFunction)
                .setIncrementSeed(incrementSeed)
                .build()
            );
        }

        public JNoiseBuilder<?> octavation(OctavationModule.OctavationModuleBuilder builder) {
            return setNoiseSource(builder);
        }

        public JNoiseBuilder<?> octavation(OctavationModule module) {
            return setNoiseSource(module);
        }

        public JNoiseBuilder<?> octavate(
            int octaves,
            double gain,
            double lacunarity,
            FractalFunction fractalFunction,
            boolean incrementSeed) {
            if (source == null) {
                throw new IllegalArgumentException("Cannot octavate an empty noise source.");
            }
            return setNoiseSource(OctavationModule.newBuilder()
                .setNoiseSource(source)
                .setOctaves(octaves)
                .setGain(gain)
                .setLacunarity(lacunarity)
                .setFractalFunction(fractalFunction)
                .setIncrementSeed(incrementSeed)
                .build()
            );
        }

        public JNoiseBuilder<?> combination(NoiseSource a, NoiseSource b, Combiner combiner) {
            return setNoiseSource(CombinationModule.newBuilder()
                .setA(a)
                .setB(b)
                .setCombiner(combiner)
                .build()
            );
        }

        public JNoiseBuilder<?> combination(CombinationModule.CombinationModuleBuilder builder) {
            return setNoiseSource(builder);
        }

        public JNoiseBuilder<?> combination(CombinationModule module) {
            return setNoiseSource(module);
        }

        public JNoiseBuilder<?> combine(NoiseSource b, Combiner combiner) {
            if (source == null) {
                throw new IllegalArgumentException("Cannot combine to an empty noise source.");
            }
            return setNoiseSource(CombinationModule.newBuilder()
                .setA(source)
                .setB(b)
                .setCombiner(combiner)
                .build()
            );
        }

        public JNoiseBuilder<?> perlin(long seed, Interpolation interpolation, FadeFunction fadeFunction) {
            return setNoiseSource(
                PerlinNoiseGenerator.newBuilder().setSeed(seed).setInterpolation(interpolation).setFadeFunction(fadeFunction).build()
            );
        }

        public JNoiseBuilder<?> perlin(PerlinNoiseGenerator.PerlinNoiseBuilder builder) {
            return setNoiseSource(builder);
        }

        public JNoiseBuilder<?> perlin(PerlinNoiseGenerator generator) {
            return setNoiseSource(generator);
        }

        public JNoiseBuilder<?> fastSimplex(
            long seed,
            Simplex2DVariant variant2D,
            Simplex3DVariant variant3D,
            Simplex4DVariant variant4D
        ) {
            return setNoiseSource(
                FastSimplexNoiseGenerator.newBuilder().setSeed(seed)
                    .setVariant2D(variant2D)
                    .setVariant3D(variant3D)
                    .setVariant4D(variant4D)
                    .build()
            );
        }

        public JNoiseBuilder<?> fastSimplex(FastSimplexNoiseGenerator.FastSimplexNoiseBuilder builder) {
            return setNoiseSource(builder);
        }

        public JNoiseBuilder<?> fastSimplex(FastSimplexNoiseGenerator generator) {
            return setNoiseSource(generator);
        }

        public JNoiseBuilder<?> superSimplex(
            long seed,
            Simplex2DVariant variant2D,
            Simplex3DVariant variant3D,
            Simplex4DVariant variant4D
        ) {
            return setNoiseSource(SuperSimplexNoiseGenerator.newBuilder().setSeed(seed)
                .setVariant2D(variant2D)
                .setVariant3D(variant3D)
                .setVariant4D(variant4D)
                .build()
            );
        }

        public JNoiseBuilder<?> superSimplex(SuperSimplexNoiseGenerator.SuperSimplexNoiseBuilder builder) {
            return setNoiseSource(builder);
        }

        public JNoiseBuilder<?> superSimplex(SuperSimplexNoiseGenerator generator) {
            return setNoiseSource(generator);
        }


        public JNoiseBuilder<?> value(long seed, Interpolation interpolation, FadeFunction fadeFunction) {
            return setNoiseSource(
                ValueNoiseGenerator.newBuilder().setSeed(seed).setInterpolation(interpolation).setFadeFunction(fadeFunction).build()
            );
        }

        public JNoiseBuilder<?> value(ValueNoiseGenerator.ValueNoiseBuilder builder) {
            return setNoiseSource(builder);
        }

        public JNoiseBuilder<?> value(ValueNoiseGenerator generator) {
            return setNoiseSource(generator);
        }

        public JNoiseBuilder<?> white(long seed) {
            return setNoiseSource(WhiteNoiseGenerator.newBuilder().setSeed(seed).build());
        }

        public JNoiseBuilder<?> white(WhiteNoiseGenerator.WhiteNoiseBuilder builder) {
            return setNoiseSource(builder);
        }

        public JNoiseBuilder<?> white(WhiteNoiseGenerator generator) {
            return setNoiseSource(generator);
        }

        public JNoiseBuilder<?> gaussianWhite(long seed) {
            return setNoiseSource(GaussianWhiteNoiseGenerator.newBuilder().setSeed(seed).build());
        }

        public JNoiseBuilder<?> gaussianWhite(GaussianWhiteNoiseGenerator.GaussianWhiteNoiseBuilder builder) {
            return setNoiseSource(builder);
        }

        public JNoiseBuilder<?> gaussianWhite(GaussianWhiteNoiseGenerator generator) {
            return setNoiseSource(generator);
        }

        public JNoiseBuilder<WorleyNoiseResult<Vector>> worley(long seed, DistanceFunction distanceFunction, IntToLongFunction fpFunction) {
            this.source = WorleyNoiseGenerator.newBuilder().setSeed(seed).setDistanceFunction(distanceFunction).setFeaturePointAmountFunction(fpFunction).build();
            return (JNoiseBuilder<WorleyNoiseResult<Vector>>) this;
        }

        public JNoiseBuilder<WorleyNoiseResult<Vector>> worley(WorleyNoiseGenerator.WorleyNoiseBuilder builder) {
            this.source = builder.build();
            return (JNoiseBuilder<WorleyNoiseResult<Vector>>) this;
        }

        public JNoiseBuilder<WorleyNoiseResult<Vector>> worley(WorleyNoiseGenerator generator) {
            this.source = generator;
            return (JNoiseBuilder<WorleyNoiseResult<Vector>>) this;
        }

        public JNoiseBuilder<?> constant(double constant) {
            return setNoiseSource(ConstantNoiseGenerator.newBuilder().setConstant(constant).build());
        }

        public JNoiseBuilder<?> constant(ConstantNoiseGenerator.ConstantNoiseBuilder builder) {
            return setNoiseSource(builder);
        }

        public JNoiseBuilder<?> constant(ConstantNoiseGenerator generator) {
            return setNoiseSource(generator);
        }

        // MODIFIERS
        public JNoiseBuilder<T> addModifier(NoiseModifier noiseModifier) {
            modifiers.add(noiseModifier);
            return this;
        }

        public JNoiseBuilder<T> abs() {
            return addModifier(new AbsoluteValueModifier());
        }

        public JNoiseBuilder<T> clamp(double a, double b) {
            return addModifier(new ClampModifier(a, b));
        }

        public JNoiseBuilder<T> invert() {
            return addModifier(new InvertModifier());
        }

        @Override
        public JNoise build() {
            if (source == null) {
                throw new IllegalArgumentException("Source must be defined.");
            }
            return new JNoise(
                simpleTransformers.toArray(new SimpleTransformer[0]),
                detailedTransformers.toArray(new DetailedTransformer[0]),
                source,
                modifiers.toArray(new NoiseModifier[0])
            );
        }

        public JNoiseDetailed<T> buildDetailed() {
            if (source == null) {
                throw new IllegalArgumentException("Source must be defined.");
            }
            if (!(source instanceof ExplicitNoiseSource<?>)) {
                throw new IllegalArgumentException("To use explicit NoiseResults the generator must be explicit.");
            }
            return new JNoiseDetailed<>(
                simpleTransformers.toArray(new SimpleTransformer[0]),
                detailedTransformers.toArray(new DetailedTransformer[0]),
                (ExplicitNoiseSource<T>) source,
                modifiers.toArray(new NoiseModifier[0])
            );
        }

    }
}
