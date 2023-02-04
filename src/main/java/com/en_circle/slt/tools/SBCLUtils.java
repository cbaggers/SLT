package com.en_circle.slt.tools;

import com.en_circle.slt.plugin.environment.SltProcessStreamGobbler;
import com.en_circle.slt.plugin.environment.SltProcessStreamGobbler.WaitForOccurrence;
import com.en_circle.slt.templates.VerifyTemplate;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.io.FileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SBCLUtils {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean verifyAndInstallDependencies(String executable, String core, String quicklisp, ProgressIndicator pi) {
        try {
            List<String> args = new ArrayList<>();
            args.add(executable);
            if (StringUtils.isNotBlank(core)) {
                args.add("--core");
                args.add(core);
            }
            args.add("--non-interactive");

            File tempTestFile = FileUtil.createTempFile("testSBCL", ".cl");
            if (tempTestFile.exists())
                tempTestFile.delete();
            FileUtils.writeStringToFile(tempTestFile, new VerifyTemplate(quicklisp).render(), StandardCharsets.UTF_8);
            tempTestFile.deleteOnExit();

            args.add("--load");
            args.add(tempTestFile.getAbsolutePath());

            try {
                ProcessBuilder processBuilder = new ProcessBuilder(args.toArray(new String[0]));
                Process process = processBuilder.start();

                StringBuilder returnValue = new StringBuilder();
                StringBuilder textValue = new StringBuilder();
                SltProcessStreamGobbler errorController = new SltProcessStreamGobbler(process.getErrorStream());
                SltProcessStreamGobbler outputController = new SltProcessStreamGobbler(process.getInputStream());
                errorController.addUpdateListener(returnValue::append);
                outputController.addUpdateListener(textValue::append);
                WaitForOccurrence waiter = new WaitForOccurrence("SltVerified");
                errorController.addUpdateListener(waiter);
                errorController.start();
                outputController.start();
                if (!waiter.awaitFor(null, errorController, 10, TimeUnit.MINUTES, pi::isCanceled)) {
                    if (process.isAlive())
                        process.destroy();

                    errorController.join();
                    outputController.join();
                    return false;
                }

                if (process.isAlive())
                    process.destroy();

                errorController.join();
                outputController.join();
                return returnValue.toString().contains("SltVerified");
            } finally {
                tempTestFile.delete();
            }
        } catch (Exception ignored) {
            return false;
        }
    }

}
