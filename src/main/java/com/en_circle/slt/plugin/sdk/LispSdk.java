package com.en_circle.slt.plugin.sdk;

import com.en_circle.slt.plugin.environment.Environment;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LispSdk implements PersistentStateComponent<LispSdk> {

    public String uuid;
    public String userName;

    // use getter!
    public Environment environment;

    // SBCL Process used
    public String sbclExecutable;
    public String sbclCorePath;
    public String quickLispPath;

    public Environment getEnvironment() {
        if (environment == null) {
            // backwards compat
            environment = Environment.SBCL_PROCESS;
        }
        return environment;
    }

    @Override
    public @Nullable LispSdk getState() {
        if (environment == null) {
            // backwards compat
            environment = Environment.SBCL_PROCESS;
        }
        return this;
    }

    @Override
    public void loadState(@NotNull LispSdk state) {
        XmlSerializerUtil.copyBean(state, this);
        if (environment == null) {
            // backwards compat
            environment = Environment.SBCL_PROCESS;
        }
    }

}
