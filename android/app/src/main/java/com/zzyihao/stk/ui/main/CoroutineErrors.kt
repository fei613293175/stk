package com.zzyihao.stk.ui.main

import kotlinx.coroutines.CancellationException

internal fun Throwable.rethrowIfCancellation() {
    if (this is CancellationException) throw this
}
