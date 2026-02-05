package com.kisanseva.ai.exception

import java.io.IOException

class ApiException(val code: Int, message: String) : IOException(message)