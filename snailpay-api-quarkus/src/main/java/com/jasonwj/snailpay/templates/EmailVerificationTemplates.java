package com.jasonwj.snailpay.templates;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;

public class EmailVerificationTemplates {

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance verificationEmail(String name, String verificationUrl);
    }
}