package com.goldenrace.wallet.server.properties.logging;

import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

public class AppLog {

    private final String   format;
    private final Object[] args;

    public AppLog(String format, Object[] args) {
        this.format = format;
        this.args = args;
    }

    public static class Builder {

        private final String   id;
        private final Object[] idArgs;
        private       String   message;
        private       Object[] messageArgs      = new Object[0];
        private       String   causes;
        private       Object[] causesArgs       = new Object[0];
        private       String   consequences;
        private       Object[] consequencesArgs = new Object[0];

        private Builder(String id) {
            this.id = id;
            this.idArgs = new Object[0];
        }

        private Builder(String format, Object... args) {
            this.id = format;
            this.idArgs = args;
        }

        /**
         * Log identifier
         *
         * @param id
         * @return
         */
        public static Builder id(String id) {
            return new Builder(id);
        }

        /**
         * Log identifier
         *
         * @param format
         * @param args
         * @return
         */
        public static Builder id(String format, Object... args) {
            return new Builder(format, args);
        }

        /**
         * Message describing the log
         *
         * @param message
         * @return
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * Message describing the log
         *
         * @param format
         * @param args
         * @return
         */
        public Builder message(String format, Object... args) {
            this.message = format;
            this.messageArgs = args;
            return this;
        }

        /**
         * What caused the log
         *
         * @param causes
         * @return
         */
        public Builder causes(String causes) {
            this.causes = causes;
            return this;
        }

        /**
         * What caused the log
         *
         * @param format
         * @param args
         * @return
         */
        public Builder causes(String format, Object... args) {
            this.causes = format;
            this.causesArgs = args;
            return this;
        }

        /**
         * The possible consequences that can cause this log
         *
         * @param consequences
         * @return
         */
        public Builder consequences(String consequences) {
            this.consequences = consequences;
            return this;
        }

        /**
         * The possible consequences that can cause this log
         *
         * @param format
         * @param args
         * @return
         */
        public Builder consequences(String format, Object... args) {
            this.consequences = format;
            this.consequencesArgs = args;
            return this;
        }

        private void addArgs(Object[] args, Object[] typeArgs, int typeIndex) {
            for (int i = 0; i < typeArgs.length; i++) {
                args[i + typeIndex] = typeArgs[i];
            }
        }

        private void build(StringJoiner sj, Object[] args, String type, Object[] typeArgs, int typeIndex, String message) {
            if (Objects.nonNull(type)) {
                sj.add(new StringBuilder(message).append(type));
                addArgs(args, typeArgs, typeIndex);
            }
        }

        public AppLog build() {
            Object[] args = new Object[idArgs.length + messageArgs.length + causesArgs.length + consequencesArgs.length];
            String   id   = Optional.ofNullable(this.id).orElse("Unknown");
            StringBuilder formatBuilder = new StringBuilder("[")
                    .append(id)
                    .append("]");
            StringJoiner sj    = new StringJoiner(" | ");
            int          index = 0;
            addArgs(args, idArgs, index);
            index += idArgs.length;
            build(sj, args, message, messageArgs, index, "");
            index += messageArgs.length;
            build(sj, args, causes, causesArgs, index, "causes: ");
            index += causesArgs.length;
            build(sj, args, consequences, consequencesArgs, index, "consequences: ");
            String format;
            if (sj.length() == 0) {
                format = formatBuilder.toString();
            } else {
                format = formatBuilder.append(" ").append(sj.toString()).toString();
            }
            return new AppLog(format, args);
        }

    }

    public String getFormat() {
        return format;
    }

    public Object[] getArgs() {
        return args;
    }

}
