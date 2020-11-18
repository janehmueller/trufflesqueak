/*
 * Copyright (c) 2017-2020 Software Architecture Group, Hasso Plattner Institute
 *
 * Licensed under the MIT License.
 */
package de.hpi.swa.trufflesqueak;

import org.graalvm.options.OptionDescriptors;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.debug.DebuggerTags;
import com.oracle.truffle.api.instrumentation.ProvidedTags;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.source.Source;

import de.hpi.swa.trufflesqueak.image.SqueakImageContext;
import de.hpi.swa.trufflesqueak.interop.SqueakFileDetector;
import de.hpi.swa.trufflesqueak.shared.SqueakLanguageConfig;
import de.hpi.swa.trufflesqueak.util.MiscUtils;

@TruffleLanguage.Registration(//
                byteMimeTypes = SqueakLanguageConfig.MIME_TYPE, //
                characterMimeTypes = SqueakLanguageConfig.ST_MIME_TYPE, //
                defaultMimeType = SqueakLanguageConfig.ST_MIME_TYPE, //
                dependentLanguages = {"nfi"}, //
                fileTypeDetectors = SqueakFileDetector.class, //
                id = SqueakLanguageConfig.ID, //
                implementationName = SqueakLanguageConfig.IMPLEMENTATION_NAME, //
                interactive = true, //
                internal = false, //
                name = SqueakLanguageConfig.NAME, //
                version = SqueakLanguageConfig.VERSION)
@ProvidedTags({StandardTags.StatementTag.class, StandardTags.CallTag.class, StandardTags.RootTag.class, DebuggerTags.AlwaysHalt.class})
public final class SqueakLanguage extends TruffleLanguage<SqueakImageContext> {

    @Override
    protected SqueakImageContext createContext(final Env env) {
        return new SqueakImageContext(this, env);
    }

    @Override
    protected CallTarget parse(final ParsingRequest request) throws Exception {
        final SqueakImageContext image = getContext();
        final Source source = request.getSource();
        if (source.hasBytes()) {
            image.setImagePath(source.getPath());
            return image.getSqueakImage().asCallTarget();
        } else {
            image.ensureLoaded();
            if (source.isInternal()) {
                image.printToStdOut(MiscUtils.format("Evaluating '%s'...", source.getCharacters().toString()));
            }
            return Truffle.getRuntime().createCallTarget(image.getDoItContextNode(source));
        }
    }

    @Override
    protected boolean isThreadAccessAllowed(final Thread thread, final boolean singleThreaded) {
        return true; // TODO: Experimental, make TruffleSqueak work in multiple threads.
    }

    @Override
    protected Object getScope(final SqueakImageContext context) {
        return context.getScope();
    }

    public static SqueakImageContext getContext() {
        CompilerAsserts.neverPartOfCompilation();
        return getCurrentContext(SqueakLanguage.class);
    }

    public String getTruffleLanguageHome() {
        return getLanguageHome();
    }

    @Override
    protected OptionDescriptors getOptionDescriptors() {
        return SqueakOptions.createDescriptors();
    }

    @Override
    protected boolean patchContext(final SqueakImageContext context, final Env newEnv) {
        return context.patch(newEnv);
    }
}
