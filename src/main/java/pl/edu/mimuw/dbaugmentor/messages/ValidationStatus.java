package pl.edu.mimuw.dbaugmentor.messages;

public enum ValidationStatus {
    OK,
    MULTIPLIER {
        @Override
        public String toString() {
            return "Multiplier has to be at least 2";
        }
    },
    MULTIPLIER_INT {
        @Override
        public String toString() {
            return "Multiplier has to be an integer";
        }
    },
    DATABASE,
    TABLE {
        @Override
        public String toString() {
            return "Provided table to start from does not exist";
        }
    },
    NO_MISSING_FK_FILE {
        @Override
        public String toString() {
            return "Could not open the file with missing foreign keys";
        }
    },
    MISSING_FK_INVALID {
        @Override
        public String toString() {
            return "Could not parse the file with missing foreign keys";
        }
    },
    CACHE_READABLE_STRINGS {
        @Override
        public String toString() {
            return "Unique keys string values have to be either readable or cached";
        }
    },
    CACHE_OPTIMIZE_UNIQUE_KEY {
        @Override
        public String toString() {
            return "Unique key values have to be cached when search is not optimized";
        }
    }
}
