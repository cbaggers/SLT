package com.en_circle.slt.plugin.swank.requests;

import com.en_circle.slt.plugin.lisp.lisp.LispContainer;
import com.en_circle.slt.plugin.lisp.lisp.LispElement;
import com.en_circle.slt.plugin.lisp.lisp.LispSymbol;
import com.en_circle.slt.plugin.swank.SlimeRequest;
import com.en_circle.slt.plugin.swank.SwankPacket;

import java.math.BigInteger;

public class SimpleCompletion extends SlimeRequest {

    public static SlimeRequest simpleCompletion(String prefix, String packageName, Callback callback) {
        return new SimpleCompletion(prefix, packageName, "CL-USER", callback);
    }

    protected final Callback callback;
    protected final String module;
    protected final String prefix;
    protected final String packageName;

    protected SimpleCompletion(String prefix, String packageName, String module, Callback callback) {
        this.callback = callback;
        this.module = module;
        this.prefix = prefix;
        this.packageName = packageName;
    }

    public void processReply(LispContainer data) {
        if (isOk(data)) {
            callback.onResult(data.getItems().get(1));
        }
    }

    private boolean isOk(LispContainer data) {
        return data.getItems().size() > 0 &&
                data.getItems().get(0) instanceof LispSymbol &&
                ":ok".equals(((LispSymbol) data.getItems().get(0)).getValue());
    }

    @Override
    public SwankPacket createPacket(BigInteger requestId) {
        return SwankPacket.simpleCompletion(prefix, packageName, module, requestId);
    }

    public interface Callback {
        void onResult(LispElement result);
    }

}
