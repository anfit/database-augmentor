package pl.edu.mimuw.dbaugmentor.messages;

public enum Status {
    CONNECT {
        public String toString() {
            return "connect to database";
        }
    },

    SCHEMA{
        public String toString() {
            return "read database schema";
        }
    },
    DATA {
        public String toString() {
            return "read data";
        }
    },
    REFERENCES {
        public String toString() {
            return "add foreign key references";
        }
    },
    COPYING {
        public String toString() {
            return "copy entities";
        }
    },
    TRANSACTION {
        public String toString() {
            return "prepare final transaction";
        }
    },
    COMMIT {
        public String toString() {
            return "commit";
        }
    },
}

