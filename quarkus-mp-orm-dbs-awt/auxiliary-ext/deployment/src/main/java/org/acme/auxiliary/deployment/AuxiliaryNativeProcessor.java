package org.acme.auxiliary.deployment;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourcePatternsBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;

public class AuxiliaryNativeProcessor {

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem("auxiliary-native-support");
    }

    @BuildStep
    void registerForReflection(BuildProducer<ReflectiveClassBuildItem> reflection) {
        reflection.produce(ReflectiveClassBuildItem.builder(
                "java.lang.String",
                "org.apache.commons.logging.impl.LogFactoryImpl",
                "org.apache.commons.logging.LogFactory",
                "org.apache.commons.logging.impl.SimpleLog"
        ).methods(true).fields(true).constructors(true).build());
    }

    @BuildStep
    void registerResources(BuildProducer<NativeImageResourcePatternsBuildItem> resources) {
        resources.produce(NativeImageResourcePatternsBuildItem.builder()
                .includeGlobs(
                        "org/apache/pdfbox/resources/glyphlist/glyphlist.txt",
                        "org/apache/pdfbox/resources/glyphlist/zapfdingbats.txt",
                        "org/apache/fontbox/cmap/*",
                        "MyFreeMono.ttf",
                        "MyFreeSerif.ttf",
                        "quarkus-icon.png"
                ).build());
    }

    @BuildStep
    void registerRuntimeInitialization(BuildProducer<RuntimeInitializedClassBuildItem> runtimeInit) {
        final String[] classes = {
                "org.apache.fontbox.ttf.TTFParser",
                "org.apache.pdfbox.pdmodel.encryption.PublicKeySecurityHandler",
                "org.apache.pdfbox.pdmodel.font.FileSystemFontProvider$FSFontInfo",
                "org.apache.pdfbox.pdmodel.font.FontMapperImpl$DefaultFontProvider",
                "org.apache.pdfbox.pdmodel.font.FontMapperImpl",
                "org.apache.pdfbox.pdmodel.font.FontMappers$DefaultFontMapper",
                "org.apache.pdfbox.pdmodel.font.PDFont",
                "org.apache.pdfbox.pdmodel.font.PDFontLike",
                "org.apache.pdfbox.pdmodel.font.PDSimpleFont",
                "org.apache.pdfbox.pdmodel.font.PDType1Font",
                "org.apache.pdfbox.pdmodel.graphics.color.PDCIEDictionaryBasedColorSpace",
                "org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace",
                "org.apache.pdfbox.pdmodel.PDDocument",
                "org.apache.pdfbox.rendering.SoftMask"
        };
        for (String className : classes) {
            runtimeInit.produce(new RuntimeInitializedClassBuildItem(className));
        }
    }
}
