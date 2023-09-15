package metabank.exception

class NoSuchDatabaseException(val url: String?, cause: Throwable?): Exception(cause)